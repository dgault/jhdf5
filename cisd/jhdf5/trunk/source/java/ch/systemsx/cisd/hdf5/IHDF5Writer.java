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

import java.io.Flushable;

import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;
import ncsa.hdf.hdf5lib.exceptions.HDF5SymbolTableException;

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
@SuppressWarnings("deprecation")
public interface IHDF5Writer extends IHDF5Reader, IHDF5SimpleWriter, IHDF5LegacyWriter
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

    /**
     * Adds a {@link Flushable} to the set of flushables. This set is flushed when {@link #flush()}
     * or {@link #flushSyncBlocking()} are called and before the writer is closed.
     * <p>
     * This function is supposed to be used for in-memory caching structures that need to make it
     * into the HDF5 file.
     * <p>
     * If the <var>flushable</var> implements
     * {@link ch.systemsx.cisd.base.exceptions.IErrorStrategy}, in case of an exception in
     * {@link Flushable#flush()}, the method
     * {@link ch.systemsx.cisd.base.exceptions.IErrorStrategy#dealWithError(Throwable)} will be
     * called to decide how do deal with the exception.
     * 
     * @param flushable The {@link Flushable} to add. Needs to fulfill the {@link Object#hashCode()}
     *            contract.
     * @return <code>true</code> if the set of flushables did not already contain the specified
     *         element.
     */
    public boolean addFlushable(Flushable flushable);

    /**
     * Removes a {@link Flushable} from the set of flushables.
     * 
     * @param flushable The {@link Flushable} to remove. Needs to fulfill the
     *            {@link Object#hashCode()} contract.
     * @return <code>true</code> if the set of flushables contained the specified element.
     */
    public boolean removeFlushable(Flushable flushable);

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
    @Override
    public void delete(String objectPath);

    /**
     * Moves or renames a link in the file atomically.
     * 
     * @throws HDF5SymbolTableException If <var>oldLinkPath</var> does not exist or if
     *             <var>newLinkPath</var> already exists.
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
    // Data Sets
    // /////////////////////

    /**
     * Sets the data set size of a one-dimensional data set to <var>newSize</var>. Note that this
     * method can only be applied to extendable data sets.
     * 
     * @throws HDF5JavaException If the data set <var>objectPath</var> is not extendable.
     */
    public void setDataSetSize(final String objectPath, final long newSize);

    /**
     * Sets the data set size of a multi-dimensional data set to <var>newDimensions</var>. Note that
     * this method can only be applied to extendable data sets.
     * 
     * @throws HDF5JavaException If the data set <var>objectPath</var> is not extendable.
     */
    public void setDataSetDimensions(final String objectPath, final long[] newDimensions);

    // /////////////////////
    // Types
    // /////////////////////

    /**
     * Sets a <var>typeVariant</var> of object <var>objectPath</var>.
     * 
     * @param objectPath The name of the object to add the type variant to.
     * @param typeVariant The type variant to add.
     */
    public void setTypeVariant(final String objectPath, final HDF5DataTypeVariant typeVariant);

    /**
     * Sets a <var>typeVariant</var> of attribute <var>attributeName</var> of object
     * <var>objectPath</var>.
     * 
     * @param objectPath The name of the object.
     * @param attributeName The name of attribute to add the type variant to.
     * @param typeVariant The type variant to add.
     */
    public void setTypeVariant(final String objectPath, final String attributeName,
            final HDF5DataTypeVariant typeVariant);

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
     * Deletes the <var>typeVariant</var> from <var>attributeName</var> of <var>objectPath</var>.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object.
     * @param attributeName The name of the attribute to delete the type variant from.
     */
    public void deleteTypeVariant(final String objectPath, final String attributeName);

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

    // /////////////////////
    // Boolean
    // /////////////////////

    /**
     * Returns the full writer for opaque values.
     */
    public IHDF5OpaqueWriter opaque();

    // /////////////////////
    // Boolean
    // /////////////////////

    /**
     * Returns the full writer for boolean values.
     */
    @Override
    public IHDF5BooleanWriter bool();

    // /////////////////////
    // Bytes
    // /////////////////////

    /**
     * Returns the full writer for byte / int8.
     */
    @Override
    public IHDF5ByteWriter int8();

    // /////////////////////
    // Short
    // /////////////////////

    /**
     * Returns the full writer for short / int16.
     */
    @Override
    public IHDF5ShortWriter int16();

    // /////////////////////
    // Int
    // /////////////////////

    /**
     * Returns the full writer for int / int32.
     */
    @Override
    public IHDF5IntWriter int32();

    // /////////////////////
    // Long
    // /////////////////////

    /**
     * Returns the full writer for long / int64.
     */
    @Override
    public IHDF5LongWriter int64();

    // /////////////////////
    // Float
    // /////////////////////

    /**
     * Returns the full writer for float / float32.
     */
    @Override
    public IHDF5FloatWriter float32();

    // /////////////////////
    // Double
    // /////////////////////

    /**
     * Returns the full writer for long / float64.
     */
    @Override
    public IHDF5DoubleWriter float64();

    // /////////////////////
    // Enums
    // /////////////////////

    /**
     * Returns the full writer for enums.
     * 
     * @deprecated Use {@link #enumeration()} instead.
     */
    @Deprecated
    @Override
    public IHDF5EnumWriter enums();

    /**
     * Returns the full writer for enumerations.
     */
    @Override
    public IHDF5EnumWriter enumeration();

    // /////////////////////
    // Compounds
    // /////////////////////

    /**
     * Returns the full writer for compounds.
     * 
     * @deprecated Use {@link #compound()} instead.
     */
    @Deprecated
    @Override
    public IHDF5CompoundWriter compounds();

    /**
     * Returns the full reader for compounds.
     */
    @Override
    public IHDF5CompoundWriter compound();

    // /////////////////////
    // Strings
    // /////////////////////

    /**
     * Returns the full writer for strings.
     */
    @Override
    public IHDF5StringWriter string();

    // /////////////////////
    // Date & Time
    // /////////////////////

    /**
     * Returns the full writer for date and times.
     */
    @Override
    public IHDF5DateTimeWriter time();

    /**
     * Returns the full writer for time durations.
     */
    @Override
    public IHDF5TimeDurationWriter duration();

    // /////////////////////
    // Object references
    // /////////////////////

    /**
     * Returns the full reader for object references.
     */
    @Override
    public IHDF5ReferenceWriter reference();

}
