/*
 * Copyright 2009 ETH Zuerich, CISD.
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

import ch.systemsx.cisd.base.mdarray.MDIntArray;

/**
 * An interface that provides methods for writing <code>int</code> values to HDF5 files.
 * 
 * @author Bernd Rinn
 */
public interface IHDF5IntBasicWriter
{
    // /////////////////////
    // Attributes
    // /////////////////////

    /**
     * Set a <code>int</code> attribute on the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void setIntAttribute(final String objectPath, final String name, final int value);

    /**
     * Set a <code>int[]</code> attribute on the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void setIntArrayAttribute(final String objectPath, final String name,
            final int[] value);

    /**
     * Set a multi-dimensional code>int</code> attribute on the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void setIntMDArrayAttribute(final String objectPath, final String name,
            final MDIntArray value);

    /**
     * Set a <code>int[][]</code> attribute on the referenced object.
     * <p>
     * The referenced object must exist, that is it need to have been written before by one of the
     * <code>write()</code> methods.
     * 
     * @param objectPath The name of the object to add the attribute to.
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     */
    public void setIntMatrixAttribute(final String objectPath, final String name,
            final int[][] value);
    
    // /////////////////////
    // Data Sets
    // /////////////////////

    /**
     * Writes out a <code>int</code> value.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param value The value to write.
     */
    public void writeInt(final String objectPath, final int value);

    /**
     * Writes out a <code>int</code> array (of rank 1).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>.
     */
    public void writeIntArray(final String objectPath, final int[] data);

    /**
     * Writes out a <code>int</code> array (of rank 1).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>.
     * @param features The storage features of the data set.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void writeIntArray(final String objectPath, final int[] data, 
            final HDF5IntStorageFeatures features);

    /**
     * Creates a <code>int</code> array (of rank 1).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The size of the int array to create. This will be the total size for 
     *          non-extendable data sets and the size of one chunk for extendable (chunked) data sets. 
     *          For extendable data sets the initial size of the array will be 0,
     *          see {@link ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator#dontUseExtendableDataTypes}.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void createIntArray(final String objectPath, final int size);

    /**
     * Creates a <code>int</code> array (of rank 1).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The size of the int array to create. When using extendable data sets 
     *          ((see {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()})), then no data 
     *          set smaller than this size can be created, however data sets may be larger.
     * @param blockSize The size of one block (for block-wise IO). Ignored if no extendable data 
     *          sets are used (see {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()}).
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void createIntArray(final String objectPath, final long size, final int blockSize);

    /**
     * Creates a <code>int</code> array (of rank 1).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The size of the int array to create. This will be the total size for 
     *          non-extendable data sets and the size of one chunk for extendable (chunked) data sets.
     *          For extendable data sets the initial size of the array will be 0,
     *          see {@link HDF5IntStorageFeatures}.
     * @param features The storage features of the data set.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void createIntArray(final String objectPath, final int size,
            final HDF5IntStorageFeatures features);
    
    /**
     * Creates a <code>int</code> array (of rank 1).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param size The size of the int array to create. When using extendable data sets 
     *          ((see {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()})), then no data 
     *          set smaller than this size can be created, however data sets may be larger.
     * @param blockSize The size of one block (for block-wise IO). Ignored if no extendable data 
     *          sets are used (see {@link IHDF5WriterConfigurator#dontUseExtendableDataTypes()}) and 
     *                <code>features</code> is <code>HDF5IntStorageFeature.INTNO_COMPRESSION</code>.
     * @param features The storage features of the data set.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void createIntArray(final String objectPath, final long size, final int blockSize,
            final HDF5IntStorageFeatures features);

    /**
     * Writes out a block of a <code>int</code> array (of rank 1). The data set needs to have
     * been created by {@link #createIntArray(String, long, int, HDF5IntStorageFeatures)}
     * beforehand.
     * <p>
     * <i>Note:</i> For best performance, the block size in this method should be chosen to be equal
     * to the <var>blockSize</var> argument of the
     * {@link #createIntArray(String, long, int, HDF5IntStorageFeatures)} call that was used to
     * create the data set.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. The length defines the block size. Must not be
     *            <code>null</code> or of length 0.
     * @param blockNumber The number of the block to write.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void writeIntArrayBlock(final String objectPath, final int[] data,
            final long blockNumber);

    /**
     * Writes out a block of a <code>int</code> array (of rank 1). The data set needs to have
     * been created by {@link #createIntArray(String, long, int, HDF5IntStorageFeatures)}
     * beforehand.
     * <p>
     * Use this method instead of {@link #writeIntArrayBlock(String, int[], long)} if the
     * total size of the data set is not a multiple of the block size.
     * <p>
     * <i>Note:</i> For best performance, the typical <var>dataSize</var> in this method should be
     * chosen to be equal to the <var>blockSize</var> argument of the
     * {@link #createIntArray(String, long, int, HDF5IntStorageFeatures)} call that was used to
     * create the data set.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. The length defines the block size. Must not be
     *            <code>null</code> or of length 0.
     * @param dataSize The (real) size of <code>data</code> (needs to be <code><= data.length</code>
     *            )
     * @param offset The offset in the data set to start writing to.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void writeIntArrayBlockWithOffset(final String objectPath, final int[] data,
            final int dataSize, final long offset);

    /**
     * Writes out a <code>int</code> matrix (array of rank 2).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>. All columns need to have the
     *            same length.
     */
    public void writeIntMatrix(final String objectPath, final int[][] data);

    /**
     * Writes out a <code>int</code> matrix (array of rank 2).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>. All columns need to have the
     *            same length.
     * @param features The storage features of the data set.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void writeIntMatrix(final String objectPath, final int[][] data, 
            final HDF5IntStorageFeatures features);

    /**
     * Creates a <code>int</code> matrix (array of rank 2). The initial size of the matrix is 0.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param blockSizeX The size of one block in the x dimension.
     * @param blockSizeY The size of one block in the y dimension.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void createIntMatrix(final String objectPath, final int blockSizeX, 
            final int blockSizeY);

    /**
     * Creates a <code>int</code> matrix (array of rank 2).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param sizeX The size of the x dimension of the int matrix to create.
     * @param sizeY The size of the y dimension of the int matrix to create.
     * @param blockSizeX The size of one block in the x dimension.
     * @param blockSizeY The size of one block in the y dimension.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void createIntMatrix(final String objectPath, final long sizeX, final long sizeY,
            final int blockSizeX, final int blockSizeY);

    /**
     * Creates a <code>int</code> matrix (array of rank 2).
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param sizeX The size of the x dimension of the int matrix to create.
     * @param sizeY The size of the y dimension of the int matrix to create.
     * @param blockSizeX The size of one block in the x dimension.
     * @param blockSizeY The size of one block in the y dimension.
     * @param features The storage features of the data set.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void createIntMatrix(final String objectPath, final long sizeX, final long sizeY,
            final int blockSizeX, final int blockSizeY, final HDF5IntStorageFeatures features);

    /**
     * Writes out a block of a <code>int</code> matrix (array of rank 2). The data set needs to
     * have been created by
     * {@link #createIntMatrix(String, long, long, int, int, HDF5IntStorageFeatures)} beforehand.
     * <p>
     * Use this method instead of
     * {@link #createIntMatrix(String, long, long, int, int, HDF5IntStorageFeatures)} if the total
     * size of the data set is not a multiple of the block size.
     * <p>
     * <i>Note:</i> For best performance, the size of <var>data</var> in this method should match
     * the <var>blockSizeX/Y</var> arguments of the
     * {@link #createIntMatrix(String, long, long, int, int, HDF5IntStorageFeatures)} call that was
     * used to create the data set.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. The length defines the block size. Must not be
     *            <code>null</code> or of length 0.
     * @param blockNumberX The block number in the x dimension (offset: multiply with
     *            <code>data.length</code>).
     * @param blockNumberY The block number in the y dimension (offset: multiply with
     *            <code>data[0.length</code>).
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void writeIntMatrixBlock(final String objectPath, final int[][] data,
            final long blockNumberX, final long blockNumberY);

    /**
     * Writes out a block of a <code>int</code> matrix (array of rank 2). The data set needs to
     * have been created by
     * {@link #createIntMatrix(String, long, long, int, int, HDF5IntStorageFeatures)} beforehand.
     * <p>
     * Use this method instead of {@link #writeIntMatrixBlock(String, int[][], long, long)} if
     * the total size of the data set is not a multiple of the block size.
     * <p>
     * <i>Note:</i> For best performance, the typical <var>dataSize</var> in this method should be
     * chosen to be equal to the <var>blockSize</var> argument of the
     * {@link #createIntMatrix(String, long, long, int, int, HDF5IntStorageFeatures)} call that was
     * used to create the data set.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write.
     * @param offsetX The x offset in the data set to start writing to.
     * @param offsetY The y offset in the data set to start writing to.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void writeIntMatrixBlockWithOffset(final String objectPath, final int[][] data,
            final long offsetX, final long offsetY);

    /**
     * Writes out a block of a <code>int</code> matrix (array of rank 2). The data set needs to
     * have been created by
     * {@link #createIntMatrix(String, long, long, int, int, HDF5IntStorageFeatures)} beforehand.
     * <p>
     * Use this method instead of {@link #writeIntMatrixBlock(String, int[][], long, long)} if
     * the total size of the data set is not a multiple of the block size.
     * <p>
     * <i>Note:</i> For best performance, the typical <var>dataSize</var> in this method should be
     * chosen to be equal to the <var>blockSize</var> argument of the
     * {@link #createIntMatrix(String, long, long, int, int, HDF5IntStorageFeatures)} call that was
     * used to create the data set.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write.
     * @param dataSizeX The (real) size of <code>data</code> along the x axis (needs to be
     *            <code><= data.length</code> )
     * @param dataSizeY The (real) size of <code>data</code> along the y axis (needs to be
     *            <code><= data[0].length</code> )
     * @param offsetX The x offset in the data set to start writing to.
     * @param offsetY The y offset in the data set to start writing to.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void writeIntMatrixBlockWithOffset(final String objectPath, final int[][] data,
            final int dataSizeX, final int dataSizeY, final long offsetX, final long offsetY);

    /**
     * Writes out a multi-dimensional <code>int</code> array.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>. All columns need to have the
     *            same length.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void writeIntMDArray(final String objectPath, final MDIntArray data);

    /**
     * Writes out a multi-dimensional <code>int</code> array.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>. All columns need to have the
     *            same length.
     * @param features The storage features of the data set.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void writeIntMDArray(final String objectPath, final MDIntArray data,
            final HDF5IntStorageFeatures features);

    /**
     * Creates a multi-dimensional <code>int</code> array.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param dimensions The dimensions of the int array to create. This will be the total dimensions 
     *          for non-extendable data sets and the dimensions of one chunk (extent along each axis) 
     *          for extendable (chunked) data sets. For extendable data sets the initial size of the 
     *          array along each axis will be 0, 
     *          see {@link ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator#dontUseExtendableDataTypes}.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void createIntMDArray(final String objectPath, final int[] dimensions);

    /**
     * Creates a multi-dimensional <code>int</code> array.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param dimensions The dimensions of the array.
     * @param blockDimensions The dimensions of one block (chunk) of the array.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void createIntMDArray(final String objectPath, final long[] dimensions,
            final int[] blockDimensions);

    /**
     * Creates a multi-dimensional <code>int</code> array.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param dimensions The dimensions of the array. Will be the total dimensions for non-extendable 
     *       data sets and the dimensions of one chunk for extendable (chunked) data sets
     *       For extendable data sets the initial size of the array along each axis will be 0,
     *       see {@link HDF5IntStorageFeatures}.
     * @param features The storage features of the data set.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void createIntMDArray(final String objectPath, final int[] dimensions,
            final HDF5IntStorageFeatures features);

    /**
     * Creates a multi-dimensional <code>int</code> array.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param dimensions The dimensions of the array.
     * @param blockDimensions The dimensions of one block (chunk) of the array.
     * @param features The storage features of the data set.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void createIntMDArray(final String objectPath, final long[] dimensions,
            final int[] blockDimensions, final HDF5IntStorageFeatures features);

    /**
     * Writes out a block of a multi-dimensional <code>int</code> array.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>. All columns need to have the
     *            same length.
     * @param blockNumber The block number in each dimension (offset: multiply with the extend in
     *            the according dimension).
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void writeIntMDArrayBlock(final String objectPath, final MDIntArray data,
            final long[] blockNumber);

    /**
     * Writes out a block of a multi-dimensional <code>int</code> array.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>. All columns need to have the
     *            same length.
     * @param offset The offset in the data set  to start writing to in each dimension.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void writeIntMDArrayBlockWithOffset(final String objectPath, final MDIntArray data,
            final long[] offset);

   /**
     * Writes out a block of a multi-dimensional <code>int</code> array.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     * @param data The data to write. Must not be <code>null</code>.
     * @param blockDimensions The dimensions of the block to write to the data set.
     * @param offset The offset of the block in the data set to start writing to in each dimension.
     * @param memoryOffset The offset of the block in the <var>data</var> array.
     * @deprecated Use the corresponding method in {@link IHDF5Writer#int32()}.
     */
    @Deprecated
    public void writeIntMDArrayBlockWithOffset(final String objectPath, final MDIntArray data,
            final int[] blockDimensions, final long[] offset, final int[] memoryOffset);
}
