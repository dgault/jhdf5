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

import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5T_COMPOUND;

import java.util.Iterator;

import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;

import ch.systemsx.cisd.base.mdarray.MDArray;
import ch.systemsx.cisd.hdf5.HDF5BaseReader.DataSpaceParameters;
import ch.systemsx.cisd.hdf5.cleanup.ICallableWithCleanUp;
import ch.systemsx.cisd.hdf5.cleanup.ICleanUpRegistry;

/**
 * The implementation of {@link IHDF5CompoundReader}.
 * 
 * @author Bernd Rinn
 */
class HDF5CompoundReader extends HDF5CompoundInformationRetriever implements IHDF5CompoundReader
{

    HDF5CompoundReader(HDF5BaseReader baseReader)
    {
        super(baseReader);
    }

    public <T> T getCompoundAttribute(final String objectPath, final String attributeName,
            final HDF5CompoundType<T> type) throws HDF5JavaException
    {
        return primGetCompoundAttribute(objectPath, attributeName, type, null);
    }

    public <T> T getCompoundAttribute(final String objectPath, final String attributeName,
            final Class<T> pojoClass) throws HDF5JavaException
    {
        baseReader.checkOpen();
        final HDF5CompoundType<T> attributeCompoundType =
                getAttributeCompoundType(objectPath, attributeName, pojoClass);
        attributeCompoundType.checkMappingComplete();
        return primGetCompoundAttribute(objectPath, attributeName, attributeCompoundType, null);
    }

    private <T> T primGetCompoundAttribute(final String objectPath, final String attributeName,
            final HDF5CompoundType<T> type, final IByteArrayInspector inspectorOrNull)
            throws HDF5JavaException
    {
        final ICallableWithCleanUp<T> writeRunnable = new ICallableWithCleanUp<T>()
            {
                public T call(final ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openObject(baseReader.fileId, objectPath, registry);
                    final int attributeId =
                            baseReader.h5.openAttribute(dataSetId, attributeName, registry);
                    final int storageDataTypeId =
                            baseReader.h5.getDataTypeForAttribute(attributeId, registry);
                    checkCompoundType(storageDataTypeId, objectPath, type);
                    final int nativeDataTypeId = type.getNativeTypeId();
                    final byte[] byteArr =
                            baseReader.h5.readAttributeAsByteArray(attributeId, nativeDataTypeId,
                                    type.getObjectByteifyer().getRecordSize());
                    if (inspectorOrNull != null)
                    {
                        inspectorOrNull.inspect(byteArr);
                    }
                    return type.getObjectByteifyer().arrayifyScalar(storageDataTypeId, byteArr,
                            type.getCompoundType());
                }
            };
        return baseReader.runner.call(writeRunnable);
    }

    public <T> T readCompound(final String objectPath, final HDF5CompoundType<T> type)
            throws HDF5JavaException
    {
        return readCompound(objectPath, type, null);
    }

    public <T> T readCompound(final String objectPath, final Class<T> pojoClass)
            throws HDF5JavaException
    {
        baseReader.checkOpen();
        final HDF5CompoundType<T> dataSetCompoundType =
                getDataSetCompoundType(objectPath, pojoClass);
        dataSetCompoundType.checkMappingComplete();
        return readCompound(objectPath, dataSetCompoundType, null);
    }

    public <T> T readCompound(final String objectPath, final HDF5CompoundType<T> type,
            final IByteArrayInspector inspectorOrNull) throws HDF5JavaException
    {
        baseReader.checkOpen();
        type.check(baseReader.fileId);
        return primReadCompound(objectPath, -1, -1, type, inspectorOrNull);
    }

    public <T> T[] readCompoundArray(final String objectPath, final HDF5CompoundType<T> type)
            throws HDF5JavaException
    {
        return readCompoundArray(objectPath, type, null);
    }

    public <T> T[] readCompoundArray(final String objectPath, final HDF5CompoundType<T> type,
            final IByteArrayInspector inspectorOrNull) throws HDF5JavaException
    {
        baseReader.checkOpen();
        type.check(baseReader.fileId);
        return primReadCompoundArray(objectPath, -1, -1, type, inspectorOrNull);
    }

    public <T> T[] readCompoundArray(final String objectPath, final Class<T> pojoClass)
            throws HDF5JavaException
    {
        baseReader.checkOpen();
        final HDF5CompoundType<T> dataSetCompoundType =
                getDataSetCompoundType(objectPath, pojoClass);
        dataSetCompoundType.checkMappingComplete();
        return readCompoundArray(objectPath, dataSetCompoundType, null);
    }

    public <T> T[] readCompoundArrayBlock(final String objectPath, final HDF5CompoundType<T> type,
            final int blockSize, final long blockNumber) throws HDF5JavaException
    {
        return readCompoundArrayBlock(objectPath, type, blockSize, blockNumber, null);
    }

    public <T> T[] readCompoundArrayBlock(final String objectPath, final HDF5CompoundType<T> type,
            final int blockSize, final long blockNumber, final IByteArrayInspector inspectorOrNull)
            throws HDF5JavaException
    {
        baseReader.checkOpen();
        type.check(baseReader.fileId);
        return primReadCompoundArray(objectPath, blockSize, blockSize * blockNumber, type,
                inspectorOrNull);
    }

    public <T> T[] readCompoundArrayBlockWithOffset(final String objectPath,
            final HDF5CompoundType<T> type, final int blockSize, final long offset)
            throws HDF5JavaException
    {
        return readCompoundArrayBlockWithOffset(objectPath, type, blockSize, offset, null);
    }

    public <T> T[] readCompoundArrayBlockWithOffset(final String objectPath,
            final HDF5CompoundType<T> type, final int blockSize, final long offset,
            final IByteArrayInspector inspectorOrNull) throws HDF5JavaException
    {
        baseReader.checkOpen();
        type.check(baseReader.fileId);
        return primReadCompoundArray(objectPath, blockSize, offset, type, inspectorOrNull);
    }

    public <T> Iterable<HDF5DataBlock<T[]>> getCompoundArrayNaturalBlocks(final String objectPath,
            final HDF5CompoundType<T> type) throws HDF5JavaException
    {
        return getCompoundArrayNaturalBlocks(objectPath, type, null);
    }

    public <T> Iterable<HDF5DataBlock<T[]>> getCompoundArrayNaturalBlocks(final String objectPath,
            final HDF5CompoundType<T> type, final IByteArrayInspector inspectorOrNull)
            throws HDF5JavaException
    {
        baseReader.checkOpen();
        type.check(baseReader.fileId);
        final HDF5NaturalBlock1DParameters params =
                new HDF5NaturalBlock1DParameters(baseReader.getDataSetInformation(objectPath));

        return primGetCompoundArrayNaturalBlocks(objectPath, type, params, inspectorOrNull);
    }

    public <T> Iterable<HDF5DataBlock<T[]>> primGetCompoundArrayNaturalBlocks(
            final String objectPath, final HDF5CompoundType<T> type,
            final HDF5NaturalBlock1DParameters params, final IByteArrayInspector inspectorOrNull)
    {
        return new Iterable<HDF5DataBlock<T[]>>()
            {
                public Iterator<HDF5DataBlock<T[]>> iterator()
                {
                    return new Iterator<HDF5DataBlock<T[]>>()
                        {
                            final HDF5NaturalBlock1DParameters.HDF5NaturalBlock1DIndex index =
                                    params.getNaturalBlockIndex();

                            public boolean hasNext()
                            {
                                return index.hasNext();
                            }

                            public HDF5DataBlock<T[]> next()
                            {
                                final long offset = index.computeOffsetAndSizeGetOffset();
                                final T[] block =
                                        readCompoundArrayBlockWithOffset(objectPath, type,
                                                index.getBlockSize(), offset, inspectorOrNull);
                                return new HDF5DataBlock<T[]>(block, index.getAndIncIndex(), offset);
                            }

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    public <T> Iterable<HDF5DataBlock<T[]>> getCompoundArrayNaturalBlocks(String objectPath,
            Class<T> pojoClass) throws HDF5JavaException
    {
        baseReader.checkOpen();
        final HDF5NaturalBlock1DParameters params =
                new HDF5NaturalBlock1DParameters(baseReader.getDataSetInformation(objectPath));

        final HDF5CompoundType<T> dataSetCompoundType =
                getDataSetCompoundType(objectPath, pojoClass);
        dataSetCompoundType.checkMappingComplete();
        return primGetCompoundArrayNaturalBlocks(objectPath, dataSetCompoundType, params, null);
    }

    private <T> T primReadCompound(final String objectPath, final int blockSize, final long offset,
            final HDF5CompoundType<T> type, final IByteArrayInspector inspectorOrNull)
            throws HDF5JavaException
    {
        final ICallableWithCleanUp<T> writeRunnable = new ICallableWithCleanUp<T>()
            {
                public T call(final ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final int storageDataTypeId =
                            baseReader.h5.getDataTypeForDataSet(dataSetId, registry);
                    checkCompoundType(storageDataTypeId, objectPath, type);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, offset, blockSize, registry);
                    final int nativeDataTypeId = type.getNativeTypeId();
                    final byte[] byteArr =
                            new byte[spaceParams.blockSize
                                    * type.getObjectByteifyer().getRecordSize()];
                    baseReader.h5.readDataSet(dataSetId, nativeDataTypeId,
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId, byteArr);
                    if (inspectorOrNull != null)
                    {
                        inspectorOrNull.inspect(byteArr);
                    }
                    return type.getObjectByteifyer().arrayifyScalar(storageDataTypeId, byteArr,
                            type.getCompoundType());
                }
            };
        return baseReader.runner.call(writeRunnable);
    }

    private <T> T[] primReadCompoundArray(final String objectPath, final int blockSize,
            final long offset, final HDF5CompoundType<T> type,
            final IByteArrayInspector inspectorOrNull) throws HDF5JavaException
    {
        final ICallableWithCleanUp<T[]> writeRunnable = new ICallableWithCleanUp<T[]>()
            {
                public T[] call(final ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final int storageDataTypeId =
                            baseReader.h5.getDataTypeForDataSet(dataSetId, registry);
                    checkCompoundType(storageDataTypeId, objectPath, type);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, offset, blockSize, registry);
                    final int nativeDataTypeId = type.getNativeTypeId();
                    final byte[] byteArr =
                            new byte[spaceParams.blockSize
                                    * type.getObjectByteifyer().getRecordSize()];
                    baseReader.h5.readDataSet(dataSetId, nativeDataTypeId,
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId, byteArr);
                    if (inspectorOrNull != null)
                    {
                        inspectorOrNull.inspect(byteArr);
                    }
                    return type.getObjectByteifyer().arrayify(storageDataTypeId, byteArr,
                            type.getCompoundType());
                }
            };
        return baseReader.runner.call(writeRunnable);
    }

    private void checkCompoundType(final int dataTypeId, final String path,
            final HDF5CompoundType<?> type) throws HDF5JavaException
    {
        final boolean isCompound = (baseReader.h5.getClassType(dataTypeId) == H5T_COMPOUND);
        if (isCompound == false)
        {
            throw new HDF5JavaException("Data set '" + path + "' is no compound.");
        }
        final boolean isEqual =
                (baseReader.h5.dataTypesAreEqual(dataTypeId, type.getStorageTypeId()));
        if (isEqual == false)
        {
            throw new HDF5JavaException("The compound type '" + type.getName()
                    + "' is not suitable for data set '" + path + "'.");
        }
    }

    public <T> MDArray<T> readCompoundMDArray(final String objectPath,
            final HDF5CompoundType<T> type) throws HDF5JavaException
    {
        baseReader.checkOpen();
        type.check(baseReader.fileId);
        return primReadCompoundArrayRankN(objectPath, type, null, null, null);
    }

    public <T> MDArray<T> readCompoundMDArray(final String objectPath,
            final HDF5CompoundType<T> type, final IByteArrayInspector inspectorOrNull)
            throws HDF5JavaException
    {
        baseReader.checkOpen();
        type.check(baseReader.fileId);
        return primReadCompoundArrayRankN(objectPath, type, null, null, inspectorOrNull);
    }

    public <T> MDArray<T> readCompoundMDArray(String objectPath, Class<T> pojoClass)
            throws HDF5JavaException
    {
        baseReader.checkOpen();
        final HDF5CompoundType<T> dataSetCompoundType =
                getDataSetCompoundType(objectPath, pojoClass);
        dataSetCompoundType.checkMappingComplete();
        return primReadCompoundArrayRankN(objectPath, dataSetCompoundType, null, null, null);
    }

    public <T> MDArray<T> readCompoundMDArrayBlock(final String objectPath,
            final HDF5CompoundType<T> type, final int[] blockDimensions, final long[] blockNumber)
            throws HDF5JavaException
    {
        return readCompoundMDArrayBlock(objectPath, type, blockDimensions, blockNumber, null);
    }

    public <T> MDArray<T> readCompoundMDArrayBlock(final String objectPath,
            final HDF5CompoundType<T> type, final int[] blockDimensions, final long[] blockNumber,
            final IByteArrayInspector inspectorOrNull) throws HDF5JavaException
    {
        baseReader.checkOpen();
        type.check(baseReader.fileId);
        final long[] offset = new long[blockDimensions.length];
        for (int i = 0; i < offset.length; ++i)
        {
            offset[i] = blockDimensions[i] * blockNumber[i];
        }
        return primReadCompoundArrayRankN(objectPath, type, blockDimensions, offset,
                inspectorOrNull);
    }

    public <T> MDArray<T> readCompoundMDArrayBlockWithOffset(final String objectPath,
            final HDF5CompoundType<T> type, final int[] blockDimensions, final long[] offset)
            throws HDF5JavaException
    {
        return readCompoundMDArrayBlockWithOffset(objectPath, type, blockDimensions, offset, null);
    }

    public <T> MDArray<T> readCompoundMDArrayBlockWithOffset(final String objectPath,
            final HDF5CompoundType<T> type, final int[] blockDimensions, final long[] offset,
            final IByteArrayInspector inspectorOrNull) throws HDF5JavaException
    {
        baseReader.checkOpen();
        type.check(baseReader.fileId);
        return primReadCompoundArrayRankN(objectPath, type, blockDimensions, offset,
                inspectorOrNull);
    }

    public <T> Iterable<HDF5MDDataBlock<MDArray<T>>> getCompoundMDArrayNaturalBlocks(
            final String objectPath, final HDF5CompoundType<T> type) throws HDF5JavaException
    {
        return getCompoundMDArrayNaturalBlocks(objectPath, type, null);
    }

    public <T> Iterable<HDF5MDDataBlock<MDArray<T>>> getCompoundMDArrayNaturalBlocks(
            final String objectPath, final HDF5CompoundType<T> type,
            final IByteArrayInspector inspectorOrNull) throws HDF5JavaException
    {
        baseReader.checkOpen();
        type.check(baseReader.fileId);
        final HDF5NaturalBlockMDParameters params =
                new HDF5NaturalBlockMDParameters(baseReader.getDataSetInformation(objectPath));

        return primGetCompoundMDArrayNaturalBlocks(objectPath, type, params, inspectorOrNull);
    }

    public <T> Iterable<HDF5MDDataBlock<MDArray<T>>> primGetCompoundMDArrayNaturalBlocks(
            final String objectPath, final HDF5CompoundType<T> type,
            final HDF5NaturalBlockMDParameters params, final IByteArrayInspector inspectorOrNull)
    {
        return new Iterable<HDF5MDDataBlock<MDArray<T>>>()
            {
                public Iterator<HDF5MDDataBlock<MDArray<T>>> iterator()
                {
                    return new Iterator<HDF5MDDataBlock<MDArray<T>>>()
                        {
                            final HDF5NaturalBlockMDParameters.HDF5NaturalBlockMDIndex index =
                                    params.getNaturalBlockIndex();

                            public boolean hasNext()
                            {
                                return index.hasNext();
                            }

                            public HDF5MDDataBlock<MDArray<T>> next()
                            {
                                final long[] offset = index.computeOffsetAndSizeGetOffsetClone();
                                final MDArray<T> block =
                                        readCompoundMDArrayBlockWithOffset(objectPath, type,
                                                index.getBlockSize(), offset, inspectorOrNull);
                                return new HDF5MDDataBlock<MDArray<T>>(block,
                                        index.getIndexClone(), offset);
                            }

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    private <T> MDArray<T> primReadCompoundArrayRankN(final String objectPath,
            final HDF5CompoundType<T> type, final int[] dimensionsOrNull,
            final long[] offsetOrNull, final IByteArrayInspector inspectorOrNull)
            throws HDF5JavaException
    {
        final ICallableWithCleanUp<MDArray<T>> writeRunnable =
                new ICallableWithCleanUp<MDArray<T>>()
                    {
                        public MDArray<T> call(final ICleanUpRegistry registry)
                        {
                            final int dataSetId =
                                    baseReader.h5.openDataSet(baseReader.fileId, objectPath,
                                            registry);
                            final int storageDataTypeId =
                                    baseReader.h5.getDataTypeForDataSet(dataSetId, registry);
                            checkCompoundType(storageDataTypeId, objectPath, type);
                            final DataSpaceParameters spaceParams =
                                    baseReader.getSpaceParameters(dataSetId, offsetOrNull,
                                            dimensionsOrNull, registry);
                            final int nativeDataTypeId = type.getNativeTypeId();
                            final byte[] byteArr =
                                    new byte[spaceParams.blockSize
                                            * type.getObjectByteifyer().getRecordSize()];
                            baseReader.h5.readDataSet(dataSetId, nativeDataTypeId,
                                    spaceParams.memorySpaceId, spaceParams.dataSpaceId, byteArr);
                            if (inspectorOrNull != null)
                            {
                                inspectorOrNull.inspect(byteArr);
                            }
                            return new MDArray<T>(type.getObjectByteifyer().arrayify(
                                    storageDataTypeId, byteArr, type.getCompoundType()),
                                    spaceParams.dimensions);
                        }
                    };
        return baseReader.runner.call(writeRunnable);
    }

    public <T> Iterable<HDF5MDDataBlock<MDArray<T>>> getCompoundMDArrayNaturalBlocks(
            String objectPath, Class<T> pojoClass) throws HDF5JavaException
    {
        baseReader.checkOpen();
        final HDF5NaturalBlockMDParameters params =
                new HDF5NaturalBlockMDParameters(baseReader.getDataSetInformation(objectPath));

        final HDF5CompoundType<T> dataSetCompoundType =
                getDataSetCompoundType(objectPath, pojoClass);
        dataSetCompoundType.checkMappingComplete();
        return primGetCompoundMDArrayNaturalBlocks(objectPath, dataSetCompoundType, params, null);
    }

}
