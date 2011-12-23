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

import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;

/**
 * An interface that provides methods for writing enumeration values from HDF5 files.
 * 
 * @author Bernd Rinn
 */
public interface IHDF5EnumWriter extends IHDF5EnumTypeRetriever
{

    // /////////////////////
    // Attributes
    // /////////////////////

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

    /**
     * Sets an enum attribute to the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     * @throws HDF5JavaException If the enum type of <var>value</var> is not a type of this file.
     */
    public void setEnumAttribute(final String objectPath, final String name, final Enum<?> value)
            throws HDF5JavaException;

    /**
     * Sets an enum array attribute to the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     */
    public void setEnumArrayAttribute(final String objectPath, final String name,
            final HDF5EnumerationValueArray value);

    /**
     * Sets an enum array attribute to the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param name The name of the attribute.
     * @param data The data to write.
     * @throws HDF5JavaException If the enum type of <var>value</var> is not a type of this file.
     */
    public void setEnumArrayAttribute(final String objectPath, final String name,
            final Enum<?>[] data) throws HDF5JavaException;

    // /////////////////////
    // Data Sets
    // /////////////////////

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
     * Writes out an enum value.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param value The value of the data set.
     * @throws HDF5JavaException If the enum type of <var>value</var> is not a type of this file.
     */
    public void writeEnum(final String objectPath, final Enum<?> value) throws HDF5JavaException;

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
     * Writes out an array of enum values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write.
     * @throws HDF5JavaException If the enum type of <var>value</var> is not a type of this file.
     */
    public <T extends Enum<T>> void writeEnumArray(final String objectPath, final Enum<T>[] data)
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
    public <T extends Enum<T>> void writeEnumArray(final String objectPath, final Enum<T>[] data,
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
            final long size);

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
     * @param size The size of the enum array to create. This will be the total size for
     *            non-extendable data sets and the size of one chunk for extendable (chunked) data
     *            sets. For extendable data sets the initial size of the array will be 0, see
     *            {@link HDF5IntStorageFeatures}.
     * @param features The storage features of the data set.
     */
    public void createEnumArray(final String objectPath, final HDF5EnumerationType enumType,
            final long size, final HDF5IntStorageFeatures features);

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
     * @param enumType The Java enumeration of this array.
     * @param size The size of the enum array to create. When using extendable data sets ((see
     *            {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()})), then no data set
     *            smaller than this size can be created, however data sets may be larger.
     * @return The HDF5 enumeration type of the array.
     */
    public HDF5EnumerationType createEnumArray(final String objectPath,
            final Class<? extends Enum<?>> enumType, final long size);

    /**
     * Creates am enum array (of rank 1). The initial size of the array is 0.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param enumClass the {@link Enum} class to represent the values of.
     * @param size The size of the enum array to create. When using extendable data sets ((see
     *            {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()})), then no data set
     *            smaller than this size can be created, however data sets may be larger.
     * @param blockSize The size of one block (for block-wise IO). Ignored if no extendable data
     *            sets are used (see {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()}).
     * @return The HDF5 enumeration type of the array.
     */
    public HDF5EnumerationType createEnumArray(final String objectPath,
            final Class<? extends Enum<?>> enumClass, final long size, final int blockSize);

    /**
     * Creates am enum array (of rank 1). The initial size of the array is 0.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param enumClass the {@link Enum} class to represent the values of.
     * @param size The size of the enum array to create. When using extendable data sets ((see
     *            {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()})), then no data set
     *            smaller than this size can be created, however data sets may be larger.
     * @param features The storage features of the data set.
     * @return The HDF5 enumeration type of the array.
     */
    public HDF5EnumerationType createEnumArray(final String objectPath,
            final Class<? extends Enum<?>> enumClass, final long size,
            final HDF5IntStorageFeatures features);

    /**
     * Creates am enum array (of rank 1). The initial size of the array is 0.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param enumClass the {@link Enum} class to represent the values of.
     * @param size The size of the enum array to create. When using extendable data sets ((see
     *            {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()})), then no data set
     *            smaller than this size can be created, however data sets may be larger.
     * @param blockSize The size of one block (for block-wise IO). Ignored if no extendable data
     *            sets are used (see {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()}).
     * @param features The storage features of the data set.
     * @return The HDF5 enumeration type of the array.
     */
    public HDF5EnumerationType createEnumArray(final String objectPath,
            final Class<? extends Enum<?>> enumClass, final long size, final int blockSize,
            final HDF5IntStorageFeatures features);

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
     * @param enumType The enumeration type of the dataset.
     * @param data The data to write. The length of the array defines the block size. Must not be
     *            <code>null</code> or of length 0.
     * @param blockNumber The number of the block to write.
     */
    public void writeEnumArrayBlock(final String objectPath, final HDF5EnumerationType enumType,
            final Enum<?>[] data, final long blockNumber);

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
     * @param data The data to write. The length of the array defines the block size. Must not be
     *            <code>null</code> or of length 0.
     * @param blockNumber The number of the block to write.
     */
    public void writeEnumArrayBlock(final String objectPath, final Enum<?>[] data,
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
     * @param enumType The enumeration type of the dataset.
     * @param data The data to write. The length of the array defines the block size. Must not be
     *            <code>null</code> or of length 0.
     * @param dataSize The (real) size of <code>data</code> (needs to be
     *            <code><= data.getLength()</code> )
     * @param offset The offset in the data set to start writing to.
     */
    public void writeEnumArrayBlockWithOffset(final String objectPath,
            final HDF5EnumerationType enumType, Enum<?>[] data, final int dataSize,
            final long offset);

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
    public void writeEnumArrayBlockWithOffset(final String objectPath, Enum<?>[] data,
            final int dataSize, final long offset);

}
