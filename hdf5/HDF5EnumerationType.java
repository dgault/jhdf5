/*
 * Copyright 2008 ETH Zuerich, CISD
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
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5T_NATIVE_INT32;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5T_NATIVE_INT8;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5T_STD_U16LE;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5T_STD_U32LE;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5T_STD_U8LE;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;

import ch.systemsx.cisd.base.convert.NativeData;
import ch.systemsx.cisd.base.convert.NativeData.ByteOrder;
import ch.systemsx.cisd.base.mdarray.MDAbstractArray;
import ch.systemsx.cisd.base.mdarray.MDByteArray;
import ch.systemsx.cisd.base.mdarray.MDIntArray;
import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.hdf5lib.HDFNativeData;

/**
 * A class that represents an enumeration for a given HDF5 file and <var>values</var> array.
 * 
 * @author Bernd Rinn
 */
public final class HDF5EnumerationType extends HDF5DataType implements Iterable<String>
{
    enum StorageFormEnum
    {
        BYTE(1, H5T_NATIVE_INT8, H5T_STD_U8LE), SHORT(2, H5T_NATIVE_INT16, H5T_STD_U16LE), INT(4,
                H5T_NATIVE_INT32, H5T_STD_U32LE);

        private final byte storageSize;

        private final int intNativeType;

        private final int intStorageType;

        StorageFormEnum(int storageSize, int intNativeType, int intStorageType)
        {
            this.storageSize = (byte) storageSize;
            this.intNativeType = intNativeType;
            this.intStorageType = intStorageType;
        }

        byte getStorageSize()
        {
            return storageSize;
        }

        int getIntNativeTypeId()
        {
            return intNativeType;
        }

        int getIntStorageTypeId()
        {
            return intStorageType;
        }
    }

    private final String nameOrNull;

    private final String[] values;

    private final List<String> unmodifiableValues;

    private Map<String, Integer> nameToIndexMap;

    /**
     * Returns the storage data type id of the corresponding integer type of this type.
     */
    int getIntStorageTypeId()
    {
        return getStorageForm().getIntStorageTypeId();
    }

    /**
     * Returns the native data type id of the corresponding integer type of this type.
     */
    int getIntNativeTypeId()
    {
        return getStorageForm().getIntNativeTypeId();
    }

    HDF5EnumerationType(int fileId, int storageTypeId, int nativeTypeId, String nameOrNull,
            String[] values)
    {
        super(fileId, storageTypeId, nativeTypeId);

        assert values != null;

        this.nameOrNull = nameOrNull;
        this.values = values;
        this.unmodifiableValues = Collections.unmodifiableList(Arrays.asList(values));
    }

    private Map<String, Integer> getMap()
    {
        if (nameToIndexMap == null)
        {
            nameToIndexMap = new HashMap<String, Integer>(values.length);
            for (int i = 0; i < values.length; ++i)
            {
                nameToIndexMap.put(values[i], i);
            }
        }
        return nameToIndexMap;
    }

    String[] getValueArray()
    {
        return values;
    }

    Object createArray(int length)
    {
        if (values.length < Byte.MAX_VALUE)
        {
            return new byte[length];
        } else if (values.length < Short.MAX_VALUE)
        {
            return new short[length];
        } else
        {
            return new int[length];
        }
    }

    /**
     * Returns the ordinal value for the given string <var>value</var>, if <var>value</var> is a
     * member of the enumeration, and <code>null</code> otherwise.
     */
    public Integer tryGetIndexForValue(String value)
    {
        return getMap().get(value);
    }

    /**
     * Returns the name of this type, if it exists and <code>null</code> otherwise.
     */
    @Override
    public String tryGetName()
    {
        return nameOrNull;
    }

    /**
     * Returns the allowed values of this enumeration type.
     */
    public List<String> getValues()
    {
        return unmodifiableValues;
    }

    StorageFormEnum getStorageForm()
    {
        final int len = values.length;
        if (len < Byte.MAX_VALUE)
        {
            return StorageFormEnum.BYTE;
        } else if (len < Short.MAX_VALUE)
        {
            return StorageFormEnum.SHORT;
        } else
        {
            return StorageFormEnum.INT;
        }
    }

    byte getNumberOfBits()
    {
        final int n = (values.length > 0) ? values.length - 1 : 0;
        // Binary search - decision tree (5 tests, rarely 6)
        return (byte) (n < 1 << 15 ? (n < 1 << 7 ? (n < 1 << 3 ? (n < 1 << 1 ? (n < 1 << 0 ? (n < 0 ? 32
                : 0)
                : 1)
                : (n < 1 << 2 ? 2 : 3))
                : (n < 1 << 5 ? (n < 1 << 4 ? 4 : 5) : (n < 1 << 6 ? 6 : 7)))
                : (n < 1 << 11 ? (n < 1 << 9 ? (n < 1 << 8 ? 8 : 9) : (n < 1 << 10 ? 10 : 11))
                        : (n < 1 << 13 ? (n < 1 << 12 ? 12 : 13) : (n < 1 << 14 ? 14 : 15))))
                : (n < 1 << 23 ? (n < 1 << 19 ? (n < 1 << 17 ? (n < 1 << 16 ? 16 : 17)
                        : (n < 1 << 18 ? 18 : 19)) : (n < 1 << 21 ? (n < 1 << 20 ? 20 : 21)
                        : (n < 1 << 22 ? 22 : 23)))
                        : (n < 1 << 27 ? (n < 1 << 25 ? (n < 1 << 24 ? 24 : 25) : (n < 1 << 26 ? 26
                                : 27)) : (n < 1 << 29 ? (n < 1 << 28 ? 28 : 29) : (n < 1 << 30 ? 30
                                : 31)))));
    }

    byte[] toStorageForm(int ordinal)
    {
        switch (getStorageForm())
        {
            case BYTE:
                return HDFNativeData.byteToByte((byte) ordinal);
            case SHORT:
                return HDFNativeData.shortToByte((short) ordinal);
            case INT:
                return HDFNativeData.intToByte(ordinal);
        }
        throw new Error("Illegal storage size.");
    }

    static int fromStorageForm(byte[] data)
    {
        if (data.length == 1)
        {
            return data[0];
        } else if (data.length == 2)
        {
            return NativeData.byteToShort(data, ByteOrder.NATIVE)[0];
        } else if (data.length == 4)
        {
            return NativeData.byteToInt(data, ByteOrder.NATIVE)[0];
        }
        throw new HDF5JavaException("Unexpected size for Enum data type (" + data.length + ")");
    }

    static int fromStorageForm(byte[] data, int index, int size)
    {
        if (size == 1)
        {
            return data[index];
        } else if (size == 2)
        {
            return NativeData.byteToShort(data, ByteOrder.NATIVE, size * index, 1)[0];
        } else if (size == 4)
        {
            return NativeData.byteToInt(data, ByteOrder.NATIVE, index, 1)[0];
        }
        throw new HDF5JavaException("Unexpected size for Enum data type (" + size + ")");
    }

    static Object fromStorageForm(byte[] data, StorageFormEnum storageForm)
    {
        switch (storageForm)
        {
            case BYTE:
                return data;
            case SHORT:
                return NativeData.byteToShort(data, ByteOrder.NATIVE);
            case INT:
                return NativeData.byteToInt(data, ByteOrder.NATIVE);
        }
        throw new Error("Illegal storage size.");
    }

    static MDAbstractArray<?> fromStorageForm(byte[] data, long[] dimensions,
            StorageFormEnum storageForm)
    {
        switch (storageForm)
        {
            case BYTE:
                return new MDByteArray(data, dimensions);
            case SHORT:
                return new MDShortArray(NativeData.byteToShort(data, ByteOrder.NATIVE), dimensions);
            case INT:
                return new MDIntArray(NativeData.byteToInt(data, ByteOrder.NATIVE), dimensions);
        }
        throw new Error("Illegal storage size.");
    }

    static MDAbstractArray<?> fromStorageForm(byte[] data, int[] dimensions,
            StorageFormEnum storageForm)
    {
        switch (storageForm)
        {
            case BYTE:
                return new MDByteArray(data, dimensions);
            case SHORT:
                return new MDShortArray(NativeData.byteToShort(data, ByteOrder.NATIVE), dimensions);
            case INT:
                return new MDIntArray(NativeData.byteToInt(data, ByteOrder.NATIVE), dimensions);
        }
        throw new Error("Illegal storage size.");
    }

    HDF5EnumerationValue createFromStorageForm(byte[] data, int offset)
    {
        return new HDF5EnumerationValue(this, getOrdinalFromStorageForm(data, offset));
    }

    String createStringFromStorageForm(byte[] data, int offset)
    {
        return values[getOrdinalFromStorageForm(data, offset)];
    }

    int getOrdinalFromStorageForm(byte[] data, int offset)
    {
        switch (getStorageForm())
        {
            case BYTE:
                return data[offset];
            case SHORT:
                return HDFNativeData.byteToShort(data, offset);
            case INT:
                return HDFNativeData.byteToInt(data, offset);
        }
        throw new Error("Illegal storage form (" + getStorageForm() + ".)");
    }

    //
    // Iterable
    //

    /**
     * Returns an {@link Iterator} over all values of this enumeration type.
     * {@link Iterator#remove()} is not allowed and will throw an
     * {@link UnsupportedOperationException}.
     */
    public Iterator<String> iterator()
    {
        return new Iterator<String>()
            {
                private int index = 0;

                public boolean hasNext()
                {
                    return index < values.length;
                }

                public String next()
                {
                    return values[index++];
                }

                /**
                 * @throws UnsupportedOperationException As this iterator doesn't support removal.
                 */
                public void remove() throws UnsupportedOperationException
                {
                    throw new UnsupportedOperationException();
                }

            };
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(values);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final HDF5EnumerationType other = (HDF5EnumerationType) obj;
        return Arrays.equals(values, other.values);
    }

}
