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

import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;

import ch.systemsx.cisd.base.mdarray.MDAbstractArray;
import ch.systemsx.cisd.hdf5.cleanup.ICleanUpRegistry;

/**
 * A class that byteifies Java value objects. The fields have to be specified by name. This class
 * can handle all primitive types and Strings.
 * 
 * @author Bernd Rinn
 */
class HDF5ValueObjectByteifyer<T>
{

    private final HDF5MemberByteifyer[] byteifyers;

    private final int recordSize;

    /** A role that provides some information about the HDF5 file to this byteifyer. */
    interface FileInfoProvider
    {
        public int getBooleanDataTypeId();

        public int getStringDataTypeId(int maxLength);

        public int getArrayTypeId(int baseTypeId, int length);

        public int getArrayTypeId(int baseTypeId, int[] dimensions);
    }

    public HDF5ValueObjectByteifyer(Class<T> clazz, FileInfoProvider fileInfoProvider,
            HDF5CompoundMemberMapping... members)
    {
        byteifyers = createMemberByteifyers(clazz, fileInfoProvider, members);
        if (byteifyers.length > 0)
        {
            recordSize = byteifyers[byteifyers.length - 1].getTotalSize();
        } else
        {
            recordSize = 0;
        }

    }

    private static HDF5MemberByteifyer[] createMemberByteifyers(Class<?> clazz,
            FileInfoProvider fileInfoProvider, HDF5CompoundMemberMapping... members)
    {
        final HDF5MemberByteifyer[] result = new HDF5MemberByteifyer[members.length];
        int offset = 0;
        for (int i = 0; i < result.length; ++i)
        {
            final Class<?> memberClazz = members[i].getField(clazz).getType();
            // May be -1 if not known
            final int typeId = members[i].getStorageDataTypeId();
            if (memberClazz == boolean.class)
            {
                result[i] =
                        HDF5MemberByteifyer.createBooleanMemberByteifyer(
                                members[i].getField(clazz), members[i].getMemberName(),
                                (typeId < 0) ? fileInfoProvider.getBooleanDataTypeId() : typeId,
                                offset);
            } else if (memberClazz == String.class)
            {
                final int stringDataTypeId =
                        (typeId < 0) ? fileInfoProvider.getStringDataTypeId(members[i]
                                .getMemberTypeLength()) : typeId;
                result[i] =
                        HDF5MemberByteifyer.createStringMemberByteifyer(members[i].getField(clazz),
                                members[i].getMemberName(), offset, stringDataTypeId, members[i]
                                        .getMemberTypeLength());
            } else if (memberClazz == char[].class)
            {
                final int stringDataTypeId =
                        (typeId < 0) ? fileInfoProvider.getStringDataTypeId(members[i]
                                .getMemberTypeLength()) : typeId;
                result[i] =
                        HDF5MemberByteifyer.createCharArrayMemberByteifyer(members[i]
                                .getField(clazz), members[i].getMemberName(), offset,
                                stringDataTypeId, members[i].getMemberTypeLength());
            } else if (memberClazz == HDF5EnumerationValue.class)
            {
                final HDF5EnumerationType enumType = members[i].tryGetEnumerationType();
                if (enumType == null)
                {
                    throw new NullPointerException("Enumeration type not set for member byteifyer.");
                }
                result[i] =
                        HDF5MemberByteifyer.createEnumMemberByteifyer(members[i].getField(clazz),
                                members[i].getMemberName(), members[i].tryGetEnumerationType(),
                                offset);
            } else if (memberClazz == HDF5EnumerationValueArray.class)
            {
                final HDF5EnumerationType enumType = members[i].tryGetEnumerationType();
                if (enumType == null)
                {
                    throw new NullPointerException("Enumeration type not set for member byteifyer.");
                }
                result[i] =
                        HDF5MemberByteifyer.createEnumArrayMemberByteifyer(members[i]
                                .getField(clazz), members[i].getMemberName(), members[i]
                                .tryGetEnumerationType(), offset, fileInfoProvider, members[i]
                                .getMemberTypeLength(), members[i].getStorageDataTypeId());
            } else if (isOneDimensionalArray(memberClazz) || memberClazz == java.util.BitSet.class)
            {
                result[i] =
                        HDF5MemberByteifyer.createArrayMemberByteifyer(members[i].getField(clazz),
                                members[i].getMemberName(), offset, fileInfoProvider, members[i]
                                        .getMemberTypeLength(), members[i].getStorageDataTypeId());
            } else if (MDAbstractArray.class.isAssignableFrom(memberClazz)
                    || isTwoDimensionalArray(memberClazz))
            {
                result[i] =
                        HDF5MemberByteifyer.createArrayMemberByteifyer(members[i].getField(clazz),
                                members[i].getMemberName(), offset, fileInfoProvider, members[i]
                                        .getMemberTypeDimensions(), members[i]
                                        .getStorageDataTypeId());
            } else
            {
                result[i] =
                        HDF5MemberByteifyer.createMemberByteifyer(members[i].getField(clazz),
                                members[i].getMemberName(), offset);
            }
            offset += result[i].getSize();
        }
        return result;
    }

    private static boolean isOneDimensionalArray(final Class<?> memberClazz)
    {
        return memberClazz.isArray() && memberClazz.getComponentType().isPrimitive();
    }

    private static boolean isTwoDimensionalArray(final Class<?> memberClazz)
    {
        return memberClazz.isArray() && memberClazz.getComponentType().isArray()
                && memberClazz.getComponentType().getComponentType().isPrimitive();
    }

    public int insertMemberTypes(int dataTypeId)
    {
        for (HDF5MemberByteifyer byteifyer : byteifyers)
        {
            byteifyer.insertType(dataTypeId);
        }
        return dataTypeId;
    }

    public int insertNativeMemberTypes(int dataTypeId, HDF5 h5, ICleanUpRegistry registry)
    {
        for (HDF5MemberByteifyer byteifyer : byteifyers)
        {
            byteifyer.insertNativeType(dataTypeId, h5, registry);
        }
        return dataTypeId;
    }

    /**
     * @throw {@link HDF5JavaException} if one of the elements in <var>arr</var> exceeding its
     *        pre-defined size.
     */
    public byte[] byteify(int compoundDataTypeId, T[] arr) throws HDF5JavaException
    {
        final byte[] barray = new byte[arr.length * recordSize];
        int offset = 0;
        int counter = 0;
        for (Object obj : arr)
        {
            for (HDF5MemberByteifyer byteifyer : byteifyers)
            {
                try
                {
                    final byte[] b = byteifyer.byteify(compoundDataTypeId, obj);
                    if (b.length > byteifyer.getSize())
                    {
                        throw new HDF5JavaException("Compound " + byteifyer.describe()
                                + " of array element " + counter + " must not exceed "
                                + byteifyer.getSize() + " bytes, but is of size " + b.length
                                + " bytes.");
                    }
                    System.arraycopy(b, 0, barray, offset + byteifyer.getOffset(), b.length);
                } catch (IllegalAccessException ex)
                {
                    throw new HDF5JavaException("Error accessing " + byteifyer.describe());
                }
            }
            offset += recordSize;
            ++counter;
        }
        return barray;
    }

    /**
     * @throw {@link HDF5JavaException} if <var>obj</var> exceeding its pre-defined size.
     */
    public byte[] byteify(int compoundDataTypeId, T obj) throws HDF5JavaException
    {
        final byte[] barray = new byte[recordSize];
        for (HDF5MemberByteifyer byteifyer : byteifyers)
        {
            try
            {
                final byte[] b = byteifyer.byteify(compoundDataTypeId, obj);
                if (b.length > byteifyer.getSize())
                {
                    throw new HDF5JavaException("Compound " + byteifyer.describe()
                            + " must not exceed " + byteifyer.getSize() + " bytes, but is of size "
                            + b.length + " bytes.");
                }
                System.arraycopy(b, 0, barray, byteifyer.getOffset(), b.length);
            } catch (IllegalAccessException ex)
            {
                throw new HDF5JavaException("Error accessing " + byteifyer.describe());
            }
        }
        return barray;
    }

    public T[] arrayify(int compoundDataTypeId, byte[] byteArr, Class<T> recordClass)
    {
        final int length = byteArr.length / recordSize;
        if (length * recordSize != byteArr.length)
        {
            throw new HDF5JavaException("Illegal byte array for compound type (length "
                    + byteArr.length + " is not a multiple of record size " + recordSize + ")");
        }
        final T[] result = HDF5Utils.createArray(recordClass, length);
        int offset = 0;
        for (int i = 0; i < length; ++i)
        {
            result[i] = primArrayifyScalar(compoundDataTypeId, byteArr, recordClass, offset);
            offset += recordSize;
        }
        return result;
    }

    public T arrayifyScalar(int compoundDataTypeId, byte[] byteArr, Class<T> recordClass)
    {
        if (byteArr.length < recordSize)
        {
            throw new HDF5JavaException("Illegal byte array for scalar compound type (length "
                    + byteArr.length + " is smaller than record size " + recordSize + ")");
        }
        return primArrayifyScalar(compoundDataTypeId, byteArr, recordClass, 0);
    }

    private T primArrayifyScalar(int compoundDataTypeId, byte[] byteArr, Class<T> recordClass,
            int offset)
    {
        T result = newInstance(recordClass);
        for (HDF5MemberByteifyer byteifyer : byteifyers)
        {
            try
            {
                byteifyer.setFromByteArray(compoundDataTypeId, result, byteArr, offset);
            } catch (IllegalAccessException ex)
            {
                throw new HDF5JavaException("Error accessing " + byteifyer.describe());
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private T newInstance(Class<?> recordClass) throws HDF5JavaException
    {
        try
        {
            return (T) ReflectionUtils.newInstance(recordClass);
        } catch (Exception ex)
        {
            throw new HDF5JavaException("Creation of new object of class "
                    + recordClass.getCanonicalName() + " by default constructor failed: "
                    + ex.toString());
        }

    }

    public final int getRecordSize()
    {
        return recordSize;
    }

}
