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

import static ch.systemsx.cisd.hdf5.hdf5lib.H5D.H5Dwrite;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5P_DEFAULT;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5S_ALL;
import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;

import ch.systemsx.cisd.hdf5.cleanup.ICallableWithCleanUp;
import ch.systemsx.cisd.hdf5.cleanup.ICleanUpRegistry;

/**
 * The implementation of {@link IHDF5EnumWriter}.
 * 
 * @author Bernd Rinn
 */
class HDF5EnumWriter implements IHDF5EnumWriter
{
    private final HDF5BaseWriter baseWriter;

    HDF5EnumWriter(HDF5BaseWriter baseWriter)
    {
        this.baseWriter = baseWriter;
    }

    // /////////////////////
    // Types
    // /////////////////////

    public HDF5EnumerationType getEnumType(final String name, final String[] values)
            throws HDF5JavaException
    {
        return getEnumType(name, values, true);
    }

    public HDF5EnumerationType getEnumType(final String name, final String[] values,
            final boolean check) throws HDF5JavaException
    {
        baseWriter.checkOpen();
        final int storageDataTypeId =
                getOrCreateEnumDataType(name, values, baseWriter.keepDataSetIfExists, check);
        final int nativeDataTypeId =
                baseWriter.h5.getNativeDataType(storageDataTypeId, baseWriter.fileRegistry);
        return new HDF5EnumerationType(baseWriter.fileId, storageDataTypeId, nativeDataTypeId,
                name, values);
    }

    public HDF5EnumerationType getEnumType(final String name,
            final Class<? extends Enum<?>> enumClass) throws HDF5JavaException
    {
        return getEnumType(name, HDF5EnumReader.getEnumOptions(enumClass), true);
    }

    public HDF5EnumerationType getEnumType(final String name,
            final Class<? extends Enum<?>> enumClass, final boolean check) throws HDF5JavaException
    {
        return getEnumType(name, HDF5EnumReader.getEnumOptions(enumClass), check);
    }

    public HDF5EnumerationType getEnumType(final Class<? extends Enum<?>> enumClass)
            throws HDF5JavaException
    {
        return getEnumType(enumClass.getSimpleName(), HDF5EnumReader.getEnumOptions(enumClass),
                true);
    }

    public HDF5EnumerationType getEnumType(final Class<? extends Enum<?>> enumClass,
            final boolean check) throws HDF5JavaException
    {
        return getEnumType(enumClass.getSimpleName(), HDF5EnumReader.getEnumOptions(enumClass),
                check);
    }

    private int getOrCreateEnumDataType(final String dataTypeName, final String[] values,
            boolean committedDataTypeHasPreference, boolean checkIfExists)
    {
        final String dataTypePath =
                HDF5Utils.createDataTypePath(HDF5Utils.ENUM_PREFIX, dataTypeName);
        final int committedStorageDataTypeId = baseWriter.getDataTypeId(dataTypePath);
        final boolean typeExists = (committedStorageDataTypeId >= 0);
        int storageDataTypeId = committedStorageDataTypeId;
        final boolean commitType;
        if ((typeExists == false) || (committedDataTypeHasPreference == false))
        {
            storageDataTypeId = baseWriter.h5.createDataTypeEnum(values, baseWriter.fileRegistry);
            final boolean typesAreEqual =
                    typeExists
                            && baseWriter.h5.dataTypesAreEqual(committedStorageDataTypeId,
                                    storageDataTypeId);
            commitType = (typeExists == false) || (typesAreEqual == false);
            if (typeExists && commitType)
            {
                final String replacementDataTypePath = baseWriter.moveLinkOutOfTheWay(dataTypePath);
                baseWriter.renameNamedDataType(dataTypePath, replacementDataTypePath);
            }
            if (typesAreEqual)
            {
                storageDataTypeId = committedStorageDataTypeId;
            }
        } else
        {
            commitType = false;
            if (checkIfExists)
            {
                baseWriter.checkEnumValues(storageDataTypeId, values, dataTypeName);
            }
        }
        if (commitType)
        {
            baseWriter.commitDataType(dataTypePath, storageDataTypeId);
        }
        return storageDataTypeId;
    }

    // /////////////////////
    // Attributes
    // /////////////////////

    public void setEnumAttribute(final String objectPath, final String name,
            final HDF5EnumerationValue value)
    {
        assert objectPath != null;
        assert name != null;
        assert value != null;

        baseWriter.checkOpen();
        value.getType().check(baseWriter.fileId);
        final int storageDataTypeId = value.getType().getStorageTypeId();
        final int nativeDataTypeId = value.getType().getNativeTypeId();
        baseWriter.setAttribute(objectPath, name, storageDataTypeId, nativeDataTypeId,
                value.toStorageForm());
    }

    public void setEnumArrayAttribute(final String objectPath, final String name,
            final HDF5EnumerationValueArray value)
    {
        baseWriter.setEnumArrayAttribute(objectPath, name, value);
    }

    // /////////////////////
    // Data Sets
    // /////////////////////

    public void writeEnum(final String objectPath, final HDF5EnumerationValue value)
            throws HDF5JavaException
    {
        assert objectPath != null;
        assert value != null;

        baseWriter.checkOpen();
        value.getType().check(baseWriter.fileId);
        final int storageDataTypeId = value.getType().getStorageTypeId();
        final int nativeDataTypeId = value.getType().getNativeTypeId();
        baseWriter.writeScalar(objectPath, storageDataTypeId, nativeDataTypeId,
                value.toStorageForm());
    }

    public void writeEnum(final String objectPath, final Enum<?> value) throws HDF5JavaException
    {
        @SuppressWarnings("unchecked")
        final Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) value.getClass();
        writeEnum(objectPath, new HDF5EnumerationValue(getEnumType(enumClass), value));
    }

    public void writeEnumArray(final String objectPath, final HDF5EnumerationValueArray data)
            throws HDF5JavaException
    {
        writeEnumArray(objectPath, data, HDF5IntStorageFeatures.INT_NO_COMPRESSION);
    }

    public <T extends Enum<T>> void writeEnumArray(final String objectPath, Class<T> enumClass,
            final Enum<T>[] data) throws HDF5JavaException
    {
        writeEnumArray(objectPath, new HDF5EnumerationValueArray(getEnumType(enumClass), data));
    }

    public void writeEnumArray(final String objectPath, final HDF5EnumerationValueArray data,
            final HDF5IntStorageFeatures features) throws HDF5JavaException
    {
        assert objectPath != null;
        assert data != null;

        baseWriter.checkOpen();
        data.getType().check(baseWriter.fileId);
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    if (features.isScaling())
                    {
                        features.checkScalingOK(baseWriter.fileFormat);
                        final HDF5IntStorageFeatures actualFeatures =
                                HDF5IntStorageFeatures.createDeflateAndIntegerScaling(features
                                        .getDeflateLevel(), data.getType().getNumberOfBits(),
                                        baseWriter.keepDataIfExists(features));
                        final int dataSetId =
                                baseWriter.getOrCreateDataSetId(objectPath, data.getType()
                                        .getIntStorageTypeId(), new long[]
                                    { data.getLength() }, data.getStorageForm().getStorageSize(),
                                        actualFeatures, registry);
                        H5Dwrite(dataSetId, data.getType().getIntNativeTypeId(), H5S_ALL, H5S_ALL,
                                H5P_DEFAULT, data.toStorageForm());
                        baseWriter.setTypeVariant(dataSetId, HDF5DataTypeVariant.ENUM, registry);
                        baseWriter.setStringAttribute(dataSetId,
                                HDF5Utils.ENUM_TYPE_NAME_ATTRIBUTE, data.getType().getName(), data
                                        .getType().getName().length(), registry);
                    } else
                    {
                        final int dataSetId =
                                baseWriter.getOrCreateDataSetId(objectPath, data.getType()
                                        .getStorageTypeId(), new long[]
                                    { data.getLength() }, data.getStorageForm().getStorageSize(),
                                        features, registry);
                        H5Dwrite(dataSetId, data.getType().getNativeTypeId(), H5S_ALL, H5S_ALL,
                                H5P_DEFAULT, data.toStorageForm());
                    }
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    public <T extends Enum<T>> void writeEnumArray(final String objectPath, Class<T> enumClass,
            final Enum<T>[] data, HDF5IntStorageFeatures features) throws HDF5JavaException
    {
        writeEnumArray(objectPath, new HDF5EnumerationValueArray(getEnumType(enumClass), data),
                features);
    }

    public void createEnumArray(final String objectPath, final HDF5EnumerationType enumType,
            final int size)
    {
        createEnumArray(objectPath, enumType, size, HDF5IntStorageFeatures.INT_NO_COMPRESSION);
    }

    public void createEnumArray(final String objectPath, final HDF5EnumerationType enumType,
            final long size, final int blockSize)
    {
        createEnumArray(objectPath, enumType, size, blockSize,
                HDF5IntStorageFeatures.INT_NO_COMPRESSION);
    }

    public void createEnumArray(final String objectPath, final HDF5EnumerationType enumType,
            final long size, final int blockSize, final HDF5IntStorageFeatures features)
    {
        baseWriter.checkOpen();
        enumType.check(baseWriter.fileId);
        final ICallableWithCleanUp<Void> createRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    if (features.isScaling())
                    {
                        features.checkScalingOK(baseWriter.fileFormat);
                        final HDF5IntStorageFeatures actualCompression =
                                HDF5IntStorageFeatures.createDeflateAndIntegerScaling(
                                        features.getDeflateLevel(), enumType.getNumberOfBits());
                        final int dataSetId =
                                baseWriter.createDataSet(objectPath,
                                        enumType.getIntStorageTypeId(), actualCompression,
                                        new long[]
                                            { size }, new long[]
                                            { blockSize }, enumType.getStorageForm()
                                                .getStorageSize(), registry);
                        baseWriter.setTypeVariant(dataSetId, HDF5DataTypeVariant.ENUM, registry);
                        baseWriter.setStringAttribute(dataSetId,
                                HDF5Utils.ENUM_TYPE_NAME_ATTRIBUTE, enumType.getName(), enumType
                                        .getName().length(), registry);
                    } else
                    {
                        baseWriter.createDataSet(objectPath, enumType.getStorageTypeId(), features,
                                new long[]
                                    { size }, new long[]
                                    { blockSize }, enumType.getStorageForm().getStorageSize(),
                                registry);
                    }
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(createRunnable);
    }

    public void createEnumArray(final String objectPath, final HDF5EnumerationType enumType,
            final long size, final HDF5IntStorageFeatures features)
    {
        baseWriter.checkOpen();
        enumType.check(baseWriter.fileId);
        final ICallableWithCleanUp<Void> createRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    if (features.requiresChunking())
                    {
                        create(new long[]
                            { 0 }, new long[]
                            { size }, registry);
                    } else
                    {
                        create(new long[]
                            { size }, null, registry);
                    }
                    return null; // Nothing to return.
                }

                private void create(final long[] dimensions, final long[] blockDimensionsOrNull,
                        final ICleanUpRegistry registry)
                {
                    if (features.isScaling())
                    {
                        features.checkScalingOK(baseWriter.fileFormat);
                        final HDF5IntStorageFeatures actualCompression =
                                HDF5IntStorageFeatures.createDeflateAndIntegerScaling(
                                        features.getDeflateLevel(), enumType.getNumberOfBits());
                        final int dataSetId =
                                baseWriter.createDataSet(objectPath,
                                        enumType.getIntStorageTypeId(), actualCompression,
                                        dimensions, blockDimensionsOrNull, enumType
                                                .getStorageForm().getStorageSize(), registry);
                        baseWriter.setTypeVariant(dataSetId, HDF5DataTypeVariant.ENUM, registry);
                        baseWriter.setStringAttribute(dataSetId,
                                HDF5Utils.ENUM_TYPE_NAME_ATTRIBUTE, enumType.getName(), enumType
                                        .getName().length(), registry);
                    } else
                    {
                        baseWriter.createDataSet(objectPath, enumType.getStorageTypeId(), features,
                                dimensions, blockDimensionsOrNull, enumType.getStorageForm()
                                        .getStorageSize(), registry);
                    }
                }
            };
        baseWriter.runner.call(createRunnable);
    }

    public void writeEnumArrayBlock(final String objectPath, final HDF5EnumerationValueArray data,
            final long blockNumber)
    {
        assert objectPath != null;
        assert data != null;

        writeEnumArrayBlockWithOffset(objectPath, data, data.getLength(), data.getLength()
                * blockNumber);
    }

    public void writeEnumArrayBlockWithOffset(final String objectPath,
            final HDF5EnumerationValueArray data, final int dataSize, final long offset)
    {
        assert objectPath != null;
        assert data != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final long[] blockDimensions = new long[]
                        { dataSize };
                    final long[] slabStartOrNull = new long[]
                        { offset };
                    final int dataSetId =
                            baseWriter.h5.openAndExtendDataSet(baseWriter.fileId, objectPath,
                                    baseWriter.fileFormat, new long[]
                                        { offset + dataSize }, -1, registry);
                    final int dataSpaceId =
                            baseWriter.h5.getDataSpaceForDataSet(dataSetId, registry);
                    baseWriter.h5.setHyperslabBlock(dataSpaceId, slabStartOrNull, blockDimensions);
                    final int memorySpaceId =
                            baseWriter.h5.createSimpleDataSpace(blockDimensions, registry);
                    if (baseWriter.isScaledEnum(dataSetId, registry))
                    {
                        H5Dwrite(dataSetId, data.getType().getIntNativeTypeId(), memorySpaceId,
                                dataSpaceId, H5P_DEFAULT, data.toStorageForm());
                        baseWriter.setTypeVariant(dataSetId, HDF5DataTypeVariant.ENUM, registry);
                        baseWriter.setStringAttribute(dataSetId,
                                HDF5Utils.ENUM_TYPE_NAME_ATTRIBUTE, data.getType().getName(), data
                                        .getType().getName().length(), registry);
                    } else
                    {
                        H5Dwrite(dataSetId, data.getType().getNativeTypeId(), memorySpaceId,
                                dataSpaceId, H5P_DEFAULT, data.toStorageForm());
                    }
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

}
