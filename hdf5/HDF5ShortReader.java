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

import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5T_NATIVE_INT16;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5T_ARRAY;

import java.util.Iterator;

import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

import ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants;
import ch.systemsx.cisd.base.mdarray.MDArray;
import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.cleanup.ICallableWithCleanUp;
import ch.systemsx.cisd.hdf5.cleanup.ICleanUpRegistry;
import ch.systemsx.cisd.hdf5.HDF5BaseReader.DataSpaceParameters;

/**
 * The implementation of {@link IHDF5ShortReader}.
 * 
 * @author Bernd Rinn
 */
class HDF5ShortReader implements IHDF5ShortReader
{
    private final HDF5BaseReader baseReader;

    HDF5ShortReader(HDF5BaseReader baseReader)
    {
        assert baseReader != null;

        this.baseReader = baseReader;
    }

    // /////////////////////
    // Attributes
    // /////////////////////

    @Override
    public short getShortAttribute(final String objectPath, final String attributeName)
    {
        assert objectPath != null;
        assert attributeName != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<Short> getAttributeRunnable = new ICallableWithCleanUp<Short>()
            {
                @Override
                public Short call(ICleanUpRegistry registry)
                {
                    final int objectId =
                            baseReader.h5.openObject(baseReader.fileId, objectPath, registry);
                    final int attributeId =
                            baseReader.h5.openAttribute(objectId, attributeName, registry);
                    final short[] data =
                            baseReader.h5
                                    .readAttributeAsShortArray(attributeId, H5T_NATIVE_INT16, 1);
                    return data[0];
                }
            };
        return baseReader.runner.call(getAttributeRunnable);
    }

    @Override
    public short[] getShortArrayAttribute(final String objectPath, final String attributeName)
    {
        assert objectPath != null;
        assert attributeName != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<short[]> getAttributeRunnable =
                new ICallableWithCleanUp<short[]>()
                    {
                        @Override
                        public short[] call(ICleanUpRegistry registry)
                        {
                            final int objectId =
                                    baseReader.h5.openObject(baseReader.fileId, objectPath,
                                            registry);
                            return getShortArrayAttribute(objectId, attributeName, registry);
                        }
                    };
        return baseReader.runner.call(getAttributeRunnable);
    }

    @Override
    public MDShortArray getShortMDArrayAttribute(final String objectPath,
            final String attributeName)
    {
        assert objectPath != null;
        assert attributeName != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<MDShortArray> getAttributeRunnable =
                new ICallableWithCleanUp<MDShortArray>()
                    {
                        @Override
                        public MDShortArray call(ICleanUpRegistry registry)
                        {
                            final int objectId =
                                    baseReader.h5.openObject(baseReader.fileId, objectPath,
                                            registry);
                            return getShortMDArrayAttribute(objectId, attributeName, registry);
                        }
                    };
        return baseReader.runner.call(getAttributeRunnable);
    }

    @Override
    public short[][] getShortMatrixAttribute(final String objectPath, final String attributeName)
            throws HDF5JavaException
    {
        final MDShortArray array = getShortMDArrayAttribute(objectPath, attributeName);
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

    @Override
    public short readShort(final String objectPath)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<Short> readCallable = new ICallableWithCleanUp<Short>()
            {
                @Override
                public Short call(ICleanUpRegistry registry)
                {
                    final int dataSetId = 
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final short[] data = new short[1];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT16, data);
                    return data[0];
                }
            };
        return baseReader.runner.call(readCallable);
    }

    @Override
    public short[] readShortArray(final String objectPath)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<short[]> readCallable = new ICallableWithCleanUp<short[]>()
            {
                @Override
                public short[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId = 
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    return readShortArray(dataSetId, registry);
                }
            };
        return baseReader.runner.call(readCallable);
    }

    private short[] readShortArray(int dataSetId, ICleanUpRegistry registry)
    {
        try
        {
            final DataSpaceParameters spaceParams =
                    baseReader.getSpaceParameters(dataSetId, registry);
            final short[] data = new short[spaceParams.blockSize];
            baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT16, spaceParams.memorySpaceId,
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
                    return readShortArrayFromArrayType(dataSetId, dataTypeId, registry);
                }
            }
            throw ex;
        }
    }

    private short[] readShortArrayFromArrayType(int dataSetId, final int dataTypeId,
            ICleanUpRegistry registry)
    {
        final int spaceId = baseReader.h5.createScalarDataSpace();
        final int[] dimensions = baseReader.h5.getArrayDimensions(dataTypeId);
        final short[] data = new short[HDF5Utils.getOneDimensionalArraySize(dimensions)];
        final int memoryDataTypeId =
                baseReader.h5.createArrayType(H5T_NATIVE_INT16, data.length, registry);
        baseReader.h5.readDataSet(dataSetId, memoryDataTypeId, spaceId, spaceId, data);
        return data;
    }

    @Override
    public int[] readToShortMDArrayWithOffset(final String objectPath, final MDShortArray array,
            final int[] memoryOffset)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<int[]> readCallable = new ICallableWithCleanUp<int[]>()
            {
                @Override
                public int[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId = 
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getBlockSpaceParameters(dataSetId, memoryOffset, array
                                    .dimensions(), registry);
                    final int nativeDataTypeId =
                            baseReader.getNativeDataTypeId(dataSetId, H5T_NATIVE_INT16, registry);
                    baseReader.h5.readDataSet(dataSetId, nativeDataTypeId, 
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId, array.
                            getAsFlatArray());
                    return MDArray.toInt(spaceParams.dimensions);
                }
            };
        return baseReader.runner.call(readCallable);
    }

    @Override
    public int[] readToShortMDArrayBlockWithOffset(final String objectPath,
            final MDShortArray array, final int[] blockDimensions, final long[] offset,
            final int[] memoryOffset)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<int[]> readCallable = new ICallableWithCleanUp<int[]>()
            {
                @Override
                public int[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId = 
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getBlockSpaceParameters(dataSetId, memoryOffset, array
                                    .dimensions(), offset, blockDimensions, registry);
                    final int nativeDataTypeId =
                            baseReader.getNativeDataTypeId(dataSetId, H5T_NATIVE_INT16, registry);
                    baseReader.h5.readDataSet(dataSetId, nativeDataTypeId, 
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId, array
                            .getAsFlatArray());
                    return MDArray.toInt(spaceParams.dimensions);
                }
            };
        return baseReader.runner.call(readCallable);
    }

    @Override
    public short[] readShortArrayBlock(final String objectPath, final int blockSize,
            final long blockNumber)
    {
        return readShortArrayBlockWithOffset(objectPath, blockSize, blockNumber * blockSize);
    }

    @Override
    public short[] readShortArrayBlockWithOffset(final String objectPath, final int blockSize,
            final long offset)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<short[]> readCallable = new ICallableWithCleanUp<short[]>()
            {
                @Override
                public short[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId = 
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, offset, blockSize, registry);
                    final short[] data = new short[spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT16, spaceParams.memorySpaceId,
                            spaceParams.dataSpaceId, data);
                    return data;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    @Override
    public short[][] readShortMatrix(final String objectPath) throws HDF5JavaException
    {
        final MDShortArray array = readShortMDArray(objectPath);
        if (array.rank() != 2)
        {
            throw new HDF5JavaException("Array is supposed to be of rank 2, but is of rank "
                    + array.rank());
        }
        return array.toMatrix();
    }

    @Override
    public short[][] readShortMatrixBlock(final String objectPath, final int blockSizeX,
            final int blockSizeY, final long blockNumberX, final long blockNumberY) 
            throws HDF5JavaException
    {
        final MDShortArray array = readShortMDArrayBlock(objectPath, new int[]
            { blockSizeX, blockSizeY }, new long[]
            { blockNumberX, blockNumberY });
        if (array.rank() != 2)
        {
            throw new HDF5JavaException("Array is supposed to be of rank 2, but is of rank "
                    + array.rank());
        }
        return array.toMatrix();
    }

    @Override
    public short[][] readShortMatrixBlockWithOffset(final String objectPath, final int blockSizeX,
            final int blockSizeY, final long offsetX, final long offsetY) throws HDF5JavaException
    {
        final MDShortArray array = readShortMDArrayBlockWithOffset(objectPath, new int[]
            { blockSizeX, blockSizeY }, new long[]
            { offsetX, offsetY });
        if (array.rank() != 2)
        {
            throw new HDF5JavaException("Array is supposed to be of rank 2, but is of rank "
                    + array.rank());
        }
        return array.toMatrix();
    }

    @Override
    public MDShortArray readShortMDArray(final String objectPath)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<MDShortArray> readCallable = new ICallableWithCleanUp<MDShortArray>()
            {
                @Override
                public MDShortArray call(ICleanUpRegistry registry)
                {
                    final int dataSetId = 
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    return readShortMDArray(dataSetId, registry);
                }
            };
        return baseReader.runner.call(readCallable);
    }

    private MDShortArray readShortMDArray(int dataSetId, ICleanUpRegistry registry)
    {
        try
        {
            final DataSpaceParameters spaceParams =
                    baseReader.getSpaceParameters(dataSetId, registry);
            final short[] data = new short[spaceParams.blockSize];
            baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT16, spaceParams.memorySpaceId,
                    spaceParams.dataSpaceId, data);
            return new MDShortArray(data, spaceParams.dimensions);
        } catch (HDF5LibraryException ex)
        {
            if (ex.getMajorErrorNumber() == HDF5Constants.H5E_DATATYPE
                    && ex.getMinorErrorNumber() == HDF5Constants.H5E_CANTINIT)
            {
                // Check whether it is an array data type.
                final int dataTypeId = baseReader.h5.getDataTypeForDataSet(dataSetId, registry);
                if (baseReader.h5.getClassType(dataTypeId) == HDF5Constants.H5T_ARRAY)
                {
                    return readShortMDArrayFromArrayType(dataSetId, dataTypeId, registry);
                }
            }
            throw ex;
        }
    }

    private MDShortArray readShortMDArrayFromArrayType(int dataSetId, final int dataTypeId,
            ICleanUpRegistry registry)
    {
        final int spaceId = baseReader.h5.createScalarDataSpace();
        final int[] dimensions = baseReader.h5.getArrayDimensions(dataTypeId);
        final short[] data = new short[MDArray.getLength(dimensions)];
        final int memoryDataTypeId =
                baseReader.h5.createArrayType(H5T_NATIVE_INT16, dimensions, registry);
        baseReader.h5.readDataSet(dataSetId, memoryDataTypeId, spaceId, spaceId, data);
        return new MDShortArray(data, dimensions);
    }

    @Override
    public MDShortArray readShortMDArrayBlock(final String objectPath, final int[] blockDimensions,
            final long[] blockNumber)
    {
        final long[] offset = new long[blockDimensions.length];
        for (int i = 0; i < offset.length; ++i)
        {
            offset[i] = blockNumber[i] * blockDimensions[i];
        }
        return readShortMDArrayBlockWithOffset(objectPath, blockDimensions, offset);
    }

    @Override
    public MDShortArray readShortMDArrayBlockWithOffset(final String objectPath,
            final int[] blockDimensions, final long[] offset)
    {
        assert objectPath != null;
        assert blockDimensions != null;
        assert offset != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<MDShortArray> readCallable = new ICallableWithCleanUp<MDShortArray>()
            {
                @Override
                public MDShortArray call(ICleanUpRegistry registry)
                {
                    final int dataSetId = 
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, offset, blockDimensions, 
                                    registry);
                    final short[] dataBlock = new short[spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT16, spaceParams.memorySpaceId,
                            spaceParams.dataSpaceId, dataBlock);
                    return new MDShortArray(dataBlock, blockDimensions);
                }
            };
        return baseReader.runner.call(readCallable);
    }
    
    @Override
    public Iterable<HDF5DataBlock<short[]>> getShortArrayNaturalBlocks(final String dataSetPath)
            throws HDF5JavaException
    {
        baseReader.checkOpen();
        final HDF5NaturalBlock1DParameters params =
                new HDF5NaturalBlock1DParameters(baseReader.getDataSetInformation(dataSetPath));

        return new Iterable<HDF5DataBlock<short[]>>()
            {
                @Override
                public Iterator<HDF5DataBlock<short[]>> iterator()
                {
                    return new Iterator<HDF5DataBlock<short[]>>()
                        {
                            final HDF5NaturalBlock1DParameters.HDF5NaturalBlock1DIndex index =
                                    params.getNaturalBlockIndex();

                            @Override
                            public boolean hasNext()
                            {
                                return index.hasNext();
                            }

                            @Override
                            public HDF5DataBlock<short[]> next()
                            {
                                final long offset = index.computeOffsetAndSizeGetOffset();
                                final short[] block =
                                        readShortArrayBlockWithOffset(dataSetPath, index
                                                .getBlockSize(), offset);
                                return new HDF5DataBlock<short[]>(block, index.getAndIncIndex(), 
                                        offset);
                            }

                            @Override
                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    @Override
    public Iterable<HDF5MDDataBlock<MDShortArray>> getShortMDArrayNaturalBlocks(final String dataSetPath)
    {
        baseReader.checkOpen();
        final HDF5NaturalBlockMDParameters params =
                new HDF5NaturalBlockMDParameters(baseReader.getDataSetInformation(dataSetPath));

        return new Iterable<HDF5MDDataBlock<MDShortArray>>()
            {
                @Override
                public Iterator<HDF5MDDataBlock<MDShortArray>> iterator()
                {
                    return new Iterator<HDF5MDDataBlock<MDShortArray>>()
                        {
                            final HDF5NaturalBlockMDParameters.HDF5NaturalBlockMDIndex index =
                                    params.getNaturalBlockIndex();

                            @Override
                            public boolean hasNext()
                            {
                                return index.hasNext();
                            }

                            @Override
                            public HDF5MDDataBlock<MDShortArray> next()
                            {
                                final long[] offset = index.computeOffsetAndSizeGetOffsetClone();
                                final MDShortArray data =
                                        readShortMDArrayBlockWithOffset(dataSetPath, index
                                                .getBlockSize(), offset);
                                return new HDF5MDDataBlock<MDShortArray>(data, index
                                        .getIndexClone(), offset);
                            }

                            @Override
                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    short[] getShortArrayAttribute(final int objectId, final String attributeName,
            ICleanUpRegistry registry)
    {
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
                    baseReader.h5.createArrayType(H5T_NATIVE_INT16, len,
                            registry);
        } else
        {
            final long[] arrayDimensions =
                    baseReader.h5.getDataDimensionsForAttribute(attributeId,
                            registry);
            memoryTypeId = H5T_NATIVE_INT16;
            len = HDF5Utils.getOneDimensionalArraySize(arrayDimensions);
        }
        final short[] data =
                baseReader.h5.readAttributeAsShortArray(attributeId,
                        memoryTypeId, len);
        return data;
    }

    MDShortArray getShortMDArrayAttribute(final int objectId,
            final String attributeName, ICleanUpRegistry registry)
    {
        try
        {
            final int attributeId =
                    baseReader.h5.openAttribute(objectId, attributeName, registry);
            final int attributeTypeId =
                    baseReader.h5.getDataTypeForAttribute(attributeId, registry);
            final int memoryTypeId;
            final int[] arrayDimensions;
            if (baseReader.h5.getClassType(attributeTypeId) == H5T_ARRAY)
            {
                arrayDimensions = baseReader.h5.getArrayDimensions(attributeTypeId);
                memoryTypeId =
                        baseReader.h5.createArrayType(H5T_NATIVE_INT16,
                                arrayDimensions, registry);
            } else
            {
                arrayDimensions =
                        MDArray.toInt(baseReader.h5.getDataDimensionsForAttribute(
                                attributeId, registry));
                memoryTypeId = H5T_NATIVE_INT16;
            }
            final int len = MDArray.getLength(arrayDimensions);
            final short[] data =
                    baseReader.h5.readAttributeAsShortArray(attributeId,
                            memoryTypeId, len);
            return new MDShortArray(data, arrayDimensions);
        } catch (IllegalArgumentException ex)
        {
            throw new HDF5JavaException(ex.getMessage());
        }
    }
}
