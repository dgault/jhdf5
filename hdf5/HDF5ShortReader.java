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

import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_NATIVE_INT16;

import java.util.Iterator;
import java.util.NoSuchElementException;

import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;

import ch.systemsx.cisd.common.array.MDArray;
import ch.systemsx.cisd.common.array.MDShortArray;
import ch.systemsx.cisd.hdf5.HDF5BaseReader.DataSpaceParameters;
import ch.systemsx.cisd.hdf5.HDF5DataSetInformation.StorageLayout;
import ch.systemsx.cisd.hdf5.cleanup.ICallableWithCleanUp;
import ch.systemsx.cisd.hdf5.cleanup.ICleanUpRegistry;

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

    public short readShort(final String objectPath)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<Short> readCallable = new ICallableWithCleanUp<Short>()
            {
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

    public short[] readShortArray(final String objectPath)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<short[]> readCallable = new ICallableWithCleanUp<short[]>()
            {
                public short[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId = 
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams = 
                            baseReader.getSpaceParameters(dataSetId, registry);
                    final short[] data = new short[spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT16, spaceParams.memorySpaceId,
                            spaceParams.dataSpaceId, data);
                    return data;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public void readToShortMDArrayWithOffset(final String objectPath, final MDShortArray array,
            final int[] memoryOffset)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<Void> readCallable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
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
                    return null; // Nothing to return.
                }
            };
        baseReader.runner.call(readCallable);
    }

    public void readToShortMDArrayBlockWithOffset(final String objectPath,
            final MDShortArray array, final int[] blockDimensions, final long[] offset,
            final int[] memoryOffset)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<Void> readCallable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
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
                    return null; // Nothing to return.
                }
            };
        baseReader.runner.call(readCallable);
    }

    public short[] readShortArrayBlock(final String objectPath, final int blockSize,
            final long blockNumber)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<short[]> readCallable = new ICallableWithCleanUp<short[]>()
            {
                public short[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId = 
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, blockNumber * blockSize,
                                    blockSize, registry);
                    final short[] data = new short[spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT16, spaceParams.memorySpaceId,
                            spaceParams.dataSpaceId, data);
                    return data;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public short[] readShortArrayBlockWithOffset(final String objectPath, final int blockSize,
            final long offset)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<short[]> readCallable = new ICallableWithCleanUp<short[]>()
            {
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

    public MDShortArray readShortMDArray(final String objectPath)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<MDShortArray> readCallable = new ICallableWithCleanUp<MDShortArray>()
            {
                public MDShortArray call(ICleanUpRegistry registry)
                {
                    final int dataSetId = 
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, registry);
                    final int nativeDataTypeId =
                            baseReader.getNativeDataTypeId(dataSetId, H5T_NATIVE_INT16, registry);
                    final short[] data = new short[spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, nativeDataTypeId, spaceParams.memorySpaceId,
                            spaceParams.dataSpaceId, data);
                    return new MDShortArray(data, spaceParams.dimensions);
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public MDShortArray readShortMDArrayBlock(final String objectPath, final int[] blockDimensions,
            final long[] blockNumber)
    {
        assert objectPath != null;
        assert blockDimensions != null;
        assert blockNumber != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<MDShortArray> readCallable = new ICallableWithCleanUp<MDShortArray>()
            {
                public MDShortArray call(ICleanUpRegistry registry)
                {
                    final long[] offset = new long[blockDimensions.length];
                    for (int i = 0; i < offset.length; ++i)
                    {
                        offset[i] = blockNumber[i] * blockDimensions[i];
                    }
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

    public MDShortArray readShortMDArrayBlockWithOffset(final String objectPath,
            final int[] blockDimensions, final long[] offset)
    {
        assert objectPath != null;
        assert blockDimensions != null;
        assert offset != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<MDShortArray> readCallable = new ICallableWithCleanUp<MDShortArray>()
            {
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
    
    public Iterable<HDF5DataBlock<short[]>> getShortArrayNaturalBlocks(final String dataSetPath)
            throws HDF5JavaException
    {
        baseReader.checkOpen();
        final HDF5DataSetInformation info = baseReader.getDataSetInformation(dataSetPath);
        if (info.getRank() > 1)
        {
            throw new HDF5JavaException("Data Set is expected to be of rank 1 (rank="
                    + info.getRank() + ")");
        }
        final long longSize = info.getDimensions()[0];
        final int size = (int) longSize;
        if (size != longSize)
        {
            throw new HDF5JavaException("Data Set is too large (" + longSize + ")");
        }
        final int naturalBlockSize =
                (info.getStorageLayout() == StorageLayout.CHUNKED) ? info.tryGetChunkSizes()[0]
                        : size;
        final int sizeModNaturalBlockSize = size % naturalBlockSize;
        final long numberOfBlocks =
                (size / naturalBlockSize) + (sizeModNaturalBlockSize != 0 ? 1 : 0);
        final int lastBlockSize =
                (sizeModNaturalBlockSize != 0) ? sizeModNaturalBlockSize : naturalBlockSize;

        return new Iterable<HDF5DataBlock<short[]>>()
            {
                public Iterator<HDF5DataBlock<short[]>> iterator()
                {
                    return new Iterator<HDF5DataBlock<short[]>>()
                        {
                            long index = 0;

                            public boolean hasNext()
                            {
                                return index < numberOfBlocks;
                            }

                            public HDF5DataBlock<short[]> next()
                            {
                                if (hasNext() == false)
                                {
                                    throw new NoSuchElementException();
                                }
                                final long offset = naturalBlockSize * index;
                                final int blockSize =
                                        (index == numberOfBlocks - 1) ? lastBlockSize
                                                : naturalBlockSize;
                                final short[] block =
                                        readShortArrayBlockWithOffset(dataSetPath, blockSize,
                                                offset);
                                return new HDF5DataBlock<short[]>(block, index++, offset);
                            }

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    public Iterable<HDF5MDDataBlock<MDShortArray>> getShortMDArrayNaturalBlocks(final String dataSetPath)
    {
        baseReader.checkOpen();
        final HDF5DataSetInformation info = baseReader.getDataSetInformation(dataSetPath);
        final int rank = info.getRank();
        final int[] size = MDArray.toInt(info.getDimensions());
        final int[] naturalBlockSize =
                (info.getStorageLayout() == StorageLayout.CHUNKED) ? info.tryGetChunkSizes() : size;
        final long[] numberOfBlocks = new long[rank];
        final int[] lastBlockSize = new int[rank];
        for (int i = 0; i < size.length; ++i)
        {
            final int sizeModNaturalBlockSize = size[i] % naturalBlockSize[i];
            numberOfBlocks[i] =
                    (size[i] / naturalBlockSize[i]) + (sizeModNaturalBlockSize != 0 ? 1 : 0);
            lastBlockSize[i] =
                    (sizeModNaturalBlockSize != 0) ? sizeModNaturalBlockSize : naturalBlockSize[i];
        }

        return new Iterable<HDF5MDDataBlock<MDShortArray>>()
            {
                public Iterator<HDF5MDDataBlock<MDShortArray>> iterator()
                {
                    return new Iterator<HDF5MDDataBlock<MDShortArray>>()
                        {
                            long[] index = new long[rank];

                            long[] offset = new long[rank];

                            int[] blockSize = naturalBlockSize.clone();

                            boolean indexCalculated = true;

                            public boolean hasNext()
                            {
                                if (indexCalculated)
                                {
                                    return true;
                                }
                                for (int i = index.length - 1; i >= 0; --i)
                                {
                                    ++index[i];
                                    if (index[i] < numberOfBlocks[i])
                                    {
                                        offset[i] += naturalBlockSize[i];
                                        if (index[i] == numberOfBlocks[i] - 1)
                                        {
                                            blockSize[i] = lastBlockSize[i];
                                        }
                                        indexCalculated = true;
                                        break;
                                    } else
                                    {
                                        index[i] = 0;
                                        offset[i] = 0;
                                        blockSize[i] = naturalBlockSize[i];
                                    }
                                }
                                return indexCalculated;
                            }

                            public HDF5MDDataBlock<MDShortArray> next()
                            {
                                if (hasNext() == false)
                                {
                                    throw new NoSuchElementException();
                                }
                                final MDShortArray data =
                                        readShortMDArrayBlockWithOffset(dataSetPath, blockSize,
                                                offset);
                                prepareNext();
                                return new HDF5MDDataBlock<MDShortArray>(data, index.clone(),
                                        offset.clone());
                            }

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }

                            private void prepareNext()
                            {
                                indexCalculated = false;
                            }
                        };
                }
            };
    }
}
