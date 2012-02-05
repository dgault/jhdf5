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

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.hdf5.HDF5DataTypeInformation.DataTypeInfoOptions;

/**
 * An interface for reading HDF5 files (HDF5 1.8.x and older).
 * <p>
 * The interface focuses on ease of use instead of completeness. As a consequence not all features
 * of a valid HDF5 files can be read using this class, but only a subset. (All information written
 * by {@link IHDF5Writer} can be read by this class.)
 * <p>
 * Usage:
 * 
 * <pre>
 * IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(new File(&quot;test.h5&quot;));
 * float[] f = reader.readFloatArray(&quot;/some/path/dataset&quot;);
 * String s = reader.getStringAttribute(&quot;/some/path/dataset&quot;, &quot;some key&quot;);
 * reader.close();
 * </pre>
 * 
 * @author Bernd Rinn
 */
public interface IHDF5Reader extends IHDF5SimpleReader, IHDF5PrimitiveReader, IHDF5StringReader,
        IHDF5EnumBasicReader, IHDF5CompoundReader, IHDF5BooleanReader, IHDF5GenericReader,
        IHDF5DateTimeReader, IHDF5ReferenceReader
{

    // /////////////////////
    // Configuration
    // /////////////////////

    /**
     * Returns <code>true</code>, if numeric conversions should be performed automatically, e.g.
     * between <code>float</code> and <code>int</code>.
     */
    public boolean isPerformNumericConversions();

    /**
     * Returns the HDF5 file that this class is reading.
     */
    public File getFile();

    /**
     * Closes this object and the file referenced by this object. This object must not be used after
     * being closed.
     */
    public void close();

    // /////////////////////
    // Objects & Links
    // /////////////////////

    /**
     * Returns the link information for the given <var>objectPath</var>. If <var>objectPath</var>
     * does not exist, the link information will have a type {@link HDF5ObjectType#NONEXISTENT}.
     */
    public HDF5LinkInformation getLinkInformation(final String objectPath);

    /**
     * Returns the object information for the given <var>objectPath</var>. If <var>objectPath</var>
     * is a symbolic link, this method will return the type of the object that this link points to
     * rather than the type of the link. If <var>objectPath</var> does not exist, the object
     * information will have a type {@link HDF5ObjectType#NONEXISTENT} and the other fields will not
     * be set.
     */
    public HDF5ObjectInformation getObjectInformation(final String objectPath);

    /**
     * Returns the type of the given <var>objectPath<var>. If <var>followLink</var> is
     * <code>false</code> and <var>objectPath<var> is a symbolic link, this method will return the
     * type of the link rather than the type of the object that the link points to.
     */
    public HDF5ObjectType getObjectType(final String objectPath, boolean followLink);

    /**
     * Returns the type of the given <var>objectPath</var>. If <var>objectPath</var> is a symbolic
     * link, this method will return the type of the object that this link points to rather than the
     * type of the link, that is, it will follow symbolic links.
     */
    public HDF5ObjectType getObjectType(final String objectPath);

    /**
     * Returns <code>true</code>, if <var>objectPath</var> exists and <code>false</code> otherwise.
     * if <var>followLink</var> is <code>false</code> and <var>objectPath</var> is a symbolic link,
     * this method will return <code>true</code> regardless of whether the link target exists or
     * not.
     */
    public boolean exists(final String objectPath, boolean followLink);

    /**
     * Returns <code>true</code>, if <var>objectPath</var> exists and <code>false</code> otherwise.
     * If <var>objectPath</var> is a symbolic link, the method will return <code>true</code> if the
     * link target exists, that is, this method will follow symbolic links.
     */
    public boolean exists(final String objectPath);

    /**
     * Returns <code>true</code> if the <var>objectPath</var> exists and represents a group and
     * <code>false</code> otherwise. Note that if <var>followLink</var> is <code>false</code> this
     * method will return <code>false</code> if <var>objectPath</var> is a symbolic link that points
     * to a group.
     */
    public boolean isGroup(final String objectPath, boolean followLink);

    /**
     * Returns <code>true</code> if the <var>objectPath</var> exists and represents a group and
     * <code>false</code> otherwise. Note that if <var>objectPath</var> is a symbolic link, this
     * method will return <code>true</code> if the link target of the symbolic link is a group, that
     * is, this method will follow symbolic links.
     */
    public boolean isGroup(final String objectPath);

    /**
     * Returns <code>true</code> if the <var>objectPath</var> exists and represents a data set and
     * <code>false</code> otherwise. Note that if <var>followLink</var> is <code>false</code> this
     * method will return <code>false</code> if <var>objectPath</var> is a symbolic link that points
     * to a data set.
     */
    public boolean isDataSet(final String objectPath, boolean followLink);

    /**
     * Returns <code>true</code> if the <var>objectPath</var> exists and represents a data set and
     * <code>false</code> otherwise. Note that if <var>objectPath</var> is a symbolic link, this
     * method will return <code>true</code> if the link target of the symbolic link is a data set,
     * that is, this method will follow symbolic links.
     */
    public boolean isDataSet(final String objectPath);

    /**
     * Returns <code>true</code> if the <var>objectPath</var> exists and represents a data type and
     * <code>false</code> otherwise. Note that if <var>followLink</var> is <code>false</code> this
     * method will return <code>false</code> if <var>objectPath</var> is a symbolic link that points
     * to a data type.
     */
    public boolean isDataType(final String objectPath, boolean followLink);

    /**
     * Returns <code>true</code> if the <var>objectPath</var> exists and represents a data type and
     * <code>false</code> otherwise. Note that if <var>objectPath</var> is a symbolic link, this
     * method will return <code>true</code> if the link target of the symbolic link is a data type,
     * that is, this method will follow symbolic links.
     */
    public boolean isDataType(final String objectPath);

    /**
     * Returns <code>true</code> if the <var>objectPath</var> exists and represents a soft link and
     * <code>false</code> otherwise.
     */
    public boolean isSoftLink(final String objectPath);

    /**
     * Returns <code>true</code> if the <var>objectPath</var> exists and represents an external link
     * and <code>false</code> otherwise.
     */
    public boolean isExternalLink(final String objectPath);

    /**
     * Returns <code>true</code> if the <var>objectPath</var> exists and represents either a soft
     * link or an external link and <code>false</code> otherwise.
     */
    public boolean isSymbolicLink(final String objectPath);

    /**
     * Returns the target of the symbolic link that <var>objectPath</var> points to, or
     * <code>null</code>, if <var>objectPath</var> is not a symbolic link.
     */
    public String tryGetSymbolicLinkTarget(final String objectPath);

    /**
     * Returns the path of the data type of the data set <var>objectPath</var>, or <code>null</code>
     * , if this data set is not of a named data type.
     */
    public String tryGetDataTypePath(final String objectPath);

    /**
     * Returns the path of the data <var>type</var>, or <code>null</code>, if <var>type</var> is not
     * a named data type.
     */
    public String tryGetDataTypePath(HDF5DataType type);

    /**
     * Returns the names of the attributes of the given <var>objectPath</var>.
     * 
     * @param objectPath The name (including path information) of the object (data set or group) to
     *            return the attributes for.
     */
    public List<String> getAttributeNames(final String objectPath);

    /**
     * Returns the names of all attributes of the given <var>objectPath</var>.
     * <p>
     * This may include attributes that are used internally by the library and are not supposed to
     * be changed by application programmers.
     * 
     * @param objectPath The name (including path information) of the object (data set or group) to
     *            return the attributes for.
     */
    public List<String> getAllAttributeNames(final String objectPath);

    /**
     * Returns the information about a data set as a {@link HDF5DataTypeInformation} object.
     * 
     * @param objectPath The name (including path information) of the object that has the attribute
     *            to return information about.
     * @param attributeName The name of the attribute to get information about.
     */
    public HDF5DataTypeInformation getAttributeInformation(final String objectPath,
            final String attributeName);

    /**
     * Returns the information about a data set as a {@link HDF5DataTypeInformation} object.
     * 
     * @param objectPath The name (including path information) of the object that has the attribute
     *            to return information about.
     * @param attributeName The name of the attribute to get information about.
     * @param dataTypeInfoOptions The options on which information to get about the member data
     *            types.
     */
    public HDF5DataTypeInformation getAttributeInformation(final String objectPath,
            final String attributeName, final DataTypeInfoOptions dataTypeInfoOptions);

    /**
     * Returns the information about a data set as a {@link HDF5DataSetInformation} object. It is a
     * failure condition if the <var>dataSetPath</var> does not exist or does not identify a data
     * set.
     * 
     * @param dataSetPath The name (including path information) of the data set to return
     *            information about.
     */
    public HDF5DataSetInformation getDataSetInformation(final String dataSetPath);

    /**
     * Returns the information about a data set as a {@link HDF5DataSetInformation} object. It is a
     * failure condition if the <var>dataSetPath</var> does not exist or does not identify a data
     * set.
     * 
     * @param dataSetPath The name (including path information) of the data set to return
     *            information about.
     * @param dataTypeInfoOptions The options on which information to get about the member data
     *            types.
     */
    public HDF5DataSetInformation getDataSetInformation(final String dataSetPath,
            final DataTypeInfoOptions dataTypeInfoOptions);

    /**
     * Returns the total size (in bytes) of <var>objectPath</var>. It is a failure condition if the
     * <var>dataSetPath</var> does not exist or does not identify a data set. This method follows
     * symbolic links.
     */
    public long getSize(final String objectPath);

    /**
     * Returns the total number of elements of <var>objectPath</var> It is a failure condition if
     * the <var>dataSetPath</var> does not exist or does not identify a data set. This method
     * follows symbolic links.
     */
    public long getNumberOfElements(final String objectPath);

    /**
     * Copies the <var>sourceObject</var> to the <var>destinationObject</var> of the HDF5 file
     * represented by the <var>destinationWriter</var>. If <var>destiantionObject</var> ends with
     * "/", it will be considered a group and the name of <var>sourceObject</var> will be appended.
     */
    public void copy(String sourceObject, IHDF5Writer destinationWriter, String destinationObject);

    /**
     * Copies the <var>sourceObject</var> to the root group of the HDF5 file represented by the
     * <var>destinationWriter</var>.
     */
    public void copy(String sourceObject, IHDF5Writer destinationWriter);

    /**
     * Copies all objects of the file represented by this reader to the root group of the HDF5 file
     * represented by the <var>destinationWriter</var>.
     */
    public void copyAll(IHDF5Writer destinationWriter);

    // /////////////////////
    // Group
    // /////////////////////

    /**
     * Returns the members of <var>groupPath</var>. The order is <i>not</i> well defined.
     * 
     * @param groupPath The path of the group to get the members for.
     * @throws IllegalArgumentException If <var>groupPath</var> is not a group.
     */
    public List<String> getGroupMembers(final String groupPath);

    /**
     * Returns all members of <var>groupPath</var>, including internal groups that may be used by
     * the library to do house-keeping. The order is <i>not</i> well defined.
     * 
     * @param groupPath The path of the group to get the members for.
     * @throws IllegalArgumentException If <var>groupPath</var> is not a group.
     */
    public List<String> getAllGroupMembers(final String groupPath);

    /**
     * Returns the paths of the members of <var>groupPath</var> (including the parent). The order is
     * <i>not</i> well defined.
     * 
     * @param groupPath The path of the group to get the member paths for.
     * @throws IllegalArgumentException If <var>groupPath</var> is not a group.
     */
    public List<String> getGroupMemberPaths(final String groupPath);

    /**
     * Returns the link information about the members of <var>groupPath</var>. The order is
     * <i>not</i> well defined.
     * 
     * @param groupPath The path of the group to get the members for.
     * @param readLinkTargets If <code>true</code>, for symbolic links the link targets will be
     *            available via {@link HDF5LinkInformation#tryGetSymbolicLinkTarget()}.
     * @throws IllegalArgumentException If <var>groupPath</var> is not a group.
     */
    public List<HDF5LinkInformation> getGroupMemberInformation(final String groupPath,
            boolean readLinkTargets);

    /**
     * Returns the link information about all members of <var>groupPath</var>. The order is
     * <i>not</i> well defined.
     * <p>
     * This may include attributes that are used internally by the library and are not supposed to
     * be changed by application programmers.
     * 
     * @param groupPath The path of the group to get the members for.
     * @param readLinkTargets If <code>true</code>, the link targets will be read for symbolic
     *            links.
     * @throws IllegalArgumentException If <var>groupPath</var> is not a group.
     */
    public List<HDF5LinkInformation> getAllGroupMemberInformation(final String groupPath,
            boolean readLinkTargets);

    // /////////////////////
    // Types
    // /////////////////////

    /**
     * Returns the data type variant of <var>objectPath</var>, or <code>null</code>, if no type
     * variant is defined for this <var>objectPath</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @return The data type variant or <code>null</code>.
     */
    public HDF5DataTypeVariant tryGetTypeVariant(final String objectPath);

    /**
     * Returns the data type variant of <var>attributeName</var> of object <var>objectPath</var>, or
     * <code>null</code>, if no type variant is defined for this <var>objectPath</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @return The data type variant or <code>null</code>.
     */
    public HDF5DataTypeVariant tryGetTypeVariant(final String objectPath, String attributeName);

    // /////////////////////
    // Attributes
    // /////////////////////

    /**
     * Returns <code>true</code>, if the <var>objectPath</var> has an attribute with name
     * <var>attributeName</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param attributeName The name of the attribute to read.
     * @return <code>true</code>, if the attribute exists for the object.
     */
    public boolean hasAttribute(final String objectPath, final String attributeName);

    // /////////////////////
    // Enums
    // /////////////////////

    /**
     * Returns the full reader for enums.
     */
    public IHDF5EnumReader enums();

}
