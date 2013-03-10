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

import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5T_NATIVE_B64;

import java.util.BitSet;

import ncsa.hdf.hdf5lib.exceptions.HDF5DatatypeInterfaceException;
import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;

import ch.systemsx.cisd.base.mdarray.MDLongArray;
import ch.systemsx.cisd.hdf5.HDF5BaseReader.DataSpaceParameters;
import ch.systemsx.cisd.hdf5.cleanup.ICallableWithCleanUp;
import ch.systemsx.cisd.hdf5.cleanup.ICleanUpRegistry;

/**
 * Implementation of {@link IHDF5BooleanReader}.
 * 
 * @author Bernd Rinn
 */
public class HDF5BooleanReader implements IHDF5BooleanReader
{

    private final HDF5BaseReader baseReader;

    HDF5BooleanReader(HDF5BaseReader baseReader)
    {
        assert baseReader != null;

        this.baseReader = baseReader;
    }

    // /////////////////////
    // Attributes
    // /////////////////////

    @Override
    public boolean getAttr(final String objectPath, final String attributeName)
            throws HDF5JavaException
    {
        assert objectPath != null;
        assert attributeName != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<Boolean> writeRunnable = new ICallableWithCleanUp<Boolean>()
            {
                @Override
                public Boolean call(ICleanUpRegistry registry)
                {
                    final int objectId =
                            baseReader.h5.openObject(baseReader.fileId, objectPath, registry);
                    final int attributeId =
                            baseReader.h5.openAttribute(objectId, attributeName, registry);
                    final int nativeDataTypeId =
                            baseReader.h5.getNativeDataTypeForAttribute(attributeId, registry);
                    byte[] data =
                            baseReader.h5
                                    .readAttributeAsByteArray(attributeId, nativeDataTypeId, 1);
                    final Boolean value =
                            baseReader.h5.tryGetBooleanValue(nativeDataTypeId, data[0]);
                    if (value == null)
                    {
                        throw new HDF5JavaException("Attribute " + attributeName + " of path "
                                + objectPath + " needs to be a Boolean.");
                    }
                    return value;
                }
            };
        return baseReader.runner.call(writeRunnable);
    }

    // /////////////////////
    // Data Sets
    // /////////////////////

    @Override
    public boolean read(final String objectPath) throws HDF5JavaException
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<Boolean> writeRunnable = new ICallableWithCleanUp<Boolean>()
            {
                @Override
                public Boolean call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final int nativeDataTypeId =
                            baseReader.h5.getNativeDataTypeForDataSet(dataSetId, registry);
                    final byte[] data = new byte[1];
                    baseReader.h5.readDataSet(dataSetId, nativeDataTypeId, data);
                    final Boolean value =
                            baseReader.h5.tryGetBooleanValue(nativeDataTypeId, data[0]);
                    if (value == null)
                    {
                        throw new HDF5JavaException(objectPath + " needs to be a Boolean.");
                    }
                    return value;
                }
            };
        return baseReader.runner.call(writeRunnable);
    }

    @Override
    public BitSet readBitField(final String objectPath) throws HDF5DatatypeInterfaceException
    {
        baseReader.checkOpen();
        return BitSetConversionUtils.fromStorageForm(readBitFieldStorageForm(objectPath));
    }

    private long[] readBitFieldStorageForm(final String objectPath)
    {
        assert objectPath != null;

        final ICallableWithCleanUp<long[]> readCallable = new ICallableWithCleanUp<long[]>()
            {
                @Override
                public long[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, registry);
                    final long[] data = new long[spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_B64, spaceParams.memorySpaceId,
                            spaceParams.dataSpaceId, data);
                    return data;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    @Override
    public BitSet readBitFieldBlock(String objectPath, int blockSize, long blockNumber)
    {
        return readBitFieldBlockWithOffset(objectPath, blockSize, blockSize * blockNumber);
    }

    private long[] readBitFieldStorageForm(final String objectPath, final int blockSize,
            final long offset, final boolean nullWhenOutside)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<long[]> readCallable = new ICallableWithCleanUp<long[]>()
            {
                @Override
                public long[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParamsOrNull =
                            baseReader.tryGetSpaceParameters(dataSetId, offset, blockSize,
                                    nullWhenOutside, registry);
                    if (spaceParamsOrNull == null)
                    {
                        return null;
                    }
                    final long[] data = new long[spaceParamsOrNull.blockSize];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_B64,
                            spaceParamsOrNull.memorySpaceId, spaceParamsOrNull.dataSpaceId, data);
                    return data;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    @Override
    public BitSet readBitFieldBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        baseReader.checkOpen();
        return BitSetConversionUtils.fromStorageForm(readBitFieldStorageForm(objectPath, blockSize,
                offset, false));
    }

    @Override
    public boolean isBitSet(String objectPath, int bitIndex)
    {
        final int wordIndex = BitSetConversionUtils.getWordIndex(bitIndex);
        final long[] storageFormOrNull = readBitFieldStorageForm(objectPath, 1, wordIndex, true);
        // If the bitIndex is outside of the bit field, we return false
        if (storageFormOrNull == null)
        {
            return false;
        }
        final long word = storageFormOrNull[0];
        return (word & BitSetConversionUtils.getBitMaskInWord(bitIndex)) != 0;
    }

    @Override
    public BitSet[] readBitFieldArray(String objectPath)
    {
        baseReader.checkOpen();
        return BitSetConversionUtils.fromStorageForm2D(readBitFieldArrayStorageForm(objectPath));
    }

    private MDLongArray readBitFieldArrayStorageForm(final String objectPath)
    {
        assert objectPath != null;

        final ICallableWithCleanUp<MDLongArray> readCallable = new ICallableWithCleanUp<MDLongArray>()
            {
                @Override
                public MDLongArray call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, registry);
                    final long[] data = new long[spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_B64, spaceParams.memorySpaceId,
                            spaceParams.dataSpaceId, data);
                    return new MDLongArray(data, spaceParams.dimensions);
                }
            };
        return baseReader.runner.call(readCallable);
    }

}
