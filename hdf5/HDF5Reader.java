/*
 * Copyright 2007 - 2014 ETH Zuerich, CISD and SIS.
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

import java.io.File;
import java.util.Arrays;
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
import ch.systemsx.cisd.hdf5.IHDF5CompoundInformationRetriever.IByteArrayInspector;

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
    private final HDF5BaseReader baseReader;
    
    private final IHDF5FileLevelReadOnlyHandler fileHandler;
    
    private final IHDF5ObjectReadOnlyInfoProviderHandler objectHandler;

    private final IHDF5ByteReader byteReader;

    private final IHDF5ByteReader ubyteReader;

    private final IHDF5ShortReader shortReader;

    private final IHDF5ShortReader ushortReader;

    private final IHDF5IntReader intReader;

    private final IHDF5IntReader uintReader;

    protected final IHDF5LongReader longReader;

    private final IHDF5LongReader ulongReader;

    private final IHDF5FloatReader floatReader;

    private final IHDF5DoubleReader doubleReader;

    private final IHDF5BooleanReader booleanReader;

    private final IHDF5StringReader stringReader;

    private final IHDF5EnumReader enumReader;

    private final IHDF5CompoundReader compoundReader;

    private final IHDF5DateTimeReader dateTimeReader;

    private final HDF5TimeDurationReader timeDurationReader;

    private final IHDF5ReferenceReader referenceReader;

    private final IHDF5OpaqueReader opaqueReader;

    HDF5Reader(final HDF5BaseReader baseReader)
    {
        assert baseReader != null;

        this.baseReader = baseReader;
        // Ensure the finalizer of this HDF5Reader doesn't close the file behind the back of the 
        // specialized readers when they are still in operation.
        baseReader.setMyReader(this);
        this.fileHandler = new HDF5FileLevelReadOnlyHandler(baseReader);
        this.objectHandler = new HDF5ObjectReadOnlyInfoProviderHandler(baseReader);
        this.byteReader = new HDF5ByteReader(baseReader);
        this.ubyteReader = new HDF5UnsignedByteReader(baseReader);
        this.shortReader = new HDF5ShortReader(baseReader);
        this.ushortReader = new HDF5UnsignedShortReader(baseReader);
        this.intReader = new HDF5IntReader(baseReader);
        this.uintReader = new HDF5UnsignedIntReader(baseReader);
        this.longReader = new HDF5LongReader(baseReader);
        this.ulongReader = new HDF5UnsignedLongReader(baseReader);
        this.floatReader = new HDF5FloatReader(baseReader);
        this.doubleReader = new HDF5DoubleReader(baseReader);
        this.booleanReader = new HDF5BooleanReader(baseReader);
        this.stringReader = new HDF5StringReader(baseReader);
        this.enumReader = new HDF5EnumReader(baseReader);
        this.compoundReader = new HDF5CompoundReader(baseReader, enumReader);
        this.dateTimeReader = new HDF5DateTimeReader(baseReader, (HDF5LongReader) longReader);
        this.timeDurationReader = new HDF5TimeDurationReader(baseReader, (HDF5LongReader) longReader);
        this.referenceReader = new HDF5ReferenceReader(baseReader);
        this.opaqueReader = new HDF5OpaqueReader(baseReader);
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
    // File
    // /////////////////////
    
    @Override
    public IHDF5FileLevelReadOnlyHandler file()
    {
        return fileHandler;
    }

    @Override
    public boolean isPerformNumericConversions()
    {
        return baseReader.performNumericConversions;
    }

    @Override
    public String getHouseKeepingNameSuffix()
    {
        return baseReader.houseKeepingNameSuffix;
    }
    
    @Override
    public File getFile()
    {
        return baseReader.hdf5File;
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        close();
    }

    @Override
    public void close()
    {
        baseReader.close();
    }

    @Override
    public boolean isClosed()
    {
        return baseReader.isClosed();
    }

    // /////////////////////////////////
    // Objects, links, groups and types
    // /////////////////////////////////

    @Override
    public IHDF5ObjectReadOnlyInfoProviderHandler object()
    {
        return objectHandler;
    }

    @Override
    public HDF5LinkInformation getLinkInformation(String objectPath)
    {
        return objectHandler.getLinkInformation(objectPath);
    }

    @Override
    public HDF5ObjectInformation getObjectInformation(String objectPath)
    {
        return objectHandler.getObjectInformation(objectPath);
    }

    @Override
    public HDF5ObjectType getObjectType(String objectPath, boolean followLink)
    {
        return objectHandler.getObjectType(objectPath, followLink);
    }

    @Override
    public HDF5ObjectType getObjectType(String objectPath)
    {
        return objectHandler.getObjectType(objectPath);
    }

    @Override
    public boolean exists(String objectPath, boolean followLink)
    {
        return objectHandler.exists(objectPath, followLink);
    }

    @Override
    public boolean exists(String objectPath)
    {
        return objectHandler.exists(objectPath);
    }

    @Override
    public String toHouseKeepingPath(String objectPath)
    {
        return objectHandler.toHouseKeepingPath(objectPath);
    }

    @Override
    public boolean isHouseKeepingObject(String objectPath)
    {
        return objectHandler.isHouseKeepingObject(objectPath);
    }

    @Override
    public boolean isGroup(String objectPath, boolean followLink)
    {
        return objectHandler.isGroup(objectPath, followLink);
    }

    @Override
    public boolean isGroup(String objectPath)
    {
        return objectHandler.isGroup(objectPath);
    }

    @Override
    public boolean isDataSet(String objectPath, boolean followLink)
    {
        return objectHandler.isDataSet(objectPath, followLink);
    }

    @Override
    public boolean isDataSet(String objectPath)
    {
        return objectHandler.isDataSet(objectPath);
    }

    @Override
    public boolean isDataType(String objectPath, boolean followLink)
    {
        return objectHandler.isDataType(objectPath, followLink);
    }

    @Override
    public boolean isDataType(String objectPath)
    {
        return objectHandler.isDataType(objectPath);
    }

    @Override
    public boolean isSoftLink(String objectPath)
    {
        return objectHandler.isSoftLink(objectPath);
    }

    @Override
    public boolean isExternalLink(String objectPath)
    {
        return objectHandler.isExternalLink(objectPath);
    }

    @Override
    public boolean isSymbolicLink(String objectPath)
    {
        return objectHandler.isSymbolicLink(objectPath);
    }

    @Override
    public String tryGetSymbolicLinkTarget(String objectPath)
    {
        return objectHandler.tryGetSymbolicLinkTarget(objectPath);
    }

    @Override
    public boolean hasAttribute(String objectPath, String attributeName)
    {
        return objectHandler.hasAttribute(objectPath, attributeName);
    }

    @Override
    public List<String> getAttributeNames(String objectPath)
    {
        return objectHandler.getAttributeNames(objectPath);
    }

    @Override
    public List<String> getAllAttributeNames(String objectPath)
    {
        return objectHandler.getAllAttributeNames(objectPath);
    }

    @Override
    public HDF5DataTypeInformation getAttributeInformation(String objectPath, String attributeName)
    {
        return objectHandler.getAttributeInformation(objectPath, attributeName);
    }

    @Override
    public HDF5DataTypeInformation getAttributeInformation(String objectPath, String attributeName,
            DataTypeInfoOptions dataTypeInfoOptions)
    {
        return objectHandler.getAttributeInformation(objectPath, attributeName,
                dataTypeInfoOptions);
    }

    @Override
    public HDF5DataSetInformation getDataSetInformation(String dataSetPath)
    {
        return objectHandler.getDataSetInformation(dataSetPath);
    }

    @Override
    public HDF5DataSetInformation getDataSetInformation(String dataSetPath,
            DataTypeInfoOptions dataTypeInfoOptions)
    {
        return objectHandler.getDataSetInformation(dataSetPath, dataTypeInfoOptions);
    }

    @Override
    public long getSize(String objectPath)
    {
        return objectHandler.getSize(objectPath);
    }

    @Override
    public long getNumberOfElements(String objectPath)
    {
        return objectHandler.getNumberOfElements(objectPath);
    }

    @Override
    public void copy(String sourceObject, IHDF5Writer destinationWriter, String destinationObject)
    {
        objectHandler.copy(sourceObject, destinationWriter, destinationObject);
    }

    @Override
    public void copy(String sourceObject, IHDF5Writer destinationWriter)
    {
        objectHandler.copy(sourceObject, destinationWriter);
    }

    @Override
    public void copyAll(IHDF5Writer destinationWriter)
    {
        objectHandler.copyAll(destinationWriter);
    }

    @Override
    public List<String> getGroupMembers(String groupPath)
    {
        return objectHandler.getGroupMembers(groupPath);
    }

    @Override
    public List<String> getAllGroupMembers(String groupPath)
    {
        return objectHandler.getAllGroupMembers(groupPath);
    }

    @Override
    public List<String> getGroupMemberPaths(String groupPath)
    {
        return objectHandler.getGroupMemberPaths(groupPath);
    }

    @Override
    public List<HDF5LinkInformation> getGroupMemberInformation(String groupPath,
            boolean readLinkTargets)
    {
        return objectHandler.getGroupMemberInformation(groupPath, readLinkTargets);
    }

    @Override
    public List<HDF5LinkInformation> getAllGroupMemberInformation(String groupPath,
            boolean readLinkTargets)
    {
        return objectHandler.getAllGroupMemberInformation(groupPath, readLinkTargets);
    }

    @Override
    public HDF5DataTypeVariant tryGetTypeVariant(String objectPath)
    {
        return objectHandler.tryGetTypeVariant(objectPath);
    }

    @Override
    public HDF5DataTypeVariant tryGetTypeVariant(String objectPath, String attributeName)
    {
        return objectHandler.tryGetTypeVariant(objectPath, attributeName);
    }

    @Override
    public String tryGetDataTypePath(String objectPath)
    {
        return objectHandler.tryGetDataTypePath(objectPath);
    }

    @Override
    public String tryGetDataTypePath(HDF5DataType type)
    {
        return objectHandler.tryGetDataTypePath(type);
    }

    @Override
    public boolean getBooleanAttribute(String objectPath, String attributeName)
            throws HDF5JavaException
    {
        return booleanReader.getAttr(objectPath, attributeName);
    }

    @Override
    public String getEnumAttributeAsString(final String objectPath, final String attributeName)
            throws HDF5JavaException
    {
        return enumReader.getAttrAsString(objectPath, attributeName);
    }

    @Override
    public HDF5EnumerationValue getEnumAttribute(final String objectPath, final String attributeName)
            throws HDF5JavaException
    {
        return enumReader.getAttr(objectPath, attributeName);
    }

    @Override
    public <T extends Enum<T>> T getEnumAttribute(String objectPath, String attributeName,
            Class<T> enumClass) throws HDF5JavaException
    {
        return enumReader.getAttr(objectPath, attributeName, enumClass);
    }

    @Override
    public String[] getEnumArrayAttributeAsString(final String objectPath,
            final String attributeName) throws HDF5JavaException
    {
        return enumReader.getArrayAttr(objectPath, attributeName).toStringArray();
    }

    @Override
    public HDF5EnumerationValueArray getEnumArrayAttribute(final String objectPath,
            final String attributeName) throws HDF5JavaException
    {
        return enumReader.getArrayAttr(objectPath, attributeName);
    }

    @Override
    public HDF5EnumerationType getEnumType(String dataTypeName)
    {
        return enumReader.getType(dataTypeName);
    }

    @Override
    public HDF5EnumerationType getEnumType(String dataTypeName, String[] values)
            throws HDF5JavaException
    {
        return enumReader.getType(dataTypeName, values);
    }

    @Override
    public HDF5EnumerationType getEnumType(String dataTypeName, String[] values, boolean check)
            throws HDF5JavaException
    {
        return enumReader.getType(dataTypeName, values, check);
    }

    @Override
    public HDF5EnumerationType getDataSetEnumType(String dataSetPath)
    {
        return enumReader.getDataSetType(dataSetPath);
    }

    @Override
    public HDF5EnumerationType getEnumTypeForObject(String dataSetPath)
    {
        return enumReader.getDataSetType(dataSetPath);
    }

    // /////////////////////
    // Data Sets reading
    // /////////////////////

    //
    // Opaque
    //

    @Override
    public String tryGetOpaqueTag(String objectPath)
    {
        return opaqueReader.tryGetOpaqueTag(objectPath);
    }

    @Override
    public HDF5OpaqueType tryGetOpaqueType(String objectPath)
    {
        return opaqueReader.tryGetOpaqueType(objectPath);
    }

    @Override
    public IHDF5OpaqueReader opaque()
    {
        return opaqueReader;
    }

    @Override
    public Iterable<HDF5DataBlock<byte[]>> getAsByteArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return opaqueReader.getArrayNaturalBlocks(dataSetPath);
    }

    @Override
    public byte[] readAsByteArray(String objectPath)
    {
        return opaqueReader.readArray(objectPath);
    }

    @Override
    public byte[] getAttributeAsByteArray(String objectPath, String attributeName)
    {
        return opaqueReader.getArrayAttr(objectPath, attributeName);
    }

    @Override
    public byte[] readAsByteArrayBlock(String objectPath, int blockSize, long blockNumber)
            throws HDF5JavaException
    {
        return opaqueReader.readArrayBlock(objectPath, blockSize, blockNumber);
    }

    @Override
    public byte[] readAsByteArrayBlockWithOffset(String objectPath, int blockSize, long offset)
            throws HDF5JavaException
    {
        return opaqueReader.readArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    @Override
    public int readAsByteArrayToBlockWithOffset(String objectPath, byte[] buffer, int blockSize,
            long offset, int memoryOffset) throws HDF5JavaException
    {
        return opaqueReader.readArrayToBlockWithOffset(objectPath, buffer, blockSize,
                offset, memoryOffset);
    }

    //
    // Boolean
    //

    @Override
    public IHDF5BooleanReader bool()
    {
        return booleanReader;
    }

    @Override
    public BitSet readBitField(String objectPath) throws HDF5DatatypeInterfaceException
    {
        return booleanReader.readBitField(objectPath);
    }

    @Override
    public BitSet readBitFieldBlock(String objectPath, int blockSize, long blockNumber)
    {
        return booleanReader.readBitFieldBlock(objectPath, blockSize, blockNumber);
    }

    @Override
    public BitSet readBitFieldBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return booleanReader.readBitFieldBlockWithOffset(objectPath, blockSize, offset);
    }

    @Override
    public boolean isBitSetInBitField(String objectPath, int bitIndex)
    {
        return booleanReader.isBitSet(objectPath, bitIndex);
    }

    @Override
    public boolean readBoolean(String objectPath) throws HDF5JavaException
    {
        return booleanReader.read(objectPath);
    }

    //
    // Time & date
    //

    @Override
    public IHDF5DateTimeReader time()
    {
        return dateTimeReader;
    }

    @Override
    public IHDF5TimeDurationReader duration()
    {
        return timeDurationReader;
    }

    @Override
    public long getTimeStampAttribute(String objectPath, String attributeName)
    {
        return dateTimeReader.getAttrAsLong(objectPath, attributeName);
    }

    @Override
    public Date getDateAttribute(String objectPath, String attributeName)
    {
        return dateTimeReader.getAttr(objectPath, attributeName);
    }

    @Override
    public boolean isTimeStamp(String objectPath, String attributeName) throws HDF5JavaException
    {
        return dateTimeReader.isTimeStamp(objectPath, attributeName);
    }

    @Override
    public HDF5TimeDuration getTimeDurationAttribute(String objectPath, String attributeName)
    {
        return timeDurationReader.getAttr(objectPath, attributeName);
    }

    @Override
    public boolean isTimeDuration(String objectPath) throws HDF5JavaException
    {
        return timeDurationReader.isTimeDuration(objectPath);
    }

    @Override
    public boolean isTimeStamp(String objectPath) throws HDF5JavaException
    {
        return dateTimeReader.isTimeStamp(objectPath);
    }

    @Override
    public boolean isTimeDuration(String objectPath, String attributeName) throws HDF5JavaException
    {
        return timeDurationReader.isTimeDuration(objectPath, attributeName);
    }

    @Override
    public HDF5TimeUnit tryGetTimeUnit(String objectPath, String attributeName)
            throws HDF5JavaException
    {
        return timeDurationReader.tryGetTimeUnit(objectPath, attributeName);
    }

    @Override
    public long[] getTimeStampArrayAttribute(String objectPath, String attributeName)
    {
        return dateTimeReader.getArrayAttrAsLong(objectPath, attributeName);
    }

    @Override
    public Date[] getDateArrayAttribute(String objectPath, String attributeName)
    {
        return dateTimeReader.getArrayAttr(objectPath, attributeName);
    }

    @Override
    public HDF5TimeDurationArray getTimeDurationArrayAttribute(String objectPath,
            String attributeName)
    {
        return timeDurationReader.getArrayAttr(objectPath, attributeName);
    }

    @Override
    public HDF5TimeUnit tryGetTimeUnit(String objectPath) throws HDF5JavaException
    {
        return timeDurationReader.tryGetTimeUnit(objectPath);
    }

    @Override
    @Deprecated
    public Iterable<HDF5DataBlock<long[]>> getTimeDurationArrayNaturalBlocks(String dataSetPath,
            HDF5TimeUnit timeUnit) throws HDF5JavaException
    {
        return timeDurationReader.getTimeDurationArrayNaturalBlocks(dataSetPath, timeUnit);
    }

    @Override
    @Deprecated
    public Iterable<HDF5DataBlock<HDF5TimeDuration[]>> getTimeDurationAndUnitArrayNaturalBlocks(
            String objectPath) throws HDF5JavaException
    {
        return timeDurationReader.getTimeDurationAndUnitArrayNaturalBlocks(objectPath);
    }

    @Override
    public Iterable<HDF5DataBlock<long[]>> getTimeStampArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return dateTimeReader.getTimeStampArrayNaturalBlocks(dataSetPath);
    }

    @Override
    public Date readDate(String objectPath) throws HDF5JavaException
    {
        return dateTimeReader.readDate(objectPath);
    }

    @Override
    public Date[] readDateArray(String objectPath) throws HDF5JavaException
    {
        return dateTimeReader.readDateArray(objectPath);
    }

    @Override
    @Deprecated
    public long readTimeDuration(String objectPath, HDF5TimeUnit timeUnit) throws HDF5JavaException
    {
        return timeDurationReader.readTimeDuration(objectPath, timeUnit);
    }

    @Override
    public HDF5TimeDuration readTimeDuration(String objectPath) throws HDF5JavaException
    {
        return timeDurationReader.read(objectPath);
    }

    @Override
    @Deprecated
    public HDF5TimeDuration readTimeDurationAndUnit(String objectPath) throws HDF5JavaException
    {
        return timeDurationReader.readTimeDurationAndUnit(objectPath);
    }

    @Override
    @Deprecated
    public long[] readTimeDurationArray(String objectPath, HDF5TimeUnit timeUnit)
            throws HDF5JavaException
    {
        return timeDurationReader.readTimeDurationArray(objectPath, timeUnit);
    }

    @Override
    public HDF5TimeDurationArray readTimeDurationArray(String objectPath) throws HDF5JavaException
    {
        return timeDurationReader.readArray(objectPath);
    }

    @Override
    @Deprecated
    public HDF5TimeDuration[] readTimeDurationAndUnitArray(String objectPath)
            throws HDF5JavaException
    {
        return timeDurationReader.readTimeDurationAndUnitArray(objectPath);
    }

    @Override
    @Deprecated
    public long[] readTimeDurationArrayBlock(String objectPath, int blockSize, long blockNumber,
            HDF5TimeUnit timeUnit)
    {
        return timeDurationReader.readTimeDurationArrayBlock(objectPath, blockSize, blockNumber,
                timeUnit);
    }

    @Override
    @Deprecated
    public long[] readTimeDurationArrayBlockWithOffset(String objectPath, int blockSize,
            long offset, HDF5TimeUnit timeUnit)
    {
        return timeDurationReader.readTimeDurationArrayBlockWithOffset(objectPath, blockSize, offset,
                timeUnit);
    }

    @Override
    @Deprecated
    public HDF5TimeDuration[] readTimeDurationAndUnitArrayBlock(String objectPath, int blockSize,
            long blockNumber) throws HDF5JavaException
    {
        return timeDurationReader.readTimeDurationAndUnitArrayBlock(objectPath, blockSize, blockNumber);
    }

    @Override
    @Deprecated
    public HDF5TimeDuration[] readTimeDurationAndUnitArrayBlockWithOffset(String objectPath,
            int blockSize, long offset) throws HDF5JavaException
    {
        return timeDurationReader.readTimeDurationAndUnitArrayBlockWithOffset(objectPath, blockSize,
                offset);
    }

    @Override
    public long readTimeStamp(String objectPath) throws HDF5JavaException
    {
        return dateTimeReader.readTimeStamp(objectPath);
    }

    @Override
    public long[] readTimeStampArray(String objectPath) throws HDF5JavaException
    {
        return dateTimeReader.readTimeStampArray(objectPath);
    }

    @Override
    public long[] readTimeStampArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return dateTimeReader.readTimeStampArrayBlock(objectPath, blockSize, blockNumber);
    }

    @Override
    public long[] readTimeStampArrayBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return dateTimeReader.readTimeStampArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    //
    // Reference
    //

    @Override
    public IHDF5ReferenceReader reference()
    {
        return referenceReader;
    }

    @Override
    public String readObjectReference(final String objectPath)
    {
        return referenceReader.read(objectPath);
    }

    @Override
    public String readObjectReference(String objectPath, boolean resolveName)
    {
        return referenceReader.read(objectPath, resolveName);
    }

    @Override
    public String[] readObjectReferenceArrayBlock(String objectPath, int blockSize,
            long blockNumber, boolean resolveName)
    {
        return referenceReader.readArrayBlock(objectPath, blockSize, blockNumber,
                resolveName);
    }

    @Override
    public String[] readObjectReferenceArrayBlockWithOffset(String objectPath, int blockSize,
            long offset, boolean resolveName)
    {
        return referenceReader.readArrayBlockWithOffset(objectPath, blockSize,
                offset, resolveName);
    }

    @Override
    public MDArray<String> readObjectReferenceMDArrayBlock(String objectPath,
            int[] blockDimensions, long[] blockNumber, boolean resolveName)
    {
        return referenceReader.readMDArrayBlock(objectPath, blockDimensions,
                blockNumber, resolveName);
    }

    @Override
    public MDArray<String> readObjectReferenceMDArrayBlockWithOffset(String objectPath,
            int[] blockDimensions, long[] offset, boolean resolveName)
    {
        return referenceReader.readMDArrayBlockWithOffset(objectPath,
                blockDimensions, offset, resolveName);
    }

    @Override
    public Iterable<HDF5DataBlock<String[]>> getObjectReferenceArrayNaturalBlocks(
            String dataSetPath, boolean resolveName)
    {
        return referenceReader.getArrayNaturalBlocks(dataSetPath, resolveName);
    }

    @Override
    public Iterable<HDF5MDDataBlock<MDArray<String>>> getObjectReferenceMDArrayNaturalBlocks(
            String dataSetPath, boolean resolveName)
    {
        return referenceReader.getMDArrayNaturalBlocks(dataSetPath, resolveName);
    }

    @Override
    public String[] readObjectReferenceArray(String objectPath)
    {
        return referenceReader.readArray(objectPath);
    }

    @Override
    public String[] readObjectReferenceArray(String objectPath, boolean resolveName)
    {
        return referenceReader.readArray(objectPath, resolveName);
    }

    @Override
    public MDArray<String> readObjectReferenceMDArray(String objectPath)
    {
        return referenceReader.readMDArray(objectPath);
    }

    @Override
    public MDArray<String> readObjectReferenceMDArray(String objectPath, boolean resolveName)
    {
        return referenceReader.readMDArray(objectPath, resolveName);
    }

    @Override
    public String getObjectReferenceAttribute(String objectPath, String attributeName,
            boolean resolveName)
    {
        return referenceReader.getAttr(objectPath, attributeName, resolveName);
    }

    @Override
    public String[] getObjectReferenceArrayAttribute(String objectPath, String attributeName,
            boolean resolveName)
    {
        return referenceReader.getArrayAttr(objectPath, attributeName,
                resolveName);
    }

    @Override
    public MDArray<String> getObjectReferenceMDArrayAttribute(String objectPath,
            String attributeName, boolean resolveName)
    {
        return referenceReader.getMDArrayAttr(objectPath, attributeName,
                resolveName);
    }

    @Override
    public HDF5TimeDurationArray readTimeDurationArrayBlock(String objectPath, int blockSize,
            long blockNumber) throws HDF5JavaException
    {
        return timeDurationReader.readArrayBlock(objectPath, blockSize, blockNumber);
    }

    @Override
    public HDF5TimeDurationArray readTimeDurationArrayBlockWithOffset(String objectPath,
            int blockSize, long offset) throws HDF5JavaException
    {
        return timeDurationReader.readArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    @Override
    public Iterable<HDF5DataBlock<HDF5TimeDurationArray>> getTimeDurationArrayNaturalBlocks(
            String objectPath) throws HDF5JavaException
    {
        return timeDurationReader.getArrayNaturalBlocks(objectPath);
    }

    //
    // References
    //

    @Override
    public String resolvePath(String reference) throws HDF5JavaException
    {
        return referenceReader.resolvePath(reference);
    }

    @Override
    public String getObjectReferenceAttribute(final String objectPath, final String attributeName)
    {
        return referenceReader.getAttr(objectPath, attributeName);
    }

    @Override
    public String[] getObjectReferenceArrayAttribute(String objectPath, String attributeName)
    {
        return referenceReader.getArrayAttr(objectPath, attributeName);
    }

    @Override
    public MDArray<String> getObjectReferenceMDArrayAttribute(String objectPath,
            String attributeName)
    {
        return referenceReader.getMDArrayAttr(objectPath, attributeName);
    }

    @Override
    public String[] readObjectReferenceArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return referenceReader.readArrayBlock(objectPath, blockSize, blockNumber);
    }

    @Override
    public String[] readObjectReferenceArrayBlockWithOffset(String objectPath, int blockSize,
            long offset)
    {
        return referenceReader.readArrayBlockWithOffset(objectPath, blockSize,
                offset);
    }

    @Override
    public MDArray<String> readObjectReferenceMDArrayBlock(String objectPath,
            int[] blockDimensions, long[] blockNumber)
    {
        return referenceReader.readMDArrayBlock(objectPath, blockDimensions,
                blockNumber);
    }

    @Override
    public MDArray<String> readObjectReferenceMDArrayBlockWithOffset(String objectPath,
            int[] blockDimensions, long[] offset)
    {
        return referenceReader.readMDArrayBlockWithOffset(objectPath,
                blockDimensions, offset);
    }

    @Override
    public Iterable<HDF5DataBlock<String[]>> getObjectReferenceArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return referenceReader.getArrayNaturalBlocks(dataSetPath);
    }

    @Override
    public Iterable<HDF5MDDataBlock<MDArray<String>>> getObjectReferenceMDArrayNaturalBlocks(
            String dataSetPath)
    {
        return referenceReader.getMDArrayNaturalBlocks(dataSetPath);
    }

    //
    // String
    //

    @Override
    public IHDF5StringReader string()
    {
        return stringReader;
    }

    @Override
    public String getStringAttribute(String objectPath, String attributeName)
    {
        return stringReader.getAttr(objectPath, attributeName);
    }

    @Override
    public String[] getStringArrayAttribute(String objectPath, String attributeName)
    {
        return stringReader.getArrayAttr(objectPath, attributeName);
    }

    @Override
    public MDArray<String> getStringMDArrayAttribute(String objectPath, String attributeName)
    {
        return stringReader.getMDArrayAttr(objectPath, attributeName);
    }

    @Override
    public String readString(String objectPath) throws HDF5JavaException
    {
        return stringReader.read(objectPath);
    }

    @Override
    public String[] readStringArray(String objectPath) throws HDF5JavaException
    {
        return stringReader.readArray(objectPath);
    }

    @Override
    public String[] readStringArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return stringReader.readArrayBlock(objectPath, blockSize, blockNumber);
    }

    @Override
    public String[] readStringArrayBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return stringReader.readArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    @Override
    public MDArray<String> readStringMDArray(String objectPath)
    {
        return stringReader.readMDArray(objectPath);
    }

    @Override
    public MDArray<String> readStringMDArrayBlock(String objectPath, int[] blockDimensions,
            long[] blockNumber)
    {
        return stringReader.readMDArrayBlock(objectPath, blockDimensions, blockNumber);
    }

    @Override
    public MDArray<String> readStringMDArrayBlockWithOffset(String objectPath,
            int[] blockDimensions, long[] offset)
    {
        return stringReader.readMDArrayBlockWithOffset(objectPath, blockDimensions, offset);
    }

    @Override
    public Iterable<HDF5DataBlock<String[]>> getStringArrayNaturalBlocks(String objectPath)
            throws HDF5JavaException
    {
        return stringReader.getArrayNaturalBlocks(objectPath);
    }

    @Override
    public Iterable<HDF5MDDataBlock<MDArray<String>>> getStringMDArrayNaturalBlocks(
            String objectPath)
    {
        return stringReader.getMDArrayNaturalBlocks(objectPath);
    }

    //
    // Enums
    //

    @Override
    public IHDF5EnumReader enums()
    {
        return enumReader;
    }

    @Override
    public IHDF5EnumReader enumeration()
    {
        return enumReader;
    }

    @Override
    public Iterable<HDF5DataBlock<HDF5EnumerationValueArray>> getEnumArrayNaturalBlocks(
            String objectPath, HDF5EnumerationType enumType) throws HDF5JavaException
    {
        return enumReader.getArrayBlocks(objectPath, enumType);
    }

    @Override
    public Iterable<HDF5DataBlock<HDF5EnumerationValueArray>> getEnumArrayNaturalBlocks(
            String objectPath) throws HDF5JavaException
    {
        return enumReader.getArrayBlocks(objectPath);
    }

    @Override
    public HDF5EnumerationValue readEnum(String objectPath, HDF5EnumerationType enumType)
            throws HDF5JavaException
    {
        return enumReader.read(objectPath, enumType);
    }

    @Override
    public HDF5EnumerationValue readEnum(String objectPath) throws HDF5JavaException
    {
        return enumReader.read(objectPath);
    }

    @Override
    public <T extends Enum<T>> T readEnum(String objectPath, Class<T> enumClass)
            throws HDF5JavaException
    {
        return enumReader.read(objectPath, enumClass);
    }

    @Override
    public HDF5EnumerationValueArray readEnumArray(String objectPath, HDF5EnumerationType enumType)
            throws HDF5JavaException
    {
        return enumReader.readArray(objectPath, enumType);
    }

    @Override
    public HDF5EnumerationValueArray readEnumArray(String objectPath) throws HDF5JavaException
    {
        return enumReader.readArray(objectPath);
    }

    @Override
    public <T extends Enum<T>> T[] readEnumArray(String objectPath, Class<T> enumClass)
            throws HDF5JavaException
    {
        return readEnumArray(objectPath).toEnumArray(enumClass);
    }

    @Override
    public String[] readEnumArrayAsString(String objectPath) throws HDF5JavaException
    {
        return enumReader.readArray(objectPath).toStringArray();
    }

    @Override
    public HDF5EnumerationValueArray readEnumArrayBlock(String objectPath,
            HDF5EnumerationType enumType, int blockSize, long blockNumber)
    {
        return enumReader.readArrayBlock(objectPath, enumType, blockSize, blockNumber);
    }

    @Override
    public HDF5EnumerationValueArray readEnumArrayBlock(String objectPath, int blockSize,
            long blockNumber)
    {
        return enumReader.readArrayBlock(objectPath, blockSize, blockNumber);
    }

    @Override
    public HDF5EnumerationValueArray readEnumArrayBlockWithOffset(String objectPath,
            HDF5EnumerationType enumType, int blockSize, long offset)
    {
        return enumReader.readArrayBlockWithOffset(objectPath, enumType, blockSize, offset);
    }

    @Override
    public HDF5EnumerationValueArray readEnumArrayBlockWithOffset(String objectPath, int blockSize,
            long offset)
    {
        return enumReader.readArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    @Override
    public String readEnumAsString(String objectPath) throws HDF5JavaException
    {
        return enumReader.readAsString(objectPath);
    }

    //
    // Compounds
    //

    @Override
    public IHDF5CompoundReader compounds()
    {
        return compoundReader;
    }

    @Override
    public IHDF5CompoundReader compound()
    {
        return compoundReader;
    }

    @Override
    public <T> Iterable<HDF5DataBlock<T[]>> getCompoundArrayNaturalBlocks(String objectPath,
            HDF5CompoundType<T> type, IByteArrayInspector inspectorOrNull) throws HDF5JavaException
    {
        return compoundReader.getArrayBlocks(objectPath, type, inspectorOrNull);
    }

    @Override
    public <T> Iterable<HDF5DataBlock<T[]>> getCompoundArrayNaturalBlocks(String objectPath,
            HDF5CompoundType<T> type) throws HDF5JavaException
    {
        return compoundReader.getArrayBlocks(objectPath, type);
    }

    @Override
    public <T> Iterable<HDF5MDDataBlock<MDArray<T>>> getCompoundMDArrayNaturalBlocks(
            String objectPath, HDF5CompoundType<T> type, IByteArrayInspector inspectorOrNull)
            throws HDF5JavaException
    {
        return compoundReader.getMDArrayBlocks(objectPath, type, inspectorOrNull);
    }

    @Override
    public <T> Iterable<HDF5MDDataBlock<MDArray<T>>> getCompoundMDArrayNaturalBlocks(
            String objectPath, HDF5CompoundType<T> type) throws HDF5JavaException
    {
        return compoundReader.getMDArrayBlocks(objectPath, type);
    }

    @Override
    public <T> Iterable<HDF5DataBlock<T[]>> getCompoundArrayNaturalBlocks(String objectPath,
            Class<T> pojoClass) throws HDF5JavaException
    {
        return compoundReader.getArrayBlocks(objectPath, pojoClass);
    }

    @Override
    public <T> Iterable<HDF5MDDataBlock<MDArray<T>>> getCompoundMDArrayNaturalBlocks(
            String objectPath, Class<T> pojoClass) throws HDF5JavaException
    {
        return compoundReader.getMDArrayBlocks(objectPath, pojoClass);
    }

    @Override
    public HDF5CompoundMemberInformation[] getCompoundDataSetInformation(String dataSetPath,
            boolean sortAlphabetically) throws HDF5JavaException
    {
        final HDF5CompoundMemberInformation[] compoundInformation =
                compoundReader.getDataSetInfo(dataSetPath, DataTypeInfoOptions.DEFAULT);
        if (sortAlphabetically)
        {
            Arrays.sort(compoundInformation);
        }
        return compoundInformation;
    }

    @Override
    public HDF5CompoundMemberInformation[] getCompoundDataSetInformation(String dataSetPath)
            throws HDF5JavaException
    {
        return compoundReader.getDataSetInfo(dataSetPath);
    }

    @Override
    public <T> HDF5CompoundMemberInformation[] getCompoundMemberInformation(Class<T> compoundClass)
    {
        return compoundReader.getMemberInfo(compoundClass);
    }

    @Override
    public HDF5CompoundMemberInformation[] getCompoundMemberInformation(String dataTypeName)
    {
        return compoundReader.getMemberInfo(dataTypeName);
    }

    @Override
    public <T> HDF5CompoundType<T> getCompoundType(Class<T> pojoClass,
            HDF5CompoundMemberMapping... members)
    {
        return compoundReader.getType(pojoClass, members);
    }

    @Override
    public <T> HDF5CompoundType<T> getCompoundType(String name, Class<T> compoundType,
            HDF5CompoundMemberMapping... members)
    {
        return compoundReader.getType(name, compoundType, members);
    }

    @Override
    public <T> HDF5CompoundType<T> getDataSetCompoundType(String objectPath, Class<T> compoundClass)
    {
        return compoundReader.getDataSetType(objectPath, compoundClass);
    }

    @Override
    public <T> HDF5CompoundType<T> getAttributeCompoundType(String objectPath,
            String attributeName, Class<T> pojoClass)
    {
        return compoundReader.getAttributeType(objectPath, attributeName, pojoClass);
    }

    @Override
    public <T> HDF5CompoundType<T> getInferredCompoundType(Class<T> pojoClass)
    {
        return compoundReader.getInferredType(pojoClass);
    }

    @Override
    public <T> HDF5CompoundType<T> getInferredCompoundType(String name, Class<T> compoundType)
    {
        return compoundReader.getInferredType(name, compoundType);
    }

    @Override
    public <T> HDF5CompoundType<T> getInferredCompoundType(String name, T template)
    {
        return compoundReader.getInferredType(name, template);
    }

    @Override
    public <T> HDF5CompoundType<T> getInferredCompoundType(T template)
    {
        return compoundReader.getInferredType(template);
    }

    @Override
    public <T> HDF5CompoundType<T> getInferredCompoundType(String name, T template,
            HDF5CompoundMappingHints hints)
    {
        return compoundReader.getInferredType(name, template, hints);
    }

    @Override
    public HDF5CompoundType<List<?>> getInferredCompoundType(String name, List<String> memberNames,
            List<?> template)
    {
        return compoundReader.getInferredType(name, memberNames, template);
    }

    @Override
    public HDF5CompoundType<Object[]> getInferredCompoundType(String name, String[] memberNames,
            Object[] template)
    {
        return compoundReader.getInferredType(name, memberNames, template);
    }

    @Override
    public HDF5CompoundType<List<?>> getInferredCompoundType(List<String> memberNames,
            List<?> template)
    {
        return compoundReader.getInferredType(memberNames, template);
    }

    @Override
    public HDF5CompoundType<Object[]> getInferredCompoundType(String[] memberNames,
            Object[] template)
    {
        return compoundReader.getInferredType(memberNames, template);
    }

    @Override
    public <T> HDF5CompoundType<T> getNamedCompoundType(Class<T> compoundClass)
    {
        return compoundReader.getNamedType(compoundClass);
    }

    @Override
    public <T> HDF5CompoundType<T> getNamedCompoundType(String dataTypeName, Class<T> compoundClass)
    {
        return compoundReader.getNamedType(dataTypeName, compoundClass);
    }

    @Override
    public <T> T readCompound(String objectPath, HDF5CompoundType<T> type,
            IByteArrayInspector inspectorOrNull) throws HDF5JavaException
    {
        return compoundReader.read(objectPath, type, inspectorOrNull);
    }

    @Override
    public <T> T readCompound(String objectPath, HDF5CompoundType<T> type) throws HDF5JavaException
    {
        return compoundReader.read(objectPath, type);
    }

    @Override
    public <T> T readCompound(String objectPath, Class<T> pojoClass) throws HDF5JavaException
    {
        return compoundReader.read(objectPath, pojoClass);
    }

    @Override
    public <T> T[] readCompoundArray(String objectPath, HDF5CompoundType<T> type,
            IByteArrayInspector inspectorOrNull) throws HDF5JavaException
    {
        return compoundReader.readArray(objectPath, type, inspectorOrNull);
    }

    @Override
    public <T> T[] readCompoundArray(String objectPath, HDF5CompoundType<T> type)
            throws HDF5JavaException
    {
        return compoundReader.readArray(objectPath, type);
    }

    @Override
    public <T> T[] readCompoundArray(String objectPath, Class<T> pojoClass)
            throws HDF5JavaException
    {
        return compoundReader.readArray(objectPath, pojoClass);
    }

    @Override
    public <T> T[] readCompoundArrayBlock(String objectPath, HDF5CompoundType<T> type,
            int blockSize, long blockNumber, IByteArrayInspector inspectorOrNull)
            throws HDF5JavaException
    {
        return compoundReader.readArrayBlock(objectPath, type, blockSize, blockNumber,
                inspectorOrNull);
    }

    @Override
    public <T> T[] readCompoundArrayBlock(String objectPath, HDF5CompoundType<T> type,
            int blockSize, long blockNumber) throws HDF5JavaException
    {
        return compoundReader.readArrayBlock(objectPath, type, blockSize, blockNumber);
    }

    @Override
    public <T> T[] readCompoundArrayBlockWithOffset(String objectPath, HDF5CompoundType<T> type,
            int blockSize, long offset, IByteArrayInspector inspectorOrNull)
            throws HDF5JavaException
    {
        return compoundReader.readArrayBlockWithOffset(objectPath, type, blockSize, offset,
                inspectorOrNull);
    }

    @Override
    public <T> T[] readCompoundArrayBlockWithOffset(String objectPath, HDF5CompoundType<T> type,
            int blockSize, long offset) throws HDF5JavaException
    {
        return compoundReader.readArrayBlockWithOffset(objectPath, type, blockSize, offset);
    }

    @Override
    public <T> MDArray<T> readCompoundMDArray(String objectPath, HDF5CompoundType<T> type,
            IByteArrayInspector inspectorOrNull) throws HDF5JavaException
    {
        return compoundReader.readMDArray(objectPath, type, inspectorOrNull);
    }

    @Override
    public <T> MDArray<T> readCompoundMDArray(String objectPath, HDF5CompoundType<T> type)
            throws HDF5JavaException
    {
        return compoundReader.readMDArray(objectPath, type);
    }

    @Override
    public <T> MDArray<T> readCompoundMDArray(String objectPath, Class<T> pojoClass)
            throws HDF5JavaException
    {
        return compoundReader.readMDArray(objectPath, pojoClass);
    }

    @Override
    public <T> MDArray<T> readCompoundMDArrayBlock(String objectPath, HDF5CompoundType<T> type,
            int[] blockDimensions, long[] blockNumber, IByteArrayInspector inspectorOrNull)
            throws HDF5JavaException
    {
        return compoundReader.readMDArrayBlock(objectPath, type, blockDimensions, blockNumber,
                inspectorOrNull);
    }

    @Override
    public <T> MDArray<T> readCompoundMDArrayBlock(String objectPath, HDF5CompoundType<T> type,
            int[] blockDimensions, long[] blockNumber) throws HDF5JavaException
    {
        return compoundReader.readMDArrayBlock(objectPath, type, blockDimensions, blockNumber);
    }

    @Override
    public <T> MDArray<T> readCompoundMDArrayBlockWithOffset(String objectPath,
            HDF5CompoundType<T> type, int[] blockDimensions, long[] offset,
            IByteArrayInspector inspectorOrNull) throws HDF5JavaException
    {
        return compoundReader.readMDArrayBlockWithOffset(objectPath, type, blockDimensions, offset,
                inspectorOrNull);
    }

    @Override
    public <T> MDArray<T> readCompoundMDArrayBlockWithOffset(String objectPath,
            HDF5CompoundType<T> type, int[] blockDimensions, long[] offset)
            throws HDF5JavaException
    {
        return compoundReader.readMDArrayBlockWithOffset(objectPath, type, blockDimensions, offset);
    }

    // ------------------------------------------------------------------------------
    // Primite types - START
    // ------------------------------------------------------------------------------

    @Override
    public byte[] getByteArrayAttribute(String objectPath, String attributeName)
    {
        return byteReader.getArrayAttr(objectPath, attributeName);
    }

    @Override
    public Iterable<HDF5DataBlock<byte[]>> getByteArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return byteReader.getArrayNaturalBlocks(dataSetPath);
    }

    @Override
    public byte getByteAttribute(String objectPath, String attributeName)
    {
        return byteReader.getAttr(objectPath, attributeName);
    }

    @Override
    public MDByteArray getByteMDArrayAttribute(String objectPath, String attributeName)
    {
        return byteReader.getMDArrayAttr(objectPath, attributeName);
    }

    @Override
    public Iterable<HDF5MDDataBlock<MDByteArray>> getByteMDArrayNaturalBlocks(String dataSetPath)
    {
        return byteReader.getMDArrayNaturalBlocks(dataSetPath);
    }

    @Override
    public byte[][] getByteMatrixAttribute(String objectPath, String attributeName)
            throws HDF5JavaException
    {
        return byteReader.getMatrixAttr(objectPath, attributeName);
    }

    @Override
    public byte readByte(String objectPath)
    {
        return byteReader.read(objectPath);
    }

    @Override
    public byte[] readByteArray(String objectPath)
    {
        return byteReader.readArray(objectPath);
    }

    @Override
    public byte[] readByteArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return byteReader.readArrayBlock(objectPath, blockSize, blockNumber);
    }

    @Override
    public byte[] readByteArrayBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return byteReader.readArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    @Override
    public MDByteArray readByteMDArray(String objectPath)
    {
        return byteReader.readMDArray(objectPath);
    }

    @Override
    public MDByteArray readByteMDArrayBlock(String objectPath, int[] blockDimensions,
            long[] blockNumber)
    {
        return byteReader.readMDArrayBlock(objectPath, blockDimensions, blockNumber);
    }

    @Override
    public MDByteArray readByteMDArrayBlockWithOffset(String objectPath, int[] blockDimensions,
            long[] offset)
    {
        return byteReader.readMDArrayBlockWithOffset(objectPath, blockDimensions, offset);
    }

    @Override
    public byte[][] readByteMatrix(String objectPath) throws HDF5JavaException
    {
        return byteReader.readMatrix(objectPath);
    }

    @Override
    public byte[][] readByteMatrixBlock(String objectPath, int blockSizeX, int blockSizeY,
            long blockNumberX, long blockNumberY) throws HDF5JavaException
    {
        return byteReader.readMatrixBlock(objectPath, blockSizeX, blockSizeY, blockNumberX,
                blockNumberY);
    }

    @Override
    public byte[][] readByteMatrixBlockWithOffset(String objectPath, int blockSizeX,
            int blockSizeY, long offsetX, long offsetY) throws HDF5JavaException
    {
        return byteReader.readMatrixBlockWithOffset(objectPath, blockSizeX, blockSizeY,
                offsetX, offsetY);
    }

    @Override
    public int[] readToByteMDArrayBlockWithOffset(String objectPath, MDByteArray array,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        return byteReader.readToMDArrayBlockWithOffset(objectPath, array, blockDimensions,
                offset, memoryOffset);
    }

    @Override
    public int[] readToByteMDArrayWithOffset(String objectPath, MDByteArray array,
            int[] memoryOffset)
    {
        return byteReader.readToMDArrayWithOffset(objectPath, array, memoryOffset);
    }

    @Override
    public double[] getDoubleArrayAttribute(String objectPath, String attributeName)
    {
        return doubleReader.getArrayAttr(objectPath, attributeName);
    }

    @Override
    public Iterable<HDF5DataBlock<double[]>> getDoubleArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return doubleReader.getArrayNaturalBlocks(dataSetPath);
    }

    @Override
    public double getDoubleAttribute(String objectPath, String attributeName)
    {
        return doubleReader.getAttr(objectPath, attributeName);
    }

    @Override
    public MDDoubleArray getDoubleMDArrayAttribute(String objectPath, String attributeName)
    {
        return doubleReader.getMDArrayAttr(objectPath, attributeName);
    }

    @Override
    public Iterable<HDF5MDDataBlock<MDDoubleArray>> getDoubleMDArrayNaturalBlocks(String dataSetPath)
    {
        return doubleReader.getMDArrayNaturalBlocks(dataSetPath);
    }

    @Override
    public double[][] getDoubleMatrixAttribute(String objectPath, String attributeName)
            throws HDF5JavaException
    {
        return doubleReader.getMatrixAttr(objectPath, attributeName);
    }

    @Override
    public double readDouble(String objectPath)
    {
        return doubleReader.read(objectPath);
    }

    @Override
    public double[] readDoubleArray(String objectPath)
    {
        return doubleReader.readArray(objectPath);
    }

    @Override
    public double[] readDoubleArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return doubleReader.readArrayBlock(objectPath, blockSize, blockNumber);
    }

    @Override
    public double[] readDoubleArrayBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return doubleReader.readArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    @Override
    public MDDoubleArray readDoubleMDArray(String objectPath)
    {
        return doubleReader.readMDArray(objectPath);
    }

    @Override
    public MDDoubleArray readDoubleMDArrayBlock(String objectPath, int[] blockDimensions,
            long[] blockNumber)
    {
        return doubleReader.readMDArrayBlock(objectPath, blockDimensions, blockNumber);
    }

    @Override
    public MDDoubleArray readDoubleMDArrayBlockWithOffset(String objectPath, int[] blockDimensions,
            long[] offset)
    {
        return doubleReader.readMDArrayBlockWithOffset(objectPath, blockDimensions, offset);
    }

    @Override
    public double[][] readDoubleMatrix(String objectPath) throws HDF5JavaException
    {
        return doubleReader.readMatrix(objectPath);
    }

    @Override
    public double[][] readDoubleMatrixBlock(String objectPath, int blockSizeX, int blockSizeY,
            long blockNumberX, long blockNumberY) throws HDF5JavaException
    {
        return doubleReader.readMatrixBlock(objectPath, blockSizeX, blockSizeY, blockNumberX,
                blockNumberY);
    }

    @Override
    public double[][] readDoubleMatrixBlockWithOffset(String objectPath, int blockSizeX,
            int blockSizeY, long offsetX, long offsetY) throws HDF5JavaException
    {
        return doubleReader.readMatrixBlockWithOffset(objectPath, blockSizeX, blockSizeY,
                offsetX, offsetY);
    }

    @Override
    public int[] readToDoubleMDArrayBlockWithOffset(String objectPath, MDDoubleArray array,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        return doubleReader.readToMDArrayBlockWithOffset(objectPath, array, blockDimensions,
                offset, memoryOffset);
    }

    @Override
    public int[] readToDoubleMDArrayWithOffset(String objectPath, MDDoubleArray array,
            int[] memoryOffset)
    {
        return doubleReader.readToMDArrayWithOffset(objectPath, array, memoryOffset);
    }

    @Override
    public float[] getFloatArrayAttribute(String objectPath, String attributeName)
    {
        return floatReader.getArrayAttr(objectPath, attributeName);
    }

    @Override
    public Iterable<HDF5DataBlock<float[]>> getFloatArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return floatReader.getArrayNaturalBlocks(dataSetPath);
    }

    @Override
    public float getFloatAttribute(String objectPath, String attributeName)
    {
        return floatReader.getAttr(objectPath, attributeName);
    }

    @Override
    public MDFloatArray getFloatMDArrayAttribute(String objectPath, String attributeName)
    {
        return floatReader.getMDArrayAttr(objectPath, attributeName);
    }

    @Override
    public Iterable<HDF5MDDataBlock<MDFloatArray>> getFloatMDArrayNaturalBlocks(String dataSetPath)
    {
        return floatReader.getMDArrayNaturalBlocks(dataSetPath);
    }

    @Override
    public float[][] getFloatMatrixAttribute(String objectPath, String attributeName)
            throws HDF5JavaException
    {
        return floatReader.getMatrixAttr(objectPath, attributeName);
    }

    @Override
    public float readFloat(String objectPath)
    {
        return floatReader.read(objectPath);
    }

    @Override
    public float[] readFloatArray(String objectPath)
    {
        return floatReader.readArray(objectPath);
    }

    @Override
    public float[] readFloatArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return floatReader.readArrayBlock(objectPath, blockSize, blockNumber);
    }

    @Override
    public float[] readFloatArrayBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return floatReader.readArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    @Override
    public MDFloatArray readFloatMDArray(String objectPath)
    {
        return floatReader.readMDArray(objectPath);
    }

    @Override
    public MDFloatArray readFloatMDArrayBlock(String objectPath, int[] blockDimensions,
            long[] blockNumber)
    {
        return floatReader.readMDArrayBlock(objectPath, blockDimensions, blockNumber);
    }

    @Override
    public MDFloatArray readFloatMDArrayBlockWithOffset(String objectPath, int[] blockDimensions,
            long[] offset)
    {
        return floatReader.readMDArrayBlockWithOffset(objectPath, blockDimensions, offset);
    }

    @Override
    public float[][] readFloatMatrix(String objectPath) throws HDF5JavaException
    {
        return floatReader.readMatrix(objectPath);
    }

    @Override
    public float[][] readFloatMatrixBlock(String objectPath, int blockSizeX, int blockSizeY,
            long blockNumberX, long blockNumberY) throws HDF5JavaException
    {
        return floatReader.readMatrixBlock(objectPath, blockSizeX, blockSizeY, blockNumberX,
                blockNumberY);
    }

    @Override
    public float[][] readFloatMatrixBlockWithOffset(String objectPath, int blockSizeX,
            int blockSizeY, long offsetX, long offsetY) throws HDF5JavaException
    {
        return floatReader.readMatrixBlockWithOffset(objectPath, blockSizeX, blockSizeY,
                offsetX, offsetY);
    }

    @Override
    public int[] readToFloatMDArrayBlockWithOffset(String objectPath, MDFloatArray array,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        return floatReader.readToMDArrayBlockWithOffset(objectPath, array, blockDimensions,
                offset, memoryOffset);
    }

    @Override
    public int[] readToFloatMDArrayWithOffset(String objectPath, MDFloatArray array,
            int[] memoryOffset)
    {
        return floatReader.readToMDArrayWithOffset(objectPath, array, memoryOffset);
    }

    @Override
    public int[] getIntArrayAttribute(String objectPath, String attributeName)
    {
        return intReader.getArrayAttr(objectPath, attributeName);
    }

    @Override
    public Iterable<HDF5DataBlock<int[]>> getIntArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return intReader.getArrayNaturalBlocks(dataSetPath);
    }

    @Override
    public int getIntAttribute(String objectPath, String attributeName)
    {
        return intReader.getAttr(objectPath, attributeName);
    }

    @Override
    public MDIntArray getIntMDArrayAttribute(String objectPath, String attributeName)
    {
        return intReader.getMDArrayAttr(objectPath, attributeName);
    }

    @Override
    public Iterable<HDF5MDDataBlock<MDIntArray>> getIntMDArrayNaturalBlocks(String dataSetPath)
    {
        return intReader.getMDArrayNaturalBlocks(dataSetPath);
    }

    @Override
    public int[][] getIntMatrixAttribute(String objectPath, String attributeName)
            throws HDF5JavaException
    {
        return intReader.getMatrixAttr(objectPath, attributeName);
    }

    @Override
    public int readInt(String objectPath)
    {
        return intReader.read(objectPath);
    }

    @Override
    public int[] readIntArray(String objectPath)
    {
        return intReader.readArray(objectPath);
    }

    @Override
    public int[] readIntArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return intReader.readArrayBlock(objectPath, blockSize, blockNumber);
    }

    @Override
    public int[] readIntArrayBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return intReader.readArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    @Override
    public MDIntArray readIntMDArray(String objectPath)
    {
        return intReader.readMDArray(objectPath);
    }

    @Override
    public MDIntArray readIntMDArrayBlock(String objectPath, int[] blockDimensions,
            long[] blockNumber)
    {
        return intReader.readMDArrayBlock(objectPath, blockDimensions, blockNumber);
    }

    @Override
    public MDIntArray readIntMDArrayBlockWithOffset(String objectPath, int[] blockDimensions,
            long[] offset)
    {
        return intReader.readMDArrayBlockWithOffset(objectPath, blockDimensions, offset);
    }

    @Override
    public int[][] readIntMatrix(String objectPath) throws HDF5JavaException
    {
        return intReader.readMatrix(objectPath);
    }

    @Override
    public int[][] readIntMatrixBlock(String objectPath, int blockSizeX, int blockSizeY,
            long blockNumberX, long blockNumberY) throws HDF5JavaException
    {
        return intReader.readMatrixBlock(objectPath, blockSizeX, blockSizeY, blockNumberX,
                blockNumberY);
    }

    @Override
    public int[][] readIntMatrixBlockWithOffset(String objectPath, int blockSizeX, int blockSizeY,
            long offsetX, long offsetY) throws HDF5JavaException
    {
        return intReader.readMatrixBlockWithOffset(objectPath, blockSizeX, blockSizeY, offsetX,
                offsetY);
    }

    @Override
    public int[] readToIntMDArrayBlockWithOffset(String objectPath, MDIntArray array,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        return intReader.readToMDArrayBlockWithOffset(objectPath, array, blockDimensions,
                offset, memoryOffset);
    }

    @Override
    public int[] readToIntMDArrayWithOffset(String objectPath, MDIntArray array, int[] memoryOffset)
    {
        return intReader.readToMDArrayWithOffset(objectPath, array, memoryOffset);
    }

    @Override
    public long[] getLongArrayAttribute(String objectPath, String attributeName)
    {
        return longReader.getArrayAttr(objectPath, attributeName);
    }

    @Override
    public Iterable<HDF5DataBlock<long[]>> getLongArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return longReader.getArrayNaturalBlocks(dataSetPath);
    }

    @Override
    public long getLongAttribute(String objectPath, String attributeName)
    {
        return longReader.getAttr(objectPath, attributeName);
    }

    @Override
    public MDLongArray getLongMDArrayAttribute(String objectPath, String attributeName)
    {
        return longReader.getMDArrayAttr(objectPath, attributeName);
    }

    @Override
    public Iterable<HDF5MDDataBlock<MDLongArray>> getLongMDArrayNaturalBlocks(String dataSetPath)
    {
        return longReader.getMDArrayNaturalBlocks(dataSetPath);
    }

    @Override
    public long[][] getLongMatrixAttribute(String objectPath, String attributeName)
            throws HDF5JavaException
    {
        return longReader.getMatrixAttr(objectPath, attributeName);
    }

    @Override
    public long readLong(String objectPath)
    {
        return longReader.read(objectPath);
    }

    @Override
    public long[] readLongArray(String objectPath)
    {
        return longReader.readArray(objectPath);
    }

    @Override
    public long[] readLongArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return longReader.readArrayBlock(objectPath, blockSize, blockNumber);
    }

    @Override
    public long[] readLongArrayBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return longReader.readArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    @Override
    public MDLongArray readLongMDArray(String objectPath)
    {
        return longReader.readMDArray(objectPath);
    }

    @Override
    public MDLongArray readLongMDArrayBlock(String objectPath, int[] blockDimensions,
            long[] blockNumber)
    {
        return longReader.readMDArrayBlock(objectPath, blockDimensions, blockNumber);
    }

    @Override
    public MDLongArray readLongMDArrayBlockWithOffset(String objectPath, int[] blockDimensions,
            long[] offset)
    {
        return longReader.readMDArrayBlockWithOffset(objectPath, blockDimensions, offset);
    }

    @Override
    public long[][] readLongMatrix(String objectPath) throws HDF5JavaException
    {
        return longReader.readMatrix(objectPath);
    }

    @Override
    public long[][] readLongMatrixBlock(String objectPath, int blockSizeX, int blockSizeY,
            long blockNumberX, long blockNumberY) throws HDF5JavaException
    {
        return longReader.readMatrixBlock(objectPath, blockSizeX, blockSizeY, blockNumberX,
                blockNumberY);
    }

    @Override
    public long[][] readLongMatrixBlockWithOffset(String objectPath, int blockSizeX,
            int blockSizeY, long offsetX, long offsetY) throws HDF5JavaException
    {
        return longReader.readMatrixBlockWithOffset(objectPath, blockSizeX, blockSizeY,
                offsetX, offsetY);
    }

    @Override
    public int[] readToLongMDArrayBlockWithOffset(String objectPath, MDLongArray array,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        return longReader.readToMDArrayBlockWithOffset(objectPath, array, blockDimensions,
                offset, memoryOffset);
    }

    @Override
    public int[] readToLongMDArrayWithOffset(String objectPath, MDLongArray array,
            int[] memoryOffset)
    {
        return longReader.readToMDArrayWithOffset(objectPath, array, memoryOffset);
    }

    @Override
    public short[] getShortArrayAttribute(String objectPath, String attributeName)
    {
        return shortReader.getArrayAttr(objectPath, attributeName);
    }

    @Override
    public Iterable<HDF5DataBlock<short[]>> getShortArrayNaturalBlocks(String dataSetPath)
            throws HDF5JavaException
    {
        return shortReader.getArrayNaturalBlocks(dataSetPath);
    }

    @Override
    public short getShortAttribute(String objectPath, String attributeName)
    {
        return shortReader.getAttr(objectPath, attributeName);
    }

    @Override
    public MDShortArray getShortMDArrayAttribute(String objectPath, String attributeName)
    {
        return shortReader.getMDArrayAttr(objectPath, attributeName);
    }

    @Override
    public Iterable<HDF5MDDataBlock<MDShortArray>> getShortMDArrayNaturalBlocks(String dataSetPath)
    {
        return shortReader.getMDArrayNaturalBlocks(dataSetPath);
    }

    @Override
    public short[][] getShortMatrixAttribute(String objectPath, String attributeName)
            throws HDF5JavaException
    {
        return shortReader.getMatrixAttr(objectPath, attributeName);
    }

    @Override
    public short readShort(String objectPath)
    {
        return shortReader.read(objectPath);
    }

    @Override
    public short[] readShortArray(String objectPath)
    {
        return shortReader.readArray(objectPath);
    }

    @Override
    public short[] readShortArrayBlock(String objectPath, int blockSize, long blockNumber)
    {
        return shortReader.readArrayBlock(objectPath, blockSize, blockNumber);
    }

    @Override
    public short[] readShortArrayBlockWithOffset(String objectPath, int blockSize, long offset)
    {
        return shortReader.readArrayBlockWithOffset(objectPath, blockSize, offset);
    }

    @Override
    public MDShortArray readShortMDArray(String objectPath)
    {
        return shortReader.readMDArray(objectPath);
    }

    @Override
    public MDShortArray readShortMDArrayBlock(String objectPath, int[] blockDimensions,
            long[] blockNumber)
    {
        return shortReader.readMDArrayBlock(objectPath, blockDimensions, blockNumber);
    }

    @Override
    public MDShortArray readShortMDArrayBlockWithOffset(String objectPath, int[] blockDimensions,
            long[] offset)
    {
        return shortReader.readMDArrayBlockWithOffset(objectPath, blockDimensions, offset);
    }

    @Override
    public short[][] readShortMatrix(String objectPath) throws HDF5JavaException
    {
        return shortReader.readMatrix(objectPath);
    }

    @Override
    public short[][] readShortMatrixBlock(String objectPath, int blockSizeX, int blockSizeY,
            long blockNumberX, long blockNumberY) throws HDF5JavaException
    {
        return shortReader.readMatrixBlock(objectPath, blockSizeX, blockSizeY, blockNumberX,
                blockNumberY);
    }

    @Override
    public short[][] readShortMatrixBlockWithOffset(String objectPath, int blockSizeX,
            int blockSizeY, long offsetX, long offsetY) throws HDF5JavaException
    {
        return shortReader.readMatrixBlockWithOffset(objectPath, blockSizeX, blockSizeY,
                offsetX, offsetY);
    }

    @Override
    public int[] readToShortMDArrayBlockWithOffset(String objectPath, MDShortArray array,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        return shortReader.readToMDArrayBlockWithOffset(objectPath, array, blockDimensions,
                offset, memoryOffset);
    }

    @Override
    public int[] readToShortMDArrayWithOffset(String objectPath, MDShortArray array,
            int[] memoryOffset)
    {
        return shortReader.readToMDArrayWithOffset(objectPath, array, memoryOffset);
    }

    @Override
    public IHDF5ByteReader int8()
    {
        return byteReader;
    }

    @Override
    public IHDF5ByteReader uint8()
    {
        return ubyteReader;
    }

    @Override
    public IHDF5ShortReader int16()
    {
        return shortReader;
    }

    @Override
    public IHDF5ShortReader uint16()
    {
        return ushortReader;
    }

    @Override
    public IHDF5IntReader int32()
    {
        return intReader;
    }

    @Override
    public IHDF5IntReader uint32()
    {
        return uintReader;
    }

    @Override
    public IHDF5LongReader int64()
    {
        return longReader;
    }

    @Override
    public IHDF5LongReader uint64()
    {
        return ulongReader;
    }

    @Override
    public IHDF5FloatReader float32()
    {
        return floatReader;
    }

    @Override
    public IHDF5DoubleReader float64()
    {
        return doubleReader;
    }

    // ------------------------------------------------------------------------------
    // Primitive types - END
    // ------------------------------------------------------------------------------

}
