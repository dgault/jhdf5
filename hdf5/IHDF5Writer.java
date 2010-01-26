/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.BitSet;
import java.util.Date;

import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;
import ncsa.hdf.hdf5lib.exceptions.HDF5SymbolTableException;

import ch.systemsx.cisd.base.mdarray.MDArray;
import ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator.FileFormat;

/**
 * An interface for writing HDF5 files (HDF5 1.6.x or HDF5 1.8.x).
 * <p>
 * The interface focuses on ease of use instead of completeness. As a consequence not all valid HDF5
 * files can be generated using this class, but only a subset.
 * <p>
 * Usage:
 * 
 * <pre>
 * float[] f = new float[100];
 * ...
 * IHDF5Writer writer = HDF5FactoryProvider.get().open(new File(&quot;test.h5&quot;));
 * writer.writeFloatArray(&quot;/some/path/dataset&quot;, f);
 * writer.setStringAttribute(&quot;some key&quot;, &quot;some value&quot;);
 * writer.close();
 * </pre>
 * 
 * @author Bernd Rinn
 */
public interface IHDF5Writer extends IHDF5Reader, IHDF5SimpleWriter, IHDF5PrimitiveWriter
{

    // /////////////////////
    // Configuration
    // /////////////////////

    /**
     * Returns <code>true</code>, if the {@link IHDF5WriterConfigurator} was <em>not</em> configured
     * with {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()}, that is if extendable data
     * types are used for new data sets.
     */
    public boolean isUseExtendableDataTypes();

    /**
     * Returns the {@link FileFormat} compatibility setting for this writer.
     */
    public FileFormat getFileFormat();

    // /////////////////////
    // File
    // /////////////////////

    /**
     * Flushes the cache to disk (without discarding it). Note that this may or may not trigger a
     * <code>fsync(2)</code>, depending on the {@link IHDF5WriterConfigurator.SyncMode} used.
     */
    public void flush();

    /**
     * Flushes the cache to disk (without discarding it) and synchronizes the file with the
     * underlying storage using a method like <code>fsync(2)</code>, regardless of what
     * {@link IHDF5WriterConfigurator.SyncMode} has been set for this file.
     * <p>
     * This method blocks until <code>fsync(2)</code> has returned.
     */
    public void flushSyncBlocking();

    // /////////////////////
    // Objects & Links
    // /////////////////////

    /**
     * Creates a hard link.
     * 
     * @param currentPath The name of the data set (including path information) to create a link to.
     * @param newPath The name (including path information) of the link to create.
     */
    public void createHardLink(String currentPath, String newPath);

    /**
     * Creates a soft link.
     * 
     * @param targetPath The name of the data set (including path information) to create a link to.
     * @param linkPath The name (including path information) of the link to create.
     */
    public void createSoftLink(String targetPath, String linkPath);

    /**
     * Creates or updates a soft link.
     * <p>
     * <em>Note: This method will never overwrite a data set, but only a symbolic link.</em>
     * 
     * @param targetPath The name of the data set (including path information) to create a link to.
     * @param linkPath The name (including path information) of the link to create.
     */
    public void createOrUpdateSoftLink(String targetPath, String linkPath);

    /**
     * Creates an external link, that is a link to a data set in another HDF5 file, the
     * <em>target</em> .
     * <p>
     * <em>Note: This method is only allowed when the {@link IHDF5WriterConfigurator} was not 
     * configured to enforce strict HDF5 1.6 compatibility.</em>
     * 
     * @param targetFileName The name of the file where the data set resides that should be linked.
     * @param targetPath The name of the data set (including path information) in the
     *            <var>targetFileName</var> to create a link to.
     * @param linkPath The name (including path information) of the link to create.
     * @throws IllegalStateException If the {@link IHDF5WriterConfigurator} was configured to
     *             enforce strict HDF5 1.6 compatibility.
     */
    public void createExternalLink(String targetFileName, String targetPath, String linkPath)
            throws IllegalStateException;

    /**
     * Creates or updates an external link, that is a link to a data set in another HDF5 file, the
     * <em>target</em> .
     * <p>
     * <em>Note: This method will never overwrite a data set, but only a symbolic link.</em>
     * <p>
     * <em>Note: This method is only allowed when the {@link IHDF5WriterConfigurator} was not 
     * configured to enforce strict HDF5 1.6 compatibility.</em>
     * 
     * @param targetFileName The name of the file where the data set resides that should be linked.
     * @param targetPath The name of the data set (including path information) in the
     *            <var>targetFileName</var> to create a link to.
     * @param linkPath The name (including path information) of the link to create.
     * @throws IllegalStateException If the {@link IHDF5WriterConfigurator} was configured to
     *             enforce strict HDF5 1.6 compatibility.
     */
    public void createOrUpdateExternalLink(String targetFileName, String targetPath, String linkPath)
            throws IllegalStateException;

    /**
     * Removes an object from the file. If there is more than one link to the object, only the
     * specified link will be removed.
     */
    public void delete(String objectPath);

    /**
     * Moves or renames a link in the file atomically.
     * 
     * @throws HDF5SymbolTableException If <var>oldLinkPath</var> does not exist or if
     *             <var>newLinkPath</var> already exists (minor in both (!) cases:
     *             {@link HDF5Constants#H5E_NOTFOUND}).
     */
    public void move(String oldLinkPath, String newLinkPath) throws HDF5SymbolTableException;

    // /////////////////////
    // Group
    // /////////////////////

    /**
     * Creates a group with path <var>objectPath</var> in the HDF5 file.
     * <p>
     * All intermediate groups will be created as well, if they do not already exist.
     * 
     * @param groupPath The path of the group to create.
     */
    public void createGroup(final String groupPath);

    /**
     * Creates a group with path <var>objectPath</var> in the HDF5 file, giving the library a hint
     * about the size (<var>sizeHint</var>). If you have this information in advance, it will be
     * more efficient to tell it the library rather than to let the library figure out itself, but
     * the hint must not be misunderstood as a limit.
     * <p>
     * All intermediate groups will be created as well, if they do not already exist.
     * <p>
     * <i>Note: This method creates an "old-style group", that is the type of group of HDF5 1.6 and
     * earlier.</i>
     * 
     * @param groupPath The path of the group to create.
     * @param sizeHint The estimated size of all group entries (in bytes).
     */
    public void createGroup(final String groupPath, final int sizeHint);

    /**
     * Creates a group with path <var>objectPath</var> in the HDF5 file, giving the library hints
     * about when to switch between compact and dense. Setting appropriate values may improve
     * performance.
     * <p>
     * All intermediate groups will be created as well, if they do not already exist.
     * <p>
     * <i>Note: This method creates a "new-style group", that is the type of group of HDF5 1.8 and
     * above. Thus it will fail, if the writer is configured to enforce HDF5 1.6 compatibility.</i>
     * 
     * @param groupPath The path of the group to create.
     * @param maxCompact When the group grows to more than this number of entries, the library will
     *            convert the group style from compact to dense.
     * @param minDense When the group shrinks below this number of entries, the library will convert
     *            the group style from dense to compact.
     */
    public void createGroup(final String groupPath, final int maxCompact, final int minDense);

    // /////////////////////
    // Attributes
    // /////////////////////

    /**
     * Deletes an attribute.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to delete the attribute from.
     * @param name The name of the attribute to delete.
     */
    public void deleteAttribute(final String objectPath, final String name);

    /**
     * Sets a <var>typeVariant</var> to <var>objectPath</var>.
     * 
     * @param objectPath The name of the object to add the type variant to.
     * @param typeVariant The type variant to add.
     */
    public void setTypeVariant(final String objectPath, final HDF5DataTypeVariant typeVariant);

    /**
     * Deletes the <var>typeVariant</var> from <var>objectPath</var>.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to delete the type variant from.
     */
    public void deleteTypeVariant(final String objectPath);

    /**
     * Sets a string attribute to the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     */
    public void setStringAttribute(final String objectPath, final String name, final String value);

    /**
     * Sets a string attribute to the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     * @param maxLength The maximal length of the value.
     */
    public void setStringAttribute(final String objectPath, final String name, final String value,
            final int maxLength);

    public void setStringAttributeVariableLength(final String objectPath, final String name,
            final String value);

    /**
     * Sets a <code>boolean</code> attribute to the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     */
    public void setBooleanAttribute(final String objectPath, final String name, final boolean value);

    /**
     * Sets an enum attribute to the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     */
    public void setEnumAttribute(final String objectPath, final String name,
            final HDF5EnumerationValue value);

    // /////////////////////
    // Data Sets
    // /////////////////////

    //
    // Generic
    //

    /**
     * Sets the data set size of a one-dimensional data set to <var>newSize</var>. Note that this
     * method can only be applied to extendable data sets.
     * 
     * @throw HDF5JavaException If the data set <var>objectPath</var> is not extendable.
     */
    public void setDataSetSize(final String objectPath, final long newSize);

    /**
     * Sets the data set size of a multi-dimensional data set to <var>newDimensions</var>. Note that
     * this method can only be applied to extendable data sets.
     * 
     * @throw HDF5JavaException If the data set <var>objectPath</var> is not extendable.
     */
    public void setDataSetDimensions(final String objectPath, final long[] newDimensions);

    //
    // Boolean
    //

    /**
     * Writes out a <code>boolean</code> value.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param value The value of the data set.
     */
    public void writeBoolean(final String objectPath, final boolean value);

    /**
     * Writes out a bit field ((which can be considered the equivalent to a boolean array of rank
     * 1), provided as a Java {@link BitSet}.
     * <p>
     * Note that the storage form of the bit array is a <code>long[]</code>. However, it is marked
     * in HDF5 to be interpreted bit-wise. Thus a data set written by this method cannot be read
     * back by {@link #readLongArray(String)} but will throw a
     * {@link ncsa.hdf.hdf5lib.exceptions.HDF5DatatypeInterfaceException}.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>.
     */
    public void writeBitField(final String objectPath, final BitSet data);

    /**
     * Writes out a bit field ((which can be considered the equivalent to a boolean array of rank
     * 1), provided as a Java {@link BitSet}.
     * <p>
     * Note that the storage form of the bit array is a <code>long[]</code>. However, it is marked
     * in HDF5 to be interpreted bit-wise. Thus a data set written by this method cannot be read
     * back by {@link #readLongArray(String)} but will throw a
     * {@link ncsa.hdf.hdf5lib.exceptions.HDF5DatatypeInterfaceException}.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>.
     * @param features The storage features of the data set.
     */
    public void writeBitField(final String objectPath, final BitSet data,
            final HDF5GenericStorageFeatures features);

    //
    // Opaque
    //

    /**
     * Writes out an opaque data type described by <var>tag</var> and defined by a <code>byte</code>
     * array (of rank 1).
     * <p>
     * Note that there is no dedicated method for reading opaque types. Use the method
     * {@link #readAsByteArray(String)} instead.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param tag The tag of the data set.
     * @param data The data to write. Must not be <code>null</code>.
     */
    public void writeOpaqueByteArray(final String objectPath, final String tag, final byte[] data);

    /**
     * Writes out an opaque data type described by <var>tag</var> and defined by a <code>byte</code>
     * array (of rank 1).
     * <p>
     * Note that there is no dedicated method for reading opaque types. Use the method
     * {@link #readAsByteArray(String)} instead.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param tag The tag of the data set.
     * @param data The data to write. Must not be <code>null</code>.
     * @param features The storage features of the data set.
     */
    public void writeOpaqueByteArray(final String objectPath, final String tag, final byte[] data,
            final HDF5GenericStorageFeatures features);

    /**
     * Creates an opaque data set that will be represented as a <code>byte</code> array (of rank 1).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The size of the byte array to create.
     * @param blockSize The size of on block (for block-wise IO)
     * @return The {@link HDF5OpaqueType} that can be used in methods
     *         {@link #writeOpaqueByteArrayBlock(String, HDF5OpaqueType, byte[], long)} and
     *         {@link #writeOpaqueByteArrayBlockWithOffset(String, HDF5OpaqueType, byte[], int, long)}
     *         to represent this opaque type.
     */
    public HDF5OpaqueType createOpaqueByteArray(final String objectPath, final String tag,
            final long size, final int blockSize);

    /**
     * Creates an opaque data set that will be represented as a <code>byte</code> array (of rank 1).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The size of the byte array to create. This will be the total size for
     *            non-extendable data sets and the size of one chunk for extendable (chunked) data
     *            sets. For extendable data sets the initial size of the array will be 0, see
     *            {@link ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator#dontUseExtendableDataTypes}.
     * @return The {@link HDF5OpaqueType} that can be used in methods
     *         {@link #writeOpaqueByteArrayBlock(String, HDF5OpaqueType, byte[], long)} and
     *         {@link #writeOpaqueByteArrayBlockWithOffset(String, HDF5OpaqueType, byte[], int, long)}
     *         to represent this opaque type.
     */
    public HDF5OpaqueType createOpaqueByteArray(final String objectPath, final String tag,
            final int size);

    /**
     * Creates an opaque data set that will be represented as a <code>byte</code> array (of rank 1).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The size of the byte array to create.
     * @param blockSize The size of on block (for block-wise IO)
     * @param features The storage features of the data set.
     * @return The {@link HDF5OpaqueType} that can be used in methods
     *         {@link #writeOpaqueByteArrayBlock(String, HDF5OpaqueType, byte[], long)} and
     *         {@link #writeOpaqueByteArrayBlockWithOffset(String, HDF5OpaqueType, byte[], int, long)}
     *         to represent this opaque type.
     */
    public HDF5OpaqueType createOpaqueByteArray(final String objectPath, final String tag,
            final long size, final int blockSize, final HDF5GenericStorageFeatures features);

    /**
     * Creates an opaque data set that will be represented as a <code>byte</code> array (of rank 1).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The size of the byte array to create. This will be the total size for
     *            non-extendable data sets and the size of one chunk for extendable (chunked) data
     *            sets. For extendable data sets the initial size of the array will be 0, see
     *            {@link HDF5GenericStorageFeatures}.
     * @param features The storage features of the data set.
     * @return The {@link HDF5OpaqueType} that can be used in methods
     *         {@link #writeOpaqueByteArrayBlock(String, HDF5OpaqueType, byte[], long)} and
     *         {@link #writeOpaqueByteArrayBlockWithOffset(String, HDF5OpaqueType, byte[], int, long)}
     *         to represent this opaque type.
     */
    public HDF5OpaqueType createOpaqueByteArray(final String objectPath, final String tag,
            final int size, final HDF5GenericStorageFeatures features);

    /**
     * Writes out a block of an opaque data type represented by a <code>byte</code> array (of rank
     * 1). The data set needs to have been created by
     * {@link #createOpaqueByteArray(String, String, long, int, HDF5GenericStorageFeatures)}
     * beforehand.
     * <p>
     * <i>Note:</i> For best performance, the block size in this method should be chosen to be equal
     * to the <var>blockSize</var> argument of the
     * {@link #createOpaqueByteArray(String, String, long, int, HDF5GenericStorageFeatures)} call
     * that was used to created the data set.
     * <p>
     * Note that there is no dedicated method for reading opaque types. Use the method
     * {@link #readAsByteArrayBlock(String, int, long)} instead.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. The length defines the block size. Must not be
     *            <code>null</code> or of length 0.
     * @param blockNumber The number of the block to write.
     */
    public void writeOpaqueByteArrayBlock(final String objectPath, final HDF5OpaqueType dataType,
            final byte[] data, final long blockNumber);

    /**
     * Writes out a block of an opaque data type represented by a <code>byte</code> array (of rank
     * 1). The data set needs to have been created by
     * {@link #createOpaqueByteArray(String, String, long, int, HDF5GenericStorageFeatures)}
     * beforehand.
     * <p>
     * Use this method instead of
     * {@link #writeOpaqueByteArrayBlock(String, HDF5OpaqueType, byte[], long)} if the total size of
     * the data set is not a multiple of the block size.
     * <p>
     * <i>Note:</i> For best performance, the typical <var>dataSize</var> in this method should be
     * chosen to be equal to the <var>blockSize</var> argument of the
     * {@link #createOpaqueByteArray(String, String, long, int, HDF5GenericStorageFeatures)} call
     * that was used to created the data set.
     * <p>
     * Note that there is no dedicated method for reading opaque types. Use the method
     * {@link #readAsByteArrayBlockWithOffset(String, int, long)} instead.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. The length defines the block size. Must not be
     *            <code>null</code> or of length 0.
     * @param dataSize The (real) size of <code>data</code> (needs to be <code><= data.length</code>
     *            )
     * @param offset The offset in the data set to start writing to.
     */
    public void writeOpaqueByteArrayBlockWithOffset(final String objectPath,
            final HDF5OpaqueType dataType, final byte[] data, final int dataSize, final long offset);

    //
    // Date
    //

    /**
     * Writes out a time stamp value. The data set will be tagged as type variant
     * {@link HDF5DataTypeVariant#TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH}.
     * <p>
     * <em>Note: Time stamps are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param timeStamp The timestamp to write as number of milliseconds since January 1, 1970,
     *            00:00:00 GMT.
     */
    public void writeTimeStamp(final String objectPath, final long timeStamp);

    /**
     * Creates a time stamp array (of rank 1).
     * <p>
     * <em>Note: Time stamps are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The length of the data set to create.
     * @param blockSize The size of one block (for block-wise IO). Ignored if no extendable data
     *            sets are used (see {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()})
     *            and <code>deflate == false</code>.
     */
    public void createTimeStampArray(final String objectPath, final long size, final int blockSize);

    /**
     * Creates a time stamp array (of rank 1).
     * <p>
     * <em>Note: Time stamps are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The size of the byte array to create. This will be the total size for
     *            non-extendable data sets and the size of one chunk for extendable (chunked) data
     *            sets. For extendable data sets the initial size of the array will be 0, see
     *            {@link ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator#dontUseExtendableDataTypes}.
     */
    public void createTimeStampArray(final String objectPath, final int size);

    /**
     * Creates a time stamp array (of rank 1).
     * <p>
     * <em>Note: Time stamps are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The length of the data set to create.
     * @param blockSize The size of one block (for block-wise IO). Ignored if no extendable data
     *            sets are used (see {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()})
     *            and <code>deflate == false</code>.
     * @param features The storage features of the data set.
     */
    public void createTimeStampArray(final String objectPath, final long size, final int blockSize,
            final HDF5GenericStorageFeatures features);

    /**
     * Creates a time stamp array (of rank 1).
     * <p>
     * <em>Note: Time stamps are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The size of the array to create. This will be the total size for non-extendable
     *            data sets and the size of one chunk for extendable (chunked) data sets. For
     *            extendable data sets the initial size of the array will be 0, see
     *            {@link HDF5GenericStorageFeatures}.
     * @param features The storage features of the data set.
     */
    public void createTimeStampArray(final String objectPath, final int size,
            final HDF5GenericStorageFeatures features);

    /**
     * Writes out a time stamp array (of rank 1). The data set will be tagged as type variant
     * {@link HDF5DataTypeVariant#TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH}.
     * <p>
     * <em>Note: Time stamps are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param timeStamps The timestamps to write as number of milliseconds since January 1, 1970,
     *            00:00:00 GMT.
     */
    public void writeTimeStampArray(final String objectPath, final long[] timeStamps);

    /**
     * Writes out a time stamp array (of rank 1). The data set will be tagged as type variant
     * {@link HDF5DataTypeVariant#TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH}.
     * <p>
     * <em>Note: Time stamps are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param timeStamps The timestamps to write as number of milliseconds since January 1, 1970,
     *            00:00:00 GMT.
     * @param features The storage features of the data set.
     */
    public void writeTimeStampArray(final String objectPath, final long[] timeStamps,
            final HDF5GenericStorageFeatures features);

    /**
     * Writes out a block of a time stamp array (which is stored as a <code>long</code> array of
     * rank 1). The data set needs to have been created by
     * {@link #createTimeStampArray(String, long, int, HDF5GenericStorageFeatures)} beforehand.
     * <p>
     * <i>Note:</i> For best performance, the block size in this method should be chosen to be equal
     * to the <var>blockSize</var> argument of the
     * {@link #createLongArray(String, long, int, HDF5IntStorageFeatures)} call that was used to
     * create the data set.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. The length defines the block size. Must not be
     *            <code>null</code> or of length 0.
     * @param blockNumber The number of the block to write.
     */
    public void writeTimeStampArrayBlock(final String objectPath, final long[] data,
            final long blockNumber);

    /**
     * Writes out a block of a time stamp array (which is stored as a <code>long</code> array of
     * rank 1). The data set needs to have been created by
     * {@link #createTimeStampArray(String, long, int, HDF5GenericStorageFeatures)} beforehand.
     * <p>
     * Use this method instead of {@link #writeTimeStampArrayBlock(String, long[], long)} if the
     * total size of the data set is not a multiple of the block size.
     * <p>
     * <i>Note:</i> For best performance, the typical <var>dataSize</var> in this method should be
     * chosen to be equal to the <var>blockSize</var> argument of the
     * {@link #createLongArray(String, long, int, HDF5IntStorageFeatures)} call that was used to
     * create the data set.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. The length defines the block size. Must not be
     *            <code>null</code> or of length 0.
     * @param dataSize The (real) size of <code>data</code> (needs to be <code><= data.length</code>
     *            )
     * @param offset The offset in the data set to start writing to.
     */
    public void writeTimeStampArrayBlockWithOffset(final String objectPath, final long[] data,
            final int dataSize, final long offset);

    /**
     * Writes out a time stamp value provided as a {@link Date}. The data set will be tagged as type
     * variant {@link HDF5DataTypeVariant#TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH}.
     * <p>
     * <em>Note: Time stamps are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param date The date to write.
     * @see #writeTimeStamp(String, long)
     */
    public void writeDate(final String objectPath, final Date date);

    /**
     * Writes out a {@link Date} array (of rank 1).
     * <p>
     * <em>Note: Time stamps are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param dates The dates to write.
     * @see #writeTimeStampArray(String, long[])
     */
    public void writeDateArray(final String objectPath, final Date[] dates);

    /**
     * Writes out a {@link Date} array (of rank 1).
     * <p>
     * <em>Note: Time stamps are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param dates The dates to write.
     * @param features The storage features of the data set.
     * @see #writeTimeStampArray(String, long[], HDF5GenericStorageFeatures)
     */
    public void writeDateArray(final String objectPath, final Date[] dates,
            final HDF5GenericStorageFeatures features);

    //
    // Duration
    //

    /**
     * Writes out a time duration value in seconds. The data set will be tagged as type variant
     * {@link HDF5DataTypeVariant#TIME_DURATION_SECONDS}.
     * <p>
     * <em>Note: Time durations are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param timeDuration The duration of time to write in seconds.
     */
    public void writeTimeDuration(final String objectPath, final long timeDuration);

    /**
     * Writes out a time duration value. The data set will be tagged as the according type variant.
     * <p>
     * <em>Note: Time durations are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param timeDuration The duration of time to write in the given <var>timeUnit</var>.
     * @param timeUnit The unit of the time duration.
     */
    public void writeTimeDuration(final String objectPath, final long timeDuration,
            final HDF5TimeUnit timeUnit);

    /**
     * Creates a time duration array (of rank 1).
     * <p>
     * <em>Note: Time durations are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The size of the byte array to create. This will be the total size for
     *            non-extendable data sets and the size of one chunk for extendable (chunked) data
     *            sets. For extendable data sets the initial size of the array will be 0, see
     *            {@link ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator#dontUseExtendableDataTypes}.
     * @param timeUnit The unit of the time duration.
     */
    public void createTimeDurationArray(final String objectPath, final int size,
            final HDF5TimeUnit timeUnit);

    /**
     * Creates a time duration array (of rank 1).
     * <p>
     * <em>Note: Time durations are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The size of the data set to create.
     * @param blockSize The size of one block (for block-wise IO). Ignored if no extendable data
     *            sets are used (see {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()})
     *            and <code>deflate == false</code>.
     * @param timeUnit The unit of the time duration.
     */
    public void createTimeDurationArray(final String objectPath, final long size,
            final int blockSize, final HDF5TimeUnit timeUnit);

    /**
     * Creates a time duration array (of rank 1).
     * <p>
     * <em>Note: Time durations are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The size of the data set to create.
     * @param blockSize The size of one block (for block-wise IO). Ignored if no extendable data
     *            sets are used (see {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()})
     *            and <code>deflate == false</code>.
     * @param timeUnit The unit of the time duration.
     * @param features The storage features of the data set.
     */
    public void createTimeDurationArray(final String objectPath, final long size,
            final int blockSize, final HDF5TimeUnit timeUnit,
            final HDF5GenericStorageFeatures features);

    /**
     * Creates a time duration array (of rank 1).
     * <p>
     * <em>Note: Time durations are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The size of the array to create. This will be the total size for non-extendable
     *            data sets and the size of one chunk for extendable (chunked) data sets. For
     *            extendable data sets the initial size of the array will be 0, see
     *            {@link HDF5GenericStorageFeatures}.
     * @param timeUnit The unit of the time duration.
     * @param features The storage features of the data set.
     */
    public void createTimeDurationArray(final String objectPath, final int size,
            final HDF5TimeUnit timeUnit, final HDF5GenericStorageFeatures features);

    /**
     * Writes out a time duration array in seconds (of rank 1). The data set will be tagged as type
     * variant {@link HDF5DataTypeVariant#TIME_DURATION_SECONDS}.
     * <p>
     * <em>Note: Time durations are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param timeDurations The time durations to write in seconds.
     */
    public void writeTimeDurationArray(final String objectPath, final long[] timeDurations);

    /**
     * Writes out a time duration array (of rank 1). The data set will be tagged as the according
     * type variant.
     * <p>
     * <em>Note: Time durations are stored as <code>long[]</code> arrays.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param timeDurations The time durations to write in the given <var>timeUnit</var>.
     * @param timeUnit The unit of the time duration.
     */
    public void writeTimeDurationArray(final String objectPath, final long[] timeDurations,
            final HDF5TimeUnit timeUnit);

    /**
     * Writes out a time duration array (of rank 1). The data set will be tagged as the according
     * type variant.
     * <p>
     * <em>Note: Time durations are stored as <code>long</code> values.</em>
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param timeDurations The time durations to write in the given <var>timeUnit</var>.
     * @param timeUnit The unit of the time duration.
     * @param features The storage features of the data set.
     */
    public void writeTimeDurationArray(final String objectPath, final long[] timeDurations,
            final HDF5TimeUnit timeUnit, final HDF5IntStorageFeatures features);

    /**
     * Writes out a block of a time duration array (which is stored as a <code>long</code> array of
     * rank 1). The data set needs to have been created by
     * {@link #createTimeDurationArray(String, long, int, HDF5TimeUnit, HDF5GenericStorageFeatures)}
     * beforehand.
     * <p>
     * <i>Note:</i> For best performance, the block size in this method should be chosen to be equal
     * to the <var>blockSize</var> argument of the
     * {@link #createTimeDurationArray(String, long, int, HDF5TimeUnit, HDF5GenericStorageFeatures)}
     * call that was used to create the data set.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. The length defines the block size. Must not be
     *            <code>null</code> or of length 0.
     * @param blockNumber The number of the block to write.
     */
    public void writeTimeDurationArrayBlock(final String objectPath, final long[] data,
            final long blockNumber, final HDF5TimeUnit timeUnit);

    /**
     * Writes out a block of a time duration array (which is stored as a <code>long</code> array of
     * rank 1). The data set needs to have been created by
     * {@link #createTimeDurationArray(String, long, int, HDF5TimeUnit, HDF5GenericStorageFeatures)}
     * beforehand.
     * <p>
     * Use this method instead of
     * {@link #writeTimeDurationArrayBlock(String, long[], long, HDF5TimeUnit)} if the total size of
     * the data set is not a multiple of the block size.
     * <p>
     * <i>Note:</i> For best performance, the typical <var>dataSize</var> in this method should be
     * chosen to be equal to the <var>blockSize</var> argument of the
     * {@link #createTimeDurationArray(String, long, int, HDF5TimeUnit, HDF5GenericStorageFeatures)}
     * call that was used to create the data set.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. The length defines the block size. Must not be
     *            <code>null</code> or of length 0.
     * @param dataSize The (real) size of <code>data</code> (needs to be <code><= data.length</code>
     *            )
     * @param offset The offset in the data set to start writing to.
     */
    public void writeTimeDurationArrayBlockWithOffset(final String objectPath, final long[] data,
            final int dataSize, final long offset, final HDF5TimeUnit timeUnit);

    //
    // String
    //

    /**
     * Writes out a <code>String</code> with a fixed maximal length.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>.
     * @param maxLength The maximal length of the <var>data</var>.
     */
    public void writeString(final String objectPath, final String data, final int maxLength);

    /**
     * Writes out a <code>String</code> with a fixed maximal length (which is the length of the
     * string <var>data</var>).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>.
     */
    public void writeString(final String objectPath, final String data);

    /**
     * Writes out a <code>String</code> with a fixed maximal length.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>.
     * @param features The storage features of the data set.
     */
    public void writeString(final String objectPath, final String data,
            final HDF5GenericStorageFeatures features);

    /**
     * Writes out a <code>String</code> with a fixed maximal length.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>.
     * @param maxLength The maximal length of the <var>data</var>.
     * @param features The storage features of the data set.
     */
    public void writeString(final String objectPath, final String data, final int maxLength,
            final HDF5GenericStorageFeatures features);

    /**
     * Writes out a <code>String</code> array (of rank 1).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>.
     * @param features The storage features of the data set.
     */
    public void writeStringArray(final String objectPath, final String[] data,
            final HDF5GenericStorageFeatures features);

    /**
     * Writes out a <code>String</code> array (of rank 1). Each element of the array will have a
     * fixed maximal length which is defined by the longest string in <var>data</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>.
     */
    public void writeStringArray(final String objectPath, final String[] data);

    /**
     * Writes out a <code>String</code> array (of rank 1). Each element of the array will have a
     * fixed maximal length which is given by <var>maxLength</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>.
     * @param maxLength The maximal length of any of the strings in <var>data</var>.
     */
    public void writeStringArray(final String objectPath, final String[] data, final int maxLength);

    /**
     * Writes out a <code>String</code> array (of rank 1). Each element of the array will have a
     * fixed maximal length which is given by <var>maxLength</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>.
     * @param maxLength The maximal length of any of the strings in <var>data</var>.
     * @param features The storage features of the data set.
     */
    public void writeStringArray(final String objectPath, final String[] data, final int maxLength,
            final HDF5GenericStorageFeatures features);

    /**
     * Creates a <code>String</code> array (of rank 1) for Strings of length <var>maxLength</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param maxLength The maximal length of one String in the array.
     * @param size The size of the byte array to create. This will be the total size for
     *            non-extendable data sets and the size of one chunk for extendable (chunked) data
     *            sets. For extendable data sets the initial size of the array will be 0, see
     *            {@link ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator#dontUseExtendableDataTypes}.
     */
    public void createStringArray(final String objectPath, final int maxLength, final int size);

    /**
     * Creates a <code>String</code> array (of rank 1) for Strings of length <var>maxLength</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param maxLength The maximal length of one String in the array.
     * @param size The size of the String array to create. When using extendable data sets ((see
     *            {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()})), then no data set
     *            smaller than this size can be created, however data sets may be larger.
     * @param blockSize The size of one block (for block-wise IO). Ignored if no extendable data
     *            sets are used (see {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()}).
     */
    public void createStringArray(final String objectPath, final int maxLength, final long size,
            final int blockSize);

    /**
     * Creates a <code>String</code> array (of rank 1) for Strings of length <var>maxLength</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param maxLength The maximal length of one String in the array.
     * @param size The size of the array to create. This will be the total size for non-extendable
     *            data sets and the size of one chunk for extendable (chunked) data sets. For
     *            extendable data sets the initial size of the array will be 0, see
     *            {@link HDF5GenericStorageFeatures}.
     */
    public void createStringArray(final String objectPath, final int maxLength, final int size,
            final HDF5GenericStorageFeatures features);

    /**
     * Creates a <code>String</code> array (of rank 1) for Strings of length <var>maxLength</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param maxLength The maximal length of one String in the array.
     * @param size The size of the String array to create. When using extendable data sets ((see
     *            {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()})), then no data set
     *            smaller than this size can be created, however data sets may be larger.
     * @param blockSize The size of one block (for block-wise IO). Ignored if no extendable data
     *            sets are used (see {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()}).
     * @param features The storage features of the data set.
     */
    public void createStringArray(final String objectPath, final int maxLength, final long size,
            final int blockSize, final HDF5GenericStorageFeatures features);

    /**
     * Writes out a block of a <code>String</code> array (of rank 1). The data set needs to have
     * been created by
     * {@link #createStringArray(String, int, long, int, HDF5GenericStorageFeatures)} beforehand.
     * <p>
     * <i>Note:</i> For best performance, the block size in this method should be chosen to be equal
     * to the <var>blockSize</var> argument of the
     * {@link #createStringArray(String, int, long, int, HDF5GenericStorageFeatures)} call that was
     * used to create the data set.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. The length defines the block size. Must not be
     *            <code>null</code> or of length 0.
     * @param blockNumber The number of the block to write.
     */
    public void writeStringArrayBlock(final String objectPath, final String[] data,
            final long blockNumber);

    /**
     * Writes out a block of a <code>String</code> array (of rank 1). The data set needs to have
     * been created by
     * {@link #createStringArray(String, int, long, int, HDF5GenericStorageFeatures)} beforehand.
     * <p>
     * Use this method instead of {@link #writeStringArrayBlock(String, String[], long)} if the
     * total size of the data set is not a multiple of the block size.
     * <p>
     * <i>Note:</i> For best performance, the block size in this method should be chosen to be equal
     * to the <var>blockSize</var> argument of the
     * {@link #createStringArray(String, int, long, int, HDF5GenericStorageFeatures)} call that was
     * used to create the data set.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. The length defines the block size. Must not be
     *            <code>null</code> or of length 0.
     * @param dataSize The (real) size of <code>data</code> (needs to be <code><= data.length</code>
     *            )
     * @param offset The offset in the data set to start writing to.
     */
    public void writeStringArrayBlockWithOffset(final String objectPath, final String[] data,
            final int dataSize, final long offset);

    /**
     * Writes out a <code>String</code> with variable maximal length.
     * <p>
     * The advantage of this method over {@link #writeString(String, String)} is that when writing a
     * new string later it can have a different (also greater) length. The disadvantage is that it
     * it is more time consuming to read and write this kind of string and that it can't be
     * compressed.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>.
     */
    public void writeStringVariableLength(final String objectPath, final String data);

    /**
     * Writes out a <code>String[]</code> where each String of the array has a variable maximal
     * length.
     * <p>
     * The advantage of this method over {@link #writeStringArray(String, String[])} is that when
     * writing a new string later it can have a different (also greater) length. The disadvantage is
     * that it it is more time consuming to read and write this kind of string and that it can't be
     * compressed.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>.
     */
    public void writeStringVariableLengthArray(final String objectPath, final String[] data);

    /**
     * Creates a <code>String[]</code> where each String of the array has a variable maximal length.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The size of the byte array to create. This will be the total size for
     *            non-extendable data sets and the size of one chunk for extendable (chunked) data
     *            sets. For extendable data sets the initial size of the array will be 0, see
     *            {@link ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator#dontUseExtendableDataTypes}.
     */
    public void createStringVariableLengthArray(final String objectPath, final int size);

    /**
     * Creates a <code>String[]</code> where each String of the array has a variable maximal length.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The intial size of the array.
     * @param blockSize The size of block in the array.
     */
    public void createStringVariableLengthArray(final String objectPath, final long size,
            final int blockSize);

    /**
     * Creates a <code>String[]</code> where each String of the array has a variable maximal length.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The intial size of the array.
     * @param blockSize The size of block in the array.
     * @param features The storage features of the data set.
     */
    public void createStringVariableLengthArray(final String objectPath, final long size,
            final int blockSize, final HDF5GenericStorageFeatures features);

    /**
     * Creates a <code>String[]</code> where each String of the array has a variable maximal length.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The size of the byte array to create. This will be the total size for
     *            non-extendable data sets and the size of one chunk for extendable (chunked) data
     *            sets. For extendable data sets the initial size of the array will be 0, see
     *            {@link HDF5GenericStorageFeatures}.
     * @param features The storage features of the data set.
     */
    public void createStringVariableLengthArray(final String objectPath, final int size,
            final HDF5GenericStorageFeatures features);

    //
    // Enum
    //

    /**
     * Writes out an enum value.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param value The value of the data set.
     * @throws HDF5JavaException If the enum type of <var>value</var> is not a type of this file.
     */
    public void writeEnum(final String objectPath, final HDF5EnumerationValue value)
            throws HDF5JavaException;

    /**
     * Writes out an array of enum values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write.
     * @throws HDF5JavaException If the enum type of <var>value</var> is not a type of this file.
     */
    public void writeEnumArray(final String objectPath, final HDF5EnumerationValueArray data)
            throws HDF5JavaException;

    /**
     * Writes out an array of enum values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write.
     * @param features The storage features of the data set. Note that for scaling compression the
     *            compression factor is ignored. Instead, the scaling factor is computed from the
     *            number of entries in the enumeration.
     * @throws HDF5JavaException If the enum type of <var>value</var> is not a type of this file.
     */
    public void writeEnumArray(final String objectPath, final HDF5EnumerationValueArray data,
            final HDF5IntStorageFeatures features) throws HDF5JavaException;

    /**
     * Creates am enum array (of rank 1).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param enumType The enumeration type of this array.
     * @param size The size of the byte array to create. This will be the total size for
     *            non-extendable data sets and the size of one chunk for extendable (chunked) data
     *            sets. For extendable data sets the initial size of the array will be 0, see
     *            {@link ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator#dontUseExtendableDataTypes}.
     */
    public void createEnumArray(final String objectPath, final HDF5EnumerationType enumType,
            final int size);

    /**
     * Creates am enum array (of rank 1). The initial size of the array is 0.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param enumType The enumeration type of this array.
     * @param size The size of the enum array to create. When using extendable data sets ((see
     *            {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()})), then no data set
     *            smaller than this size can be created, however data sets may be larger.
     * @param blockSize The size of one block (for block-wise IO). Ignored if no extendable data
     *            sets are used (see {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()}).
     */
    public void createEnumArray(final String objectPath, final HDF5EnumerationType enumType,
            final long size, final int blockSize);

    /**
     * Creates am enum array (of rank 1). The initial size of the array is 0.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param enumType The enumeration type of this array.
     * @param size The size of the enum array to create. When using extendable data sets ((see
     *            {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()})), then no data set
     *            smaller than this size can be created, however data sets may be larger.
     * @param blockSize The size of one block (for block-wise IO). Ignored if no extendable data
     *            sets are used (see {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()}).
     * @param features The storage features of the data set.
     */
    public void createEnumArray(final String objectPath, final HDF5EnumerationType enumType,
            final long size, final int blockSize, final HDF5IntStorageFeatures features);

    /**
     * Creates am enum array (of rank 1). The initial size of the array is 0.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param enumType The enumeration type of this array.
     * @param size The size of the enum array to create. This will be the total size for
     *            non-extendable data sets and the size of one chunk for extendable (chunked) data
     *            sets. For extendable data sets the initial size of the array will be 0, see
     *            {@link HDF5IntStorageFeatures}.
     * @param features The storage features of the data set.
     */
    public void createEnumArray(final String objectPath, final HDF5EnumerationType enumType,
            final long size, final HDF5IntStorageFeatures features);

    /**
     * Writes out a block of an enum array (of rank 1). The data set needs to have been created by
     * {@link #createEnumArray(String, HDF5EnumerationType, long, int, HDF5IntStorageFeatures)}
     * beforehand. Obviously the {@link HDF5EnumerationType} of the create call and this call needs
     * to match.
     * <p>
     * <i>Note:</i> For best performance, the block size in this method should be chosen to be equal
     * to the <var>blockSize</var> argument of the
     * {@link #createEnumArray(String, HDF5EnumerationType, long, int, HDF5IntStorageFeatures)} call
     * that was used to create the data set.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. The value of {@link HDF5EnumerationValueArray#getLength()}
     *            defines the block size. Must not be <code>null</code> or of length 0.
     * @param blockNumber The number of the block to write.
     */
    public void writeEnumArrayBlock(final String objectPath, final HDF5EnumerationValueArray data,
            final long blockNumber);

    /**
     * Writes out a block of an enum array (of rank 1). The data set needs to have been created by
     * {@link #createEnumArray(String, HDF5EnumerationType, long, int, HDF5IntStorageFeatures)}
     * beforehand. Obviously the {@link HDF5EnumerationType} of the create call and this call needs
     * to match.
     * <p>
     * <i>Note:</i> For best performance, the block size in this method should be chosen to be equal
     * to the <var>blockSize</var> argument of the
     * {@link #createEnumArray(String, HDF5EnumerationType, long, int, HDF5IntStorageFeatures)} call
     * that was used to create the data set.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. The value of {@link HDF5EnumerationValueArray#getLength()}
     *            defines the block size. Must not be <code>null</code> or of length 0.
     * @param dataSize The (real) size of <code>data</code> (needs to be
     *            <code><= data.getLength()</code> )
     * @param offset The offset in the data set to start writing to.
     */
    public void writeEnumArrayBlockWithOffset(final String objectPath,
            final HDF5EnumerationValueArray data, final int dataSize, final long offset);

    //
    // Compound
    //

    /**
     * Writes out an array (of rank 1) of compound values. Uses a compact storage layout. Must only
     * be used for small data sets.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     */
    public <T> void writeCompound(final String objectPath, final HDF5CompoundType<T> type,
            final T data);

    /**
     * Writes out an array (of rank 1) of compound values. Uses a compact storage layout. Must only
     * be used for small data sets.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     * @param inspectorOrNull The inspector to be called after translating the Java objects to a
     *            byte array and before writing the byte array to the HDF5.
     */
    public <T> void writeCompound(final String objectPath, final HDF5CompoundType<T> type,
            final T data, final IByteArrayInspector inspectorOrNull);

    /**
     * Writes out an array (of rank 1) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     */
    public <T> void writeCompoundArray(final String objectPath, final HDF5CompoundType<T> type,
            final T[] data);

    /**
     * Writes out an array (of rank 1) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     * @param features The storage features of the data set.
     */
    public <T> void writeCompoundArray(final String objectPath, final HDF5CompoundType<T> type,
            final T[] data, final HDF5GenericStorageFeatures features);

    /**
     * Writes out an array (of rank 1) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     * @param features The storage features of the data set.
     * @param inspectorOrNull The inspector to be called after translating the Java objects to a
     *            byte array and before writing the byte array to the HDF5.
     */
    public <T> void writeCompoundArray(final String objectPath, final HDF5CompoundType<T> type,
            final T[] data, final HDF5GenericStorageFeatures features,
            final IByteArrayInspector inspectorOrNull);

    /**
     * Writes out a block <var>blockNumber</var> of an array (of rank 1) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     * @param blockNumber The number of the block to write.
     */
    public <T> void writeCompoundArrayBlock(final String objectPath,
            final HDF5CompoundType<T> type, final T[] data, final long blockNumber);

    /**
     * Writes out a block <var>blockNumber</var> of an array (of rank 1) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     * @param blockNumber The number of the block to write.
     * @param inspectorOrNull The inspector to be called after translating the Java objects to a
     *            byte array and before writing the byte array to the HDF5.
     */
    public <T> void writeCompoundArrayBlock(final String objectPath,
            final HDF5CompoundType<T> type, final T[] data, final long blockNumber,
            final IByteArrayInspector inspectorOrNull);

    /**
     * Writes out a block of an array (of rank 1) of compound values with given <var>offset</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     * @param offset The offset of the block in the data set.
     */
    public <T> void writeCompoundArrayBlockWithOffset(final String objectPath,
            final HDF5CompoundType<T> type, final T[] data, final long offset);

    /**
     * Writes out a block of an array (of rank 1) of compound values with given <var>offset</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     * @param offset The offset of the block in the data set.
     * @param inspectorOrNull The inspector to be called after translating the Java objects to a
     *            byte array and before writing the byte array to the HDF5.
     */
    public <T> void writeCompoundArrayBlockWithOffset(final String objectPath,
            final HDF5CompoundType<T> type, final T[] data, final long offset,
            final IByteArrayInspector inspectorOrNull);

    /**
     * Creates an array (of rank 1) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param size The size of the array to create. This will be the total size for non-extendable
     *            data sets and the size of one chunk for extendable (chunked) data sets. For
     *            extendable data sets the initial size of the array will be 0, see
     *            {@link ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator#dontUseExtendableDataTypes}.
     */
    public <T> void createCompoundArray(final String objectPath, final HDF5CompoundType<T> type,
            final int size);

    /**
     * Creates an array (of rank 1) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param size The size of the compound array to create.
     * @param blockSize The size of one block (for block-wise IO). Ignored if no extendable data
     *            sets are used (see {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()})
     *            and <code>deflate == false</code>.
     */
    public <T> void createCompoundArray(final String objectPath, final HDF5CompoundType<T> type,
            final long size, final int blockSize);

    /**
     * Creates an array (of rank 1) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param size The size of the compound array to create.
     * @param blockSize The size of one block (for block-wise IO). Ignored if no extendable data
     *            sets are used (see {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()})
     *            and <code>deflate == false</code>.
     * @param features The storage features of the data set.
     */
    public <T> void createCompoundArray(final String objectPath, final HDF5CompoundType<T> type,
            final long size, final int blockSize, final HDF5GenericStorageFeatures features);

    /**
     * Creates an array (of rank 1) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param size The size of the byte array to create. This will be the total size for
     *            non-extendable data sets and the size of one chunk for extendable (chunked) data
     *            sets. For extendable data sets the initial size of the array will be 0, see
     *            {@link HDF5GenericStorageFeatures}.
     * @param features The storage features of the data set.
     */
    public <T> void createCompoundArray(final String objectPath, final HDF5CompoundType<T> type,
            final long size, final HDF5GenericStorageFeatures features);

    /**
     * Writes out an array (of rank N) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The data to write.
     */
    public <T> void writeCompoundMDArray(final String objectPath, final HDF5CompoundType<T> type,
            final MDArray<T> data);

    /**
     * Writes out an array (of rank N) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The data to write.
     * @param features The storage features of the data set.
     */
    public <T> void writeCompoundMDArray(final String objectPath, final HDF5CompoundType<T> type,
            final MDArray<T> data, final HDF5GenericStorageFeatures features);

    /**
     * Writes out an array (of rank N) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The data to write.
     * @param features The storage features of the data set.
     * @param inspectorOrNull The inspector to be called after translating the Java objects to a
     *            byte array and before writing the byte array to the HDF5.
     */
    public <T> void writeCompoundMDArray(final String objectPath, final HDF5CompoundType<T> type,
            final MDArray<T> data, final HDF5GenericStorageFeatures features,
            final IByteArrayInspector inspectorOrNull);

    /**
     * Writes out a block of an array (of rank N) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The data to write.
     * @param blockDimensions The extent of the block to write on each axis.
     */
    public <T> void writeCompoundMDArrayBlock(final String objectPath,
            final HDF5CompoundType<T> type, final MDArray<T> data, final long[] blockDimensions);

    /**
     * Writes out a block of an array (of rank N) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The data to write.
     * @param blockDimensions The extent of the block to write on each axis.
     * @param inspectorOrNull The inspector to be called after translating the Java objects to a
     *            byte array and before writing the byte array to the HDF5.
     */
    public <T> void writeCompoundMDArrayBlock(final String objectPath,
            final HDF5CompoundType<T> type, final MDArray<T> data, final long[] blockDimensions,
            final IByteArrayInspector inspectorOrNull);

    /**
     * Writes out a block of an array (of rank N) of compound values give a given <var>offset</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The data to write.
     * @param offset The offset of the block to write on each axis.
     */
    public <T> void writeCompoundMDArrayBlockWithOffset(final String objectPath,
            final HDF5CompoundType<T> type, final MDArray<T> data, final long[] offset);

    /**
     * Writes out a block of an array (of rank N) of compound values give a given <var>offset</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The data to write.
     * @param offset The offset of the block to write on each axis.
     * @param inspectorOrNull The inspector to be called after translating the Java objects to a
     *            byte array and before writing the byte array to the HDF5.
     */
    public <T> void writeCompoundMDArrayBlockWithOffset(final String objectPath,
            final HDF5CompoundType<T> type, final MDArray<T> data, final long[] offset,
            final IByteArrayInspector inspectorOrNull);

    /**
     * Writes out a block of an array (of rank N) of compound values give a given <var>offset</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The data to write.
     * @param blockDimensions The dimensions of the block to write to the data set.
     * @param offset The offset of the block in the data set to start writing to in each dimension.
     * @param memoryOffset The offset of the block in the <var>data</var> array.
     */
    public <T> void writeCompoundMDArrayBlockWithOffset(final String objectPath,
            final HDF5CompoundType<T> type, final MDArray<T> data, final int[] blockDimensions,
            final long[] offset, final int[] memoryOffset);

    /**
     * Writes out a block of an array (of rank N) of compound values give a given <var>offset</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The data to write.
     * @param blockDimensions The dimensions of the block to write to the data set.
     * @param offset The offset of the block in the data set to start writing to in each dimension.
     * @param memoryOffset The offset of the block in the <var>data</var> array.
     * @param inspectorOrNull The inspector to be called after translating the Java objects to a
     *            byte array and before writing the byte array to the HDF5.
     */
    public <T> void writeCompoundMDArrayBlockWithOffset(final String objectPath,
            final HDF5CompoundType<T> type, final MDArray<T> data, final int[] blockDimensions,
            final long[] offset, final int[] memoryOffset, final IByteArrayInspector inspectorOrNull);

    /**
     * Creates an array (of rank N) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param dimensions The dimensions of the byte array to create. This will be the total
     *            dimensions for non-extendable data sets and the dimensions of one chunk (along
     *            each axis) for extendable (chunked) data sets. For extendable data sets the
     *            initial size of the array (along each axis) will be 0, see
     *            {@link ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator#dontUseExtendableDataTypes}.
     */
    public <T> void createCompoundMDArray(final String objectPath, final HDF5CompoundType<T> type,
            final int[] dimensions);

    /**
     * Creates an array (of rank N) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param dimensions The extent of the compound array along each of the axis.
     * @param blockDimensions The extent of one block along each of the axis. (for block-wise IO).
     *            Ignored if no extendable data sets are used (see
     *            {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()}) and
     *            <code>deflate == false</code>.
     */
    public <T> void createCompoundMDArray(final String objectPath, final HDF5CompoundType<T> type,
            final long[] dimensions, final int[] blockDimensions);

    /**
     * Creates an array (of rank N) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param dimensions The extent of the compound array along each of the axis.
     * @param blockDimensions The extent of one block along each of the axis. (for block-wise IO).
     *            Ignored if no extendable data sets are used (see
     *            {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()}) and
     *            <code>deflate == false</code>.
     * @param features The storage features of the data set.
     */
    public <T> void createCompoundMDArray(final String objectPath, final HDF5CompoundType<T> type,
            final long[] dimensions, final int[] blockDimensions,
            final HDF5GenericStorageFeatures features);

    /**
     * Creates an array (of rank N) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param dimensions The dimensions of the byte array to create. This will be the total
     *            dimensions for non-extendable data sets and the dimensions of one chunk (along
     *            each axis) for extendable (chunked) data sets. For extendable data sets the
     *            initial size of the array (along each axis) will be 0, see
     *            {@link HDF5GenericStorageFeatures}.
     * @param features The storage features of the data set.
     */
    public <T> void createCompoundMDArray(final String objectPath, final HDF5CompoundType<T> type,
            final int[] dimensions, final HDF5GenericStorageFeatures features);
}
