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

import ch.systemsx.cisd.base.mdarray.MDArray;

/**
 * An interface that provides methods for writing compound values to HDF5 files.
 * 
 * @author Bernd Rinn
 */
public interface IHDF5CompoundWriter extends IHDF5CompoundReader
{

    // /////////////////////
    // Attributes
    // /////////////////////

    /**
     * Sets a compound attribute to the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param attributeName The name of the attribute.
     * @param type The type definition of this compound type.
     * @param value The value of the attribute. May be a Data Transfer Object, a
     *            {@link HDF5CompoundDataMap}, {@link HDF5CompoundDataList} or <code>Object[]</code>
     *            .
     */
    public <T> void setAttr(String objectPath, String attributeName, HDF5CompoundType<T> type,
            T value);

    /**
     * Sets a compound attribute to the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param attributeName The name of the attribute.
     * @param value The value of the attribute. May be a Data Transfer Object, a
     *            {@link HDF5CompoundDataMap}, {@link HDF5CompoundDataList} or <code>Object[]</code>
     *            .
     */
    public <T> void setAttr(String objectPath, String attributeName, T value);

    /**
     * Sets a compound attribute array (of rank 1) to the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param attributeName The name of the attribute.
     * @param type The type definition of this compound type.
     * @param value The value of the attribute. Data Transfer Object, a {@link HDF5CompoundDataMap},
     *            {@link HDF5CompoundDataList} or <code>Object[]</code> .
     */
    public <T> void setArrayAttr(String objectPath, String attributeName, HDF5CompoundType<T> type,
            T[] value);

    /**
     * Sets a compound attribute array (of rank 1) to the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param attributeName The name of the attribute.
     * @param value The value of the attribute. May be a Data Transfer Object, a
     *            {@link HDF5CompoundDataMap}, {@link HDF5CompoundDataList} or <code>Object[]</code>
     *            .
     */
    public <T> void setArrayAttr(String objectPath, String attributeName, T[] value);

    /**
     * Sets a compound attribute array (of rank N) to the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param attributeName The name of the attribute.
     * @param type The type definition of this compound type.
     * @param value The value of the attribute. Data Transfer Object, a {@link HDF5CompoundDataMap},
     *            {@link HDF5CompoundDataList} or <code>Object[]</code> .
     */
    public <T> void setMDArrayAttr(String objectPath, String attributeName, HDF5CompoundType<T> type,
            MDArray<T> value);

    /**
     * Sets a compound attribute array (of rank N) to the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param attributeName The name of the attribute.
     * @param value The value of the attribute. May be a Data Transfer Object, a
     *            {@link HDF5CompoundDataMap}, {@link HDF5CompoundDataList} or <code>Object[]</code>
     *            .
     */
    public <T> void setMDArrayAttr(String objectPath, String attributeName, MDArray<T> value);

    // /////////////////////
    // Data Sets
    // /////////////////////

    /**
     * Writes out a compound value of <var>type</var> given in <var>data</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     */
    public <T> void write(String objectPath, HDF5CompoundType<T> type, T data);

    /**
     * Writes out a compound value. The type is inferred based on the values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The value of the data set. May be a pojo (Data Transfer Object), a
     *            {@link HDF5CompoundDataMap}, {@link HDF5CompoundDataList} or <code>Object[]</code>
     *            .
     * @see CompoundType
     * @see CompoundElement
     */
    public <T> void write(String objectPath, T data);

    /**
     * Writes out an array (of rank 1) of compound values. Uses a compact storage layout. Must only
     * be used for small data sets.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     * @param inspectorOrNull The inspector to be called after translating the Java objects to a
     *            byte array and before writing the byte array to the HDF5 file.
     */
    public <T> void write(String objectPath, HDF5CompoundType<T> type, T data,
            IByteArrayInspector inspectorOrNull);

    /**
     * Writes out an array (of rank 1) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     */
    public <T> void writeArray(String objectPath, HDF5CompoundType<T> type, T[] data);

    /**
     * Writes out an array (of rank 1) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     * @param features The storage features of the data set.
     */
    public <T> void writeArray(String objectPath, HDF5CompoundType<T> type, T[] data,
            HDF5GenericStorageFeatures features);

    /**
     * Writes out an array (of rank 1) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     * @param features The storage features of the data set.
     * @param inspectorOrNull The inspector to be called after translating the Java objects to a
     *            byte array and before writing the byte array to the HDF5 file.
     */
    public <T> void writeArray(String objectPath, HDF5CompoundType<T> type, T[] data,
            HDF5GenericStorageFeatures features, IByteArrayInspector inspectorOrNull);

    /**
     * Writes out an array (of rank 1) of compound values. The type is inferred based on the values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The value of the data set. May be a pojo (Data Transfer Object), a
     *            {@link HDF5CompoundDataMap}, {@link HDF5CompoundDataList} or <code>Object[]</code>
     *            .
     * @see CompoundType
     * @see CompoundElement
     */
    public <T> void writeArray(String objectPath, T[] data);

    /**
     * Writes out an array (of rank 1) of compound values. The type is inferred based on the values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The value of the data set. May be a {@link HDF5CompoundDataMap},
     *            {@link HDF5CompoundDataList} or <code>Object[]</code>.
     * @param features The storage features of the data set.
     * @see CompoundType
     * @see CompoundElement
     */
    public <T> void writeArray(String objectPath, T[] data, HDF5GenericStorageFeatures features);

    /**
     * Writes out a block <var>blockNumber</var> of an array (of rank 1) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     * @param blockNumber The number of the block to write.
     */
    public <T> void writeArrayBlock(String objectPath, HDF5CompoundType<T> type, T[] data,
            long blockNumber);

    /**
     * Writes out a block <var>blockNumber</var> of an array (of rank 1) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     * @param blockNumber The number of the block to write.
     * @param inspectorOrNull The inspector to be called after translating the Java objects to a
     *            byte array and before writing the byte array to the HDF5 file.
     */
    public <T> void writeArrayBlock(String objectPath, HDF5CompoundType<T> type, T[] data,
            long blockNumber, IByteArrayInspector inspectorOrNull);

    /**
     * Writes out a block of an array (of rank 1) of compound values with given <var>offset</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     * @param offset The offset of the block in the data set.
     */
    public <T> void writeArrayBlockWithOffset(String objectPath, HDF5CompoundType<T> type,
            T[] data, long offset);

    /**
     * Writes out a block of an array (of rank 1) of compound values with given <var>offset</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The value of the data set.
     * @param offset The offset of the block in the data set.
     * @param inspectorOrNull The inspector to be called after translating the Java objects to a
     *            byte array and before writing the byte array to the HDF5 file.
     */
    public <T> void writeArrayBlockWithOffset(String objectPath, HDF5CompoundType<T> type,
            T[] data, long offset, IByteArrayInspector inspectorOrNull);

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
    public <T> void createArray(String objectPath, HDF5CompoundType<T> type, int size);

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
    public <T> void createArray(String objectPath, HDF5CompoundType<T> type, long size,
            int blockSize);

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
    public <T> void createArray(String objectPath, HDF5CompoundType<T> type, long size,
            int blockSize, HDF5GenericStorageFeatures features);

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
    public <T> void createArray(String objectPath, HDF5CompoundType<T> type, long size,
            HDF5GenericStorageFeatures features);

    /**
     * Writes out an array (of rank N) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The data to write.
     */
    public <T> void writeMDArray(String objectPath, HDF5CompoundType<T> type, MDArray<T> data);

    /**
     * Writes out an array (of rank N) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The data to write.
     * @param features The storage features of the data set.
     */
    public <T> void writeMDArray(String objectPath, HDF5CompoundType<T> type, MDArray<T> data,
            HDF5GenericStorageFeatures features);

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
    public <T> void writeMDArray(String objectPath, HDF5CompoundType<T> type, MDArray<T> data,
            HDF5GenericStorageFeatures features, IByteArrayInspector inspectorOrNull);

    /**
     * Writes out an array (of rank N) of compound values. The type is inferred based on the values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The value of the data set. May be a pojo (Data Transfer Object), a
     *            {@link HDF5CompoundDataMap}, {@link HDF5CompoundDataList} or <code>Object[]</code>
     *            .
     * @see CompoundType
     * @see CompoundElement
     */
    public <T> void writeMDArray(String objectPath, MDArray<T> data);

    /**
     * Writes out an array (of rank N) of compound values. The type is inferred based on the values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The value of the data set. May be a pojo (Data Transfer Object), a
     *            {@link HDF5CompoundDataMap}, {@link HDF5CompoundDataList} or <code>Object[]</code>
     *            .
     * @param features The storage features of the data set.
     * @see CompoundType
     * @see CompoundElement
     */
    public <T> void writeMDArray(String objectPath, MDArray<T> data,
            HDF5GenericStorageFeatures features);

    /**
     * Writes out a block of an array (of rank N) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The data to write.
     * @param blockNumber The block number in each dimension (offset: multiply with the extend in
     *            the according dimension).
     */
    public <T> void writeMDArrayBlock(String objectPath, HDF5CompoundType<T> type, MDArray<T> data,
            long[] blockNumber);

    /**
     * Writes out a block of an array (of rank N) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The data to write.
     * @param blockNumber The extent of the block to write on each axis.
     * @param inspectorOrNull The inspector to be called after translating the Java objects to a
     *            byte array and before writing the byte array to the HDF5.
     */
    public <T> void writeMDArrayBlock(String objectPath, HDF5CompoundType<T> type, MDArray<T> data,
            long[] blockNumber, IByteArrayInspector inspectorOrNull);

    /**
     * Writes out a block of an array (of rank N) of compound values give a given <var>offset</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param data The data to write.
     * @param offset The offset of the block to write on each axis.
     */
    public <T> void writeMDArrayBlockWithOffset(String objectPath, HDF5CompoundType<T> type,
            MDArray<T> data, long[] offset);

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
    public <T> void writeMDArrayBlockWithOffset(String objectPath, HDF5CompoundType<T> type,
            MDArray<T> data, long[] offset, IByteArrayInspector inspectorOrNull);

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
    public <T> void writeMDArrayBlockWithOffset(String objectPath, HDF5CompoundType<T> type,
            MDArray<T> data, int[] blockDimensions, long[] offset, int[] memoryOffset);

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
    public <T> void writeMDArrayBlockWithOffset(String objectPath, HDF5CompoundType<T> type,
            MDArray<T> data, int[] blockDimensions, long[] offset, int[] memoryOffset,
            IByteArrayInspector inspectorOrNull);

    /**
     * Creates an array (of rank N) of compound values.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param type The type definition of this compound type.
     * @param dimensions The dimensions of the compound array to create. This will be the total
     *            dimensions for non-extendable data sets and the dimensions of one chunk (along
     *            each axis) for extendable (chunked) data sets. For extendable data sets the
     *            initial size of the array (along each axis) will be 0, see
     *            {@link ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator#dontUseExtendableDataTypes}.
     */
    public <T> void createMDArray(String objectPath, HDF5CompoundType<T> type, int[] dimensions);

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
    public <T> void createMDArray(String objectPath, HDF5CompoundType<T> type, long[] dimensions,
            int[] blockDimensions);

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
    public <T> void createMDArray(String objectPath, HDF5CompoundType<T> type, long[] dimensions,
            int[] blockDimensions, HDF5GenericStorageFeatures features);

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
    public <T> void createMDArray(String objectPath, HDF5CompoundType<T> type, int[] dimensions,
            HDF5GenericStorageFeatures features);

}
