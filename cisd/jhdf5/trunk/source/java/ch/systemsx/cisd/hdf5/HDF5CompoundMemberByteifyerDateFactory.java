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

package ch.systemsx.cisd.hdf5;

import static ch.systemsx.cisd.base.convert.NativeData.LONG_SIZE;
import static ch.systemsx.cisd.hdf5.HDF5CompoundByteifyerFactory.getArray;
import static ch.systemsx.cisd.hdf5.HDF5CompoundByteifyerFactory.getList;
import static ch.systemsx.cisd.hdf5.HDF5CompoundByteifyerFactory.getMap;
import static ch.systemsx.cisd.hdf5.HDF5CompoundByteifyerFactory.putMap;
import static ch.systemsx.cisd.hdf5.HDF5CompoundByteifyerFactory.setArray;
import static ch.systemsx.cisd.hdf5.HDF5CompoundByteifyerFactory.setList;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5T_STD_I64LE;

import java.lang.reflect.Field;
import java.util.Date;

import ch.systemsx.cisd.hdf5.HDF5CompoundByteifyerFactory.AccessType;
import ch.systemsx.cisd.hdf5.HDF5CompoundByteifyerFactory.IHDF5CompoundMemberBytifyerFactory;
import ch.systemsx.cisd.hdf5.HDF5ValueObjectByteifyer.FileInfoProvider;
import ch.systemsx.cisd.hdf5.hdf5lib.HDFNativeData;

/**
 * A {@link HDF5CompoundByteifyerFactory.IHDF5CompoundMemberBytifyerFactory} for <code>Date</code>.
 * 
 * @author Bernd Rinn
 */
class HDF5CompoundMemberByteifyerDateFactory implements IHDF5CompoundMemberBytifyerFactory
{

    public boolean canHandle(Class<?> clazz)
    {
        return (clazz == Date.class);
    }

    public Class<?> tryGetOverrideJavaType(HDF5DataClass dataClass, int rank, int elementSize,
            HDF5DataTypeVariant typeVariantOrNull)
    {
        if (dataClass == HDF5DataClass.INTEGER
                && rank == 0
                && elementSize == 8
                && typeVariantOrNull == HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH)
        {
            return java.util.Date.class;
        } else
        {
            return null;
        }
    }

    public HDF5MemberByteifyer createBytifyer(AccessType accessType, Field fieldOrNull,
            HDF5CompoundMemberMapping member,
            HDF5CompoundMemberInformation compoundMemberInfoOrNull, Class<?> memberClazz,
            int index, int offset, FileInfoProvider fileInfoProvider)
    {
        final String memberName = member.getMemberName();
        final HDF5DataTypeVariant typeVariant =
                HDF5DataTypeVariant.isTypeVariant(member.tryGetTypeVariant()) ? member
                        .tryGetTypeVariant()
                        : HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH;
        switch (accessType)
        {
            case FIELD:
                return createByteifyerForField(fieldOrNull, memberName, offset, typeVariant);
            case MAP:
                return createByteifyerForMap(memberName, offset, typeVariant);
            case LIST:
                return createByteifyerForList(memberName, index, offset, typeVariant);
            case ARRAY:
                return createByteifyerForArray(memberName, index, offset, typeVariant);
            default:
                throw new Error("Unknown access type");
        }
    }

    private HDF5MemberByteifyer createByteifyerForField(final Field field, final String memberName,
            final int offset, final HDF5DataTypeVariant typeVariant)
    {
        ReflectionUtils.ensureAccessible(field);
        return new HDF5MemberByteifyer(field, memberName, LONG_SIZE, offset, typeVariant)
            {
                @Override
                protected int getMemberStorageTypeId()
                {
                    return H5T_STD_I64LE;
                }

                @Override
                protected int getMemberNativeTypeId()
                {
                    return -1;
                }

                @Override
                public byte[] byteify(int compoundDataTypeId, Object obj)
                        throws IllegalAccessException
                {
                    return HDFNativeData.longToByte(((java.util.Date) field.get(obj)).getTime());
                }

                @Override
                public void setFromByteArray(int compoundDataTypeId, Object obj, byte[] byteArr,
                        int arrayOffset) throws IllegalAccessException
                {
                    field.set(
                            obj,
                            new java.util.Date(HDFNativeData.byteToLong(byteArr, arrayOffset
                                    + offset)));
                }
            };
    }

    private HDF5MemberByteifyer createByteifyerForMap(final String memberName, final int offset,
            final HDF5DataTypeVariant typeVariant)
    {
        return new HDF5MemberByteifyer(null, memberName, LONG_SIZE, offset, typeVariant)
            {
                @Override
                protected int getMemberStorageTypeId()
                {
                    return H5T_STD_I64LE;
                }

                @Override
                protected int getMemberNativeTypeId()
                {
                    return -1;
                }

                @Override
                public byte[] byteify(int compoundDataTypeId, Object obj)
                        throws IllegalAccessException
                {
                    final Object dateObj = getMap(obj, memberName);
                    if (dateObj instanceof java.util.Date)
                    {
                        return HDFNativeData.longToByte(((java.util.Date) dateObj).getTime());
                    } else
                    {
                        return HDFNativeData.longToByte(((Long) dateObj));
                    }
                }

                @Override
                public void setFromByteArray(int compoundDataTypeId, Object obj, byte[] byteArr,
                        int arrayOffset) throws IllegalAccessException
                {
                    putMap(obj,
                            memberName,
                            new java.util.Date(HDFNativeData.byteToLong(byteArr, arrayOffset
                                    + offset)));
                }
            };
    }

    private HDF5MemberByteifyer createByteifyerForList(final String memberName, final int index,
            final int offset, final HDF5DataTypeVariant typeVariant)
    {
        return new HDF5MemberByteifyer(null, memberName, LONG_SIZE, offset, typeVariant)
            {
                @Override
                protected int getMemberStorageTypeId()
                {
                    return H5T_STD_I64LE;
                }

                @Override
                protected int getMemberNativeTypeId()
                {
                    return -1;
                }

                @Override
                public byte[] byteify(int compoundDataTypeId, Object obj)
                        throws IllegalAccessException
                {
                    final Object dateObj = getList(obj, index);
                    if (dateObj instanceof java.util.Date)
                    {
                        return HDFNativeData.longToByte(((java.util.Date) dateObj).getTime());
                    } else
                    {
                        return HDFNativeData.longToByte(((Long) dateObj));
                    }
                }

                @Override
                public void setFromByteArray(int compoundDataTypeId, Object obj, byte[] byteArr,
                        int arrayOffset) throws IllegalAccessException
                {
                    setList(obj,
                            index,
                            new java.util.Date(HDFNativeData.byteToLong(byteArr, arrayOffset
                                    + offset)));
                }
            };
    }

    private HDF5MemberByteifyer createByteifyerForArray(final String memberName, final int index,
            final int offset, final HDF5DataTypeVariant typeVariant)
    {
        return new HDF5MemberByteifyer(null, memberName, LONG_SIZE, offset, typeVariant)
            {
                @Override
                protected int getMemberStorageTypeId()
                {
                    return H5T_STD_I64LE;
                }

                @Override
                protected int getMemberNativeTypeId()
                {
                    return -1;
                }

                @Override
                public byte[] byteify(int compoundDataTypeId, Object obj)
                        throws IllegalAccessException
                {
                    final Object dateObj = getArray(obj, index);
                    if (dateObj instanceof java.util.Date)
                    {
                        return HDFNativeData.longToByte(((java.util.Date) dateObj).getTime());
                    } else
                    {
                        return HDFNativeData.longToByte(((Long) dateObj));
                    }
                }

                @Override
                public void setFromByteArray(int compoundDataTypeId, Object obj, byte[] byteArr,
                        int arrayOffset) throws IllegalAccessException
                {
                    setArray(
                            obj,
                            index,
                            new java.util.Date(HDFNativeData.byteToLong(byteArr, arrayOffset
                                    + offset)));
                }
            };
    }

}
