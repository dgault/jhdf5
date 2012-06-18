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

import static ch.systemsx.cisd.hdf5.HDF5Utils.createObjectTypeVariantAttributeName;
import static ch.systemsx.cisd.hdf5.HDF5Utils.createAttributeTypeVariantAttributeName;

import java.io.Flushable;
import java.util.BitSet;
import java.util.Date;
import java.util.List;

import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;

import ch.systemsx.cisd.base.mdarray.MDArray;
import ch.systemsx.cisd.base.mdarray.MDByteArray;
import ch.systemsx.cisd.base.mdarray.MDDoubleArray;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.base.mdarray.MDIntArray;
import ch.systemsx.cisd.base.mdarray.MDLongArray;
import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.IHDF5CompoundInformationRetriever.IByteArrayInspector;
import ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator.FileFormat;
import ch.systemsx.cisd.hdf5.cleanup.ICallableWithCleanUp;
import ch.systemsx.cisd.hdf5.cleanup.ICleanUpRegistry;

/**
 * A class for writing HDF5 files (HDF5 1.6.x or HDF5 1.8.x).
 * <p>
 * The class focuses on ease of use instead of completeness. As a consequence not all valid HDF5
 * files can be generated using this class, but only a subset.
 * <p>
 * Usage:
 * 
 * <pre>
 * float[] f = new float[100];
 * ...
 * HDF5Writer writer = new HDF5WriterConfig(&quot;test.h5&quot;).writer();
 * writer.writeFloatArray(&quot;/some/path/dataset&quot;, f);
 * writer.addAttribute(&quot;some key&quot;, &quot;some value&quot;);
 * writer.close();
 * </pre>
 * 
 * @author Bernd Rinn
 */
final class HDF5Writer extends HDF5Reader implements IHDF5Writer
{
    private final HDF5BaseWriter baseWriter;

    private final IHDF5ByteWriter byteWriter;

    private final IHDF5ShortWriter shortWriter;

    private final IHDF5IntWriter intWriter;

    private final IHDF5LongWriter longWriter;

    private final IHDF5FloatWriter floatWriter;

    private final IHDF5DoubleWriter doubleWriter;

    private final IHDF5BooleanWriter booleanWriter;

    private final IHDF5StringWriter stringWriter;

    private final IHDF5EnumWriter enumWriter;

    private final IHDF5CompoundWriter compoundWriter;

    private final IHDF5DateTimeWriter dateTimeWriter;

    private final IHDF5ReferenceWriter referenceWriter;

    private final IHDF5OpaqueWriter opaqueWriter;

    HDF5Writer(HDF5BaseWriter baseWriter)
    {
        super(baseWriter);
        this.baseWriter = baseWriter;
        this.byteWriter = new HDF5ByteWriter(baseWriter);
        this.shortWriter = new HDF5ShortWriter(baseWriter);
        this.intWriter = new HDF5IntWriter(baseWriter);
        this.longWriter = new HDF5LongWriter(baseWriter);
        this.floatWriter = new HDF5FloatWriter(baseWriter);
        this.doubleWriter = new HDF5DoubleWriter(baseWriter);
        this.booleanWriter = new HDF5BooleanWriter(baseWriter);
        this.stringWriter = new HDF5StringWriter(baseWriter);
        this.enumWriter = new HDF5EnumWriter(baseWriter);
        this.compoundWriter = new HDF5CompoundWriter(baseWriter, enumWriter);
        this.dateTimeWriter = new HDF5DateTimeWriter(baseWriter);
        this.referenceWriter = new HDF5ReferenceWriter(baseWriter);
        this.opaqueWriter = new HDF5OpaqueWriter(baseWriter);
    }

    HDF5BaseWriter getBaseWriter()
    {
        return baseWriter;
    }

    // /////////////////////
    // Configuration
    // /////////////////////

    public boolean isUseExtendableDataTypes()
    {
        return baseWriter.useExtentableDataTypes;
    }

    public FileFormat getFileFormat()
    {
        return baseWriter.fileFormat;
    }

    // /////////////////////
    // File
    // /////////////////////

    public void flush()
    {
        baseWriter.checkOpen();
        baseWriter.flush();
    }

    public void flushSyncBlocking()
    {
        baseWriter.checkOpen();
        baseWriter.flushSyncBlocking();
    }

    public boolean addFlushable(Flushable flushable)
    {
        return baseWriter.addFlushable(flushable);
    }

    public boolean removeFlushable(Flushable flushable)
    {
        return baseWriter.removeFlushable(flushable);
    }

    // /////////////////////
    // Objects & Links
    // /////////////////////

    public void createHardLink(String currentPath, String newPath)
    {
        assert currentPath != null;
        assert newPath != null;

        baseWriter.checkOpen();
        baseWriter.h5.createHardLink(baseWriter.fileId, currentPath, newPath);
    }

    public void createSoftLink(String targetPath, String linkPath)
    {
        assert targetPath != null;
        assert linkPath != null;

        baseWriter.checkOpen();
        baseWriter.h5.createSoftLink(baseWriter.fileId, linkPath, targetPath);
    }

    public void createOrUpdateSoftLink(String targetPath, String linkPath)
    {
        assert targetPath != null;
        assert linkPath != null;

        baseWriter.checkOpen();
        if (isSymbolicLink(linkPath))
        {
            delete(linkPath);
        }
        baseWriter.h5.createSoftLink(baseWriter.fileId, linkPath, targetPath);
    }

    public void createExternalLink(String targetFileName, String targetPath, String linkPath)
            throws IllegalStateException
    {
        assert targetFileName != null;
        assert targetPath != null;
        assert linkPath != null;

        baseWriter.checkOpen();
        if (baseWriter.fileFormat.isHDF5_1_8_OK() == false)
        {
            throw new IllegalStateException(
                    "External links are not allowed in strict HDF5 1.6.x compatibility mode.");
        }
        baseWriter.h5.createExternalLink(baseWriter.fileId, linkPath, targetFileName, targetPath);
    }

    public void createOrUpdateExternalLink(String targetFileName, String targetPath, String linkPath)
            throws IllegalStateException
    {
        assert targetFileName != null;
        assert targetPath != null;
        assert linkPath != null;

        baseWriter.checkOpen();
        if (baseWriter.fileFormat.isHDF5_1_8_OK() == false)
        {
            throw new IllegalStateException(
                    "External links are not allowed in strict HDF5 1.6.x compatibility mode.");
        }
        if (isSymbolicLink(linkPath))
        {
            delete(linkPath);
        }
        baseWriter.h5.createExternalLink(baseWriter.fileId, linkPath, targetFileName, targetPath);
    }

    public void delete(String objectPath)
    {
        baseWriter.checkOpen();
        if (isGroup(objectPath, false))
        {
            for (String path : getGroupMemberPaths(objectPath))
            {
                delete(path);
            }
        }
        baseWriter.h5.deleteObject(baseWriter.fileId, objectPath);
    }

    public void move(String oldLinkPath, String newLinkPath)
    {
        baseWriter.checkOpen();
        baseWriter.h5.moveLink(baseWriter.fileId, oldLinkPath, newLinkPath);
    }

    // /////////////////////
    // Group
    // /////////////////////

    public void createGroup(final String groupPath)
    {
        baseWriter.checkOpen();
        baseWriter.h5.createGroup(baseWriter.fileId, groupPath);
    }

    public void createGroup(final String groupPath, final int sizeHint)
    {
        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> createGroupRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    baseWriter.h5.createOldStyleGroup(baseWriter.fileId, groupPath, sizeHint,
                            registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(createGroupRunnable);
    }

    public void createGroup(final String groupPath, final int maxCompact, final int minDense)
    {
        baseWriter.checkOpen();
        if (baseWriter.fileFormat.isHDF5_1_8_OK() == false)
        {
            throw new IllegalStateException(
                    "New style groups are not allowed in strict HDF5 1.6.x compatibility mode.");
        }
        final ICallableWithCleanUp<Void> createGroupRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    baseWriter.h5.createNewStyleGroup(baseWriter.fileId, groupPath, maxCompact,
                            minDense, registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(createGroupRunnable);
    }

    // /////////////////////
    // Attributes
    // /////////////////////

    public void deleteAttribute(final String objectPath, final String name)
    {
        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> deleteAttributeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final int objectId =
                            baseWriter.h5.openObject(baseWriter.fileId, objectPath, registry);
                    baseWriter.h5.deleteAttribute(objectId, name);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(deleteAttributeRunnable);
    }

    public void setTypeVariant(final String objectPath, final HDF5DataTypeVariant typeVariant)
    {
        baseWriter.checkOpen();
        baseWriter.setAttribute(objectPath,
                createObjectTypeVariantAttributeName(baseWriter.houseKeepingNameSuffix),
                baseWriter.typeVariantDataType.getStorageTypeId(),
                baseWriter.typeVariantDataType.getNativeTypeId(),
                baseWriter.typeVariantDataType.toStorageForm(typeVariant.ordinal()));
    }

    public void setTypeVariant(String objectPath, String attributeName,
            HDF5DataTypeVariant typeVariant)
    {
        baseWriter.checkOpen();
        baseWriter.setAttribute(objectPath,
                createAttributeTypeVariantAttributeName(attributeName, baseWriter.houseKeepingNameSuffix),
                baseWriter.typeVariantDataType.getStorageTypeId(),
                baseWriter.typeVariantDataType.getNativeTypeId(),
                baseWriter.typeVariantDataType.toStorageForm(typeVariant.ordinal()));
    }

    public void deleteTypeVariant(String objectPath)
    {
        deleteAttribute(objectPath, createObjectTypeVariantAttributeName(baseWriter.houseKeepingNameSuffix));
    }

    public void deleteTypeVariant(String objectPath, String attributeName)
    {
        deleteAttribute(objectPath,
                createAttributeTypeVariantAttributeName(attributeName, baseWriter.houseKeepingNameSuffix));
    }

    public void setBooleanAttribute(String objectPath, String name, boolean value)
    {
        booleanWriter.setBooleanAttribute(objectPath, name, value);
    }

    // /////////////////////
    // Data Sets
    // /////////////////////

    //
    // Generic
    //

    public void setDataSetSize(final String objectPath, final long newSize)
    {
        setDataSetDimensions(objectPath, new long[]
            { newSize });
    }

    public void setDataSetDimensions(final String objectPath, final long[] newDimensions)
    {
        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    baseWriter.setDataSetDimensions(objectPath, newDimensions, registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    //
    // Boolean
    //

    public void writeBitField(String objectPath, BitSet data, HDF5GenericStorageFeatures features)
    {
        booleanWriter.writeBitField(objectPath, data, features);
    }

    public void writeBitField(String objectPath, BitSet data)
    {
        booleanWriter.writeBitField(objectPath, data);
    }

    public void writeBoolean(String objectPath, boolean value)
    {
        booleanWriter.writeBoolean(objectPath, value);
    }

    //
    // Opaque
    //

    public void createBitField(String objectPath, int size)
    {
        booleanWriter.createBitField(objectPath, size);
    }

    public void createBitField(String objectPath, long size, int blockSize)
    {
        booleanWriter.createBitField(objectPath, size, blockSize);
    }

    public void createBitField(String objectPath, int size, HDF5IntStorageFeatures features)
    {
        booleanWriter.createBitField(objectPath, size, features);
    }

    public void createBitField(String objectPath, long size, int blockSize,
            HDF5IntStorageFeatures features)
    {
        booleanWriter.createBitField(objectPath, size, blockSize, features);
    }

    public void writeBitFieldBlock(String objectPath, BitSet data, int dataSize, long blockNumber)
    {
        booleanWriter.writeBitFieldBlock(objectPath, data, dataSize, blockNumber);
    }

    public void writeBitFieldBlockWithOffset(String objectPath, BitSet data, int dataSize,
            long offset)
    {
        booleanWriter.writeBitFieldBlockWithOffset(objectPath, data, dataSize, offset);
    }

    public HDF5OpaqueType createOpaqueByteArray(String objectPath, String tag, int size,
            HDF5GenericStorageFeatures features)
    {
        return opaqueWriter.createOpaqueByteArray(objectPath, tag, size, features);
    }

    public HDF5OpaqueType createOpaqueByteArray(String objectPath, String tag, int size)
    {
        return opaqueWriter.createOpaqueByteArray(objectPath, tag, size);
    }

    public HDF5OpaqueType createOpaqueByteArray(String objectPath, String tag, long size,
            int blockSize, HDF5GenericStorageFeatures features)
    {
        return opaqueWriter.createOpaqueByteArray(objectPath, tag, size, blockSize, features);
    }

    public HDF5OpaqueType createOpaqueByteArray(String objectPath, String tag, long size,
            int blockSize)
    {
        return opaqueWriter.createOpaqueByteArray(objectPath, tag, size, blockSize);
    }

    public void writeOpaqueByteArray(String objectPath, String tag, byte[] data,
            HDF5GenericStorageFeatures features)
    {
        opaqueWriter.writeOpaqueByteArray(objectPath, tag, data, features);
    }

    public void writeOpaqueByteArray(String objectPath, String tag, byte[] data)
    {
        opaqueWriter.writeOpaqueByteArray(objectPath, tag, data);
    }

    public void writeOpaqueByteArrayBlock(String objectPath, HDF5OpaqueType dataType, byte[] data,
            long blockNumber)
    {
        opaqueWriter.writeOpaqueByteArrayBlock(objectPath, dataType, data, blockNumber);
    }

    public void writeOpaqueByteArrayBlockWithOffset(String objectPath, HDF5OpaqueType dataType,
            byte[] data, int dataSize, long offset)
    {
        opaqueWriter.writeOpaqueByteArrayBlockWithOffset(objectPath, dataType, data, dataSize,
                offset);
    }

    //
    // Date
    //

    public void createTimeStampArray(String objectPath, int size,
            HDF5GenericStorageFeatures features)
    {
        dateTimeWriter.createTimeStampArray(objectPath, size, features);
    }

    public void setTimeStampAttribute(String objectPath, String name, long value)
    {
        dateTimeWriter.setTimeStampAttribute(objectPath, name, value);
    }

    public void setDateAttribute(String objectPath, String name, Date date)
    {
        dateTimeWriter.setDateAttribute(objectPath, name, date);
    }

    public void setTimeDurationAttribute(String objectPath, String name,
            HDF5TimeDuration timeDuration)
    {
        dateTimeWriter.setTimeDurationAttribute(objectPath, name, timeDuration);
    }

    public void setTimeDurationAttribute(String objectPath, String name, long timeDuration,
            HDF5TimeUnit timeUnit)
    {
        dateTimeWriter.setTimeDurationAttribute(objectPath, name, timeDuration, timeUnit);
    }

    public void setDateArrayAttribute(String objectPath, String name, Date[] dates)
    {
        dateTimeWriter.setDateArrayAttribute(objectPath, name, dates);
    }

    public void setTimeStampArrayAttribute(String objectPath, String name, long[] timeStamps)
    {
        dateTimeWriter.setTimeStampArrayAttribute(objectPath, name, timeStamps);
    }

    public void setTimeDurationArrayAttribute(String objectPath, String name,
            HDF5TimeDurationArray timeDurations)
    {
        dateTimeWriter.setTimeDurationArrayAttribute(objectPath, name, timeDurations);
    }

    public void createTimeStampArray(String objectPath, int size)
    {
        dateTimeWriter.createTimeStampArray(objectPath, size);
    }

    public void createTimeStampArray(String objectPath, long size, int blockSize,
            HDF5GenericStorageFeatures features)
    {
        dateTimeWriter.createTimeStampArray(objectPath, size, blockSize, features);
    }

    public void createTimeStampArray(String objectPath, long size, int blockSize)
    {
        dateTimeWriter.createTimeStampArray(objectPath, size, blockSize);
    }

    public void writeDate(String objectPath, Date date)
    {
        dateTimeWriter.writeDate(objectPath, date);
    }

    public void writeDateArray(String objectPath, Date[] dates, HDF5GenericStorageFeatures features)
    {
        dateTimeWriter.writeDateArray(objectPath, dates, features);
    }

    public void writeDateArray(String objectPath, Date[] dates)
    {
        dateTimeWriter.writeDateArray(objectPath, dates);
    }

    public void writeTimeStamp(String objectPath, long timeStamp)
    {
        dateTimeWriter.writeTimeStamp(objectPath, timeStamp);
    }

    public void writeTimeStampArray(String objectPath, long[] timeStamps,
            HDF5GenericStorageFeatures features)
    {
        dateTimeWriter.writeTimeStampArray(objectPath, timeStamps, features);
    }

    public void writeTimeStampArray(String objectPath, long[] timeStamps)
    {
        dateTimeWriter.writeTimeStampArray(objectPath, timeStamps);
    }

    public void writeTimeStampArrayBlock(String objectPath, long[] data, long blockNumber)
    {
        dateTimeWriter.writeTimeStampArrayBlock(objectPath, data, blockNumber);
    }

    public void writeTimeStampArrayBlockWithOffset(String objectPath, long[] data, int dataSize,
            long offset)
    {
        dateTimeWriter.writeTimeStampArrayBlockWithOffset(objectPath, data, dataSize, offset);
    }

    //
    // Duration
    //

    public void createTimeDurationArray(String objectPath, int size, HDF5TimeUnit timeUnit,
            HDF5GenericStorageFeatures features)
    {
        dateTimeWriter.createTimeDurationArray(objectPath, size, timeUnit, features);
    }

    public void createTimeDurationArray(String objectPath, int size, HDF5TimeUnit timeUnit)
    {
        dateTimeWriter.createTimeDurationArray(objectPath, size, timeUnit);
    }

    public void createTimeDurationArray(String objectPath, long size, int blockSize,
            HDF5TimeUnit timeUnit, HDF5GenericStorageFeatures features)
    {
        dateTimeWriter.createTimeDurationArray(objectPath, size, blockSize, timeUnit, features);
    }

    public void createTimeDurationArray(String objectPath, long size, int blockSize,
            HDF5TimeUnit timeUnit)
    {
        dateTimeWriter.createTimeDurationArray(objectPath, size, blockSize, timeUnit);
    }

    public void writeTimeDuration(String objectPath, long timeDuration, HDF5TimeUnit timeUnit)
    {
        dateTimeWriter.writeTimeDuration(objectPath, timeDuration, timeUnit);
    }

    public void writeTimeDuration(String objectPath, HDF5TimeDuration timeDuration)
    {
        dateTimeWriter.writeTimeDuration(objectPath, timeDuration);
    }

    @Deprecated
    public void writeTimeDuration(String objectPath, long timeDuration)
    {
        dateTimeWriter.writeTimeDuration(objectPath, timeDuration);
    }

    @Deprecated
    public void writeTimeDurationArray(String objectPath, long[] timeDurations,
            HDF5TimeUnit timeUnit, HDF5IntStorageFeatures features)
    {
        dateTimeWriter.writeTimeDurationArray(objectPath, timeDurations, timeUnit, features);
    }

    public void writeTimeDurationArray(String objectPath, HDF5TimeDurationArray timeDurations)
    {
        dateTimeWriter.writeTimeDurationArray(objectPath, timeDurations);
    }

    public void writeTimeDurationArray(String objectPath, HDF5TimeDurationArray timeDurations,
            HDF5IntStorageFeatures features)
    {
        dateTimeWriter.writeTimeDurationArray(objectPath, timeDurations, features);
    }

    @Deprecated
    public void writeTimeDurationArray(String objectPath, long[] timeDurations,
            HDF5TimeUnit timeUnit)
    {
        dateTimeWriter.writeTimeDurationArray(objectPath, timeDurations, timeUnit);
    }

    @Deprecated
    public void writeTimeDurationArray(String objectPath, long[] timeDurations)
    {
        dateTimeWriter.writeTimeDurationArray(objectPath, timeDurations);
    }

    @Deprecated
    public void writeTimeDurationArray(String objectPath, HDF5TimeDuration[] timeDurations)
    {
        dateTimeWriter.writeTimeDurationArray(objectPath, timeDurations);
    }

    @Deprecated
    public void writeTimeDurationArray(String objectPath, HDF5TimeDuration[] timeDurations,
            HDF5IntStorageFeatures features)
    {
        dateTimeWriter.writeTimeDurationArray(objectPath, timeDurations, features);
    }

    public void writeTimeDurationArrayBlock(String objectPath, HDF5TimeDurationArray data,
            long blockNumber)
    {
        dateTimeWriter.writeTimeDurationArrayBlock(objectPath, data, blockNumber);
    }

    public void writeTimeDurationArrayBlockWithOffset(String objectPath,
            HDF5TimeDurationArray data, int dataSize, long offset)
    {
        dateTimeWriter.writeTimeDurationArrayBlockWithOffset(objectPath, data, dataSize, offset);
    }

    @Deprecated
    public void writeTimeDurationArrayBlock(String objectPath, long[] data, long blockNumber,
            HDF5TimeUnit timeUnit)
    {
        dateTimeWriter.writeTimeDurationArrayBlock(objectPath, data, blockNumber, timeUnit);
    }

    @Deprecated
    public void writeTimeDurationArrayBlockWithOffset(String objectPath, long[] data, int dataSize,
            long offset, HDF5TimeUnit timeUnit)
    {
        dateTimeWriter.writeTimeDurationArrayBlockWithOffset(objectPath, data, dataSize, offset,
                timeUnit);
    }

    @Deprecated
    public void writeTimeDurationArrayBlock(String objectPath, HDF5TimeDuration[] data,
            long blockNumber)
    {
        dateTimeWriter.writeTimeDurationArrayBlock(objectPath, data, blockNumber);
    }

    @Deprecated
    public void writeTimeDurationArrayBlockWithOffset(String objectPath, HDF5TimeDuration[] data,
            int dataSize, long offset)
    {
        dateTimeWriter.writeTimeDurationArrayBlockWithOffset(objectPath, data, dataSize, offset);
    }

    //
    // References
    //

    public void writeObjectReference(String objectPath, String referencedObjectPath)
    {
        referenceWriter.writeObjectReference(objectPath, referencedObjectPath);
    }

    public void writeObjectReferenceArray(String objectPath, String[] referencedObjectPath)
    {
        referenceWriter.writeObjectReferenceArray(objectPath, referencedObjectPath);
    }

    public void writeObjectReferenceArray(String objectPath, String[] referencedObjectPath,
            HDF5IntStorageFeatures features)
    {
        referenceWriter.writeObjectReferenceArray(objectPath, referencedObjectPath, features);
    }

    public void writeObjectReferenceMDArray(String objectPath, MDArray<String> referencedObjectPaths)
    {
        referenceWriter.writeObjectReferenceMDArray(objectPath, referencedObjectPaths);
    }

    public void writeObjectReferenceMDArray(String objectPath,
            MDArray<String> referencedObjectPaths, HDF5IntStorageFeatures features)
    {
        referenceWriter.writeObjectReferenceMDArray(objectPath, referencedObjectPaths, features);
    }

    public void setObjectReferenceAttribute(String objectPath, String name,
            String referencedObjectPath)
    {
        referenceWriter.setObjectReferenceAttribute(objectPath, name, referencedObjectPath);
    }

    public void setObjectReferenceArrayAttribute(String objectPath, String name, String[] value)
    {
        referenceWriter.setObjectReferenceArrayAttribute(objectPath, name, value);
    }

    //
    // String
    //

    public void setObjectReferenceMDArrayAttribute(String objectPath, String name,
            MDArray<String> referencedObjectPaths)
    {
        referenceWriter.setObjectReferenceMDArrayAttribute(objectPath, name, referencedObjectPaths);
    }

    public void createObjectReferenceArray(String objectPath, int size)
    {
        referenceWriter.createObjectReferenceArray(objectPath, size);
    }

    public void createObjectReferenceArray(String objectPath, int size,
            HDF5IntStorageFeatures features)
    {
        referenceWriter.createObjectReferenceArray(objectPath, size, features);
    }

    public void createObjectReferenceArray(String objectPath, long size, int blockSize,
            HDF5IntStorageFeatures features)
    {
        referenceWriter.createObjectReferenceArray(objectPath, size, blockSize, features);
    }

    public void writeObjectReferenceArrayBlock(String objectPath, String[] referencedObjectPaths,
            long blockNumber)
    {
        referenceWriter.writeObjectReferenceArrayBlock(objectPath, referencedObjectPaths,
                blockNumber);
    }

    public void writeObjectReferenceArrayBlockWithOffset(String objectPath,
            String[] referencedObjectPaths, int dataSize, long offset)
    {
        referenceWriter.writeObjectReferenceArrayBlockWithOffset(objectPath, referencedObjectPaths,
                dataSize, offset);
    }

    public void createObjectReferenceMDArray(String objectPath, int[] dimensions)
    {
        referenceWriter.createObjectReferenceMDArray(objectPath, dimensions);
    }

    public void createObjectReferenceMDArray(String objectPath, long[] dimensions,
            int[] blockDimensions)
    {
        referenceWriter.createObjectReferenceMDArray(objectPath, dimensions, blockDimensions);
    }

    public void createObjectReferenceMDArray(String objectPath, int[] dimensions,
            HDF5IntStorageFeatures features)
    {
        referenceWriter.createObjectReferenceMDArray(objectPath, dimensions, features);
    }

    public void createObjectReferenceMDArray(String objectPath, long[] dimensions,
            int[] blockDimensions, HDF5IntStorageFeatures features)
    {
        referenceWriter.createObjectReferenceMDArray(objectPath, dimensions, blockDimensions,
                features);
    }

    public void writeObjectReferenceMDArrayBlock(String objectPath,
            MDArray<String> referencedObjectPaths, long[] blockNumber)
    {
        referenceWriter.writeObjectReferenceMDArrayBlock(objectPath, referencedObjectPaths,
                blockNumber);
    }

    public void writeObjectReferenceMDArrayBlockWithOffset(String objectPath,
            MDArray<String> referencedObjectPaths, long[] offset)
    {
        referenceWriter.writeObjectReferenceMDArrayBlockWithOffset(objectPath,
                referencedObjectPaths, offset);
    }

    public void writeObjectReferenceMDArrayBlockWithOffset(String objectPath, MDLongArray data,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        referenceWriter.writeObjectReferenceMDArrayBlockWithOffset(objectPath, data,
                blockDimensions, offset, memoryOffset);
    }

    public void createStringArray(String objectPath, int maxLength, int size)
    {
        stringWriter.createStringArray(objectPath, maxLength, size);
    }

    public void createStringArray(String objectPath, int maxLength, long size, int blockSize)
    {
        stringWriter.createStringArray(objectPath, maxLength, size, blockSize);
    }

    public void createStringArray(String objectPath, int maxLength, int size,
            HDF5GenericStorageFeatures features)
    {
        stringWriter.createStringArray(objectPath, maxLength, size, features);
    }

    public void createStringArray(String objectPath, int maxLength, long size, int blockSize,
            HDF5GenericStorageFeatures features)
    {
        stringWriter.createStringArray(objectPath, maxLength, size, blockSize, features);
    }

    public void createStringVariableLengthArray(String objectPath, int size)
    {
        stringWriter.createStringVariableLengthArray(objectPath, size);
    }

    public void createStringVariableLengthArray(String objectPath, long size, int blockSize)
    {
        stringWriter.createStringVariableLengthArray(objectPath, size, blockSize);
    }

    public void createStringVariableLengthArray(String objectPath, long size, int blockSize,
            HDF5GenericStorageFeatures features)
    {
        stringWriter.createStringVariableLengthArray(objectPath, size, blockSize, features);
    }

    public void createStringVariableLengthArray(String objectPath, int size,
            HDF5GenericStorageFeatures features)
    {
        stringWriter.createStringVariableLengthArray(objectPath, size, features);
    }

    public void setStringAttribute(String objectPath, String name, String value)
    {
        stringWriter.setStringAttribute(objectPath, name, value);
    }

    public void setStringAttribute(String objectPath, String name, String value, int maxLength)
    {
        stringWriter.setStringAttribute(objectPath, name, value, maxLength);
    }

    public void setStringAttributeExplicitLength(String objectPath, String name, int stringLength)
    {
        stringWriter.setStringAttributeExplicitLength(objectPath, name, stringLength);
    }

    public void setStringArrayAttribute(String objectPath, String name, String[] value,
            int maxLength)
    {
        stringWriter.setStringArrayAttribute(objectPath, name, value, maxLength);
    }

    public void setStringArrayAttribute(String objectPath, String name, String[] value)
    {
        stringWriter.setStringArrayAttribute(objectPath, name, value);
    }

    public void setStringMDArrayAttribute(String objectPath, String name, MDArray<String> value)
    {
        stringWriter.setStringMDArrayAttribute(objectPath, name, value);
    }

    public void setStringMDArrayAttribute(String objectPath, String name, MDArray<String> value,
            int maxLength)
    {
        stringWriter.setStringMDArrayAttribute(objectPath, name, value, maxLength);
    }

    public void setStringAttributeVariableLength(String objectPath, String name, String value)
    {
        stringWriter.setStringAttributeVariableLength(objectPath, name, value);
    }

    public void writeString(String objectPath, String data, int maxLength)
    {
        stringWriter.writeString(objectPath, data, maxLength);
    }

    public void writeString(String objectPath, String data)
    {
        stringWriter.writeString(objectPath, data);
    }

    public void writeString(String objectPath, String data, HDF5GenericStorageFeatures features)
    {
        stringWriter.writeString(objectPath, data, features);
    }

    public void writeString(String objectPath, String data, int maxLength,
            HDF5GenericStorageFeatures features)
    {
        stringWriter.writeString(objectPath, data, maxLength, features);
    }

    public void writeStringArray(String objectPath, String[] data,
            HDF5GenericStorageFeatures features)
    {
        stringWriter.writeStringArray(objectPath, data, features);
    }

    public void writeStringArray(String objectPath, String[] data)
    {
        stringWriter.writeStringArray(objectPath, data);
    }

    public void writeStringArray(String objectPath, String[] data, int maxLength)
    {
        stringWriter.writeStringArray(objectPath, data, maxLength);
    }

    public void writeStringArray(String objectPath, String[] data, int maxLength,
            HDF5GenericStorageFeatures features)
    {
        stringWriter.writeStringArray(objectPath, data, maxLength, features);
    }

    public void createStringMDArray(String objectPath, int maxLength, int[] dimensions,
            HDF5GenericStorageFeatures features)
    {
        stringWriter.createStringMDArray(objectPath, maxLength, dimensions, features);
    }

    public void createStringMDArray(String objectPath, int maxLength, int[] dimensions)
    {
        stringWriter.createStringMDArray(objectPath, maxLength, dimensions);
    }

    public void createStringMDArray(String objectPath, int maxLength, long[] dimensions,
            int[] blockSize, HDF5GenericStorageFeatures features)
    {
        stringWriter.createStringMDArray(objectPath, maxLength, dimensions, blockSize, features);
    }

    public void createStringMDArray(String objectPath, int maxLength, long[] dimensions,
            int[] blockSize)
    {
        stringWriter.createStringMDArray(objectPath, maxLength, dimensions, blockSize);
    }

    public void writeStringMDArray(String objectPath, MDArray<String> data, int maxLength)
            throws HDF5JavaException
    {
        stringWriter.writeStringMDArray(objectPath, data, maxLength);
    }

    public void writeStringMDArray(String objectPath, MDArray<String> data)
            throws HDF5JavaException
    {
        stringWriter.writeStringMDArray(objectPath, data);
    }

    public void writeStringMDArray(String objectPath, MDArray<String> data, int maxLength,
            HDF5GenericStorageFeatures features) throws HDF5JavaException
    {
        stringWriter.writeStringMDArray(objectPath, data, maxLength, features);
    }

    public void writeStringArrayBlock(String objectPath, String[] data, long blockNumber)
    {
        stringWriter.writeStringArrayBlock(objectPath, data, blockNumber);
    }

    public void writeStringArrayBlockWithOffset(String objectPath, String[] data, int dataSize,
            long offset)
    {
        stringWriter.writeStringArrayBlockWithOffset(objectPath, data, dataSize, offset);
    }

    public void writeStringMDArrayBlock(String objectPath, MDArray<String> data, long[] blockNumber)
    {
        stringWriter.writeStringMDArrayBlock(objectPath, data, blockNumber);
    }

    public void writeStringMDArrayBlockWithOffset(String objectPath, MDArray<String> data,
            long[] offset)
    {
        stringWriter.writeStringMDArrayBlockWithOffset(objectPath, data, offset);
    }

    public void writeStringVariableLength(String objectPath, String data)
    {
        stringWriter.writeStringVariableLength(objectPath, data);
    }

    public void writeStringVariableLengthArray(String objectPath, String[] data)
    {
        stringWriter.writeStringVariableLengthArray(objectPath, data);
    }

    public void writeStringVariableLengthArray(String objectPath, String[] data,
            HDF5GenericStorageFeatures features)
    {
        stringWriter.writeStringVariableLengthArray(objectPath, data, features);
    }

    public void writeStringVariableLengthMDArray(String objectPath, MDArray<String> data,
            HDF5GenericStorageFeatures features)
    {
        stringWriter.writeStringVariableLengthMDArray(objectPath, data, features);
    }

    public void writeStringVariableLengthMDArray(String objectPath, MDArray<String> data)
    {
        stringWriter.writeStringVariableLengthMDArray(objectPath, data);
    }

    public void createStringVariableLengthMDArray(String objectPath, int[] dimensions,
            HDF5GenericStorageFeatures features)
    {
        stringWriter.createStringVariableLengthMDArray(objectPath, dimensions, features);
    }

    public void createStringVariableLengthMDArray(String objectPath, int[] dimensions)
    {
        stringWriter.createStringVariableLengthMDArray(objectPath, dimensions);
    }

    public void createStringVariableLengthMDArray(String objectPath, long[] dimensions,
            int[] blockSize, HDF5GenericStorageFeatures features)
    {
        stringWriter.createStringVariableLengthMDArray(objectPath, dimensions, blockSize, features);
    }

    public void createStringVariableLengthMDArray(String objectPath, long[] dimensions,
            int[] blockSize)
    {
        stringWriter.createStringVariableLengthMDArray(objectPath, dimensions, blockSize);
    }

    //
    // Enum
    //

    @Override
    public IHDF5EnumWriter enums()
    {
        return enumWriter;
    }

    @Override
    public HDF5EnumerationType getEnumType(final String name, final String[] values)
            throws HDF5JavaException
    {
        return enumWriter.getType(name, values);
    }

    @Override
    public HDF5EnumerationType getEnumType(final String name, final String[] values,
            final boolean check) throws HDF5JavaException
    {
        return enumWriter.getType(name, values, check);
    }

    public HDF5EnumerationType createEnumArray(String objectPath, HDF5EnumerationType enumType,
            int size)
    {
        return enumWriter.createArray(objectPath, enumType, size);
    }

    public HDF5EnumerationType createEnumArray(String objectPath, HDF5EnumerationType enumType,
            long size, HDF5IntStorageFeatures features)
    {
        return enumWriter.createArray(objectPath, enumType, size, features);
    }

    public HDF5EnumerationType createEnumArray(String objectPath, HDF5EnumerationType enumType,
            long size, int blockSize, HDF5IntStorageFeatures features)
    {
        return enumWriter.createArray(objectPath, enumType, size, blockSize, features);
    }

    public HDF5EnumerationType createEnumArray(String objectPath, HDF5EnumerationType enumType,
            long size, int blockSize)
    {
        return enumWriter.createArray(objectPath, enumType, size, blockSize);
    }

    public void setEnumAttribute(String objectPath, String name, HDF5EnumerationValue value)
    {
        enumWriter.setAttr(objectPath, name, value);
    }

    public void setEnumAttribute(String objectPath, String name, Enum<?> value)
            throws HDF5JavaException
    {
        enumWriter.setAttr(objectPath, name, value);
    }

    public <T extends Enum<T>> void writeEnum(String objectPath, Enum<T> value)
            throws HDF5JavaException
    {
        enumWriter.write(objectPath, value);
    }

    public void writeEnum(String objectPath, String[] options, String value)
    {
        enumWriter.write(objectPath, enumWriter.newAnonVal(options, value));
    }

    public <T extends Enum<T>> void writeEnumArray(String objectPath, Enum<T>[] data)
    {
        enumWriter.writeArray(objectPath, enumWriter.newAnonArray(data));
    }

    public void writeEnumArray(String objectPath, String[] options, String[] data)
    {
        enumWriter.writeArray(objectPath, enumWriter.newAnonArray(options, data));
    }

    public void setEnumArrayAttribute(String objectPath, String name,
            HDF5EnumerationValueArray value)
    {
        enumWriter.setArrayAttr(objectPath, name, value);
    }

    public void writeEnum(String objectPath, HDF5EnumerationValue value) throws HDF5JavaException
    {
        enumWriter.write(objectPath, value);
    }

    public void writeEnumArray(String objectPath, HDF5EnumerationValueArray data,
            HDF5IntStorageFeatures features) throws HDF5JavaException
    {
        enumWriter.writeArray(objectPath, data, features);
    }

    public void writeEnumArray(String objectPath, HDF5EnumerationValueArray data)
            throws HDF5JavaException
    {
        enumWriter.writeArray(objectPath, data);
    }

    public void writeEnumArrayBlock(String objectPath, HDF5EnumerationValueArray data,
            long blockNumber)
    {
        enumWriter.writeArrayBlock(objectPath, data, blockNumber);
    }

    public void writeEnumArrayBlockWithOffset(String objectPath, HDF5EnumerationValueArray data,
            int dataSize, long offset)
    {
        enumWriter.writeArrayBlockWithOffset(objectPath, data, dataSize, offset);
    }

    //
    // Compound
    //

    @Override
    public IHDF5CompoundWriter compounds()
    {
        return compoundWriter;
    }

    @Override
    public <T> HDF5CompoundType<T> getCompoundType(final String name, Class<T> pojoClass,
            HDF5CompoundMemberMapping... members)
    {
        return compoundWriter.getType(name, pojoClass, members);
    }

    @Override
    public <T> HDF5CompoundType<T> getCompoundType(Class<T> pojoClass,
            HDF5CompoundMemberMapping... members)
    {
        return compoundWriter.getType(pojoClass, members);
    }

    @Override
    public <T> HDF5CompoundType<T> getInferredCompoundType(final String name, Class<T> pojoClass)
    {
        return compoundWriter.getInferredType(name, pojoClass);
    }

    @Override
    public <T> HDF5CompoundType<T> getInferredCompoundType(Class<T> pojoClass)
    {
        return compoundWriter.getInferredType(pojoClass);
    }

    @Override
    public <T> HDF5CompoundType<T> getInferredCompoundType(final String name, T template)
    {
        return compoundWriter.getInferredType(name, template);
    }

    @Override
    public <T> HDF5CompoundType<T> getInferredCompoundType(T template)
    {
        return compoundWriter.getInferredType(template);
    }

    public <T> void createCompoundArray(String objectPath, HDF5CompoundType<T> type, int size)
    {
        compoundWriter.createArray(objectPath, type, size);
    }

    public <T> void createCompoundArray(String objectPath, HDF5CompoundType<T> type, long size,
            HDF5GenericStorageFeatures features)
    {
        compoundWriter.createArray(objectPath, type, size, features);
    }

    public <T> void createCompoundArray(String objectPath, HDF5CompoundType<T> type, long size,
            int blockSize, HDF5GenericStorageFeatures features)
    {
        compoundWriter.createArray(objectPath, type, size, blockSize, features);
    }

    public <T> void createCompoundArray(String objectPath, HDF5CompoundType<T> type, long size,
            int blockSize)
    {
        compoundWriter.createArray(objectPath, type, size, blockSize);
    }

    public <T> void createCompoundMDArray(String objectPath, HDF5CompoundType<T> type,
            int[] dimensions, HDF5GenericStorageFeatures features)
    {
        compoundWriter.createMDArray(objectPath, type, dimensions, features);
    }

    public <T> void createCompoundMDArray(String objectPath, HDF5CompoundType<T> type,
            int[] dimensions)
    {
        compoundWriter.createMDArray(objectPath, type, dimensions);
    }

    public <T> void createCompoundMDArray(String objectPath, HDF5CompoundType<T> type,
            long[] dimensions, int[] blockDimensions, HDF5GenericStorageFeatures features)
    {
        compoundWriter.createMDArray(objectPath, type, dimensions, blockDimensions, features);
    }

    public <T> void createCompoundMDArray(String objectPath, HDF5CompoundType<T> type,
            long[] dimensions, int[] blockDimensions)
    {
        compoundWriter.createMDArray(objectPath, type, dimensions, blockDimensions);
    }

    public <T> void writeCompound(String objectPath, HDF5CompoundType<T> type, T data,
            IByteArrayInspector inspectorOrNull)
    {
        compoundWriter.write(objectPath, type, data, inspectorOrNull);
    }

    public <T> void writeCompound(String objectPath, HDF5CompoundType<T> type, T data)
    {
        compoundWriter.write(objectPath, type, data);
    }

    public <T> void writeCompound(String objectPath, T data)
    {
        compoundWriter.write(objectPath, data);
    }

    public <T> void writeCompoundArray(String objectPath, HDF5CompoundType<T> type, T[] data,
            HDF5GenericStorageFeatures features, IByteArrayInspector inspectorOrNull)
    {
        compoundWriter.writeArray(objectPath, type, data, features, inspectorOrNull);
    }

    public <T> void writeCompoundArray(String objectPath, HDF5CompoundType<T> type, T[] data,
            HDF5GenericStorageFeatures features)
    {
        compoundWriter.writeArray(objectPath, type, data, features);
    }

    public <T> void writeCompoundArray(String objectPath, HDF5CompoundType<T> type, T[] data)
    {
        compoundWriter.writeArray(objectPath, type, data);
    }

    public <T> void writeCompoundArrayBlock(String objectPath, HDF5CompoundType<T> type, T[] data,
            long blockNumber, IByteArrayInspector inspectorOrNull)
    {
        compoundWriter.writeArrayBlock(objectPath, type, data, blockNumber, inspectorOrNull);
    }

    public <T> void writeCompoundArrayBlock(String objectPath, HDF5CompoundType<T> type, T[] data,
            long blockNumber)
    {
        compoundWriter.writeArrayBlock(objectPath, type, data, blockNumber);
    }

    public <T> void writeCompoundArrayBlockWithOffset(String objectPath, HDF5CompoundType<T> type,
            T[] data, long offset, IByteArrayInspector inspectorOrNull)
    {
        compoundWriter.writeArrayBlockWithOffset(objectPath, type, data, offset, inspectorOrNull);
    }

    public <T> void writeCompoundArrayBlockWithOffset(String objectPath, HDF5CompoundType<T> type,
            T[] data, long offset)
    {
        compoundWriter.writeArrayBlockWithOffset(objectPath, type, data, offset);
    }

    public <T> void writeCompoundMDArray(String objectPath, HDF5CompoundType<T> type,
            MDArray<T> data, HDF5GenericStorageFeatures features,
            IByteArrayInspector inspectorOrNull)
    {
        compoundWriter.writeMDArray(objectPath, type, data, features, inspectorOrNull);
    }

    public <T> void writeCompoundMDArray(String objectPath, HDF5CompoundType<T> type,
            MDArray<T> data, HDF5GenericStorageFeatures features)
    {
        compoundWriter.writeMDArray(objectPath, type, data, features);
    }

    public <T> void writeCompoundMDArray(String objectPath, HDF5CompoundType<T> type,
            MDArray<T> data)
    {
        compoundWriter.writeMDArray(objectPath, type, data);
    }

    public <T> void writeCompoundArray(String objectPath, T[] data)
    {
        compoundWriter.writeArray(objectPath, data);
    }

    public <T> void writeCompoundArray(String objectPath, T[] data,
            HDF5GenericStorageFeatures features)
    {
        compoundWriter.writeArray(objectPath, data, features);
    }

    public <T> void writeCompoundMDArray(String objectPath, MDArray<T> data)
    {
        compoundWriter.writeMDArray(objectPath, data);
    }

    public <T> void writeCompoundMDArray(String objectPath, MDArray<T> data,
            HDF5GenericStorageFeatures features)
    {
        compoundWriter.writeMDArray(objectPath, data, features);
    }

    public <T> void writeCompoundMDArrayBlock(String objectPath, HDF5CompoundType<T> type,
            MDArray<T> data, long[] blockDimensions, IByteArrayInspector inspectorOrNull)
    {
        compoundWriter.writeMDArrayBlock(objectPath, type, data, blockDimensions, inspectorOrNull);
    }

    public <T> void writeCompoundMDArrayBlock(String objectPath, HDF5CompoundType<T> type,
            MDArray<T> data, long[] blockDimensions)
    {
        compoundWriter.writeMDArrayBlock(objectPath, type, data, blockDimensions);
    }

    public <T> void writeCompoundMDArrayBlockWithOffset(String objectPath,
            HDF5CompoundType<T> type, MDArray<T> data, int[] blockDimensions, long[] offset,
            int[] memoryOffset, IByteArrayInspector inspectorOrNull)
    {
        compoundWriter.writeMDArrayBlockWithOffset(objectPath, type, data, blockDimensions, offset,
                memoryOffset, inspectorOrNull);
    }

    public <T> void writeCompoundMDArrayBlockWithOffset(String objectPath,
            HDF5CompoundType<T> type, MDArray<T> data, int[] blockDimensions, long[] offset,
            int[] memoryOffset)
    {
        compoundWriter.writeMDArrayBlockWithOffset(objectPath, type, data, blockDimensions, offset,
                memoryOffset);
    }

    public <T> void writeCompoundMDArrayBlockWithOffset(String objectPath,
            HDF5CompoundType<T> type, MDArray<T> data, long[] offset,
            IByteArrayInspector inspectorOrNull)
    {
        compoundWriter.writeMDArrayBlockWithOffset(objectPath, type, data, offset, inspectorOrNull);
    }

    public <T> void writeCompoundMDArrayBlockWithOffset(String objectPath,
            HDF5CompoundType<T> type, MDArray<T> data, long[] offset)
    {
        compoundWriter.writeMDArrayBlockWithOffset(objectPath, type, data, offset);
    }

    @Override
    public <T> HDF5CompoundMemberInformation[] getCompoundMemberInformation(Class<T> compoundClass)
    {
        return compoundWriter.getMemberInfo(compoundClass);
    }

    @Override
    public HDF5CompoundMemberInformation[] getCompoundMemberInformation(String dataTypeName)
    {
        return compoundWriter.getMemberInfo(dataTypeName);
    }

    @Override
    public HDF5CompoundMemberInformation[] getCompoundDataSetInformation(String dataSetPath)
            throws HDF5JavaException
    {
        return compoundWriter.getDataSetInfo(dataSetPath);
    }

    @Override
    public HDF5CompoundType<List<?>> getInferredCompoundType(String name, List<String> memberNames,
            List<?> template)
    {
        return compoundWriter.getInferredType(name, memberNames, template);
    }

    @Override
    public HDF5CompoundType<List<?>> getInferredCompoundType(List<String> memberNames,
            List<?> template)
    {
        return compoundWriter.getInferredType(memberNames, template);
    }

    @Override
    public HDF5CompoundType<Object[]> getInferredCompoundType(String name, String[] memberNames,
            Object[] template)
    {
        return compoundWriter.getInferredType(name, memberNames, template);
    }

    @Override
    public HDF5CompoundType<Object[]> getInferredCompoundType(String[] memberNames,
            Object[] template)
    {
        return compoundWriter.getInferredType(memberNames, template);
    }

    @Override
    public <T> HDF5CompoundType<T> getDataSetCompoundType(String objectPath, Class<T> pojoClass)
    {
        return compoundWriter.getDataSetType(objectPath, pojoClass);
    }

    @Override
    public <T> HDF5CompoundType<T> getNamedCompoundType(String dataTypeName, Class<T> pojoClass)
    {
        return compoundWriter.getNamedType(dataTypeName, pojoClass);
    }

    @Override
    public <T> HDF5CompoundType<T> getNamedCompoundType(Class<T> pojoClass)
    {
        return compoundWriter.getNamedType(pojoClass);
    }

    // ------------------------------------------------------------------------------
    // GENERATED CODE SECTION - START
    // ------------------------------------------------------------------------------

    public void createByteArray(String objectPath, int blockSize)
    {
        byteWriter.createByteArray(objectPath, blockSize);
    }

    public void createByteArray(String objectPath, long size, int blockSize)
    {
        byteWriter.createByteArray(objectPath, size, blockSize);
    }

    public void createByteArray(String objectPath, int size, HDF5IntStorageFeatures features)
    {
        byteWriter.createByteArray(objectPath, size, features);
    }

    public void createByteArray(String objectPath, long size, int blockSize,
            HDF5IntStorageFeatures features)
    {
        byteWriter.createByteArray(objectPath, size, blockSize, features);
    }

    public void createByteMDArray(String objectPath, int[] blockDimensions)
    {
        byteWriter.createByteMDArray(objectPath, blockDimensions);
    }

    public void createByteMDArray(String objectPath, long[] dimensions, int[] blockDimensions)
    {
        byteWriter.createByteMDArray(objectPath, dimensions, blockDimensions);
    }

    public void createByteMDArray(String objectPath, int[] dimensions,
            HDF5IntStorageFeatures features)
    {
        byteWriter.createByteMDArray(objectPath, dimensions, features);
    }

    public void createByteMDArray(String objectPath, long[] dimensions, int[] blockDimensions,
            HDF5IntStorageFeatures features)
    {
        byteWriter.createByteMDArray(objectPath, dimensions, blockDimensions, features);
    }

    public void createByteMatrix(String objectPath, int blockSizeX, int blockSizeY)
    {
        byteWriter.createByteMatrix(objectPath, blockSizeX, blockSizeY);
    }

    public void createByteMatrix(String objectPath, long sizeX, long sizeY, int blockSizeX,
            int blockSizeY)
    {
        byteWriter.createByteMatrix(objectPath, sizeX, sizeY, blockSizeX, blockSizeY);
    }

    public void createByteMatrix(String objectPath, long sizeX, long sizeY, int blockSizeX,
            int blockSizeY, HDF5IntStorageFeatures features)
    {
        byteWriter.createByteMatrix(objectPath, sizeX, sizeY, blockSizeX, blockSizeY, features);
    }

    public void setByteArrayAttribute(String objectPath, String name, byte[] value)
    {
        byteWriter.setByteArrayAttribute(objectPath, name, value);
    }

    public void setByteAttribute(String objectPath, String name, byte value)
    {
        byteWriter.setByteAttribute(objectPath, name, value);
    }

    public void setByteMDArrayAttribute(String objectPath, String name, MDByteArray value)
    {
        byteWriter.setByteMDArrayAttribute(objectPath, name, value);
    }

    public void setByteMatrixAttribute(String objectPath, String name, byte[][] value)
    {
        byteWriter.setByteMatrixAttribute(objectPath, name, value);
    }

    public void writeByte(String objectPath, byte value)
    {
        byteWriter.writeByte(objectPath, value);
    }

    public void writeByteArray(String objectPath, byte[] data)
    {
        byteWriter.writeByteArray(objectPath, data);
    }

    public void writeByteArray(String objectPath, byte[] data, HDF5IntStorageFeatures features)
    {
        byteWriter.writeByteArray(objectPath, data, features);
    }

    public void writeByteArrayBlock(String objectPath, byte[] data, long blockNumber)
    {
        byteWriter.writeByteArrayBlock(objectPath, data, blockNumber);
    }

    public void writeByteArrayBlockWithOffset(String objectPath, byte[] data, int dataSize,
            long offset)
    {
        byteWriter.writeByteArrayBlockWithOffset(objectPath, data, dataSize, offset);
    }

    public void writeByteMDArray(String objectPath, MDByteArray data)
    {
        byteWriter.writeByteMDArray(objectPath, data);
    }

    public void writeByteMDArray(String objectPath, MDByteArray data,
            HDF5IntStorageFeatures features)
    {
        byteWriter.writeByteMDArray(objectPath, data, features);
    }

    public void writeByteMDArrayBlock(String objectPath, MDByteArray data, long[] blockNumber)
    {
        byteWriter.writeByteMDArrayBlock(objectPath, data, blockNumber);
    }

    public void writeByteMDArrayBlockWithOffset(String objectPath, MDByteArray data, long[] offset)
    {
        byteWriter.writeByteMDArrayBlockWithOffset(objectPath, data, offset);
    }

    public void writeByteMDArrayBlockWithOffset(String objectPath, MDByteArray data,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        byteWriter.writeByteMDArrayBlockWithOffset(objectPath, data, blockDimensions, offset,
                memoryOffset);
    }

    public void writeByteMatrix(String objectPath, byte[][] data)
    {
        byteWriter.writeByteMatrix(objectPath, data);
    }

    public void writeByteMatrix(String objectPath, byte[][] data, HDF5IntStorageFeatures features)
    {
        byteWriter.writeByteMatrix(objectPath, data, features);
    }

    public void writeByteMatrixBlock(String objectPath, byte[][] data, long blockNumberX,
            long blockNumberY)
    {
        byteWriter.writeByteMatrixBlock(objectPath, data, blockNumberX, blockNumberY);
    }

    public void writeByteMatrixBlockWithOffset(String objectPath, byte[][] data, long offsetX,
            long offsetY)
    {
        byteWriter.writeByteMatrixBlockWithOffset(objectPath, data, offsetX, offsetY);
    }

    public void writeByteMatrixBlockWithOffset(String objectPath, byte[][] data, int dataSizeX,
            int dataSizeY, long offsetX, long offsetY)
    {
        byteWriter.writeByteMatrixBlockWithOffset(objectPath, data, dataSizeX, dataSizeY, offsetX,
                offsetY);
    }

    public void createDoubleArray(String objectPath, int blockSize)
    {
        doubleWriter.createDoubleArray(objectPath, blockSize);
    }

    public void createDoubleArray(String objectPath, long size, int blockSize)
    {
        doubleWriter.createDoubleArray(objectPath, size, blockSize);
    }

    public void createDoubleArray(String objectPath, int size, HDF5FloatStorageFeatures features)
    {
        doubleWriter.createDoubleArray(objectPath, size, features);
    }

    public void createDoubleArray(String objectPath, long size, int blockSize,
            HDF5FloatStorageFeatures features)
    {
        doubleWriter.createDoubleArray(objectPath, size, blockSize, features);
    }

    public void createDoubleMDArray(String objectPath, int[] blockDimensions)
    {
        doubleWriter.createDoubleMDArray(objectPath, blockDimensions);
    }

    public void createDoubleMDArray(String objectPath, long[] dimensions, int[] blockDimensions)
    {
        doubleWriter.createDoubleMDArray(objectPath, dimensions, blockDimensions);
    }

    public void createDoubleMDArray(String objectPath, int[] dimensions,
            HDF5FloatStorageFeatures features)
    {
        doubleWriter.createDoubleMDArray(objectPath, dimensions, features);
    }

    public void createDoubleMDArray(String objectPath, long[] dimensions, int[] blockDimensions,
            HDF5FloatStorageFeatures features)
    {
        doubleWriter.createDoubleMDArray(objectPath, dimensions, blockDimensions, features);
    }

    public void createDoubleMatrix(String objectPath, int blockSizeX, int blockSizeY)
    {
        doubleWriter.createDoubleMatrix(objectPath, blockSizeX, blockSizeY);
    }

    public void createDoubleMatrix(String objectPath, long sizeX, long sizeY, int blockSizeX,
            int blockSizeY)
    {
        doubleWriter.createDoubleMatrix(objectPath, sizeX, sizeY, blockSizeX, blockSizeY);
    }

    public void createDoubleMatrix(String objectPath, long sizeX, long sizeY, int blockSizeX,
            int blockSizeY, HDF5FloatStorageFeatures features)
    {
        doubleWriter.createDoubleMatrix(objectPath, sizeX, sizeY, blockSizeX, blockSizeY, features);
    }

    public void setDoubleArrayAttribute(String objectPath, String name, double[] value)
    {
        doubleWriter.setDoubleArrayAttribute(objectPath, name, value);
    }

    public void setDoubleAttribute(String objectPath, String name, double value)
    {
        doubleWriter.setDoubleAttribute(objectPath, name, value);
    }

    public void setDoubleMDArrayAttribute(String objectPath, String name, MDDoubleArray value)
    {
        doubleWriter.setDoubleMDArrayAttribute(objectPath, name, value);
    }

    public void setDoubleMatrixAttribute(String objectPath, String name, double[][] value)
    {
        doubleWriter.setDoubleMatrixAttribute(objectPath, name, value);
    }

    public void writeDouble(String objectPath, double value)
    {
        doubleWriter.writeDouble(objectPath, value);
    }

    public void writeDoubleArray(String objectPath, double[] data)
    {
        doubleWriter.writeDoubleArray(objectPath, data);
    }

    public void writeDoubleArray(String objectPath, double[] data, HDF5FloatStorageFeatures features)
    {
        doubleWriter.writeDoubleArray(objectPath, data, features);
    }

    public void writeDoubleArrayBlock(String objectPath, double[] data, long blockNumber)
    {
        doubleWriter.writeDoubleArrayBlock(objectPath, data, blockNumber);
    }

    public void writeDoubleArrayBlockWithOffset(String objectPath, double[] data, int dataSize,
            long offset)
    {
        doubleWriter.writeDoubleArrayBlockWithOffset(objectPath, data, dataSize, offset);
    }

    public void writeDoubleMDArray(String objectPath, MDDoubleArray data)
    {
        doubleWriter.writeDoubleMDArray(objectPath, data);
    }

    public void writeDoubleMDArray(String objectPath, MDDoubleArray data,
            HDF5FloatStorageFeatures features)
    {
        doubleWriter.writeDoubleMDArray(objectPath, data, features);
    }

    public void writeDoubleMDArrayBlock(String objectPath, MDDoubleArray data, long[] blockNumber)
    {
        doubleWriter.writeDoubleMDArrayBlock(objectPath, data, blockNumber);
    }

    public void writeDoubleMDArrayBlockWithOffset(String objectPath, MDDoubleArray data,
            long[] offset)
    {
        doubleWriter.writeDoubleMDArrayBlockWithOffset(objectPath, data, offset);
    }

    public void writeDoubleMDArrayBlockWithOffset(String objectPath, MDDoubleArray data,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        doubleWriter.writeDoubleMDArrayBlockWithOffset(objectPath, data, blockDimensions, offset,
                memoryOffset);
    }

    public void writeDoubleMatrix(String objectPath, double[][] data)
    {
        doubleWriter.writeDoubleMatrix(objectPath, data);
    }

    public void writeDoubleMatrix(String objectPath, double[][] data,
            HDF5FloatStorageFeatures features)
    {
        doubleWriter.writeDoubleMatrix(objectPath, data, features);
    }

    public void writeDoubleMatrixBlock(String objectPath, double[][] data, long blockNumberX,
            long blockNumberY)
    {
        doubleWriter.writeDoubleMatrixBlock(objectPath, data, blockNumberX, blockNumberY);
    }

    public void writeDoubleMatrixBlockWithOffset(String objectPath, double[][] data, long offsetX,
            long offsetY)
    {
        doubleWriter.writeDoubleMatrixBlockWithOffset(objectPath, data, offsetX, offsetY);
    }

    public void writeDoubleMatrixBlockWithOffset(String objectPath, double[][] data, int dataSizeX,
            int dataSizeY, long offsetX, long offsetY)
    {
        doubleWriter.writeDoubleMatrixBlockWithOffset(objectPath, data, dataSizeX, dataSizeY,
                offsetX, offsetY);
    }

    public void createFloatArray(String objectPath, int blockSize)
    {
        floatWriter.createFloatArray(objectPath, blockSize);
    }

    public void createFloatArray(String objectPath, long size, int blockSize)
    {
        floatWriter.createFloatArray(objectPath, size, blockSize);
    }

    public void createFloatArray(String objectPath, int size, HDF5FloatStorageFeatures features)
    {
        floatWriter.createFloatArray(objectPath, size, features);
    }

    public void createFloatArray(String objectPath, long size, int blockSize,
            HDF5FloatStorageFeatures features)
    {
        floatWriter.createFloatArray(objectPath, size, blockSize, features);
    }

    public void createFloatMDArray(String objectPath, int[] blockDimensions)
    {
        floatWriter.createFloatMDArray(objectPath, blockDimensions);
    }

    public void createFloatMDArray(String objectPath, long[] dimensions, int[] blockDimensions)
    {
        floatWriter.createFloatMDArray(objectPath, dimensions, blockDimensions);
    }

    public void createFloatMDArray(String objectPath, int[] dimensions,
            HDF5FloatStorageFeatures features)
    {
        floatWriter.createFloatMDArray(objectPath, dimensions, features);
    }

    public void createFloatMDArray(String objectPath, long[] dimensions, int[] blockDimensions,
            HDF5FloatStorageFeatures features)
    {
        floatWriter.createFloatMDArray(objectPath, dimensions, blockDimensions, features);
    }

    public void createFloatMatrix(String objectPath, int blockSizeX, int blockSizeY)
    {
        floatWriter.createFloatMatrix(objectPath, blockSizeX, blockSizeY);
    }

    public void createFloatMatrix(String objectPath, long sizeX, long sizeY, int blockSizeX,
            int blockSizeY)
    {
        floatWriter.createFloatMatrix(objectPath, sizeX, sizeY, blockSizeX, blockSizeY);
    }

    public void createFloatMatrix(String objectPath, long sizeX, long sizeY, int blockSizeX,
            int blockSizeY, HDF5FloatStorageFeatures features)
    {
        floatWriter.createFloatMatrix(objectPath, sizeX, sizeY, blockSizeX, blockSizeY, features);
    }

    public void setFloatArrayAttribute(String objectPath, String name, float[] value)
    {
        floatWriter.setFloatArrayAttribute(objectPath, name, value);
    }

    public void setFloatAttribute(String objectPath, String name, float value)
    {
        floatWriter.setFloatAttribute(objectPath, name, value);
    }

    public void setFloatMDArrayAttribute(String objectPath, String name, MDFloatArray value)
    {
        floatWriter.setFloatMDArrayAttribute(objectPath, name, value);
    }

    public void setFloatMatrixAttribute(String objectPath, String name, float[][] value)
    {
        floatWriter.setFloatMatrixAttribute(objectPath, name, value);
    }

    public void writeFloat(String objectPath, float value)
    {
        floatWriter.writeFloat(objectPath, value);
    }

    public void writeFloatArray(String objectPath, float[] data)
    {
        floatWriter.writeFloatArray(objectPath, data);
    }

    public void writeFloatArray(String objectPath, float[] data, HDF5FloatStorageFeatures features)
    {
        floatWriter.writeFloatArray(objectPath, data, features);
    }

    public void writeFloatArrayBlock(String objectPath, float[] data, long blockNumber)
    {
        floatWriter.writeFloatArrayBlock(objectPath, data, blockNumber);
    }

    public void writeFloatArrayBlockWithOffset(String objectPath, float[] data, int dataSize,
            long offset)
    {
        floatWriter.writeFloatArrayBlockWithOffset(objectPath, data, dataSize, offset);
    }

    public void writeFloatMDArray(String objectPath, MDFloatArray data)
    {
        floatWriter.writeFloatMDArray(objectPath, data);
    }

    public void writeFloatMDArray(String objectPath, MDFloatArray data,
            HDF5FloatStorageFeatures features)
    {
        floatWriter.writeFloatMDArray(objectPath, data, features);
    }

    public void writeFloatMDArrayBlock(String objectPath, MDFloatArray data, long[] blockNumber)
    {
        floatWriter.writeFloatMDArrayBlock(objectPath, data, blockNumber);
    }

    public void writeFloatMDArrayBlockWithOffset(String objectPath, MDFloatArray data, long[] offset)
    {
        floatWriter.writeFloatMDArrayBlockWithOffset(objectPath, data, offset);
    }

    public void writeFloatMDArrayBlockWithOffset(String objectPath, MDFloatArray data,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        floatWriter.writeFloatMDArrayBlockWithOffset(objectPath, data, blockDimensions, offset,
                memoryOffset);
    }

    public void writeFloatMatrix(String objectPath, float[][] data)
    {
        floatWriter.writeFloatMatrix(objectPath, data);
    }

    public void writeFloatMatrix(String objectPath, float[][] data,
            HDF5FloatStorageFeatures features)
    {
        floatWriter.writeFloatMatrix(objectPath, data, features);
    }

    public void writeFloatMatrixBlock(String objectPath, float[][] data, long blockNumberX,
            long blockNumberY)
    {
        floatWriter.writeFloatMatrixBlock(objectPath, data, blockNumberX, blockNumberY);
    }

    public void writeFloatMatrixBlockWithOffset(String objectPath, float[][] data, long offsetX,
            long offsetY)
    {
        floatWriter.writeFloatMatrixBlockWithOffset(objectPath, data, offsetX, offsetY);
    }

    public void writeFloatMatrixBlockWithOffset(String objectPath, float[][] data, int dataSizeX,
            int dataSizeY, long offsetX, long offsetY)
    {
        floatWriter.writeFloatMatrixBlockWithOffset(objectPath, data, dataSizeX, dataSizeY,
                offsetX, offsetY);
    }

    public void createIntArray(String objectPath, int blockSize)
    {
        intWriter.createIntArray(objectPath, blockSize);
    }

    public void createIntArray(String objectPath, long size, int blockSize)
    {
        intWriter.createIntArray(objectPath, size, blockSize);
    }

    public void createIntArray(String objectPath, int size, HDF5IntStorageFeatures features)
    {
        intWriter.createIntArray(objectPath, size, features);
    }

    public void createIntArray(String objectPath, long size, int blockSize,
            HDF5IntStorageFeatures features)
    {
        intWriter.createIntArray(objectPath, size, blockSize, features);
    }

    public void createIntMDArray(String objectPath, int[] blockDimensions)
    {
        intWriter.createIntMDArray(objectPath, blockDimensions);
    }

    public void createIntMDArray(String objectPath, long[] dimensions, int[] blockDimensions)
    {
        intWriter.createIntMDArray(objectPath, dimensions, blockDimensions);
    }

    public void createIntMDArray(String objectPath, int[] dimensions,
            HDF5IntStorageFeatures features)
    {
        intWriter.createIntMDArray(objectPath, dimensions, features);
    }

    public void createIntMDArray(String objectPath, long[] dimensions, int[] blockDimensions,
            HDF5IntStorageFeatures features)
    {
        intWriter.createIntMDArray(objectPath, dimensions, blockDimensions, features);
    }

    public void createIntMatrix(String objectPath, int blockSizeX, int blockSizeY)
    {
        intWriter.createIntMatrix(objectPath, blockSizeX, blockSizeY);
    }

    public void createIntMatrix(String objectPath, long sizeX, long sizeY, int blockSizeX,
            int blockSizeY)
    {
        intWriter.createIntMatrix(objectPath, sizeX, sizeY, blockSizeX, blockSizeY);
    }

    public void createIntMatrix(String objectPath, long sizeX, long sizeY, int blockSizeX,
            int blockSizeY, HDF5IntStorageFeatures features)
    {
        intWriter.createIntMatrix(objectPath, sizeX, sizeY, blockSizeX, blockSizeY, features);
    }

    public void setIntArrayAttribute(String objectPath, String name, int[] value)
    {
        intWriter.setIntArrayAttribute(objectPath, name, value);
    }

    public void setIntAttribute(String objectPath, String name, int value)
    {
        intWriter.setIntAttribute(objectPath, name, value);
    }

    public void setIntMDArrayAttribute(String objectPath, String name, MDIntArray value)
    {
        intWriter.setIntMDArrayAttribute(objectPath, name, value);
    }

    public void setIntMatrixAttribute(String objectPath, String name, int[][] value)
    {
        intWriter.setIntMatrixAttribute(objectPath, name, value);
    }

    public void writeInt(String objectPath, int value)
    {
        intWriter.writeInt(objectPath, value);
    }

    public void writeIntArray(String objectPath, int[] data)
    {
        intWriter.writeIntArray(objectPath, data);
    }

    public void writeIntArray(String objectPath, int[] data, HDF5IntStorageFeatures features)
    {
        intWriter.writeIntArray(objectPath, data, features);
    }

    public void writeIntArrayBlock(String objectPath, int[] data, long blockNumber)
    {
        intWriter.writeIntArrayBlock(objectPath, data, blockNumber);
    }

    public void writeIntArrayBlockWithOffset(String objectPath, int[] data, int dataSize,
            long offset)
    {
        intWriter.writeIntArrayBlockWithOffset(objectPath, data, dataSize, offset);
    }

    public void writeIntMDArray(String objectPath, MDIntArray data)
    {
        intWriter.writeIntMDArray(objectPath, data);
    }

    public void writeIntMDArray(String objectPath, MDIntArray data, HDF5IntStorageFeatures features)
    {
        intWriter.writeIntMDArray(objectPath, data, features);
    }

    public void writeIntMDArrayBlock(String objectPath, MDIntArray data, long[] blockNumber)
    {
        intWriter.writeIntMDArrayBlock(objectPath, data, blockNumber);
    }

    public void writeIntMDArrayBlockWithOffset(String objectPath, MDIntArray data, long[] offset)
    {
        intWriter.writeIntMDArrayBlockWithOffset(objectPath, data, offset);
    }

    public void writeIntMDArrayBlockWithOffset(String objectPath, MDIntArray data,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        intWriter.writeIntMDArrayBlockWithOffset(objectPath, data, blockDimensions, offset,
                memoryOffset);
    }

    public void writeIntMatrix(String objectPath, int[][] data)
    {
        intWriter.writeIntMatrix(objectPath, data);
    }

    public void writeIntMatrix(String objectPath, int[][] data, HDF5IntStorageFeatures features)
    {
        intWriter.writeIntMatrix(objectPath, data, features);
    }

    public void writeIntMatrixBlock(String objectPath, int[][] data, long blockNumberX,
            long blockNumberY)
    {
        intWriter.writeIntMatrixBlock(objectPath, data, blockNumberX, blockNumberY);
    }

    public void writeIntMatrixBlockWithOffset(String objectPath, int[][] data, long offsetX,
            long offsetY)
    {
        intWriter.writeIntMatrixBlockWithOffset(objectPath, data, offsetX, offsetY);
    }

    public void writeIntMatrixBlockWithOffset(String objectPath, int[][] data, int dataSizeX,
            int dataSizeY, long offsetX, long offsetY)
    {
        intWriter.writeIntMatrixBlockWithOffset(objectPath, data, dataSizeX, dataSizeY, offsetX,
                offsetY);
    }

    public void createLongArray(String objectPath, int blockSize)
    {
        longWriter.createLongArray(objectPath, blockSize);
    }

    public void createLongArray(String objectPath, long size, int blockSize)
    {
        longWriter.createLongArray(objectPath, size, blockSize);
    }

    public void createLongArray(String objectPath, int size, HDF5IntStorageFeatures features)
    {
        longWriter.createLongArray(objectPath, size, features);
    }

    public void createLongArray(String objectPath, long size, int blockSize,
            HDF5IntStorageFeatures features)
    {
        longWriter.createLongArray(objectPath, size, blockSize, features);
    }

    public void createLongMDArray(String objectPath, int[] blockDimensions)
    {
        longWriter.createLongMDArray(objectPath, blockDimensions);
    }

    public void createLongMDArray(String objectPath, long[] dimensions, int[] blockDimensions)
    {
        longWriter.createLongMDArray(objectPath, dimensions, blockDimensions);
    }

    public void createLongMDArray(String objectPath, int[] dimensions,
            HDF5IntStorageFeatures features)
    {
        longWriter.createLongMDArray(objectPath, dimensions, features);
    }

    public void createLongMDArray(String objectPath, long[] dimensions, int[] blockDimensions,
            HDF5IntStorageFeatures features)
    {
        longWriter.createLongMDArray(objectPath, dimensions, blockDimensions, features);
    }

    public void createLongMatrix(String objectPath, int blockSizeX, int blockSizeY)
    {
        longWriter.createLongMatrix(objectPath, blockSizeX, blockSizeY);
    }

    public void createLongMatrix(String objectPath, long sizeX, long sizeY, int blockSizeX,
            int blockSizeY)
    {
        longWriter.createLongMatrix(objectPath, sizeX, sizeY, blockSizeX, blockSizeY);
    }

    public void createLongMatrix(String objectPath, long sizeX, long sizeY, int blockSizeX,
            int blockSizeY, HDF5IntStorageFeatures features)
    {
        longWriter.createLongMatrix(objectPath, sizeX, sizeY, blockSizeX, blockSizeY, features);
    }

    public void setLongArrayAttribute(String objectPath, String name, long[] value)
    {
        longWriter.setLongArrayAttribute(objectPath, name, value);
    }

    public void setLongAttribute(String objectPath, String name, long value)
    {
        longWriter.setLongAttribute(objectPath, name, value);
    }

    public void setLongMDArrayAttribute(String objectPath, String name, MDLongArray value)
    {
        longWriter.setLongMDArrayAttribute(objectPath, name, value);
    }

    public void setLongMatrixAttribute(String objectPath, String name, long[][] value)
    {
        longWriter.setLongMatrixAttribute(objectPath, name, value);
    }

    public void writeLong(String objectPath, long value)
    {
        longWriter.writeLong(objectPath, value);
    }

    public void writeLongArray(String objectPath, long[] data)
    {
        longWriter.writeLongArray(objectPath, data);
    }

    public void writeLongArray(String objectPath, long[] data, HDF5IntStorageFeatures features)
    {
        longWriter.writeLongArray(objectPath, data, features);
    }

    public void writeLongArrayBlock(String objectPath, long[] data, long blockNumber)
    {
        longWriter.writeLongArrayBlock(objectPath, data, blockNumber);
    }

    public void writeLongArrayBlockWithOffset(String objectPath, long[] data, int dataSize,
            long offset)
    {
        longWriter.writeLongArrayBlockWithOffset(objectPath, data, dataSize, offset);
    }

    public void writeLongMDArray(String objectPath, MDLongArray data)
    {
        longWriter.writeLongMDArray(objectPath, data);
    }

    public void writeLongMDArray(String objectPath, MDLongArray data,
            HDF5IntStorageFeatures features)
    {
        longWriter.writeLongMDArray(objectPath, data, features);
    }

    public void writeLongMDArrayBlock(String objectPath, MDLongArray data, long[] blockNumber)
    {
        longWriter.writeLongMDArrayBlock(objectPath, data, blockNumber);
    }

    public void writeLongMDArrayBlockWithOffset(String objectPath, MDLongArray data, long[] offset)
    {
        longWriter.writeLongMDArrayBlockWithOffset(objectPath, data, offset);
    }

    public void writeLongMDArrayBlockWithOffset(String objectPath, MDLongArray data,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        longWriter.writeLongMDArrayBlockWithOffset(objectPath, data, blockDimensions, offset,
                memoryOffset);
    }

    public void writeLongMatrix(String objectPath, long[][] data)
    {
        longWriter.writeLongMatrix(objectPath, data);
    }

    public void writeLongMatrix(String objectPath, long[][] data, HDF5IntStorageFeatures features)
    {
        longWriter.writeLongMatrix(objectPath, data, features);
    }

    public void writeLongMatrixBlock(String objectPath, long[][] data, long blockNumberX,
            long blockNumberY)
    {
        longWriter.writeLongMatrixBlock(objectPath, data, blockNumberX, blockNumberY);
    }

    public void writeLongMatrixBlockWithOffset(String objectPath, long[][] data, long offsetX,
            long offsetY)
    {
        longWriter.writeLongMatrixBlockWithOffset(objectPath, data, offsetX, offsetY);
    }

    public void writeLongMatrixBlockWithOffset(String objectPath, long[][] data, int dataSizeX,
            int dataSizeY, long offsetX, long offsetY)
    {
        longWriter.writeLongMatrixBlockWithOffset(objectPath, data, dataSizeX, dataSizeY, offsetX,
                offsetY);
    }

    public void createShortArray(String objectPath, int blockSize)
    {
        shortWriter.createShortArray(objectPath, blockSize);
    }

    public void createShortArray(String objectPath, long size, int blockSize)
    {
        shortWriter.createShortArray(objectPath, size, blockSize);
    }

    public void createShortArray(String objectPath, int size, HDF5IntStorageFeatures features)
    {
        shortWriter.createShortArray(objectPath, size, features);
    }

    public void createShortArray(String objectPath, long size, int blockSize,
            HDF5IntStorageFeatures features)
    {
        shortWriter.createShortArray(objectPath, size, blockSize, features);
    }

    public void createShortMDArray(String objectPath, int[] blockDimensions)
    {
        shortWriter.createShortMDArray(objectPath, blockDimensions);
    }

    public void createShortMDArray(String objectPath, long[] dimensions, int[] blockDimensions)
    {
        shortWriter.createShortMDArray(objectPath, dimensions, blockDimensions);
    }

    public void createShortMDArray(String objectPath, int[] dimensions,
            HDF5IntStorageFeatures features)
    {
        shortWriter.createShortMDArray(objectPath, dimensions, features);
    }

    public void createShortMDArray(String objectPath, long[] dimensions, int[] blockDimensions,
            HDF5IntStorageFeatures features)
    {
        shortWriter.createShortMDArray(objectPath, dimensions, blockDimensions, features);
    }

    public void createShortMatrix(String objectPath, int blockSizeX, int blockSizeY)
    {
        shortWriter.createShortMatrix(objectPath, blockSizeX, blockSizeY);
    }

    public void createShortMatrix(String objectPath, long sizeX, long sizeY, int blockSizeX,
            int blockSizeY)
    {
        shortWriter.createShortMatrix(objectPath, sizeX, sizeY, blockSizeX, blockSizeY);
    }

    public void createShortMatrix(String objectPath, long sizeX, long sizeY, int blockSizeX,
            int blockSizeY, HDF5IntStorageFeatures features)
    {
        shortWriter.createShortMatrix(objectPath, sizeX, sizeY, blockSizeX, blockSizeY, features);
    }

    public void setShortArrayAttribute(String objectPath, String name, short[] value)
    {
        shortWriter.setShortArrayAttribute(objectPath, name, value);
    }

    public void setShortAttribute(String objectPath, String name, short value)
    {
        shortWriter.setShortAttribute(objectPath, name, value);
    }

    public void setShortMDArrayAttribute(String objectPath, String name, MDShortArray value)
    {
        shortWriter.setShortMDArrayAttribute(objectPath, name, value);
    }

    public void setShortMatrixAttribute(String objectPath, String name, short[][] value)
    {
        shortWriter.setShortMatrixAttribute(objectPath, name, value);
    }

    public void writeShort(String objectPath, short value)
    {
        shortWriter.writeShort(objectPath, value);
    }

    public void writeShortArray(String objectPath, short[] data)
    {
        shortWriter.writeShortArray(objectPath, data);
    }

    public void writeShortArray(String objectPath, short[] data, HDF5IntStorageFeatures features)
    {
        shortWriter.writeShortArray(objectPath, data, features);
    }

    public void writeShortArrayBlock(String objectPath, short[] data, long blockNumber)
    {
        shortWriter.writeShortArrayBlock(objectPath, data, blockNumber);
    }

    public void writeShortArrayBlockWithOffset(String objectPath, short[] data, int dataSize,
            long offset)
    {
        shortWriter.writeShortArrayBlockWithOffset(objectPath, data, dataSize, offset);
    }

    public void writeShortMDArray(String objectPath, MDShortArray data)
    {
        shortWriter.writeShortMDArray(objectPath, data);
    }

    public void writeShortMDArray(String objectPath, MDShortArray data,
            HDF5IntStorageFeatures features)
    {
        shortWriter.writeShortMDArray(objectPath, data, features);
    }

    public void writeShortMDArrayBlock(String objectPath, MDShortArray data, long[] blockNumber)
    {
        shortWriter.writeShortMDArrayBlock(objectPath, data, blockNumber);
    }

    public void writeShortMDArrayBlockWithOffset(String objectPath, MDShortArray data, long[] offset)
    {
        shortWriter.writeShortMDArrayBlockWithOffset(objectPath, data, offset);
    }

    public void writeShortMDArrayBlockWithOffset(String objectPath, MDShortArray data,
            int[] blockDimensions, long[] offset, int[] memoryOffset)
    {
        shortWriter.writeShortMDArrayBlockWithOffset(objectPath, data, blockDimensions, offset,
                memoryOffset);
    }

    public void writeShortMatrix(String objectPath, short[][] data)
    {
        shortWriter.writeShortMatrix(objectPath, data);
    }

    public void writeShortMatrix(String objectPath, short[][] data, HDF5IntStorageFeatures features)
    {
        shortWriter.writeShortMatrix(objectPath, data, features);
    }

    public void writeShortMatrixBlock(String objectPath, short[][] data, long blockNumberX,
            long blockNumberY)
    {
        shortWriter.writeShortMatrixBlock(objectPath, data, blockNumberX, blockNumberY);
    }

    public void writeShortMatrixBlockWithOffset(String objectPath, short[][] data, long offsetX,
            long offsetY)
    {
        shortWriter.writeShortMatrixBlockWithOffset(objectPath, data, offsetX, offsetY);
    }

    public void writeShortMatrixBlockWithOffset(String objectPath, short[][] data, int dataSizeX,
            int dataSizeY, long offsetX, long offsetY)
    {
        shortWriter.writeShortMatrixBlockWithOffset(objectPath, data, dataSizeX, dataSizeY,
                offsetX, offsetY);
    }

    // ------------------------------------------------------------------------------
    // GENERATED CODE SECTION - END
    // ------------------------------------------------------------------------------
}
