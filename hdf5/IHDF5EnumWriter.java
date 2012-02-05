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
public interface IHDF5EnumWriter extends IHDF5EnumReader, IHDF5EnumValueCreator
{

    // /////////////////////
    // Types
    // /////////////////////

    /**
     * Returns an anonymous enumeration type for this HDF5 file. An anonymous type can only be
     * retrieved by {@link IHDF5EnumReader#getDataSetType(String)}.
     * 
     * @param options The values of the enumeration type.
     * @throws HDF5JavaException If the data type exists and is not compatible with the
     *             <var>values</var> provided.
     */
    public HDF5EnumerationType getAnonType(final String[] options)
            throws HDF5JavaException;

    /**
     * Returns an anonymous enumeration type for this HDF5 file. An anonymous type can only be
     * retrieved by {@link IHDF5EnumReader#getDataSetType(String)}.
     * 
     * @param enumClass The enumeration class to get the values from.
     * @throws HDF5JavaException If the data type exists and is not compatible with the
     *             <var>enumClass</var> provided.
     */
    public HDF5EnumerationType getAnonType(final Class<? extends Enum<?>> enumClass)
            throws HDF5JavaException;

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
    public void setAttr(final String objectPath, final String name,
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
    public void setAttr(final String objectPath, final String name, final Enum<?> value)
            throws HDF5JavaException;

    /**
     * Sets an enum array attribute (of rank 1) to the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     */
    public void setArrayAttr(final String objectPath, final String name,
            final HDF5EnumerationValueArray value);

    /**
     * Sets an enum array (of rank N) attribute to the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     */
    public void setMDArrayAttr(final String objectPath, final String name,
            final HDF5EnumerationValueMDArray value);

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
    public void write(final String objectPath, final HDF5EnumerationValue value)
            throws HDF5JavaException;

    /**
     * Writes out an enum value.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param value The value of the data set.
     * @throws HDF5JavaException If the enum type of <var>value</var> is not a type of this file.
     */
    public void write(final String objectPath, final Enum<?> value) throws HDF5JavaException;

    /**
     * Writes out an array of enum values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write.
     * @throws HDF5JavaException If the enum type of <var>value</var> is not a type of this file.
     */
    public void writeArray(final String objectPath, final HDF5EnumerationValueArray data)
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
    public void writeArray(final String objectPath, final HDF5EnumerationValueArray data,
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
     * @return <var>enumType</var>
     */
    public HDF5EnumerationType createArray(final String objectPath,
            final HDF5EnumerationType enumType, final int size);

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
     * @return <var>enumType</var>
     */
    public HDF5EnumerationType createArray(final String objectPath,
            final HDF5EnumerationType enumType, final long size, final int blockSize);

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
     * @return <var>enumType</var>
     */
    public HDF5EnumerationType createArray(final String objectPath,
            final HDF5EnumerationType enumType, final long size,
            final HDF5IntStorageFeatures features);

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
     * @return <var>enumType</var>
     */
    public HDF5EnumerationType createArray(final String objectPath,
            final HDF5EnumerationType enumType, final long size, final int blockSize,
            final HDF5IntStorageFeatures features);

    /**
     * Writes out a block of an enum array (of rank 1). The data set needs to have been created by
     * {@link #createArray(String, HDF5EnumerationType, long, int, HDF5IntStorageFeatures)}
     * beforehand. Obviously the {@link HDF5EnumerationType} of the create call and this call needs
     * to match.
     * <p>
     * <i>Note:</i> For best performance, the block size in this method should be chosen to be equal
     * to the <var>blockSize</var> argument of the
     * {@link #createArray(String, HDF5EnumerationType, long, int, HDF5IntStorageFeatures)} call
     * that was used to create the data set.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. The value of {@link HDF5EnumerationValueArray#getLength()}
     *            defines the block size. Must not be <code>null</code> or of length 0.
     * @param blockNumber The number of the block to write.
     */
    public void writeArrayBlock(final String objectPath, final HDF5EnumerationValueArray data,
            final long blockNumber);

    /**
     * Writes out a block of an enum array (of rank 1). The data set needs to have been created by
     * {@link #createArray(String, HDF5EnumerationType, long, int, HDF5IntStorageFeatures)}
     * beforehand. Obviously the {@link HDF5EnumerationType} of the create call and this call needs
     * to match.
     * <p>
     * <i>Note:</i> For best performance, the block size in this method should be chosen to be equal
     * to the <var>blockSize</var> argument of the
     * {@link #createArray(String, HDF5EnumerationType, long, int, HDF5IntStorageFeatures)} call
     * that was used to create the data set.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. The value of {@link HDF5EnumerationValueArray#getLength()}
     *            defines the block size. Must not be <code>null</code> or of length 0.
     * @param dataSize The (real) size of <code>data</code> (needs to be
     *            <code><= data.getLength()</code> )
     * @param offset The offset in the data set to start writing to.
     */
    public void writeArrayBlockWithOffset(final String objectPath,
            final HDF5EnumerationValueArray data, final int dataSize, final long offset);

    /**
     * Writes out an array (of rank N) of Enum values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write.
     * @param features The storage features of the data set.
     */
    public void writeMDArray(final String objectPath, final HDF5EnumerationValueMDArray data,
            final HDF5IntStorageFeatures features);

    /**
     * Writes out an array (of rank N) of Enum values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write.
     */
    public void writeMDArray(final String objectPath, final HDF5EnumerationValueMDArray data);

    /**
     * Creates an array (of rank N) of Enum values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this Enum type.
     * @param dimensions The dimensions of the byte array to create. This will be the total
     *            dimensions for non-extendable data sets and the dimensions of one chunk (along
     *            each axis) for extendable (chunked) data sets. For extendable data sets the
     *            initial size of the array (along each axis) will be 0, see
     *            {@link ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator#dontUseExtendableDataTypes}.
     * @return <var>enumType</var>
     */
    public HDF5EnumerationType createMDArray(final String objectPath,
            final HDF5EnumerationType type, final int[] dimensions);

    /**
     * Creates an array (of rank N) of Enum values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this Enum type.
     * @param dimensions The extent of the Enum array along each of the axis.
     * @param blockDimensions The extent of one block along each of the axis. (for block-wise IO).
     *            Ignored if no extendable data sets are used (see
     *            {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()}) and
     *            <code>deflate == false</code>.
     * @return <var>enumType</var>
     */
    public HDF5EnumerationType createMDArray(final String objectPath,
            final HDF5EnumerationType type, final long[] dimensions, final int[] blockDimensions);

    /**
     * Creates an array (of rank N) of Enum values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this Enum type.
     * @param dimensions The extent of the Enum array along each of the axis.
     * @param blockDimensions The extent of one block along each of the axis. (for block-wise IO).
     *            Ignored if no extendable data sets are used (see
     *            {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()}) and
     *            <code>deflate == false</code>.
     * @param features The storage features of the data set.
     * @return <var>enumType</var>
     */
    public HDF5EnumerationType createMDArray(final String objectPath,
            final HDF5EnumerationType type, final long[] dimensions, final int[] blockDimensions,
            final HDF5IntStorageFeatures features);

    /**
     * Creates an array (of rank N) of Enum values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this Enum type.
     * @param dimensions The dimensions of the byte array to create. This will be the total
     *            dimensions for non-extendable data sets and the dimensions of one chunk (along
     *            each axis) for extendable (chunked) data sets. For extendable data sets the
     *            initial size of the array (along each axis) will be 0, see
     *            {@link HDF5GenericStorageFeatures}.
     * @param features The storage features of the data set.
     * @return <var>enumType</var>
     */
    public HDF5EnumerationType createMDArray(final String objectPath,
            final HDF5EnumerationType type, final int[] dimensions,
            final HDF5IntStorageFeatures features);

    /**
     * Writes out a block of an array (of rank N) of Enum values give a given <var>offset</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write.
     * @param blockNumber The block number in each dimension (offset: multiply with the extend in
     *            the according dimension).
     */
    public void writeMDArrayBlock(final String objectPath,
            final HDF5EnumerationValueMDArray data, final long[] blockNumber);

    /**
     * Writes out a block of an array (of rank N) of Enum values give a given <var>offset</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write.
     * @param offset The offset of the block in the data set to start writing to in each dimension.
     */
    public void writeMDArrayBlockWithOffset(final String objectPath,
            final HDF5EnumerationValueMDArray data, final long[] offset);

}
