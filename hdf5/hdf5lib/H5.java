/****************************************************************************
 * NCSA HDF                                                                 *
 * National Comptational Science Alliance                                   *
 * University of Illinois at Urbana-Champaign                               *
 * 605 E. Springfield, Champaign IL 61820                                   *
 *                                                                          *
 * For conditions of distribution and use, see the accompanying             *
 * hdf-java/COPYING file.                                                   *
 *                                                                          *
 ****************************************************************************/

package ch.systemsx.cisd.hdf5.hdf5lib;

import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

import ch.systemsx.cisd.base.utilities.NativeLibraryUtilities;

/**
 * The low-level C function wrappers. These functions are <i>not</i> thread-safe and need to be used
 * through thread-safe wrappers.
 * <p>
 * <b>This is an internal API that should not be expected to be stable between releases!</b>
 */
class H5
{
    /** Expected major number of the library. */
    private final static int expectedMajnum = 1;

    /** Expected minor number of the library. */
    private final static int expectedMinnum = 8;

    /** Expected minimal release number of the library. */
    private final static int expectedRelnum = 13;

    static
    {
        synchronized (ncsa.hdf.hdf5lib.H5.class)
        {
            if (NativeLibraryUtilities.loadNativeLibrary("jhdf5") == false)
            {
                throw new UnsupportedOperationException("No suitable HDF5 native library found for this platform.");
            }
    
            // Important! Exit quietly
            try
            {
                H5dont_atexit();
            } catch (final HDF5LibraryException e)
            {
                System.exit(1);
            }
    
            H5error_off();
    
            // Ensure we have the expected version of the library (with at least the expected release
            // number)
            final int[] libversion = new int[3];
            H5get_libversion(libversion);
            if (libversion[0] != expectedMajnum || libversion[1] != expectedMinnum
                    || libversion[2] < expectedRelnum)
            {
                throw new UnsupportedOperationException("The HDF5 native library is outdated! It is version "
                        + libversion[0] + "." + libversion[1] + "." + libversion[2]
                        + ", but we require " + expectedMajnum + "." + expectedMinnum + ".x with x >= "
                        + expectedRelnum + ".");
            }
        }
    }

    /** Call to ensure that the native library is loaded. */
    public static void ensureNativeLibIsLoaded()
    {
    }

    // ////////////////////////////////////////////////////////////////

    /**
     * J2C converts a Java constant to an HDF5 constant determined at runtime
     * 
     * @param java_constant The value of Java constant
     * @return the value of an HDF5 constant determined at runtime
     */
    public static native int J2C(int java_constant);

    /**
     * Turn off error handling By default, the C library prints the error stack of the HDF-5 C
     * library on stdout. This behavior may be disabled by calling H5error_off().
     */
    public static native int H5error_off();

    // ////////////////////////////////////////////////////////////
    // //
    // Functions related to variable-length string copying       //
    // //
    // ////////////////////////////////////////////////////////////
    
    /**
     * Returns the size of a pointer on this platform.
     */
    public static native int getPointerSize();
    
    /**
     * Creates a C copy of str (using calloc) and put the reference of it into buf at bufOfs.
     */
    public static native int compoundCpyVLStr(String str, byte[] buf, int bufOfs);
    
    /**
     * Creates a Java copy from a C char* pointer in the buf at bufOfs. 
     */
    public static native String createVLStrFromCompound(byte[] buf, int bufOfs);
    
    /**
     * Frees the variable-length strings in compound buf, where one compound has size recordSize and the 
     * variable-length members can be found at byte-offsets vlIndices.
     */
    public static native int freeCompoundVLStr(byte[] buf, int recordSize, int[] vlIndices);

    // ////////////////////////////////////////////////////////////
    // //
    // H5: General Library Functions //
    // //
    // ////////////////////////////////////////////////////////////

    /**
     * H5open initialize the library.
     * 
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5open() throws HDF5LibraryException;

    /**
     * H5close flushes all data to disk, closes all file identifiers, and cleans up all memory used
     * by the library.
     * 
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5close() throws HDF5LibraryException;

    /**
     * H5dont_atexit indicates to the library that an atexit() cleanup routine should not be
     * installed. In order to be effective, this routine must be called before any other HDF
     * function calls, and must be called each time the library is loaded/linked into the
     * application (the first time and after it's been un-loaded).
     * <P>
     * This is called by the static initializer, so this should never need to be explicitly called
     * by a Java program.
     * 
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    private static native int H5dont_atexit() throws HDF5LibraryException;

    /**
     * H5get_libversion retrieves the major, minor, and release numbers of the version of the HDF
     * library which is linked to the application.
     * 
     * @param libversion The version information of the HDF library.
     * 
     *            <pre>
     * 
     *            libversion[0] = The major version of the library. libversion[1] = The minor
     *            version of the library. libversion[2] = The release number of the library.
     * 
     * </pre>
     * @return a non-negative value if successful, along with the version information.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5get_libversion(int[] libversion) throws HDF5LibraryException;

    /**
     * H5check_version verifies that the arguments match the version numbers compiled into the
     * library.
     * 
     * @param majnum The major version of the library.
     * @param minnum The minor version of the library.
     * @param relnum The release number of the library.
     * @return a non-negative value if successful. Upon failure (when the versions do not match),
     *         this function causes the application to abort (i.e., crash) See C API function:
     *         herr_t H5check_version()
     */
    public static native int H5check_version(int majnum, int minnum, int relnum);

    /**
     * H5garbage_collect collects on all free-lists of all types.
     * <p>
     * Note: this is new with HDF5.1.2.2. If using an earlier version, use 'configure
     * --enable-hdf5_1_2_1' so this routine will fail safely.
     * 
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5garbage_collect() throws HDF5LibraryException;

    // ////////////////////////////////////////////////////////////
    // //
    // H5E: Error Stack //
    // //
    // ////////////////////////////////////////////////////////////

    /**
     * H5Eclear clears the error stack for the current thread. H5Eclear can fail if there are
     * problems initializing the library.
     * <p>
     * This may be used by exception handlers to assure that the error condition in the HDF-5
     * library has been reset.
     * 
     * @return Returns a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Eclear() throws HDF5LibraryException;

    // ////////////////////////////////////////////////////////////
    // //
    // H5A: Attribute Interface Functions //
    // //
    // ////////////////////////////////////////////////////////////

    /**
     * H5Lexists returns <code>true</code> if an attribute with <var>name</var> exists for the
     * object defined by <var>obj_id</var> and <code> false </code> otherwise.
     */
    public static native boolean H5Aexists(int obj_id, String name) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5Acreate creates an attribute which is attached to the object specified with loc_id.
     * 
     * @param loc_id IN: Object (dataset, group, or named datatype) to be attached to.
     * @param name IN: Name of attribute to create.
     * @param type_id IN: Identifier of datatype for attribute.
     * @param space_id IN: Identifier of dataspace for attribute.
     * @param create_plist_id IN: Identifier of creation property list (currently not used).
     * @param access_plist_id IN: Attribute access property list identifier (currently not used).
     * @return an attribute identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Acreate(int loc_id, String name, int type_id, int space_id,
            int create_plist_id, int access_plist_id) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5Aopen_name opens an attribute specified by its name, name, which is attached to the object
     * specified with loc_id.
     * 
     * @param loc_id IN: Identifier of a group, dataset, or named datatype atttribute
     * @param name IN: Attribute name.
     * @return attribute identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Aopen_name(int loc_id, String name) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5Aopen_idx opens an attribute which is attached to the object specified with loc_id. The
     * location object may be either a group, dataset, or named datatype, all of which may have any
     * sort of attribute.
     * 
     * @param loc_id IN: Identifier of the group, dataset, or named datatype attribute
     * @param idx IN: Index of the attribute to open.
     * @return attribute identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Aopen_idx(int loc_id, int idx) throws HDF5LibraryException;

    /**
     * H5Awrite writes an attribute, specified with attr_id. The attribute's memory datatype is
     * specified with mem_type_id. The entire attribute is written from buf to the file.
     * 
     * @param attr_id IN: Identifier of an attribute to write.
     * @param mem_type_id IN: Identifier of the attribute datatype (in memory).
     * @param buf IN: Data to be written.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - data is null.
     */
    public static native int H5Awrite(int attr_id, int mem_type_id, byte[] buf)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Awrite writes an attribute, specified with attr_id. The attribute's memory datatype is
     * specified with mem_type_id. The entire attribute is written from buf to the file.
     * 
     * @param attr_id IN: Identifier of an attribute to write.
     * @param mem_type_id IN: Identifier of the attribute datatype (in memory).
     * @param buf IN: Data to be written.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - data is null.
     */
    public static native int H5Awrite(int attr_id, int mem_type_id, short[] buf)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Awrite writes an attribute, specified with attr_id. The attribute's memory datatype is
     * specified with mem_type_id. The entire attribute is written from buf to the file.
     * 
     * @param attr_id IN: Identifier of an attribute to write.
     * @param mem_type_id IN: Identifier of the attribute datatype (in memory).
     * @param buf IN: Data to be written.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - data is null.
     */
    public static native int H5Awrite(int attr_id, int mem_type_id, int[] buf)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Awrite writes an attribute, specified with attr_id. The attribute's memory datatype is
     * specified with mem_type_id. The entire attribute is written from buf to the file.
     * 
     * @param attr_id IN: Identifier of an attribute to write.
     * @param mem_type_id IN: Identifier of the attribute datatype (in memory).
     * @param buf IN: Data to be written.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - data is null.
     */
    public static native int H5Awrite(int attr_id, int mem_type_id, long[] buf)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Awrite writes an attribute, specified with attr_id. The attribute's memory datatype is
     * specified with mem_type_id. The entire attribute is written from buf to the file.
     * 
     * @param attr_id IN: Identifier of an attribute to write.
     * @param mem_type_id IN: Identifier of the attribute datatype (in memory).
     * @param buf IN: Data to be written.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - data is null.
     */
    public static native int H5Awrite(int attr_id, int mem_type_id, float[] buf)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Awrite writes an attribute, specified with attr_id. The attribute's memory datatype is
     * specified with mem_type_id. The entire attribute is written from buf to the file.
     * 
     * @param attr_id IN: Identifier of an attribute to write.
     * @param mem_type_id IN: Identifier of the attribute datatype (in memory).
     * @param buf IN: Data to be written.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - data is null.
     */
    public static native int H5Awrite(int attr_id, int mem_type_id, double[] buf)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5AwriteString writes a (partial) variable length String attribute, specified by its
     * identifier attr_id, from the application memory buffer buf into the file.
     * 
     * @param attr_id Identifier of the dataset read from.
     * @param mem_type_id Identifier of the memory datatype.
     * @param buf Buffer with data to be written to the file.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5AwriteString(int attr_id, int mem_type_id, String[] buf)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Acopy copies the content of one attribute to another.
     * 
     * @param src_aid the identifier of the source attribute
     * @param dst_aid the identifier of the destinaiton attribute
     */
    public static native int H5Acopy(int src_aid, int dst_aid) throws HDF5LibraryException;

    /**
     * H5Aread reads an attribute, specified with attr_id. The attribute's memory datatype is
     * specified with mem_type_id. The entire attribute is read into buf from the file.
     * 
     * @param attr_id IN: Identifier of an attribute to read.
     * @param mem_type_id IN: Identifier of the attribute datatype (in memory).
     * @param buf IN: Buffer for data to be read.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - data buffer is null.
     */
    public static native int H5Aread(int attr_id, int mem_type_id, byte[] buf)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Aread reads an attribute, specified with attr_id. The attribute's memory datatype is
     * specified with mem_type_id. The entire attribute is read into buf from the file.
     * 
     * @param attr_id IN: Identifier of an attribute to read.
     * @param mem_type_id IN: Identifier of the attribute datatype (in memory).
     * @param buf IN: Buffer for data to be read.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - data buffer is null.
     */
    public static native int H5Aread(int attr_id, int mem_type_id, short[] buf)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Aread reads an attribute, specified with attr_id. The attribute's memory datatype is
     * specified with mem_type_id. The entire attribute is read into buf from the file.
     * 
     * @param attr_id IN: Identifier of an attribute to read.
     * @param mem_type_id IN: Identifier of the attribute datatype (in memory).
     * @param buf IN: Buffer for data to be read.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - data buffer is null.
     */
    public static native int H5Aread(int attr_id, int mem_type_id, int[] buf)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Aread reads an attribute, specified with attr_id. The attribute's memory datatype is
     * specified with mem_type_id. The entire attribute is read into buf from the file.
     * 
     * @param attr_id IN: Identifier of an attribute to read.
     * @param mem_type_id IN: Identifier of the attribute datatype (in memory).
     * @param buf IN: Buffer for data to be read.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - data buffer is null.
     */
    public static native int H5Aread(int attr_id, int mem_type_id, long[] buf)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Aread reads an attribute, specified with attr_id. The attribute's memory datatype is
     * specified with mem_type_id. The entire attribute is read into buf from the file.
     * 
     * @param attr_id IN: Identifier of an attribute to read.
     * @param mem_type_id IN: Identifier of the attribute datatype (in memory).
     * @param buf IN: Buffer for data to be read.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - data buffer is null.
     */
    public static native int H5Aread(int attr_id, int mem_type_id, float[] buf)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Aread reads an attribute, specified with attr_id. The attribute's memory datatype is
     * specified with mem_type_id. The entire attribute is read into buf from the file.
     * 
     * @param attr_id IN: Identifier of an attribute to read.
     * @param mem_type_id IN: Identifier of the attribute datatype (in memory).
     * @param buf IN: Buffer for data to be read.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - data buffer is null.
     */
    public static native int H5Aread(int attr_id, int mem_type_id, double[] buf)
            throws HDF5LibraryException, NullPointerException;

    public static native int H5AreadVL(int attr_id, int mem_type_id, String[] buf)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Aget_space retrieves a copy of the dataspace for an attribute.
     * 
     * @param attr_id IN: Identifier of an attribute.
     * @return attribute dataspace identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Aget_space(int attr_id) throws HDF5LibraryException;

    /**
     * H5Aget_type retrieves a copy of the datatype for an attribute.
     * 
     * @param attr_id IN: Identifier of an attribute.
     * @return a datatype identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Aget_type(int attr_id) throws HDF5LibraryException;

    /**
     * H5Aget_name retrieves the name of an attribute specified by the identifier, attr_id.
     * 
     * @param attr_id IN: Identifier of the attribute.
     * @param buf_size IN: The size of the buffer to store the name in.
     * @param name OUT: Buffer to store name in.
     * @exception ArrayIndexOutOfBoundsException JNI error writing back array
     * @exception ArrayStoreException JNI error writing back array
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     * @exception IllegalArgumentException - bub_size <= 0.
     * @return the length of the attribute's name if successful.
     */
    public static native long H5Aget_name(int attr_id, long buf_size, String[] name)
            throws ArrayIndexOutOfBoundsException, ArrayStoreException, HDF5LibraryException,
            NullPointerException, IllegalArgumentException;

    /**
     * H5Aget_num_attrs returns the number of attributes attached to the object specified by its
     * identifier, loc_id.
     * 
     * @param loc_id IN: Identifier of a group, dataset, or named datatype.
     * @return the number of attributes if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Aget_num_attrs(int loc_id) throws HDF5LibraryException;

    /**
     * H5Adelete removes the attribute specified by its name, name, from a dataset, group, or named
     * datatype.
     * 
     * @param loc_id IN: Identifier of the dataset, group, or named datatype.
     * @param name IN: Name of the attribute to delete.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Adelete(int loc_id, String name) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5Aclose terminates access to the attribute specified by its identifier, attr_id.
     * 
     * @param attr_id IN: Attribute to release access to.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Aclose(int attr_id) throws HDF5LibraryException;

    // ////////////////////////////////////////////////////////////
    // //
    // H5D: Datasets Interface Functions //
    // //
    // ////////////////////////////////////////////////////////////

    /**
     * H5Dcreate creates a data set with a name, name, in the file or in the group specified by the
     * identifier loc_id.
     * 
     * @param loc_id Identifier of the file or group to create the dataset within.
     * @param name The name of the dataset to create.
     * @param type_id Identifier of the datatype to use when creating the dataset.
     * @param space_id Identifier of the dataspace to use when creating the dataset.
     * @param link_create_plist_id Identifier of the link creation property list.
     * @param dset_create_plist_id Identifier of the dataset creation property list.
     * @param dset_access_plist_id Identifier of the dataset access property list.
     * @return a dataset identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Dcreate(int loc_id, String name, int type_id, int space_id,
            int link_create_plist_id, int dset_create_plist_id, int dset_access_plist_id)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Dopen opens an existing dataset for access in the file or group specified in loc_id.
     * 
     * @param loc_id Identifier of the dataset to open or the file or group
     * @param name The name of the dataset to access.
     * @param access_plist_id Dataset access property list.
     * @return a dataset identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Dopen(int loc_id, String name, int access_plist_id)
            throws HDF5LibraryException, NullPointerException;

    public static native int H5Dchdir_ext(String dir_name) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Dgetdir_ext(String[] dir_name, int size)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Dget_space returns an identifier for a copy of the dataspace for a dataset.
     * 
     * @param dataset_id Identifier of the dataset to query.
     * @return a dataspace identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Dget_space(int dataset_id) throws HDF5LibraryException;

    /**
     * H5Dget_type returns an identifier for a copy of the datatype for a dataset.
     * 
     * @param dataset_id Identifier of the dataset to query.
     * @return a datatype identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Dget_type(int dataset_id) throws HDF5LibraryException;

    /**
     * H5Dget_create_plist returns an identifier for a copy of the dataset creation property list
     * for a dataset.
     * 
     * @param dataset_id Identifier of the dataset to query.
     * @return a dataset creation property list identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Dget_create_plist(int dataset_id) throws HDF5LibraryException;

    /**
     * H5Dread reads a (partial) dataset, specified by its identifier dataset_id, from the file into
     * the application memory buffer buf.
     * 
     * @param dataset_id Identifier of the dataset read from.
     * @param mem_type_id Identifier of the memory datatype.
     * @param mem_space_id Identifier of the memory dataspace.
     * @param file_space_id Identifier of the dataset's dataspace in the file.
     * @param xfer_plist_id Identifier of a transfer property list for this I/O operation.
     * @param buf Buffer to store data read from the file.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - data buffer is null.
     */
    public static native int H5Dread(int dataset_id, int mem_type_id, int mem_space_id,
            int file_space_id, int xfer_plist_id, byte[] buf) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5DreadVL(int dataset_id, int mem_type_id, int mem_space_id,
            int file_space_id, int xfer_plist_id, Object[] buf) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5DwriteString writes a (partial) variable length String dataset, specified by its identifier
     * dataset_id, from the application memory buffer buf into the file.
     * <p>
     * <i>contributed by Rosetta Biosoftware.</i>
     * 
     * @param dataset_id Identifier of the dataset read from.
     * @param mem_type_id Identifier of the memory datatype.
     * @param mem_space_id Identifier of the memory dataspace.
     * @param file_space_id Identifier of the dataset's dataspace in the file.
     * @param xfer_plist_id Identifier of a transfer property list for this I/O operation.
     * @param buf Buffer with data to be written to the file.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5DwriteString(int dataset_id, int mem_type_id, int mem_space_id,
            int file_space_id, int xfer_plist_id, String[] buf) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5Dwrite writes a (partial) dataset, specified by its identifier dataset_id, from the
     * application memory buffer buf into the file.
     * 
     * @param dataset_id Identifier of the dataset read from.
     * @param mem_type_id Identifier of the memory datatype.
     * @param mem_space_id Identifier of the memory dataspace.
     * @param file_space_id Identifier of the dataset's dataspace in the file.
     * @param xfer_plist_id Identifier of a transfer property list for this I/O operation.
     * @param buf Buffer with data to be written to the file.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Dwrite(int dataset_id, int mem_type_id, int mem_space_id,
            int file_space_id, int xfer_plist_id, byte[] buf) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5Dextend verifies that the dataset is at least of size size.
     * 
     * @param dataset_id Identifier of the dataset.
     * @param size Array containing the new magnitude of each dimension.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - size array is null.
     */
    public static native int H5Dextend(int dataset_id, byte[] size) throws HDF5LibraryException,
            NullPointerException;

    public static int H5Dextend(final int dataset_id, final long[] size) throws HDF5Exception,
            NullPointerException
    {
        final byte[] buf = HDFNativeData.longToByte(size);

        return H5Dextend(dataset_id, buf);
    }

    /**
     * H5Dset_extent sets the size of the dataset to <var>size</var>. Make sure that no important
     * are lost since this method will not check that the data dimensions are not larger than
     * <var>size</var>.
     * 
     * @param dataset_id Identifier of the dataset.
     * @param size Array containing the new magnitude of each dimension.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - size array is null.
     */
    public static native int H5Dset_extent(int dataset_id, byte[] size)
            throws HDF5LibraryException, NullPointerException;

    public static int H5Dset_extent(final int dataset_id, final long[] size) throws HDF5Exception,
            NullPointerException
    {
        final byte[] buf = HDFNativeData.longToByte(size);

        return H5Dset_extent(dataset_id, buf);
    }

    /**
     * H5Dclose ends access to a dataset specified by dataset_id and releases resources used by it.
     * 
     * @param dataset_id Identifier of the dataset to finish access to.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Dclose(int dataset_id) throws HDF5LibraryException;

    // following static native functions are missing from HDF5 RM version 1.0.1

    /**
     * H5Dget_storage_size returns the amount of storage that is required for the dataset.
     * 
     * @param dataset_id Identifier of the dataset in question
     * @return he amount of storage space allocated for the dataset.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native long H5Dget_storage_size(int dataset_id) throws HDF5LibraryException;

    /**
     * H5Dcopy copies the content of one dataset to another dataset.
     * 
     * @param src_did the identifier of the source dataset
     * @param dst_did the identifier of the destinaiton dataset
     */
    public static native int H5Dcopy(int src_did, int dst_did) throws HDF5LibraryException;

    /*
     *
     */
    public static native int H5Dvlen_get_buf_size(int dataset_id, int type_id, int space_id,
            int[] size) throws HDF5LibraryException;

    /**
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - buf is null.
     */
    public static native int H5Dvlen_reclaim(int type_id, int space_id, int xfer_plist_id,
            byte[] buf) throws HDF5LibraryException, NullPointerException;

    // ////////////////////////////////////////////////////////////
    // //
    // H5F: File Interface Functions //
    // //
    // ////////////////////////////////////////////////////////////

    /**
     * H5Fopen opens an existing file and is the primary function for accessing existing HDF5 files.
     * 
     * @param name Name of the file to access.
     * @param flags File access flags.
     * @param access_id Identifier for the file access properties list.
     * @return a file identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Fopen(String name, int flags, int access_id)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Fcreate is the primary function for creating HDF5 files.
     * 
     * @param name Name of the file to access.
     * @param flags File access flags. Possible values include:
     *            <UL>
     *            <LI>H5F_ACC_RDWR Allow read and write access to file.</LI>
     *            <LI>H5F_ACC_RDONLY Allow read-only access to file.</LI>
     *            <LI>H5F_ACC_TRUNC Truncate file, if it already exists, erasing all data previously
     *            stored in the file.</LI>
     *            <LI>H5F_ACC_EXCL Fail if file already exists.</LI>
     *            <LI>H5F_ACC_DEBUG Print debug information.</LI>
     *            <LI>H5P_DEFAULT Apply default file access and creation properties.</LI>
     *            </UL>
     * @param create_id File creation property list identifier, used when modifying default file
     *            meta-data. Use H5P_DEFAULT for default access properties.
     * @param access_id File access property list identifier. If parallel file access is desired,
     *            this is a collective call according to the communicator stored in the access_id
     *            (not supported in Java). Use H5P_DEFAULT for default access properties.
     * @return a file identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Fcreate(String name, int flags, int create_id, int access_id)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Fflush causes all buffers associated with a file or object to be immediately flushed
     * (written) to disk without removing the data from the (memory) cache.
     * <P>
     * After this call completes, the file (or object) is in a consistent state and all data written
     * to date is assured to be permanent.
     * 
     * @param object_id Identifier of object used to identify the file. <b>object_id</b> can be any
     *            object associated with the file, including the file itself, a dataset, a group, an
     *            attribute, or a named data type.
     * @param scope specifies the scope of the flushing action, in the case that the HDF-5 file is
     *            not a single physical file.
     *            <P>
     *            Valid values are:
     *            <UL>
     *            <LI>H5F_SCOPE_GLOBAL Flushes the entire virtual file.</LI>
     *            <LI>H5F_SCOPE_LOCAL Flushes only the specified file.</LI>
     *            </UL>
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Fflush(int object_id, int scope) throws HDF5LibraryException;

    /**
     * H5Fis_hdf5 determines whether a file is in the HDF5 format.
     * 
     * @param name File name to check format.
     * @return true if is HDF-5, false if not.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native boolean H5Fis_hdf5(String name) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5Fget_create_plist returns a file creation property list identifier identifying the creation
     * properties used to create this file.
     * 
     * @param file_id Identifier of the file to get creation property list
     * @return a file creation property list identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Fget_create_plist(int file_id) throws HDF5LibraryException;

    /**
     * H5Fget_access_plist returns the file access property list identifier of the specified file.
     * 
     * @param file_id Identifier of file to get access property list of
     * @return a file access property list identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Fget_access_plist(int file_id) throws HDF5LibraryException;

    /**
     * H5Fclose terminates access to an HDF5 file.
     * 
     * @param file_id Identifier of a file to terminate access to.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Fclose(int file_id) throws HDF5LibraryException;

    /**
     * H5Fmount mounts the file specified by child_id onto the group specified by loc_id and name
     * using the mount properties plist_id.
     * 
     * @param loc_id The identifier for the group onto which the file specified by child_id is to be
     *            mounted.
     * @param name The name of the group onto which the file specified by child_id is to be mounted.
     * @param child_id The identifier of the file to be mounted.
     * @param plist_id The identifier of the property list to be used.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Fmount(int loc_id, String name, int child_id, int plist_id)
            throws HDF5LibraryException, NullPointerException;

    /**
     * Given a mount point, H5Funmount dissassociates the mount point's file from the file mounted
     * there.
     * 
     * @param loc_id The identifier for the location at which the specified file is to be unmounted.
     * @param name The name of the file to be unmounted.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Funmount(int loc_id, String name) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5Freopen reopens an HDF5 file.
     * 
     * @param file_id Identifier of a file to terminate and reopen access to.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @return a new file identifier if successful
     */
    public static native int H5Freopen(int file_id) throws HDF5LibraryException;

    // ////////////////////////////////////////////////////////////
    // //
    // H5G: Group Interface Functions //
    // //
    // ////////////////////////////////////////////////////////////

    /**
     * H5Gcreate creates a new group with the specified name at the specified location, loc_id.
     * 
     * @param loc_id The file or group identifier.
     * @param name The absolute or relative name of the new group.
     * @param link_create_plist_id Property list for link creation.
     * @param group_create_plist_id Property list for group creation.
     * @param group_access_plist_id Property list for group access.
     * @return a valid group identifier for the open group if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Gcreate(int loc_id, String name, int link_create_plist_id,
            int group_create_plist_id, int group_access_plist_id) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5Gopen opens an existing group with the specified name at the specified location, loc_id.
     * 
     * @param loc_id File or group identifier within which group is to be open.
     * @param name Name of group to open.
     * @param access_plist_id Group access property list identifier (H5P_DEFAULT for the default
     *            property list).
     * @return a valid group identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Gopen(int loc_id, String name, int access_plist_id)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Gclose releases resources used by a group which was opened by a call to H5Gcreate() or
     * H5Gopen().
     * 
     * @param group_id Group identifier to release.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Gclose(int group_id) throws HDF5LibraryException;

    /**
     * H5Glink creates a new name for an already existing object.
     * 
     * @param loc_id File, group, dataset, or datatype identifier.
     * @param link_type Link type. Possible values are:
     *            <UL>
     *            <LI>H5G_LINK_HARD</LI>
     *            <LI>H5G_LINK_SOFT.</LI>
     *            </UL>
     * @param current_name A name of the existing object if link is a hard link. Can be anything for
     *            the soft link.
     * @param new_name New name for the object.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - current_name or name is null.
     */
    @Deprecated
    public static native int H5Glink(int loc_id, int link_type, String current_name, String new_name)
            throws HDF5LibraryException, NullPointerException;

    @Deprecated
    public static native int H5Glink2(int curr_loc_id, String current_name, int link_type,
            int new_loc_id, String new_name) throws HDF5LibraryException, NullPointerException;

    /**
     * H5Gunlink removes an association between a name and an object.
     * 
     * @param loc_id Identifier of the file containing the object.
     * @param name Name of the object to unlink.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Gunlink(int loc_id, String name) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5Gmove renames an object within an HDF5 file. The original name, src, is unlinked from the
     * group graph and the new name, dst, is inserted as an atomic operation. Both names are
     * interpreted relative to loc_id, which is either a file or a group identifier.
     * 
     * @param loc_id File or group identifier.
     * @param src Object's original name.
     * @param dst Object's new name.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - src or dst is null.
     */
    public static native int H5Gmove(int loc_id, String src, String dst)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Gget_linkval returns size characters of the link value through the value argument if loc_id
     * (a file or group identifier) and name specify a symbolic link.
     * 
     * @param loc_id IN: Identifier of the file, group, dataset, or datatype.
     * @param name IN: Name of the object whose link value is to be checked.
     * @param size IN: Maximum number of characters of value to be returned.
     * @param value OUT: Link value.
     * @return a non-negative value, with the link value in value, if successful.
     * @exception ArrayIndexOutOfBoundsException Copy back failed
     * @exception ArrayStoreException Copy back failed
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     * @exception IllegalArgumentException - size is invalid
     */
    public static native int H5Gget_linkval(int loc_id, String name, int size, String[] value)
            throws ArrayIndexOutOfBoundsException, ArrayStoreException, HDF5LibraryException,
            NullPointerException, IllegalArgumentException;

    /**
     * H5Gset_comment sets the comment for the the object name to comment. Any previously existing
     * comment is overwritten.
     * 
     * @param loc_id IN: Identifier of the file, group, dataset, or datatype.
     * @param name IN: Name of the object whose comment is to be set or reset.
     * @param comment IN: The new comment.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name or comment is null.
     */
    public static native int H5Gset_comment(int loc_id, String name, String comment)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Gget_comment retrieves the comment for the the object name. The comment is returned in the
     * buffer comment.
     * 
     * @param loc_id IN: Identifier of the file, group, dataset, or datatype.
     * @param name IN: Name of the object whose comment is to be set or reset.
     * @param bufsize IN: Anticipated size of the buffer required to hold comment.
     * @param comment OUT: The comment.
     * @return the number of characters in the comment, counting the null terminator, if successful
     * @exception ArrayIndexOutOfBoundsException - JNI error writing back data
     * @exception ArrayStoreException - JNI error writing back data
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     * @exception IllegalArgumentException - size < 1, comment is invalid.
     */
    public static native int H5Gget_comment(int loc_id, String name, int bufsize, String[] comment)
            throws ArrayIndexOutOfBoundsException, ArrayStoreException, HDF5LibraryException,
            NullPointerException, IllegalArgumentException;

    // ////////////////////////////////////////////////////////////
    // //
    // H5I: Identifier Interface Functions //
    // //
    // ////////////////////////////////////////////////////////////

    /**
     * H5Iget_type retrieves the type of the object identified by obj_id.
     * 
     * @param obj_id IN: Object identifier whose type is to be determined.
     * @return the object type if successful; otherwise H5I_BADID.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Iget_type(int obj_id) throws HDF5LibraryException;

    // ////////////////////////////////////////////////////////////
    // //
    // H5P: Property List Interface Functions //
    // //
    // ////////////////////////////////////////////////////////////

    /**
     * H5Pcreate creates a new property as an instance of some property list class.
     * 
     * @param type IN: The type of property list to create.
     * @return a property list identifier (plist) if successful; otherwise Fail (-1).
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pcreate(int type) throws HDF5LibraryException;

    /**
     * H5Pclose terminates access to a property list.
     * 
     * @param plist IN: Identifier of the property list to terminate access to.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pclose(int plist) throws HDF5LibraryException;

    /**
     * H5Pget_class returns the property list class for the property list identified by the plist
     * parameter.
     * 
     * @param plist IN: Identifier of property list to query.
     * @return a property list class if successful. Otherwise returns H5P_NO_CLASS (-1).
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pget_class(int plist) throws HDF5LibraryException;

    /**
     * H5Pcopy copies an existing property list to create a new property list.
     * 
     * @param plist IN: Identifier of property list to duplicate.
     * @return a property list identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pcopy(int plist) throws HDF5LibraryException;

    /**
     * H5Pget_version retrieves the version information of various objects for a file creation
     * property list.
     * 
     * @param plist IN: Identifier of the file creation property list.
     * @param version_info OUT: version information.
     * 
     *            <pre>
     * 
     *            version_info[0] = boot // boot block version number version_info[1] = freelist //
     *            global freelist version version_info[2] = stab // symbol tabl version number
     *            version_info[3] = shhdr // hared object header version
     * 
     * </pre>
     * @return a non-negative value, with the values of version_info initialized, if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - version_info is null.
     * @exception IllegalArgumentException - version_info is illegal.
     */
    public static native int H5Pget_version(int plist, int[] version_info)
            throws HDF5LibraryException, NullPointerException, IllegalArgumentException;

    /**
     * H5Pset_userblock sets the user block size of a file creation property list.
     * 
     * @param plist IN: Identifier of property list to modify.
     * @param size IN: Size of the user-block in bytes.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pset_userblock(int plist, long size) throws HDF5LibraryException;

    /**
     * H5Pget_userblock retrieves the size of a user block in a file creation property list.
     * 
     * @param plist IN: Identifier for property list to query.
     * @param size OUT: Pointer to location to return user-block size.
     * @return a non-negative value and the size of the user block; if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - size is null.
     */
    public static native int H5Pget_userblock(int plist, long[] size) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5Pset_small_data_block_size reserves blocks of size bytes for the contiguous storage of the
     * raw data portion of small datasets.
     * 
     * @param plist IN: Identifier of property list to modify.
     * @param size IN: Size of the blocks in bytes.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pset_small_data_block_size(int plist, long size)
            throws HDF5LibraryException;

    /**
     * H5Pget_small_data_block_size retrieves the size of a block of small data in a file creation
     * property list.
     * 
     * @param plist IN: Identifier for property list to query.
     * @param size OUT: Pointer to location to return block size.
     * @return a non-negative value and the size of the user block; if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - size is null.
     */
    public static native int H5Pget_small_data_block_size(int plist, long[] size)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Pset_sizes sets the byte size of the offsets and lengths used to address objects in an HDF5
     * file.
     * 
     * @param plist IN: Identifier of property list to modify.
     * @param sizeof_addr IN: Size of an object offset in bytes.
     * @param sizeof_size IN: Size of an object length in bytes.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pset_sizes(int plist, int sizeof_addr, int sizeof_size)
            throws HDF5LibraryException;

    /**
     * H5Pget_sizes retrieves the size of the offsets and lengths used in an HDF5 file. This
     * function is only valid for file creation property lists.
     * 
     * @param plist IN: Identifier of property list to query.
     * @param size OUT: the size of the offsets and length.
     * 
     *            <pre>
     * 
     *            size[0] = sizeof_addr // offset size in bytes size[1] = sizeof_size // length size
     *            in bytes
     * 
     * </pre>
     * @return a non-negative value with the sizes initialized; if successful;
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - size is null.
     * @exception IllegalArgumentException - size is invalid.
     */
    public static native int H5Pget_sizes(int plist, int[] size) throws HDF5LibraryException,
            NullPointerException, IllegalArgumentException;

    /**
     * H5Pset_sym_k sets the size of parameters used to control the symbol table nodes.
     * 
     * @param plist IN: Identifier for property list to query.
     * @param ik IN: Symbol table tree rank.
     * @param lk IN: Symbol table node size.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pset_sym_k(int plist, int ik, int lk) throws HDF5LibraryException;

    /**
     * H5Pget_sym_k retrieves the size of the symbol table B-tree 1/2 rank and the symbol table leaf
     * node 1/2 size.
     * 
     * @param plist IN: Property list to query.
     * @param size OUT: the symbol table's B-tree 1/2 rank and leaf node 1/2 size.
     * 
     *            <pre>
     * 
     *            size[0] = ik // the symbol table's B-tree 1/2 rank size[1] = lk // leaf node 1/2
     *            size
     * 
     * </pre>
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - size is null.
     * @exception IllegalArgumentException - size is invalid.
     */
    public static native int H5Pget_sym_k(int plist, int[] size) throws HDF5LibraryException,
            NullPointerException, IllegalArgumentException;

    /**
     * H5Pset_istore_k sets the size of the parameter used to control the B-trees for indexing
     * chunked datasets.
     * 
     * @param plist IN: Identifier of property list to query.
     * @param ik IN: 1/2 rank of chunked storage B-tree.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pset_istore_k(int plist, int ik) throws HDF5LibraryException;

    /**
     * H5Pget_istore_k queries the 1/2 rank of an indexed storage B-tree.
     * 
     * @param plist IN: Identifier of property list to query.
     * @param ik OUT: Pointer to location to return the chunked storage B-tree 1/2 rank.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - ik array is null.
     */
    public static native int H5Pget_istore_k(int plist, int[] ik) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5Pset_layout sets the type of storage used store the raw data for a dataset.
     * 
     * @param plist IN: Identifier of property list to query.
     * @param layout IN: Type of storage layout for raw data.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pset_layout(int plist, int layout) throws HDF5LibraryException;

    /**
     * H5Pget_layout returns the layout of the raw data for a dataset.
     * 
     * @param plist IN: Identifier for property list to query.
     * @return the layout type of a dataset creation property list if successful. Otherwise returns
     *         H5D_LAYOUT_ERROR (-1).
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pget_layout(int plist) throws HDF5LibraryException;

    /**
     * H5Pset_chunk sets the size of the chunks used to store a chunked layout dataset.
     * 
     * @param plist IN: Identifier for property list to query.
     * @param ndims IN: The number of dimensions of each chunk.
     * @param dim IN: An array containing the size of each chunk.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - dims array is null.
     * @exception IllegalArgumentException - dims <=0
     */
    public static native int H5Pset_chunk(int plist, int ndims, byte[] dim)
            throws HDF5LibraryException, NullPointerException, IllegalArgumentException;

    public static int H5Pset_chunk(final int plist, final int ndims, final long[] dim)
            throws HDF5Exception, NullPointerException, IllegalArgumentException
    {
        if (dim == null)
        {
            return -1;
        }

        final byte[] thedims = HDFNativeData.longToByte(dim);

        return H5Pset_chunk(plist, ndims, thedims);
    }

    /**
     * H5Pget_chunk retrieves the size of chunks for the raw data of a chunked layout dataset.
     * 
     * @param plist IN: Identifier of property list to query.
     * @param max_ndims IN: Size of the dims array.
     * @param dims OUT: Array to store the chunk dimensions.
     * @return chunk dimensionality successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - dims array is null.
     * @exception IllegalArgumentException - max_ndims <=0
     */
    public static native int H5Pget_chunk(int plist, int max_ndims, long[] dims)
            throws HDF5LibraryException, NullPointerException, IllegalArgumentException;

    /**
     * H5Pset_alignment sets the alignment properties of a file access property list so that any
     * file object >= THRESHOLD bytes will be aligned on an address which is a multiple of
     * ALIGNMENT.
     * 
     * @param plist IN: Identifier for a file access property list.
     * @param threshold IN: Threshold value.
     * @param alignment IN: Alignment value.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pset_alignment(int plist, long threshold, long alignment)
            throws HDF5LibraryException;

    /**
     * H5Pget_alignment retrieves the current settings for alignment properties from a file access
     * property list.
     * 
     * @param plist IN: Identifier of a file access property list.
     * @param alignment OUT: threshold value and alignment value.
     * 
     *            <pre>
     * 
     *            alignment[0] = threshold // threshold value alignment[1] = alignment // alignment
     *            value
     * 
     * </pre>
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - aligment array is null.
     * @exception IllegalArgumentException - aligment array is invalid.
     */
    public static native int H5Pget_alignment(int plist, long[] alignment)
            throws HDF5LibraryException, NullPointerException, IllegalArgumentException;

    /**
     * H5Pset_external adds an external file to the list of external files.
     * 
     * @param plist IN: Identifier of a dataset creation property list.
     * @param name IN: Name of an external file.
     * @param offset IN: Offset, in bytes, from the beginning of the file to the location in the
     *            file where the data starts.
     * @param size IN: Number of bytes reserved in the file for the data.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Pset_external(int plist, String name, long offset, long size)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Pget_external_count returns the number of external files for the specified dataset.
     * 
     * @param plist IN: Identifier of a dataset creation property list.
     * @return the number of external files if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pget_external_count(int plist) throws HDF5LibraryException;

    /**
     * H5Pget_external returns information about an external file.
     * 
     * @param plist IN: Identifier of a dataset creation property list.
     * @param idx IN: External file index.
     * @param name_size IN: Maximum length of name array.
     * @param name OUT: Name of the external file.
     * @param size OUT: the offset value and the size of the external file data.
     * 
     *            <pre>
     * 
     *            size[0] = offset // a location to return an offset value size[1] = size // a
     *            location to return the size of // the external file data.
     * 
     * </pre>
     * @return a non-negative value if successful
     * @exception ArrayIndexOutOfBoundsException Fatal error on Copyback
     * @exception ArrayStoreException Fatal error on Copyback
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name or size is null.
     * @exception IllegalArgumentException - name_size <= 0 .
     */
    public static native int H5Pget_external(int plist, int idx, int name_size, String[] name,
            long[] size) throws ArrayIndexOutOfBoundsException, ArrayStoreException,
            HDF5LibraryException, NullPointerException, IllegalArgumentException;

    /**
     * H5Pset_fill_value sets the fill value for a dataset creation property list.
     * 
     * @param plist_id IN: Property list identifier.
     * @param type_id IN: The datatype identifier of value.
     * @param value IN: The fill value.
     * @return a non-negative value if successful
     * @exception HDF5Exception - Error converting data array
     */
    public static native int H5Pset_fill_value(int plist_id, int type_id, byte[] value)
            throws HDF5Exception;

    /**
     * H5Pget_fill_value queries the fill value property of a dataset creation property list. <b>NOT
     * IMPLEMENTED YET</B>
     * 
     * @param plist_id IN: Property list identifier.
     * @param type_id IN: The datatype identifier of value.
     * @param value IN: The fill value.
     * @return a non-negative value if successful
     */
    public static native int H5Pget_fill_value(int plist_id, int type_id, byte[] value)
            throws HDF5Exception;

    /**
     * H5Pset_filter adds the specified filter and corresponding properties to the end of an output
     * filter pipeline.
     * 
     * @param plist IN: Property list identifier.
     * @param filter IN: Filter to be added to the pipeline.
     * @param flags IN: Bit vector specifying certain general properties of the filter.
     * @param cd_nelmts IN: Number of elements in cd_values
     * @param cd_values IN: Auxiliary data for the filter.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pset_filter(int plist, int filter, int flags, int cd_nelmts,
            int[] cd_values) throws HDF5LibraryException;

    /**
     * H5Pget_nfilters returns the number of filters defined in the filter pipeline associated with
     * the property list plist.
     * 
     * @param plist IN: Property list identifier.
     * @return the number of filters in the pipeline if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pget_nfilters(int plist) throws HDF5LibraryException;

    /**
     * H5Pget_filter returns information about a filter, specified by its filter number, in a filter
     * pipeline, specified by the property list with which it is associated.
     * 
     * @param plist IN: Property list identifier.
     * @param filter_number IN: Sequence number within the filter pipeline of the filter for which
     *            information is sought.
     * @param flags OUT: Bit vector specifying certain general properties of the filter.
     * @param cd_nelmts IN/OUT: Number of elements in cd_values
     * @param cd_values OUT: Auxiliary data for the filter.
     * @param namelen IN: Anticipated number of characters in name.
     * @param name OUT: Name of the filter.
     * @return the filter identification number if successful. Otherwise returns H5Z_FILTER_ERROR
     *         (-1).
     * @exception ArrayIndexOutOfBoundsException Fatal error on Copyback
     * @exception ArrayStoreException Fatal error on Copyback
     * @exception NullPointerException - name or an array is null.
     */
    public static native int H5Pget_filter(int plist, int filter_number, int[] flags,
            int[] cd_nelmts, int[] cd_values, int namelen, String[] name)
            throws ArrayIndexOutOfBoundsException, ArrayStoreException, HDF5LibraryException,
            NullPointerException;

    // REMOVED in HDF5.1.4
    //
    // /**
    // * H5Pget_driver returns the identifier of the low-level
    // * file driver.
    // * <p>
    // * Valid identifiers are:
    // * <UL>
    // * <LI>
    // * H5F_LOW_STDIO (0)
    // * </LI>
    // * <LI>
    // * H5F_LOW_SEC2 (1)
    // * </LI>
    // * <LI>
    // * H5F_LOW_MPIO (2)
    // * </LI>
    // * <LI>
    // * H5F_LOW_CORE (3)
    // * </LI>
    // * <LI>
    // * H5F_LOW_SPLIT (4)
    // * </LI>
    // * <LI>
    // * H5F_LOW_FAMILY (5)
    // * </LI>
    // * </UL>
    // *
    // * @param plist IN: Identifier of a file access property list.
    // *
    // * @return a low-level driver identifier if successful. Otherwise returns
    // * H5F_LOW_ERROR (-1).
    // *
    // * @exception HDF5LibraryException - Error from the HDF-5 Library.
    // **/
    // public static native int H5Pget_driver(int plist)
    // throws HDF5LibraryException;
    //
    // /**
    // * H5Pset_stdio sets the low level file driver to use the
    // * functions declared in the stdio.h file: fopen(), fseek()
    // * or fseek64(), fread(), fwrite(), and fclose().
    // *
    // * @param plist IN: Identifier of a file access property list.
    // *
    // * @return a non-negative value if successful
    // *
    // **/
    // public static native int H5Pset_stdio(int plist)
    // throws HDF5LibraryException;
    //
    // /**
    // * H5Pget_stdio checks to determine whether the file access
    // * property list is set to the stdio driver.
    // *
    // * @param plist IN: Identifier of a file access property list.
    // * @return true if the file access property list is set to
    // * the stdio driver. Otherwise returns a negative value.
    // *
    // **/
    // public static native boolean H5Pget_stdio(int plist);
    //
    // /**
    // * H5Pset_sec2 sets the low-level file driver to use the
    // * functions declared in the unistd.h file: open(), lseek()
    // * or lseek64(), read(), write(), and close().
    // *
    // * @param plist IN: Identifier of a file access property list.
    // * @return a non-negative value if successful
    // *
    // * @exception HDF5LibraryException - Error from the HDF-5 Library.
    // **/
    // public static native int H5Pset_sec2(int plist)
    // throws HDF5LibraryException;
    //
    //
    // /**
    // * H5Pget_sec2 checks to determine whether the file access
    // * property list is set to the sec2 driver.
    // *
    // * @param plist IN: Identifier of a file access property list.
    // * @return true if the file access property list is set to
    // * the sec2 driver. Otherwise returns a negative value.
    // *
    // * @exception HDF5LibraryException - Error from the HDF-5 Library.
    // **/
    // public static native boolean H5Pget_sec2(int plist)
    // throws HDF5LibraryException;
    //
    // /**
    // * H5Pset_core sets the low-level file driver to use malloc() and
    // * free().
    // *
    // * @param plist IN: Identifier of a file access property list.
    // * @param increment IN: File block size in bytes.
    // *
    // * @return a non-negative value if successful
    // *
    // * @exception HDF5LibraryException - Error from the HDF-5 Library.
    // **/
    // public static native int H5Pset_core(int plist, int increment)
    // throws HDF5LibraryException;
    //
    //
    // /**
    // * H5Pget_core checks to determine whether the file access
    // * property list is set to the core driver.
    // *
    // * @param plist IN: Identifier of the file access property list.
    // * @param increment OUT: A location to return the file block size
    // * @return true if the file access property list is set to
    // * the core driver.
    // *
    // * @exception HDF5LibraryException - Error from the HDF-5 Library.
    // **/
    // public static native boolean H5Pget_core(int plist,
    // int[] increment)
    // throws HDF5LibraryException;
    //
    //
    // /**
    // * H5Pset_split sets the low-level driver to split meta data
    // * from raw data, storing meta data in one file and raw data
    // * in another file.
    // *
    // * @param plist IN: Identifier of the file access property list.
    // * @param meta_ext IN: Name of the extension for the metafile
    // * filename. Recommended default value: <i>.meta</i>.
    // * @param meta_plist IN: Identifier of the meta file access
    // * property list.
    // * @param raw_ext IN: Name extension for the raw file filename.
    // * Recommended default value: <i>.raw</i>.
    // * @param raw_plist IN: Identifier of the raw file access
    // * property list.
    // * @return a non-negative value if successful
    // *
    // * @exception HDF5LibraryException - Error from the HDF-5 Library.
    // * @exception NullPointerException - a string is null.
    // **/
    // public static native int H5Pset_split(int plist, String meta_ext,
    // int meta_plist, String raw_ext, int raw_plist)
    // throws HDF5LibraryException,
    // NullPointerException;
    //
    //
    // /**
    // * H5Pget_split checks to determine whether the file access
    // * property list is set to the split driver.
    // *
    // * @param plist IN: Identifier of the file access property list.
    // * @param meta_ext_size IN: Number of characters of the
    // * meta file extension to be copied to the meta_ext buffer.
    // * @param meta_ext IN: Meta file extension.
    // * @param *meta_properties OUT: A copy of the meta file
    // * access property list.
    // * @param raw_ext_size IN: Number of characters of the
    // * raw file extension to be copied to the raw_ext buffer.
    // * @param raw_ext OUT: Raw file extension.
    // * @param *raw_properties OUT: A copy of the raw file
    // * access property list.
    // *
    // * @return true if the file access property list is set to
    // * the split driver.
    // *
    // * @exception ArrayIndexOutOfBoundsException JNI error
    // * writing back array
    // * @exception ArrayStoreException JNI error writing back array
    // * @exception HDF5LibraryException - Error from the HDF-5 Library.
    // * @exception NullPointerException - a string or array is null.
    // **/
    // public static native boolean H5Pget_split(int plist,
    // int meta_ext_size, String[] meta_ext,
    // int[] meta_properties, int raw_ext_size,
    // String[] raw_ext, int[] raw_properties)
    // throws ArrayIndexOutOfBoundsException,
    // ArrayStoreException,
    // HDF5LibraryException,
    // NullPointerException;
    //
    // /**
    // * H5Pset_family sets the file access properties to use the
    // * family driver; any previously defined driver properties
    // * are erased from the property list.
    // *
    // * @param plist IN: Identifier of the file access property list.
    // * @param memb_size IN: Logical size, in bytes, of each
    // * family member.
    // * @param memb_plist IN: Identifier of the file access
    // * property list for each member of the family.
    // * @return a non-negative value if successful
    // *
    // * @exception HDF5LibraryException - Error from the HDF-5 Library.
    // **/
    // public static native int H5Pset_family(int plist, long memb_size,
    // int memb_plist)
    // throws HDF5LibraryException;
    //
    //
    // /**
    // * H5Pget_family checks to determine whether the file access
    // * property list is set to the family driver.
    // *
    // * @param plist IN: Identifier of the file access property list.
    // * @param memb_size OUT: Logical size, in bytes, of each
    // * family member.
    // * @param *memb_plist OUT: Identifier of the file access
    // * property list for each member of the family.
    // *
    // * @return a non-negative value if the file access property
    // * list is set to the family driver.
    // *
    // * @exception HDF5LibraryException - Error from the HDF-5 Library.
    // * @exception NullPointerException - an array is null.
    // **/
    // public static native int H5Pget_family(int tid, long[] memb_size,
    // int[] memb_plist)
    // throws HDF5LibraryException, NullPointerException;

    /**
     * H5Pset_cache sets the number of elements (objects) in the meta data cache and the total
     * number of bytes in the raw data chunk cache.
     * 
     * @param plist IN: Identifier of the file access property list.
     * @param mdc_nelmts IN: Number of elements (objects) in the meta data cache.
     * @param rdcc_nbytes IN: Total size of the raw data chunk cache, in bytes.
     * @param rdcc_w0 IN: Preemption policy.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pset_cache(int plist, int mdc_nelmts, int rdcc_nelmts,
            int rdcc_nbytes, double rdcc_w0) throws HDF5LibraryException;

    /**
     * Retrieves the maximum possible number of elements in the meta data cache and the maximum
     * possible number of bytes and the RDCC_W0 value in the raw data chunk cache.
     * 
     * @param plist IN: Identifier of the file access property list.
     * @param mdc_nelmts IN/OUT: Number of elements (objects) in the meta data cache.
     * @param rdcc_nbytes IN/OUT: Total size of the raw data chunk cache, in bytes.
     * @param rdcc_w0 IN/OUT: Preemption policy.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - an array is null.
     */
    public static native int H5Pget_cache(int plist, int[] mdc_nelmts, int[] rdcc_nelmts,
            int[] rdcc_nbytes, double[] rdcc_w0) throws HDF5LibraryException, NullPointerException;

    /**
     * H5Pset_buffer sets type conversion and background buffers. status to TRUE or FALSE. Given a
     * dataset transfer property list, H5Pset_buffer sets the maximum size for the type conversion
     * buffer and background buffer and optionally supplies pointers to application-allocated
     * buffers. If the buffer size is smaller than the entire amount of data being transferred
     * between the application and the file, and a type conversion buffer or background buffer is
     * required, then strip mining will be used. Note that there are minimum size requirements for
     * the buffer. Strip mining can only break the data up along the first dimension, so the buffer
     * must be large enough to accommodate a complete slice that encompasses all of the remaining
     * dimensions. For example, when strip mining a 100x200x300 hyperslab of a simple data space,
     * the buffer must be large enough to hold 1x200x300 data elements. When strip mining a
     * 100x200x300x150 hyperslab of a simple data space, the buffer must be large enough to hold
     * 1x200x300x150 data elements. If tconv and/or bkg are null pointers, then buffers will be
     * allocated and freed during the data transfer.
     * 
     * @param plist Identifier for the dataset transfer property list.
     * @param size Size, in bytes, of the type conversion and background buffers.
     * @param tconv byte array of application-allocated type conversion buffer.
     * @param bkg byte array of application-allocated background buffer.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception IllegalArgumentException - plist is invalid.
     */
    public static native int H5Pset_buffer(int plist, int size, byte[] tconv, byte[] bkg)
            throws HDF5LibraryException, IllegalArgumentException;

    /**
     * HH5Pget_buffer gets type conversion and background buffers. Returns buffer size, in bytes, if
     * successful; otherwise 0 on failure.
     * 
     * @param plist Identifier for the dataset transfer property list.
     * @param tconv byte array of application-allocated type conversion buffer.
     * @param bkg byte array of application-allocated background buffer.
     * @return buffer size, in bytes, if successful; otherwise 0 on failure
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception IllegalArgumentException - plist is invalid.
     */
    public static native int H5Pget_buffer(int plist, byte[] tconv, byte[] bkg)
            throws HDF5LibraryException, IllegalArgumentException;

    /**
     * H5Pset_preserve sets the dataset transfer property list status to TRUE or FALSE.
     * 
     * @param plist IN: Identifier for the dataset transfer property list.
     * @param status IN: Status of for the dataset transfer property list.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception IllegalArgumentException - plist is invalid.
     */
    public static native int H5Pset_preserve(int plist, boolean status)
            throws HDF5LibraryException, IllegalArgumentException;

    /**
     * H5Pget_preserve checks the status of the dataset transfer property list.
     * 
     * @param plist IN: Identifier for the dataset transfer property list.
     * @return TRUE or FALSE if successful; otherwise returns a negative value
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pget_preserve(int plist) throws HDF5LibraryException;

    /**
     * H5Pset_deflate sets the compression method for a dataset.
     * 
     * @param plist IN: Identifier for the dataset creation property list.
     * @param level IN: Compression level.
     * @return non-negative if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pset_deflate(int plist, int level) throws HDF5LibraryException;

    /**
     * H5Pset_nbit sets the compression method for a dataset to n-bits.
     * <p>
     * Keeps only n-bits from an integer or float value.
     * 
     * @param plist IN: Identifier for the dataset creation property list.
     * @return non-negative if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pset_nbit(int plist) throws HDF5LibraryException;

    /**
     * H5Pset_scaleoffset sets the compression method for a dataset to scale_offset.
     * <p>
     * Generally speaking, Scale-Offset compression performs a scale and/or offset operation on each
     * data value and truncates the resulting value to a minimum number of bits (MinBits) before
     * storing it. The current Scale-Offset filter supports integer and floating-point datatype.
     * 
     * @param plist IN: Identifier for the dataset creation property list.
     * @param scale_type IN: One of {@link HDF5Constants#H5Z_SO_INT},
     *            {@link HDF5Constants#H5Z_SO_FLOAT_DSCALE} or
     *            {@link HDF5Constants#H5Z_SO_FLOAT_ESCALE}. Note that
     *            {@link HDF5Constants#H5Z_SO_FLOAT_ESCALE} is not implemented as of HDF5 1.8.2.
     * @return non-negative if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pset_scaleoffset(int plist, int scale_type, int scale_factor)
            throws HDF5LibraryException;

    /**
     * H5Pset_gc_references Sets the flag for garbage collecting references for the file. Default
     * value for garbage collecting references is off.
     * 
     * @param fapl_id IN File access property list
     * @param gc_ref IN set GC on (true) or off (false)
     * @return non-negative if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pset_gc_references(int fapl_id, boolean gc_ref)
            throws HDF5LibraryException;

    /**
     * H5Pget_gc_references Returns the current setting for the garbage collection refernces
     * property from a file access property list.
     * <p>
     * Note: this routine changed name with HDF5.1.2.2. If using an earlier version, use 'configure
     * --enable-hdf5_1_2_1' so this routine will link to the old name.
     * 
     * @param fapl_id IN File access property list
     * @param gc_ref OUT GC is on (true) or off (false)
     * @return non-negative if succeed
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - array is null.
     */
    public static native int H5Pget_gc_references(int fapl_id, boolean[] gc_ref)
            throws HDF5LibraryException, NullPointerException;

    /*
     * Earlier versions of the HDF5 library had a different name. This is included as an alias.
     */
    public static int H5Pget_gc_reference(final int fapl_id, final boolean[] gc_ref)
            throws HDF5LibraryException, NullPointerException
    {
        return H5Pget_gc_references(fapl_id, gc_ref);
    }

    /**
     * H5Pset_btree_ratio Sets B-tree split ratios for a dataset transfer property list. The split
     * ratios determine what percent of children go in the first node when a node splits.
     * 
     * @param plist_id IN Dataset transfer property list
     * @param left IN split ratio for leftmost nodes
     * @param right IN split ratio for righttmost nodes
     * @param middle IN split ratio for all other nodes
     * @return non-negative if succeed
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Pset_btree_ratios(int plist_id, double left, double middle,
            double right) throws HDF5LibraryException;

    /**
     * H5Pget_btree_ratio Get the B-tree split ratios for a dataset transfer property list.
     * 
     * @param plist_id IN Dataset transfer property list
     * @param left OUT split ratio for leftmost nodes
     * @param right OUT split ratio for righttmost nodes
     * @param middle OUT split ratio for all other nodes
     * @return non-negative if succeed
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - an input array is null.
     */
    public static native int H5Pget_btree_ratios(int plist_id, double[] left, double[] middle,
            double[] right) throws HDF5LibraryException, NullPointerException;

    /**
     * H5Pset_create_intermediate_group pecifies in property list whether to create missing
     * intermediate groups.
     * <p>
     * H5Pset_create_intermediate_group specifies whether to set the link creation property list
     * lcpl_id so that calls to functions that create objects in groups different from the current
     * working group will create intermediate groups that may be missing in the path of a new or
     * moved object.
     * <p>
     * Functions that create objects in or move objects to a group other than the current working
     * group make use of this property. H5Gcreate_anon and H5Lmove are examles of such functions.
     * <p>
     * If crt_intermed_group is <code>true</code>, the H5G_CRT_INTMD_GROUP will be added to lcpl_id
     * (if it is not already there). Missing intermediate groups will be created upon calls to
     * functions such as those listed above that use lcpl_id.
     * <p>
     * If crt_intermed_group is <code>false</code>, the H5G_CRT_INTMD_GROUP, if present, will be
     * removed from lcpl_id. Missing intermediate groups will not be created upon calls to functions
     * such as those listed above that use lcpl_id.
     * 
     * @param lcpl_id Link creation property list identifier
     * @param crt_intermed_group Flag specifying whether to create intermediate groups upon the
     *            creation of an object
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native void H5Pset_create_intermediate_group(int lcpl_id,
            boolean crt_intermed_group) throws HDF5LibraryException;

    /**
     * Determines whether property is set to enable creating missing intermediate groups.
     * 
     * @return <code>true</code> if intermediate groups are created, <code>false</code> otherwise.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native boolean H5Pget_create_intermediate_group(int lcpl_id)
            throws HDF5LibraryException;

    /**
     * Returns a dataset transfer property list (<code>H5P_DATASET_XFER</code>) that has a
     * conversion exception handler set which abort conversions that triggers overflows.
     */
    public static native int H5Pcreate_xfer_abort_overflow();

    /**
     * Returns a dataset transfer property list (<code>H5P_DATASET_XFER</code>) that has a
     * conversion exception handler set which aborts all conversions.
     */
    public static native int H5Pcreate_xfer_abort();

    // ////////////////////////////////////////////////////////////
    // //
    // H5R: Reference Interface Functions //
    // //
    // ////////////////////////////////////////////////////////////

    private static native int H5Rcreate(byte[] ref, int loc_id, String name, int ref_type,
            int space_id) throws HDF5LibraryException, NullPointerException,
            IllegalArgumentException;

    /**
     * H5Rcreate creates the reference, ref, of the type specified in ref_type, pointing to the
     * object name located at loc_id.
     * 
     * @param loc_id IN: Location identifier used to locate the object being pointed to.
     * @param name IN: Name of object at location loc_id.
     * @param ref_type IN: Type of reference.
     * @param space_id IN: Dataspace identifier with selection.
     * @return the reference (byte[]) if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - an input array is null.
     * @exception IllegalArgumentException - an input array is invalid.
     */
    public static byte[] H5Rcreate(final int loc_id, final String name, final int ref_type,
            final int space_id) throws HDF5LibraryException, NullPointerException,
            IllegalArgumentException
    {
        /* These sizes are correct for HDF5.1.2 */
        int ref_size = 8;
        if (ref_type == HDF5Constants.H5R_DATASET_REGION)
        {
            ref_size = 12;
        }
        final byte rbuf[] = new byte[ref_size];

        /* will raise an exception if fails */
        H5Rcreate(rbuf, loc_id, name, ref_type, space_id);

        return rbuf;
    }

    /**
     * H5Rcreate creates the object references, pointing to the object names located at loc_id.
     * 
     * @param loc_id IN: Location identifier used to locate the object being pointed to.
     * @param name IN: Names of objects at location loc_id.
     * @return the reference (long[]) if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - an input array is null.
     * @exception IllegalArgumentException - an input array is invalid.
     */
    public static native long[] H5Rcreate(final int loc_id, final String[] name)
            throws HDF5LibraryException, NullPointerException, IllegalArgumentException;

    /**
     * Given a reference to some object, H5Rdereference opens that object and return an identifier.
     * 
     * @param loc_id IN: Location identifier used to locate the object being pointed to.
     * @param ref_type IN: The reference type of ref.
     * @param ref IN: reference to an object
     * @return valid identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - output array is null.
     * @exception IllegalArgumentException - output array is invalid.
     */
    public static native int H5Rdereference(int loc_id, int ref_type, byte[] ref)
            throws HDF5LibraryException, NullPointerException, IllegalArgumentException;

    /**
     * Given a reference to some object, H5Rdereference opens that object and return an identifier.
     * 
     * @param loc_id IN: Location identifier used to locate the object being pointed to.
     * @param ref IN: reference to an object
     * @return valid identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - output array is null.
     * @exception IllegalArgumentException - output array is invalid.
     */
    public static native int H5Rdereference(int loc_id, long ref)
            throws HDF5LibraryException, NullPointerException, IllegalArgumentException;

    /**
     * Given a reference to an object ref, H5Rget_region creates a copy of the dataspace of the
     * dataset pointed to and defines a selection in the copy which is the region pointed to.
     * 
     * @param loc_id IN: loc_id of the reference object.
     * @param ref_type IN: The reference type of ref.
     * @param ref OUT: the reference to the object and region
     * @return a valid identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - output array is null.
     * @exception IllegalArgumentException - output array is invalid.
     */
    public static native int H5Rget_region(int loc_id, int ref_type, byte[] ref)
            throws HDF5LibraryException, NullPointerException, IllegalArgumentException;

    /**
     * Given a reference to an object, H5Rget_obj_type returns the type of the object pointed to.
     * 
     * @param loc_id Identifier of the reference object.
     * @param ref_type Type of reference to query.
     * @param ref The reference.
     * @return a valid identifier if successful; otherwise a negative value is returned to signal
     *         failure.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - array is null.
     * @exception IllegalArgumentException - array is invalid.
     */
    public static native int H5Rget_obj_type(int loc_id, int ref_type, byte[] ref)
            throws HDF5LibraryException, NullPointerException, IllegalArgumentException;

    /**
     * Given a reference to an object, H5Rget_name returns the name (path) of the object pointed to.
     * 
     * @param loc_id Identifier of the reference object.
     * @param ref_type Type of reference to query.
     * @param ref The reference.
     * @return The path of the object being pointed to, or an empty string, if the object being
     *         pointed to has no name.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - array is null.
     * @exception IllegalArgumentException - array is invalid.
     */
    public static native String H5Rget_name(int loc_id, int ref_type, byte[] ref);

    /**
     * Given a reference to an object, H5Rget_name returns the name (path) of the object pointed to.
     * 
     * @param loc_id Identifier of the reference object.
     * @param ref The reference.
     * @return The path of the object being pointed to, or an empty string, if the object being
     *         pointed to has no name.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - array is null.
     * @exception IllegalArgumentException - array is invalid.
     */
    public static native String H5Rget_name(int loc_id, long ref);

    /**
     * Given an array of object references (ref), H5Rget_name returns the names (paths) of the
     * objects pointed to.
     * 
     * @param loc_id Identifier of the reference object.
     * @param ref The references.
     * @return The paths of the objects being pointed to, or an empty string, if an object being
     *         pointed to has no name.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - array is null.
     * @exception IllegalArgumentException - array is invalid.
     */
    public static native String[] H5Rget_name(int loc_id, long[] ref);

    // ////////////////////////////////////////////////////////////
    // //
    // H5S: Dataspace Interface Functions //
    // //
    // ////////////////////////////////////////////////////////////

    /**
     * H5Screate creates a new dataspace of a particular type.
     * 
     * @param type The type of dataspace to be created.
     * @return a dataspace identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Screate(int type) throws HDF5LibraryException;

    /**
     * H5Screate_simple creates a new simple data space and opens it for access.
     * 
     * @param rank Number of dimensions of dataspace.
     * @param dims An array of the size of each dimension.
     * @param maxdims An array of the maximum size of each dimension.
     * @return a dataspace identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - dims or maxdims is null.
     */
    public static native int H5Screate_simple(int rank, byte[] dims, byte[] maxdims)
            throws HDF5LibraryException, NullPointerException;

    public static int H5Screate_simple(final int rank, final long[] dims, final long[] maxdims)
            throws HDF5Exception, NullPointerException
    {
        if (dims == null)
        {
            return -1;
        }

        final byte[] dimsAsByteArray = HDFNativeData.longToByte(dims);
        final byte[] maxDimsAsByteArray =
                (maxdims != null) ? HDFNativeData.longToByte(maxdims) : null;

        return H5Screate_simple(rank, dimsAsByteArray, maxDimsAsByteArray);
    }

    /**
     * H5Scopy creates a new dataspace which is an exact copy of the dataspace identified by
     * space_id.
     * 
     * @param space_id Identifier of dataspace to copy.
     * @return a dataspace identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Scopy(int space_id) throws HDF5LibraryException;

    /**
     * H5Sselect_elements selects array elements to be included in the selection for the space_id
     * dataspace.
     * 
     * @param space_id Identifier of the dataspace.
     * @param op operator specifying how the new selection is combined.
     * @param num_elements Number of elements to be selected.
     * @param coord A 2-dimensional array specifying the coordinates of the elements.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Sselect_elements(int space_id, int op, int num_elements, byte[] coord)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Sselect_all selects the entire extent of the dataspace space_id.
     * 
     * @param space_id IN: The identifier of the dataspace to be selected.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Sselect_all(int space_id) throws HDF5LibraryException;

    /**
     * H5Sselect_none resets the selection region for the dataspace space_id to include no elements.
     * 
     * @param space_id IN: The identifier of the dataspace to be reset.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Sselect_none(int space_id) throws HDF5LibraryException;

    /**
     * H5Sselect_valid verifies that the selection for the dataspace.
     * 
     * @param space_id The identifier for the dataspace in which the selection is being reset.
     * @return true if the selection is contained within the extent and FALSE if it is not or is an
     *         error.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native boolean H5Sselect_valid(int space_id) throws HDF5LibraryException;

    /**
     * H5Sget_simple_extent_npoints determines the number of elements in a dataspace.
     * 
     * @param space_id ID of the dataspace object to query
     * @return the number of elements in the dataspace if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native long H5Sget_simple_extent_npoints(int space_id)
            throws HDF5LibraryException;

    /**
     * H5Sget_select_npoints determines the number of elements in the current selection of a
     * dataspace.
     * 
     * @param space_id Dataspace identifier.
     * @return the number of elements in the selection if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native long H5Sget_select_npoints(int space_id) throws HDF5LibraryException;

    /**
     * H5Sget_simple_extent_ndims determines the dimensionality (or rank) of a dataspace.
     * 
     * @param space_id Identifier of the dataspace
     * @return the number of dimensions in the dataspace if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Sget_simple_extent_ndims(int space_id) throws HDF5LibraryException;

    /**
     * H5Sget_simple_extent_dims returns the size and maximum sizes of each dimension of a dataspace
     * through the dims and maxdims parameters.
     * 
     * @param space_id IN: Identifier of the dataspace object to query
     * @param dims OUT: Pointer to array to store the size of each dimension.
     * @param maxdims OUT: Pointer to array to store the maximum size of each dimension.
     * @return the number of dimensions in the dataspace if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - dims or maxdims is null.
     */
    public static native int H5Sget_simple_extent_dims(int space_id, long[] dims, long[] maxdims)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Sget_simple_extent_type queries a dataspace to determine the current class of a dataspace.
     * 
     * @param space_id Dataspace identifier.
     * @return a dataspace class name if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Sget_simple_extent_type(int space_id) throws HDF5LibraryException;

    /**
     * H5Sset_extent_simple sets or resets the size of an existing dataspace.
     * 
     * @param space_id Dataspace identifier.
     * @param rank Rank, or dimensionality, of the dataspace.
     * @param current_size Array containing current size of dataspace.
     * @param maximum_size Array containing maximum size of dataspace.
     * @return a dataspace identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Sset_extent_simple(int space_id, int rank, byte[] current_size,
            byte[] maximum_size) throws HDF5LibraryException, NullPointerException;

    public static int H5Sset_extent_simple(final int space_id, final int rank,
            final long[] currentSize, final long[] maxSize) throws HDF5Exception,
            NullPointerException
    {
        if (currentSize == null)
        {
            return -1;
        }

        final byte[] currentSizeAsByteArray = HDFNativeData.longToByte(currentSize);
        final byte[] maxSizeAsByteArray =
                (maxSize != null) ? HDFNativeData.longToByte(maxSize) : null;

        return H5Screate_simple(rank, currentSizeAsByteArray, maxSizeAsByteArray);
    }

    /**
     * H5Sis_simple determines whether a dataspace is a simple dataspace.
     * 
     * @param space_id Identifier of the dataspace to query
     * @return true if is a simple dataspace
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native boolean H5Sis_simple(int space_id) throws HDF5LibraryException;

    /**
     * H5Soffset_simple sets the offset of a simple dataspace space_id.
     * 
     * @param space_id IN: The identifier for the dataspace object to reset.
     * @param offset IN: The offset at which to position the selection.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - offset array is null.
     */
    public static native int H5Soffset_simple(int space_id, byte[] offset)
            throws HDF5LibraryException, NullPointerException;

    public static int H5Soffset_simple(final int space_id, final long[] offset)
            throws HDF5Exception, NullPointerException
    {
        if (offset == null)
        {
            return -1;
        }

        final byte[] offsetAsByteArray = HDFNativeData.longToByte(offset);

        return H5Soffset_simple(space_id, offsetAsByteArray);
    }

    /**
     * H5Sextent_copy copies the extent from source_space_id to dest_space_id. This action may
     * change the type of the dataspace.
     * 
     * @param dest_space_id IN: The identifier for the dataspace from which the extent is copied.
     * @param source_space_id IN: The identifier for the dataspace to which the extent is copied.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Sextent_copy(int dest_space_id, int source_space_id)
            throws HDF5LibraryException;

    /**
     * H5Sset_extent_none removes the extent from a dataspace and sets the type to H5S_NONE.
     * 
     * @param space_id The identifier for the dataspace from which the extent is to be removed.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Sset_extent_none(int space_id) throws HDF5LibraryException;

    /**
     * H5Sselect_hyperslab selects a hyperslab region to add to the current selected region for the
     * dataspace specified by space_id. The start, stride, count, and block arrays must be the same
     * size as the rank of the dataspace.
     * 
     * @param space_id IN: Identifier of dataspace selection to modify
     * @param op IN: Operation to perform on current selection.
     * @param start IN: Offset of start of hyperslab
     * @param count IN: Number of blocks included in hyperslab.
     * @param stride IN: Hyperslab stride.
     * @param block IN: Size of block in hyperslab.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - an input array is null.
     * @exception NullPointerException - an input array is invalid.
     */
    public static native int H5Sselect_hyperslab(int space_id, int op, byte[] start, byte[] stride,
            byte[] count, byte[] block) throws HDF5LibraryException, NullPointerException,
            IllegalArgumentException;

    public static int H5Sselect_hyperslab(final int space_id, final int op, final long[] start,
            final long[] stride, final long[] count, final long[] block) throws HDF5Exception,
            NullPointerException, IllegalArgumentException
    {
        final byte[] startAsByteArray = (start != null) ? HDFNativeData.longToByte(start) : null;
        final byte[] countAsByteArray = (count != null) ? HDFNativeData.longToByte(count) : null;
        final byte[] strideAsByteArray = (stride != null) ? HDFNativeData.longToByte(stride) : null;
        final byte[] blockAsByteArray = (block != null) ? HDFNativeData.longToByte(block) : null;

        return H5Sselect_hyperslab(space_id, op, startAsByteArray, strideAsByteArray,
                countAsByteArray, blockAsByteArray);
    }

    /**
     * H5Sclose releases a dataspace.
     * 
     * @param space_id Identifier of dataspace to release.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Sclose(int space_id) throws HDF5LibraryException;

    // --//

    // following static native functions are missing from HDF5 (version 1.0.1) RM

    /**
     * H5Sget_select_hyper_nblocks returns the number of hyperslab blocks in the current dataspace
     * selection.
     * 
     * @param spaceid Identifier of dataspace to release.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native long H5Sget_select_hyper_nblocks(int spaceid) throws HDF5LibraryException;

    /**
     * H5Sget_select_elem_npoints returns the number of element points in the current dataspace
     * selection.
     * 
     * @param spaceid Identifier of dataspace to release.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native long H5Sget_select_elem_npoints(int spaceid) throws HDF5LibraryException;

    /**
     * H5Sget_select_hyper_blocklist returns an array of hyperslab blocks. The block coordinates
     * have the same dimensionality (rank) as the dataspace they are located within. The list of
     * blocks is formatted as follows:
     * 
     * <pre>
     * 
     * &lt;&quot;start&quot; coordinate&gt;, immediately followed by &lt;&quot;opposite&quot; corner
     * coordinate&gt;, followed by the next &quot;start&quot; and &quot;opposite&quot; coordinates,
     * etc. until all of the selected blocks have been listed.
     * 
     * </pre>
     * 
     * @param spaceid Identifier of dataspace to release.
     * @param startblock first block to retrieve
     * @param numblocks number of blocks to retrieve
     * @param buf returns blocks startblock to startblock+num-1, each block is <i>rank</i> * 2
     *            (corners) longs.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - buf is null.
     */
    public static native int H5Sget_select_hyper_blocklist(int spaceid, long startblock,
            long numblocks, long[] buf) throws HDF5LibraryException, NullPointerException;

    /**
     * H5Sget_select_elem_pointlist returns an array of of element points in the current dataspace
     * selection. The point coordinates have the same dimensionality (rank) as the dataspace they
     * are located within, one coordinate per point.
     * 
     * @param spaceid Identifier of dataspace to release.
     * @param startpoint first point to retrieve
     * @param numpoints number of points to retrieve
     * @param buf returns points startblock to startblock+num-1, each points is <i>rank</i> longs.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - buf is null.
     */
    public static native int H5Sget_select_elem_pointlist(int spaceid, long startpoint,
            long numpoints, long[] buf) throws HDF5LibraryException, NullPointerException;

    /**
     * H5Sget_select_bounds retrieves the coordinates of the bounding box containing the current
     * selection and places them into user-supplied buffers.
     * <P>
     * The start and end buffers must be large enough to hold the dataspace rank number of
     * coordinates.
     * 
     * @param spaceid Identifier of dataspace to release.
     * @param start coordinates of lowest corner of bounding box.
     * @param end coordinates of highest corner of bounding box.
     * @return a non-negative value if successful,with start and end initialized.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - start or end is null.
     */
    public static native int H5Sget_select_bounds(int spaceid, long[] start, long[] end)
            throws HDF5LibraryException, NullPointerException;

    // ////////////////////////////////////////////////////////////
    // //
    // H5T: Datatype Interface Functions //
    // //
    // ////////////////////////////////////////////////////////////

    /**
     * H5Topen opens a named datatype at the location specified by loc_id and return an identifier
     * for the datatype.
     * 
     * @param loc_id A file, group, or datatype identifier.
     * @param name A datatype name.
     * @param access_plist_id Datatype access property list identifier.
     * @return a named datatype identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Topen(int loc_id, String name, int access_plist_id)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Tcommit commits a transient datatype (not immutable) to a file, turned it into a named
     * datatype.
     * 
     * @param loc_id A file or group identifier.
     * @param name A datatype name.
     * @param type_id A datatype identifier.
     * @param link_create_plist_id Link creation property list.
     * @param dtype_create_plist_id Datatype creation property list.
     * @param dtype_access_plist_id Datatype access property list.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Tcommit(int loc_id, String name, int type_id,
            int link_create_plist_id, int dtype_create_plist_id, int dtype_access_plist_id)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Tcommitted queries a type to determine whether the type specified by the type identifier is
     * a named type or a transient type.
     * 
     * @param type Datatype identifier.
     * @return true if successfully committed
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native boolean H5Tcommitted(int type) throws HDF5LibraryException;

    /**
     * H5Tcreate creates a new dataype of the specified class with the specified number of bytes.
     * 
     * @param dclass Class of datatype to create.
     * @param size The number of bytes in the datatype to create.
     * @return datatype identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tcreate(int dclass, int size) throws HDF5LibraryException;

    /**
     * H5Tcopy copies an existing datatype. The returned type is always transient and unlocked.
     * 
     * @param type_id Identifier of datatype to copy. Can be a datatype identifier, a predefined
     *            datatype (defined in H5Tpublic.h), or a dataset Identifier.
     * @return a datatype identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tcopy(int type_id) throws HDF5LibraryException;

    /**
     * H5Tequal determines whether two datatype identifiers refer to the same datatype.
     * 
     * @param type_id1 Identifier of datatype to compare.
     * @param type_id2 Identifier of datatype to compare.
     * @return true if the datatype identifiers refer to the same datatype, else FALSE.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native boolean H5Tequal(int type_id1, int type_id2) throws HDF5LibraryException;

    /**
     * H5Tlock locks the datatype specified by the type_id identifier, making it read-only and
     * non-destrucible.
     * 
     * @param type_id Identifier of datatype to lock.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tlock(int type_id) throws HDF5LibraryException;

    /**
     * H5Tget_class returns the datatype class identifier.
     * 
     * @param type_id Identifier of datatype to query.
     * @return datatype class identifier if successful; otherwise H5T_NO_CLASS (-1).
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tget_class(int type_id) throws HDF5LibraryException;

    /**
     * H5Tget_size returns the size of a datatype in bytes as an int value.
     * 
     * @param type_id Identifier of datatype to query.
     * @return the size of the datatype in bytes if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library, or if the size of the data
     *                type exceeds an int
     */
    public static native int H5Tget_size(int type_id) throws HDF5LibraryException;

    /**
     * H5Tget_size returns the size of a datatype in bytes as a long value.
     * 
     * @param type_id Identifier of datatype to query.
     * @return the size of the datatype in bytes if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native long H5Tget_size_long(int type_id) throws HDF5LibraryException;

    /**
     * H5Tset_size sets the total size in bytes, size, for an atomic datatype (this operation is not
     * permitted on compound datatypes).
     * 
     * @param type_id Identifier of datatype to change size.
     * @param size Size in bytes to modify datatype.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tset_size(int type_id, int size) throws HDF5LibraryException;

    /**
     * H5Tget_order returns the byte order of an atomic datatype.
     * 
     * @param type_id Identifier of datatype to query.
     * @return a byte order constant if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tget_order(int type_id) throws HDF5LibraryException;

    /**
     * H5Tset_order sets the byte ordering of an atomic datatype.
     * 
     * @param type_id Identifier of datatype to set.
     * @param order Byte ordering constant.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tset_order(int type_id, int order) throws HDF5LibraryException;

    /**
     * H5Tget_precision returns the precision of an atomic datatype.
     * 
     * @param type_id Identifier of datatype to query.
     * @return the number of significant bits if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tget_precision(int type_id) throws HDF5LibraryException;

    /**
     * H5Tset_precision sets the precision of an atomic datatype.
     * 
     * @param type_id Identifier of datatype to set.
     * @param precision Number of bits of precision for datatype.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tset_precision(int type_id, int precision)
            throws HDF5LibraryException;

    /**
     * H5Tget_offset retrieves the bit offset of the first significant bit.
     * 
     * @param type_id Identifier of datatype to query.
     * @return a positive offset value if successful; otherwise 0.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tget_offset(int type_id) throws HDF5LibraryException;

    /**
     * H5Tset_offset sets the bit offset of the first significant bit.
     * 
     * @param type_id Identifier of datatype to set.
     * @param offset Offset of first significant bit.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tset_offset(int type_id, int offset) throws HDF5LibraryException;

    /**
     * H5Tget_pad retrieves the padding type of the least and most-significant bit padding.
     * 
     * @param type_id IN: Identifier of datatype to query.
     * @param pad OUT: locations to return least-significant and most-significant bit padding type.
     * 
     *            <pre>
     * 
     *            pad[0] = lsb // least-significant bit padding type pad[1] = msb //
     *            most-significant bit padding type
     * 
     * </pre>
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - pad is null.
     */
    public static native int H5Tget_pad(int type_id, int[] pad) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5Tset_pad sets the least and most-significant bits padding types.
     * 
     * @param type_id Identifier of datatype to set.
     * @param lsb Padding type for least-significant bits.
     * @param msb Padding type for most-significant bits.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tset_pad(int type_id, int lsb, int msb) throws HDF5LibraryException;

    /**
     * H5Tget_sign retrieves the sign type for an integer type.
     * 
     * @param type_id Identifier of datatype to query.
     * @return a valid sign type if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tget_sign(int type_id) throws HDF5LibraryException;

    /**
     * H5Tset_sign sets the sign proprety for an integer type.
     * 
     * @param type_id Identifier of datatype to set.
     * @param sign Sign type.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tset_sign(int type_id, int sign) throws HDF5LibraryException;

    /**
     * H5Tget_fields retrieves information about the locations of the various bit fields of a
     * floating point datatype.
     * 
     * @param type_id IN: Identifier of datatype to query.
     * @param fields OUT: location of size and bit-position.
     * 
     *            <pre>
     * 
     *            fields[0] = spos OUT: location to return size of in bits. fields[1] = epos OUT:
     *            location to return exponent bit-position. fields[2] = esize OUT: location to
     *            return size of exponent in bits. fields[3] = mpos OUT: location to return mantissa
     *            bit-position. fields[4] = msize OUT: location to return size of mantissa in bits.
     * 
     * </pre>
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - fileds is null.
     * @exception IllegalArgumentException - fileds array is invalid.
     */
    public static native int H5Tget_fields(int type_id, int[] fields) throws HDF5LibraryException,
            NullPointerException, IllegalArgumentException;

    /**
     * H5Tset_fields sets the locations and sizes of the various floating point bit fields.
     * 
     * @param type_id Identifier of datatype to set.
     * @param spos Size position.
     * @param epos Exponent bit position.
     * @param esize Size of exponent in bits.
     * @param mpos Mantissa bit position.
     * @param msize Size of mantissa in bits.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tset_fields(int type_id, int spos, int epos, int esize, int mpos,
            int msize) throws HDF5LibraryException;

    /**
     * H5Tget_ebias retrieves the exponent bias of a floating-point type.
     * 
     * @param type_id Identifier of datatype to query.
     * @return the bias if successful; otherwise 0.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tget_ebias(int type_id) throws HDF5LibraryException;

    /**
     * H5Tset_ebias sets the exponent bias of a floating-point type.
     * 
     * @param type_id Identifier of datatype to set.
     * @param ebias Exponent bias value.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tset_ebias(int type_id, int ebias) throws HDF5LibraryException;

    /**
     * H5Tget_norm retrieves the mantissa normalization of a floating-point datatype.
     * 
     * @param type_id Identifier of datatype to query.
     * @return a valid normalization type if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tget_norm(int type_id) throws HDF5LibraryException;

    /**
     * H5Tset_norm sets the mantissa normalization of a floating-point datatype.
     * 
     * @param type_id Identifier of datatype to set.
     * @param norm Mantissa normalization type.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tset_norm(int type_id, int norm) throws HDF5LibraryException;

    /**
     * H5Tget_inpad retrieves the internal padding type for unused bits in floating-point datatypes.
     * 
     * @param type_id Identifier of datatype to query.
     * @return a valid padding type if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tget_inpad(int type_id) throws HDF5LibraryException;

    /**
     * If any internal bits of a floating point type are unused (that is, those significant bits
     * which are not part of the sign, exponent, or mantissa), then H5Tset_inpad will be filled
     * according to the value of the padding value property inpad.
     * 
     * @param type_id Identifier of datatype to modify.
     * @param inpad Padding type.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tset_inpad(int type_id, int inpad) throws HDF5LibraryException;

    /**
     * H5Tget_cset retrieves the character set type of a string datatype.
     * 
     * @param type_id Identifier of datatype to query.
     * @return a valid character set type if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tget_cset(int type_id) throws HDF5LibraryException;

    /**
     * H5Tset_cset the character set to be used.
     * 
     * @param type_id Identifier of datatype to modify.
     * @param cset Character set type.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tset_cset(int type_id, int cset) throws HDF5LibraryException;

    /**
     * H5Tget_strpad retrieves the string padding method for a string datatype.
     * 
     * @param type_id Identifier of datatype to query.
     * @return a valid string padding type if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tget_strpad(int type_id) throws HDF5LibraryException;

    /**
     * H5Tset_strpad defines the storage mechanism for the string.
     * 
     * @param type_id Identifier of datatype to modify.
     * @param strpad String padding type.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tset_strpad(int type_id, int strpad) throws HDF5LibraryException;

    /**
     * H5Tget_nmembers retrieves the number of fields a compound datatype has.
     * 
     * @param type_id Identifier of datatype to query.
     * @return number of members datatype has if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tget_nmembers(int type_id) throws HDF5LibraryException;

    /**
     * H5Tget_member_name retrieves the name of a field of a compound datatype.
     * 
     * @param type_id Identifier of datatype to query.
     * @param field_idx Field index (0-based) of the field name to retrieve.
     * @return a valid pointer if successful; otherwise null.
     */
    public static native String H5Tget_member_name(int type_id, int field_idx);

    /**
     * H5Tget_member_index retrieves the index of a field of a compound datatype.
     * 
     * @param type_id Identifier of datatype to query.
     * @param field_name Field name of the field index to retrieve.
     * @return if field is defined, the index; else negative.
     */
    public static native int H5Tget_member_index(int type_id, String field_name);

    /**
     * H5Tget_member_class returns the datatype of the specified member.
     * 
     * @param type_id Identifier of datatype to query.
     * @param field_idx Field index (0-based) of the field type to retrieve.
     * @return the identifier of a copy of the datatype of the field if successful;
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tget_member_class(int type_id, int field_idx)
            throws HDF5LibraryException;

    /**
     * H5Tget_member_type returns the datatype of the specified member.
     * 
     * @param type_id Identifier of datatype to query.
     * @param field_idx Field index (0-based) of the field type to retrieve.
     * @return the identifier of a copy of the datatype of the field if successful;
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tget_member_type(int type_id, int field_idx)
            throws HDF5LibraryException;

    /**
     * H5Tget_member_offset returns the byte offset of the specified member of the compound
     * datatype. This is the byte offset in the HDF-5 file/library, NOT the offset of any Java
     * object which might be mapped to this data item.
     * 
     * @param type_id Identifier of datatype to query.
     * @param membno Field index (0-based) of the field type to retrieve.
     * @return the offset of the member.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native long H5Tget_member_offset(int type_id, int membno)
            throws HDF5LibraryException;

    /**
     * H5Tinsert adds another member to the compound datatype type_id.
     * 
     * @param type_id Identifier of compound datatype to modify.
     * @param name Name of the field to insert.
     * @param offset Offset in memory structure of the field to insert.
     * @param field_id Datatype identifier of the field to insert.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Tinsert(int type_id, String name, long offset, int field_id)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Tpack recursively removes padding from within a compound datatype to make it more efficient
     * (space-wise) to store that data.
     * <P>
     * <b>WARNING:</b> This call only affects the C-data, even if it succeeds, there may be no
     * visible effect on Java objects.
     * 
     * @param type_id Identifier of datatype to modify.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tpack(int type_id) throws HDF5LibraryException;

    /**
     * H5Tclose releases a datatype.
     * 
     * @param type_id Identifier of datatype to release.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tclose(int type_id) throws HDF5LibraryException;

    /**
     * H5Tenum_create creates a new enumeration datatype based on the specified base datatype,
     * parent_id, which must be an integer type.
     * 
     * @param base_id Identifier of the parent datatype to release.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tenum_create(int base_id) throws HDF5LibraryException;

    /**
     * H5Tenum_insert inserts a new enumeration datatype member into an 8bit enumeration datatype.
     * 
     * @param type Identifier of datatype.
     * @param name The name of the member
     * @param value The value of the member, data of the correct type
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Tenum_insert(int type, String name, byte value)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Tenum_insert inserts a new enumeration datatype member into a 16bit enumeration datatype.
     * 
     * @param type Identifier of datatype.
     * @param name The name of the member
     * @param value The value of the member, data of the correct type
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Tenum_insert(int type, String name, short value)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Tenum_insert inserts a new enumeration datatype member into a 32bit enumeration datatype.
     * 
     * @param type Identifier of datatype.
     * @param name The name of the member
     * @param value The value of the member, data of the correct type
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Tenum_insert(int type, String name, int value)
            throws HDF5LibraryException, NullPointerException;

    /**
     * Converts the <var>value</var> (in place) to little endian.
     * 
     * @return a non-negative value if successful
     */
    public static native int H5Tconvert_to_little_endian(short[] value);

    /**
     * Converts the <var>value</var> (in place) to little endian.
     * 
     * @return a non-negative value if successful
     */
    public static native int H5Tconvert_to_little_endian(int[] value);

    /**
     * H5Tenum_nameof finds the symbol name that corresponds to the specified value of the
     * enumeration datatype type.
     * 
     * @param type IN: Identifier of datatype.
     * @param value IN: The value of the member, data of the correct
     * @param name OUT: The name of the member
     * @param size IN: The max length of the name
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Tenum_nameof(int type, int[] value, String[] name, int size)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Tenum_valueof finds the value that corresponds to the specified name of the enumeration
     * datatype type.
     * 
     * @param type IN: Identifier of datatype.
     * @param name IN: The name of the member
     * @param value OUT: The value of the member
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Tenum_valueof(int type, String name, int[] value)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Tvlen_create creates a new variable-length (VL) dataype.
     * 
     * @param base_id IN: Identifier of parent datatype.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tvlen_create(int base_id) throws HDF5LibraryException;

    /**
     * H5Tset_tag tags an opaque datatype type_id with a unique ASCII identifier tag.
     * 
     * @param type IN: Identifier of parent datatype.
     * @param tag IN: Name of the tag (will be stored as ASCII)
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tset_tag(int type, String tag) throws HDF5LibraryException;

    /**
     * H5Tget_tag returns the tag associated with datatype type_id.
     * 
     * @param type IN: Identifier of datatype.
     * @return the tag
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native String H5Tget_tag(int type) throws HDF5LibraryException;

    /**
     * H5Tget_super returns the type from which TYPE is derived.
     * 
     * @param type IN: Identifier of datatype.
     * @return the parent type
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Tget_super(int type) throws HDF5LibraryException;

    /**
     * H5Tget_member_value returns the value of the enumeration datatype member memb_no.
     * 
     * @param type_id IN: Identifier of datatype.
     * @param membno IN: The name of the member
     * @param value OUT: The value of the member
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Tget_member_value(int type_id, int membno, int[] value)
            throws HDF5LibraryException, NullPointerException;

    /**
     * * Array data types, new in HDF5.1.4.
     */
    /**
     * Creates an array datatype object.
     * 
     * @param base_type_id Datatype identifier for the array base datatype.
     * @param rank Rank of the array.
     * @param dims Size of each array dimension.
     * @return a valid datatype identifier if successful; otherwise returns a negative value.
     * @exception HDF5LibraryException Error from the HDF5 Library.
     * @exception NullPointerException rank is &lt; 1 or dims is null.
     */
    public static native int H5Tarray_create(int base_type_id, int rank, int[] dims)
            throws HDF5LibraryException, NullPointerException;

    /**
     * Returns the rank of an array datatype.
     * 
     * @param adtype_id Datatype identifier of array object.
     * @return the rank of the array if successful; otherwise returns a negative value.
     * @exception HDF5LibraryException Error from the HDF5 Library.
     */
    public static native int H5Tget_array_ndims(int adtype_id) throws HDF5LibraryException;

    /**
     * Returns sizes of array dimensions.
     * 
     * @param adtype_id IN: Datatype identifier of array object.
     * @param dims OUT: Sizes of array dimensions.
     * @return the non-negative number of dimensions of the array type if successful; otherwise
     *         returns a negative value.
     * @exception HDF5LibraryException Error from the HDF5 Library.
     * @exception NullPointerException dims is null.
     */
    public static native int H5Tget_array_dims(int adtype_id, int[] dims)
            throws HDF5LibraryException, NullPointerException;

    // ////////////////////////////////////////////////////////////
    // //
    // New APIs for HDF5.1.6 //
    // removed APIs: H5Pset_hyper_cache, H5Pget_hyper_cache //
    // //
    // ////////////////////////////////////////////////////////////

    /**
     * Returns number of objects in the group specified by its identifier
     * 
     * @param loc_id Identifier of the group or the file
     * @param num_obj Number of objects in the group
     * @return positive value if successful; otherwise returns a negative value.
     * @throws HDF5LibraryException
     * @throws NullPointerException
     */
    public static native int H5Gget_num_objs(int loc_id, long[] num_obj)
            throws HDF5LibraryException, NullPointerException;

    /**
     * Returns a name of an object specified by an index.
     * 
     * @param group_id Group or file identifier
     * @param idx Transient index identifying object
     * @param name the object name
     * @param size Name length
     * @return the size of the object name if successful, or 0 if no name is associated with the
     *         group identifier. Otherwise returns a negative value
     * @throws HDF5LibraryException
     * @throws NullPointerException
     */
    public static native long H5Gget_objname_by_idx(int group_id, long idx, String[] name, long size)
            throws HDF5LibraryException, NullPointerException;

    /**
     * Returns the type of an object specified by an index.
     * 
     * @param group_id Group or file identifier.
     * @param idx Transient index identifying object.
     * @return Returns the type of the object if successful. Otherwise returns a negative value
     * @throws HDF5LibraryException
     * @throws NullPointerException
     */
    public static native int H5Gget_objtype_by_idx(int group_id, long idx)
            throws HDF5LibraryException;

    public static native long H5Gget_nlinks(int group_id) throws HDF5LibraryException;

    public static native int H5Tget_native_type(int tid, int alloc_time)
            throws HDF5LibraryException, NullPointerException;

    //
    // Backward compatibility:
    // These functions have been replaced by new HDF5 library calls.
    // The interface is preserved as a convenience to existing code.
    //
    /**
     * H5Gn_members report the number of objects in a Group. The 'objects' include everything that
     * will be visited by H5Giterate. Each link is returned, so objects with multiple links will be
     * counted once for each link.
     * 
     * @param loc_id file or group ID.
     * @param name name of the group to iterate, relative to the loc_id
     * @return the number of members in the group or -1 if error.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static int H5Gn_members(final int loc_id, final String name)
            throws HDF5LibraryException, NullPointerException
    {
        final int grp_id = H5Gopen(loc_id, name, HDF5Constants.H5P_DEFAULT);
        final long[] nobj = new long[1];
        nobj[0] = -1;
        H5Gget_num_objs(grp_id, nobj);
        final int r = (new Long(nobj[0])).intValue();
        return (r);
    }

    /**
     * H5Gget_obj_info_idx report the name and type of object with index 'idx' in a Group. The 'idx'
     * corresponds to the index maintained by H5Giterate. Each link is returned, so objects with
     * multiple links will be counted once for each link.
     * 
     * @param loc_id IN: file or group ID.
     * @param name IN: name of the group to iterate, relative to the loc_id
     * @param idx IN: the index of the object to iterate.
     * @param oname the name of the object [OUT]
     * @param type the type of the object [OUT]
     * @return non-negative if successful, -1 if not.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static int H5Gget_obj_info_idx(final int loc_id, final String name, final int idx,
            final String[] oname, final int[] type) throws HDF5LibraryException,
            NullPointerException
    {
        final long default_buf_size = 4096;
        final String n[] = new String[1];
        n[0] = new String("");
        final int grp_id = H5Gopen(loc_id, name, HDF5Constants.H5P_DEFAULT);
        final long val = H5Gget_objname_by_idx(grp_id, idx, n, default_buf_size);
        final int type_code = H5Gget_objtype_by_idx(grp_id, idx);
        oname[0] = new String(n[0]);
        type[0] = type_code;
        final int ret = (new Long(val)).intValue();
        return ret;
    }

    public static int H5Gget_obj_info_all(final int loc_id, final String name,
            final String[] oname, final int[] type) throws HDF5LibraryException,
            NullPointerException
    {
        if (oname == null)
        {
            throw new NullPointerException("H5Gget_obj_info_all(): name array is null");
        }

        if (type == null)
        {
            throw new NullPointerException("H5Gget_obj_info_all(): type array is null");
        }

        if (oname.length == 0)
        {
            throw new HDF5JavaException("H5Gget_obj_info_all(): array size is zero");
        }

        if (oname.length != type.length)
        {
            throw new HDF5JavaException(
                    "H5Gget_obj_info_all(): name and type array sizes are different");
        }

        return H5Gget_obj_info_all(loc_id, name, oname, type, oname.length);
    }

    public static native int H5Gget_obj_info_all(int loc_id, String name, String[] oname,
            int[] type, int n) throws HDF5LibraryException, NullPointerException;

    //
    // This function is deprecated. It is recommended that the new
    // library calls should be used,
    // H5Gget_objname_by_idx
    // H5Gget_objtype_by_idx
    //
    /**
     * H5Gget_objinfo returns information about the specified object.
     * 
     * @param loc_id IN: File, group, dataset, or datatype identifier.
     * @param name IN: Name of the object for which status is being sought.
     * @param follow_link IN: Link flag.
     * @param fileno OUT: file id numbers.
     * @param objno OUT: object id numbers.
     * @param link_info OUT: link information.
     * 
     *            <pre>
     * 
     *            link_info[0] = nlink link_info[1] = type link_info[2] = linklen
     * 
     * </pre>
     * @param mtime OUT: modification time
     * @return a non-negative value if successful, with the fields of link_info and mtime (if
     *         non-null) initialized.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name or array is null.
     * @exception IllegalArgumentException - bad argument.
     */
    public static native int H5Gget_objinfo(int loc_id, String name, boolean follow_link,
            long[] fileno, long[] objno, int[] link_info, long[] mtime)
            throws HDF5LibraryException, NullPointerException, IllegalArgumentException;

    /**
     * H5Gget_objinfo returns information about the specified object in an HDF5GroupInfo object.
     * 
     * @param loc_id IN: File, group, dataset, or datatype identifier.
     * @param name IN: Name of the object for which status is being sought.
     * @param follow_link IN: Link flag.
     * @param info OUT: the HDF5GroupInfo object to store the object infomation
     * @return a non-negative value if successful, with the fields of HDF5GroupInfo object (if
     *         non-null) initialized.
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     * @see ch.systemsx.cisd.hdf5.hdf5lib.HDF5GroupInfo See public static native int
     *      H5Gget_objinfo();
     */
    public static int H5Gget_objinfo(final int loc_id, final String name,
            final boolean follow_link, final HDF5GroupInfo info) throws HDF5LibraryException,
            NullPointerException
    {
        int status = -1;
        final long[] fileno = new long[2];
        final long[] objno = new long[2];
        final int[] link_info = new int[3];
        final long[] mtime = new long[1];

        status = H5Gget_objinfo(loc_id, name, follow_link, fileno, objno, link_info, mtime);

        if (status >= 0)
        {
            info.setGroupInfo(fileno, objno, link_info[0], link_info[1], mtime[0], link_info[2]);
        }
        return status;
    }

    public static int H5Tget_native_type(final int tid) throws HDF5LibraryException,
            NullPointerException
    {
        return H5Tget_native_type(tid, HDF5Constants.H5T_DIR_ASCEND);
    }

    public static native int H5Pset_alloc_time(int plist_id, int alloc_time)
            throws HDF5LibraryException, NullPointerException;

    public static native int H5Pget_alloc_time(int plist_id, int[] alloc_time)
            throws HDF5LibraryException, NullPointerException;

    public static native int H5Pset_fill_time(int plist_id, int fill_time)
            throws HDF5LibraryException, NullPointerException;

    public static native int H5Pget_fill_time(int plist_id, int[] fill_time)
            throws HDF5LibraryException, NullPointerException;

    public static native int H5Pfill_value_defined(int plist_id, int[] status)
            throws HDF5LibraryException, NullPointerException;

    public static native int H5Pset_fletcher32(int plist) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Pset_edc_check(int plist, int check) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Pget_edc_check(int plist) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Pset_shuffle(int plist_id) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Pset_szip(int plist, int options_mask, int pixels_per_block)
            throws HDF5LibraryException, NullPointerException;

    public static native int H5Dget_space_status(int dset_id, int[] status)
            throws HDF5LibraryException, NullPointerException;

    public static native long H5Iget_name(int obj_id, String[] name, long size)
            throws HDF5LibraryException, NullPointerException;

    public static native int H5set_free_list_limits(int reg_global_lim, int reg_list_lim,
            int arr_global_lim, int arr_list_lim, int blk_global_lim, int blk_list_lim)
            throws HDF5LibraryException, NullPointerException;

    public static native int H5Fget_obj_ids(int file_id, int types, int max, int[] obj_id_list)
            throws HDF5LibraryException, NullPointerException;

    public static native int H5Fget_obj_count(int file_id, int types) throws HDF5LibraryException,
            NullPointerException;

    public static native boolean H5Tis_variable_str(int dtype_id) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Zfilter_avail(int filter) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Zunregister(int filter) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Pmodify_filter(int plist, int filter, int flags, long cd_nelmts,
            int[] cd_values) throws HDF5LibraryException, NullPointerException;

    public static native int H5Pget_filter_by_id(int plist_id, int filter, int[] flags,
            long[] cd_nelmts, int[] cd_values, long namelen, String[] name)
            throws HDF5LibraryException, NullPointerException;

    public static native boolean H5Pall_filters_avail(int dcpl_id) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Pset_hyper_vector_size(int dxpl_id, long vector_size)
            throws HDF5LibraryException, NullPointerException;

    public static native int H5Pget_hyper_vector_size(int dxpl_id, long[] vector_size)
            throws HDF5LibraryException, NullPointerException;

    public static native boolean H5Tdetect_class(int dtype_id, int dtype_class)
            throws HDF5LibraryException, NullPointerException;

    // //////////////////////////////////////////////////////////////////
    // //
    // New APIs for read data from library //
    // Using H5Dread(..., Object buf) requires function calls //
    // theArray.emptyBytes() and theArray.arrayify( buf), which //
    // triples the actual memory needed by the data set. //
    // Using the following APIs solves the problem. //
    // //
    // //////////////////////////////////////////////////////////////////

    public static native int H5Dread(int dataset_id, int mem_type_id, int mem_space_id,
            int file_space_id, int xfer_plist_id, short[] buf) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Dread(int dataset_id, int mem_type_id, int mem_space_id,
            int file_space_id, int xfer_plist_id, int[] buf) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Dread(int dataset_id, int mem_type_id, int mem_space_id,
            int file_space_id, int xfer_plist_id, long[] buf) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Dread(int dataset_id, int mem_type_id, int mem_space_id,
            int file_space_id, int xfer_plist_id, float[] buf) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Dread(int dataset_id, int mem_type_id, int mem_space_id,
            int file_space_id, int xfer_plist_id, double[] buf) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Dread_string(int dataset_id, int mem_type_id, int mem_space_id,
            int file_space_id, int xfer_plist_id, String[] buf) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Dread_reg_ref(int dataset_id, int mem_type_id, int mem_space_id,
            int file_space_id, int xfer_plist_id, String[] buf) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Dwrite(int dataset_id, int mem_type_id, int mem_space_id,
            int file_space_id, int xfer_plist_id, short[] buf) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Dwrite(int dataset_id, int mem_type_id, int mem_space_id,
            int file_space_id, int xfer_plist_id, int[] buf) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Dwrite(int dataset_id, int mem_type_id, int mem_space_id,
            int file_space_id, int xfer_plist_id, long[] buf) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Dwrite(int dataset_id, int mem_type_id, int mem_space_id,
            int file_space_id, int xfer_plist_id, float[] buf) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Dwrite(int dataset_id, int mem_type_id, int mem_space_id,
            int file_space_id, int xfer_plist_id, double[] buf) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Pset_fclose_degree(int plist, int degree)
            throws HDF5LibraryException, NullPointerException;

    public static native int H5Pget_fclose_degree(int plist_id) throws HDF5LibraryException,
            NullPointerException;

    // //////////////////////////////////////////////////////////////////
    // //
    // New APIs from release 1.6.2 //
    // August 20, 2004 //
    // //////////////////////////////////////////////////////////////////

    public static native int H5Iget_ref(int obj_id) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Iinc_ref(int obj_id) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Idec_ref(int obj_id) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Pset_fapl_family(int fapl_id, long memb_size, int memb_fapl_id)
            throws HDF5LibraryException, NullPointerException;

    public static native int H5Pget_fapl_family(int fapl_id, long[] memb_size, int[] memb_fapl_id)
            throws HDF5LibraryException, NullPointerException;

    public static native int H5Pset_fapl_core(int fapl_id, int increment, boolean backing_store)
            throws HDF5LibraryException, NullPointerException;

    public static native int H5Pget_fapl_core(int fapl_id, int[] increment, boolean[] backing_store)
            throws HDF5LibraryException, NullPointerException;

    public static native int H5Pset_family_offset(int fapl_id, long offset)
            throws HDF5LibraryException, NullPointerException;

    public static native long H5Pget_family_offset(int fapl_id) throws HDF5LibraryException,
            NullPointerException;

    public static native int H5Pset_fapl_log(int fapl_id, String logfile, int flags, int buf_size)
            throws HDF5LibraryException, NullPointerException;

    // //////////////////////////////////////////////////////////////////
    // //
    // New APIs from release 1.6.3 //
    // August 25, 2004 //
    // //////////////////////////////////////////////////////////////////

    public static native long H5Fget_name(int obj_id, String name, int size)
            throws HDF5LibraryException;

    public static native long H5Fget_filesize(int file_id) throws HDF5LibraryException;

    public static native int H5Iget_file_id(int obj_id) throws HDF5LibraryException;

    public static native int H5Premove_filter(int obj_id, int filter) throws HDF5LibraryException;

    public static native int H5Zget_filter_info(int filter) throws HDF5LibraryException;

    // ////////////////////////////////////////////////////////////////////////
    // Modified by Peter Cao on July 26, 2006: //
    // Some of the Generic Property APIs have callback function //
    // pointers, which Java does not support. Only the Generic //
    // Property APIs without function pointers are implemented //
    // ////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new property list class of a given class
     * 
     * @param cls IN: Class of property list to create
     * @return a valid property list identifier if successful; a negative value if failed
     * @throws HDF5LibraryException
     */
    public static native int H5Pcreate_list(int cls) throws HDF5LibraryException;

    /**
     * Sets a property list value (support integer only)
     * 
     * @param plid IN: Property list identifier to modify
     * @param name IN: Name of property to modify
     * @param value IN: value to set the property to
     * @return a non-negative value if successful; a negative value if failed
     * @throws HDF5LibraryException
     */
    public static native int H5Pset(int plid, String name, int value) throws HDF5LibraryException;

    /**
     * H5Pexist determines whether a property exists within a property list or class
     * 
     * @param plid IN: Identifier for the property to query
     * @param name IN: Name of property to check for
     * @return a positive value if the property exists in the property object; zero if the property
     *         does not exist; a negative value if failed
     * @throws HDF5LibraryException
     */
    public static native int H5Pexist(int plid, String name) throws HDF5LibraryException;

    /**
     * H5Pget_size retrieves the size of a property's value in bytes
     * 
     * @param plid IN: Identifier of property object to query
     * @param name IN: Name of property to query
     * @return size of a property's value if successful; a negative value if failed
     * @throws HDF5LibraryException
     */
    public static native long H5Pget_size(int plid, String name) throws HDF5LibraryException;

    /**
     * H5Pget_nprops retrieves the number of properties in a property list or class
     * 
     * @param plid IN: Identifier of property object to query
     * @return number of properties if successful; a negative value if failed
     * @throws HDF5LibraryException
     */
    public static native long H5Pget_nprops(int plid) throws HDF5LibraryException;

    /**
     * H5Pget_class_name retrieves the name of a generic property list class
     * 
     * @param plid IN: Identifier of property object to query
     * @return name of a property list if successful; null if failed
     * @throws HDF5LibraryException
     */
    public static native String H5Pget_class_name(int plid) throws HDF5LibraryException;

    /**
     * H5Pget_class_parent retrieves an identifier for the parent class of a property class
     * 
     * @param plid IN: Identifier of the property class to query
     * @return a valid parent class object identifier if successful; a negative value if failed
     * @throws HDF5LibraryException
     */
    public static native int H5Pget_class_parent(int plid) throws HDF5LibraryException;

    /**
     * H5Pisa_class checks to determine whether a property list is a member of the specified class
     * 
     * @param plist IN: Identifier of the property list
     * @param pclass IN: Identifier of the property class
     * @return a positive value if equal; zero if unequal; a negative value if failed
     * @throws HDF5LibraryException
     */
    public static native int H5Pisa_class(int plist, int pclass) throws HDF5LibraryException;

    /**
     * H5Pget retrieves a copy of the value for a property in a property list (support integer only)
     * 
     * @param plid IN: Identifier of property object to query
     * @param name IN: Name of property to query
     * @return value for a property if successful; a negative value if failed
     * @throws HDF5LibraryException
     */
    public static native int H5Pget(int plid, String name) throws HDF5LibraryException;

    /**
     * H5Pequal determines if two property lists or classes are equal
     * 
     * @param plid1 IN: First property object to be compared
     * @param plid2 IN: Second property object to be compared
     * @return positive value if equal; zero if unequal, a negative value if failed
     * @throws HDF5LibraryException
     */
    public static native int H5Pequal(int plid1, int plid2) throws HDF5LibraryException;

    /**
     * H5Pcopy_prop copies a property from one property list or class to another
     * 
     * @param dst_id IN: Identifier of the destination property list or class
     * @param src_id IN: Identifier of the source property list or class
     * @param name IN: Name of the property to copy
     * @return a non-negative value if successful; a negative value if failed
     * @throws HDF5LibraryException
     */
    public static native int H5Pcopy_prop(int dst_id, int src_id, String name)
            throws HDF5LibraryException;

    /**
     * H5Premove removes a property from a property list
     * 
     * @param plid IN: Identifier of the property list to modify
     * @param name IN: Name of property to remove
     * @return a non-negative value if successful; a negative value if failed
     * @throws HDF5LibraryException
     */
    public static native int H5Premove(int plid, String name) throws HDF5LibraryException;

    /**
     * H5Punregister removes a property from a property list class
     * 
     * @param plid IN: Property list class from which to remove permanent property
     * @param name IN: Name of property to remove
     * @return a non-negative value if successful; a negative value if failed
     * @throws HDF5LibraryException
     */
    public static native int H5Punregister(int plid, String name) throws HDF5LibraryException;

    /**
     * Closes an existing property list class
     * 
     * @param plid IN: Property list class to close
     * @return a non-negative value if successful; a negative value if failed
     * @throws HDF5LibraryException
     */
    public static native int H5Pclose_class(int plid) throws HDF5LibraryException;

    // //////////////////////////////////////////////////////////////////
    // //
    // New APIs from release 1.8.0 //
    // January 21, 2008 //
    // //////////////////////////////////////////////////////////////////

    /**
     * Sets the permissible bounds of the library's file format versions.
     * <p>
     * Can be set on the file access property list.
     * <p>
     * As of 1.8.0, only the combinations <code>low=H5F_LIBVER_EARLIEST</code> / <code>
     * high=H5F_LIBVER_LATEST</code> (which is the default and means that 1.6 compatible files are
     * created if no features are used that require a 1.8 format) and <code>low=H5F_LIBVER_LATEST
     * </code> / <code>high=H5F_LIBVER_LATEST</code> (which means that always 1.8 files are created
     * which cannot be read by an earlier library) are allowed.
     * 
     * @param plist_id Property list identifier.
     * @param low The lower permissible bound. One of <code>H5F_LIBVER_LATEST</code> or <code>
     *            H5F_LIBVER_LATEST</code> .
     * @param high The higher permissible bound. Must be <code>H5F_LIBVER_LATEST</code>.
     * @return a non-negative value if successful
     */
    public static native int H5Pset_libver_bounds(int plist_id, int low, int high)
            throws HDF5LibraryException;

    /**
     * Returns the permissible bounds of the library's file format versions.
     * 
     * @param plist_id Property list identifier.
     * @return an array containing <code>[low, high]</code> on success
     */
    public static native int[] H5Pget_libver_bounds(int plist_id) throws HDF5LibraryException;

    /**
     * Sets the local heap size hint for an old-style group. This is the chunk size allocated on the
     * heap for a group.
     * 
     * @param gcpl_id The group creation property list to change the heap size hint for
     * @param size_hint The size hint to set.
     * @return a non-negative value if successful
     */
    public static native int H5Pset_local_heap_size_hint(int gcpl_id, int size_hint);

    /**
     * Returns the local heap size hint for an old-style group. This is the chunk size allocated on
     * the heap for a group.
     * 
     * @param gcpl_id The group creation property list to change the heap size hint for
     * @return The size hint of the group if successful
     */
    public static native int H5Pget_local_heap_size_hint(int gcpl_id);

    /**
     * Sets the phase change parameters for a new-style group.
     * 
     * @param gcpl_id The group creation property list to set the link phase changes for
     * @param max_compact The maximum number of links in a group to store as header messages
     * @param min_dense The minimum number of links in a group to in the dense format
     * @return a non-negative value if successful
     */
    public static native int H5Pset_link_phase_change(int gcpl_id, int max_compact, int min_dense);

    /**
     * Returns the phase change parameters for a new-style group.
     * 
     * @param gcpl_id The group creation property list to set the link phase changes for
     * @return the phase change parameters as array [max_compact, min_dense] if successful
     */
    public static native int[] H5Pget_link_phase_change(int gcpl_id);

    /**
     * Sets the character encoding for the given creation property list to the given encoding.
     * 
     * @param cpl_id The creation property list to set the character encoding for.
     * @param encoding The encoding (one of {@link HDF5Constants#H5T_CSET_ASCII} or
     *            {@link HDF5Constants#H5T_CSET_UTF8}) to use.
     * @return a non-negative value if successful
     */
    public static native int H5Pset_char_encoding(int cpl_id, int encoding);

    /**
     * Returns the character encoding currently set for a creation property list.
     * 
     * @param cpl_id The creation property list to get the character encoding for.
     * @return The encoding, one of {@link HDF5Constants#H5T_CSET_ASCII} or
     *         {@link HDF5Constants#H5T_CSET_UTF8}.
     */
    public static native int H5Pget_char_encoding(int cpl_id);

    /**
     * H5Oopen opens an existing object with the specified name at the specified location, loc_id.
     * 
     * @param loc_id File or group identifier within which object is to be open.
     * @param name Name of object to open.
     * @param access_plist_id Object access property list identifier (H5P_DEFAULT for the default
     *            property list).
     * @return a valid object identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Oopen(int loc_id, String name, int access_plist_id)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Oclose releases resources used by an object which was opened by a call to H5Oopen().
     * 
     * @param loc_id Object identifier to release.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Oclose(int loc_id) throws HDF5LibraryException;

    /**
     * H5Ocopy copies an existing object with the specified src_name at the specified location,
     * src_loc_id, to the specified dst_name at the specified destination location, dst_loc_id.
     * 
     * @param src_loc_id Source File or group identifier within which object is to be open.
     * @param src_name Name of source object to open.
     * @param dst_loc_id Destination File or group identifier within which object is to be open.
     * @param dst_name Name of destination object to open.
     * @param object_copy_plist Object copy property list identifier (H5P_DEFAULT for the default
     *            property list).
     * @param link_creation_plist Link creation property list identifier for the new hard link
     *            (H5P_DEFAULT for the default property list).
     * @return a valid object identifier if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - name is null.
     */
    public static native int H5Ocopy(int src_loc_id, String src_name, int dst_loc_id,
            String dst_name, int object_copy_plist, int link_creation_plist)
            throws HDF5LibraryException, NullPointerException;

    /**
     * H5Oget_info_by_name returns information about the object. This method follows soft links and
     * returns information about the link target, rather than the link.
     * <p>
     * If not <code>null</code>, <var>info</var> needs to be an array of length 5 and will return
     * the following information in each index:
     * <ul>
     * <li>0: filenumber that the object is in</li>
     * <li>1: address of the object in the file</li>
     * <li>2: reference count of the object (will be {@code > 1} if more than one hard link exists
     * to the object)</li>
     * <li>3: creation time of the object (in seconds since start of the epoch)</li>
     * <li>4: number of attributes that this object has</li>
     * </ul>
     * 
     * @param loc_id File or group identifier within which object is to be open.
     * @param object_name Name of object to get info for.
     * @param infoOrNull If not <code>null</code>, it will return additional information about this
     *            object. Needs to be either <code>null</code> or an array of length 5.
     * @param exception_when_non_existent If <code>true</code>, -1 will be returned when the object
     *            does not exist, otherwise a HDF5LibraryException will be thrown.
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     */
    public static native int H5Oget_info_by_name(int loc_id, String object_name, long[] infoOrNull,
            boolean exception_when_non_existent) throws HDF5LibraryException;

    /**
     * H5Lcreate_hard creates a hard link for an already existing object.
     * 
     * @param obj_loc_id File, group, dataset, or datatype identifier of the existing object
     * @param obj_name A name of the existing object
     * @param link_loc_id Location identifier of the link to create
     * @param link_name Name of the link to create
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - current_name or name is null.
     */
    public static native int H5Lcreate_hard(int obj_loc_id, String obj_name, int link_loc_id,
            String link_name, int lcpl_id, int lapl_id) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5Lcreate_soft creates a soft link to some target path.
     * 
     * @param target_path The path of the link target
     * @param link_loc_id Location identifier of the link to create
     * @param link_name Name of the link to create
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - current_name or name is null.
     */
    public static native int H5Lcreate_soft(String target_path, int link_loc_id, String link_name,
            int lcpl_id, int lapl_id) throws HDF5LibraryException, NullPointerException;

    /**
     * H5Lcreate_external creates an external link to some object in another file.
     * 
     * @param file_name File name of the link target
     * @param obj_name Object name of the link target
     * @param link_loc_id Location identifier of the link to create
     * @param link_name Name of the link to create
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - current_name or name is null.
     */
    public static native int H5Lcreate_external(String file_name, String obj_name, int link_loc_id,
            String link_name, int lcpl_id, int lapl_id) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5Lmove moves a link atomically to a new group or renames it.
     * 
     * @param src_loc_id The old location identifier of the object to be renamed
     * @param src_name The old name of the object to be renamed
     * @param dst_loc_id The new location identifier of the link
     * @param dst_name The new name the object
     * @return a non-negative value if successful
     * @exception HDF5LibraryException - Error from the HDF-5 Library.
     * @exception NullPointerException - current_name or name is null.
     */
    public static native int H5Lmove(int src_loc_id, String src_name, int dst_loc_id,
            String dst_name, int lcpl_id, int lapl_id) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5Lexists returns <code>true</code> if a link with <var>name</var> exists and <code>false
     * </code> otherwise.
     * <p>
     * <i>Note:</i> The Java wrapper differs from the low-level C routine in that it will return
     * <code>false</code> if <var>name</var> is a path that contains groups which don't exist (the C
     * routine will give you an <code>H5E_NOTFOUND</code> in this case).
     */
    public static native boolean H5Lexists(int loc_id, String name) throws HDF5LibraryException,
            NullPointerException;

    /**
     * H5Lget_link_info returns the type of the link. If <code>lname != null</code> and
     * <var>name</var> is a symbolic link, <code>lname[0]</code> will contain the target of the
     * link. If <var>exception_when_non_existent</var> is <code>true</code>, the method will throw
     * an exception when the link does not exist, otherwise -1 will be returned.
     */
    public static native int H5Lget_link_info(int loc_id, String name, String[] lname,
            boolean exception_when_non_existent) throws HDF5LibraryException, NullPointerException;

    /**
     * H5Lget_link_info_all returns the names, types and link targets of all links in group
     * <var>name</var>.
     */
    public static int H5Lget_link_info_all(final int loc_id, final String name,
            final String[] oname, final int[] type, final String[] lname)
            throws HDF5LibraryException, NullPointerException
    {
        if (oname == null)
        {
            throw new NullPointerException("H5Lget_obj_info_all(): name array is null");
        }

        if (type == null)
        {
            throw new NullPointerException("H5Lget_obj_info_all(): type array is null");
        }

        if (oname.length != type.length)
        {
            throw new HDF5JavaException(
                    "H5Lget_obj_info_all(): oname and type array sizes are different");
        }
        if (lname != null && oname.length != lname.length)
        {
            throw new HDF5JavaException(
                    "H5Lget_obj_info_all(): oname and lname array sizes are different");
        }

        return H5Lget_link_info_all(loc_id, name, oname, type, lname, oname.length);
    }

    public static native int H5Lget_link_info_all(int loc_id, String name, String[] oname,
            int[] type, String[] lname, int n) throws HDF5LibraryException, NullPointerException;

    /**
     * H5Lget_link_names_all returns the names of all links in group <var>name</var>.
     */
    public static int H5Lget_link_names_all(final int loc_id, final String name,
            final String[] oname) throws HDF5LibraryException, NullPointerException
    {
        if (oname == null)
        {
            throw new NullPointerException("H5Lget_obj_info_all(): name array is null");
        }

        return H5Lget_link_names_all(loc_id, name, oname, oname.length);
    }

    public static native int H5Lget_link_names_all(int loc_id, String name, String[] oname, int n)
            throws HDF5LibraryException, NullPointerException;

}
