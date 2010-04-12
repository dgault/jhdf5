/*
 * Copyright 2009 ETH Zuerich, CISD.
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


import static ch.systemsx.cisd.hdf5.HDF5IntStorageFeatures.INT_NO_COMPRESSION;
import static ncsa.hdf.hdf5lib.H5.H5Dwrite_long;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5P_DEFAULT;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5S_ALL;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_NATIVE_INT64;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_STD_I64LE;

import ncsa.hdf.hdf5lib.HDFNativeData;

import ch.systemsx.cisd.base.mdarray.MDArray;
import ch.systemsx.cisd.base.mdarray.MDLongArray;
import ch.systemsx.cisd.hdf5.cleanup.ICallableWithCleanUp;
import ch.systemsx.cisd.hdf5.cleanup.ICleanUpRegistry;

/**
 * The implementation of {@link IHDF5LongWriter}.
 * 
 * @author Bernd Rinn
 */
class HDF5LongWriter implements IHDF5LongWriter
{
    private final HDF5BaseWriter baseWriter;

    HDF5LongWriter(HDF5BaseWriter baseWriter)
    {
        assert baseWriter != null;

        this.baseWriter = baseWriter;
    }

    // /////////////////////
    // Attributes
    // /////////////////////

    public void setLongAttribute(final String objectPath, final String name, final long value)
    {
        assert objectPath != null;
        assert name != null;

        baseWriter.checkOpen();
        baseWriter.setAttribute(objectPath, name, H5T_STD_I64LE, H5T_NATIVE_INT64, HDFNativeData
                .longToByte(value));
    }

    public void setLongArrayAttribute(final String objectPath, final String name,
            final long[] value)
    {
        assert objectPath != null;
        assert name != null;
        assert value != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> setAttributeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final int memoryTypeId =
                            baseWriter.h5.createArrayType(H5T_NATIVE_INT64, value.length, registry);
                    final int storageTypeId =
                            baseWriter.h5.createArrayType(H5T_STD_I64LE, value.length, registry);
                    baseWriter.setAttribute(objectPath, name, storageTypeId, memoryTypeId,
                            HDFNativeData.longToByte(value));
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(setAttributeRunnable);
    }

    public void setLongMDArrayAttribute(final String objectPath, final String name,
            final MDLongArray value)
    {
        assert objectPath != null;
        assert name != null;
        assert value != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> addAttributeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final int memoryTypeId =
                            baseWriter.h5.createArrayType(H5T_NATIVE_INT64, value.dimensions(),
                                    registry);
                    final int storageTypeId =
                            baseWriter.h5.createArrayType(H5T_STD_I64LE, value.dimensions(),
                                    registry);
                    baseWriter.setAttribute(objectPath, name, storageTypeId, memoryTypeId,
                            HDFNativeData.longToByte(value.getAsFlatArray()));
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(addAttributeRunnable);
    }

    public void setLongMatrixAttribute(final String objectPath, final String name,
            final long[][] value)
    {
        setLongMDArrayAttribute(objectPath, name, new MDLongArray(value));
    }
    
    // /////////////////////
    // Data Sets
    // /////////////////////

    public void writeLong(final String objectPath, final long value)
    {
        assert objectPath != null;

        baseWriter.checkOpen();
        baseWriter.writeScalar(objectPath, H5T_STD_I64LE, H5T_NATIVE_INT64, HDFNativeData.
                longToByte(value));
    }

    public void writeLongArray(final String objectPath, final long[] data)
    {
        writeLongArray(objectPath, data, INT_NO_COMPRESSION);
    }

    public void writeLongArray(final String objectPath, final long[] data,
            final HDF5IntStorageFeatures features)
    {
        assert data != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseWriter.getDataSetId(objectPath, H5T_STD_I64LE, new long[]
                                { data.length }, 8, features, registry);
                    H5Dwrite_long(dataSetId, H5T_NATIVE_INT64, H5S_ALL, H5S_ALL, H5P_DEFAULT, 
                            data);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    public void createLongArray(final String objectPath, final int size)
    {
        createLongArray(objectPath, size, INT_NO_COMPRESSION);
    }

    public void createLongArray(final String objectPath, final long size, final int blockSize)
    {
        createLongArray(objectPath, size, blockSize, INT_NO_COMPRESSION);
    }

    public void createLongArray(final String objectPath, final int size,
            final HDF5IntStorageFeatures features)
    {
        assert objectPath != null;
        assert size >= 0;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> createRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    if (features.requiresChunking())
                    {
                        baseWriter.createDataSet(objectPath, H5T_STD_I64LE, features, new long[]
                            { 0 }, new long[]
                            { size }, 8, registry);

                    } else
                    {
                        baseWriter.createDataSet(objectPath, H5T_STD_I64LE, features, new long[]
                            { size }, null, 8, registry);
                    }
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(createRunnable);
    }

    public void createLongArray(final String objectPath, final long size, final int blockSize,
            final HDF5IntStorageFeatures features)
    {
        assert objectPath != null;
        assert size >= 0;
        assert blockSize >= 0 && (blockSize <= size || size == 0);

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> createRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    baseWriter.createDataSet(objectPath, H5T_STD_I64LE, features, new long[]
                        { size }, new long[]
                        { blockSize }, 8, registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(createRunnable);
    }

    public void writeLongArrayBlock(final String objectPath, final long[] data,
            final long blockNumber)
    {
        writeLongArrayBlockWithOffset(objectPath, data, data.length, data.length * blockNumber);
    }

    public void writeLongArrayBlockWithOffset(final String objectPath, final long[] data,
            final int dataSize, final long offset)
    {
        assert objectPath != null;
        assert data != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
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
                    H5Dwrite_long(dataSetId, H5T_NATIVE_INT64, memorySpaceId, dataSpaceId, 
                            H5P_DEFAULT, data);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    /**
     * Writes out a <code>long</code> matrix (array of rank 2).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>. All columns need to have the
     *            same length.
     */
    public void writeLongMatrix(final String objectPath, final long[][] data)
    {
        writeLongMatrix(objectPath, data, INT_NO_COMPRESSION);
    }

    public void writeLongMatrix(final String objectPath, final long[][] data, 
            final HDF5IntStorageFeatures features)
    {
        assert objectPath != null;
        assert data != null;
        assert HDF5Utils.areMatrixDimensionsConsistent(data);

        writeLongMDArray(objectPath, new MDLongArray(data), features);
    }

    public void createLongMatrix(final String objectPath, final int blockSizeX, 
            final int blockSizeY)
    {
        createLongMatrix(objectPath, 0, 0, blockSizeX, blockSizeY, INT_NO_COMPRESSION);
    }

    public void createLongMatrix(final String objectPath, final long sizeX, final long sizeY,
            final int blockSizeX, final int blockSizeY)
    {
        createLongMatrix(objectPath, sizeX, sizeY, blockSizeX, blockSizeY, INT_NO_COMPRESSION);
    }

    public void createLongMatrix(final String objectPath, final long sizeX, final long sizeY,
            final int blockSizeX, final int blockSizeY, final HDF5IntStorageFeatures features)
    {
        assert objectPath != null;
        assert sizeX >= 0;
        assert sizeY >= 0;
        assert blockSizeX >= 0 && (blockSizeX <= sizeX || sizeX == 0);
        assert blockSizeY >= 0 && (blockSizeY <= sizeY || sizeY == 0);

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> createRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final long[] dimensions = new long[]
                        { sizeX, sizeY };
                    final long[] blockDimensions = new long[]
                        { blockSizeX, blockSizeY };
                    baseWriter
                            .createDataSet(objectPath, H5T_STD_I64LE, features, dimensions,
                            blockDimensions, 8, registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(createRunnable);
    }

    public void writeLongMatrixBlock(final String objectPath, final long[][] data,
            final long blockNumberX, final long blockNumberY)
    {
        assert objectPath != null;
        assert data != null;

        writeLongMDArrayBlock(objectPath, new MDLongArray(data), new long[]
            { blockNumberX, blockNumberY });
    }

    public void writeLongMatrixBlockWithOffset(final String objectPath, final long[][] data,
            final long offsetX, final long offsetY)
    {
        assert objectPath != null;
        assert data != null;

        writeLongMDArrayBlockWithOffset(objectPath, new MDLongArray(data, new int[]
            { data.length, data[0].length }), new long[]
            { offsetX, offsetY });
    }

    public void writeLongMatrixBlockWithOffset(final String objectPath, final long[][] data,
            final int dataSizeX, final int dataSizeY, final long offsetX, final long offsetY)
    {
        assert objectPath != null;
        assert data != null;

        writeLongMDArrayBlockWithOffset(objectPath, new MDLongArray(data, new int[]
            { dataSizeX, dataSizeY }), new long[]
            { offsetX, offsetY });
    }

    public void writeLongMDArray(final String objectPath, final MDLongArray data)
    {
        writeLongMDArray(objectPath, data, INT_NO_COMPRESSION);
    }

    public void writeLongMDArray(final String objectPath, final MDLongArray data,
            final HDF5IntStorageFeatures features)
    {
        assert objectPath != null;
        assert data != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseWriter.getDataSetId(objectPath, H5T_STD_I64LE, 
                                    data.longDimensions(), 8, features, registry);
                    H5Dwrite_long(dataSetId, H5T_NATIVE_INT64, H5S_ALL, H5S_ALL, H5P_DEFAULT, 
                            data.getAsFlatArray());
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    public void createLongMDArray(final String objectPath, final int[] dimensions)
    {
        createLongMDArray(objectPath, dimensions, INT_NO_COMPRESSION);
    }

    public void createLongMDArray(final String objectPath, final long[] dimensions,
            final int[] blockDimensions)
    {
        createLongMDArray(objectPath, dimensions, blockDimensions, INT_NO_COMPRESSION);
    }

    public void createLongMDArray(final String objectPath, final int[] dimensions,
            final HDF5IntStorageFeatures features)
    {
        assert objectPath != null;
        assert dimensions != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> createRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    if (features.requiresChunking())
                    {
                        final long[] nullDimensions = new long[dimensions.length];
                        baseWriter.createDataSet(objectPath, H5T_STD_I64LE, features,
                                nullDimensions, MDArray.toLong(dimensions), 8, registry);
                    } else
                    {
                        baseWriter.createDataSet(objectPath, H5T_STD_I64LE, features, MDArray
                                .toLong(dimensions), null, 8, registry);
                    }
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(createRunnable);
    }

    public void createLongMDArray(final String objectPath, final long[] dimensions,
            final int[] blockDimensions, final HDF5IntStorageFeatures features)
    {
        assert objectPath != null;
        assert dimensions != null;
        assert blockDimensions != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> createRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    baseWriter.createDataSet(objectPath, H5T_STD_I64LE, features, dimensions, 
                            MDArray.toLong(blockDimensions), 8, registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(createRunnable);
    }

    public void writeLongMDArrayBlock(final String objectPath, final MDLongArray data,
            final long[] blockNumber)
    {
        assert blockNumber != null;

        final long[] dimensions = data.longDimensions();
        final long[] offset = new long[dimensions.length];
        for (int i = 0; i < offset.length; ++i)
        {
            offset[i] = blockNumber[i] * dimensions[i];
        }
        writeLongMDArrayBlockWithOffset(objectPath, data, offset);
    }

    public void writeLongMDArrayBlockWithOffset(final String objectPath, final MDLongArray data,
            final long[] offset)
    {
        assert objectPath != null;
        assert data != null;
        assert offset != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
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
                    H5Dwrite_long(dataSetId, H5T_NATIVE_INT64, memorySpaceId, dataSpaceId, 
                            H5P_DEFAULT, data.getAsFlatArray());
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    public void writeLongMDArrayBlockWithOffset(final String objectPath, final MDLongArray data,
            final int[] blockDimensions, final long[] offset, final int[] memoryOffset)
    {
        assert objectPath != null;
        assert data != null;
        assert offset != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final long[] memoryDimensions = data.longDimensions();
                    assert memoryDimensions.length == offset.length;
                    final long[] longBlockDimensions = MDArray.toLong(blockDimensions);
                    assert longBlockDimensions.length == offset.length;
                    final long[] dataSetDimensions = new long[blockDimensions.length];
                    for (int i = 0; i < offset.length; ++i)
                    {
                        dataSetDimensions[i] = offset[i] + blockDimensions[i];
                    }
                    final int dataSetId =
                            baseWriter.h5.openAndExtendDataSet(baseWriter.fileId, objectPath,
                                    baseWriter.fileFormat, dataSetDimensions, -1, registry);
                    final int dataSpaceId = 
                            baseWriter.h5.getDataSpaceForDataSet(dataSetId, registry);
                    baseWriter.h5.setHyperslabBlock(dataSpaceId, offset, longBlockDimensions);
                    final int memorySpaceId = 
                            baseWriter.h5.createSimpleDataSpace(memoryDimensions, registry);
                    baseWriter.h5.setHyperslabBlock(memorySpaceId, MDArray.toLong(memoryOffset),
                            longBlockDimensions);
                    H5Dwrite_long(dataSetId, H5T_NATIVE_INT64, memorySpaceId, dataSpaceId,
                            H5P_DEFAULT, data.getAsFlatArray());
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }
}
