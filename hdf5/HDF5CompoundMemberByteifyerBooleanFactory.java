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

import static ch.systemsx.cisd.hdf5.HDF5CompoundByteifyerFactory.*;

import java.lang.reflect.Field;

import ch.systemsx.cisd.hdf5.HDF5ValueObjectByteifyer.FileInfoProvider;
import ch.systemsx.cisd.hdf5.hdf5lib.HDFNativeData;

/**
 * A {@link HDF5CompoundByteifyerFactory.IHDF5CompoundMemberBytifyerFactory} for
 * <code>boolean</code>.
 * 
 * @author Bernd Rinn
 */
class HDF5CompoundMemberByteifyerBooleanFactory implements IHDF5CompoundMemberBytifyerFactory
{

    public boolean canHandle(Class<?> clazz, HDF5CompoundMemberInformation memberInfoOrNull)
    {
        if (memberInfoOrNull != null)
        {
            final HDF5DataClass dataClass = memberInfoOrNull.getType().getDataClass();
            return (clazz == boolean.class)
                    && (dataClass == HDF5DataClass.BOOLEAN || dataClass == HDF5DataClass.INTEGER);
        } else
        {
            return (clazz == boolean.class);
        }
    }

    public Class<?> tryGetOverrideJavaType(HDF5DataClass dataClass, int rank, int elementSize,
            HDF5DataTypeVariant typeVariantOrNull)
    {
        return null;
    }

    public HDF5MemberByteifyer createBytifyer(final AccessType accessType, final Field fieldOrNull,
            final HDF5CompoundMemberMapping member,
            final HDF5CompoundMemberInformation compoundMemberInfoOrNull,
            HDF5EnumerationType enumTypeOrNull, final Class<?> memberClazz, final int index,
            final int offset, final FileInfoProvider fileInfoProvider)
    {
        final String memberName = member.getMemberName();
        // May be -1 if not known
        final int memberTypeId = member.getStorageDataTypeId();
        final int booleanDataTypeId =
                (memberTypeId < 0) ? fileInfoProvider.getBooleanDataTypeId() : memberTypeId;
        switch (accessType)
        {
            case FIELD:
                return createByteifyerForField(fieldOrNull, memberName, offset, booleanDataTypeId,
                        member.tryGetTypeVariant());
            case MAP:
                return createByteifyerForMap(memberName, offset, booleanDataTypeId,
                        member.tryGetTypeVariant());
            case LIST:
                return createByteifyerForList(memberName, index, offset, booleanDataTypeId,
                        member.tryGetTypeVariant());
            case ARRAY:
                return createByteifyerForArray(memberName, index, offset, booleanDataTypeId,
                        member.tryGetTypeVariant());
            default:
                throw new Error("Unknown access type");
        }
    }

    private HDF5MemberByteifyer createByteifyerForField(final Field field, final String memberName,
            final int offset, final int booleanDataTypeId, final HDF5DataTypeVariant typeVariant)
    {
        ReflectionUtils.ensureAccessible(field);
        return new HDF5MemberByteifyer(field, memberName, 1, offset, typeVariant)
            {
                @Override
                protected int getMemberStorageTypeId()
                {
                    return booleanDataTypeId;
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
                    return HDFNativeData.byteToByte((byte) (field.getBoolean(obj) ? 1 : 0));
                }

                @Override
                public void setFromByteArray(int compoundDataTypeId, Object obj, byte[] byteArr,
                        int arrayOffset) throws IllegalAccessException
                {
                    final boolean value = (byteArr[arrayOffset + offset] == 0) ? false : true;
                    field.setBoolean(obj, value);
                }
            };
    }

    private HDF5MemberByteifyer createByteifyerForMap(final String memberName, final int offset,
            final int booleanDataTypeId, final HDF5DataTypeVariant typeVariant)
    {
        return new HDF5MemberByteifyer(null, memberName, 1, offset, typeVariant)
            {
                @Override
                protected int getMemberStorageTypeId()
                {
                    return booleanDataTypeId;
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
                    return HDFNativeData.byteToByte((byte) (((Boolean) getMap(obj, memberName)) ? 1
                            : 0));
                }

                @Override
                public void setFromByteArray(int compoundDataTypeId, Object obj, byte[] byteArr,
                        int arrayOffset) throws IllegalAccessException
                {
                    final boolean value = (byteArr[arrayOffset + offset] == 0) ? false : true;
                    putMap(obj, memberName, value);
                }
            };
    }

    private HDF5MemberByteifyer createByteifyerForList(final String memberName, final int index,
            final int offset, final int booleanDataTypeId, final HDF5DataTypeVariant typeVariant)
    {
        return new HDF5MemberByteifyer(null, memberName, 1, offset, typeVariant)
            {
                @Override
                protected int getMemberStorageTypeId()
                {
                    return booleanDataTypeId;
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
                    return HDFNativeData
                            .byteToByte((byte) (((Boolean) getList(obj, index)) ? 1 : 0));
                }

                @Override
                public void setFromByteArray(int compoundDataTypeId, Object obj, byte[] byteArr,
                        int arrayOffset) throws IllegalAccessException
                {
                    final boolean value = (byteArr[arrayOffset + offset] == 0) ? false : true;
                    setList(obj, index, value);
                }
            };
    }

    private HDF5MemberByteifyer createByteifyerForArray(final String memberName, final int index,
            final int offset, final int booleanDataTypeId, final HDF5DataTypeVariant typeVariant)
    {
        return new HDF5MemberByteifyer(null, memberName, 1, offset, typeVariant)
            {
                @Override
                protected int getMemberStorageTypeId()
                {
                    return booleanDataTypeId;
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
                    return HDFNativeData.byteToByte((byte) (((Boolean) getArray(obj, index)) ? 1
                            : 0));
                }

                @Override
                public void setFromByteArray(int compoundDataTypeId, Object obj, byte[] byteArr,
                        int arrayOffset) throws IllegalAccessException
                {
                    final boolean value = (byteArr[arrayOffset + offset] == 0) ? false : true;
                    setArray(obj, index, value);
                }
            };
    }

}
