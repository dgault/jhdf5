/*
 * Copyright 2007 ETH Zuerich, CISD.
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

import static ch.systemsx.cisd.hdf5.HDF5Utils.ENUM_PREFIX;
import static ch.systemsx.cisd.hdf5.HDF5Utils.createDataTypePath;
import static ch.systemsx.cisd.hdf5.HDF5Utils.getOneDimensionalArraySize;
import static ch.systemsx.cisd.hdf5.HDF5Utils.removeInternalNames;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_ENUM;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_NATIVE_B64;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_NATIVE_INT16;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_NATIVE_INT32;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_NATIVE_INT64;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_NATIVE_INT8;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_STRING;

import java.io.File;
import java.util.BitSet;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import ncsa.hdf.hdf5lib.HDFNativeData;
import ncsa.hdf.hdf5lib.exceptions.HDF5DatatypeInterfaceException;
import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;

import ch.systemsx.cisd.base.mdarray.MDArray;
import ch.systemsx.cisd.base.mdarray.MDByteArray;
import ch.systemsx.cisd.base.mdarray.MDDoubleArray;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.base.mdarray.MDIntArray;
import ch.systemsx.cisd.base.mdarray.MDLongArray;
import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.HDF5BaseReader.DataSpaceParameters;
import ch.systemsx.cisd.hdf5.cleanup.ICallableWithCleanUp;
import ch.systemsx.cisd.hdf5.cleanup.ICleanUpRegistry;

/**
 * A class for reading HDF5 files (HDF5 1.8.x and older).
 * <p>
 * The class focuses on ease of use instead of completeness. As a consequence not all features of a
 * valid HDF5 files can be read using this class, but only a subset. (All information written by
 * {@link HDF5Writer} can be read by this class.)
 * <p>
 * Usage:
 * 
 * <pre>
 * HDF5Reader reader = new HDF5ReaderConfig(&quot;test.h5&quot;).reader();
 * float[] f = reader.readFloatArray(&quot;/some/path/dataset&quot;);
 * String s = reader.getAttributeString(&quot;/some/path/dataset&quot;, &quot;some key&quot;);
 * reader.close();
 * </pre>
 * 
 * @author Bernd Rinn
 */
class HDF5Reader implements IHDF5Reader
{
    private static final int MIN_ENUM_SIZE_FOR_UPFRONT_LOADING = 10;

    private final HDF5BaseReader baseReader;

    private final IHDF5ByteReader byteReader;

    private final IHDF5ShortReader shortReader;

    private final IHDF5IntReader intReader;

    private final IHDF5LongReader longReader;

    private final IHDF5FloatReader floatReader;

    private final IHDF5DoubleReader doubleReader;
    
    private final IHDF5CompoundReader compoundReader;

    HDF5Reader(HDF5BaseReader baseReader)
    {
        assert baseReader != null;

        this.baseReader = baseReader;
        this.byteReader = new HDF5ByteReader(baseReader);
        this.shortReader = new HDF5ShortReader(baseReader);
        this.intReader = new HDF5IntReader(baseReader);
        this.longReader = new HDF5LongReader(baseReader);
        this.floatReader = new HDF5FloatReader(baseReader);
        this.doubleReader = new HDF5DoubleReader(baseReader);
        this.compoundReader = new HDF5CompoundReader(baseReader);
    }

    void checkOpen()
    {
        baseReader.checkOpen();
    }

    int getFileId()
    {
        return baseReader.fileId;
    }

    // /////////////////////
    // Configuration
    // /////////////////////

    public boolean isPerformNumericConversions()
    {
        return baseReader.performNumericConversions;
    }

    public File getFile()
    {
        return baseReader.hdf5File;
    }

    public void close()
    {
        baseReader.close();
    }

    // /////////////////////
    // Objects & Links
    // /////////////////////

    public HDF5LinkInformation getLinkInformation(final String objectPath)
    {
        baseReader.checkOpen();
        return baseReader.h5.getLinkInfo(baseReader.fileId, objectPath, false);
    }

    public HDF5ObjectInformation getObjectInformation(final String objectPath)
    {
        baseReader.checkOpen();
        return baseReader.h5.getObjectInfo(baseReader.fileId, objectPath, false);
    }

    public HDF5ObjectType getObjectType(final String objectPath, boolean followLink)
    {
        baseReader.checkOpen();
        if (followLink)
        {
            return baseReader.h5.getObjectTypeInfo(baseReader.fileId, objectPath, false);
        } else
        {
            return baseReader.h5.getLinkTypeInfo(baseReader.fileId, objectPath, false);
        }
    }

    public HDF5ObjectType getObjectType(final String objectPath)
    {
        return getObjectType(objectPath, true);
    }

    public boolean exists(final String objectPath, boolean followLink)
    {
        baseReader.checkOpen();
        if ("/".equals(objectPath))
        {
            return true;
        }
        if (followLink == false)
        {
            // Optimization
            return baseReader.h5.exists(baseReader.fileId, objectPath);
        } else
        {
            return exists(objectPath);
        }
    }

    public boolean exists(final String objectPath)
    {
        baseReader.checkOpen();
        return baseReader.h5.getObjectTypeId(baseReader.fileId, objectPath, false) >= 0;
    }

    public boolean isGroup(final String objectPath, boolean followLink)
    {
        return HDF5ObjectType.isGroup(getObjectType(objectPath, followLink));
    }

    public boolean isGroup(final String objectPath)
    {
        return HDF5ObjectType.isGroup(getObjectType(objectPath));
    }

    public boolean isDataSet(final String objectPath, boolean followLink)
    {
        return HDF5ObjectType.isDataSet(getObjectType(objectPath, followLink));
    }

    public boolean isDataSet(final String objectPath)
    {
        return HDF5ObjectType.isDataSet(getObjectType(objectPath));
    }

    public boolean isDataType(final String objectPath, boolean followLink)
    {
        return HDF5ObjectType.isDataType(getObjectType(objectPath, followLink));
    }

    public boolean isDataType(final String objectPath)
    {
        return HDF5ObjectType.isDataType(getObjectType(objectPath));
    }

    public boolean isSoftLink(final String objectPath)
    {
        return HDF5ObjectType.isSoftLink(getObjectType(objectPath, false));
    }

    public boolean isExternalLink(final String objectPath)
    {
        return HDF5ObjectType.isExternalLink(getObjectType(objectPath, false));
    }

    public boolean isSymbolicLink(final String objectPath)
    {
        return HDF5ObjectType.isSymbolicLink(getObjectType(objectPath, false));
    }

    public String tryGetSymbolicLinkTarget(final String objectPath)
    {
        return getLinkInformation(objectPath).tryGetSymbolicLinkTarget();
    }

    public String tryGetDataTypePath(final String objectPath)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<String> dataTypeNameCallable =
                new ICallableWithCleanUp<String>()
                    {
                        public String call(ICleanUpRegistry registry)
                        {
                            final int dataSetId =
                                    baseReader.h5.openDataSet(baseReader.fileId, objectPath,
                                            registry);
                            final int dataTypeId =
                                    baseReader.h5.getDataTypeForDataSet(dataSetId, registry);
                            return baseReader.h5.tryGetDataTypePath(dataTypeId);
                        }
                    };
        return baseReader.runner.call(dataTypeNameCallable);
    }

    public String tryGetDataTypePath(HDF5DataType type)
    {
        assert type != null;

        baseReader.checkOpen();
        type.check(baseReader.fileId);
        return baseReader.h5.tryGetDataTypePath(type.getStorageTypeId());
    }

    public List<String> getAttributeNames(final String objectPath)
    {
        assert objectPath != null;
        baseReader.checkOpen();
        return removeInternalNames(getAllAttributeNames(objectPath));
    }

    public List<String> getAllAttributeNames(final String objectPath)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<List<String>> attributeNameReaderRunnable =
                new ICallableWithCleanUp<List<String>>()
                    {
                        public List<String> call(ICleanUpRegistry registry)
                        {
                            final int objectId =
                                    baseReader.h5.openObject(baseReader.fileId, objectPath,
                                            registry);
                            return baseReader.h5.getAttributeNames(objectId, registry);
                        }
                    };
        return baseReader.runner.call(attributeNameReaderRunnable);
    }

    public HDF5DataTypeInformation getAttributeInformation(final String dataSetPath,
            final String attributeName)
    {
        assert dataSetPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<HDF5DataTypeInformation> informationDeterminationRunnable =
                new ICallableWithCleanUp<HDF5DataTypeInformation>()
                    {
                        public HDF5DataTypeInformation call(ICleanUpRegistry registry)
                        {
                            try
                            {
                                final int objectId =
                                        baseReader.h5.openObject(baseReader.fileId, dataSetPath,
                                                registry);
                                final int attributeId =
                                        baseReader.h5.openAttribute(objectId, attributeName,
                                                registry);
                                final int dataTypeId =
                                        baseReader.h5
                                                .getDataTypeForAttribute(attributeId, registry);
                                final HDF5DataTypeInformation dataTypeInformation =
                                        baseReader.getDataTypeInformation(dataTypeId);
                                if (dataTypeInformation.isArrayType() == false)
                                {
                                    final int[] dimensions =
                                            MDArray.toInt(baseReader.h5
                                                    .getDataDimensionsForAttribute(attributeId,
                                                            registry));
                                    if (dimensions.length > 0)
                                    {
                                        dataTypeInformation.setDimensions(dimensions);
                                    }
                                }
                                return dataTypeInformation;
                            } catch (RuntimeException ex)
                            {
                                throw ex;
                            }
                        }
                    };
        return baseReader.runner.call(informationDeterminationRunnable);
    }

    public HDF5DataSetInformation getDataSetInformation(final String dataSetPath)
    {
        assert dataSetPath != null;

        baseReader.checkOpen();
        return baseReader.getDataSetInformation(dataSetPath);
    }

    public long getSize(final String objectPath)
    {
        return getDataSetInformation(objectPath).getSize();
    }

    public long getNumberOfElements(final String objectPath)
    {
        return getDataSetInformation(objectPath).getNumberOfElements();
    }

    // /////////////////////
    // Copies
    // /////////////////////

    public void copy(final String sourceObject, final IHDF5Writer destinationWriter,
            final String destinationObject)
    {
        baseReader.checkOpen();
        final HDF5Writer dwriter = (HDF5Writer) destinationWriter;
        if (dwriter != this)
        {
            dwriter.checkOpen();
        }
        baseReader.copyObject(sourceObject, dwriter.getFileId(), destinationObject);
    }

    public void copy(String sourceObject, IHDF5Writer destinationWriter)
    {
        copy(sourceObject, destinationWriter, "/");
    }

    public void copyAll(IHDF5Writer destinationWriter)
    {
        copy("/", destinationWriter, "/");
    }

    // /////////////////////
    // Group
    // /////////////////////

    public List<String> getGroupMembers(final String groupPath)
    {
        assert groupPath != null;

        baseReader.checkOpen();
        return baseReader.getGroupMembers(groupPath);
    }

    public List<String> getAllGroupMembers(final String groupPath)
    {
        assert groupPath != null;

        baseReader.checkOpen();
        return baseReader.getAllGroupMembers(groupPath);
    }

    public List<String> getGroupMemberPaths(final String groupPath)
    {
        assert groupPath != null;

        baseReader.checkOpen();
        return baseReader.getGroupMemberPaths(groupPath);
    }

    public List<HDF5LinkInformation> getGroupMemberInformation(final String groupPath,
            boolean readLinkTargets)
    {
        baseReader.checkOpen();
        if (readLinkTargets)
        {
            return baseReader.h5.getGroupMemberLinkInfo(baseReader.fileId, groupPath, false);
        } else
        {
            return baseReader.h5.getGroupMemberTypeInfo(baseReader.fileId, groupPath, false);
        }
    }

    public List<HDF5LinkInformation> getAllGroupMemberInformation(final String groupPath,
            boolean readLinkTargets)
    {
        baseReader.checkOpen();
        if (readLinkTargets)
        {
            return baseReader.h5.getGroupMemberLinkInfo(baseReader.fileId, groupPath, true);
        } else
        {
            return baseReader.h5.getGroupMemberTypeInfo(baseReader.fileId, groupPath, true);
        }
    }

    // /////////////////////
    // Types
    // /////////////////////

    public String tryGetOpaqueTag(final String objectPath)
    {
        baseReader.checkOpen();
        final ICallableWithCleanUp<String> readTagCallable = new ICallableWithCleanUp<String>()
            {
                public String call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final int dataTypeId = baseReader.h5.getDataTypeForDataSet(dataSetId, registry);
                    return baseReader.h5.tryGetOpaqueTag(dataTypeId);
                }
            };
        return baseReader.runner.call(readTagCallable);
    }

    public HDF5OpaqueType tryGetOpaqueType(final String objectPath)
    {
        baseReader.checkOpen();
        final ICallableWithCleanUp<HDF5OpaqueType> readTagCallable =
                new ICallableWithCleanUp<HDF5OpaqueType>()
                    {
                        public HDF5OpaqueType call(ICleanUpRegistry registry)
                        {
                            final int dataSetId =
                                    baseReader.h5.openDataSet(baseReader.fileId, objectPath,
                                            registry);
                            final int dataTypeId =
                                    baseReader.h5.getDataTypeForDataSet(dataSetId,
                                            baseReader.fileRegistry);
                            final String opaqueTagOrNull =
                                    baseReader.h5.tryGetOpaqueTag(dataTypeId);
                            if (opaqueTagOrNull == null)
                            {
                                return null;
                            } else
                            {
                                return new HDF5OpaqueType(baseReader.fileId, dataTypeId,
                                        opaqueTagOrNull);
                            }
                        }
                    };
        return baseReader.runner.call(readTagCallable);
    }

    public HDF5EnumerationType getEnumType(final String name)
    {
        baseReader.checkOpen();
        final String dataTypePath = createDataTypePath(ENUM_PREFIX, name);
        final int storageDataTypeId = baseReader.getDataTypeId(dataTypePath);
        return baseReader.getEnumTypeForStorageDataType(storageDataTypeId, baseReader.fileRegistry);
    }

    public HDF5EnumerationType getEnumType(final String name, final String[] values)
            throws HDF5JavaException
    {
        return getEnumType(name, values, true);
    }

    public HDF5EnumerationType getEnumType(final String name, final String[] values,
            final boolean check) throws HDF5JavaException
    {
        baseReader.checkOpen();
        final HDF5EnumerationType dataType = getEnumType(name);
        if (check)
        {
            checkEnumValues(dataType.getStorageTypeId(), values, name);
        }
        return dataType;
    }

    protected void checkEnumValues(int dataTypeId, final String[] values, final String nameOrNull)
    {
        final String[] valuesStored = baseReader.h5.getNamesForEnumOrCompoundMembers(dataTypeId);
        if (valuesStored.length != values.length)
        {
            throw new IllegalStateException("Enum "
                    + getCompoundDataTypeName(nameOrNull, dataTypeId) + " has "
                    + valuesStored.length + " members, but should have " + values.length);
        }
        for (int i = 0; i < values.length; ++i)
        {
            if (values[i].equals(valuesStored[i]) == false)
            {
                throw new HDF5JavaException("Enum member index " + i + " of enum "
                        + getCompoundDataTypeName(nameOrNull, dataTypeId) + " is '"
                        + valuesStored[i] + "', but should be " + values[i]);
            }
        }
    }

    private String getCompoundDataTypeName(final String nameOrNull, final int dataTypeId)
    {
        if (nameOrNull != null)
        {
            return nameOrNull;
        } else
        {
            final String path = baseReader.h5.tryGetDataTypePath(dataTypeId);
            if (path == null)
            {
                return "UNKNOWN";
            } else
            {
                return path.substring(HDF5Utils.createDataTypePath(HDF5Utils.COMPOUND_PREFIX)
                        .length());
            }
        }
    }

    public HDF5EnumerationType getEnumTypeForObject(final String dataSetPath)
    {
        baseReader.checkOpen();
        final ICallableWithCleanUp<HDF5EnumerationType> readEnumTypeCallable =
                new ICallableWithCleanUp<HDF5EnumerationType>()
                    {
                        public HDF5EnumerationType call(ICleanUpRegistry registry)
                        {
                            final int dataSetId =
                                    baseReader.h5.openDataSet(baseReader.fileId, dataSetPath,
                                            registry);
                            return getEnumTypeForDataSetId(dataSetId, dataSetPath, isScaledEnum(
                                    dataSetId, registry), registry);
                        }
                    };
        return baseReader.runner.call(readEnumTypeCallable);
    }

    private HDF5EnumerationType getEnumTypeForDataSetId(final int objectId,
            final String objectName, final boolean scaledEnum, final ICleanUpRegistry registry)
    {
        if (scaledEnum)
        {
            final String enumTypeName =
                    getStringAttribute(objectId, objectName, HDF5Utils.ENUM_TYPE_NAME_ATTRIBUTE,
                            registry);
            return getEnumType(enumTypeName);
        } else
        {
            final int storageDataTypeId =
                    baseReader.h5.getDataTypeForDataSet(objectId, baseReader.fileRegistry);
            return baseReader.getEnumTypeForStorageDataType(storageDataTypeId, baseReader.fileRegistry);
        }
    }

    protected boolean isScaledEnum(final int objectId, final ICleanUpRegistry registry)
    {
        final HDF5DataTypeVariant typeVariantOrNull =
                baseReader.tryGetTypeVariant(objectId, registry);
        return (HDF5DataTypeVariant.ENUM == typeVariantOrNull);

    }

    public HDF5DataTypeVariant tryGetTypeVariant(final String objectPath)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<HDF5DataTypeVariant> readRunnable =
                new ICallableWithCleanUp<HDF5DataTypeVariant>()
                    {
                        public HDF5DataTypeVariant call(ICleanUpRegistry registry)
                        {
                            final int objectId =
                                    baseReader.h5.openObject(baseReader.fileId, objectPath,
                                            registry);
                            return baseReader.tryGetTypeVariant(objectId, registry);
                        }
                    };

        return baseReader.runner.call(readRunnable);
    }

    // /////////////////////
    // Attributes
    // /////////////////////

    public boolean hasAttribute(final String objectPath, final String attributeName)
    {
        assert objectPath != null;
        assert attributeName != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<Boolean> writeRunnable = new ICallableWithCleanUp<Boolean>()
            {
                public Boolean call(ICleanUpRegistry registry)
                {
                    final int objectId =
                            baseReader.h5.openObject(baseReader.fileId, objectPath, registry);
                    return baseReader.h5.existsAttribute(objectId, attributeName);
                }
            };
        return baseReader.runner.call(writeRunnable);
    }

    public String getStringAttribute(final String objectPath, final String attributeName)
    {
        assert objectPath != null;
        assert attributeName != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<String> readRunnable = new ICallableWithCleanUp<String>()
            {
                public String call(ICleanUpRegistry registry)
                {
                    final int objectId =
                            baseReader.h5.openObject(baseReader.fileId, objectPath, registry);
                    return getStringAttribute(objectId, objectPath, attributeName, registry);
                }
            };
        return baseReader.runner.call(readRunnable);
    }

    private String getStringAttribute(final int objectId, final String objectPath,
            final String attributeName, final ICleanUpRegistry registry)
    {
        final int attributeId = baseReader.h5.openAttribute(objectId, attributeName, registry);
        final int dataTypeId = baseReader.h5.getDataTypeForAttribute(attributeId, registry);
        final boolean isString = (baseReader.h5.getClassType(dataTypeId) == H5T_STRING);
        if (isString == false)
        {
            throw new IllegalArgumentException("Attribute " + attributeName + " of object "
                    + objectPath + " needs to be a String.");
        }
        final int size = baseReader.h5.getDataTypeSize(dataTypeId);
        final int stringDataTypeId = baseReader.h5.getDataTypeForAttribute(attributeId, registry);
        if (baseReader.h5.isVariableLengthString(stringDataTypeId))
        {
            String[] data = new String[1];
            baseReader.h5.readAttributeVL(attributeId, stringDataTypeId, data);
            return data[0];
        } else
        {
            byte[] data =
                    baseReader.h5.readAttributeAsByteArray(attributeId, stringDataTypeId, size);
            int termIdx;
            for (termIdx = 0; termIdx < size && data[termIdx] != 0; ++termIdx)
            {
            }
            return new String(data, 0, termIdx);
        }
    }

    public boolean getBooleanAttribute(final String objectPath, final String attributeName)
            throws HDF5JavaException
    {
        assert objectPath != null;
        assert attributeName != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<Boolean> writeRunnable = new ICallableWithCleanUp<Boolean>()
            {
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

    public String getEnumAttributeAsString(final String objectPath, final String attributeName)
            throws HDF5JavaException
    {
        assert objectPath != null;
        assert attributeName != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<String> readRunnable = new ICallableWithCleanUp<String>()
            {
                public String call(ICleanUpRegistry registry)
                {
                    final int objectId =
                            baseReader.h5.openObject(baseReader.fileId, objectPath, registry);
                    final int attributeId =
                            baseReader.h5.openAttribute(objectId, attributeName, registry);
                    final int storageDataTypeId =
                            baseReader.h5.getDataTypeForAttribute(attributeId, registry);
                    final int nativeDataTypeId =
                            baseReader.h5.getNativeDataType(storageDataTypeId, registry);
                    final byte[] data =
                            baseReader.h5
                                    .readAttributeAsByteArray(attributeId, nativeDataTypeId, 4);
                    final String value =
                            baseReader.h5.getNameForEnumOrCompoundMemberIndex(storageDataTypeId,
                                    HDFNativeData.byteToInt(data, 0));
                    if (value == null)
                    {
                        throw new HDF5JavaException("Attribute " + attributeName + " of path "
                                + objectPath + " needs to be an Enumeration.");
                    }
                    return value;
                }
            };
        return baseReader.runner.call(readRunnable);
    }

    public HDF5EnumerationValue getEnumAttribute(final String objectPath, final String attributeName)
            throws HDF5JavaException
    {
        assert objectPath != null;
        assert attributeName != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<HDF5EnumerationValue> readRunnable =
                new ICallableWithCleanUp<HDF5EnumerationValue>()
                    {
                        public HDF5EnumerationValue call(ICleanUpRegistry registry)
                        {
                            final int objectId =
                                    baseReader.h5.openObject(baseReader.fileId, objectPath,
                                            registry);
                            final int attributeId =
                                    baseReader.h5.openAttribute(objectId, attributeName, registry);
                            final HDF5EnumerationType enumType =
                                    getEnumTypeForAttributeId(attributeId);
                            final int enumOrdinal =
                                    baseReader.getEnumOrdinal(attributeId, enumType);
                            return new HDF5EnumerationValue(enumType, enumOrdinal);
                        }
                    };

        return baseReader.runner.call(readRunnable);
    }

    private HDF5EnumerationType getEnumTypeForAttributeId(final int objectId)
    {
        final int storageDataTypeId =
                baseReader.h5.getDataTypeForAttribute(objectId, baseReader.fileRegistry);
        return baseReader.getEnumTypeForStorageDataType(storageDataTypeId, baseReader.fileRegistry);
    }

    // /////////////////////
    // Data Sets
    // /////////////////////

    //
    // Generic
    //

    public byte[] readAsByteArray(final String objectPath)
    {
        baseReader.checkOpen();
        final ICallableWithCleanUp<byte[]> readCallable = new ICallableWithCleanUp<byte[]>()
            {
                public byte[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, registry);
                    final int nativeDataTypeId =
                            baseReader.h5.getNativeDataTypeForDataSetCheckBitFields(dataSetId,
                                    registry);
                    final int elementSize = baseReader.h5.getDataTypeSize(nativeDataTypeId);
                    final byte[] data =
                            new byte[((spaceParams.blockSize == 0) ? 1 : spaceParams.blockSize)
                                    * elementSize];
                    baseReader.h5.readDataSet(dataSetId, nativeDataTypeId,
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId, data);
                    return data;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public byte[] readAsByteArrayBlock(final String objectPath, final int blockSize,
            final long blockNumber) throws HDF5JavaException
    {
        baseReader.checkOpen();
        final ICallableWithCleanUp<byte[]> readCallable = new ICallableWithCleanUp<byte[]>()
            {
                public byte[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, blockNumber * blockSize,
                                    blockSize, registry);
                    final int nativeDataTypeId =
                            baseReader.h5.getNativeDataTypeForDataSet(dataSetId, registry);
                    final int elementSize = baseReader.h5.getDataTypeSize(nativeDataTypeId);
                    final byte[] data = new byte[elementSize * spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, nativeDataTypeId,
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId, data);
                    return data;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public byte[] readAsByteArrayBlockWithOffset(final String objectPath, final int blockSize,
            final long offset) throws HDF5JavaException
    {
        baseReader.checkOpen();
        final ICallableWithCleanUp<byte[]> readCallable = new ICallableWithCleanUp<byte[]>()
            {
                public byte[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, offset, blockSize, registry);
                    final int nativeDataTypeId =
                            baseReader.h5.getNativeDataTypeForDataSet(dataSetId, registry);
                    final int elementSize = baseReader.h5.getDataTypeSize(nativeDataTypeId);
                    final byte[] data = new byte[elementSize * spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, nativeDataTypeId,
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId, data);
                    return data;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public int readAsByteArrayToBlockWithOffset(final String objectPath, final byte[] buffer,
            final int blockSize, final long offset, final int memoryOffset)
            throws HDF5JavaException
    {
        if (blockSize + memoryOffset > buffer.length)
        {
            throw new HDF5JavaException("Buffer not large enough for blockSize and memoryOffset");
        }
        baseReader.checkOpen();
        final ICallableWithCleanUp<Integer> readCallable = new ICallableWithCleanUp<Integer>()
            {
                public Integer call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, memoryOffset, offset,
                                    blockSize, registry);
                    final int nativeDataTypeId =
                            baseReader.h5.getNativeDataTypeForDataSet(dataSetId, registry);
                    final int elementSize = baseReader.h5.getDataTypeSize(nativeDataTypeId);
                    if ((blockSize + memoryOffset) * elementSize > buffer.length)
                    {
                        throw new HDF5JavaException("Buffer not large enough for blockSize and memoryOffset");
                    }
                    baseReader.h5.readDataSet(dataSetId, nativeDataTypeId,
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId, buffer);
                    return spaceParams.blockSize;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public Iterable<HDF5DataBlock<byte[]>> getAsByteArrayNaturalBlocks(final String dataSetPath)
            throws HDF5JavaException
    {
        final HDF5DataSetInformation info = getDataSetInformation(dataSetPath);
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
                (info.getStorageLayout() == HDF5StorageLayout.CHUNKED) ? info.tryGetChunkSizes()[0]
                        : size;
        final int sizeModNaturalBlockSize = size % naturalBlockSize;
        final long numberOfBlocks =
                (size / naturalBlockSize) + (sizeModNaturalBlockSize != 0 ? 1 : 0);
        final int lastBlockSize =
                (sizeModNaturalBlockSize != 0) ? sizeModNaturalBlockSize : naturalBlockSize;

        return new Iterable<HDF5DataBlock<byte[]>>()
            {
                public Iterator<HDF5DataBlock<byte[]>> iterator()
                {
                    return new Iterator<HDF5DataBlock<byte[]>>()
                        {
                            long index = 0;

                            public boolean hasNext()
                            {
                                return index < numberOfBlocks;
                            }

                            public HDF5DataBlock<byte[]> next()
                            {
                                if (hasNext() == false)
                                {
                                    throw new NoSuchElementException();
                                }
                                final long offset = naturalBlockSize * index;
                                final int blockSize =
                                        (index == numberOfBlocks - 1) ? lastBlockSize
                                                : naturalBlockSize;
                                final byte[] block =
                                        readAsByteArrayBlockWithOffset(dataSetPath, blockSize,
                                                offset);
                                return new HDF5DataBlock<byte[]>(block, index++, offset);
                            }

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    //
    // Boolean
    //

    public boolean readBoolean(final String objectPath) throws HDF5JavaException
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<Boolean> writeRunnable = new ICallableWithCleanUp<Boolean>()
            {
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

    //
    // Time stamp
    //

    public boolean isTimeStamp(final String objectPath) throws HDF5JavaException
    {
        final HDF5DataTypeVariant typeVariantOrNull = tryGetTypeVariant(objectPath);
        return typeVariantOrNull != null && typeVariantOrNull.isTimeStamp();
    }

    public long readTimeStamp(final String objectPath) throws HDF5JavaException
    {
        baseReader.checkOpen();
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<Long> readCallable = new ICallableWithCleanUp<Long>()
            {
                public Long call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    checkIsTimeStamp(objectPath, dataSetId, registry);
                    final long[] data = new long[1];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT64, data);
                    return data[0];
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public long[] readTimeStampArray(final String objectPath) throws HDF5JavaException
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<long[]> readCallable = new ICallableWithCleanUp<long[]>()
            {
                public long[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    checkIsTimeStamp(objectPath, dataSetId, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, registry);
                    final long[] data = new long[spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT64,
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId, data);
                    return data;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public long[] readTimeStampArrayBlock(final String objectPath, final int blockSize,
            final long blockNumber)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<long[]> readCallable = new ICallableWithCleanUp<long[]>()
            {
                public long[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    checkIsTimeStamp(objectPath, dataSetId, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, blockNumber * blockSize,
                                    blockSize, registry);
                    final long[] data = new long[spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT64,
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId, data);
                    return data;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public long[] readTimeStampArrayBlockWithOffset(final String objectPath, final int blockSize,
            final long offset)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<long[]> readCallable = new ICallableWithCleanUp<long[]>()
            {
                public long[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    checkIsTimeStamp(objectPath, dataSetId, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, offset, blockSize, registry);
                    final long[] data = new long[spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT64,
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId, data);
                    return data;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public Iterable<HDF5DataBlock<long[]>> getTimeStampArrayNaturalBlocks(final String dataSetPath)
            throws HDF5JavaException
    {
        final HDF5DataSetInformation info = getDataSetInformation(dataSetPath);
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
                (info.getStorageLayout() == HDF5StorageLayout.CHUNKED) ? info.tryGetChunkSizes()[0]
                        : size;
        final int sizeModNaturalBlockSize = size % naturalBlockSize;
        final long numberOfBlocks =
                (size / naturalBlockSize) + (sizeModNaturalBlockSize != 0 ? 1 : 0);
        final int lastBlockSize =
                (sizeModNaturalBlockSize != 0) ? sizeModNaturalBlockSize : naturalBlockSize;

        return new Iterable<HDF5DataBlock<long[]>>()
            {
                public Iterator<HDF5DataBlock<long[]>> iterator()
                {
                    return new Iterator<HDF5DataBlock<long[]>>()
                        {
                            long index = 0;

                            public boolean hasNext()
                            {
                                return index < numberOfBlocks;
                            }

                            public HDF5DataBlock<long[]> next()
                            {
                                if (hasNext() == false)
                                {
                                    throw new NoSuchElementException();
                                }
                                final long offset = naturalBlockSize * index;
                                final int blockSize =
                                        (index == numberOfBlocks - 1) ? lastBlockSize
                                                : naturalBlockSize;
                                final long[] block =
                                        readTimeStampArrayBlockWithOffset(dataSetPath, blockSize,
                                                offset);
                                return new HDF5DataBlock<long[]>(block, index++, offset);
                            }

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    public Date readDate(final String objectPath) throws HDF5JavaException
    {
        return new Date(readTimeStamp(objectPath));
    }

    public Date[] readDateArray(final String objectPath) throws HDF5JavaException
    {
        final long[] timeStampArray = readTimeStampArray(objectPath);
        return timeStampsToDates(timeStampArray);
    }

    private static Date[] timeStampsToDates(final long[] timeStampArray)
    {
        assert timeStampArray != null;

        final Date[] dateArray = new Date[timeStampArray.length];
        for (int i = 0; i < dateArray.length; ++i)
        {
            dateArray[i] = new Date(timeStampArray[i]);
        }
        return dateArray;
    }

    protected void checkIsTimeStamp(final String objectPath, final int dataSetId,
            ICleanUpRegistry registry) throws HDF5JavaException
    {
        final int typeVariantOrdinal = baseReader.getAttributeTypeVariant(dataSetId, registry);
        if (typeVariantOrdinal != HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH
                .ordinal())
        {
            throw new HDF5JavaException("Data set '" + objectPath + "' is not a time stamp.");
        }
    }

    //
    // Duration
    //

    public boolean isTimeDuration(final String objectPath) throws HDF5JavaException
    {
        final HDF5DataTypeVariant typeVariantOrNull = tryGetTypeVariant(objectPath);
        return typeVariantOrNull != null && typeVariantOrNull.isTimeDuration();
    }

    public HDF5TimeUnit tryGetTimeUnit(final String objectPath) throws HDF5JavaException
    {
        final HDF5DataTypeVariant typeVariantOrNull = tryGetTypeVariant(objectPath);
        return (typeVariantOrNull != null) ? typeVariantOrNull.tryGetTimeUnit() : null;
    }

    public long readTimeDuration(final String objectPath) throws HDF5JavaException
    {
        return readTimeDuration(objectPath, HDF5TimeUnit.SECONDS);
    }

    public long readTimeDuration(final String objectPath, final HDF5TimeUnit timeUnit)
            throws HDF5JavaException
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<Long> readCallable = new ICallableWithCleanUp<Long>()
            {
                public Long call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final HDF5TimeUnit storedUnit =
                            checkIsTimeDuration(objectPath, dataSetId, registry);
                    final long[] data = new long[1];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT64, data);
                    return timeUnit.convert(data[0], storedUnit);
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public long[] readTimeDurationArray(final String objectPath) throws HDF5JavaException
    {
        return readTimeDurationArray(objectPath, HDF5TimeUnit.SECONDS);
    }

    public long[] readTimeDurationArray(final String objectPath, final HDF5TimeUnit timeUnit)
            throws HDF5JavaException
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<long[]> readCallable = new ICallableWithCleanUp<long[]>()
            {
                public long[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final HDF5TimeUnit storedUnit =
                            checkIsTimeDuration(objectPath, dataSetId, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, registry);
                    final long[] data = new long[spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT64,
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId, data);
                    convertTimeDurations(timeUnit, storedUnit, data);
                    return data;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public long[] readTimeDurationArrayBlock(final String objectPath, final int blockSize,
            final long blockNumber, final HDF5TimeUnit timeUnit)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<long[]> readCallable = new ICallableWithCleanUp<long[]>()
            {
                public long[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final HDF5TimeUnit storedUnit =
                            checkIsTimeDuration(objectPath, dataSetId, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, blockNumber * blockSize,
                                    blockSize, registry);
                    final long[] data = new long[spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT64,
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId, data);
                    convertTimeDurations(timeUnit, storedUnit, data);
                    return data;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public long[] readTimeDurationArrayBlockWithOffset(final String objectPath,
            final int blockSize, final long offset, final HDF5TimeUnit timeUnit)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<long[]> readCallable = new ICallableWithCleanUp<long[]>()
            {
                public long[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final HDF5TimeUnit storedUnit =
                            checkIsTimeDuration(objectPath, dataSetId, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, offset, blockSize, registry);
                    final long[] data = new long[spaceParams.blockSize];
                    baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT64,
                            spaceParams.memorySpaceId, spaceParams.dataSpaceId, data);
                    convertTimeDurations(timeUnit, storedUnit, data);
                    return data;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public Iterable<HDF5DataBlock<long[]>> getTimeDurationArrayNaturalBlocks(
            final String dataSetPath, final HDF5TimeUnit timeUnit) throws HDF5JavaException
    {
        final HDF5DataSetInformation info = getDataSetInformation(dataSetPath);
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
                (info.getStorageLayout() == HDF5StorageLayout.CHUNKED) ? info.tryGetChunkSizes()[0]
                        : size;
        final int sizeModNaturalBlockSize = size % naturalBlockSize;
        final long numberOfBlocks =
                (size / naturalBlockSize) + (sizeModNaturalBlockSize != 0 ? 1 : 0);
        final int lastBlockSize =
                (sizeModNaturalBlockSize != 0) ? sizeModNaturalBlockSize : naturalBlockSize;

        return new Iterable<HDF5DataBlock<long[]>>()
            {
                public Iterator<HDF5DataBlock<long[]>> iterator()
                {
                    return new Iterator<HDF5DataBlock<long[]>>()
                        {
                            long index = 0;

                            public boolean hasNext()
                            {
                                return index < numberOfBlocks;
                            }

                            public HDF5DataBlock<long[]> next()
                            {
                                if (hasNext() == false)
                                {
                                    throw new NoSuchElementException();
                                }
                                final long offset = naturalBlockSize * index;
                                final int blockSize =
                                        (index == numberOfBlocks - 1) ? lastBlockSize
                                                : naturalBlockSize;
                                final long[] block =
                                        readTimeDurationArrayBlockWithOffset(dataSetPath,
                                                blockSize, offset, timeUnit);
                                return new HDF5DataBlock<long[]>(block, index++, offset);
                            }

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    protected HDF5TimeUnit checkIsTimeDuration(final String objectPath, final int dataSetId,
            ICleanUpRegistry registry) throws HDF5JavaException
    {
        final int typeVariantOrdinal = baseReader.getAttributeTypeVariant(dataSetId, registry);
        if (HDF5DataTypeVariant.isTimeDuration(typeVariantOrdinal) == false)
        {
            throw new HDF5JavaException("Data set '" + objectPath + "' is not a time duration.");
        }
        return HDF5DataTypeVariant.getTimeUnit(typeVariantOrdinal);
    }

    static void convertTimeDurations(final HDF5TimeUnit timeUnit, final HDF5TimeUnit storedUnit,
            final long[] data)
    {
        if (timeUnit != storedUnit)
        {
            for (int i = 0; i < data.length; ++i)
            {
                data[i] = timeUnit.convert(data[i], storedUnit);
            }
        }
    }

    //
    // String
    //

    public String readString(final String objectPath) throws HDF5JavaException
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<String> writeRunnable = new ICallableWithCleanUp<String>()
            {
                public String call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final int dataTypeId =
                            baseReader.h5.getNativeDataTypeForDataSet(dataSetId, registry);
                    final boolean isString = (baseReader.h5.getClassType(dataTypeId) == H5T_STRING);
                    if (isString == false)
                    {
                        throw new HDF5JavaException(objectPath + " needs to be a String.");
                    }
                    if (baseReader.h5.isVariableLengthString(dataTypeId))
                    {
                        String[] data = new String[1];
                        baseReader.h5.readDataSetVL(dataSetId, dataTypeId, data);
                        return data[0];
                    } else
                    {
                        final int size = baseReader.h5.getDataTypeSize(dataTypeId);
                        byte[] data = new byte[size];
                        baseReader.h5.readDataSetNonNumeric(dataSetId, dataTypeId, data);
                        int termIdx;
                        for (termIdx = 0; termIdx < size && data[termIdx] != 0; ++termIdx)
                        {
                        }
                        return new String(data, 0, termIdx);
                    }
                }
            };
        return baseReader.runner.call(writeRunnable);
    }

    public String[] readStringArray(final String objectPath) throws HDF5JavaException
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<String[]> writeRunnable = new ICallableWithCleanUp<String[]>()
            {
                public String[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final long[] dimensions = baseReader.h5.getDataDimensions(dataSetId);
                    final String[] data = new String[getOneDimensionalArraySize(dimensions)];
                    final int dataTypeId =
                            baseReader.h5.getNativeDataTypeForDataSet(dataSetId, registry);
                    if (baseReader.h5.isVariableLengthString(dataTypeId))
                    {
                        baseReader.h5.readDataSetVL(dataSetId, dataTypeId, data);
                    } else
                    {
                        final boolean isString =
                                (baseReader.h5.getClassType(dataTypeId) == H5T_STRING);
                        if (isString == false)
                        {
                            throw new HDF5JavaException(objectPath + " needs to be a String.");
                        }
                        baseReader.h5.readDataSetString(dataSetId, dataTypeId, data);
                    }
                    return data;
                }
            };
        return baseReader.runner.call(writeRunnable);
    }

    public String[] readStringArrayBlock(final String objectPath, final int blockSize,
            final long blockNumber)
    {
        return readStringArrayBlockWithOffset(objectPath, blockSize, blockSize * blockNumber);
    }

    public String[] readStringArrayBlockWithOffset(final String objectPath, final int blockSize,
            final long offset)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<String[]> readCallable = new ICallableWithCleanUp<String[]>()
            {
                public String[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final DataSpaceParameters spaceParams =
                            baseReader.getSpaceParameters(dataSetId, offset, blockSize, registry);
                    final String[] data = new String[spaceParams.blockSize];
                    final int dataTypeId =
                            baseReader.h5.getNativeDataTypeForDataSet(dataSetId, registry);
                    if (baseReader.h5.isVariableLengthString(dataTypeId))
                    {
                        baseReader.h5.readDataSetVL(dataSetId, dataTypeId,
                                spaceParams.memorySpaceId, spaceParams.dataSpaceId, data);
                    } else
                    {
                        final boolean isString =
                                (baseReader.h5.getClassType(dataTypeId) == H5T_STRING);
                        if (isString == false)
                        {
                            throw new HDF5JavaException(objectPath + " needs to be a String.");
                        }
                        baseReader.h5.readDataSetString(dataSetId, dataTypeId,
                                spaceParams.memorySpaceId, spaceParams.dataSpaceId, data);
                    }
                    return data;
                }
            };
        return baseReader.runner.call(readCallable);
    }

    public Iterable<HDF5DataBlock<String[]>> getStringArrayNaturalBlocks(final String dataSetPath)
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
                (info.getStorageLayout() == HDF5StorageLayout.CHUNKED) ? info.tryGetChunkSizes()[0]
                        : size;
        final int sizeModNaturalBlockSize = size % naturalBlockSize;
        final long numberOfBlocks =
                (size / naturalBlockSize) + (sizeModNaturalBlockSize != 0 ? 1 : 0);
        final int lastBlockSize =
                (sizeModNaturalBlockSize != 0) ? sizeModNaturalBlockSize : naturalBlockSize;

        return new Iterable<HDF5DataBlock<String[]>>()
            {
                public Iterator<HDF5DataBlock<String[]>> iterator()
                {
                    return new Iterator<HDF5DataBlock<String[]>>()
                        {
                            long index = 0;

                            public boolean hasNext()
                            {
                                return index < numberOfBlocks;
                            }

                            public HDF5DataBlock<String[]> next()
                            {
                                if (hasNext() == false)
                                {
                                    throw new NoSuchElementException();
                                }
                                final long offset = naturalBlockSize * index;
                                final int blockSize =
                                        (index == numberOfBlocks - 1) ? lastBlockSize
                                                : naturalBlockSize;
                                final String[] block =
                                        readStringArrayBlockWithOffset(dataSetPath, blockSize,
                                                offset);
                                return new HDF5DataBlock<String[]>(block, index++, offset);
                            }

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    //
    // Enum
    //

    public String readEnumAsString(final String objectPath) throws HDF5JavaException
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<String> writeRunnable = new ICallableWithCleanUp<String>()
            {
                public String call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final int storageDataTypeId =
                            baseReader.h5.getDataTypeForDataSet(dataSetId, registry);
                    final int nativeDataTypeId =
                            baseReader.h5.getNativeDataType(storageDataTypeId, registry);
                    final int size = baseReader.h5.getDataTypeSize(nativeDataTypeId);
                    final String value;
                    switch (size)
                    {
                        case 1:
                        {
                            final byte[] data = new byte[1];
                            baseReader.h5.readDataSet(dataSetId, nativeDataTypeId, data);
                            value =
                                    baseReader.h5.getNameForEnumOrCompoundMemberIndex(
                                            storageDataTypeId, data[0]);
                            break;
                        }
                        case 2:
                        {
                            final short[] data = new short[1];
                            baseReader.h5.readDataSet(dataSetId, nativeDataTypeId, data);
                            value =
                                    baseReader.h5.getNameForEnumOrCompoundMemberIndex(
                                            storageDataTypeId, data[0]);
                            break;
                        }
                        case 4:
                        {
                            final int[] data = new int[1];
                            baseReader.h5.readDataSet(dataSetId, nativeDataTypeId, data);
                            value =
                                    baseReader.h5.getNameForEnumOrCompoundMemberIndex(
                                            storageDataTypeId, data[0]);
                            break;
                        }
                        default:
                            throw new HDF5JavaException("Unexpected size for Enum data type ("
                                    + size + ")");
                    }
                    if (value == null)
                    {
                        throw new HDF5JavaException(objectPath + " needs to be an Enumeration.");
                    }
                    return value;
                }
            };
        return baseReader.runner.call(writeRunnable);
    }

    public HDF5EnumerationValue readEnum(final String objectPath) throws HDF5JavaException
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<HDF5EnumerationValue> readRunnable =
                new ICallableWithCleanUp<HDF5EnumerationValue>()
                    {
                        public HDF5EnumerationValue call(ICleanUpRegistry registry)
                        {
                            final int dataSetId =
                                    baseReader.h5.openDataSet(baseReader.fileId, objectPath,
                                            registry);
                            final HDF5EnumerationType enumType =
                                    getEnumTypeForDataSetId(dataSetId, objectPath, false, registry);
                            return readEnumValue(dataSetId, enumType);
                        }
                    };

        return baseReader.runner.call(readRunnable);
    }

    public HDF5EnumerationValue readEnum(final String objectPath, final HDF5EnumerationType enumType)
            throws HDF5JavaException
    {
        assert objectPath != null;
        assert enumType != null;

        baseReader.checkOpen();
        enumType.check(baseReader.fileId);
        final ICallableWithCleanUp<HDF5EnumerationValue> readRunnable =
                new ICallableWithCleanUp<HDF5EnumerationValue>()
                    {
                        public HDF5EnumerationValue call(ICleanUpRegistry registry)
                        {
                            final int dataSetId =
                                    baseReader.h5.openDataSet(baseReader.fileId, objectPath,
                                            registry);
                            return readEnumValue(dataSetId, enumType);
                        }
                    };

        return baseReader.runner.call(readRunnable);
    }

    private HDF5EnumerationValue readEnumValue(final int dataSetId,
            final HDF5EnumerationType enumType)
    {
        switch (enumType.getStorageForm())
        {
            case BYTE:
            {
                final byte[] data = new byte[1];
                baseReader.h5.readDataSet(dataSetId, enumType.getNativeTypeId(), data);
                return new HDF5EnumerationValue(enumType, data[0]);
            }
            case SHORT:
            {
                final short[] data = new short[1];
                baseReader.h5.readDataSet(dataSetId, enumType.getNativeTypeId(), data);
                return new HDF5EnumerationValue(enumType, data[0]);
            }
            case INT:
            {
                final int[] data = new int[1];
                baseReader.h5.readDataSet(dataSetId, enumType.getNativeTypeId(), data);
                return new HDF5EnumerationValue(enumType, data[0]);
            }
            default:
                throw new HDF5JavaException("Illegal storage form for enum ("
                        + enumType.getStorageForm() + ")");
        }
    }

    public HDF5EnumerationValueArray readEnumArray(final String objectPath,
            final HDF5EnumerationType enumTypeOrNull) throws HDF5JavaException
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<HDF5EnumerationValueArray> readRunnable =
                new ICallableWithCleanUp<HDF5EnumerationValueArray>()
                    {
                        public HDF5EnumerationValueArray call(ICleanUpRegistry registry)
                        {
                            final int dataSetId =
                                    baseReader.h5.openDataSet(baseReader.fileId, objectPath,
                                            registry);
                            final long[] dimensions = baseReader.h5.getDataDimensions(dataSetId);
                            final boolean scaledEnum = isScaledEnum(dataSetId, registry);
                            final HDF5EnumerationType actualEnumType =
                                    (enumTypeOrNull == null) ? getEnumTypeForDataSetId(dataSetId,
                                            objectPath, scaledEnum, registry) : enumTypeOrNull;
                            final int arraySize = HDF5Utils.getOneDimensionalArraySize(dimensions);
                            switch (actualEnumType.getStorageForm())
                            {
                                case BYTE:
                                {
                                    final byte[] data = new byte[arraySize];
                                    if (scaledEnum)
                                    {
                                        baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT8, data);
                                    } else
                                    {
                                        baseReader.h5.readDataSet(dataSetId, actualEnumType
                                                .getNativeTypeId(), data);
                                    }
                                    return new HDF5EnumerationValueArray(actualEnumType, data);
                                }
                                case SHORT:
                                {
                                    final short[] data = new short[arraySize];
                                    if (scaledEnum)
                                    {
                                        baseReader.h5
                                                .readDataSet(dataSetId, H5T_NATIVE_INT16, data);
                                    } else
                                    {
                                        baseReader.h5.readDataSet(dataSetId, actualEnumType
                                                .getNativeTypeId(), data);
                                    }
                                    return new HDF5EnumerationValueArray(actualEnumType, data);
                                }
                                case INT:
                                {
                                    final int[] data = new int[arraySize];
                                    if (scaledEnum)
                                    {
                                        baseReader.h5
                                                .readDataSet(dataSetId, H5T_NATIVE_INT32, data);
                                    } else
                                    {
                                        baseReader.h5.readDataSet(dataSetId, actualEnumType
                                                .getNativeTypeId(), data);
                                    }
                                    return new HDF5EnumerationValueArray(actualEnumType, data);
                                }
                            }
                            throw new Error("Illegal storage form ("
                                    + actualEnumType.getStorageForm() + ".)");
                        }
                    };

        return baseReader.runner.call(readRunnable);
    }

    public HDF5EnumerationValueArray readEnumArray(final String objectPath)
            throws HDF5JavaException
    {
        return readEnumArray(objectPath, null);
    }

    public String[] readEnumArrayAsString(final String objectPath) throws HDF5JavaException
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<String[]> writeRunnable = new ICallableWithCleanUp<String[]>()
            {
                public String[] call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseReader.h5.openDataSet(baseReader.fileId, objectPath, registry);
                    final long[] dimensions = baseReader.h5.getDataDimensions(dataSetId);
                    final int vectorLength = getOneDimensionalArraySize(dimensions);
                    final int storageDataTypeId =
                            baseReader.h5.getDataTypeForDataSet(dataSetId, registry);
                    final int nativeDataTypeId =
                            baseReader.h5.getNativeDataType(storageDataTypeId, registry);
                    final HDF5EnumerationType enumTypeOrNull =
                            tryGetEnumTypeForResolution(dataSetId, objectPath, nativeDataTypeId,
                                    vectorLength, registry);
                    final int size = baseReader.h5.getDataTypeSize(nativeDataTypeId);

                    final String[] value = new String[vectorLength];
                    switch (size)
                    {
                        case 1:
                        {
                            final byte[] data = new byte[vectorLength];
                            baseReader.h5.readDataSet(dataSetId, nativeDataTypeId, data);
                            if (enumTypeOrNull != null)
                            {
                                for (int i = 0; i < data.length; ++i)
                                {
                                    value[i] = enumTypeOrNull.getValueArray()[data[i]];
                                }
                            } else
                            {
                                for (int i = 0; i < data.length; ++i)
                                {
                                    value[i] =
                                            baseReader.h5.getNameForEnumOrCompoundMemberIndex(
                                                    storageDataTypeId, data[i]);
                                }
                            }
                            break;
                        }
                        case 2:
                        {
                            final short[] data = new short[vectorLength];
                            baseReader.h5.readDataSet(dataSetId, nativeDataTypeId, data);
                            if (enumTypeOrNull != null)
                            {
                                for (int i = 0; i < data.length; ++i)
                                {
                                    value[i] = enumTypeOrNull.getValueArray()[data[i]];
                                }
                            } else
                            {
                                for (int i = 0; i < data.length; ++i)
                                {
                                    value[i] =
                                            baseReader.h5.getNameForEnumOrCompoundMemberIndex(
                                                    storageDataTypeId, data[i]);
                                }
                            }
                            break;
                        }
                        case 4:
                        {
                            final int[] data = new int[vectorLength];
                            baseReader.h5.readDataSet(dataSetId, nativeDataTypeId, data);
                            if (enumTypeOrNull != null)
                            {
                                for (int i = 0; i < data.length; ++i)
                                {
                                    value[i] = enumTypeOrNull.getValueArray()[data[i]];
                                }
                            } else
                            {
                                for (int i = 0; i < data.length; ++i)
                                {
                                    value[i] =
                                            baseReader.h5.getNameForEnumOrCompoundMemberIndex(
                                                    storageDataTypeId, data[i]);
                                }
                            }
                            break;
                        }
                        default:
                            throw new HDF5JavaException("Unexpected size for Enum data type ("
                                    + size + ")");
                    }
                    return value;
                }
            };
        return baseReader.runner.call(writeRunnable);
    }

    private HDF5EnumerationType tryGetEnumTypeForResolution(final int dataSetId,
            final String objectPath, final int nativeDataTypeId,
            final int numberOfEntriesToResolve, ICleanUpRegistry registry)
    {
        final boolean nativeEnum = (baseReader.h5.getClassType(nativeDataTypeId) == H5T_ENUM);
        final boolean scaledEnum = nativeEnum ? false : isScaledEnum(dataSetId, registry);
        if (nativeEnum == false && scaledEnum == false)
        {
            throw new HDF5JavaException(objectPath + " is not an enum.");
        }
        if (scaledEnum || numberOfEntriesToResolve >= MIN_ENUM_SIZE_FOR_UPFRONT_LOADING)
        {
            return getEnumTypeForDataSetId(dataSetId, objectPath, scaledEnum, registry);
        }
        return null;
    }

    public HDF5EnumerationValueArray readEnumArrayBlockWithOffset(final String objectPath,
            final HDF5EnumerationType enumTypeOrNull, final int blockSize, final long offset)
    {
        assert objectPath != null;

        baseReader.checkOpen();
        final ICallableWithCleanUp<HDF5EnumerationValueArray> readRunnable =
                new ICallableWithCleanUp<HDF5EnumerationValueArray>()
                    {
                        public HDF5EnumerationValueArray call(ICleanUpRegistry registry)
                        {
                            final int dataSetId =
                                    baseReader.h5.openDataSet(baseReader.fileId, objectPath,
                                            registry);
                            final DataSpaceParameters spaceParams =
                                    baseReader.getSpaceParameters(dataSetId, offset, blockSize,
                                            registry);
                            final boolean scaledEnum = isScaledEnum(dataSetId, registry);
                            final HDF5EnumerationType actualEnumType =
                                    (enumTypeOrNull == null) ? getEnumTypeForDataSetId(dataSetId,
                                            objectPath, scaledEnum, registry) : enumTypeOrNull;
                            switch (actualEnumType.getStorageForm())
                            {
                                case BYTE:
                                {
                                    final byte[] data = new byte[spaceParams.blockSize];
                                    if (scaledEnum)
                                    {
                                        baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT8,
                                                spaceParams.memorySpaceId, spaceParams.dataSpaceId,
                                                data);
                                    } else
                                    {
                                        baseReader.h5.readDataSet(dataSetId, actualEnumType
                                                .getNativeTypeId(), spaceParams.memorySpaceId,
                                                spaceParams.dataSpaceId, data);
                                    }
                                    return new HDF5EnumerationValueArray(actualEnumType, data);
                                }
                                case SHORT:
                                {
                                    final short[] data = new short[spaceParams.blockSize];
                                    if (scaledEnum)
                                    {
                                        baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT16,
                                                spaceParams.memorySpaceId, spaceParams.dataSpaceId,
                                                data);
                                    } else
                                    {
                                        baseReader.h5.readDataSet(dataSetId, actualEnumType
                                                .getNativeTypeId(), spaceParams.memorySpaceId,
                                                spaceParams.dataSpaceId, data);
                                    }
                                    return new HDF5EnumerationValueArray(actualEnumType, data);
                                }
                                case INT:
                                {
                                    final int[] data = new int[spaceParams.blockSize];
                                    if (scaledEnum)
                                    {
                                        baseReader.h5.readDataSet(dataSetId, H5T_NATIVE_INT32,
                                                spaceParams.memorySpaceId, spaceParams.dataSpaceId,
                                                data);
                                    } else
                                    {
                                        baseReader.h5.readDataSet(dataSetId, actualEnumType
                                                .getNativeTypeId(), spaceParams.memorySpaceId,
                                                spaceParams.dataSpaceId, data);
                                    }
                                    return new HDF5EnumerationValueArray(actualEnumType, data);
                                }
                            }
                            throw new Error("Illegal storage form ("
                                    + actualEnumType.getStorageForm() + ".)");
                        }
                    };

        return baseReader.runner.call(readRunnable);
    }

    public HDF5EnumerationValueArray readEnumArrayBlockWithOffset(final String objectPath,
            final int blockSize, final long offset)
    {
        return readEnumArrayBlockWithOffset(objectPath, null, blockSize, offset);
    }

    public HDF5EnumerationValueArray readEnumArrayBlock(final String objectPath,
            final int blockSize, final long blockNumber)
    {
        return readEnumArrayBlockWithOffset(objectPath, null, blockSize, blockNumber * blockSize);
    }

    public HDF5EnumerationValueArray readEnumArrayBlock(final String objectPath,
            final HDF5EnumerationType enumType, final int blockSize, final long blockNumber)
    {
        return readEnumArrayBlockWithOffset(objectPath, enumType, blockSize, blockNumber
                * blockSize);
    }

    public Iterable<HDF5DataBlock<HDF5EnumerationValueArray>> getEnumArrayNaturalBlocks(
            final String objectPath, final HDF5EnumerationType enumTypeOrNull)
            throws HDF5JavaException
    {
        baseReader.checkOpen();
        final HDF5DataSetInformation info = baseReader.getDataSetInformation(objectPath);
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
                (info.getStorageLayout() == HDF5StorageLayout.CHUNKED) ? info.tryGetChunkSizes()[0]
                        : size;
        final int sizeModNaturalBlockSize = size % naturalBlockSize;
        final long numberOfBlocks =
                (size / naturalBlockSize) + (sizeModNaturalBlockSize != 0 ? 1 : 0);
        final int lastBlockSize =
                (sizeModNaturalBlockSize != 0) ? sizeModNaturalBlockSize : naturalBlockSize;

        return new Iterable<HDF5DataBlock<HDF5EnumerationValueArray>>()
            {
                public Iterator<HDF5DataBlock<HDF5EnumerationValueArray>> iterator()
                {
                    return new Iterator<HDF5DataBlock<HDF5EnumerationValueArray>>()
                        {
                            long index = 0;

                            public boolean hasNext()
                            {
                                return index < numberOfBlocks;
                            }

                            public HDF5DataBlock<HDF5EnumerationValueArray> next()
                            {
                                if (hasNext() == false)
                                {
                                    throw new NoSuchElementException();
                                }
                                final long offset = naturalBlockSize * index;
                                final int blockSize =
                                        (index == numberOfBlocks - 1) ? lastBlockSize
                                                : naturalBlockSize;
                                final HDF5EnumerationValueArray block =
                                        readEnumArrayBlockWithOffset(objectPath, enumTypeOrNull,
                                                blockSize, offset);
                                return new HDF5DataBlock<HDF5EnumerationValueArray>(block, index++,
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

    public Iterable<HDF5DataBlock<HDF5EnumerationValueArray>> getEnumArrayNaturalBlocks(
            final String objectPath) throws HDF5JavaException
    {
        return getEnumArrayNaturalBlocks(objectPath, null);
    }

    //
    // Compound
    //

    public <T> Iterable<HDF5DataBlock<T[]>> getCompoundArrayNaturalBlocks(String objectPath,
            HDF5CompoundType<T> type, IByteArrayInspector inspectorOrNull) throws HDF5JavaException
    {
        return compoundReader.getCompoundArrayNaturalBlocks(objectPath, type, inspectorOrNull);
    }

    public <T> Iterable<HDF5DataBlock<T[]>> getCompoundArrayNaturalBlocks(String objectPath,
            HDF5CompoundType<T> type) throws HDF5JavaException
    {
        return compoundReader.getCompoundArrayNaturalBlocks(objectPath, type);
    }

    public HDF5CompoundMemberInformation[] getCompoundDataSetInformation(String dataSetPath,
            boolean sortAlphabetically) throws HDF5JavaException
    {
        return compoundReader.getCompoundDataSetInformation(dataSetPath, sortAlphabetically);
    }

    public HDF5CompoundMemberInformation[] getCompoundDataSetInformation(String dataSetPath)
            throws HDF5JavaException
    {
        return compoundReader.getCompoundDataSetInformation(dataSetPath);
    }

    public <T> HDF5CompoundMemberInformation[] getCompoundMemberInformation(Class<T> compoundClass)
    {
        return compoundReader.getCompoundMemberInformation(compoundClass);
    }

    public HDF5CompoundMemberInformation[] getCompoundMemberInformation(String dataTypeName)
    {
        return compoundReader.getCompoundMemberInformation(dataTypeName);
    }

    public <T> HDF5CompoundType<T> getCompoundType(Class<T> compoundType,
            HDF5CompoundMemberMapping... members)
    {
        return compoundReader.getCompoundType(compoundType, members);
    }

    public <T> HDF5CompoundType<T> getCompoundType(String name, Class<T> compoundType,
            HDF5CompoundMemberMapping... members)
    {
        return compoundReader.getCompoundType(name, compoundType, members);
    }

    public <T> HDF5CompoundType<T> getCompoundTypeForDataSet(String objectPath,
            Class<T> compoundClass)
    {
        return compoundReader.getCompoundTypeForDataSet(objectPath, compoundClass);
    }

    public <T> HDF5CompoundType<T> getNamedCompoundType(Class<T> compoundClass)
    {
        return compoundReader.getNamedCompoundType(compoundClass);
    }

    public <T> HDF5CompoundType<T> getNamedCompoundType(String dataTypeName, Class<T> compoundClass)
    {
        return compoundReader.getNamedCompoundType(dataTypeName, compoundClass);
    }

    public <T> T readCompound(String objectPath, HDF5CompoundType<T> type,
            IByteArrayInspector inspectorOrNull) throws HDF5JavaException
    {
        return compoundReader.readCompound(objectPath, type, inspectorOrNull);
    }

    public <T> T readCompound(String objectPath, HDF5CompoundType<T> type) throws HDF5JavaException
    {
        return compoundReader.readCompound(objectPath, type);
    }

    public <T> T[] readCompoundArray(String objectPath, HDF5CompoundType<T> type,
            IByteArrayInspector inspectorOrNull) throws HDF5JavaException
    {
        return compoundReader.readCompoundArray(objectPath, type, inspectorOrNull);
    }

    public <T> T[] readCompoundArray(String objectPath, HDF5CompoundType<T> type)
            throws HDF5JavaException
    {
        return compoundReader.readCompoundArray(objectPath, type);
    }

    public <T> T[] readCompoundArrayBlock(String objectPath, HDF5CompoundType<T> type,
            int blockSize, long blockNumber, IByteArrayInspector inspectorOrNull)
            throws HDF5JavaException
    {
        return compoundReader.readCompoundArrayBlock(objectPath, type, blockSize, blockNumber,
                inspectorOrNull);
    }

    public <T> T[] readCompoundArrayBlock(String objectPath, HDF5CompoundType<T> type,
            int blockSize, long blockNumber) throws HDF5JavaException
    {
        return compoundReader.readCompoundArrayBlock(objectPath, type, blockSize, blockNumber);
    }

    public <T> T[] readCompoundArrayBlockWithOffset(String objectPath, HDF5CompoundType<T> type,
            int blockSize, long offset, IByteArrayInspector inspectorOrNull)
            throws HDF5JavaException
    {
        return compoundReader.readCompoundArrayBlockWithOffset(objectPath, type, blockSize, offset,
                inspectorOrNull);
    }

    public <T> T[] readCompoundArrayBlockWithOffset(String objectPath, HDF5CompoundType<T> type,
            int blockSize, long offset) throws HDF5JavaException
    {
        return compoundReader.readCompoundArrayBlockWithOffset(objectPath, type, blockSize, offset);
    }

    public <T> MDArray<T> readCompoundMDArray(String objectPath, HDF5CompoundType<T> type,
            IByteArrayInspector inspectorOrNull) throws HDF5JavaException
    {
        return compoundReader.readCompoundMDArray(objectPath, type, inspectorOrNull);
    }

    public <T> MDArray<T> readCompoundMDArray(String objectPath, HDF5CompoundType<T> type)
            throws HDF5JavaException
    {
        return compoundReader.readCompoundMDArray(objectPath, type);
    }

    public <T> MDArray<T> readCompoundMDArrayBlock(String objectPath, HDF5CompoundType<T> type,
            int[] blockDimensions, long[] blockNumber, IByteArrayInspector inspectorOrNull)
            throws HDF5JavaException
    {
        return compoundReader.readCompoundMDArrayBlock(objectPath, type, blockDimensions,
                blockNumber, inspectorOrNull);
    }

    public <T> MDArray<T> readCompoundMDArrayBlock(String objectPath, HDF5CompoundType<T> type,
            int[] blockDimensions, long[] blockNumber) throws HDF5JavaException
    {
        return compoundReader.readCompoundMDArrayBlock(objectPath, type, blockDimensions,
                blockNumber);
    }

    public <T> MDArray<T> readCompoundMDArrayBlockWithOffset(String objectPath,
            HDF5CompoundType<T> type, int[] blockDimensions, long[] offset,
            IByteArrayInspector inspectorOrNull) throws HDF5JavaException
    {
        return compoundReader.readCompoundMDArrayBlockWithOffset(objectPath, type, blockDimensions,
                offset, inspectorOrNull);
    }

    public <T> MDArray<T> readCompoundMDArrayBlockWithOffset(String objectPath,
            HDF5CompoundType<T> type, int[] blockDimensions, long[] offset)
            throws HDF5JavaException
    {
        return compoundReader.readCompoundMDArrayBlockWithOffset(objectPath, type, blockDimensions,
                offset);
    }

    // ------------------------------------------------------------------------------
    // GENERATED CODE SECTION - START
    // ------------------------------------------------------------------------------

    public byte[] getByteArrayAttribute(String objectPath, String attributeName)
    {
        return byteReader.getByteArrayAttribute(objectPath, attributeName);
    }

    public Iterable<HDF5DataBlock<byte[]>> getByteArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return byteReader.getByteArrayNaturalBlocks(dataSetPath);
    }

    public byte getByteAttribute(String objectPath, String attributeName)
    {
        return byteReader.getByteAttribute(objectPath, attributeName);
    }

    public MDByteArray getByteMDArrayAttribute(String objectPath, String attributeName)
    {
        return byteReader.getByteMDArrayAttribute(objectPath, attributeName);
    }

    public Iterable<HDF5MDDataBlock<MDByteArray>> getByteMDArrayNaturalBlocks(String dataSetPath)
    {
        return byteReader.getByteMDArrayNaturalBlocks(dataSetPath);
    }

    public byte[][] getByteMatrixAttribute(String objectPath, String attributeName)
            throws HDF5JavaException
    {
        return byteReader.getByteMatrixAttribute(objectPath, attributeName);
    }

    public byte readByte(String objectPath)
    {
        return byteReader.readByte(objectPath);
    }

    public byte[] readByteArray(String objectPath)
    {
        return byteReader.readByteArray(objectPath);
    }

    public byte[] readByteArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return byteReader.readByteArrayBlock(objectPath, blockSize, blockNumber);
    }

    public byte[] readByteArrayBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return byteReader.readByteArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    public MDByteArray readByteMDArray(String objectPath)
    {
        return byteReader.readByteMDArray(objectPath);
    }

    public MDByteArray readByteMDArrayBlock(String objectPath, int[] blockDimensions,
            long[] blockNumber)
    {
        return byteReader.readByteMDArrayBlock(objectPath, blockDimensions, blockNumber);
    }

    public MDByteArray readByteMDArrayBlockWithOffset(String objectPath, int[] blockDimensions,
            long[] offset)
    {
        return byteReader.readByteMDArrayBlockWithOffset(objectPath, blockDimensions, offset);
    }

    public byte[][] readByteMatrix(String objectPath) throws HDF5JavaException
    {
        return byteReader.readByteMatrix(objectPath);
    }

    public byte[][] readByteMatrixBlock(String objectPath, int blockSizeX, int blockSizeY,
            long blockNumberX, long blockNumberY) throws HDF5JavaException
    {
        return byteReader.readByteMatrixBlock(objectPath, blockSizeX, blockSizeY, blockNumberX,
                blockNumberY);
    }

    public byte[][] readByteMatrixBlockWithOffset(String objectPath, int blockSizeX,
            int blockSizeY, long offsetX, long offsetY) throws HDF5JavaException
    {
        return byteReader.readByteMatrixBlockWithOffset(objectPath, blockSizeX, blockSizeY,
                offsetX, offsetY);
    }

    public int[] readToByteMDArrayBlockWithOffset(String objectPath, MDByteArray array,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        return byteReader.readToByteMDArrayBlockWithOffset(objectPath, array, blockDimensions,
                offset, memoryOffset);
    }

    public int[] readToByteMDArrayWithOffset(String objectPath, MDByteArray array,
            int[] memoryOffset)
    {
        return byteReader.readToByteMDArrayWithOffset(objectPath, array, memoryOffset);
    }

    public double[] getDoubleArrayAttribute(String objectPath, String attributeName)
    {
        return doubleReader.getDoubleArrayAttribute(objectPath, attributeName);
    }

    public Iterable<HDF5DataBlock<double[]>> getDoubleArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return doubleReader.getDoubleArrayNaturalBlocks(dataSetPath);
    }

    public double getDoubleAttribute(String objectPath, String attributeName)
    {
        return doubleReader.getDoubleAttribute(objectPath, attributeName);
    }

    public MDDoubleArray getDoubleMDArrayAttribute(String objectPath, String attributeName)
    {
        return doubleReader.getDoubleMDArrayAttribute(objectPath, attributeName);
    }

    public Iterable<HDF5MDDataBlock<MDDoubleArray>> getDoubleMDArrayNaturalBlocks(String dataSetPath)
    {
        return doubleReader.getDoubleMDArrayNaturalBlocks(dataSetPath);
    }

    public double[][] getDoubleMatrixAttribute(String objectPath, String attributeName)
            throws HDF5JavaException
    {
        return doubleReader.getDoubleMatrixAttribute(objectPath, attributeName);
    }

    public double readDouble(String objectPath)
    {
        return doubleReader.readDouble(objectPath);
    }

    public double[] readDoubleArray(String objectPath)
    {
        return doubleReader.readDoubleArray(objectPath);
    }

    public double[] readDoubleArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return doubleReader.readDoubleArrayBlock(objectPath, blockSize, blockNumber);
    }

    public double[] readDoubleArrayBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return doubleReader.readDoubleArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    public MDDoubleArray readDoubleMDArray(String objectPath)
    {
        return doubleReader.readDoubleMDArray(objectPath);
    }

    public MDDoubleArray readDoubleMDArrayBlock(String objectPath, int[] blockDimensions,
            long[] blockNumber)
    {
        return doubleReader.readDoubleMDArrayBlock(objectPath, blockDimensions, blockNumber);
    }

    public MDDoubleArray readDoubleMDArrayBlockWithOffset(String objectPath, int[] blockDimensions,
            long[] offset)
    {
        return doubleReader.readDoubleMDArrayBlockWithOffset(objectPath, blockDimensions, offset);
    }

    public double[][] readDoubleMatrix(String objectPath) throws HDF5JavaException
    {
        return doubleReader.readDoubleMatrix(objectPath);
    }

    public double[][] readDoubleMatrixBlock(String objectPath, int blockSizeX, int blockSizeY,
            long blockNumberX, long blockNumberY) throws HDF5JavaException
    {
        return doubleReader.readDoubleMatrixBlock(objectPath, blockSizeX, blockSizeY, blockNumberX,
                blockNumberY);
    }

    public double[][] readDoubleMatrixBlockWithOffset(String objectPath, int blockSizeX,
            int blockSizeY, long offsetX, long offsetY) throws HDF5JavaException
    {
        return doubleReader.readDoubleMatrixBlockWithOffset(objectPath, blockSizeX, blockSizeY,
                offsetX, offsetY);
    }

    public int[] readToDoubleMDArrayBlockWithOffset(String objectPath, MDDoubleArray array,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        return doubleReader.readToDoubleMDArrayBlockWithOffset(objectPath, array, blockDimensions,
                offset, memoryOffset);
    }

    public int[] readToDoubleMDArrayWithOffset(String objectPath, MDDoubleArray array,
            int[] memoryOffset)
    {
        return doubleReader.readToDoubleMDArrayWithOffset(objectPath, array, memoryOffset);
    }

    public float[] getFloatArrayAttribute(String objectPath, String attributeName)
    {
        return floatReader.getFloatArrayAttribute(objectPath, attributeName);
    }

    public Iterable<HDF5DataBlock<float[]>> getFloatArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return floatReader.getFloatArrayNaturalBlocks(dataSetPath);
    }

    public float getFloatAttribute(String objectPath, String attributeName)
    {
        return floatReader.getFloatAttribute(objectPath, attributeName);
    }

    public MDFloatArray getFloatMDArrayAttribute(String objectPath, String attributeName)
    {
        return floatReader.getFloatMDArrayAttribute(objectPath, attributeName);
    }

    public Iterable<HDF5MDDataBlock<MDFloatArray>> getFloatMDArrayNaturalBlocks(String dataSetPath)
    {
        return floatReader.getFloatMDArrayNaturalBlocks(dataSetPath);
    }

    public float[][] getFloatMatrixAttribute(String objectPath, String attributeName)
            throws HDF5JavaException
    {
        return floatReader.getFloatMatrixAttribute(objectPath, attributeName);
    }

    public float readFloat(String objectPath)
    {
        return floatReader.readFloat(objectPath);
    }

    public float[] readFloatArray(String objectPath)
    {
        return floatReader.readFloatArray(objectPath);
    }

    public float[] readFloatArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return floatReader.readFloatArrayBlock(objectPath, blockSize, blockNumber);
    }

    public float[] readFloatArrayBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return floatReader.readFloatArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    public MDFloatArray readFloatMDArray(String objectPath)
    {
        return floatReader.readFloatMDArray(objectPath);
    }

    public MDFloatArray readFloatMDArrayBlock(String objectPath, int[] blockDimensions,
            long[] blockNumber)
    {
        return floatReader.readFloatMDArrayBlock(objectPath, blockDimensions, blockNumber);
    }

    public MDFloatArray readFloatMDArrayBlockWithOffset(String objectPath, int[] blockDimensions,
            long[] offset)
    {
        return floatReader.readFloatMDArrayBlockWithOffset(objectPath, blockDimensions, offset);
    }

    public float[][] readFloatMatrix(String objectPath) throws HDF5JavaException
    {
        return floatReader.readFloatMatrix(objectPath);
    }

    public float[][] readFloatMatrixBlock(String objectPath, int blockSizeX, int blockSizeY,
            long blockNumberX, long blockNumberY) throws HDF5JavaException
    {
        return floatReader.readFloatMatrixBlock(objectPath, blockSizeX, blockSizeY, blockNumberX,
                blockNumberY);
    }

    public float[][] readFloatMatrixBlockWithOffset(String objectPath, int blockSizeX,
            int blockSizeY, long offsetX, long offsetY) throws HDF5JavaException
    {
        return floatReader.readFloatMatrixBlockWithOffset(objectPath, blockSizeX, blockSizeY,
                offsetX, offsetY);
    }

    public int[] readToFloatMDArrayBlockWithOffset(String objectPath, MDFloatArray array,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        return floatReader.readToFloatMDArrayBlockWithOffset(objectPath, array, blockDimensions,
                offset, memoryOffset);
    }

    public int[] readToFloatMDArrayWithOffset(String objectPath, MDFloatArray array,
            int[] memoryOffset)
    {
        return floatReader.readToFloatMDArrayWithOffset(objectPath, array, memoryOffset);
    }

    public int[] getIntArrayAttribute(String objectPath, String attributeName)
    {
        return intReader.getIntArrayAttribute(objectPath, attributeName);
    }

    public Iterable<HDF5DataBlock<int[]>> getIntArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return intReader.getIntArrayNaturalBlocks(dataSetPath);
    }

    public int getIntAttribute(String objectPath, String attributeName)
    {
        return intReader.getIntAttribute(objectPath, attributeName);
    }

    public MDIntArray getIntMDArrayAttribute(String objectPath, String attributeName)
    {
        return intReader.getIntMDArrayAttribute(objectPath, attributeName);
    }

    public Iterable<HDF5MDDataBlock<MDIntArray>> getIntMDArrayNaturalBlocks(String dataSetPath)
    {
        return intReader.getIntMDArrayNaturalBlocks(dataSetPath);
    }

    public int[][] getIntMatrixAttribute(String objectPath, String attributeName)
            throws HDF5JavaException
    {
        return intReader.getIntMatrixAttribute(objectPath, attributeName);
    }

    public int readInt(String objectPath)
    {
        return intReader.readInt(objectPath);
    }

    public int[] readIntArray(String objectPath)
    {
        return intReader.readIntArray(objectPath);
    }

    public int[] readIntArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return intReader.readIntArrayBlock(objectPath, blockSize, blockNumber);
    }

    public int[] readIntArrayBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return intReader.readIntArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    public MDIntArray readIntMDArray(String objectPath)
    {
        return intReader.readIntMDArray(objectPath);
    }

    public MDIntArray readIntMDArrayBlock(String objectPath, int[] blockDimensions,
            long[] blockNumber)
    {
        return intReader.readIntMDArrayBlock(objectPath, blockDimensions, blockNumber);
    }

    public MDIntArray readIntMDArrayBlockWithOffset(String objectPath, int[] blockDimensions,
            long[] offset)
    {
        return intReader.readIntMDArrayBlockWithOffset(objectPath, blockDimensions, offset);
    }

    public int[][] readIntMatrix(String objectPath) throws HDF5JavaException
    {
        return intReader.readIntMatrix(objectPath);
    }

    public int[][] readIntMatrixBlock(String objectPath, int blockSizeX, int blockSizeY,
            long blockNumberX, long blockNumberY) throws HDF5JavaException
    {
        return intReader.readIntMatrixBlock(objectPath, blockSizeX, blockSizeY, blockNumberX,
                blockNumberY);
    }

    public int[][] readIntMatrixBlockWithOffset(String objectPath, int blockSizeX, int blockSizeY,
            long offsetX, long offsetY) throws HDF5JavaException
    {
        return intReader.readIntMatrixBlockWithOffset(objectPath, blockSizeX, blockSizeY, offsetX,
                offsetY);
    }

    public int[] readToIntMDArrayBlockWithOffset(String objectPath, MDIntArray array,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        return intReader.readToIntMDArrayBlockWithOffset(objectPath, array, blockDimensions,
                offset, memoryOffset);
    }

    public int[] readToIntMDArrayWithOffset(String objectPath, MDIntArray array, int[] memoryOffset)
    {
        return intReader.readToIntMDArrayWithOffset(objectPath, array, memoryOffset);
    }

    public long[] getLongArrayAttribute(String objectPath, String attributeName)
    {
        return longReader.getLongArrayAttribute(objectPath, attributeName);
    }

    public Iterable<HDF5DataBlock<long[]>> getLongArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return longReader.getLongArrayNaturalBlocks(dataSetPath);
    }

    public long getLongAttribute(String objectPath, String attributeName)
    {
        return longReader.getLongAttribute(objectPath, attributeName);
    }

    public MDLongArray getLongMDArrayAttribute(String objectPath, String attributeName)
    {
        return longReader.getLongMDArrayAttribute(objectPath, attributeName);
    }

    public Iterable<HDF5MDDataBlock<MDLongArray>> getLongMDArrayNaturalBlocks(String dataSetPath)
    {
        return longReader.getLongMDArrayNaturalBlocks(dataSetPath);
    }

    public long[][] getLongMatrixAttribute(String objectPath, String attributeName)
            throws HDF5JavaException
    {
        return longReader.getLongMatrixAttribute(objectPath, attributeName);
    }

    public long readLong(String objectPath)
    {
        return longReader.readLong(objectPath);
    }

    public long[] readLongArray(String objectPath)
    {
        return longReader.readLongArray(objectPath);
    }

    public long[] readLongArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return longReader.readLongArrayBlock(objectPath, blockSize, blockNumber);
    }

    public long[] readLongArrayBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return longReader.readLongArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    public MDLongArray readLongMDArray(String objectPath)
    {
        return longReader.readLongMDArray(objectPath);
    }

    public MDLongArray readLongMDArrayBlock(String objectPath, int[] blockDimensions,
            long[] blockNumber)
    {
        return longReader.readLongMDArrayBlock(objectPath, blockDimensions, blockNumber);
    }

    public MDLongArray readLongMDArrayBlockWithOffset(String objectPath, int[] blockDimensions,
            long[] offset)
    {
        return longReader.readLongMDArrayBlockWithOffset(objectPath, blockDimensions, offset);
    }

    public long[][] readLongMatrix(String objectPath) throws HDF5JavaException
    {
        return longReader.readLongMatrix(objectPath);
    }

    public long[][] readLongMatrixBlock(String objectPath, int blockSizeX, int blockSizeY,
            long blockNumberX, long blockNumberY) throws HDF5JavaException
    {
        return longReader.readLongMatrixBlock(objectPath, blockSizeX, blockSizeY, blockNumberX,
                blockNumberY);
    }

    public long[][] readLongMatrixBlockWithOffset(String objectPath, int blockSizeX,
            int blockSizeY, long offsetX, long offsetY) throws HDF5JavaException
    {
        return longReader.readLongMatrixBlockWithOffset(objectPath, blockSizeX, blockSizeY,
                offsetX, offsetY);
    }

    public int[] readToLongMDArrayBlockWithOffset(String objectPath, MDLongArray array,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        return longReader.readToLongMDArrayBlockWithOffset(objectPath, array, blockDimensions,
                offset, memoryOffset);
    }

    public int[] readToLongMDArrayWithOffset(String objectPath, MDLongArray array,
            int[] memoryOffset)
    {
        return longReader.readToLongMDArrayWithOffset(objectPath, array, memoryOffset);
    }

    public short[] getShortArrayAttribute(String objectPath, String attributeName)
    {
        return shortReader.getShortArrayAttribute(objectPath, attributeName);
    }

    public Iterable<HDF5DataBlock<short[]>> getShortArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return shortReader.getShortArrayNaturalBlocks(dataSetPath);
    }

    public short getShortAttribute(String objectPath, String attributeName)
    {
        return shortReader.getShortAttribute(objectPath, attributeName);
    }

    public MDShortArray getShortMDArrayAttribute(String objectPath, String attributeName)
    {
        return shortReader.getShortMDArrayAttribute(objectPath, attributeName);
    }

    public Iterable<HDF5MDDataBlock<MDShortArray>> getShortMDArrayNaturalBlocks(String dataSetPath)
    {
        return shortReader.getShortMDArrayNaturalBlocks(dataSetPath);
    }

    public short[][] getShortMatrixAttribute(String objectPath, String attributeName)
            throws HDF5JavaException
    {
        return shortReader.getShortMatrixAttribute(objectPath, attributeName);
    }

    public short readShort(String objectPath)
    {
        return shortReader.readShort(objectPath);
    }

    public short[] readShortArray(String objectPath)
    {
        return shortReader.readShortArray(objectPath);
    }

    public short[] readShortArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return shortReader.readShortArrayBlock(objectPath, blockSize, blockNumber);
    }

    public short[] readShortArrayBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return shortReader.readShortArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    public MDShortArray readShortMDArray(String objectPath)
    {
        return shortReader.readShortMDArray(objectPath);
    }

    public MDShortArray readShortMDArrayBlock(String objectPath, int[] blockDimensions,
            long[] blockNumber)
    {
        return shortReader.readShortMDArrayBlock(objectPath, blockDimensions, blockNumber);
    }

    public MDShortArray readShortMDArrayBlockWithOffset(String objectPath, int[] blockDimensions,
            long[] offset)
    {
        return shortReader.readShortMDArrayBlockWithOffset(objectPath, blockDimensions, offset);
    }

    public short[][] readShortMatrix(String objectPath) throws HDF5JavaException
    {
        return shortReader.readShortMatrix(objectPath);
    }

    public short[][] readShortMatrixBlock(String objectPath, int blockSizeX, int blockSizeY,
            long blockNumberX, long blockNumberY) throws HDF5JavaException
    {
        return shortReader.readShortMatrixBlock(objectPath, blockSizeX, blockSizeY, blockNumberX,
                blockNumberY);
    }

    public short[][] readShortMatrixBlockWithOffset(String objectPath, int blockSizeX,
            int blockSizeY, long offsetX, long offsetY) throws HDF5JavaException
    {
        return shortReader.readShortMatrixBlockWithOffset(objectPath, blockSizeX, blockSizeY,
                offsetX, offsetY);
    }

    public int[] readToShortMDArrayBlockWithOffset(String objectPath, MDShortArray array,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        return shortReader.readToShortMDArrayBlockWithOffset(objectPath, array, blockDimensions,
                offset, memoryOffset);
    }

    public int[] readToShortMDArrayWithOffset(String objectPath, MDShortArray array,
            int[] memoryOffset)
    {
        return shortReader.readToShortMDArrayWithOffset(objectPath, array, memoryOffset);
    }

    // ------------------------------------------------------------------------------
    // GENERATED CODE SECTION - END
    // ------------------------------------------------------------------------------

}
