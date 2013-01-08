/*
 * Copyright 2010 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.hdf5.HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION;
import static ch.systemsx.cisd.hdf5.hdf5lib.H5D.H5Dwrite;
import static ch.systemsx.cisd.hdf5.hdf5lib.H5D.H5DwriteString;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5P_DEFAULT;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5S_ALL;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5S_SCALAR;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

import ch.systemsx.cisd.base.mdarray.MDAbstractArray;
import ch.systemsx.cisd.base.mdarray.MDArray;
import ch.systemsx.cisd.hdf5.HDF5BaseWriter.StringArrayBuffer;
import ch.systemsx.cisd.hdf5.cleanup.ICallableWithCleanUp;
import ch.systemsx.cisd.hdf5.cleanup.ICleanUpRegistry;

/**
 * The implementation of {@link IHDF5StringWriter}.
 * 
 * @author Bernd Rinn
 */
public class HDF5StringWriter implements IHDF5StringWriter
{

    private static final int MAX_COMPACT_SIZE = 64 * 1024 - 12;

    private final HDF5BaseWriter baseWriter;

    HDF5StringWriter(HDF5BaseWriter baseWriter)
    {
        assert baseWriter != null;

        this.baseWriter = baseWriter;
    }

    // /////////////////////
    // Attributes
    // /////////////////////

    @Override
    public void setStringAttributeVariableLength(final String objectPath, final String name,
            final String value)
    {
        assert name != null;
        assert value != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Object> addAttributeRunnable =
                new ICallableWithCleanUp<Object>()
                    {
                        @Override
                        public Object call(ICleanUpRegistry registry)
                        {
                            final int objectId =
                                    baseWriter.h5.openObject(baseWriter.fileId, objectPath,
                                            registry);
                            baseWriter.setStringAttributeVariableLength(objectId, name, value,
                                    registry);
                            return null; // Nothing to return.
                        }
                    };
        baseWriter.runner.call(addAttributeRunnable);
    }

    @Override
    public void setStringAttribute(final String objectPath, final String name, final String value)
    {
        setStringAttribute(objectPath, name, value, value.length(), true);
    }

    @Override
    public void setStringAttribute(final String objectPath, final String name, final String value,
            final int maxLength)
    {
        setStringAttribute(objectPath, name, value, maxLength, false);
    }

    void setStringAttribute(final String objectPath, final String name, final String value,
            final int maxLength, final boolean lengthFitsValue)
    {
        assert objectPath != null;
        assert name != null;
        assert value != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Object> addAttributeRunnable =
                new ICallableWithCleanUp<Object>()
                    {
                        @Override
                        public Object call(ICleanUpRegistry registry)
                        {
                            final int objectId =
                                    baseWriter.h5.openObject(baseWriter.fileId, objectPath,
                                            registry);
                            baseWriter.setStringAttribute(objectId, name, value, maxLength,
                                    lengthFitsValue, registry);
                            return null; // Nothing to return.
                        }
                    };
        baseWriter.runner.call(addAttributeRunnable);
    }

    @Override
    public void setStringArrayAttribute(final String objectPath, final String name,
            final String[] value)
    {
        setStringArrayAttribute(objectPath, name, value, -1, true);
    }

    @Override
    public void setStringArrayAttribute(final String objectPath, final String name,
            final String[] value, final int maxLength)
    {
        setStringArrayAttribute(objectPath, name, value, maxLength, false);
    }

    void setStringArrayAttribute(final String objectPath, final String name, final String[] value,
            final int maxLength, final boolean lengthFitsValue)
    {
        assert objectPath != null;
        assert name != null;
        assert value != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> setAttributeRunnable = new ICallableWithCleanUp<Void>()
            {
                @Override
                public Void call(ICleanUpRegistry registry)
                {
                    final int objectId =
                            baseWriter.h5.openObject(baseWriter.fileId, objectPath, registry);
                    baseWriter.setStringArrayAttribute(objectId, name, value, maxLength,
                            lengthFitsValue, registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(setAttributeRunnable);
    }

    @Override
    public void setStringMDArrayAttribute(final String objectPath, final String name,
            final MDArray<String> value)
    {
        setStringMDArrayAttribute(objectPath, name, value, -1, true);
    }

    @Override
    public void setStringMDArrayAttribute(final String objectPath, final String name,
            final MDArray<String> value, final int maxLength)
    {
        setStringMDArrayAttribute(objectPath, name, value, maxLength, false);
    }

    void setStringMDArrayAttribute(final String objectPath, final String name,
            final MDArray<String> value, final int maxLength, final boolean lengthFitsValue)
    {
        assert objectPath != null;
        assert name != null;
        assert value != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> setAttributeRunnable = new ICallableWithCleanUp<Void>()
            {
                @Override
                public Void call(ICleanUpRegistry registry)
                {
                    final int objectId =
                            baseWriter.h5.openObject(baseWriter.fileId, objectPath, registry);
                    baseWriter.setStringArrayAttribute(objectId, name, value, maxLength,
                            lengthFitsValue, registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(setAttributeRunnable);
    }

    // /////////////////////
    // Data Sets
    // /////////////////////

    @Override
    public void writeString(final String objectPath, final String data, final int maxLength)
    {
        writeString(objectPath, data, maxLength, true,
                HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION);
    }

    @Override
    public void writeString(final String objectPath, final String data)
    {
        writeString(objectPath, data, data.length(), true,
                HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION);
    }

    @Override
    public void writeString(final String objectPath, final String data,
            final HDF5GenericStorageFeatures features)
    {
        writeString(objectPath, data, data.length(), true, features);
    }

    @Override
    public void writeString(final String objectPath, final String data, final int maxLength,
            final HDF5GenericStorageFeatures features)
    {
        writeString(objectPath, data, maxLength, false, features);
    }

    // Implementation note: this needs special treatment as we want to create a (possibly chunked)
    // data set with max dimension 1 instead of infinity.
    void writeString(final String objectPath, final String data, final int maxLength,
            final boolean lengthFitsValue, final HDF5GenericStorageFeatures features)
    {
        assert objectPath != null;
        assert data != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Object> writeRunnable = new ICallableWithCleanUp<Object>()
            {
                @Override
                public Object call(ICleanUpRegistry registry)
                {
                    final byte[] bytes;
                    final int realMaxLengthInBytes;
                    if (lengthFitsValue)
                    {
                        bytes = StringUtils.toBytes0Term(data, baseWriter.encodingForNewDataSets);
                        realMaxLengthInBytes = (bytes.length == 1) ? 1 : bytes.length - 1;
                    } else
                    {
                        bytes =
                                StringUtils.toBytes0Term(data, maxLength,
                                        baseWriter.encodingForNewDataSets);
                        realMaxLengthInBytes =
                                baseWriter.encodingForNewDataSets.getMaxBytesPerChar()
                                        * ((maxLength == 0) ? 1 : maxLength);
                    }

                    boolean exists = baseWriter.h5.exists(baseWriter.fileId, objectPath);
                    if (exists && baseWriter.keepDataIfExists(features) == false)
                    {
                        baseWriter.h5.deleteObject(baseWriter.fileId, objectPath);
                        exists = false;
                    }
                    final int stringDataTypeId =
                            baseWriter.h5.createDataTypeString(realMaxLengthInBytes, registry);
                    if (features.requiresChunking() == false)
                    {
                        // If we do not want to compress, we can create a scalar dataset.
                        baseWriter.writeScalar(objectPath, stringDataTypeId, stringDataTypeId,
                                bytes, features.allowsCompact()
                                        && (realMaxLengthInBytes < MAX_COMPACT_SIZE),
                                baseWriter.keepDataIfExists(features), registry);
                    } else
                    {
                        final long[] chunkSizeOrNull =
                                HDF5Utils.tryGetChunkSizeForString(realMaxLengthInBytes,
                                        features.requiresChunking());
                        final int dataSetId;
                        if (exists)
                        {
                            dataSetId =
                                    baseWriter.h5.openDataSet(baseWriter.fileId, objectPath,
                                            registry);

                        } else
                        {
                            final HDF5StorageLayout layout =
                                    baseWriter.determineLayout(stringDataTypeId,
                                            HDF5Utils.SCALAR_DIMENSIONS, chunkSizeOrNull, null);
                            dataSetId =
                                    baseWriter.h5.createDataSet(baseWriter.fileId,
                                            HDF5Utils.SCALAR_DIMENSIONS, chunkSizeOrNull,
                                            stringDataTypeId, features, objectPath, layout,
                                            baseWriter.fileFormat, registry);
                        }
                        H5Dwrite(dataSetId, stringDataTypeId, H5S_ALL, H5S_ALL, H5P_DEFAULT, bytes);
                    }
                    return null; // Nothing to return.
                }

            };
        baseWriter.runner.call(writeRunnable);
    }

    @Override
    public void writeStringArray(final String objectPath, final String[] data,
            final HDF5GenericStorageFeatures features)
    {
        assert objectPath != null;
        assert data != null;

        writeStringArray(objectPath, data, getMaxLength(data), true, features, false);
    }

    @Override
    public void writeStringArray(final String objectPath, final String[] data)
    {
        assert objectPath != null;
        assert data != null;

        writeStringArray(objectPath, data, getMaxLength(data), true,
                HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION, false);
    }

    @Override
    public void writeStringArray(final String objectPath, final String[] data, final int maxLength)
    {
        writeStringArray(objectPath, data, maxLength, false,
                HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION, false);
    }

    private static int getMaxLength(String[] data)
    {
        int maxLength = 0;
        for (String s : data)
        {
            maxLength = Math.max(maxLength, s.length());
        }
        return maxLength;
    }

    @Override
    public void writeStringArray(final String objectPath, final String[] data, int maxLength,
            final HDF5GenericStorageFeatures features) throws HDF5JavaException
    {
        assert maxLength >= 0;

        writeStringArray(objectPath, data, maxLength, false, features, false);
    }

    @Override
    public void writeStringArrayVariableLength(String objectPath, String[] data,
            HDF5GenericStorageFeatures features)
    {
        writeStringArray(objectPath, data, 0, false, features, true);
    }

    @Override
    public void writeStringArrayVariableLength(String objectPath, String[] data)
    {
        writeStringArray(objectPath, data, 0, false,
                HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION, true);
    }

    private void writeStringArray(final String objectPath, final String[] data,
            final int maxLength, final boolean lengthFitsValue,
            final HDF5GenericStorageFeatures features, final boolean variableLength)
    {
        assert objectPath != null;
        assert data != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Object> writeRunnable = new ICallableWithCleanUp<Object>()
            {
                @Override
                public Object call(ICleanUpRegistry registry)
                {
                    if (variableLength)
                    {
                        final int elementSize = 8; // 64bit pointers
                        final int stringDataTypeId = baseWriter.variableLengthStringDataTypeId;
                        final int dataSetId =
                                baseWriter.getOrCreateDataSetId(objectPath, stringDataTypeId,
                                        new long[]
                                            { data.length }, elementSize, features, registry);
                        baseWriter.writeStringVL(dataSetId, data);
                    } else
                    {
                        final StringArrayBuffer array =
                                baseWriter.new StringArrayBuffer(maxLength, lengthFitsValue);
                        array.addAll(data);
                        final byte[] arrData = array.toArray();
                        final int elementSize = array.getMaxLengthInByte();
                        final int stringDataTypeId =
                                baseWriter.h5.createDataTypeString(elementSize, registry);
                        final int dataSetId =
                                baseWriter.getOrCreateDataSetId(objectPath, stringDataTypeId,
                                        new long[]
                                            { data.length }, elementSize, features, registry);
                        H5Dwrite(dataSetId, stringDataTypeId, H5S_ALL, H5S_ALL, H5P_DEFAULT,
                                arrData);
                    }
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    @Override
    public void createStringArray(final String objectPath, final int maxLength, final int size)
    {
        createStringArray(objectPath, maxLength, size, GENERIC_NO_COMPRESSION);
    }

    @Override
    public void createStringArray(final String objectPath, final int maxLength, final long size,
            final int blockSize)
    {
        createStringArray(objectPath, maxLength, size, blockSize, GENERIC_NO_COMPRESSION);
    }

    @Override
    public void createStringArray(final String objectPath, final int maxLength, final int size,
            final HDF5GenericStorageFeatures features)
    {
        assert maxLength > 0;

        createStringArray(objectPath, maxLength, size, features, false);
    }

    private void createStringArray(final String objectPath, final int maxLength, final int size,
            final HDF5GenericStorageFeatures features, final boolean variableLength)
    {
        assert objectPath != null;
        assert size >= 0;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Object> writeRunnable = new ICallableWithCleanUp<Object>()
            {
                @Override
                public Object call(ICleanUpRegistry registry)
                {
                    final int elementSize;
                    final int stringDataTypeId;
                    if (variableLength)
                    {
                        elementSize = 8; // 64bit pointers
                        stringDataTypeId = baseWriter.variableLengthStringDataTypeId;
                    } else
                    {
                        elementSize =
                                baseWriter.encodingForNewDataSets.getMaxBytesPerChar() * maxLength;
                        stringDataTypeId =
                                baseWriter.h5.createDataTypeString(elementSize, registry);
                    }
                    if (features.requiresChunking())
                    {
                        baseWriter.createDataSet(objectPath, stringDataTypeId, features, new long[]
                            { 0 }, new long[]
                            { size }, elementSize, registry);
                    } else
                    {
                        baseWriter.createDataSet(objectPath, stringDataTypeId, features, new long[]
                            { size }, null, elementSize, registry);
                    }
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    @Override
    public void createStringArray(final String objectPath, final int maxLength, final long size,
            final int blockSize, final HDF5GenericStorageFeatures features)
    {
        assert maxLength > 0;

        createStringArray(objectPath, maxLength, size, blockSize, features, false);
    }

    private void createStringArray(final String objectPath, final int maxLength, final long size,
            final int blockSize, final HDF5GenericStorageFeatures features,
            final boolean variableLength)
    {
        assert objectPath != null;
        assert blockSize > 0;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Object> writeRunnable = new ICallableWithCleanUp<Object>()
            {
                @Override
                public Object call(ICleanUpRegistry registry)
                {
                    final int elementSize;
                    final int stringDataTypeId;
                    if (variableLength)
                    {
                        elementSize = 8; // 64bit pointers
                        stringDataTypeId = baseWriter.variableLengthStringDataTypeId;
                    } else
                    {
                        elementSize =
                                baseWriter.encodingForNewDataSets.getMaxBytesPerChar() * maxLength;
                        stringDataTypeId =
                                baseWriter.h5.createDataTypeString(elementSize, registry);
                    }
                    baseWriter.createDataSet(objectPath, stringDataTypeId, features, new long[]
                        { size }, new long[]
                        { blockSize }, elementSize, registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    @Override
    public void writeStringArrayBlock(final String objectPath, final String[] data,
            final long blockNumber)
    {
        assert data != null;
        writeStringArrayBlockWithOffset(objectPath, data, data.length, data.length * blockNumber);
    }

    @Override
    public void writeStringArrayBlockWithOffset(final String objectPath, final String[] data,
            final int dataSize, final long offset)
    {
        assert objectPath != null;
        assert data != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                @Override
                public Void call(ICleanUpRegistry registry)
                {
                    final long[] blockDimensions = new long[]
                        { dataSize };
                    final long[] slabStartOrNull = new long[]
                        { offset };
                    final int dataSetId =
                            baseWriter.h5.openAndExtendDataSet(baseWriter.fileId, objectPath,
                                    baseWriter.fileFormat, new long[]
                                        { offset + dataSize }, -1, registry);
                    final int dataSpaceId =
                            baseWriter.h5.getDataSpaceForDataSet(dataSetId, registry);
                    baseWriter.h5.setHyperslabBlock(dataSpaceId, slabStartOrNull, blockDimensions);
                    final int memorySpaceId =
                            baseWriter.h5.createSimpleDataSpace(blockDimensions, registry);
                    final int stringDataTypeId =
                            baseWriter.h5.getDataTypeForDataSet(dataSetId, registry);
                    if (baseWriter.h5.isVariableLengthString(stringDataTypeId))
                    {
                        baseWriter.writeStringVL(dataSetId, memorySpaceId, dataSpaceId, data);
                    } else
                    {
                        final int maxLength = baseWriter.h5.getDataTypeSize(stringDataTypeId);
                        writeStringArray(dataSetId, stringDataTypeId, memorySpaceId, dataSpaceId,
                                H5P_DEFAULT, data, maxLength);
                    }
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    @Override
    public void writeStringMDArray(final String objectPath, final MDArray<String> data)
            throws HDF5JavaException
    {
        writeStringMDArray(objectPath, data, getMaxLength(data.getAsFlatArray()), true,
                HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION, false);
    }

    @Override
    public void writeStringMDArray(final String objectPath, final MDArray<String> data,
            final HDF5GenericStorageFeatures features) throws HDF5JavaException
    {
        writeStringMDArray(objectPath, data, getMaxLength(data.getAsFlatArray()), true, features,
                false);
    }

    @Override
    public void writeStringMDArray(final String objectPath, final MDArray<String> data,
            final int maxLength) throws HDF5JavaException
    {
        writeStringMDArray(objectPath, data, maxLength,
                HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION);
    }

    @Override
    public void writeStringMDArray(final String objectPath, final MDArray<String> data,
            final int maxLength, final HDF5GenericStorageFeatures features)
            throws HDF5JavaException
    {
        writeStringMDArray(objectPath, data, maxLength, false, features, false);
    }

    @Override
    public void writeStringMDArrayVariableLength(String objectPath, MDArray<String> data,
            HDF5GenericStorageFeatures features)
    {
        writeStringMDArray(objectPath, data, 0, false, features, true);
    }

    @Override
    public void writeStringMDArrayVariableLength(String objectPath, MDArray<String> data)
    {
        writeStringMDArray(objectPath, data, 0, false,
                HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION, true);
    }

    private void writeStringMDArray(final String objectPath, final MDArray<String> data,
            final int maxLength, final boolean lengthFitsValue,
            final HDF5GenericStorageFeatures features, final boolean variableLength)
    {
        assert objectPath != null;
        assert data != null;
        assert maxLength >= 0;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Object> writeRunnable = new ICallableWithCleanUp<Object>()
            {
                @Override
                public Object call(ICleanUpRegistry registry)
                {
                    if (variableLength)
                    {
                        final int elementSize = 8; // 64bit pointers
                        final int stringDataTypeId = baseWriter.variableLengthStringDataTypeId;
                        final int dataSetId =
                                baseWriter.getOrCreateDataSetId(objectPath, stringDataTypeId,
                                        data.longDimensions(), elementSize, features, registry);
                        baseWriter.writeStringVL(dataSetId, data.getAsFlatArray());
                    } else
                    {
                        final StringArrayBuffer array =
                                baseWriter.new StringArrayBuffer(maxLength, lengthFitsValue);
                        array.addAll(data.getAsFlatArray());
                        final byte[] arrData = array.toArray();
                        final int elementSize = array.getMaxLengthInByte();
                        final int stringDataTypeId =
                                baseWriter.h5.createDataTypeString(elementSize, registry);
                        final int dataSetId =
                                baseWriter.getOrCreateDataSetId(objectPath, stringDataTypeId,
                                        data.longDimensions(), elementSize, features, registry);
                        H5Dwrite(dataSetId, stringDataTypeId, H5S_ALL, H5S_ALL, H5P_DEFAULT,
                                arrData);
                    }
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    @Override
    public void createStringMDArray(final String objectPath, final int maxLength,
            final int[] dimensions)
    {
        createStringMDArray(objectPath, maxLength, dimensions, GENERIC_NO_COMPRESSION);
    }

    @Override
    public void createStringMDArray(final String objectPath, final int maxLength,
            final long[] dimensions, final int[] blockSize)
    {
        createStringMDArray(objectPath, maxLength, dimensions, blockSize, GENERIC_NO_COMPRESSION);
    }

    @Override
    public void createStringMDArray(final String objectPath, final int maxLength,
            final int[] dimensions, final HDF5GenericStorageFeatures features)
    {
        assert maxLength > 0;

        createStringMDArray(objectPath, maxLength, dimensions, features, false);
    }

    private void createStringMDArray(final String objectPath, final int maxLength,
            final int[] dimensions, final HDF5GenericStorageFeatures features,
            final boolean variableLength)
    {
        assert objectPath != null;
        assert dimensions != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Object> writeRunnable = new ICallableWithCleanUp<Object>()
            {
                @Override
                public Object call(ICleanUpRegistry registry)
                {
                    final int elementSize;
                    final int stringDataTypeId;
                    if (variableLength)
                    {
                        elementSize = 8; // 64bit pointers
                        stringDataTypeId = baseWriter.variableLengthStringDataTypeId;
                    } else
                    {
                        elementSize =
                                baseWriter.encodingForNewDataSets.getMaxBytesPerChar() * maxLength;
                        stringDataTypeId =
                                baseWriter.h5.createDataTypeString(elementSize, registry);
                    }
                    if (features.requiresChunking())
                    {
                        baseWriter.createDataSet(objectPath, stringDataTypeId, features, new long[]
                            { 0 }, MDAbstractArray.toLong(dimensions), maxLength, registry);
                    } else
                    {
                        baseWriter.createDataSet(objectPath, stringDataTypeId, features,
                                MDAbstractArray.toLong(dimensions), null, maxLength, registry);
                    }
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    @Override
    public void createStringMDArray(final String objectPath, final int maxLength,
            final long[] dimensions, final int[] blockSize,
            final HDF5GenericStorageFeatures features)
    {
        assert maxLength > 0;

        createStringMDArray(objectPath, maxLength, dimensions, blockSize, features, false);
    }

    private void createStringMDArray(final String objectPath, final int maxLength,
            final long[] dimensions, final int[] blockSize,
            final HDF5GenericStorageFeatures features, final boolean variableLength)
    {
        assert objectPath != null;
        assert dimensions != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Object> writeRunnable = new ICallableWithCleanUp<Object>()
            {
                @Override
                public Object call(ICleanUpRegistry registry)
                {
                    final int elementSize;
                    final int stringDataTypeId;
                    if (variableLength)
                    {
                        elementSize = 8; // 64bit pointers
                        stringDataTypeId = baseWriter.variableLengthStringDataTypeId;
                    } else
                    {
                        elementSize =
                                baseWriter.encodingForNewDataSets.getMaxBytesPerChar() * maxLength;
                        stringDataTypeId =
                                baseWriter.h5.createDataTypeString(elementSize, registry);
                    }
                    baseWriter.createDataSet(objectPath, stringDataTypeId, features, dimensions,
                            MDAbstractArray.toLong(blockSize), elementSize, registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    @Override
    public void writeStringMDArrayBlock(final String objectPath, final MDArray<String> data,
            final long[] blockNumber)
    {
        assert data != null;
        assert blockNumber != null;

        final long[] dimensions = data.longDimensions();
        final long[] offset = new long[dimensions.length];
        for (int i = 0; i < offset.length; ++i)
        {
            offset[i] = blockNumber[i] * dimensions[i];
        }
        writeStringMDArrayBlockWithOffset(objectPath, data, offset);
    }

    @Override
    public void writeStringMDArrayBlockWithOffset(final String objectPath,
            final MDArray<String> data, final long[] offset)
    {
        assert objectPath != null;
        assert data != null;
        assert offset != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                @Override
                public Void call(ICleanUpRegistry registry)
                {
                    final long[] dimensions = data.longDimensions();
                    assert dimensions.length == offset.length;
                    final long[] dataSetDimensions = new long[dimensions.length];
                    for (int i = 0; i < offset.length; ++i)
                    {
                        dataSetDimensions[i] = offset[i] + dimensions[i];
                    }
                    final int dataSetId =
                            baseWriter.h5.openAndExtendDataSet(baseWriter.fileId, objectPath,
                                    baseWriter.fileFormat, dataSetDimensions, -1, registry);
                    final int dataSpaceId =
                            baseWriter.h5.getDataSpaceForDataSet(dataSetId, registry);
                    baseWriter.h5.setHyperslabBlock(dataSpaceId, offset, dimensions);
                    final int memorySpaceId =
                            baseWriter.h5.createSimpleDataSpace(dimensions, registry);
                    final int stringDataTypeId =
                            baseWriter.h5.getDataTypeForDataSet(dataSetId, registry);
                    if (baseWriter.h5.isVariableLengthString(stringDataTypeId))
                    {
                        baseWriter.writeStringVL(dataSetId, memorySpaceId, dataSpaceId,
                                data.getAsFlatArray());
                    } else
                    {
                        final int maxLength = baseWriter.h5.getDataTypeSize(stringDataTypeId);
                        writeStringArray(dataSetId, stringDataTypeId, memorySpaceId, dataSpaceId,
                                H5P_DEFAULT, data.getAsFlatArray(), maxLength);
                    }
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    @Override
    public void writeStringVariableLength(final String objectPath, final String data)
    {
        assert objectPath != null;
        assert data != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Object> writeRunnable = new ICallableWithCleanUp<Object>()
            {
                @Override
                public Object call(ICleanUpRegistry registry)
                {
                    final int dataSetId;
                    if (baseWriter.h5.exists(baseWriter.fileId, objectPath))
                    {
                        dataSetId =
                                baseWriter.h5.openObject(baseWriter.fileId, objectPath, registry);
                    } else
                    {
                        dataSetId =
                                baseWriter.h5.createScalarDataSet(baseWriter.fileId,
                                        baseWriter.variableLengthStringDataTypeId, objectPath,
                                        true, registry);
                    }
                    H5DwriteString(dataSetId, baseWriter.variableLengthStringDataTypeId,
                            H5S_SCALAR, H5S_SCALAR, H5P_DEFAULT, new String[]
                                { data });
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    @Override
    public void writeStringVariableLengthArray(final String objectPath, final String[] data)
    {
        writeStringVariableLengthArray(objectPath, data, GENERIC_NO_COMPRESSION);
    }

    @Override
    public void writeStringVariableLengthArray(final String objectPath, final String[] data,
            final HDF5GenericStorageFeatures features)
    {
        writeStringArray(objectPath, data, -1, false, features, true);
    }

    @Override
    public void createStringVariableLengthArray(final String objectPath, final int size)
    {
        createStringVariableLengthArray(objectPath, size,
                HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION);
    }

    @Override
    public void createStringVariableLengthArray(final String objectPath, final long size,
            final int blockSize) throws HDF5JavaException
    {
        createStringVariableLengthArray(objectPath, size, blockSize,
                HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION);
    }

    @Override
    public void createStringVariableLengthArray(final String objectPath, final long size,
            final int blockSize, final HDF5GenericStorageFeatures features)
    {
        createStringArray(objectPath, -1, size, blockSize, features, true);
    }

    @Override
    public void createStringVariableLengthArray(final String objectPath, final int size,
            final HDF5GenericStorageFeatures features)
    {
        createStringArray(objectPath, -1, size, features, true);
    }

    @Override
    public void createStringVariableLengthMDArray(final String objectPath, final int[] dimensions,
            final HDF5GenericStorageFeatures features)
    {
        createStringMDArray(objectPath, -1, dimensions, features, true);
    }

    @Override
    public void createStringVariableLengthMDArray(final String objectPath, final int[] dimensions)
    {
        createStringMDArray(objectPath, -1, dimensions, GENERIC_NO_COMPRESSION, true);
    }

    @Override
    public void createStringVariableLengthMDArray(final String objectPath, final long[] dimensions,
            final int[] blockSize, final HDF5GenericStorageFeatures features)
    {
        createStringMDArray(objectPath, -1, dimensions, blockSize, features, true);
    }

    @Override
    public void createStringVariableLengthMDArray(final String objectPath, final long[] dimensions,
            final int[] blockSize)
    {
        createStringMDArray(objectPath, -1, dimensions, blockSize, GENERIC_NO_COMPRESSION, true);
    }

    @Override
    public void writeStringVariableLengthMDArray(final String objectPath,
            final MDArray<String> data, final HDF5GenericStorageFeatures features)
    {
        assert objectPath != null;
        assert data != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Object> writeRunnable = new ICallableWithCleanUp<Object>()
            {
                @Override
                public Object call(ICleanUpRegistry registry)
                {
                    final int pointerSize = 8; // 64bit pointers
                    final int stringDataTypeId = baseWriter.variableLengthStringDataTypeId;
                    final int dataSetId =
                            baseWriter.getOrCreateDataSetId(objectPath, stringDataTypeId,
                                    MDAbstractArray.toLong(data.dimensions()), pointerSize,
                                    features, registry);
                    baseWriter.writeStringVL(dataSetId, data.getAsFlatArray());
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    @Override
    public void writeStringVariableLengthMDArray(final String objectPath, final MDArray<String> data)
    {
        writeStringVariableLengthMDArray(objectPath, data, GENERIC_NO_COMPRESSION);
    }

    /**
     * H5Dwrite writes a (partial) dataset, specified by its identifier dataset_id, from the
     * application memory data object into the file.
     * 
     * @param dataset_id Identifier of the dataset read from.
     * @param mem_type_id Identifier of the memory datatype.
     * @param mem_space_id Identifier of the memory dataspace.
     * @param file_space_id Identifier of the dataset's dataspace in the file.
     * @param xfer_plist_id Identifier of a transfer property list for this I/O operation.
     * @param obj String array with data to be written to the file.
     * @param maxLength The maximal length of one String in the array.
     * @return a non-negative value if successful
     * @exception HDF5Exception - Failure in the data conversion.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - data object is null.
     */
    private int writeStringArray(final int dataset_id, final int mem_type_id,
            final int mem_space_id, final int file_space_id, final int xfer_plist_id,
            final String[] obj, final int maxLength) throws HDF5Exception, HDF5LibraryException,
            NullPointerException
    {
        final byte[] buf = StringUtils.toBytes(obj, maxLength, baseWriter.encodingForNewDataSets);

        /* will raise exception on error */
        final int status =
                H5Dwrite(dataset_id, mem_type_id, mem_space_id, file_space_id, xfer_plist_id, buf);

        return status;
    }

}
