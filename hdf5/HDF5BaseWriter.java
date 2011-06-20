/*
 * Copyright 2009 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.hdf5;

import static ch.systemsx.cisd.hdf5.HDF5Utils.DATATYPE_GROUP;
import static ch.systemsx.cisd.hdf5.HDF5Utils.TYPE_VARIANT_ATTRIBUTE;
import static ch.systemsx.cisd.hdf5.HDF5Utils.TYPE_VARIANT_DATA_TYPE;
import static ch.systemsx.cisd.hdf5.HDF5Utils.VARIABLE_LENGTH_STRING_DATA_TYPE;
import static ch.systemsx.cisd.hdf5.HDF5Utils.isEmpty;
import static ch.systemsx.cisd.hdf5.HDF5Utils.isNonPositive;
import static ch.systemsx.cisd.hdf5.hdf5lib.H5.H5Dwrite;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5P_DEFAULT;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5S_SCALAR;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5S_UNLIMITED;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import ncsa.hdf.hdf5lib.exceptions.HDF5DatasetInterfaceException;
import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;

import ch.systemsx.cisd.base.mdarray.MDArray;
import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator.FileFormat;
import ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator.SyncMode;
import ch.systemsx.cisd.hdf5.cleanup.ICallableWithCleanUp;
import ch.systemsx.cisd.hdf5.cleanup.ICleanUpRegistry;

/**
 * Class that provides base methods for reading and writing HDF5 files.
 * 
 * @author Bernd Rinn
 */
final class HDF5BaseWriter extends HDF5BaseReader
{

    private static final int SHUTDOWN_TIMEOUT_SECONDS = 60;

    private static final int MAX_TYPE_VARIANT_TYPES = 1024;

    private final static EnumSet<SyncMode> BLOCKING_SYNC_MODES = EnumSet.of(SyncMode.SYNC_BLOCK,
            SyncMode.SYNC_ON_FLUSH_BLOCK);

    private final static EnumSet<SyncMode> NON_BLOCKING_SYNC_MODES = EnumSet.of(SyncMode.SYNC,
            SyncMode.SYNC_ON_FLUSH);

    private final static EnumSet<SyncMode> SYNC_ON_CLOSE_MODES = EnumSet.of(SyncMode.SYNC_BLOCK,
            SyncMode.SYNC);

    /**
     * The size threshold for the COMPACT storage layout.
     */
    final static int COMPACT_LAYOUT_THRESHOLD = 256;

    /**
     * ExecutorService for calling <code>fsync(2)</code> in a non-blocking way.
     */
    private final static ExecutorService syncExecutor = new NamingThreadPoolExecutor("HDF5 Sync")
            .corePoolSize(3).daemonize();

    static
    {
        // Ensure all sync() calls are finished.
        Runtime.getRuntime().addShutdownHook(new Thread()
            {
                @Override
                public void run()
                {
                    syncExecutor.shutdownNow();
                    try
                    {
                        syncExecutor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    } catch (InterruptedException ex)
                    {
                        // Unexpected
                        ex.printStackTrace();
                    }
                }
            });
    }

    private final RandomAccessFile fileForSyncing;

    private enum Command
    {
        SYNC, CLOSE_ON_EXIT, CLOSE_SYNC, EXIT
    }

    private final BlockingQueue<Command> commandQueue;

    final boolean useExtentableDataTypes;

    final boolean overwriteFile;

    final boolean keepDataSetIfExists;

    final SyncMode syncMode;

    final FileFormat fileFormat;

    final int variableLengthStringDataTypeId;

    HDF5BaseWriter(File hdf5File, boolean performNumericConversions, boolean useUTF8CharEncoding,
            FileFormat fileFormat, boolean useExtentableDataTypes, boolean overwriteFile,
            boolean keepDataSetIfExists, SyncMode syncMode)
    {
        super(hdf5File, performNumericConversions, useUTF8CharEncoding, fileFormat, overwriteFile);
        try
        {
            this.fileForSyncing = new RandomAccessFile(hdf5File, "rw");
        } catch (FileNotFoundException ex)
        {
            // Should not be happening as openFile() was called in super()
            throw new HDF5JavaException("Cannot open RandomAccessFile: " + ex.getMessage());
        }
        this.fileFormat = fileFormat;
        this.useExtentableDataTypes = useExtentableDataTypes;
        this.overwriteFile = overwriteFile;
        this.keepDataSetIfExists = keepDataSetIfExists;
        this.syncMode = syncMode;
        readNamedDataTypes();
        variableLengthStringDataTypeId = openOrCreateVLStringType();
        commandQueue = new LinkedBlockingQueue<Command>();
        setupSyncThread();
    }

    private void setupSyncThread()
    {
        syncExecutor.execute(new Runnable()
            {
                public void run()
                {
                    while (true)
                    {
                        try
                        {
                            switch (commandQueue.take())
                            {
                                case SYNC:
                                    syncNow();
                                    break;
                                case CLOSE_ON_EXIT:
                                    closeNow();
                                    return;
                                case CLOSE_SYNC:
                                    closeSync();
                                    return;
                                case EXIT:
                                    return;
                            }
                        } catch (InterruptedException ex)
                        {
                            // Shutdown has been triggered by showdownNow(), add
                            // <code>CLOSEHDF</code> to queue.
                            // (Note that a close() on a closed RandomAccessFile is harmless.)
                            commandQueue.add(Command.CLOSE_ON_EXIT);
                        }
                    }
                }
            });
    }

    @Override
    int openFile(FileFormat fileFormatInit, boolean overwriteInit)
    {
        final boolean enforce_1_8 = (fileFormat == FileFormat.STRICTLY_1_8);
        if (hdf5File.exists() && overwriteInit == false)
        {
            if (hdf5File.canWrite() == false)
            {
                throw new HDF5JavaException("File " + this.hdf5File.getPath() + " not writable.");
            }
            return h5.openFileReadWrite(hdf5File.getPath(), enforce_1_8, fileRegistry);
        } else
        {
            final File directory = hdf5File.getParentFile();
            if (directory.exists() == false)
            {
                throw new HDF5JavaException("Directory '" + directory.getPath()
                        + "' does not exist.");
            }
            if (directory.canWrite() == false)
            {
                throw new HDF5JavaException("Directory " + directory.getPath() + " not writable.");
            }
            return h5.createFile(hdf5File.getPath(), enforce_1_8, fileRegistry);
        }
    }

    /**
     * Calls <code>fsync(2)</code> in the current thread.
     */
    private void syncNow()
    {
        try
        {
            // Implementation note 1: Unix will call fsync(), , Windows: FlushFileBuffers()
            // Implementation note 2: We do not call fileForSyncing.getChannel().force(false) which
            // might be better in terms of performance as if shutdownNow() already has been
            // triggered on the syncExecutor and thus this thread has already been interrupted,
            // channel methods would throw a ClosedByInterruptException at us no matter what we do.
            fileForSyncing.getFD().sync();
        } catch (IOException ex)
        {
            final String msg =
                    (ex.getMessage() == null) ? ex.getClass().getSimpleName() : ex.getMessage();
            throw new HDF5JavaException("Error syncing file: " + msg);
        }
    }

    /**
     * Closes and, depending on the sync mode, syncs the HDF5 file in the current thread.
     * <p>
     * To be called from the syncer thread only.
     */
    synchronized private void closeNow()
    {
        if (state == State.OPEN)
        {
            super.close();
            if (SYNC_ON_CLOSE_MODES.contains(syncMode))
            {
                syncNow();
            }
            closeSync();
        }
    }

    private void closeSync()
    {
        try
        {
            fileForSyncing.close();
        } catch (IOException ex)
        {
            throw new HDF5JavaException("Error closing file: " + ex.getMessage());
        }
    }

    synchronized void flush()
    {
        h5.flushFile(fileId);
        if (NON_BLOCKING_SYNC_MODES.contains(syncMode))
        {
            commandQueue.add(Command.SYNC);
        } else if (BLOCKING_SYNC_MODES.contains(syncMode))
        {
            syncNow();
        }
    }

    synchronized void flushSyncBlocking()
    {
        h5.flushFile(fileId);
        syncNow();
    }

    @Override
    synchronized void close()
    {
        if (state == State.OPEN)
        {
            super.close();
            if (SyncMode.SYNC == syncMode)
            {
                commandQueue.add(Command.SYNC);
            } else if (SyncMode.SYNC_BLOCK == syncMode)
            {
                syncNow();
            }

            if (EnumSet.complementOf(NON_BLOCKING_SYNC_MODES).contains(syncMode))
            {
                closeSync();
                commandQueue.add(Command.EXIT);
            } else
            {
                // End syncer thread and avoid a race condition for non-blocking sync modes as the
                // syncer thread still may want to use the fileForSynching
                commandQueue.add(Command.CLOSE_SYNC);
            }
        }
    }

    @Override
    void commitDataType(final String dataTypePath, final int dataTypeId)
    {
        h5.commitDataType(fileId, dataTypePath, dataTypeId);
    }

    HDF5EnumerationType openOrCreateTypeVariantDataType(final HDF5Writer writer)
    {
        final HDF5EnumerationType dataType;
        int dataTypeId = getDataTypeId(HDF5Utils.TYPE_VARIANT_DATA_TYPE);
        if (dataTypeId < 0
                || h5.getNumberOfMembers(dataTypeId) < HDF5DataTypeVariant.values().length)
        {
            final String typeVariantPath = findFirstUnusedTypeVariantPath(writer);
            dataType = createTypeVariantDataType();
            commitDataType(typeVariantPath, dataType.getStorageTypeId());
            writer.createOrUpdateSoftLink(typeVariantPath.substring(DATATYPE_GROUP.length() + 1),
                    TYPE_VARIANT_DATA_TYPE);
        } else
        {
            final int nativeDataTypeId = h5.getNativeDataType(dataTypeId, fileRegistry);
            final String[] typeVariantNames = h5.getNamesForEnumOrCompoundMembers(dataTypeId);
            dataType =
                    new HDF5EnumerationType(fileId, dataTypeId, nativeDataTypeId,
                            TYPE_VARIANT_DATA_TYPE, typeVariantNames);

        }
        return dataType;
    }

    void setEnumArrayAttribute(final String objectPath, final String name,
            final HDF5EnumerationValueArray value)
    {
        assert objectPath != null;
        assert name != null;
        assert value != null;

        checkOpen();
        final ICallableWithCleanUp<Void> setAttributeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final int baseMemoryTypeId = value.getType().getNativeTypeId();
                    final int memoryTypeId =
                            h5.createArrayType(baseMemoryTypeId, value.getLength(), registry);
                    final int baseStorageTypeId = value.getType().getStorageTypeId();
                    final int storageTypeId =
                            h5.createArrayType(baseStorageTypeId, value.getLength(), registry);
                    setAttribute(objectPath, name, storageTypeId, memoryTypeId,
                            value.toStorageForm());
                    return null; // Nothing to return.
                }
            };
        runner.call(setAttributeRunnable);
    }

    private String findFirstUnusedTypeVariantPath(final HDF5Reader reader)
    {
        int number = 0;
        String path;
        do
        {
            path = TYPE_VARIANT_DATA_TYPE + "." + (number++);
        } while (reader.exists(path, false) && number < MAX_TYPE_VARIANT_TYPES);
        return path;
    }

    private int openOrCreateVLStringType()
    {
        int dataTypeId = getDataTypeId(HDF5Utils.VARIABLE_LENGTH_STRING_DATA_TYPE);
        if (dataTypeId < 0)
        {
            dataTypeId = h5.createDataTypeVariableString(fileRegistry);
            commitDataType(VARIABLE_LENGTH_STRING_DATA_TYPE, dataTypeId);
        }
        return dataTypeId;
    }

    /**
     * Write a scalar value provided as <code>byte[]</code>.
     */
    void writeScalar(final String dataSetPath, final int storageDataTypeId,
            final int nativeDataTypeId, final byte[] value)
    {
        assert dataSetPath != null;
        assert storageDataTypeId >= 0;
        assert nativeDataTypeId >= 0;
        assert value != null;

        final ICallableWithCleanUp<Object> writeScalarRunnable = new ICallableWithCleanUp<Object>()
            {
                public Object call(ICleanUpRegistry registry)
                {
                    writeScalar(dataSetPath, storageDataTypeId, nativeDataTypeId, value, true,
                            keepDataSetIfExists, registry);
                    return null; // Nothing to return.
                }
            };
        runner.call(writeScalarRunnable);
    }

    /**
     * Internal method for writing a scalar value provided as <code>byte[]</code>.
     */
    int writeScalar(final String dataSetPath, final int storageDataTypeId,
            final int nativeDataTypeId, final byte[] value, final boolean compactLayout,
            final boolean keepDatasetIfExists, ICleanUpRegistry registry)
    {
        final int dataSetId;
        boolean exists = h5.exists(fileId, dataSetPath);
        if (exists && keepDatasetIfExists == false)
        {
            h5.deleteObject(fileId, dataSetPath);
            exists = false;
        }
        if (exists)
        {
            dataSetId = h5.openObject(fileId, dataSetPath, registry);
        } else
        {
            dataSetId =
                    h5.createScalarDataSet(fileId, storageDataTypeId, dataSetPath, compactLayout,
                            registry);
        }
        H5Dwrite(dataSetId, nativeDataTypeId, H5S_SCALAR, H5S_SCALAR, H5P_DEFAULT, value);
        return dataSetId;
    }

    /**
     * Write a scalar value provided as <code>byte</code>.
     */
    void writeScalar(final String dataSetPath, final int storageDataTypeId,
            final int nativeDataTypeId, final byte value)
    {
        assert dataSetPath != null;
        assert storageDataTypeId >= 0;
        assert nativeDataTypeId >= 0;

        final ICallableWithCleanUp<Object> writeScalarRunnable = new ICallableWithCleanUp<Object>()
            {
                public Object call(ICleanUpRegistry registry)
                {
                    writeScalar(dataSetPath, storageDataTypeId, nativeDataTypeId, value, true,
                            true, registry);
                    return null; // Nothing to return.
                }
            };
        runner.call(writeScalarRunnable);
    }

    /**
     * Internal method for writing a scalar value provided as <code>byte</code>.
     */
    int writeScalar(final String dataSetPath, final int storageDataTypeId,
            final int nativeDataTypeId, final byte value, final boolean compactLayout,
            final boolean keepDatasetIfExists, ICleanUpRegistry registry)
    {
        final int dataSetId;
        boolean exists = h5.exists(fileId, dataSetPath);
        if (exists && keepDatasetIfExists == false)
        {
            h5.deleteObject(fileId, dataSetPath);
            exists = false;
        }
        if (exists)
        {
            dataSetId = h5.openObject(fileId, dataSetPath, registry);
        } else
        {
            dataSetId =
                    h5.createScalarDataSet(fileId, storageDataTypeId, dataSetPath, compactLayout,
                            registry);
        }
        H5Dwrite(dataSetId, nativeDataTypeId, H5S_SCALAR, H5S_SCALAR, H5P_DEFAULT, new byte[]
            { value });
        return dataSetId;
    }

    /**
     * Write a scalar value provided as <code>short</code>.
     */
    void writeScalar(final String dataSetPath, final int storageDataTypeId,
            final int nativeDataTypeId, final short value)
    {
        assert dataSetPath != null;
        assert storageDataTypeId >= 0;
        assert nativeDataTypeId >= 0;

        final ICallableWithCleanUp<Object> writeScalarRunnable = new ICallableWithCleanUp<Object>()
            {
                public Object call(ICleanUpRegistry registry)
                {
                    writeScalar(dataSetPath, storageDataTypeId, nativeDataTypeId, value, true,
                            true, registry);
                    return null; // Nothing to return.
                }
            };
        runner.call(writeScalarRunnable);
    }

    /**
     * Internal method for writing a scalar value provided as <code>short</code>.
     */
    int writeScalar(final String dataSetPath, final int storageDataTypeId,
            final int nativeDataTypeId, final short value, final boolean compactLayout,
            final boolean keepDatasetIfExists, ICleanUpRegistry registry)
    {
        final int dataSetId;
        boolean exists = h5.exists(fileId, dataSetPath);
        if (exists && keepDatasetIfExists == false)
        {
            h5.deleteObject(fileId, dataSetPath);
            exists = false;
        }
        if (exists)
        {
            dataSetId = h5.openObject(fileId, dataSetPath, registry);
        } else
        {
            dataSetId =
                    h5.createScalarDataSet(fileId, storageDataTypeId, dataSetPath, compactLayout,
                            registry);
        }
        H5Dwrite(dataSetId, nativeDataTypeId, H5S_SCALAR, H5S_SCALAR, H5P_DEFAULT, new short[]
            { value });
        return dataSetId;
    }

    /**
     * Write a scalar value provided as <code>int</code>.
     */
    void writeScalar(final String dataSetPath, final int storageDataTypeId,
            final int nativeDataTypeId, final int value)
    {
        assert dataSetPath != null;
        assert storageDataTypeId >= 0;
        assert nativeDataTypeId >= 0;

        final ICallableWithCleanUp<Object> writeScalarRunnable = new ICallableWithCleanUp<Object>()
            {
                public Object call(ICleanUpRegistry registry)
                {
                    writeScalar(dataSetPath, storageDataTypeId, nativeDataTypeId, value, true,
                            true, registry);
                    return null; // Nothing to return.
                }
            };
        runner.call(writeScalarRunnable);
    }

    /**
     * Internal method for writing a scalar value provided as <code>int</code>.
     */
    int writeScalar(final String dataSetPath, final int storageDataTypeId,
            final int nativeDataTypeId, final int value, final boolean compactLayout,
            final boolean keepDatasetIfExists, ICleanUpRegistry registry)
    {
        final int dataSetId;
        boolean exists = h5.exists(fileId, dataSetPath);
        if (exists && keepDatasetIfExists == false)
        {
            h5.deleteObject(fileId, dataSetPath);
            exists = false;
        }
        if (exists)
        {
            dataSetId = h5.openObject(fileId, dataSetPath, registry);
        } else
        {
            dataSetId =
                    h5.createScalarDataSet(fileId, storageDataTypeId, dataSetPath, compactLayout,
                            registry);
        }
        H5Dwrite(dataSetId, nativeDataTypeId, H5S_SCALAR, H5S_SCALAR, H5P_DEFAULT, new int[]
            { value });
        return dataSetId;
    }

    /**
     * Write a scalar value provided as <code>long</code>.
     */
    void writeScalar(final String dataSetPath, final int storageDataTypeId,
            final int nativeDataTypeId, final long value)
    {
        assert dataSetPath != null;
        assert storageDataTypeId >= 0;
        assert nativeDataTypeId >= 0;

        final ICallableWithCleanUp<Object> writeScalarRunnable = new ICallableWithCleanUp<Object>()
            {
                public Object call(ICleanUpRegistry registry)
                {
                    writeScalar(dataSetPath, storageDataTypeId, nativeDataTypeId, value, true,
                            true, registry);
                    return null; // Nothing to return.
                }
            };
        runner.call(writeScalarRunnable);
    }

    /**
     * Internal method for writing a scalar value provided as <code>long</code>.
     */
    int writeScalar(final String dataSetPath, final int storageDataTypeId,
            final int nativeDataTypeId, final long value, final boolean compactLayout,
            final boolean keepDatasetIfExists, ICleanUpRegistry registry)
    {
        final int dataSetId;
        boolean exists = h5.exists(fileId, dataSetPath);
        if (exists && keepDatasetIfExists == false)
        {
            h5.deleteObject(fileId, dataSetPath);
            exists = false;
        }
        if (exists)
        {
            dataSetId = h5.openObject(fileId, dataSetPath, registry);
        } else
        {
            dataSetId =
                    h5.createScalarDataSet(fileId, storageDataTypeId, dataSetPath, compactLayout,
                            registry);
        }
        H5Dwrite(dataSetId, nativeDataTypeId, H5S_SCALAR, H5S_SCALAR, H5P_DEFAULT, new long[]
            { value });
        return dataSetId;
    }

    /**
     * Write a scalar value provided as <code>float</code>.
     */
    void writeScalar(final String dataSetPath, final int storageDataTypeId,
            final int nativeDataTypeId, final float value)
    {
        assert dataSetPath != null;
        assert storageDataTypeId >= 0;
        assert nativeDataTypeId >= 0;

        final ICallableWithCleanUp<Object> writeScalarRunnable = new ICallableWithCleanUp<Object>()
            {
                public Object call(ICleanUpRegistry registry)
                {
                    writeScalar(dataSetPath, storageDataTypeId, nativeDataTypeId, value, true,
                            keepDataSetIfExists, registry);
                    return null; // Nothing to return.
                }
            };
        runner.call(writeScalarRunnable);
    }

    /**
     * Internal method for writing a scalar value provided as <code>float</code>.
     */
    int writeScalar(final String dataSetPath, final int storageDataTypeId,
            final int nativeDataTypeId, final float value, final boolean compactLayout,
            final boolean keepDatasetIfExists, ICleanUpRegistry registry)
    {
        final int dataSetId;
        boolean exists = h5.exists(fileId, dataSetPath);
        if (exists && keepDatasetIfExists == false)
        {
            h5.deleteObject(fileId, dataSetPath);
            exists = false;
        }
        if (exists)
        {
            dataSetId = h5.openObject(fileId, dataSetPath, registry);
        } else
        {
            dataSetId =
                    h5.createScalarDataSet(fileId, storageDataTypeId, dataSetPath, compactLayout,
                            registry);
        }
        H5Dwrite(dataSetId, nativeDataTypeId, H5S_SCALAR, H5S_SCALAR, H5P_DEFAULT, new float[]
            { value });
        return dataSetId;
    }

    /**
     * Write a scalar value provided as <code>double</code>.
     */
    void writeScalar(final String dataSetPath, final int storageDataTypeId,
            final int nativeDataTypeId, final double value)
    {
        assert dataSetPath != null;
        assert storageDataTypeId >= 0;
        assert nativeDataTypeId >= 0;

        final ICallableWithCleanUp<Object> writeScalarRunnable = new ICallableWithCleanUp<Object>()
            {
                public Object call(ICleanUpRegistry registry)
                {
                    writeScalar(dataSetPath, storageDataTypeId, nativeDataTypeId, value, true,
                            true, registry);
                    return null; // Nothing to return.
                }
            };
        runner.call(writeScalarRunnable);
    }

    /**
     * Internal method for writing a scalar value provided as <code>double</code>.
     */
    int writeScalar(final String dataSetPath, final int storageDataTypeId,
            final int nativeDataTypeId, final double value, final boolean compactLayout,
            final boolean keepDatasetIfExists, ICleanUpRegistry registry)
    {
        final int dataSetId;
        boolean exists = h5.exists(fileId, dataSetPath);
        if (exists && keepDatasetIfExists == false)
        {
            h5.deleteObject(fileId, dataSetPath);
            exists = false;
        }
        if (exists)
        {
            dataSetId = h5.openObject(fileId, dataSetPath, registry);
        } else
        {
            dataSetId =
                    h5.createScalarDataSet(fileId, storageDataTypeId, dataSetPath, compactLayout,
                            registry);
        }
        H5Dwrite(dataSetId, nativeDataTypeId, H5S_SCALAR, H5S_SCALAR, H5P_DEFAULT, new double[]
            { value });
        return dataSetId;
    }

    /**
     * Writes a variable-length string array data set.
     */
    void writeStringVL(int dataSetId, int memorySpaceId, int fileSpaceId, String[] value)
    {
        h5.writeStringVL(dataSetId, variableLengthStringDataTypeId, memorySpaceId, fileSpaceId,
                value);
    }

    /**
     * Writes a variable-length string array data set.
     */
    void writeStringVL(int dataSetId, String[] value)
    {
        h5.writeStringVL(dataSetId, variableLengthStringDataTypeId, value);
    }

    /**
     * Writes a variable-length string array attribute.
     */
    void writeAttributeStringVL(int attributeId, String[] value)
    {
        h5.writeAttributeStringVL(attributeId, variableLengthStringDataTypeId, value);
    }

    /**
     * Creates a data set.
     */
    int createDataSet(final String objectPath, final int storageDataTypeId,
            final HDF5AbstractStorageFeatures features, final long[] dimensions,
            final long[] chunkSizeOrNull, int elementLength, final ICleanUpRegistry registry)
    {
        final int dataSetId;
        final boolean empty = isEmpty(dimensions);
        final boolean chunkSizeProvided =
                (chunkSizeOrNull != null && isNonPositive(chunkSizeOrNull) == false);
        final long[] definitiveChunkSizeOrNull;
        if (h5.exists(fileId, objectPath))
        {
            if (keepDataIfExists(features))
            {
                return h5.openDataSet(fileId, objectPath, registry);
            }
            h5.deleteObject(fileId, objectPath);
        }
        if (empty)
        {
            definitiveChunkSizeOrNull =
                    chunkSizeProvided ? chunkSizeOrNull : HDF5Utils.tryGetChunkSize(dimensions,
                            elementLength, features.requiresChunking(), true);
        } else if (features.tryGetProposedLayout() == HDF5StorageLayout.COMPACT
                || features.tryGetProposedLayout() == HDF5StorageLayout.CONTIGUOUS
                || (useExtentableDataTypes == false) && features.requiresChunking() == false)
        {
            definitiveChunkSizeOrNull = null;
        } else if (chunkSizeProvided)
        {
            definitiveChunkSizeOrNull = chunkSizeOrNull;
        } else
        {
            definitiveChunkSizeOrNull =
                    HDF5Utils
                            .tryGetChunkSize(
                                    dimensions,
                                    elementLength,
                                    features.requiresChunking(),
                                    useExtentableDataTypes
                                            || features.tryGetProposedLayout() == HDF5StorageLayout.CHUNKED);
        }
        final HDF5StorageLayout layout =
                determineLayout(storageDataTypeId, dimensions, definitiveChunkSizeOrNull,
                        features.tryGetProposedLayout());
        dataSetId =
                h5.createDataSet(fileId, dimensions, definitiveChunkSizeOrNull, storageDataTypeId,
                        features, objectPath, layout, fileFormat, registry);
        return dataSetId;
    }

    boolean keepDataIfExists(final HDF5AbstractStorageFeatures features)
    {
        return (features.isKeepDataSetIfExists() || keepDataSetIfExists)
                && (features.isDeleteDataSetIfExists() == false);
    }

    /**
     * Determine which {@link HDF5StorageLayout} to use for the given <var>storageDataTypeId</var>.
     */
    HDF5StorageLayout determineLayout(final int storageDataTypeId, final long[] dimensions,
            final long[] chunkSizeOrNull, final HDF5StorageLayout proposedLayoutOrNull)
    {
        if (chunkSizeOrNull != null)
        {
            return HDF5StorageLayout.CHUNKED;
        }
        if (proposedLayoutOrNull != null)
        {
            return proposedLayoutOrNull;
        }
        if (computeSizeForDimensions(storageDataTypeId, dimensions) < HDF5BaseWriter.COMPACT_LAYOUT_THRESHOLD)
        {
            return HDF5StorageLayout.COMPACT;
        }
        return HDF5StorageLayout.CONTIGUOUS;
    }

    private int computeSizeForDimensions(int dataTypeId, long[] dimensions)
    {
        int size = h5.getDataTypeSize(dataTypeId);
        for (long d : dimensions)
        {
            size *= d;
        }
        return size;
    }

    /**
     * Checks whether the given <var>dimensions</var> are in bounds for <var>dataSetId</var>.
     */
    boolean areDimensionsInBounds(final int dataSetId, final long[] dimensions)
    {
        final long[] maxDimensions = h5.getDataMaxDimensions(dataSetId);

        if (dimensions.length != maxDimensions.length) // Actually an error condition
        {
            return false;
        }

        for (int i = 0; i < dimensions.length; ++i)
        {
            if (maxDimensions[i] != H5S_UNLIMITED && dimensions[i] > maxDimensions[i])
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the data set id for the given <var>objectPath</var>. If the data sets exists, it
     * depends on the <code>features</code> and on the status of <code>keepDataSetIfExists</code>
     * whether the existing data set will be opened or whether the data set will be deleted and
     * re-created.
     */
    int getOrCreateDataSetId(final String objectPath, final int storageDataTypeId,
            final long[] dimensions, int elementLength, final HDF5AbstractStorageFeatures features,
            ICleanUpRegistry registry)
    {
        final int dataSetId;
        boolean exists = h5.exists(fileId, objectPath);
        if (exists && keepDataIfExists(features) == false)
        {
            h5.deleteObject(fileId, objectPath);
            exists = false;
        }
        if (exists)
        {
            dataSetId =
                    h5.openAndExtendDataSet(fileId, objectPath, fileFormat, dimensions,
                            storageDataTypeId, registry);
        } else
        {
            dataSetId =
                    createDataSet(objectPath, storageDataTypeId, features, dimensions, null,
                            elementLength, registry);
        }
        return dataSetId;
    }

    void setDataSetDimensions(final String objectPath, final long[] newDimensions,
            ICleanUpRegistry registry)
    {
        assert newDimensions != null;

        final int dataSetId = h5.openDataSet(fileId, objectPath, registry);
        try
        {
            h5.setDataSetExtentChunked(dataSetId, newDimensions);
        } catch (HDF5DatasetInterfaceException ex)
        {
            if (HDF5StorageLayout.CHUNKED != h5.getLayout(dataSetId, registry))
            {
                throw new HDF5JavaException("Cannot change dimensions of non-extendable data set.");
            } else
            {
                throw ex;
            }
        }
    }

    //
    // Attributes
    //

    void setAttribute(final String objectPath, final String name, final int storageDataTypeId,
            final int nativeDataTypeId, final byte[] value)
    {
        assert objectPath != null;
        assert name != null;
        assert storageDataTypeId >= 0;
        assert nativeDataTypeId >= 0;
        assert value != null;

        final ICallableWithCleanUp<Object> addAttributeRunnable =
                new ICallableWithCleanUp<Object>()
                    {
                        public Object call(ICleanUpRegistry registry)
                        {
                            final int objectId = h5.openObject(fileId, objectPath, registry);
                            setAttribute(objectId, name, storageDataTypeId, nativeDataTypeId,
                                    value, registry);
                            return null; // Nothing to return.
                        }
                    };
        runner.call(addAttributeRunnable);
    }

    void setAttribute(final int objectId, final String name, final int storageDataTypeId,
            final int nativeDataTypeId, final byte[] value, ICleanUpRegistry registry)
    {
        int attributeId;
        if (h5.existsAttribute(objectId, name))
        {
            attributeId = h5.openAttribute(objectId, name, registry);
            final int oldStorageDataTypeId = h5.getDataTypeForAttribute(attributeId, registry);
            if (h5.dataTypesAreEqual(oldStorageDataTypeId, storageDataTypeId) == false)
            {
                h5.deleteAttribute(objectId, name);
                attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
            }
        } else
        {
            attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
        }
        h5.writeAttribute(attributeId, nativeDataTypeId, value);
    }

    void setAttribute(final String objectPath, final String name, final int storageDataTypeId,
            final int nativeDataTypeId, final short[] value)
    {
        assert objectPath != null;
        assert name != null;
        assert storageDataTypeId >= 0;
        assert nativeDataTypeId >= 0;
        assert value != null;

        final ICallableWithCleanUp<Object> addAttributeRunnable =
                new ICallableWithCleanUp<Object>()
                    {
                        public Object call(ICleanUpRegistry registry)
                        {
                            final int objectId = h5.openObject(fileId, objectPath, registry);
                            setAttribute(objectId, name, storageDataTypeId, nativeDataTypeId,
                                    value, registry);
                            return null; // Nothing to return.
                        }
                    };
        runner.call(addAttributeRunnable);
    }

    void setAttribute(final int objectId, final String name, final int storageDataTypeId,
            final int nativeDataTypeId, final short[] value, ICleanUpRegistry registry)
    {
        int attributeId;
        if (h5.existsAttribute(objectId, name))
        {
            attributeId = h5.openAttribute(objectId, name, registry);
            final int oldStorageDataTypeId = h5.getDataTypeForAttribute(attributeId, registry);
            if (h5.dataTypesAreEqual(oldStorageDataTypeId, storageDataTypeId) == false)
            {
                h5.deleteAttribute(objectId, name);
                attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
            }
        } else
        {
            attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
        }
        h5.writeAttribute(attributeId, nativeDataTypeId, value);
    }

    void setAttribute(final String objectPath, final String name, final int storageDataTypeId,
            final int nativeDataTypeId, final int[] value)
    {
        assert objectPath != null;
        assert name != null;
        assert storageDataTypeId >= 0;
        assert nativeDataTypeId >= 0;
        assert value != null;

        final ICallableWithCleanUp<Object> addAttributeRunnable =
                new ICallableWithCleanUp<Object>()
                    {
                        public Object call(ICleanUpRegistry registry)
                        {
                            final int objectId = h5.openObject(fileId, objectPath, registry);
                            setAttribute(objectId, name, storageDataTypeId, nativeDataTypeId,
                                    value, registry);
                            return null; // Nothing to return.
                        }
                    };
        runner.call(addAttributeRunnable);
    }

    void setAttribute(final int objectId, final String name, final int storageDataTypeId,
            final int nativeDataTypeId, final int[] value, ICleanUpRegistry registry)
    {
        int attributeId;
        if (h5.existsAttribute(objectId, name))
        {
            attributeId = h5.openAttribute(objectId, name, registry);
            final int oldStorageDataTypeId = h5.getDataTypeForAttribute(attributeId, registry);
            if (h5.dataTypesAreEqual(oldStorageDataTypeId, storageDataTypeId) == false)
            {
                h5.deleteAttribute(objectId, name);
                attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
            }
        } else
        {
            attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
        }
        h5.writeAttribute(attributeId, nativeDataTypeId, value);
    }

    void setAttribute(final String objectPath, final String name, final int storageDataTypeId,
            final int nativeDataTypeId, final long[] value)
    {
        assert objectPath != null;
        assert name != null;
        assert storageDataTypeId >= 0;
        assert nativeDataTypeId >= 0;
        assert value != null;

        final ICallableWithCleanUp<Object> addAttributeRunnable =
                new ICallableWithCleanUp<Object>()
                    {
                        public Object call(ICleanUpRegistry registry)
                        {
                            final int objectId = h5.openObject(fileId, objectPath, registry);
                            setAttribute(objectId, name, storageDataTypeId, nativeDataTypeId,
                                    value, registry);
                            return null; // Nothing to return.
                        }
                    };
        runner.call(addAttributeRunnable);
    }

    void setAttribute(final int objectId, final String name, final int storageDataTypeId,
            final int nativeDataTypeId, final long[] value, ICleanUpRegistry registry)
    {
        int attributeId;
        if (h5.existsAttribute(objectId, name))
        {
            attributeId = h5.openAttribute(objectId, name, registry);
            final int oldStorageDataTypeId = h5.getDataTypeForAttribute(attributeId, registry);
            if (h5.dataTypesAreEqual(oldStorageDataTypeId, storageDataTypeId) == false)
            {
                h5.deleteAttribute(objectId, name);
                attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
            }
        } else
        {
            attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
        }
        h5.writeAttribute(attributeId, nativeDataTypeId, value);
    }

    void setAttribute(final String objectPath, final String name, final int storageDataTypeId,
            final int nativeDataTypeId, final float[] value)
    {
        assert objectPath != null;
        assert name != null;
        assert storageDataTypeId >= 0;
        assert nativeDataTypeId >= 0;
        assert value != null;

        final ICallableWithCleanUp<Object> addAttributeRunnable =
                new ICallableWithCleanUp<Object>()
                    {
                        public Object call(ICleanUpRegistry registry)
                        {
                            final int objectId = h5.openObject(fileId, objectPath, registry);
                            setAttribute(objectId, name, storageDataTypeId, nativeDataTypeId,
                                    value, registry);
                            return null; // Nothing to return.
                        }
                    };
        runner.call(addAttributeRunnable);
    }

    void setAttribute(final int objectId, final String name, final int storageDataTypeId,
            final int nativeDataTypeId, final float[] value, ICleanUpRegistry registry)
    {
        int attributeId;
        if (h5.existsAttribute(objectId, name))
        {
            attributeId = h5.openAttribute(objectId, name, registry);
            final int oldStorageDataTypeId = h5.getDataTypeForAttribute(attributeId, registry);
            if (h5.dataTypesAreEqual(oldStorageDataTypeId, storageDataTypeId) == false)
            {
                h5.deleteAttribute(objectId, name);
                attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
            }
        } else
        {
            attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
        }
        h5.writeAttribute(attributeId, nativeDataTypeId, value);
    }

    void setAttribute(final String objectPath, final String name, final int storageDataTypeId,
            final int nativeDataTypeId, final double[] value)
    {
        assert objectPath != null;
        assert name != null;
        assert storageDataTypeId >= 0;
        assert nativeDataTypeId >= 0;
        assert value != null;

        final ICallableWithCleanUp<Object> addAttributeRunnable =
                new ICallableWithCleanUp<Object>()
                    {
                        public Object call(ICleanUpRegistry registry)
                        {
                            final int objectId = h5.openObject(fileId, objectPath, registry);
                            setAttribute(objectId, name, storageDataTypeId, nativeDataTypeId,
                                    value, registry);
                            return null; // Nothing to return.
                        }
                    };
        runner.call(addAttributeRunnable);
    }

    void setAttribute(final int objectId, final String name, final int storageDataTypeId,
            final int nativeDataTypeId, final double[] value, ICleanUpRegistry registry)
    {
        int attributeId;
        if (h5.existsAttribute(objectId, name))
        {
            attributeId = h5.openAttribute(objectId, name, registry);
            final int oldStorageDataTypeId = h5.getDataTypeForAttribute(attributeId, registry);
            if (h5.dataTypesAreEqual(oldStorageDataTypeId, storageDataTypeId) == false)
            {
                h5.deleteAttribute(objectId, name);
                attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
            }
        } else
        {
            attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
        }
        h5.writeAttribute(attributeId, nativeDataTypeId, value);
    }

    void setTypeVariant(final int objectId, final HDF5DataTypeVariant typeVariant,
            ICleanUpRegistry registry)
    {
        setAttribute(objectId, TYPE_VARIANT_ATTRIBUTE, typeVariantDataType.getStorageTypeId(),
                typeVariantDataType.getNativeTypeId(),
                typeVariantDataType.toStorageForm(typeVariant.ordinal()), registry);
    }

    void setStringAttribute(final int objectId, final String name, final String value,
            final int maxLength, ICleanUpRegistry registry)
    {
        final int realMaxLength = ((encoding == CharacterEncoding.UTF8 ? 2 : 1) * maxLength) + 1; // Trailing
                                                                                                  // '\0'
        final int storageDataTypeId = h5.createDataTypeString(realMaxLength, registry);
        int attributeId;
        if (h5.existsAttribute(objectId, name))
        {
            attributeId = h5.openAttribute(objectId, name, registry);
            final int oldStorageDataTypeId = h5.getDataTypeForAttribute(attributeId, registry);
            if (h5.dataTypesAreEqual(oldStorageDataTypeId, storageDataTypeId) == false)
            {
                h5.deleteAttribute(objectId, name);
                attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
            }
        } else
        {
            attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
        }
        h5.writeAttribute(attributeId, storageDataTypeId,
                StringUtils.toBytes0Term(value, maxLength, encoding));
    }

    class StringArrayBuffer
    {
        private byte[] buf;

        private int len;

        private final int maxLengthPerString;

        private final int realMaxLengthPerString;

        StringArrayBuffer(int maxLengthPerString, int initialCapacity)
        {
            this.maxLengthPerString = maxLengthPerString;
            this.realMaxLengthPerString =
                    ((encoding == CharacterEncoding.UTF8 ? 2 : 1) * maxLengthPerString) + 1;
            buf = new byte[initialCapacity];
        }

        void add(String s)
        {
            final byte[] data = StringUtils.toBytes0Term(s, maxLengthPerString, encoding);
            final int dataLen = Math.min(data.length, realMaxLengthPerString);
            final int newLen = len + realMaxLengthPerString;
            if (newLen > buf.length)
            {
                final byte[] newBuf = new byte[Math.max(2 * buf.length, newLen)];
                System.arraycopy(buf, 0, newBuf, 0, len);
                buf = newBuf;
            }
            System.arraycopy(data, 0, buf, len, dataLen);
            len = newLen;
        }

        byte[] toArray()
        {
            if (len < buf.length)
            {
                final byte[] arr = new byte[len];
                System.arraycopy(buf, 0, arr, 0, len);
                return arr;
            } else
            {
                return buf;
            }
        }

        int getMaxLengthInByte()
        {
            return realMaxLengthPerString;
        }
    }

    void setStringArrayAttribute(final int objectId, final String name, final String[] value,
            final int maxLength, ICleanUpRegistry registry)
    {
        final StringArrayBuffer array = new StringArrayBuffer(maxLength, value.length * 10);
        for (int i = 0; i < value.length; ++i)
        {
            array.add(value[i]);
        }
        final byte[] arrData = array.toArray();
        final int stringDataTypeId = h5.createDataTypeString(array.getMaxLengthInByte(), registry);
        final int storageDataTypeId = h5.createArrayType(stringDataTypeId, value.length, registry);
        int attributeId;
        if (h5.existsAttribute(objectId, name))
        {
            attributeId = h5.openAttribute(objectId, name, registry);
            final int oldStorageDataTypeId = h5.getDataTypeForAttribute(attributeId, registry);
            if (h5.dataTypesAreEqual(oldStorageDataTypeId, storageDataTypeId) == false)
            {
                h5.deleteAttribute(objectId, name);
                attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
            }
        } else
        {
            attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
        }
        h5.writeAttribute(attributeId, storageDataTypeId, arrData);
    }

    void setStringArrayAttribute(final int objectId, final String name,
            final MDArray<String> value, final int maxLength, ICleanUpRegistry registry)
    {
        final StringArrayBuffer array =
                new StringArrayBuffer(maxLength, value.getAsFlatArray().length * 10);
        for (int i = 0; i < value.getAsFlatArray().length; ++i)
        {
            array.add(value.getAsFlatArray()[i]);
        }
        final byte[] arrData = array.toArray();
        final int stringDataTypeId = h5.createDataTypeString(array.getMaxLengthInByte(), registry);
        final int storageDataTypeId =
                h5.createArrayType(stringDataTypeId, value.dimensions(), registry);
        int attributeId;
        if (h5.existsAttribute(objectId, name))
        {
            attributeId = h5.openAttribute(objectId, name, registry);
            final int oldStorageDataTypeId = h5.getDataTypeForAttribute(attributeId, registry);
            if (h5.dataTypesAreEqual(oldStorageDataTypeId, storageDataTypeId) == false)
            {
                h5.deleteAttribute(objectId, name);
                attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
            }
        } else
        {
            attributeId = h5.createAttribute(objectId, name, storageDataTypeId, registry);
        }
        h5.writeAttribute(attributeId, storageDataTypeId, arrData);
    }

    void setStringAttributeVariableLength(final int objectId, final String name,
            final String value, ICleanUpRegistry registry)
    {
        int attributeId;
        if (h5.existsAttribute(objectId, name))
        {
            attributeId = h5.openAttribute(objectId, name, registry);
            final int oldStorageDataTypeId = h5.getDataTypeForAttribute(attributeId, registry);
            if (h5.dataTypesAreEqual(oldStorageDataTypeId, variableLengthStringDataTypeId) == false)
            {
                h5.deleteAttribute(objectId, name);
                attributeId =
                        h5.createAttribute(objectId, name, variableLengthStringDataTypeId, registry);
            }
        } else
        {
            attributeId =
                    h5.createAttribute(objectId, name, variableLengthStringDataTypeId, registry);
        }
        writeAttributeStringVL(attributeId, new String[]
            { value });
    }

    String moveLinkOutOfTheWay(String linkPath)
    {
        final String newLinkPath = createNonExistentReplacementLinkPath(linkPath);
        h5.moveLink(fileId, linkPath, newLinkPath);
        return newLinkPath;
    }

    private String createNonExistentReplacementLinkPath(final String dataTypePath)
    {
        final String dstLinkPath = dataTypePath + "__REPLACED_";
        int idx = 1;
        while (h5.exists(fileId, dstLinkPath + idx))
        {
            ++idx;
        }
        return dstLinkPath + idx;
    }

}
