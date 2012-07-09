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

import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_NATIVE_DOUBLE;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_ARRAY;

import java.util.Iterator;

import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import ncsa.hdf.hdf5lib.HDF5Constants;

import ch.systemsx.cisd.base.mdarray.MDArray;
import ch.systemsx.cisd.base.mdarray.MDDoubleArray;
import ch.systemsx.cisd.hdf5.cleanup.ICallableWithCleanUp;
import ch.systemsx.cisd.hdf5.cleanup.ICleanUpRegistry;
import ch.systemsx.cisd.hdf5.HDF5BaseReader.DataSpaceParameters;

/**
 * The implementation of {@link IHDF5DoubleReader}.
 * 
 * @author Bernd Rinn
 */
class HDF5DoubleReader implements IHDF5DoubleReader
{
    private final HDF5BaseReader baseReader;

    HDF5DoubleReader(HDF5BaseReader baseReader)
    {
        assert baseReader != null;

        this.baseReader = baseReader;
    }

    // /////////////////////
    // Attributes
    // /////////////////////

    public double getDoubleAttribute(final String objectPath, final String attributeName)
    {
        assert objectPath != null;
        assert attributeName != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<Double> getAttributeRunnable = new ICallableWithCleanUp<Double>()
            {
                public Double call(ICleanUpRegistry registry)
                {
                    final int objectId =
                            baseReader.h5.openObject(baseReader.fileId, objectPath, registry);
                    final int attributeId =
                            baseReader.h5.openAttribute(objectId, attributeName, registry);
                    final double[] data =
                            baseReader.h5
                                    .readAttributeAsDoubleArray(attributeId, H5T_NATIVE_DOUBLE, 1);
                    return data[0];
                }
            };
        return baseReader.runner.call(getAttributeRunnable);
    }

    public double[] getDoubleArrayAttribute(final String objectPath, final String attributeName)
    {
        assert objectPath != null;
        assert attributeName != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<double[]> getAttributeRunnable =
                new ICallableWithCleanUp<double[]>()
                    {
                        public double[] call(ICleanUpRegistry registry)
                        {
                            final int objectId =
                                    baseReader.h5.openObject(baseReader.fileId, objectPath,
                                            registry);
                            return getDoubleArrayAttribute(objectId, attributeName, registry);
                        }
                    };
        return baseReader.runner.call(getAttributeRunnable);
    }

    public MDDoubleArray getDoubleMDArrayAttribute(final String objectPath,
            final String attributeName)
    {
        assert objectPath != null;
        assert attributeName != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<MDDoubleArray> getAttributeRunnable =
                new ICallableWithCleanUp<MDDoubleArray>()
                    {
                        public MDDoubleArray call(ICleanUpRegistry registry)
                        {
                            final int objectId =
                                    baseReader.h5.openObject(baseReader.fileId, objectPath,
                                            registry);
                            return getDoubleMDArrayAttribute(objectId, attributeName, registry);
                        }
                    };
        return baseReader.runner.call(getAttributeRunnable);
    }

    public double[][] getDoubleMatrixAttribute(final String objectPath, final String attributeName)
            throws HDF5JavaException
    {
        final MDDoubleArray array = getDoubleMDArrayAttribute(objectPath, attributeName);
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

    public double readDouble(final String objectPath)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<Double> readCallable = new ICallableWithCleanUp<Double>()
            {
                public Double call(ICleanUpRegistry registry)
                {
                    final int dataSetId = 
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final double[] data = new double[1];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_DOUBLE, data);
                    return data[0];
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public double[] readDoubleArray(final String objectPath)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<double[]> readCallable = new ICallableWithCleanUp<double[]>()
            {
                public double[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId = 
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    return readDoubleArray(dataSetId, registry);
                }
            };
        return baseReader.runner.call(readCallable);
    }

    private double[] readDoubleArray(int dataSetId, ICleanUpRegistry registry)
    {
        try
        {
            final DataSpaceParameters spaceParams =
                    baseReader.getSpaceParameters(dataSetId, registry);
            final double[] data = new double[spaceParams.blockSize];
            baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_DOUBLE, spaceParams.memorySpaceId,
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
                    return readDoubleArrayFromArrayType(dataSetId, dataTypeId, registry);
                }
            }
            throw ex;
        }
    }

    private double[] readDoubleArrayFromArrayType(int dataSetId, final int dataTypeId,
            ICleanUpRegistry registry)
    {
        final int spaceId = baseReader.h5.createScalarDataSpace();
        final int[] dimensions = baseReader.h5.getArrayDimensions(dataTypeId);
        final double[] data = new double[HDF5Utils.getOneDimensionalArraySize(dimensions)];
        final int memoryDataTypeId =
                baseReader.h5.createArrayType(H5T_NATIVE_DOUBLE, data.length, registry);
        baseReader.h5.readDataSet(dataSetId, memoryDataTypeId, spaceId, spaceId, data);
        return data;
    }

    public int[] readToDoubleMDArrayWithOffset(final String objectPath, final MDDoubleArray array,
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
                            baseReader.getBlockSpaceParameters(dataSetId, memoryOffset, array
                                    .dimensions(), registry);
                    final int nativeDataTypeId =
                            baseReader.getNativeDataTypeId(dataSetId, H5T_NATIVE_DOUBLE, registry);
                    baseReader.h5.readDataSet(dataSetId, nativeDataTypeId, 
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId, array.
                            getAsFlatArray());
                    return MDArray.toInt(spaceParams.dimensions);
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public int[] readToDoubleMDArrayBlockWithOffset(final String objectPath,
            final MDDoubleArray array, final int[] blockDimensions, final long[] offset,
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
                            baseReader.getBlockSpaceParameters(dataSetId, memoryOffset, array
                                    .dimensions(), offset, blockDimensions, registry);
                    final int nativeDataTypeId =
                            baseReader.getNativeDataTypeId(dataSetId, H5T_NATIVE_DOUBLE, registry);
                    baseReader.h5.readDataSet(dataSetId, nativeDataTypeId, 
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId, array
                            .getAsFlatArray());
                    return MDArray.toInt(spaceParams.dimensions);
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public double[] readDoubleArrayBlock(final String objectPath, final int blockSize,
            final long blockNumber)
    {
        return readDoubleArrayBlockWithOffset(objectPath, blockSize, blockNumber * blockSize);
    }

    public double[] readDoubleArrayBlockWithOffset(final String objectPath, final int blockSize,
            final long offset)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<double[]> readCallable = new ICallableWithCleanUp<double[]>()
            {
                public double[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId = 
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, offset, blockSize, registry);
                    final double[] data = new double[spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_DOUBLE, spaceParams.memorySpaceId,
                            spaceParams.dataSpaceId, data);
                    return data;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public double[][] readDoubleMatrix(final String objectPath) throws HDF5JavaException
    {
        final MDDoubleArray array = readDoubleMDArray(objectPath);
        if (array.rank() != 2)
        {
            throw new HDF5JavaException("Array is supposed to be of rank 2, but is of rank "
                    + array.rank());
        }
        return array.toMatrix();
    }

    public double[][] readDoubleMatrixBlock(final String objectPath, final int blockSizeX,
            final int blockSizeY, final long blockNumberX, final long blockNumberY) 
            throws HDF5JavaException
    {
        final MDDoubleArray array = readDoubleMDArrayBlock(objectPath, new int[]
            { blockSizeX, blockSizeY }, new long[]
            { blockNumberX, blockNumberY });
        if (array.rank() != 2)
        {
            throw new HDF5JavaException("Array is supposed to be of rank 2, but is of rank "
                    + array.rank());
        }
        return array.toMatrix();
    }

    public double[][] readDoubleMatrixBlockWithOffset(final String objectPath, final int blockSizeX,
            final int blockSizeY, final long offsetX, final long offsetY) throws HDF5JavaException
    {
        final MDDoubleArray array = readDoubleMDArrayBlockWithOffset(objectPath, new int[]
            { blockSizeX, blockSizeY }, new long[]
            { offsetX, offsetY });
        if (array.rank() != 2)
        {
            throw new HDF5JavaException("Array is supposed to be of rank 2, but is of rank "
                    + array.rank());
        }
        return array.toMatrix();
    }

    public MDDoubleArray readDoubleMDArray(final String objectPath)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<MDDoubleArray> readCallable = new ICallableWithCleanUp<MDDoubleArray>()
            {
                public MDDoubleArray call(ICleanUpRegistry registry)
                {
                    final int dataSetId = 
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    return readDoubleMDArray(dataSetId, registry);
                }
            };
        return baseReader.runner.call(readCallable);
    }

    private MDDoubleArray readDoubleMDArray(int dataSetId, ICleanUpRegistry registry)
    {
        try
        {
            final DataSpaceParameters spaceParams =
                    baseReader.getSpaceParameters(dataSetId, registry);
            final double[] data = new double[spaceParams.blockSize];
            baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_DOUBLE, spaceParams.memorySpaceId,
                    spaceParams.dataSpaceId, data);
            return new MDDoubleArray(data, spaceParams.dimensions);
        } catch (HDF5LibraryException ex)
        {
            if (ex.getMajorErrorNumber() == HDF5Constants.H5E_DATATYPE
                    && ex.getMinorErrorNumber() == HDF5Constants.H5E_CANTINIT)
            {
                // Check whether it is an array data type.
                final int dataTypeId = baseReader.h5.getDataTypeForDataSet(dataSetId, registry);
                if (baseReader.h5.getClassType(dataTypeId) == HDF5Constants.H5T_ARRAY)
                {
                    return readDoubleMDArrayFromArrayType(dataSetId, dataTypeId, registry);
                }
            }
            throw ex;
        }
    }

    private MDDoubleArray readDoubleMDArrayFromArrayType(int dataSetId, final int dataTypeId,
            ICleanUpRegistry registry)
    {
        final int spaceId = baseReader.h5.createScalarDataSpace();
        final int[] dimensions = baseReader.h5.getArrayDimensions(dataTypeId);
        final double[] data = new double[MDArray.getLength(dimensions)];
        final int memoryDataTypeId =
                baseReader.h5.createArrayType(H5T_NATIVE_DOUBLE, dimensions, registry);
        baseReader.h5.readDataSet(dataSetId, memoryDataTypeId, spaceId, spaceId, data);
        return new MDDoubleArray(data, dimensions);
    }

    public MDDoubleArray readDoubleMDArrayBlock(final String objectPath, final int[] blockDimensions,
            final long[] blockNumber)
    {
        final long[] offset = new long[blockDimensions.length];
        for (int i = 0; i < offset.length; ++i)
        {
            offset[i] = blockNumber[i] * blockDimensions[i];
        }
        return readDoubleMDArrayBlockWithOffset(objectPath, blockDimensions, offset);
    }

    public MDDoubleArray readDoubleMDArrayBlockWithOffset(final String objectPath,
            final int[] blockDimensions, final long[] offset)
    {
        assert objectPath != null;
        assert blockDimensions != null;
        assert offset != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<MDDoubleArray> readCallable = new ICallableWithCleanUp<MDDoubleArray>()
            {
                public MDDoubleArray call(ICleanUpRegistry registry)
                {
                    final int dataSetId = 
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, offset, blockDimensions, 
                                    registry);
                    final double[] dataBlock = new double[spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_DOUBLE, spaceParams.memorySpaceId,
                            spaceParams.dataSpaceId, dataBlock);
                    return new MDDoubleArray(dataBlock, blockDimensions);
                }
            };
        return baseReader.runner.call(readCallable);
    }
    
    public Iterable<HDF5DataBlock<double[]>> getDoubleArrayNaturalBlocks(final String dataSetPath)
            throws HDF5JavaException
    {
        baseReader.checkOpen();
        final HDF5NaturalBlock1DParameters params =
                new HDF5NaturalBlock1DParameters(baseReader.getDataSetInformation(dataSetPath));

        return new Iterable<HDF5DataBlock<double[]>>()
            {
                public Iterator<HDF5DataBlock<double[]>> iterator()
                {
                    return new Iterator<HDF5DataBlock<double[]>>()
                        {
                            final HDF5NaturalBlock1DParameters.HDF5NaturalBlock1DIndex index =
                                    params.getNaturalBlockIndex();

                            public boolean hasNext()
                            {
                                return index.hasNext();
                            }

                            public HDF5DataBlock<double[]> next()
                            {
                                final long offset = index.computeOffsetAndSizeGetOffset();
                                final double[] block =
                                        readDoubleArrayBlockWithOffset(dataSetPath, index
                                                .getBlockSize(), offset);
                                return new HDF5DataBlock<double[]>(block, index.getAndIncIndex(), 
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

    public Iterable<HDF5MDDataBlock<MDDoubleArray>> getDoubleMDArrayNaturalBlocks(final String dataSetPath)
    {
        baseReader.checkOpen();
        final HDF5NaturalBlockMDParameters params =
                new HDF5NaturalBlockMDParameters(baseReader.getDataSetInformation(dataSetPath));

        return new Iterable<HDF5MDDataBlock<MDDoubleArray>>()
            {
                public Iterator<HDF5MDDataBlock<MDDoubleArray>> iterator()
                {
                    return new Iterator<HDF5MDDataBlock<MDDoubleArray>>()
                        {
                            final HDF5NaturalBlockMDParameters.HDF5NaturalBlockMDIndex index =
                                    params.getNaturalBlockIndex();

                            public boolean hasNext()
                            {
                                return index.hasNext();
                            }

                            public HDF5MDDataBlock<MDDoubleArray> next()
                            {
                                final long[] offset = index.computeOffsetAndSizeGetOffsetClone();
                                final MDDoubleArray data =
                                        readDoubleMDArrayBlockWithOffset(dataSetPath, index
                                                .getBlockSize(), offset);
                                return new HDF5MDDataBlock<MDDoubleArray>(data, index
                                        .getIndexClone(), offset);
                            }

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    double[] getDoubleArrayAttribute(final int objectId, final String attributeName,
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
                    baseReader.h5.createArrayType(H5T_NATIVE_DOUBLE, len,
                            registry);
        } else
        {
            final long[] arrayDimensions =
                    baseReader.h5.getDataDimensionsForAttribute(attributeId,
                            registry);
            memoryTypeId = H5T_NATIVE_DOUBLE;
            len = HDF5Utils.getOneDimensionalArraySize(arrayDimensions);
        }
        final double[] data =
                baseReader.h5.readAttributeAsDoubleArray(attributeId,
                        memoryTypeId, len);
        return data;
    }

    MDDoubleArray getDoubleMDArrayAttribute(final int objectId,
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
                        baseReader.h5.createArrayType(H5T_NATIVE_DOUBLE,
                                arrayDimensions, registry);
            } else
            {
                arrayDimensions =
                        MDArray.toInt(baseReader.h5.getDataDimensionsForAttribute(
                                attributeId, registry));
                memoryTypeId = H5T_NATIVE_DOUBLE;
            }
            final int len = MDArray.getLength(arrayDimensions);
            final double[] data =
                    baseReader.h5.readAttributeAsDoubleArray(attributeId,
                            memoryTypeId, len);
            return new MDDoubleArray(data, arrayDimensions);
        } catch (IllegalArgumentException ex)
        {
            throw new HDF5JavaException(ex.getMessage());
        }
    }
}
