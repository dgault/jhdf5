/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.hdf5.io;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.util.Arrays;

import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5FileNotFoundException;

import ch.systemsx.cisd.base.convert.NativeData;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.hdf5.HDF5DataClass;
import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5FactoryProvider;
import ch.systemsx.cisd.hdf5.HDF5GenericStorageFeatures;
import ch.systemsx.cisd.hdf5.HDF5IntStorageFeatures;
import ch.systemsx.cisd.hdf5.HDF5OpaqueType;
import ch.systemsx.cisd.hdf5.HDF5StorageLayout;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

/**
 * A {@link IRandomAccessFile} backed by an HDF5 dataset. The HDF5 dataset needs to be a byte array
 * (or opaque byte array) of rank 1.
 * 
 * @author Bernd Rinn
 */
public class HDF5DataSetRandomAccessFile implements IRandomAccessFile
{
    private final static int MB = 1024 * 1024;

    private final static int DEFAULT_CHUNK_SIZE = 10 * MB;

    private final IHDF5Reader reader;

    private final IHDF5Writer writerOrNull;

    private final String dataSetPath;

    private final HDF5DataSetInformation dataSetInfo;

    private final HDF5OpaqueType opaqueTypeOrNull;

    private final int blockSize;

    private final boolean extendable;

    private final boolean closeReaderOnCloseFile;

    private long length;

    private int realBlockSize;

    private byte[] block;

    private long blockOffset;

    private int positionInBlock;

    private boolean blockDirty;

    private long blockOffsetMark = -1;

    private int positionInBlockMark = -1;

    private boolean extensionPending;

    private ch.systemsx.cisd.base.convert.NativeData.ByteOrder byteOrder =
            ch.systemsx.cisd.base.convert.NativeData.ByteOrder.BIG_ENDIAN;

    /**
     * Creates a {@link HDF5DataSetRandomAccessFile} in read-only mode.
     * 
     * @param hdf5File The file of the HDF5 container.
     * @param dataSetPath The path of the HDF5 dataset in the HDF5 container to use as a file.
     * @return The {@link HDF5DataSetRandomAccessFile}.
     * @deprecated Use {@link HDF5IOAdapterFactory#asRandomAccessFile(IHDF5Reader, String)} instead.
     */
    @Deprecated
    public static HDF5DataSetRandomAccessFile createForReading(File hdf5File, String dataSetPath)
    {
        return new HDF5DataSetRandomAccessFile(hdf5File, dataSetPath,
                HDF5GenericStorageFeatures.GENERIC_CHUNKED, DEFAULT_CHUNK_SIZE, null, true);
    }

    /**
     * Creates a {@link HDF5DataSetRandomAccessFile} in read-write mode.
     * 
     * @param hdf5File The file of the HDF5 container.
     * @param dataSetPath The path of the HDF5 dataset in the HDF5 container to use as a file.
     * @return The {@link HDF5DataSetRandomAccessFile}.
     * @deprecated Use {@link HDF5IOAdapterFactory#asRandomAccessFile(IHDF5Writer, String)} instead.
     */
    @Deprecated
    public static HDF5DataSetRandomAccessFile create(File hdf5File, String dataSetPath)
    {
        return new HDF5DataSetRandomAccessFile(hdf5File, dataSetPath,
                HDF5GenericStorageFeatures.GENERIC_CHUNKED, DEFAULT_CHUNK_SIZE, null, false);
    }

    /**
     * Creates a {@link HDF5DataSetRandomAccessFile} in read-write mode.
     * 
     * @param hdf5File The file of the HDF5 container.
     * @param dataSetPath The path of the HDF5 dataset in the HDF5 container to use as a file.
     * @param chunkSize If the dataset does not yet exist, use this value as the chunk size.
     * @return The {@link HDF5DataSetRandomAccessFile}.
     * @deprecated Use
     *             {@link HDF5IOAdapterFactory#asRandomAccessFile(IHDF5Writer, String, HDF5GenericStorageFeatures, int, String)}
     *             instead.
     */
    @Deprecated
    public static HDF5DataSetRandomAccessFile create(File hdf5File, String dataSetPath,
            int chunkSize)
    {
        return new HDF5DataSetRandomAccessFile(hdf5File, dataSetPath,
                HDF5GenericStorageFeatures.GENERIC_CHUNKED, chunkSize, null, false);
    }

    /**
     * Creates a {@link HDF5DataSetRandomAccessFile} in read-write mode with compression switched
     * on.
     * 
     * @param hdf5File The file of the HDF5 container.
     * @param dataSetPath The path of the HDF5 dataset in the HDF5 container to use as a file.
     * @return The {@link HDF5DataSetRandomAccessFile}.
     * @deprecated Use
     *             {@link HDF5IOAdapterFactory#asRandomAccessFile(IHDF5Writer, String, HDF5GenericStorageFeatures, int, String)}
     *             instead.
     */
    @Deprecated
    public static HDF5DataSetRandomAccessFile createCompress(File hdf5File, String dataSetPath)
    {
        return new HDF5DataSetRandomAccessFile(hdf5File, dataSetPath,
                HDF5GenericStorageFeatures.GENERIC_DEFLATE, DEFAULT_CHUNK_SIZE, null, false);
    }

    /**
     * Creates a {@link HDF5DataSetRandomAccessFile} in read-write mode with compression switched
     * on.
     * 
     * @param hdf5File The file of the HDF5 container.
     * @param dataSetPath The path of the HDF5 dataset in the HDF5 container to use as a file.
     * @param chunkSize If the dataset does not yet exist, use this value as the chunk size.
     * @return The {@link HDF5DataSetRandomAccessFile}.
     * @deprecated Use
     *             {@link HDF5IOAdapterFactory#asRandomAccessFile(IHDF5Writer, String, HDF5GenericStorageFeatures, int, String)}
     *             instead.
     */
    @Deprecated
    public static HDF5DataSetRandomAccessFile createCompress(File hdf5File, String dataSetPath,
            int chunkSize)
    {
        return new HDF5DataSetRandomAccessFile(hdf5File, dataSetPath,
                HDF5GenericStorageFeatures.GENERIC_DEFLATE, chunkSize, null, false);
    }

    /**
     * Creates a {@link HDF5DataSetRandomAccessFile} in read-write mode. If the dataset has to be
     * created, create an opaque data type.
     * 
     * @param hdf5File The file of the HDF5 container.
     * @param dataSetPath The path of the HDF5 dataset in the HDF5 container to use as a file.
     * @param opaqueTag If the data does not yet exist, use this value as the tag of the opaque data
     *            type.
     * @return The {@link HDF5DataSetRandomAccessFile}.
     * @deprecated Use
     *             {@link HDF5IOAdapterFactory#asRandomAccessFile(IHDF5Writer, String, HDF5GenericStorageFeatures, int, String)}
     *             instead.
     */
    @Deprecated
    public static HDF5DataSetRandomAccessFile createOpaque(File hdf5File, String dataSetPath,
            String opaqueTag)
    {
        return new HDF5DataSetRandomAccessFile(hdf5File, dataSetPath,
                HDF5GenericStorageFeatures.GENERIC_CHUNKED, DEFAULT_CHUNK_SIZE, opaqueTag, false);
    }

    /**
     * Creates a {@link HDF5DataSetRandomAccessFile} in read-write mode. If the dataset has to be
     * created, create an opaque data type.
     * 
     * @param hdf5File The file of the HDF5 container.
     * @param dataSetPath The path of the HDF5 dataset in the HDF5 container to use as a file.
     * @param opaqueTag If the data does not yet exist, use this value as the tag of the opaque data
     *            type.
     * @param chunkSize If the dataset does not yet exist, use this value as the chunk size.
     * @return The {@link HDF5DataSetRandomAccessFile}.
     * @deprecated Use
     *             {@link HDF5IOAdapterFactory#asRandomAccessFile(IHDF5Writer, String, HDF5GenericStorageFeatures, int, String)}
     *             instead.
     */
    @Deprecated
    public static HDF5DataSetRandomAccessFile createOpaque(File hdf5File, String dataSetPath,
            String opaqueTag, int chunkSize)
    {
        return new HDF5DataSetRandomAccessFile(hdf5File, dataSetPath,
                HDF5GenericStorageFeatures.GENERIC_CHUNKED, chunkSize, opaqueTag, false);
    }

    /**
     * Creates a {@link HDF5DataSetRandomAccessFile} in read-write mode with compression switched
     * on. If the dataset has to be created, create an opaque data type.
     * 
     * @param hdf5File The file of the HDF5 container.
     * @param dataSetPath The path of the HDF5 dataset in the HDF5 container to use as a file.
     * @param opaqueTag If the data does not yet exist, use this value as the tag of the opaque data
     *            type.
     * @return The {@link HDF5DataSetRandomAccessFile}.
     * @deprecated Use
     *             {@link HDF5IOAdapterFactory#asRandomAccessFile(IHDF5Writer, String, HDF5GenericStorageFeatures, int, String)}
     *             instead.
     */
    @Deprecated
    public static HDF5DataSetRandomAccessFile createOpaqueCompress(File hdf5File,
            String dataSetPath, String opaqueTag)
    {
        return new HDF5DataSetRandomAccessFile(hdf5File, dataSetPath,
                HDF5GenericStorageFeatures.GENERIC_DEFLATE, DEFAULT_CHUNK_SIZE, opaqueTag, false);
    }

    /**
     * Creates a {@link HDF5DataSetRandomAccessFile} in read-write mode with compression switched
     * on. If the dataset has to be created, create an opaque data type.
     * 
     * @param hdf5File The file of the HDF5 container.
     * @param dataSetPath The path of the HDF5 dataset in the HDF5 container to use as a file.
     * @param opaqueTag If the data does not yet exist, use this value as the tag of the opaque data
     *            type.
     * @param chunkSize If the dataset does not yet exist, use this value as the chunk size.
     * @return The {@link HDF5DataSetRandomAccessFile}.
     * @deprecated Use
     *             {@link HDF5IOAdapterFactory#asRandomAccessFile(IHDF5Writer, String, HDF5GenericStorageFeatures, int, String)}
     *             instead.
     */
    @Deprecated
    public static HDF5DataSetRandomAccessFile createOpaqueCompress(File hdf5File,
            String dataSetPath, String opaqueTag, int chunkSize)
    {
        return new HDF5DataSetRandomAccessFile(hdf5File, dataSetPath,
                HDF5GenericStorageFeatures.GENERIC_DEFLATE, chunkSize, opaqueTag, false);
    }

    /**
     * Creates a {@link HDF5DataSetRandomAccessFile} in read-write mode, giving full controll on the
     * storage features.
     * <p>
     * <i>Note that a <code>CONTIGUOUS</code> or <code>COMPACT</code> dataset has a fixed size and
     * cannot grow or shrink. Consequently, {@link #setLength(long)} or {@link #seek(long)} beyond
     * the end of the dataset does not work with such datasets but throws an exception.</i>
     * 
     * @param hdf5File The file of the HDF5 container.
     * @param dataSetPath The path of the HDF5 dataset in the HDF5 container to use as a file.
     * @param opaqueTagOrNull If the data does not yet exist and this value is not <code>null</code>
     *            , then use this value as the tag of the opaque data type.
     * @param size If the dataset does not yet exist, use this value as the chunk size, if the new
     *            data set is chunked, and as size, if the new data set is not chunked (
     *            <code>CONTIGUOUS</code> or <code>COMPACT</code>).
     * @param creationStorageFeature If the dataset does not yet exist, use this value as the
     *            storage features of the new dataset.
     * @return The {@link HDF5DataSetRandomAccessFile}.
     * @deprecated Use
     *             {@link HDF5IOAdapterFactory#asRandomAccessFile(IHDF5Writer, String, HDF5GenericStorageFeatures, int, String)}
     *             instead.
     */
    @Deprecated
    public static HDF5DataSetRandomAccessFile createFullControl(File hdf5File, String dataSetPath,
            String opaqueTagOrNull, int size, HDF5GenericStorageFeatures creationStorageFeature)
    {
        return new HDF5DataSetRandomAccessFile(hdf5File, dataSetPath, creationStorageFeature, size,
                opaqueTagOrNull, false);
    }

    /**
     * Creates a new HDF5DataSetRandomAccessFile for the given hdf5File and dataSetPath.
     */
    HDF5DataSetRandomAccessFile(File hdf5File, String dataSetPath,
            HDF5GenericStorageFeatures creationStorageFeature, int size, String opaqueTagOrNull,
            boolean readOnly)
    {
        this(createHDF5ReaderOrWriter(hdf5File, readOnly), dataSetPath, creationStorageFeature,
                size, opaqueTagOrNull, true);
    }

    private static IHDF5Reader createHDF5ReaderOrWriter(File hdf5File, boolean readOnly)
    {
        try
        {
            if (readOnly)
            {
                return HDF5FactoryProvider.get().openForReading(hdf5File);
            } else
            {
                return HDF5FactoryProvider.get().open(hdf5File);
            }
        } catch (HDF5FileNotFoundException ex)
        {
            throw new IOExceptionUnchecked(new FileNotFoundException(ex.getMessage()));
        } catch (HDF5Exception ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    /**
     * Creates a new HDF5DataSetRandomAccessFile for the given reader and dataSetPath.
     * <p>
     * If <code>reader instanceof IHDF5Writer</code>, the random access file will be in read-write
     * mode, else it will be in readonly mode.
     */
    HDF5DataSetRandomAccessFile(IHDF5Reader reader, String dataSetPath,
            HDF5GenericStorageFeatures creationStorageFeature, int size, String opaqueTagOrNull,
            boolean closeReaderOnCloseFile) throws IOExceptionUnchecked
    {
        this.closeReaderOnCloseFile = closeReaderOnCloseFile;
        final boolean readOnly = (reader instanceof IHDF5Writer) == false;
        try
        {
            if (readOnly)
            {
                this.reader = reader;
                this.writerOrNull = null;
            } else
            {
                this.writerOrNull = (IHDF5Writer) reader;
                this.reader = writerOrNull;
                if (writerOrNull.exists(dataSetPath) == false)
                {
                    long maxSize = requiresFixedMaxSize(creationStorageFeature) ? size : 0;
                    if (opaqueTagOrNull == null)
                    {
                        writerOrNull.createByteArray(dataSetPath, maxSize, size,
                                HDF5IntStorageFeatures.createFromGeneric(creationStorageFeature));
                    } else
                    {
                        writerOrNull.createOpaqueByteArray(dataSetPath, opaqueTagOrNull, maxSize,
                                size, creationStorageFeature);
                    }
                }
            }
        } catch (HDF5Exception ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
        this.dataSetPath = dataSetPath;
        this.dataSetInfo = reader.getDataSetInformation(dataSetPath);
        if (readOnly == false
                && dataSetInfo.getTypeInformation().getDataClass() == HDF5DataClass.OPAQUE)
        {
            this.opaqueTypeOrNull = reader.tryGetOpaqueType(dataSetPath);
        } else
        {
            this.opaqueTypeOrNull = null;
        }
        if (dataSetInfo.getRank() != 1)
        {
            throw new IOExceptionUnchecked("Dataset has wrong rank (r=" + dataSetInfo.getRank()
                    + ")");
        }
        if (dataSetInfo.getTypeInformation().getElementSize() != 1)
        {
            throw new IOExceptionUnchecked("Dataset has wrong element size (size="
                    + dataSetInfo.getTypeInformation().getElementSize() + " bytes)");
        }
        this.length = dataSetInfo.getSize();

        // Chunked data sets are read chunk by chunk, other layouts are read completely.
        if (dataSetInfo.getStorageLayout() == HDF5StorageLayout.CHUNKED)
        {
            this.blockSize = dataSetInfo.tryGetChunkSizes()[0];
        } else
        {
            // Limitation: we do not yet handle the case of contiguous data sets larger than 2GB
            if ((int) length != length())
            {
                throw new IOExceptionUnchecked("Dataset is too large (size=" + length + " bytes)");

            }
            this.blockSize = (int) length;
        }
        this.extendable = (dataSetInfo.getStorageLayout() == HDF5StorageLayout.CHUNKED);
        this.blockOffset = 0;
        this.block = new byte[blockSize];
        this.realBlockSize = -1;
        this.positionInBlock = 0;
    }

    private static boolean requiresFixedMaxSize(HDF5GenericStorageFeatures features)
    {
        return features.tryGetProposedLayout() != null
                && features.tryGetProposedLayout() != HDF5StorageLayout.CHUNKED;
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        close();
    }

    private void ensureInitalizedForWriting(int lenCurrentOp) throws IOExceptionUnchecked
    {
        if (realBlockSize < 0)
        {
            realBlockSize = blockSize;
            long minLen = blockOffset + realBlockSize;
            final long oldLength = length();
            if (minLen > oldLength)
            {
                realBlockSize = Math.min(realBlockSize, lenCurrentOp);
                minLen = blockOffset + realBlockSize;
                if (minLen > oldLength)
                {
                    setLength(minLen);
                }
            }
            if ((oldLength - blockSize) > 0)
            {
                try
                {
                    this.realBlockSize =
                            reader.readAsByteArrayToBlockWithOffset(dataSetPath, block,
                                    realBlockSize, blockOffset, 0);
                } catch (HDF5Exception ex)
                {
                    throw new IOExceptionUnchecked(ex);
                }
            } else
            {
                Arrays.fill(block, (byte) 0);
            }
        }
    }

    private void ensureInitalizedForReading() throws IOExceptionUnchecked
    {
        if (realBlockSize < 0)
        {
            try
            {
                this.realBlockSize =
                        reader.readAsByteArrayToBlockWithOffset(dataSetPath, block, blockSize,
                                blockOffset, 0);
            } catch (HDF5Exception ex)
            {
                throw new IOExceptionUnchecked(ex);
            }
        }
    }

    private void readBlock(long newBlockOffset) throws IOExceptionUnchecked
    {
        if (newBlockOffset != blockOffset)
        {
            flush();
            try
            {
                this.realBlockSize =
                        reader.readAsByteArrayToBlockWithOffset(dataSetPath, block, blockSize,
                                newBlockOffset, 0);
            } catch (HDF5Exception ex)
            {
                throw new IOExceptionUnchecked(ex);
            }
            this.blockOffset = newBlockOffset;
        }
    }

    private void readNextBlockResetPosition()
    {
        readBlock(blockOffset + realBlockSize);
        this.positionInBlock = 0;
    }

    private boolean eof()
    {
        return (available() == 0);
    }

    private void checkEoFOnWrite()
    {
        if (extendable == false && eof())
        {
            throw new IOExceptionUnchecked(new EOFException("Dataset is EOF and not extendable."));
        }
    }

    public File getHdf5File()
    {
        return reader.getFile();
    }

    public String getDataSetPath()
    {
        return dataSetPath;
    }

    /**
     * Returns <code>true</code> if the HDF5 file has been opened in read-only mode.
     */
    public boolean isReadOnly()
    {
        return (writerOrNull == null);
    }

    private void extend(int numberOfBytesToExtend) throws IOExceptionUnchecked
    {
        final long len = length();
        final long pos = getFilePointer();
        final long newLen = pos + numberOfBytesToExtend;
        if (newLen > len)
        {
            if (extendable == false)
            {
                throw new IOExceptionUnchecked("Unable to extend dataset from " + len + " to "
                        + newLen + ": dataset is not extenable.");
            }
            setLength(pos + numberOfBytesToExtend);
        }
    }

    private void checkWrite(int lenCurrentOp) throws IOExceptionUnchecked
    {
        ensureInitalizedForWriting(lenCurrentOp);
        checkWriteDoNotExtend();
        if (extensionPending)
        {
            setLength(blockOffset + positionInBlock);
        }
    }

    private void checkWriteDoNotExtend() throws IOExceptionUnchecked
    {
        if (isReadOnly())
        {
            throw new IOExceptionUnchecked("HDF5 dataset opened in read-only mode.");
        }
    }

    public long getFilePointer() throws IOExceptionUnchecked
    {
        return blockOffset + positionInBlock;
    }

    public int read() throws IOExceptionUnchecked
    {
        ensureInitalizedForReading();
        if (positionInBlock == realBlockSize)
        {
            if (eof())
            {
                return -1;
            }
            readNextBlockResetPosition();
            if (eof())
            {
                return -1;
            }
        }
        return block[positionInBlock++] & 0xff;
    }

    public int read(byte[] b) throws IOExceptionUnchecked
    {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        ensureInitalizedForReading();
        int realLen = getRealLen(len);
        if (realLen == 0)
        {
            return -1;
        }
        int bytesLeft = realLen;
        int currentOff = off;
        while (bytesLeft > 0)
        {
            final int lenInBlock = Math.min(bytesLeft, bytesLeftInBlock());
            System.arraycopy(block, positionInBlock, b, currentOff, lenInBlock);
            positionInBlock += lenInBlock;
            currentOff += lenInBlock;
            bytesLeft -= lenInBlock;
            if (bytesLeft > 0)
            {
                readNextBlockResetPosition();
            }
        }
        return realLen;
    }

    private int bytesLeftInBlock()
    {
        return (realBlockSize - positionInBlock);
    }

    private int getRealLen(int len)
    {
        return Math.min(len, available());
    }

    private long getRealLen(long len)
    {
        return Math.min(len, available());
    }

    public long skip(long n) throws IOExceptionUnchecked
    {
        final long realN = getRealLen(n);
        seek(getFilePointer() + realN);
        return realN;
    }

    public int available()
    {
        return (int) Math.min(availableLong(), Integer.MAX_VALUE);
    }

    private long availableLong()
    {
        return length() - getFilePointer();
    }

    public void close() throws IOExceptionUnchecked
    {
        flush();
        if (closeReaderOnCloseFile)
        {
            try
            {
                reader.close();
            } catch (HDF5Exception ex)
            {
                throw new IOExceptionUnchecked(ex);
            }
        }
    }

    public void mark(int readlimit)
    {
        this.blockOffsetMark = blockOffset;
        this.positionInBlockMark = positionInBlock;
    }

    public void reset() throws IOExceptionUnchecked
    {
        if (blockOffsetMark < 0)
        {
            throw new IOExceptionUnchecked(new IOException("Stream not marked."));
        }
        readBlock(blockOffsetMark);
        this.positionInBlock = positionInBlockMark;
    }

    public boolean markSupported()
    {
        return true;
    }

    public void flush() throws IOExceptionUnchecked
    {
        if (isReadOnly() == false && blockDirty)
        {
            try
            {
                if (opaqueTypeOrNull != null)
                {
                    writerOrNull.writeOpaqueByteArrayBlockWithOffset(dataSetPath, opaqueTypeOrNull,
                            block, realBlockSize, blockOffset);
                } else
                {
                    writerOrNull.writeByteArrayBlockWithOffset(dataSetPath, block, realBlockSize,
                            blockOffset);
                }
            } catch (HDF5Exception ex)
            {
                throw new IOExceptionUnchecked(ex);
            }
            blockDirty = false;
        }
    }

    public void synchronize() throws IOExceptionUnchecked
    {
        if (writerOrNull != null)
        {
            flush();
            try
            {
                writerOrNull.flushSyncBlocking();
            } catch (HDF5Exception ex)
            {
                throw new IOExceptionUnchecked(ex);
            }
        }
    }

    public ByteOrder getByteOrder()
    {
        return byteOrder == ch.systemsx.cisd.base.convert.NativeData.ByteOrder.BIG_ENDIAN ? ByteOrder.BIG_ENDIAN
                : ByteOrder.LITTLE_ENDIAN;
    }

    public void setByteOrder(ByteOrder byteOrder)
    {
        if (byteOrder == ByteOrder.BIG_ENDIAN)
        {
            this.byteOrder = ch.systemsx.cisd.base.convert.NativeData.ByteOrder.BIG_ENDIAN;
        } else
        {
            this.byteOrder = ch.systemsx.cisd.base.convert.NativeData.ByteOrder.LITTLE_ENDIAN;
        }
    }

    public void seek(long pos) throws IOExceptionUnchecked
    {
        if (pos < 0)
        {
            throw new IOExceptionUnchecked("New position may not be negative.");
        }
        if (isReadOnly() && pos >= length())
        {
            throw new IOExceptionUnchecked(
                    "In read-only mode, new position may not be larger than file size.");
        }
        final long newBlockOffset = (pos / blockSize) * blockSize;
        this.positionInBlock = (int) (pos % blockSize);
        if (newBlockOffset < length())
        {
            readBlock(newBlockOffset);
        } else
        {
            this.blockOffset = newBlockOffset;
            this.realBlockSize = positionInBlock + 1;
        }
        if (pos >= length())
        {
            this.extensionPending = true;
        }
    }

    public long length() throws IOExceptionUnchecked
    {
        return length;
    }

    public void setLength(long newLength) throws IOExceptionUnchecked
    {
        checkWriteDoNotExtend();
        if (extendable == false)
        {
            throw new IOExceptionUnchecked("setLength() called on non-extendable dataset.");
        }
        try
        {
            writerOrNull.setDataSetSize(dataSetPath, newLength);
        } catch (HDF5Exception ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
        length = newLength;
    }

    public void readFully(byte[] b) throws IOExceptionUnchecked
    {
        readFully(b, 0, b.length);
    }

    public void readFully(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        final int bytesRead = read(b, off, len);
        if (bytesRead != len)
        {
            throw new IOExceptionUnchecked(new EOFException());
        }
    }

    public int skipBytes(int n) throws IOExceptionUnchecked
    {
        return (int) skip(n);
    }

    public boolean readBoolean() throws IOExceptionUnchecked
    {
        return readUnsignedByte() != 0;
    }

    public byte readByte() throws IOExceptionUnchecked
    {
        return (byte) readUnsignedByte();
    }

    public int readUnsignedByte() throws IOExceptionUnchecked
    {
        final int b = read();
        if (b < 0)
        {
            throw new IOExceptionUnchecked(new EOFException());
        }
        return b;
    }

    public short readShort() throws IOExceptionUnchecked
    {
        final byte[] byteArr = new byte[NativeData.SHORT_SIZE];
        readFully(byteArr);
        return NativeData.byteToShort(byteArr, byteOrder)[0];
    }

    public int readUnsignedShort() throws IOExceptionUnchecked
    {
        return readShort() & 0xffff;
    }

    public char readChar() throws IOExceptionUnchecked
    {
        final byte[] byteArr = new byte[NativeData.CHAR_SIZE];
        readFully(byteArr);
        return NativeData.byteToChar(byteArr, byteOrder)[0];
    }

    public int readInt() throws IOExceptionUnchecked
    {
        final byte[] byteArr = new byte[NativeData.INT_SIZE];
        readFully(byteArr);
        return NativeData.byteToInt(byteArr, byteOrder)[0];
    }

    public long readLong() throws IOExceptionUnchecked
    {
        final byte[] byteArr = new byte[NativeData.LONG_SIZE];
        readFully(byteArr);
        return NativeData.byteToLong(byteArr, byteOrder)[0];
    }

    public float readFloat() throws IOExceptionUnchecked
    {
        final byte[] byteArr = new byte[NativeData.FLOAT_SIZE];
        readFully(byteArr);
        return NativeData.byteToFloat(byteArr, byteOrder)[0];
    }

    public double readDouble() throws IOExceptionUnchecked
    {
        final byte[] byteArr = new byte[NativeData.DOUBLE_SIZE];
        readFully(byteArr);
        return NativeData.byteToDouble(byteArr, byteOrder)[0];
    }

    public String readLine() throws IOExceptionUnchecked
    {
        final StringBuilder builder = new StringBuilder();
        int b;
        boolean byteRead = false;
        while ((b = read()) >= 0)
        {
            byteRead = true;
            final char c = (char) b;
            if (c == '\r')
            {
                continue;
            }
            if (c == '\n')
            {
                break;
            }
            builder.append(c);
        }
        if (byteRead == false)
        {
            return null;
        } else
        {
            return builder.toString();
        }
    }

    public String readUTF() throws IOExceptionUnchecked
    {
        try
        {
            final byte[] strBuf = new byte[readUnsignedShort()];
            readFully(strBuf);
            return new String(strBuf, "UTF-8");
        } catch (UnsupportedEncodingException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void write(int b) throws IOExceptionUnchecked
    {
        checkWrite(1);
        extend(1);
        if (positionInBlock == realBlockSize)
        {
            checkEoFOnWrite();
            readNextBlockResetPosition();
            checkEoFOnWrite();
        }
        block[positionInBlock++] = (byte) b;
        blockDirty = true;
    }

    public void write(byte[] b) throws IOExceptionUnchecked
    {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        checkWrite(len);
        extend(len);
        int bytesLeft = len;
        int currentOff = off;
        while (bytesLeft > 0)
        {
            final int lenInBlock = Math.min(bytesLeft, bytesLeftInBlock());
            System.arraycopy(b, currentOff, block, positionInBlock, lenInBlock);
            blockDirty = true;
            positionInBlock += lenInBlock;
            currentOff += lenInBlock;
            bytesLeft -= lenInBlock;
            if (bytesLeft > 0)
            {
                readNextBlockResetPosition();
            }
        }
    }

    public void writeBoolean(boolean v) throws IOExceptionUnchecked
    {
        write(v ? 1 : 0);
    }

    public void writeByte(int v) throws IOExceptionUnchecked
    {
        write(v);
    }

    public void writeShort(int v) throws IOExceptionUnchecked
    {
        write(NativeData.shortToByte(new short[]
            { (short) v }, byteOrder));
    }

    public void writeChar(int v) throws IOExceptionUnchecked
    {
        write(NativeData.charToByte(new char[]
            { (char) v }, byteOrder));
    }

    public void writeInt(int v) throws IOExceptionUnchecked
    {
        write(NativeData.intToByte(new int[]
            { v }, byteOrder));
    }

    public void writeLong(long v) throws IOExceptionUnchecked
    {
        write(NativeData.longToByte(new long[]
            { v }, byteOrder));
    }

    public void writeFloat(float v) throws IOExceptionUnchecked
    {
        write(NativeData.floatToByte(new float[]
            { v }, byteOrder));
    }

    public void writeDouble(double v) throws IOExceptionUnchecked
    {
        write(NativeData.doubleToByte(new double[]
            { v }, byteOrder));
    }

    public void writeBytes(String s) throws IOExceptionUnchecked
    {
        for (int i = 0; i < s.length(); i++)
        {
            write((byte) s.charAt(i));
        }
    }

    public void writeChars(String s) throws IOExceptionUnchecked
    {
        for (int i = 0; i < s.length(); i++)
        {
            final char v = s.charAt(i);
            write((byte) ((v >>> 8) & 0xFF));
            write((byte) ((v >>> 0) & 0xFF));
        }
    }

    public void writeUTF(String str) throws IOExceptionUnchecked
    {
        try
        {
            final byte[] strBuf = str.getBytes("UTF-8");
            writeShort(strBuf.length);
            write(strBuf);
        } catch (UnsupportedEncodingException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
