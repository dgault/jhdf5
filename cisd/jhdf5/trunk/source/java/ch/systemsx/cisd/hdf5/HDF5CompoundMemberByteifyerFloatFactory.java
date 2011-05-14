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

import static ch.systemsx.cisd.base.convert.NativeData.FLOAT_SIZE;
import static ch.systemsx.cisd.hdf5.HDF5CompoundByteifyerFactory.getArray;
import static ch.systemsx.cisd.hdf5.HDF5CompoundByteifyerFactory.getList;
import static ch.systemsx.cisd.hdf5.HDF5CompoundByteifyerFactory.getMap;
import static ch.systemsx.cisd.hdf5.HDF5CompoundByteifyerFactory.putMap;
import static ch.systemsx.cisd.hdf5.HDF5CompoundByteifyerFactory.setArray;
import static ch.systemsx.cisd.hdf5.HDF5CompoundByteifyerFactory.setList;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_IEEE_F32LE;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;

import ncsa.hdf.hdf5lib.HDFNativeData;

import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.hdf5.HDF5CompoundByteifyerFactory.AccessType;
import ch.systemsx.cisd.hdf5.HDF5CompoundByteifyerFactory.IHDF5CompoundMemberBytifyerFactory;
import ch.systemsx.cisd.hdf5.HDF5ValueObjectByteifyer.FileInfoProvider;

/**
 * A {@link HDF5CompoundByteifyerFactory.IHDF5CompoundMemberBytifyerFactory} for <code>float</code>,
 * <code>float[]</code>, <code>float[][]</code> and <code>MDFloatArray</code>.
 * 
 * @author Bernd Rinn
 */
class HDF5CompoundMemberByteifyerFloatFactory implements IHDF5CompoundMemberBytifyerFactory
{

    private static Map<Class<?>, Rank> classToRankMap = new IdentityHashMap<Class<?>, Rank>();

    private enum Rank
    {
        SCALAR(float.class, true), ARRAY1D(float[].class, false), ARRAY2D(float[][].class, false),
        ARRAYMD(MDFloatArray.class, false);

        private Class<?> clazz;

        private boolean scalar;

        Rank(Class<?> clazz, boolean scalar)
        {
            this.clazz = clazz;
            this.scalar = scalar;
        }

        boolean isScalar()
        {
            return scalar;
        }

        Class<?> getClazz()
        {
            return clazz;
        }
    }

    static
    {
        for (Rank r : Rank.values())
        {
            classToRankMap.put(r.getClazz(), r);
        }
    }

    public boolean canHandle(Class<?> clazz)
    {
        return classToRankMap.containsKey(clazz);
    }

    public Class<?> tryGetOverrideJavaType(HDF5DataClass dataClass, int rank, int elementSize,
            HDF5DataTypeVariant typeVariantOrNull)
    {
        return null;
    }

    public HDF5MemberByteifyer createBytifyer(AccessType accessType, Field fieldOrNull,
            HDF5CompoundMemberMapping member, Class<?> memberClazz, int index, int offset,
            FileInfoProvider fileInfoProvider)
    {
        final String memberName = member.getMemberName();
        final Rank rank = classToRankMap.get(memberClazz);
        final int len = rank.isScalar() ? 1 : member.getMemberTypeLength();
        final int[] dimensions = rank.isScalar() ? new int[]
            { 1 } : member.getMemberTypeDimensions();
        final int storageTypeId = member.getStorageDataTypeId();
        final int memberTypeId =
                rank.isScalar() ? H5T_IEEE_F32LE : ((storageTypeId < 0) ? fileInfoProvider
                        .getArrayTypeId(H5T_IEEE_F32LE, dimensions) : storageTypeId);
        switch (accessType)
        {
            case FIELD:
                return createByteifyerForField(fieldOrNull, memberName, offset, dimensions, len,
                        memberTypeId, rank, member.tryGetTypeVariant());
            case MAP:
                return createByteifyerForMap(memberName, offset, dimensions, len, memberTypeId,
                        rank, member.tryGetTypeVariant());
            case LIST:
                return createByteifyerForList(memberName, index, offset, dimensions, len,
                        memberTypeId, rank, member.tryGetTypeVariant());
            case ARRAY:
                return createByteifyerForArray(memberName, index, offset, dimensions, len,
                        memberTypeId, rank, member.tryGetTypeVariant());
            default:
                throw new Error("Unknown access type");
        }
    }

    private HDF5MemberByteifyer createByteifyerForField(final Field field, final String memberName,
            final int offset, final int[] dimensions, final int len, final int memberTypeId,
            final Rank rank, final HDF5DataTypeVariant typeVariant)
    {
        ReflectionUtils.ensureAccessible(field);
        return new HDF5MemberByteifyer(field, memberName, FLOAT_SIZE * len, offset, typeVariant)
            {
                @Override
                protected int getMemberStorageTypeId()
                {
                    return memberTypeId;
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
                    switch (rank)
                    {
                        case SCALAR:
                            return HDFNativeData.floatToByte(field.getFloat(obj));
                        case ARRAY1D:
                            return HDFNativeData.floatToByte((float[]) field.get(obj));
                        case ARRAY2D:
                        {
                            final float[][] array = (float[][]) field.get(obj);
                            MatrixUtils.checkMatrixDimensions(memberName, dimensions, array);
                            return HDFNativeData.floatToByte(MatrixUtils.flatten(array));
                        }
                        case ARRAYMD:
                        {
                            final MDFloatArray array = (MDFloatArray) field.get(obj);
                            MatrixUtils.checkMDArrayDimensions(memberName, dimensions, array);
                            return HDFNativeData.floatToByte(array.getAsFlatArray());
                        }
                        default:
                            throw new Error("Unknown rank.");
                    }
                }

                @Override
                public void setFromByteArray(int compoundDataTypeId, Object obj, byte[] byteArr,
                        int arrayOffset) throws IllegalAccessException
                {
                    switch (rank)
                    {
                        case SCALAR:
                            field.setFloat(obj,
                                    HDFNativeData.byteToFloat(byteArr, arrayOffset + offset));
                            break;
                        case ARRAY1D:
                            field.set(obj,
                                    HDFNativeData.byteToFloat(byteArr, arrayOffset + offset, len));
                            break;
                        case ARRAY2D:
                        {
                            final float[] array =
                                    HDFNativeData.byteToFloat(byteArr, arrayOffset + offset, len);
                            field.set(obj, MatrixUtils.shapen(array, dimensions));
                            break;
                        }
                        case ARRAYMD:
                        {
                            final float[] array =
                                    HDFNativeData.byteToFloat(byteArr, arrayOffset + offset, len);
                            field.set(obj, new MDFloatArray(array, dimensions));
                            break;
                        }
                        default:
                            throw new Error("Unknown rank.");
                    }
                }
            };
    }

    private HDF5MemberByteifyer createByteifyerForMap(final String memberName, final int offset,
            final int[] dimensions, final int len, final int memberTypeId, final Rank rank,
            final HDF5DataTypeVariant typeVariant)
    {
        return new HDF5MemberByteifyer(null, memberName, FLOAT_SIZE * len, offset, typeVariant)
            {
                @Override
                protected int getMemberStorageTypeId()
                {
                    return memberTypeId;
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
                    switch (rank)
                    {
                        case SCALAR:
                            return HDFNativeData.floatToByte((Float) getMap(obj, memberName));
                        case ARRAY1D:
                            return HDFNativeData.floatToByte((float[]) getMap(obj, memberName));
                        case ARRAY2D:
                        {
                            final float[][] array = (float[][]) getMap(obj, memberName);
                            MatrixUtils.checkMatrixDimensions(memberName, dimensions, array);
                            return HDFNativeData.floatToByte(MatrixUtils.flatten(array));
                        }
                        case ARRAYMD:
                        {
                            final MDFloatArray array = (MDFloatArray) getMap(obj, memberName);
                            MatrixUtils.checkMDArrayDimensions(memberName, dimensions, array);
                            return HDFNativeData.floatToByte(array.getAsFlatArray());
                        }
                        default:
                            throw new Error("Unknown rank.");
                    }
                }

                @Override
                public void setFromByteArray(int compoundDataTypeId, Object obj, byte[] byteArr,
                        int arrayOffset) throws IllegalAccessException
                {
                    switch (rank)
                    {
                        case SCALAR:
                            putMap(obj, memberName,
                                    HDFNativeData.byteToFloat(byteArr, arrayOffset + offset));
                            break;
                        case ARRAY1D:
                            putMap(obj, memberName,
                                    HDFNativeData.byteToFloat(byteArr, arrayOffset + offset, len));
                            break;
                        case ARRAY2D:
                        {
                            final float[] array =
                                    HDFNativeData.byteToFloat(byteArr, arrayOffset + offset, len);
                            putMap(obj, memberName, MatrixUtils.shapen(array, dimensions));
                            break;
                        }
                        case ARRAYMD:
                        {
                            final float[] array =
                                    HDFNativeData.byteToFloat(byteArr, arrayOffset + offset, len);
                            putMap(obj, memberName, new MDFloatArray(array, dimensions));
                            break;
                        }
                        default:
                            throw new Error("Unknown rank.");
                    }
                }
            };
    }

    private HDF5MemberByteifyer createByteifyerForList(final String memberName, final int index,
            final int offset, final int[] dimensions, final int len, final int memberTypeId,
            final Rank rank, final HDF5DataTypeVariant typeVariant)
    {
        return new HDF5MemberByteifyer(null, memberName, FLOAT_SIZE * len, offset, typeVariant)
            {
                @Override
                protected int getMemberStorageTypeId()
                {
                    return memberTypeId;
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
                    switch (rank)
                    {
                        case SCALAR:
                            return HDFNativeData.floatToByte((Float) getList(obj, index));
                        case ARRAY1D:
                            return HDFNativeData.floatToByte((float[]) getList(obj, index));
                        case ARRAY2D:
                        {
                            final float[][] array = (float[][]) getList(obj, index);
                            MatrixUtils.checkMatrixDimensions(memberName, dimensions, array);
                            return HDFNativeData.floatToByte(MatrixUtils.flatten(array));
                        }
                        case ARRAYMD:
                        {
                            final MDFloatArray array = (MDFloatArray) getList(obj, index);
                            MatrixUtils.checkMDArrayDimensions(memberName, dimensions, array);
                            return HDFNativeData.floatToByte(array.getAsFlatArray());
                        }
                        default:
                            throw new Error("Unknown rank.");
                    }
                }

                @Override
                public void setFromByteArray(int compoundDataTypeId, Object obj, byte[] byteArr,
                        int arrayOffset) throws IllegalAccessException
                {
                    switch (rank)
                    {
                        case SCALAR:
                            setList(obj, index,
                                    HDFNativeData.byteToFloat(byteArr, arrayOffset + offset));
                            break;
                        case ARRAY1D:
                            putMap(obj, memberName,
                                    HDFNativeData.byteToFloat(byteArr, arrayOffset + offset, len));
                            break;
                        case ARRAY2D:
                        {
                            final float[] array =
                                    HDFNativeData.byteToFloat(byteArr, arrayOffset + offset, len);
                            setList(obj, index, MatrixUtils.shapen(array, dimensions));
                            break;
                        }
                        case ARRAYMD:
                        {
                            final float[] array =
                                    HDFNativeData.byteToFloat(byteArr, arrayOffset + offset, len);
                            setList(obj, index, new MDFloatArray(array, dimensions));
                            break;
                        }
                        default:
                            throw new Error("Unknown rank.");
                    }
                }
            };
    }

    private HDF5MemberByteifyer createByteifyerForArray(final String memberName, final int index,
            final int offset, final int[] dimensions, final int len, final int memberTypeId,
            final Rank rank, final HDF5DataTypeVariant typeVariant)
    {
        return new HDF5MemberByteifyer(null, memberName, FLOAT_SIZE * len, offset, typeVariant)
            {
                @Override
                protected int getMemberStorageTypeId()
                {
                    return memberTypeId;
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
                    switch (rank)
                    {
                        case SCALAR:
                            return HDFNativeData.floatToByte((Float) getArray(obj, index));
                        case ARRAY1D:
                            return HDFNativeData.floatToByte((float[]) getArray(obj, index));
                        case ARRAY2D:
                        {
                            final float[][] array = (float[][]) getArray(obj, index);
                            MatrixUtils.checkMatrixDimensions(memberName, dimensions, array);
                            return HDFNativeData.floatToByte(MatrixUtils.flatten(array));
                        }
                        case ARRAYMD:
                        {
                            final MDFloatArray array = (MDFloatArray) getArray(obj, index);
                            MatrixUtils.checkMDArrayDimensions(memberName, dimensions, array);
                            return HDFNativeData.floatToByte(array.getAsFlatArray());
                        }
                        default:
                            throw new Error("Unknown rank.");
                    }
                }

                @Override
                public void setFromByteArray(int compoundDataTypeId, Object obj, byte[] byteArr,
                        int arrayOffset) throws IllegalAccessException
                {
                    switch (rank)
                    {
                        case SCALAR:
                            setArray(obj, index,
                                    HDFNativeData.byteToFloat(byteArr, arrayOffset + offset));
                            break;
                        case ARRAY1D:
                            setArray(obj, index,
                                    HDFNativeData.byteToFloat(byteArr, arrayOffset + offset, len));
                            break;
                        case ARRAY2D:
                        {
                            final float[] array =
                                    HDFNativeData.byteToFloat(byteArr, arrayOffset + offset, len);
                            setArray(obj, index, MatrixUtils.shapen(array, dimensions));
                            break;
                        }
                        case ARRAYMD:
                        {
                            final float[] array =
                                    HDFNativeData.byteToFloat(byteArr, arrayOffset + offset, len);
                            setArray(obj, index, new MDFloatArray(array, dimensions));
                            break;
                        }
                        default:
                            throw new Error("Unknown rank.");
                    }
                }
            };
    }

}
