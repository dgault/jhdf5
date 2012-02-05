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

import static ch.systemsx.cisd.hdf5.HDF5Utils.removeInternalNames;

import java.io.File;
import java.util.BitSet;
import java.util.Date;
import java.util.List;

import ncsa.hdf.hdf5lib.exceptions.HDF5DatatypeInterfaceException;
import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;

import ch.systemsx.cisd.base.mdarray.MDArray;
import ch.systemsx.cisd.base.mdarray.MDByteArray;
import ch.systemsx.cisd.base.mdarray.MDDoubleArray;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.base.mdarray.MDIntArray;
import ch.systemsx.cisd.base.mdarray.MDLongArray;
import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.HDF5DataTypeInformation.DataTypeInfoOptions;
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
    interface IHDF5EnumCompleteReader extends IHDF5EnumReader, IHDF5EnumBasicReader
    {
    }

    private final HDF5BaseReader baseReader;

    private final IHDF5ByteReader byteReader;

    private final IHDF5ShortReader shortReader;

    private final IHDF5IntReader intReader;

    private final IHDF5LongReader longReader;

    private final IHDF5FloatReader floatReader;

    private final IHDF5DoubleReader doubleReader;

    private final IHDF5BooleanReader booleanReader;

    private final IHDF5StringReader stringReader;

    private final IHDF5EnumReader enumReader;

    private final IHDF5CompoundReader compoundReader;

    private final IHDF5DateTimeReader dateTimeReader;

    private final IHDF5ReferenceReader referenceReader;

    private final IHDF5GenericReader genericReader;

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
        this.booleanReader = new HDF5BooleanReader(baseReader);
        this.stringReader = new HDF5StringReader(baseReader);
        this.enumReader = new HDF5EnumReader(baseReader);
        this.compoundReader = new HDF5CompoundReader(baseReader, enumReader);
        this.dateTimeReader = new HDF5DateTimeReader(baseReader);
        this.referenceReader = new HDF5ReferenceReader(baseReader);
        this.genericReader = new HDF5GenericReader(baseReader);
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

    // /////////////////////
    // Closing
    // /////////////////////

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        close();
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
        if (followLink == false)
        {
            // Optimization
            baseReader.checkOpen();
            if ("/".equals(objectPath))
            {
                return true;
            }
            return baseReader.h5.exists(baseReader.fileId, objectPath);
        } else
        {
            return exists(objectPath);
        }
    }

    public boolean exists(final String objectPath)
    {
        baseReader.checkOpen();
        if ("/".equals(objectPath))
        {
            return true;
        }
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
                            return baseReader.tryGetDataTypePath(dataTypeId);
                        }
                    };
        return baseReader.runner.call(dataTypeNameCallable);
    }

    public String tryGetDataTypePath(HDF5DataType type)
    {
        assert type != null;

        baseReader.checkOpen();
        type.check(baseReader.fileId);
        return baseReader.tryGetDataTypePath(type.getStorageTypeId());
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
        return getAttributeInformation(dataSetPath, attributeName, DataTypeInfoOptions.DEFAULT);
    }

    public HDF5DataTypeInformation getAttributeInformation(final String dataSetPath,
            final String attributeName, final DataTypeInfoOptions dataTypeInfoOptions)
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
                                        baseReader.getDataTypeInformation(dataTypeId,
                                                dataTypeInfoOptions, registry);
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
        return getDataSetInformation(dataSetPath, DataTypeInfoOptions.DEFAULT);
    }

    public HDF5DataSetInformation getDataSetInformation(final String dataSetPath,
            final DataTypeInfoOptions dataTypeInfoOptions)
    {
        assert dataSetPath != null;

        baseReader.checkOpen();
        return baseReader.getDataSetInformation(dataSetPath, dataTypeInfoOptions);
    }

    public long getSize(final String objectPath)
    {
        return getDataSetInformation(objectPath, DataTypeInfoOptions.MINIMAL).getSize();
    }

    public long getNumberOfElements(final String objectPath)
    {
        return getDataSetInformation(objectPath, DataTypeInfoOptions.MINIMAL).getNumberOfElements();
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

    public String tryGetOpaqueTag(String objectPath)
    {
        return genericReader.tryGetOpaqueTag(objectPath);
    }

    public HDF5OpaqueType tryGetOpaqueType(String objectPath)
    {
        return genericReader.tryGetOpaqueType(objectPath);
    }

    public HDF5DataTypeVariant tryGetTypeVariant(final String objectPath)
    {
        baseReader.checkOpen();
        return baseReader.tryGetTypeVariant(objectPath);
    }

    public HDF5DataTypeVariant tryGetTypeVariant(String objectPath, String attributeName)
    {
        baseReader.checkOpen();
        return baseReader.tryGetTypeVariant(objectPath, attributeName);
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

    public boolean getBooleanAttribute(String objectPath, String attributeName)
            throws HDF5JavaException
    {
        return booleanReader.getBooleanAttribute(objectPath, attributeName);
    }

    public String getEnumAttributeAsString(final String objectPath, final String attributeName)
            throws HDF5JavaException
    {
        return enumReader.getAttrAsString(objectPath, attributeName);
    }

    public HDF5EnumerationValue getEnumAttribute(final String objectPath, final String attributeName)
            throws HDF5JavaException
    {
        return enumReader.getAttr(objectPath, attributeName);
    }

    public <T extends Enum<T>> T getEnumAttribute(String objectPath, String attributeName,
            Class<T> enumClass) throws HDF5JavaException
    {
        return enumReader.getAttr(objectPath, attributeName, enumClass);
    }

    public String[] getEnumArrayAttributeAsString(final String objectPath,
            final String attributeName) throws HDF5JavaException
    {
        return enumReader.getArrayAttr(objectPath, attributeName).toStringArray();
    }

    public HDF5EnumerationValueArray getEnumArrayAttribute(final String objectPath,
            final String attributeName) throws HDF5JavaException
    {
        return enumReader.getArrayAttr(objectPath, attributeName);
    }

    public HDF5EnumerationType getEnumType(String dataTypeName)
    {
        return enumReader.getType(dataTypeName);
    }

    public HDF5EnumerationType getEnumType(String dataTypeName, String[] values)
            throws HDF5JavaException
    {
        return enumReader.getEnumType(dataTypeName, values);
    }

    public HDF5EnumerationType getEnumType(String dataTypeName, String[] values, boolean check)
            throws HDF5JavaException
    {
        return enumReader.getEnumType(dataTypeName, values, check);
    }

    public HDF5EnumerationType getDataSetEnumType(String dataSetPath)
    {
        return enumReader.getDataSetType(dataSetPath);
    }

    public HDF5EnumerationType getEnumTypeForObject(String dataSetPath)
    {
        return enumReader.getDataSetType(dataSetPath);
    }

    // /////////////////////
    // Data Sets
    // /////////////////////

    //
    // Generic
    //

    public Iterable<HDF5DataBlock<byte[]>> getAsByteArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return genericReader.getAsByteArrayNaturalBlocks(dataSetPath);
    }

    public byte[] readAsByteArray(String objectPath)
    {
        return genericReader.readAsByteArray(objectPath);
    }

    public byte[] readAsByteArrayBlock(String objectPath, int blockSize, long blockNumber)
            throws HDF5JavaException
    {
        return genericReader.readAsByteArrayBlock(objectPath, blockSize, blockNumber);
    }

    public byte[] readAsByteArrayBlockWithOffset(String objectPath, int blockSize, long offset)
            throws HDF5JavaException
    {
        return genericReader.readAsByteArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    public int readAsByteArrayToBlockWithOffset(String objectPath, byte[] buffer, int blockSize,
            long offset, int memoryOffset) throws HDF5JavaException
    {
        return genericReader.readAsByteArrayToBlockWithOffset(objectPath, buffer, blockSize,
                offset, memoryOffset);
    }

    //
    // Boolean
    //

    public BitSet readBitField(String objectPath) throws HDF5DatatypeInterfaceException
    {
        return booleanReader.readBitField(objectPath);
    }

    public BitSet readBitFieldBlock(String objectPath, int blockSize, long blockNumber)
    {
        return booleanReader.readBitFieldBlock(objectPath, blockSize, blockNumber);
    }

    public BitSet readBitFieldBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return booleanReader.readBitFieldBlockWithOffset(objectPath, blockSize, offset);
    }

    public boolean isBitSetInBitField(String objectPath, int bitIndex)
    {
        return booleanReader.isBitSetInBitField(objectPath, bitIndex);
    }

    public boolean readBoolean(String objectPath) throws HDF5JavaException
    {
        return booleanReader.readBoolean(objectPath);
    }

    //
    // Time & date
    //

    public long getTimeStampAttribute(String objectPath, String attributeName)
    {
        return dateTimeReader.getTimeStampAttribute(objectPath, attributeName);
    }

    public Date getDateAttribute(String objectPath, String attributeName)
    {
        return dateTimeReader.getDateAttribute(objectPath, attributeName);
    }

    public boolean isTimeStamp(String objectPath, String attributeName) throws HDF5JavaException
    {
        return dateTimeReader.isTimeStamp(objectPath, attributeName);
    }

    public HDF5TimeDuration getTimeDurationAttribute(String objectPath, String attributeName)
    {
        return dateTimeReader.getTimeDurationAttribute(objectPath, attributeName);
    }

    public boolean isTimeDuration(String objectPath) throws HDF5JavaException
    {
        return dateTimeReader.isTimeDuration(objectPath);
    }

    public boolean isTimeStamp(String objectPath) throws HDF5JavaException
    {
        return dateTimeReader.isTimeStamp(objectPath);
    }

    public boolean isTimeDuration(String objectPath, String attributeName) throws HDF5JavaException
    {
        return dateTimeReader.isTimeDuration(objectPath, attributeName);
    }

    public HDF5TimeUnit tryGetTimeUnit(String objectPath, String attributeName)
            throws HDF5JavaException
    {
        return dateTimeReader.tryGetTimeUnit(objectPath, attributeName);
    }

    public long[] getTimeStampArrayAttribute(String objectPath, String attributeName)
    {
        return dateTimeReader.getTimeStampArrayAttribute(objectPath, attributeName);
    }

    public Date[] getDateArrayAttribute(String objectPath, String attributeName)
    {
        return dateTimeReader.getDateArrayAttribute(objectPath, attributeName);
    }

    public HDF5TimeDurationArray getTimeDurationArrayAttribute(String objectPath,
            String attributeName)
    {
        return dateTimeReader.getTimeDurationArrayAttribute(objectPath, attributeName);
    }

    public HDF5TimeUnit tryGetTimeUnit(String objectPath) throws HDF5JavaException
    {
        return dateTimeReader.tryGetTimeUnit(objectPath);
    }

    @Deprecated
    public Iterable<HDF5DataBlock<long[]>> getTimeDurationArrayNaturalBlocks(String dataSetPath,
            HDF5TimeUnit timeUnit) throws HDF5JavaException
    {
        return dateTimeReader.getTimeDurationArrayNaturalBlocks(dataSetPath, timeUnit);
    }

    @Deprecated
    public Iterable<HDF5DataBlock<HDF5TimeDuration[]>> getTimeDurationAndUnitArrayNaturalBlocks(
            String objectPath) throws HDF5JavaException
    {
        return dateTimeReader.getTimeDurationAndUnitArrayNaturalBlocks(objectPath);
    }

    public Iterable<HDF5DataBlock<long[]>> getTimeStampArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return dateTimeReader.getTimeStampArrayNaturalBlocks(dataSetPath);
    }

    public Date readDate(String objectPath) throws HDF5JavaException
    {
        return dateTimeReader.readDate(objectPath);
    }

    public Date[] readDateArray(String objectPath) throws HDF5JavaException
    {
        return dateTimeReader.readDateArray(objectPath);
    }

    @Deprecated
    public long readTimeDuration(String objectPath, HDF5TimeUnit timeUnit) throws HDF5JavaException
    {
        return dateTimeReader.readTimeDuration(objectPath, timeUnit);
    }

    public HDF5TimeDuration readTimeDuration(String objectPath) throws HDF5JavaException
    {
        return dateTimeReader.readTimeDuration(objectPath);
    }

    @Deprecated
    public HDF5TimeDuration readTimeDurationAndUnit(String objectPath) throws HDF5JavaException
    {
        return dateTimeReader.readTimeDurationAndUnit(objectPath);
    }

    @Deprecated
    public long[] readTimeDurationArray(String objectPath, HDF5TimeUnit timeUnit)
            throws HDF5JavaException
    {
        return dateTimeReader.readTimeDurationArray(objectPath, timeUnit);
    }

    public HDF5TimeDurationArray readTimeDurationArray(String objectPath) throws HDF5JavaException
    {
        return dateTimeReader.readTimeDurationArray(objectPath);
    }

    @Deprecated
    public HDF5TimeDuration[] readTimeDurationAndUnitArray(String objectPath)
            throws HDF5JavaException
    {
        return dateTimeReader.readTimeDurationAndUnitArray(objectPath);
    }

    @Deprecated
    public long[] readTimeDurationArrayBlock(String objectPath, int blockSize, long blockNumber,
            HDF5TimeUnit timeUnit)
    {
        return dateTimeReader.readTimeDurationArrayBlock(objectPath, blockSize, blockNumber,
                timeUnit);
    }

    @Deprecated
    public long[] readTimeDurationArrayBlockWithOffset(String objectPath, int blockSize,
            long offset, HDF5TimeUnit timeUnit)
    {
        return dateTimeReader.readTimeDurationArrayBlockWithOffset(objectPath, blockSize, offset,
                timeUnit);
    }

    @Deprecated
    public HDF5TimeDuration[] readTimeDurationAndUnitArrayBlock(String objectPath, int blockSize,
            long blockNumber) throws HDF5JavaException
    {
        return dateTimeReader.readTimeDurationAndUnitArrayBlock(objectPath, blockSize, blockNumber);
    }

    @Deprecated
    public HDF5TimeDuration[] readTimeDurationAndUnitArrayBlockWithOffset(String objectPath,
            int blockSize, long offset) throws HDF5JavaException
    {
        return dateTimeReader.readTimeDurationAndUnitArrayBlockWithOffset(objectPath, blockSize,
                offset);
    }

    public long readTimeStamp(String objectPath) throws HDF5JavaException
    {
        return dateTimeReader.readTimeStamp(objectPath);
    }

    public long[] readTimeStampArray(String objectPath) throws HDF5JavaException
    {
        return dateTimeReader.readTimeStampArray(objectPath);
    }

    public long[] readTimeStampArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return dateTimeReader.readTimeStampArrayBlock(objectPath, blockSize, blockNumber);
    }

    public long[] readTimeStampArrayBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return dateTimeReader.readTimeStampArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    //
    // Reference
    //

    public String readObjectReference(final String objectPath)
    {
        return referenceReader.readObjectReference(objectPath);
    }

    public String readObjectReference(String objectPath, boolean resolveName)
    {
        return referenceReader.readObjectReference(objectPath, resolveName);
    }

    public String[] readObjectReferenceArrayBlock(String objectPath, int blockSize,
            long blockNumber, boolean resolveName)
    {
        return referenceReader.readObjectReferenceArrayBlock(objectPath, blockSize, blockNumber,
                resolveName);
    }

    public String[] readObjectReferenceArrayBlockWithOffset(String objectPath, int blockSize,
            long offset, boolean resolveName)
    {
        return referenceReader.readObjectReferenceArrayBlockWithOffset(objectPath, blockSize,
                offset, resolveName);
    }

    public MDArray<String> readObjectReferenceMDArrayBlock(String objectPath,
            int[] blockDimensions, long[] blockNumber, boolean resolveName)
    {
        return referenceReader.readObjectReferenceMDArrayBlock(objectPath, blockDimensions,
                blockNumber, resolveName);
    }

    public MDArray<String> readObjectReferenceMDArrayBlockWithOffset(String objectPath,
            int[] blockDimensions, long[] offset, boolean resolveName)
    {
        return referenceReader.readObjectReferenceMDArrayBlockWithOffset(objectPath,
                blockDimensions, offset, resolveName);
    }

    public Iterable<HDF5DataBlock<String[]>> getObjectReferenceArrayNaturalBlocks(
            String dataSetPath, boolean resolveName)
    {
        return referenceReader.getObjectReferenceArrayNaturalBlocks(dataSetPath, resolveName);
    }

    public Iterable<HDF5MDDataBlock<MDArray<String>>> getObjectReferenceMDArrayNaturalBlocks(
            String dataSetPath, boolean resolveName)
    {
        return referenceReader.getObjectReferenceMDArrayNaturalBlocks(dataSetPath, resolveName);
    }

    public String[] readObjectReferenceArray(String objectPath)
    {
        return referenceReader.readObjectReferenceArray(objectPath);
    }

    public String[] readObjectReferenceArray(String objectPath, boolean resolveName)
    {
        return referenceReader.readObjectReferenceArray(objectPath, resolveName);
    }

    public MDArray<String> readObjectReferenceMDArray(String objectPath)
    {
        return referenceReader.readObjectReferenceMDArray(objectPath);
    }

    public MDArray<String> readObjectReferenceMDArray(String objectPath, boolean resolveName)
    {
        return referenceReader.readObjectReferenceMDArray(objectPath, resolveName);
    }

    public String getObjectReferenceAttribute(String objectPath, String attributeName,
            boolean resolveName)
    {
        return referenceReader.getObjectReferenceAttribute(objectPath, attributeName, resolveName);
    }

    public String[] getObjectReferenceArrayAttribute(String objectPath, String attributeName,
            boolean resolveName)
    {
        return referenceReader.getObjectReferenceArrayAttribute(objectPath, attributeName,
                resolveName);
    }

    public MDArray<String> getObjectReferenceMDArrayAttribute(String objectPath,
            String attributeName, boolean resolveName)
    {
        return referenceReader.getObjectReferenceMDArrayAttribute(objectPath, attributeName,
                resolveName);
    }

    public HDF5TimeDurationArray readTimeDurationArrayBlock(String objectPath, int blockSize,
            long blockNumber) throws HDF5JavaException
    {
        return dateTimeReader.readTimeDurationArrayBlock(objectPath, blockSize, blockNumber);
    }

    public HDF5TimeDurationArray readTimeDurationArrayBlockWithOffset(String objectPath,
            int blockSize, long offset) throws HDF5JavaException
    {
        return dateTimeReader.readTimeDurationArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    public Iterable<HDF5DataBlock<HDF5TimeDurationArray>> getTimeDurationArrayNaturalBlocks(
            String objectPath) throws HDF5JavaException
    {
        return dateTimeReader.getTimeDurationArrayNaturalBlocks(objectPath);
    }

    //
    // References
    //

    public String resolvePath(String reference) throws HDF5JavaException
    {
        return referenceReader.resolvePath(reference);
    }

    public String getObjectReferenceAttribute(final String objectPath, final String attributeName)
    {
        return referenceReader.getObjectReferenceAttribute(objectPath, attributeName);
    }

    public String[] getObjectReferenceArrayAttribute(String objectPath, String attributeName)
    {
        return referenceReader.getObjectReferenceArrayAttribute(objectPath, attributeName);
    }

    public MDArray<String> getObjectReferenceMDArrayAttribute(String objectPath,
            String attributeName)
    {
        return referenceReader.getObjectReferenceMDArrayAttribute(objectPath, attributeName);
    }

    public String[] readObjectReferenceArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return referenceReader.readObjectReferenceArrayBlock(objectPath, blockSize, blockNumber);
    }

    public String[] readObjectReferenceArrayBlockWithOffset(String objectPath, int blockSize,
            long offset)
    {
        return referenceReader.readObjectReferenceArrayBlockWithOffset(objectPath, blockSize,
                offset);
    }

    public MDArray<String> readObjectReferenceMDArrayBlock(String objectPath,
            int[] blockDimensions, long[] blockNumber)
    {
        return referenceReader.readObjectReferenceMDArrayBlock(objectPath, blockDimensions,
                blockNumber);
    }

    public MDArray<String> readObjectReferenceMDArrayBlockWithOffset(String objectPath,
            int[] blockDimensions, long[] offset)
    {
        return referenceReader.readObjectReferenceMDArrayBlockWithOffset(objectPath,
                blockDimensions, offset);
    }

    public Iterable<HDF5DataBlock<String[]>> getObjectReferenceArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return referenceReader.getObjectReferenceArrayNaturalBlocks(dataSetPath);
    }

    public Iterable<HDF5MDDataBlock<MDArray<String>>> getObjectReferenceMDArrayNaturalBlocks(
            String dataSetPath)
    {
        return referenceReader.getObjectReferenceMDArrayNaturalBlocks(dataSetPath);
    }

    //
    // String
    //

    public String getStringAttribute(String objectPath, String attributeName)
    {
        return stringReader.getStringAttribute(objectPath, attributeName);
    }

    public String[] getStringArrayAttribute(String objectPath, String attributeName)
    {
        return stringReader.getStringArrayAttribute(objectPath, attributeName);
    }

    public MDArray<String> getStringMDArrayAttribute(String objectPath, String attributeName)
    {
        return stringReader.getStringMDArrayAttribute(objectPath, attributeName);
    }

    public String readString(String objectPath) throws HDF5JavaException
    {
        return stringReader.readString(objectPath);
    }

    public String[] readStringArray(String objectPath) throws HDF5JavaException
    {
        return stringReader.readStringArray(objectPath);
    }

    public String[] readStringArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return stringReader.readStringArrayBlock(objectPath, blockSize, blockNumber);
    }

    public String[] readStringArrayBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return stringReader.readStringArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    public MDArray<String> readStringMDArray(String objectPath)
    {
        return stringReader.readStringMDArray(objectPath);
    }

    public MDArray<String> readStringMDArrayBlock(String objectPath, int[] blockDimensions,
            long[] blockNumber)
    {
        return stringReader.readStringMDArrayBlock(objectPath, blockDimensions, blockNumber);
    }

    public MDArray<String> readStringMDArrayBlockWithOffset(String objectPath,
            int[] blockDimensions, long[] offset)
    {
        return stringReader.readStringMDArrayBlockWithOffset(objectPath, blockDimensions, offset);
    }

    public Iterable<HDF5DataBlock<String[]>> getStringArrayNaturalBlocks(String objectPath)
            throws HDF5JavaException
    {
        return stringReader.getStringArrayNaturalBlocks(objectPath);
    }

    public Iterable<HDF5MDDataBlock<MDArray<String>>> getStringMDArrayNaturalBlocks(
            String objectPath)
    {
        return stringReader.getStringMDArrayNaturalBlocks(objectPath);
    }

    //
    // Enums
    //

    public IHDF5EnumReader enums()
    {
        return enumReader;
    }

    public Iterable<HDF5DataBlock<HDF5EnumerationValueArray>> getEnumArrayNaturalBlocks(
            String objectPath, HDF5EnumerationType enumType) throws HDF5JavaException
    {
        return enumReader.getArrayNaturalBlocks(objectPath, enumType);
    }

    public Iterable<HDF5DataBlock<HDF5EnumerationValueArray>> getEnumArrayNaturalBlocks(
            String objectPath) throws HDF5JavaException
    {
        return enumReader.getArrayNaturalBlocks(objectPath);
    }

    public HDF5EnumerationValue readEnum(String objectPath, HDF5EnumerationType enumType)
            throws HDF5JavaException
    {
        return enumReader.read(objectPath, enumType);
    }

    public HDF5EnumerationValue readEnum(String objectPath) throws HDF5JavaException
    {
        return enumReader.read(objectPath);
    }

    public <T extends Enum<T>> T readEnum(String objectPath, Class<T> enumClass)
            throws HDF5JavaException
    {
        return enumReader.read(objectPath, enumClass);
    }

    public HDF5EnumerationValueArray readEnumArray(String objectPath, HDF5EnumerationType enumType)
            throws HDF5JavaException
    {
        return enumReader.readArray(objectPath, enumType);
    }

    public HDF5EnumerationValueArray readEnumArray(String objectPath) throws HDF5JavaException
    {
        return enumReader.readArray(objectPath);
    }

    public String[] readEnumArrayAsString(String objectPath) throws HDF5JavaException
    {
        return enumReader.readArray(objectPath).toStringArray();
    }

    public HDF5EnumerationValueArray readEnumArrayBlock(String objectPath,
            HDF5EnumerationType enumType, int blockSize, long blockNumber)
    {
        return enumReader.readArrayBlock(objectPath, enumType, blockSize, blockNumber);
    }

    public HDF5EnumerationValueArray readEnumArrayBlock(String objectPath, int blockSize,
            long blockNumber)
    {
        return enumReader.readArrayBlock(objectPath, blockSize, blockNumber);
    }

    public HDF5EnumerationValueArray readEnumArrayBlockWithOffset(String objectPath,
            HDF5EnumerationType enumType, int blockSize, long offset)
    {
        return enumReader.readArrayBlockWithOffset(objectPath, enumType, blockSize, offset);
    }

    public HDF5EnumerationValueArray readEnumArrayBlockWithOffset(String objectPath, int blockSize,
            long offset)
    {
        return enumReader.readArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    public String readEnumAsString(String objectPath) throws HDF5JavaException
    {
        return enumReader.readAsString(objectPath);
    }

    //
    // Compounds
    //

    public <T> T getCompoundAttribute(String objectPath, String attributeName,
            HDF5CompoundType<T> type) throws HDF5JavaException
    {
        return compoundReader.getCompoundAttribute(objectPath, attributeName, type);
    }

    public <T> T getCompoundAttribute(String objectPath, String attributeName, Class<T> pojoClass)
            throws HDF5JavaException
    {
        return compoundReader.getCompoundAttribute(objectPath, attributeName, pojoClass);
    }

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

    public <T> Iterable<HDF5MDDataBlock<MDArray<T>>> getCompoundMDArrayNaturalBlocks(
            String objectPath, HDF5CompoundType<T> type, IByteArrayInspector inspectorOrNull)
            throws HDF5JavaException
    {
        return compoundReader.getCompoundMDArrayNaturalBlocks(objectPath, type, inspectorOrNull);
    }

    public <T> Iterable<HDF5MDDataBlock<MDArray<T>>> getCompoundMDArrayNaturalBlocks(
            String objectPath, HDF5CompoundType<T> type) throws HDF5JavaException
    {
        return compoundReader.getCompoundMDArrayNaturalBlocks(objectPath, type);
    }

    public <T> Iterable<HDF5DataBlock<T[]>> getCompoundArrayNaturalBlocks(String objectPath,
            Class<T> pojoClass) throws HDF5JavaException
    {
        return compoundReader.getCompoundArrayNaturalBlocks(objectPath, pojoClass);
    }

    public <T> Iterable<HDF5MDDataBlock<MDArray<T>>> getCompoundMDArrayNaturalBlocks(
            String objectPath, Class<T> pojoClass) throws HDF5JavaException
    {
        return compoundReader.getCompoundMDArrayNaturalBlocks(objectPath, pojoClass);
    }

    @SuppressWarnings("deprecation")
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

    public HDF5CompoundMemberInformation[] getCompoundDataSetInformation(String dataSetPath,
            DataTypeInfoOptions dataTypeInfoOptions) throws HDF5JavaException
    {
        return compoundReader.getCompoundDataSetInformation(dataSetPath, dataTypeInfoOptions);
    }

    public <T> HDF5CompoundMemberInformation[] getCompoundMemberInformation(Class<T> compoundClass)
    {
        return compoundReader.getCompoundMemberInformation(compoundClass);
    }

    public HDF5CompoundMemberInformation[] getCompoundMemberInformation(String dataTypeName)
    {
        return compoundReader.getCompoundMemberInformation(dataTypeName);
    }

    public HDF5CompoundMemberInformation[] getCompoundMemberInformation(String dataTypeName,
            DataTypeInfoOptions dataTypeInfoOptions)
    {
        return compoundReader.getCompoundMemberInformation(dataTypeName, dataTypeInfoOptions);
    }

    public <T> HDF5CompoundType<T> getCompoundType(Class<T> pojoClass,
            HDF5CompoundMemberMapping... members)
    {
        return compoundReader.getCompoundType(pojoClass, members);
    }

    public <T> HDF5CompoundType<T> getCompoundType(String name, Class<T> compoundType,
            HDF5CompoundMemberMapping... members)
    {
        return compoundReader.getCompoundType(name, compoundType, members);
    }

    public <T> HDF5CompoundType<T> getDataSetCompoundType(String objectPath, Class<T> pojoClass,
            HDF5CompoundMappingHints hints)
    {
        return compoundReader.getDataSetCompoundType(objectPath, pojoClass, hints);
    }

    public <T> HDF5CompoundType<T> getNamedCompoundType(String dataTypeName, Class<T> pojoClass,
            DataTypeInfoOptions dataTypeInfoOptions)
    {
        return compoundReader.getNamedCompoundType(dataTypeName, pojoClass, dataTypeInfoOptions);
    }

    public <T> HDF5CompoundType<T> getDataSetCompoundType(String objectPath, Class<T> compoundClass)
    {
        return compoundReader.getDataSetCompoundType(objectPath, compoundClass);
    }

    public <T> HDF5CompoundType<T> getAttributeCompoundType(String objectPath,
            String attributeName, Class<T> pojoClass)
    {
        return compoundReader.getAttributeCompoundType(objectPath, attributeName, pojoClass);
    }

    public <T> HDF5CompoundType<T> getAttributeCompoundType(String objectPath,
            String attributeName, Class<T> pojoClass, HDF5CompoundMappingHints hints)
    {
        return compoundReader.getAttributeCompoundType(objectPath, attributeName, pojoClass, hints);
    }

    public <T> HDF5CompoundType<T> getInferredCompoundType(Class<T> pojoClass)
    {
        return compoundReader.getInferredCompoundType(pojoClass);
    }

    public <T> HDF5CompoundType<T> getInferredCompoundType(String name, Class<T> compoundType)
    {
        return compoundReader.getInferredCompoundType(name, compoundType);
    }

    public <T> HDF5CompoundType<T> getInferredCompoundType(String name, T template)
    {
        return compoundReader.getInferredCompoundType(name, template);
    }

    public <T> HDF5CompoundType<T> getInferredCompoundType(T template)
    {
        return compoundReader.getInferredCompoundType(template);
    }

    public <T> HDF5CompoundType<T> getInferredCompoundType(String name, Class<T> pojoClass,
            HDF5CompoundMappingHints hints)
    {
        return compoundReader.getInferredCompoundType(name, pojoClass, hints);
    }

    public <T> HDF5CompoundType<T> getInferredCompoundType(String name, T template,
            HDF5CompoundMappingHints hints)
    {
        return compoundReader.getInferredCompoundType(name, template, hints);
    }

    public HDF5CompoundType<List<?>> getInferredCompoundType(String name, List<String> memberNames,
            List<?> template, HDF5CompoundMappingHints hints)
    {
        return compoundReader.getInferredCompoundType(name, memberNames, template, hints);
    }

    public HDF5CompoundType<List<?>> getInferredCompoundType(String name, List<String> memberNames,
            List<?> template)
    {
        return compoundReader.getInferredCompoundType(name, memberNames, template);
    }

    public HDF5CompoundType<Object[]> getInferredCompoundType(String name, String[] memberNames,
            Object[] template)
    {
        return compoundReader.getInferredCompoundType(name, memberNames, template);
    }

    public HDF5CompoundType<List<?>> getInferredCompoundType(List<String> memberNames,
            List<?> template)
    {
        return compoundReader.getInferredCompoundType(memberNames, template);
    }

    public HDF5CompoundType<Object[]> getInferredCompoundType(String[] memberNames,
            Object[] template)
    {
        return compoundReader.getInferredCompoundType(memberNames, template);
    }

    public <T> HDF5CompoundType<T> getNamedCompoundType(Class<T> compoundClass)
    {
        return compoundReader.getNamedCompoundType(compoundClass);
    }

    public <T> HDF5CompoundType<T> getNamedCompoundType(String dataTypeName, Class<T> compoundClass)
    {
        return compoundReader.getNamedCompoundType(dataTypeName, compoundClass);
    }

    public <T> HDF5CompoundType<T> getNamedCompoundType(String dataTypeName, Class<T> pojoClass,
            HDF5CompoundMappingHints hints)
    {
        return compoundReader.getNamedCompoundType(dataTypeName, pojoClass, hints);
    }

    public <T> HDF5CompoundType<T> getAttributeCompoundType(String objectPath,
            String attributeName, Class<T> pojoClass, HDF5CompoundMappingHints hints,
            DataTypeInfoOptions dataTypeInfoOptions)
    {
        return compoundReader.getAttributeCompoundType(objectPath, attributeName, pojoClass, hints,
                dataTypeInfoOptions);
    }

    public <T> HDF5CompoundType<T> getNamedCompoundType(String dataTypeName, Class<T> pojoClass,
            HDF5CompoundMappingHints hints, DataTypeInfoOptions dataTypeInfoOptions)
    {
        return compoundReader.getNamedCompoundType(dataTypeName, pojoClass, hints,
                dataTypeInfoOptions);
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

    public <T> T readCompound(String objectPath, Class<T> pojoClass) throws HDF5JavaException
    {
        return compoundReader.readCompound(objectPath, pojoClass);
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

    public <T> T[] readCompoundArray(String objectPath, Class<T> pojoClass)
            throws HDF5JavaException
    {
        return compoundReader.readCompoundArray(objectPath, pojoClass);
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

    public <T> MDArray<T> readCompoundMDArray(String objectPath, Class<T> pojoClass)
            throws HDF5JavaException
    {
        return compoundReader.readCompoundMDArray(objectPath, pojoClass);
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
