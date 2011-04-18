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

import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_ARRAY;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_NATIVE_INT32;

import java.util.Iterator;

import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

import ch.systemsx.cisd.base.mdarray.MDArray;
import ch.systemsx.cisd.base.mdarray.MDIntArray;
import ch.systemsx.cisd.hdf5.HDF5BaseReader.DataSpaceParameters;
import ch.systemsx.cisd.hdf5.cleanup.ICallableWithCleanUp;
import ch.systemsx.cisd.hdf5.cleanup.ICleanUpRegistry;

/**
 * The implementation of {@link IHDF5IntReader}.
 * 
 * @author Bernd Rinn
 */
class HDF5IntReader implements IHDF5IntReader
{
    private final HDF5BaseReader baseReader;

    HDF5IntReader(HDF5BaseReader baseReader)
    {
        assert baseReader != null;

        this.baseReader = baseReader;
    }

    // /////////////////////
    // Attributes
    // /////////////////////

    public int getIntAttribute(final String objectPath, final String attributeName)
    {
        assert objectPath != null;
        assert attributeName != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<Integer> getAttributeRunnable =
                new ICallableWithCleanUp<Integer>()
                    {
                        public Integer call(ICleanUpRegistry registry)
                        {
                            final int objectId =
                                    baseReader.h5.openObject(baseReader.fileId, objectPath,
                                            registry);
                            final int attributeId =
                                    baseReader.h5.openAttribute(objectId, attributeName, registry);
                            final int[] data =
                                    baseReader.h5.readAttributeAsIntArray(attributeId,
                                            H5T_NATIVE_INT32, 1);
                            return data[0];
                        }
                    };
        return baseReader.runner.call(getAttributeRunnable);
    }

    public int[] getIntArrayAttribute(final String objectPath, final String attributeName)
    {
        assert objectPath != null;
        assert attributeName != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<int[]> getAttributeRunnable = new ICallableWithCleanUp<int[]>()
            {
                public int[] call(ICleanUpRegistry registry)
                {
                    final int objectId =
                            baseReader.h5.openObject(baseReader.fileId, objectPath, registry);
                    final int attributeId =
                            baseReader.h5.openAttribute(objectId, attributeName, registry);
                    final int attributeTypeId =
                            baseReader.h5.getDataTypeForAttribute(attributeId, registry);
                    final int memoryTypeId;
                    final int len;
                    if (baseReader.h5.getClassType(attributeTypeId) == H5T_ARRAY)
                    {
                        final int[] arrayDimensions =
                                baseReader.h5.getArrayDimensions(attributeTypeId);
                        if (arrayDimensions.length != 1)
                        {
                            throw new HDF5JavaException(
                                    "Array needs to be of rank 1, but is of rank "
                                            + arrayDimensions.length);
                        }
                        len = arrayDimensions[0];
                        memoryTypeId =
                                baseReader.h5.createArrayType(H5T_NATIVE_INT32, len, registry);
                    } else
                    {
                        final long[] arrayDimensions =
                                baseReader.h5.getDataDimensionsForAttribute(attributeId, registry);
                        memoryTypeId = H5T_NATIVE_INT32;
                        len = HDF5Utils.getOneDimensionalArraySize(arrayDimensions);
                    }
                    final int[] data =
                            baseReader.h5.readAttributeAsIntArray(attributeId, memoryTypeId, len);
                    return data;
                }
            };
        return baseReader.runner.call(getAttributeRunnable);
    }

    public MDIntArray getIntMDArrayAttribute(final String objectPath, final String attributeName)
    {
        assert objectPath != null;
        assert attributeName != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<MDIntArray> getAttributeRunnable =
                new ICallableWithCleanUp<MDIntArray>()
                    {
                        public MDIntArray call(ICleanUpRegistry registry)
                        {
                            try
                            {
                                final int objectId =
                                        baseReader.h5.openObject(baseReader.fileId, objectPath,
                                                registry);
                                final int attributeId =
                                        baseReader.h5.openAttribute(objectId, attributeName,
                                                registry);
                                final int attributeTypeId =
                                        baseReader.h5
                                                .getDataTypeForAttribute(attributeId, registry);
                                final int memoryTypeId;
                                final int[] arrayDimensions;
                                if (baseReader.h5.getClassType(attributeTypeId) == H5T_ARRAY)
                                {
                                    arrayDimensions =
                                            baseReader.h5.getArrayDimensions(attributeTypeId);
                                    memoryTypeId =
                                            baseReader.h5.createArrayType(H5T_NATIVE_INT32,
                                                    arrayDimensions, registry);
                                } else
                                {
                                    arrayDimensions =
                                            MDArray.toInt(baseReader.h5
                                                    .getDataDimensionsForAttribute(attributeId,
                                                            registry));
                                    memoryTypeId = H5T_NATIVE_INT32;
                                }
                                final int len = MDArray.getLength(arrayDimensions);
                                final int[] data =
                                        baseReader.h5.readAttributeAsIntArray(attributeId,
                                                memoryTypeId, len);
                                return new MDIntArray(data, arrayDimensions);
                            } catch (IllegalArgumentException ex)
                            {
                                throw new HDF5JavaException(ex.getMessage());
                            }
                        }
                    };
        return baseReader.runner.call(getAttributeRunnable);
    }

    public int[][] getIntMatrixAttribute(final String objectPath, final String attributeName)
            throws HDF5JavaException
    {
        final MDIntArray array = getIntMDArrayAttribute(objectPath, attributeName);
        if (array.rank() != 2)
        {
            throw new HDF5JavaException("Array is supposed to be of rank 2, but is of rank "
                    + array.rank());
        }
        return array.toMatrix();
    }

    // /////////////////////
    // Data Sets
    // /////////////////////

    public int readInt(final String objectPath)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<Integer> readCallable = new ICallableWithCleanUp<Integer>()
            {
                public Integer call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final int[] data = new int[1];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT32, data);
                    return data[0];
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public int[] readIntArray(final String objectPath)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<int[]> readCallable = new ICallableWithCleanUp<int[]>()
            {
                public int[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    return readIntArray(dataSetId, registry);
                }
            };
        return baseReader.runner.call(readCallable);
    }

    private int[] readIntArray(int dataSetId, ICleanUpRegistry registry)
    {
        try
        {
            final DataSpaceParameters spaceParams =
                    baseReader.getSpaceParameters(dataSetId, registry);
            final int[] data = new int[spaceParams.blockSize];
            baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT32, spaceParams.memorySpaceId,
                    spaceParams.dataSpaceId, data);
            return data;
        } catch (HDF5LibraryException ex)
        {
            if (ex.getMajorErrorNumber() == HDF5Constants.H5E_DATATYPE
                    && ex.getMinorErrorNumber() == HDF5Constants.H5E_CANTINIT)
            {
                // Check whether it is an array data type.
                final int dataTypeId = baseReader.h5.getDataTypeForDataSet(dataSetId, registry);
                if (baseReader.h5.getClassType(dataTypeId) == HDF5Constants.H5T_ARRAY)
                {
                    return readIntArrayFromArrayType(dataSetId, dataTypeId, registry);
                }
            }
            throw ex;
        }
    }

    private int[] readIntArrayFromArrayType(int dataSetId, final int dataTypeId,
            ICleanUpRegistry registry)
    {
        final int spaceId = baseReader.h5.createScalarDataSpace();
        final int[] dimensions = baseReader.h5.getArrayDimensions(dataTypeId);
        final int[] data = new int[HDF5Utils.getOneDimensionalArraySize(dimensions)];
        final int memoryDataTypeId =
                baseReader.h5.createArrayType(H5T_NATIVE_INT32, data.length, registry);
        baseReader.h5.readDataSet(dataSetId, memoryDataTypeId, spaceId, spaceId, data);
        return data;
    }

    public int[] readToIntMDArrayWithOffset(final String objectPath, final MDIntArray array,
            final int[] memoryOffset)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<int[]> readCallable = new ICallableWithCleanUp<int[]>()
            {
                public int[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getBlockSpaceParameters(dataSetId, memoryOffset,
                                    array.dimensions(), registry);
                    final int nativeDataTypeId =
                            baseReader.getNativeDataTypeId(dataSetId, H5T_NATIVE_INT32, registry);
                    baseReader.h5.readDataSet(dataSetId, nativeDataTypeId,
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId,
                            array.getAsFlatArray());
                    return MDArray.toInt(spaceParams.dimensions);
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public int[] readToIntMDArrayBlockWithOffset(final String objectPath, final MDIntArray array,
            final int[] blockDimensions, final long[] offset, final int[] memoryOffset)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<int[]> readCallable = new ICallableWithCleanUp<int[]>()
            {
                public int[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getBlockSpaceParameters(dataSetId, memoryOffset,
                                    array.dimensions(), offset, blockDimensions, registry);
                    final int nativeDataTypeId =
                            baseReader.getNativeDataTypeId(dataSetId, H5T_NATIVE_INT32, registry);
                    baseReader.h5.readDataSet(dataSetId, nativeDataTypeId,
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId,
                            array.getAsFlatArray());
                    return MDArray.toInt(spaceParams.dimensions);
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public int[] readIntArrayBlock(final String objectPath, final int blockSize,
            final long blockNumber)
    {
        return readIntArrayBlockWithOffset(objectPath, blockSize, blockNumber * blockSize);
    }

    public int[] readIntArrayBlockWithOffset(final String objectPath, final int blockSize,
            final long offset)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<int[]> readCallable = new ICallableWithCleanUp<int[]>()
            {
                public int[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, offset, blockSize, registry);
                    final int[] data = new int[spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT32,
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId, data);
                    return data;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public int[][] readIntMatrix(final String objectPath) throws HDF5JavaException
    {
        final MDIntArray array = readIntMDArray(objectPath);
        if (array.rank() != 2)
        {
            throw new HDF5JavaException("Array is supposed to be of rank 2, but is of rank "
                    + array.rank());
        }
        return array.toMatrix();
    }

    public int[][] readIntMatrixBlock(final String objectPath, final int blockSizeX,
            final int blockSizeY, final long blockNumberX, final long blockNumberY)
            throws HDF5JavaException
    {
        final MDIntArray array = readIntMDArrayBlock(objectPath, new int[]
            { blockSizeX, blockSizeY }, new long[]
            { blockNumberX, blockNumberY });
        if (array.rank() != 2)
        {
            throw new HDF5JavaException("Array is supposed to be of rank 2, but is of rank "
                    + array.rank());
        }
        return array.toMatrix();
    }

    public int[][] readIntMatrixBlockWithOffset(final String objectPath, final int blockSizeX,
            final int blockSizeY, final long offsetX, final long offsetY) throws HDF5JavaException
    {
        final MDIntArray array = readIntMDArrayBlockWithOffset(objectPath, new int[]
            { blockSizeX, blockSizeY }, new long[]
            { offsetX, offsetY });
        if (array.rank() != 2)
        {
            throw new HDF5JavaException("Array is supposed to be of rank 2, but is of rank "
                    + array.rank());
        }
        return array.toMatrix();
    }

    public MDIntArray readIntMDArray(final String objectPath)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<MDIntArray> readCallable =
                new ICallableWithCleanUp<MDIntArray>()
                    {
                        public MDIntArray call(ICleanUpRegistry registry)
                        {
                            final int dataSetId =
                                    baseReader.h5.openDataSet(baseReader.fileId, objectPath,
                                            registry);
                            return readIntMDArray(dataSetId, registry);
                        }
                    };
        return baseReader.runner.call(readCallable);
    }

    private MDIntArray readIntMDArray(int dataSetId, ICleanUpRegistry registry)
    {
        try
        {
            final DataSpaceParameters spaceParams =
                    baseReader.getSpaceParameters(dataSetId, registry);
            final int[] data = new int[spaceParams.blockSize];
            baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT32, spaceParams.memorySpaceId,
                    spaceParams.dataSpaceId, data);
            return new MDIntArray(data, spaceParams.dimensions);
        } catch (HDF5LibraryException ex)
        {
            if (ex.getMajorErrorNumber() == HDF5Constants.H5E_DATATYPE
                    && ex.getMinorErrorNumber() == HDF5Constants.H5E_CANTINIT)
            {
                // Check whether it is an array data type.
                final int dataTypeId = baseReader.h5.getDataTypeForDataSet(dataSetId, registry);
                if (baseReader.h5.getClassType(dataTypeId) == HDF5Constants.H5T_ARRAY)
                {
                    return readIntMDArrayFromArrayType(dataSetId, dataTypeId, registry);
                }
            }
            throw ex;
        }
    }

    private MDIntArray readIntMDArrayFromArrayType(int dataSetId, final int dataTypeId,
            ICleanUpRegistry registry)
    {
        final int spaceId = baseReader.h5.createScalarDataSpace();
        final int[] dimensions = baseReader.h5.getArrayDimensions(dataTypeId);
        final int[] data = new int[MDArray.getLength(dimensions)];
        final int memoryDataTypeId =
                baseReader.h5.createArrayType(H5T_NATIVE_INT32, dimensions, registry);
        baseReader.h5.readDataSet(dataSetId, memoryDataTypeId, spaceId, spaceId, data);
        return new MDIntArray(data, dimensions);
    }

    public MDIntArray readIntMDArrayBlock(final String objectPath, final int[] blockDimensions,
            final long[] blockNumber)
    {
        final long[] offset = new long[blockDimensions.length];
        for (int i = 0; i < offset.length; ++i)
        {
            offset[i] = blockNumber[i] * blockDimensions[i];
        }
        return readIntMDArrayBlockWithOffset(objectPath, blockDimensions, offset);
    }

    public MDIntArray readIntMDArrayBlockWithOffset(final String objectPath,
            final int[] blockDimensions, final long[] offset)
    {
        assert objectPath != null;
        assert blockDimensions != null;
        assert offset != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<MDIntArray> readCallable =
                new ICallableWithCleanUp<MDIntArray>()
                    {
                        public MDIntArray call(ICleanUpRegistry registry)
                        {
                            final int dataSetId =
                                    baseReader.h5.openDataSet(baseReader.fileId, objectPath,
                                            registry);
                            final DataSpaceParameters spaceParams =
                                    baseReader.getSpaceParameters(dataSetId, offset,
                                            blockDimensions, registry);
                            final int[] dataBlock = new int[spaceParams.blockSize];
                            baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT32,
                                    spaceParams.memorySpaceId, spaceParams.dataSpaceId, dataBlock);
                            return new MDIntArray(dataBlock, blockDimensions);
                        }
                    };
        return baseReader.runner.call(readCallable);
    }

    public Iterable<HDF5DataBlock<int[]>> getIntArrayNaturalBlocks(final String dataSetPath)
            throws HDF5JavaException
    {
        baseReader.checkOpen();
        final HDF5NaturalBlock1DParameters params =
                new HDF5NaturalBlock1DParameters(baseReader.getDataSetInformation(dataSetPath));

        return new Iterable<HDF5DataBlock<int[]>>()
            {
                public Iterator<HDF5DataBlock<int[]>> iterator()
                {
                    return new Iterator<HDF5DataBlock<int[]>>()
                        {
                            final HDF5NaturalBlock1DParameters.HDF5NaturalBlock1DIndex index =
                                    params.getNaturalBlockIndex();

                            public boolean hasNext()
                            {
                                return index.hasNext();
                            }

                            public HDF5DataBlock<int[]> next()
                            {
                                final long offset = index.computeOffsetAndSizeGetOffset();
                                final int[] block =
                                        readIntArrayBlockWithOffset(dataSetPath,
                                                index.getBlockSize(), offset);
                                return new HDF5DataBlock<int[]>(block, index.getAndIncIndex(),
                                        offset);
                            }

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    public Iterable<HDF5MDDataBlock<MDIntArray>> getIntMDArrayNaturalBlocks(final String dataSetPath)
    {
        baseReader.checkOpen();
        final HDF5NaturalBlockMDParameters params =
                new HDF5NaturalBlockMDParameters(baseReader.getDataSetInformation(dataSetPath));

        return new Iterable<HDF5MDDataBlock<MDIntArray>>()
            {
                public Iterator<HDF5MDDataBlock<MDIntArray>> iterator()
                {
                    return new Iterator<HDF5MDDataBlock<MDIntArray>>()
                        {
                            final HDF5NaturalBlockMDParameters.HDF5NaturalBlockMDIndex index =
                                    params.getNaturalBlockIndex();

                            public boolean hasNext()
                            {
                                return index.hasNext();
                            }

                            public HDF5MDDataBlock<MDIntArray> next()
                            {
                                final long[] offset = index.computeOffsetAndSizeGetOffsetClone();
                                final MDIntArray data =
                                        readIntMDArrayBlockWithOffset(dataSetPath,
                                                index.getBlockSize(), offset);
                                return new HDF5MDDataBlock<MDIntArray>(data, index.getIndexClone(),
                                        offset);
                            }

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }
}
