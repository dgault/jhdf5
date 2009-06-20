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

import static ncsa.hdf.hdf5lib.H5.H5Aclose;
import static ncsa.hdf.hdf5lib.H5.H5Acreate;
import static ncsa.hdf.hdf5lib.H5.H5Adelete;
import static ncsa.hdf.hdf5lib.H5.H5Aexists;
import static ncsa.hdf.hdf5lib.H5.H5Aget_name;
import static ncsa.hdf.hdf5lib.H5.H5Aget_num_attrs;
import static ncsa.hdf.hdf5lib.H5.H5Aget_space;
import static ncsa.hdf.hdf5lib.H5.H5Aget_type;
import static ncsa.hdf.hdf5lib.H5.H5Aopen_idx;
import static ncsa.hdf.hdf5lib.H5.H5Aopen_name;
import static ncsa.hdf.hdf5lib.H5.H5Aread;
import static ncsa.hdf.hdf5lib.H5.H5Awrite;
import static ncsa.hdf.hdf5lib.H5.H5AreadVL;
import static ncsa.hdf.hdf5lib.H5.H5Dclose;
import static ncsa.hdf.hdf5lib.H5.H5Dcreate;
import static ncsa.hdf.hdf5lib.H5.H5Dget_create_plist;
import static ncsa.hdf.hdf5lib.H5.H5Dget_space;
import static ncsa.hdf.hdf5lib.H5.H5Dget_type;
import static ncsa.hdf.hdf5lib.H5.H5Dopen;
import static ncsa.hdf.hdf5lib.H5.H5Dread;
import static ncsa.hdf.hdf5lib.H5.H5DreadVL;
import static ncsa.hdf.hdf5lib.H5.H5Dread_double;
import static ncsa.hdf.hdf5lib.H5.H5Dread_float;
import static ncsa.hdf.hdf5lib.H5.H5Dread_int;
import static ncsa.hdf.hdf5lib.H5.H5Dread_long;
import static ncsa.hdf.hdf5lib.H5.H5Dread_short;
import static ncsa.hdf.hdf5lib.H5.H5Dread_string;
import static ncsa.hdf.hdf5lib.H5.H5Dset_extent;
import static ncsa.hdf.hdf5lib.H5.H5Fclose;
import static ncsa.hdf.hdf5lib.H5.H5Fcreate;
import static ncsa.hdf.hdf5lib.H5.H5Fflush;
import static ncsa.hdf.hdf5lib.H5.H5Fopen;
import static ncsa.hdf.hdf5lib.H5.H5Gclose;
import static ncsa.hdf.hdf5lib.H5.H5Gcreate;
import static ncsa.hdf.hdf5lib.H5.H5Gget_nlinks;
import static ncsa.hdf.hdf5lib.H5.H5Gopen;
import static ncsa.hdf.hdf5lib.H5.H5Gunlink;
import static ncsa.hdf.hdf5lib.H5.H5Iget_name;
import static ncsa.hdf.hdf5lib.H5.H5Lcreate_external;
import static ncsa.hdf.hdf5lib.H5.H5Lcreate_hard;
import static ncsa.hdf.hdf5lib.H5.H5Lcreate_soft;
import static ncsa.hdf.hdf5lib.H5.H5Lmove;
import static ncsa.hdf.hdf5lib.H5.H5Lexists;
import static ncsa.hdf.hdf5lib.H5.H5Lget_link_info;
import static ncsa.hdf.hdf5lib.H5.H5Lget_link_info_all;
import static ncsa.hdf.hdf5lib.H5.H5Lget_link_names_all;
import static ncsa.hdf.hdf5lib.H5.H5Oclose;
import static ncsa.hdf.hdf5lib.H5.H5Ocopy;
import static ncsa.hdf.hdf5lib.H5.H5Oopen;
import static ncsa.hdf.hdf5lib.H5.H5Oget_info_by_name;
import static ncsa.hdf.hdf5lib.H5.H5Pclose;
import static ncsa.hdf.hdf5lib.H5.H5Pcreate;
import static ncsa.hdf.hdf5lib.H5.H5Pcreate_xfer_abort;
import static ncsa.hdf.hdf5lib.H5.H5Pcreate_xfer_abort_overflow;
import static ncsa.hdf.hdf5lib.H5.H5Pget_chunk;
import static ncsa.hdf.hdf5lib.H5.H5Pget_layout;
import static ncsa.hdf.hdf5lib.H5.H5Pset_chunk;
import static ncsa.hdf.hdf5lib.H5.H5Pset_create_intermediate_group;
import static ncsa.hdf.hdf5lib.H5.H5Pset_deflate;
import static ncsa.hdf.hdf5lib.H5.H5Pset_layout;
import static ncsa.hdf.hdf5lib.H5.H5Pset_libver_bounds;
import static ncsa.hdf.hdf5lib.H5.H5Pset_link_phase_change;
import static ncsa.hdf.hdf5lib.H5.H5Pset_local_heap_size_hint;
import static ncsa.hdf.hdf5lib.H5.H5Pset_scaleoffset;
import static ncsa.hdf.hdf5lib.H5.H5Sclose;
import static ncsa.hdf.hdf5lib.H5.H5Screate;
import static ncsa.hdf.hdf5lib.H5.H5Screate_simple;
import static ncsa.hdf.hdf5lib.H5.H5Sget_simple_extent_dims;
import static ncsa.hdf.hdf5lib.H5.H5Sget_simple_extent_ndims;
import static ncsa.hdf.hdf5lib.H5.H5Sselect_hyperslab;
import static ncsa.hdf.hdf5lib.H5.H5Tarray_create;
import static ncsa.hdf.hdf5lib.H5.H5Tclose;
import static ncsa.hdf.hdf5lib.H5.H5Tcommit;
import static ncsa.hdf.hdf5lib.H5.H5Tcommitted;
import static ncsa.hdf.hdf5lib.H5.H5Tcopy;
import static ncsa.hdf.hdf5lib.H5.H5Tcreate;
import static ncsa.hdf.hdf5lib.H5.H5Tdetect_class;
import static ncsa.hdf.hdf5lib.H5.H5Tenum_create;
import static ncsa.hdf.hdf5lib.H5.H5Tenum_insert;
import static ncsa.hdf.hdf5lib.H5.H5Tequal;
import static ncsa.hdf.hdf5lib.H5.H5Tget_array_dims;
import static ncsa.hdf.hdf5lib.H5.H5Tget_array_ndims;
import static ncsa.hdf.hdf5lib.H5.H5Tget_class;
import static ncsa.hdf.hdf5lib.H5.H5Tget_member_index;
import static ncsa.hdf.hdf5lib.H5.H5Tget_member_name;
import static ncsa.hdf.hdf5lib.H5.H5Tget_member_type;
import static ncsa.hdf.hdf5lib.H5.H5Tget_native_type;
import static ncsa.hdf.hdf5lib.H5.H5Tget_nmembers;
import static ncsa.hdf.hdf5lib.H5.H5Tget_size;
import static ncsa.hdf.hdf5lib.H5.H5Tget_size_long;
import static ncsa.hdf.hdf5lib.H5.H5Tget_tag;
import static ncsa.hdf.hdf5lib.H5.H5Tis_variable_str;
import static ncsa.hdf.hdf5lib.H5.H5Topen;
import static ncsa.hdf.hdf5lib.H5.H5Tset_size;
import static ncsa.hdf.hdf5lib.H5.H5Tset_tag;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5D_CHUNKED;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5D_COMPACT;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5F_ACC_RDONLY;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5F_ACC_RDWR;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5F_ACC_TRUNC;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5F_LIBVER_LATEST;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5F_SCOPE_GLOBAL;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5O_TYPE_GROUP;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5P_DATASET_CREATE;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5P_DEFAULT;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5P_FILE_ACCESS;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5P_GROUP_CREATE;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5P_LINK_CREATE;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5S_ALL;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5S_MAX_RANK;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5S_SCALAR;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5S_SELECT_SET;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5S_UNLIMITED;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_BITFIELD;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_COMPOUND;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_C_S1;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_ENUM;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_FLOAT;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_INTEGER;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_OPAQUE;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_OPAQUE_TAG_MAX;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_STD_I16LE;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_STD_I32LE;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_STD_I8LE;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_VARIABLE;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5Z_SO_FLOAT_DSCALE;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5Z_SO_INT;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;

import ch.systemsx.cisd.base.mdarray.MDArray;
import ch.systemsx.cisd.hdf5.HDF5DataSetInformation.StorageLayout;
import ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator.FileFormat;
import ch.systemsx.cisd.hdf5.cleanup.CleanUpCallable;
import ch.systemsx.cisd.hdf5.cleanup.CleanUpRegistry;
import ch.systemsx.cisd.hdf5.cleanup.ICallableWithCleanUp;
import ch.systemsx.cisd.hdf5.cleanup.ICleanUpRegistry;

/**
 * A wrapper around {@link ncsa.hdf.hdf5lib.H5} that handles closing of resources automatically by
 * means of registering clean-up {@link Runnable}s.
 * 
 * @author Bernd Rinn
 */
class HDF5
{

    private final static int MAX_PATH_LENGTH = 16384;

    private final CleanUpCallable runner;

    private final int dataSetCreationPropertyListCompactStorageLayout;

    private final int numericConversionXferPropertyListID;

    private final int lcplCreateIntermediateGroups;

    public HDF5(final CleanUpRegistry fileRegistry, final boolean performNumericConversions)
    {
        this.runner = new CleanUpCallable();
        this.dataSetCreationPropertyListCompactStorageLayout =
                createDataSetCreationPropertyList(fileRegistry);
        H5Pset_layout(dataSetCreationPropertyListCompactStorageLayout, H5D_COMPACT);
        if (performNumericConversions)
        {
            this.numericConversionXferPropertyListID =
                    createDataSetXferPropertyListAbortOverflow(fileRegistry);
        } else
        {
            this.numericConversionXferPropertyListID =
                    createDataSetXferPropertyListAbort(fileRegistry);
        }
        this.lcplCreateIntermediateGroups = createLinkCreationPropertyList(true, fileRegistry);

    }

    private static void checkMaxLength(String path) throws HDF5JavaException
    {
        if (path.length() > MAX_PATH_LENGTH)
        {
            throw new HDF5JavaException("Path too long (length=" + path.length() + ")");
        }
    }

    //
    // File
    //

    public int createFile(String fileName, boolean useLatestFormat, ICleanUpRegistry registry)
    {
        final int fileAccessPropertyListId =
                createFileAccessPropertyListId(useLatestFormat, registry);
        final int fileId =
                H5Fcreate(fileName, H5F_ACC_TRUNC, H5P_DEFAULT, fileAccessPropertyListId);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Fclose(fileId);
                }
            });
        return fileId;
    }

    private int createFileAccessPropertyListId(boolean enforce_1_8, ICleanUpRegistry registry)
    {
        int fileAccessPropertyListId = H5P_DEFAULT;
        if (enforce_1_8)
        {
            final int fapl = H5Pcreate(H5P_FILE_ACCESS);
            registry.registerCleanUp(new Runnable()
                {
                    public void run()
                    {
                        H5Pclose(fapl);
                    }
                });
            H5Pset_libver_bounds(fapl, H5F_LIBVER_LATEST, H5F_LIBVER_LATEST);
            fileAccessPropertyListId = fapl;
        }
        return fileAccessPropertyListId;
    }

    public int openFileReadOnly(String fileName, ICleanUpRegistry registry)
    {
        final int fileId = H5Fopen(fileName, H5F_ACC_RDONLY, H5P_DEFAULT);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Fclose(fileId);
                }
            });
        return fileId;
    }

    public int openFileReadWrite(String fileName, boolean enforce_1_8, ICleanUpRegistry registry)
    {
        final int fileAccessPropertyListId = createFileAccessPropertyListId(enforce_1_8, registry);
        final File f = new File(fileName);
        if (f.exists() && f.isFile() == false)
        {
            throw new HDF5Exception("An entry with name '" + fileName
                    + "' exists but is not a file.");
        }
        final int fileId = H5Fopen(fileName, H5F_ACC_RDWR, fileAccessPropertyListId);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Fclose(fileId);
                }
            });
        return fileId;
    }

    public void flushFile(int fileId)
    {
        H5Fflush(fileId, H5F_SCOPE_GLOBAL);
    }

    //
    // Object
    //

    public int openObject(int fileId, String path, ICleanUpRegistry registry)
    {
        checkMaxLength(path);
        final int groupId = H5Oopen(fileId, path, H5P_DEFAULT);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Oclose(groupId);
                }
            });
        return groupId;
    }

    public int deleteObject(int fileId, String path)
    {
        checkMaxLength(path);
        final int success = H5Gunlink(fileId, path);
        return success;
    }

    public int copyObject(int srcFileId, String srcPath, int dstFileId, String dstPath)
    {
        checkMaxLength(srcPath);
        checkMaxLength(dstPath);
        final int success =
                H5Ocopy(srcFileId, srcPath, dstFileId, dstPath, H5P_DEFAULT, H5P_DEFAULT);
        return success;
    }

    public int moveLink(int fileId, String srcLinkPath, String dstLinkPath)
    {
        checkMaxLength(srcLinkPath);
        checkMaxLength(dstLinkPath);
        final int success =
                H5Lmove(fileId, srcLinkPath, fileId, dstLinkPath, lcplCreateIntermediateGroups,
                        H5P_DEFAULT);
        return success;
    }

    //
    // Group
    //

    public void createGroup(int fileId, String groupName)
    {
        checkMaxLength(groupName);
        final int groupId =
                H5Gcreate(fileId, groupName, lcplCreateIntermediateGroups, H5P_DEFAULT, H5P_DEFAULT);
        H5Gclose(groupId);
    }

    public void createOldStyleGroup(int fileId, String groupName, int sizeHint,
            ICleanUpRegistry registry)
    {
        checkMaxLength(groupName);
        final int gcplId = H5Pcreate(H5P_GROUP_CREATE);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Pclose(gcplId);
                }
            });
        H5Pset_local_heap_size_hint(gcplId, sizeHint);
        final int groupId =
                H5Gcreate(fileId, groupName, lcplCreateIntermediateGroups, gcplId, H5P_DEFAULT);
        H5Gclose(groupId);
    }

    public void createNewStyleGroup(int fileId, String groupName, int maxCompact, int minDense,
            ICleanUpRegistry registry)
    {
        checkMaxLength(groupName);
        final int gcplId = H5Pcreate(H5P_GROUP_CREATE);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Pclose(gcplId);
                }
            });
        H5Pset_link_phase_change(gcplId, maxCompact, minDense);
        final int groupId =
                H5Gcreate(fileId, groupName, lcplCreateIntermediateGroups, gcplId, H5P_DEFAULT);
        H5Gclose(groupId);
    }

    public int openGroup(int fileId, String path, ICleanUpRegistry registry)
    {
        checkMaxLength(path);
        final int groupId = H5Gopen(fileId, path, H5P_DEFAULT);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Gclose(groupId);
                }
            });
        return groupId;
    }

    public long getNumberOfGroupMembers(int fileId, String path, ICleanUpRegistry registry)
    {
        checkMaxLength(path);
        final int groupId = H5Gopen(fileId, path, H5P_DEFAULT);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Gclose(groupId);
                }
            });
        return H5Gget_nlinks(groupId);
    }

    public boolean existsAttribute(final int objectId, final String attributeName)
    {
        checkMaxLength(attributeName);
        return H5Aexists(objectId, attributeName);
    }

    public boolean exists(final int fileId, final String linkName)
    {
        checkMaxLength(linkName);
        return H5Lexists(fileId, linkName);
    }

    public HDF5LinkInformation getLinkInfo(final int fileId, final String objectName,
            boolean exceptionWhenNonExistent)
    {
        checkMaxLength(objectName);
        if ("/".equals(objectName))
        {
            return HDF5LinkInformation.ROOT_LINK_INFO;
        }
        final String[] lname = new String[1];
        final int typeId = H5Lget_link_info(fileId, objectName, lname, exceptionWhenNonExistent);
        return HDF5LinkInformation.create(objectName, typeId, lname[0]);
    }

    public HDF5ObjectType getLinkTypeInfo(final int fileId, final String objectName,
            boolean exceptionWhenNonExistent)
    {
        checkMaxLength(objectName);
        if ("/".equals(objectName))
        {
            return HDF5ObjectType.GROUP;
        }
        final int typeId = H5Lget_link_info(fileId, objectName, null, exceptionWhenNonExistent);
        return HDF5LinkInformation.objectTypeIdToObjectType(typeId);
    }

    public HDF5ObjectInformation getObjectInfo(final int fileId, final String objectName,
            boolean exceptionWhenNonExistent)
    {
        checkMaxLength(objectName);
        final long[] info = new long[5];
        final int typeId = H5Oget_info_by_name(fileId, objectName, info, exceptionWhenNonExistent);
        return new HDF5ObjectInformation(objectName, HDF5LinkInformation
                .objectTypeIdToObjectType(typeId), info);
    }

    public int getObjectTypeId(final int fileId, final String objectName,
            boolean exceptionWhenNonExistent)
    {
        checkMaxLength(objectName);
        if ("/".equals(objectName))
        {
            return H5O_TYPE_GROUP;
        }
        return H5Oget_info_by_name(fileId, objectName, null, exceptionWhenNonExistent);
    }

    public HDF5ObjectType getObjectTypeInfo(final int fileId, final String objectName,
            boolean exceptionWhenNonExistent)
    {
        return HDF5LinkInformation.objectTypeIdToObjectType(getObjectTypeId(fileId, objectName,
                exceptionWhenNonExistent));
    }

    public String[] getGroupMembers(final int fileId, final String groupName)
    {
        checkMaxLength(groupName);
        final ICallableWithCleanUp<String[]> dataDimensionRunnable =
                new ICallableWithCleanUp<String[]>()
                    {
                        public String[] call(ICleanUpRegistry registry)
                        {
                            final int groupId = openGroup(fileId, groupName, registry);
                            final long nLong = H5Gget_nlinks(groupId);
                            final int n = (int) nLong;
                            if (n != nLong)
                            {
                                throw new HDF5JavaException(
                                        "Number of group members is too large (n=" + nLong + ")");
                            }
                            final String[] names = new String[n];
                            H5Lget_link_names_all(groupId, ".", names);
                            return names;
                        }
                    };
        return runner.call(dataDimensionRunnable);
    }

    public List<HDF5LinkInformation> getGroupMemberLinkInfo(final int fileId,
            final String groupName, final boolean includeInternal)
    {
        checkMaxLength(groupName);
        final ICallableWithCleanUp<List<HDF5LinkInformation>> dataDimensionRunnable =
                new ICallableWithCleanUp<List<HDF5LinkInformation>>()
                    {
                        public List<HDF5LinkInformation> call(ICleanUpRegistry registry)
                        {
                            final int groupId = openGroup(fileId, groupName, registry);
                            final long nLong = H5Gget_nlinks(groupId);
                            final int n = (int) nLong;
                            if (n != nLong)
                            {
                                throw new HDF5JavaException(
                                        "Number of group members is too large (n=" + nLong + ")");
                            }
                            final String[] names = new String[n];
                            final String[] linkNames = new String[n];
                            final int[] types = new int[n];
                            H5Lget_link_info_all(groupId, ".", names, types, linkNames);
                            final String superGroupName =
                                    (groupName.equals("/") ? "/" : groupName + "/");
                            final List<HDF5LinkInformation> info =
                                    new LinkedList<HDF5LinkInformation>();
                            for (int i = 0; i < n; ++i)
                            {
                                if (includeInternal || HDF5Utils.isInternalName(names[i]) == false)
                                {
                                    info.add(HDF5LinkInformation.create(superGroupName + names[i],
                                            types[i], linkNames[i]));
                                }
                            }
                            return info;
                        }
                    };
        return runner.call(dataDimensionRunnable);
    }

    public List<HDF5LinkInformation> getGroupMemberTypeInfo(final int fileId,
            final String groupName, final boolean includeInternal)
    {
        checkMaxLength(groupName);
        final ICallableWithCleanUp<List<HDF5LinkInformation>> dataDimensionRunnable =
                new ICallableWithCleanUp<List<HDF5LinkInformation>>()
                    {
                        public List<HDF5LinkInformation> call(ICleanUpRegistry registry)
                        {
                            final int groupId = openGroup(fileId, groupName, registry);
                            final long nLong = H5Gget_nlinks(groupId);
                            final int n = (int) nLong;
                            if (n != nLong)
                            {
                                throw new HDF5JavaException(
                                        "Number of group members is too large (n=" + nLong + ")");
                            }
                            final String[] names = new String[n];
                            final int[] types = new int[n];
                            H5Lget_link_info_all(groupId, ".", names, types, null);
                            final String superGroupName =
                                    (groupName.equals("/") ? "/" : groupName + "/");
                            final List<HDF5LinkInformation> info =
                                    new LinkedList<HDF5LinkInformation>();
                            for (int i = 0; i < n; ++i)
                            {
                                if (includeInternal || HDF5Utils.isInternalName(names[i]) == false)
                                {
                                    info.add(HDF5LinkInformation.create(superGroupName + names[i],
                                            types[i], null));
                                }
                            }
                            return info;
                        }
                    };
        return runner.call(dataDimensionRunnable);
    }

    //
    // Link
    //

    public void createHardLink(int fileId, String objectName, String linkName)
    {
        checkMaxLength(objectName);
        checkMaxLength(linkName);
        H5Lcreate_hard(fileId, objectName, fileId, linkName, lcplCreateIntermediateGroups,
                H5P_DEFAULT);
    }

    public void createSoftLink(int fileId, String linkName, String targetPath)
    {
        checkMaxLength(linkName);
        checkMaxLength(targetPath);
        H5Lcreate_soft(targetPath, fileId, linkName, lcplCreateIntermediateGroups, H5P_DEFAULT);
    }

    public void createExternalLink(int fileId, String linkName, String targetFileName,
            String targetPath)
    {
        checkMaxLength(linkName);
        checkMaxLength(targetFileName);
        checkMaxLength(targetPath);
        H5Lcreate_external(targetFileName, targetPath, fileId, linkName,
                lcplCreateIntermediateGroups, H5P_DEFAULT);
    }

    //
    // Data Set
    //

    public int createDataSet(int fileId, long[] dimensions, long[] chunkSizeOrNull, int dataTypeId,
            HDF5AbstractCompression compression, String dataSetName, StorageLayout layout,
            FileFormat fileFormat, ICleanUpRegistry registry)
    {
        checkMaxLength(dataSetName);
        final int dataSpaceId =
                H5Screate_simple(dimensions.length, dimensions, createMaxDimensions(dimensions,
                        (layout == StorageLayout.CHUNKED)));
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Sclose(dataSpaceId);
                }
            });
        final int dataSetCreationPropertyListId;
        if (layout == StorageLayout.CHUNKED && chunkSizeOrNull != null)
        {
            dataSetCreationPropertyListId = createDataSetCreationPropertyList(registry);
            setChunkedLayout(dataSetCreationPropertyListId, chunkSizeOrNull);
            if (compression.isScaling())
            {
                compression.checkScalingOK(fileFormat);
                final int classTypeId = getClassType(dataTypeId);
                assert compression.isCompatibleWithDataClass(classTypeId);
                if (classTypeId == H5T_INTEGER)
                {
                    H5Pset_scaleoffset(dataSetCreationPropertyListId, H5Z_SO_INT, compression
                            .getScalingFactor());
                } else if (classTypeId == H5T_FLOAT)
                {
                    H5Pset_scaleoffset(dataSetCreationPropertyListId, H5Z_SO_FLOAT_DSCALE,
                            compression.getScalingFactor());
                }
            }
            if (compression.isDeflating())
            {
                setDeflate(dataSetCreationPropertyListId, compression.getDeflateLevel());
            }
        } else if (layout == StorageLayout.COMPACT)
        {
            dataSetCreationPropertyListId = dataSetCreationPropertyListCompactStorageLayout;
        } else
        {
            dataSetCreationPropertyListId = H5P_DEFAULT;
        }
        final int dataSetId =
                H5Dcreate(fileId, dataSetName, dataTypeId, dataSpaceId,
                        lcplCreateIntermediateGroups, dataSetCreationPropertyListId, H5P_DEFAULT);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Dclose(dataSetId);
                }
            });

        return dataSetId;
    }

    private int createDataSetCreationPropertyList(ICleanUpRegistry registry)
    {
        final int dataSetCreationPropertyListId = H5Pcreate(H5P_DATASET_CREATE);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Pclose(dataSetCreationPropertyListId);
                }
            });
        return dataSetCreationPropertyListId;
    }

    /**
     * Returns one of: COMPACT, CHUNKED, CONTIGUOUS.
     */
    public StorageLayout getLayout(int dataSetId, ICleanUpRegistry registry)
    {
        final int dataSetCreationPropertyListId = getCreationPropertyList(dataSetId, registry);
        final int layoutId = H5Pget_layout(dataSetCreationPropertyListId);
        if (layoutId == H5D_COMPACT)
        {
            return StorageLayout.COMPACT;
        } else if (layoutId == H5D_CHUNKED)
        {
            return StorageLayout.CHUNKED;
        } else
        {
            return StorageLayout.CONTIGUOUS;
        }
    }

    private int getCreationPropertyList(int dataSetId, ICleanUpRegistry registry)
    {
        final int dataSetCreationPropertyListId = H5Dget_create_plist(dataSetId);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Pclose(dataSetCreationPropertyListId);
                }
            });
        return dataSetCreationPropertyListId;
    }

    private static final long[] createMaxDimensions(long[] dimensions, boolean unlimited)
    {
        if (unlimited == false)
        {
            return dimensions;
        }
        final long[] maxDimensions = new long[dimensions.length];
        Arrays.fill(maxDimensions, H5S_UNLIMITED);
        return maxDimensions;
    }

    private void setChunkedLayout(int dscpId, long[] chunkSize)
    {
        assert dscpId >= 0;

        H5Pset_layout(dscpId, H5D_CHUNKED);
        H5Pset_chunk(dscpId, chunkSize.length, chunkSize);
    }

    private void setDeflate(int dscpId, int deflateLevel)
    {
        assert dscpId >= 0;
        assert deflateLevel >= 0;

        H5Pset_deflate(dscpId, deflateLevel);
    }

    public int createScalarDataSet(int fileId, int dataTypeId, String dataSetName,
            ICleanUpRegistry registry)
    {
        checkMaxLength(dataSetName);
        final int dataSpaceId = H5Screate(H5S_SCALAR);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Sclose(dataSpaceId);
                }
            });
        final int dataSetId =
                H5Dcreate(fileId, dataSetName, dataTypeId, dataSpaceId,
                        lcplCreateIntermediateGroups,
                        dataSetCreationPropertyListCompactStorageLayout, H5P_DEFAULT);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Dclose(dataSetId);
                }
            });
        return dataSetId;
    }

    public int openDataSet(int fileId, String path, ICleanUpRegistry registry)
    {
        checkMaxLength(path);
        final int dataSetId = H5Dopen(fileId, path, H5P_DEFAULT);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Dclose(dataSetId);
                }
            });
        return dataSetId;
    }

    public int openAndExtendDataSet(int fileId, String path, FileFormat fileFormat,
            long[] dimensions, boolean cutDownExtendIfNecessary, ICleanUpRegistry registry)
            throws HDF5JavaException
    {
        checkMaxLength(path);
        final int dataSetId = H5Dopen(fileId, path, H5P_DEFAULT);
        final long[] oldDimensions = getDataDimensions(dataSetId, registry);
        if (Arrays.equals(oldDimensions, dimensions) == false)
        {
            final StorageLayout layout = getLayout(dataSetId, registry);
            if (layout == StorageLayout.CHUNKED)
            {
                // Safety check. JHDF5 creates CHUNKED data sets always with unlimited max
                // dimensions but we may have to work on a file we haven't created.
                if (areDimensionsInBounds(dataSetId, dimensions, registry))
                {
                    final long[] newDimensions =
                            computeNewDimensions(oldDimensions, dimensions,
                                    cutDownExtendIfNecessary);
                    setDataSetExtentChunked(dataSetId, newDimensions);
                } else
                {
                    throw new HDF5JavaException("New data set dimension is out of bounds.");
                }
            } else
            {
                // CONTIGUOUS and COMPACT data sets are fixed size, thus we need to delete and
                // re-create it.
                final int dataTypeId = getDataTypeForDataSet(dataSetId, registry);
                H5Dclose(dataSetId);
                deleteObject(fileId, path);
                return createDataSet(fileId, dimensions, null, dataTypeId,
                        HDF5GenericCompression.GENERIC_NO_COMPRESSION, path, layout, fileFormat,
                        registry);
            }
        }
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Dclose(dataSetId);
                }
            });
        return dataSetId;
    }

    private long[] computeNewDimensions(long[] oldDimensions, long[] newDimensions,
            boolean cutDownExtendIfNecessary)
    {
        if (cutDownExtendIfNecessary)
        {
            return newDimensions;
        } else
        {
            final long[] newUncutDimensions = new long[oldDimensions.length];
            for (int i = 0; i < newUncutDimensions.length; ++i)
            {
                newUncutDimensions[i] = Math.max(oldDimensions[i], newDimensions[i]);
            }
            return newUncutDimensions;
        }
    }

    /**
     * Checks whether the given <var>dimensions</var> are in bounds for <var>dataSetId</var>.
     */
    private boolean areDimensionsInBounds(final int dataSetId, final long[] dimensions,
            ICleanUpRegistry registry)
    {
        final long[] maxDimensions = getDataMaxDimensions(dataSetId, registry);

        if (dimensions.length != maxDimensions.length) // Actually an error condition
        {
            return false;
        }

        for (int i = 0; i < dimensions.length; ++i)
        {
            if (maxDimensions[i] != H5S_UNLIMITED && dimensions[i] > maxDimensions[i])
            {
                return false;
            }
        }
        return true;
    }

    public void setDataSetExtentChunked(int dataSetId, long[] dimensions)
    {
        assert dataSetId >= 0;
        assert dimensions != null;

        H5Dset_extent(dataSetId, dimensions);
    }

    public void readDataSetNonNumeric(int dataSetId, int nativeDataTypeId, byte[] data)
    {
        H5Dread(dataSetId, nativeDataTypeId, H5S_ALL, H5S_ALL, H5P_DEFAULT, data);
    }

    public void readDataSetString(int dataSetId, int nativeDataTypeId, String[] data)
    {
        H5Dread_string(dataSetId, nativeDataTypeId, H5S_ALL, H5S_ALL, H5P_DEFAULT, data);
    }

    public void readDataSetString(int dataSetId, int nativeDataTypeId, int memorySpaceId,
            int fileSpaceId, String[] data)
    {
        H5Dread_string(dataSetId, nativeDataTypeId, memorySpaceId, fileSpaceId, H5P_DEFAULT, data);
    }

    public void readDataSet(int dataSetId, int nativeDataTypeId, byte[] data)
    {
        H5Dread(dataSetId, nativeDataTypeId, H5S_ALL, H5S_ALL, numericConversionXferPropertyListID,
                data);
    }

    public void readDataSet(int dataSetId, int nativeDataTypeId, short[] data)
    {
        H5Dread_short(dataSetId, nativeDataTypeId, H5S_ALL, H5S_ALL,
                numericConversionXferPropertyListID, data);
    }

    public void readDataSet(int dataSetId, int nativeDataTypeId, int[] data)
    {
        H5Dread_int(dataSetId, nativeDataTypeId, H5S_ALL, H5S_ALL,
                numericConversionXferPropertyListID, data);
    }

    public void readDataSet(int dataSetId, int nativeDataTypeId, long[] data)
    {
        H5Dread_long(dataSetId, nativeDataTypeId, H5S_ALL, H5S_ALL,
                numericConversionXferPropertyListID, data);
    }

    public void readDataSet(int dataSetId, int nativeDataTypeId, float[] data)
    {
        H5Dread_float(dataSetId, nativeDataTypeId, H5S_ALL, H5S_ALL,
                numericConversionXferPropertyListID, data);
    }

    public void readDataSet(int dataSetId, int nativeDataTypeId, double[] data)
    {
        H5Dread_double(dataSetId, nativeDataTypeId, H5S_ALL, H5S_ALL,
                numericConversionXferPropertyListID, data);
    }

    public void readDataSet(int dataSetId, int nativeDataTypeId, int memorySpaceId,
            int fileSpaceId, byte[] data)
    {
        H5Dread(dataSetId, nativeDataTypeId, memorySpaceId, fileSpaceId,
                numericConversionXferPropertyListID, data);
    }

    public void readDataSet(int dataSetId, int nativeDataTypeId, int memorySpaceId,
            int fileSpaceId, short[] data)
    {
        H5Dread_short(dataSetId, nativeDataTypeId, memorySpaceId, fileSpaceId,
                numericConversionXferPropertyListID, data);
    }

    public void readDataSet(int dataSetId, int nativeDataTypeId, int memorySpaceId,
            int fileSpaceId, int[] data)
    {
        H5Dread_int(dataSetId, nativeDataTypeId, memorySpaceId, fileSpaceId,
                numericConversionXferPropertyListID, data);
    }

    public void readDataSet(int dataSetId, int nativeDataTypeId, int memorySpaceId,
            int fileSpaceId, long[] data)
    {
        H5Dread_long(dataSetId, nativeDataTypeId, memorySpaceId, fileSpaceId,
                numericConversionXferPropertyListID, data);
    }

    public void readDataSet(int dataSetId, int nativeDataTypeId, int memorySpaceId,
            int fileSpaceId, float[] data)
    {
        H5Dread_float(dataSetId, nativeDataTypeId, memorySpaceId, fileSpaceId,
                numericConversionXferPropertyListID, data);
    }

    public void readDataSet(int dataSetId, int nativeDataTypeId, int memorySpaceId,
            int fileSpaceId, double[] data)
    {
        H5Dread_double(dataSetId, nativeDataTypeId, memorySpaceId, fileSpaceId,
                numericConversionXferPropertyListID, data);
    }

    public void readDataSetVL(int dataSetId, int dataTypeId, Object[] data)
    {
        H5DreadVL(dataSetId, dataTypeId, H5S_ALL, H5S_ALL, H5P_DEFAULT, data);
    }

    //
    // Attribute
    //

    public int createAttribute(int locationId, String attributeName, int dataTypeId,
            ICleanUpRegistry registry)
    {
        checkMaxLength(attributeName);
        final int dataSpaceId = H5Screate(H5S_SCALAR);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Sclose(dataSpaceId);
                }
            });
        final int attributeId =
                H5Acreate(locationId, attributeName, dataTypeId, dataSpaceId, H5P_DEFAULT,
                        H5P_DEFAULT);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Aclose(attributeId);
                }
            });
        return attributeId;
    }

    public int deleteAttribute(int locationId, String attributeName)
    {
        checkMaxLength(attributeName);
        final int success = H5Adelete(locationId, attributeName);
        return success;
    }

    public int openAttribute(int locationId, String attributeName, ICleanUpRegistry registry)
    {
        checkMaxLength(attributeName);
        final int attributeId = H5Aopen_name(locationId, attributeName);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Aclose(attributeId);
                }
            });
        return attributeId;
    }

    public List<String> getAttributeNames(int locationId, ICleanUpRegistry registry)
    {
        final int numberOfAttributes = H5Aget_num_attrs(locationId);
        final List<String> attributeNames = new LinkedList<String>();
        for (int i = 0; i < numberOfAttributes; ++i)
        {
            final int attributeId = H5Aopen_idx(locationId, i);
            registry.registerCleanUp(new Runnable()
                {
                    public void run()
                    {
                        H5Aclose(attributeId);
                    }
                });
            final String[] nameContainer = new String[1];
            // Find out length of attribute name.
            final long nameLength = H5Aget_name(attributeId, 0L, null);
            // Read attribute name
            final long nameLengthRead = H5Aget_name(attributeId, nameLength + 1, nameContainer);
            if (nameLengthRead != nameLength)
            {
                throw new HDF5JavaException(String.format(
                        "Error reading attribute name [wrong name length "
                                + "when reading attribute %d, expected: %d, found: %d]", i,
                        nameLength, nameLengthRead));
            }
            attributeNames.add(nameContainer[0]);
        }
        return attributeNames;
    }

    public byte[] readAttributeAsByteArray(int attributeId, int dataTypeId, int length)
    {
        final byte[] data = new byte[length];
        H5Aread(attributeId, dataTypeId, data);
        return data;
    }

    public void readAttributeVL(int attributeId, int dataTypeId, String[] data)
    {
        H5AreadVL(attributeId, dataTypeId, data);
    }

    public void writeAttribute(int attributeId, int dataTypeId, byte[] value)
    {
        H5Awrite(attributeId, dataTypeId, value);
    }

    //
    // Data Type
    //

    public int copyDataType(int dataTypeId, ICleanUpRegistry registry)
    {
        final int copiedDataTypeId = H5Tcopy(dataTypeId);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Tclose(copiedDataTypeId);
                }
            });
        return copiedDataTypeId;
    }

    public int createDataTypeVariableString(ICleanUpRegistry registry)
    {
        final int dataTypeId = createDataTypeStringVariableLength();
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Tclose(dataTypeId);
                }
            });
        return dataTypeId;
    }

    private int createDataTypeStringVariableLength()
    {
        int dataTypeId = H5Tcopy(H5T_C_S1);
        H5Tset_size(dataTypeId, H5T_VARIABLE);
        return dataTypeId;
    }

    public int createDataTypeString(int length, ICleanUpRegistry registry)
    {
        final int dataTypeId = createDataTypeString(length);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Tclose(dataTypeId);
                }
            });
        return dataTypeId;
    }

    private int createDataTypeString(int length)
    {
        assert length >= 0;

        int dataTypeId = H5Tcopy(H5T_C_S1);
        H5Tset_size(dataTypeId, length);
        return dataTypeId;
    }

    public int createArrayType(int baseTypeId, int length, ICleanUpRegistry registry)
    {
        final int dataTypeId = H5Tarray_create(baseTypeId, 1, new int[]
            { length });
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Tclose(dataTypeId);
                }
            });
        return dataTypeId;
    }

    private enum EnumSize
    {
        BYTE8, SHORT16, INT32
    }

    public int createDataTypeEnum(String[] names, ICleanUpRegistry registry)
    {
        for (String name : names)
        {
            checkMaxLength(name);
        }
        final EnumSize size =
                (names.length < Byte.MAX_VALUE) ? EnumSize.BYTE8
                        : (names.length < Short.MAX_VALUE) ? EnumSize.SHORT16 : EnumSize.INT32;
        final int baseDataTypeId;
        switch (size)
        {
            case BYTE8:
                baseDataTypeId = H5T_STD_I8LE;
                break;
            case SHORT16:
                baseDataTypeId = H5T_STD_I16LE;
                break;
            case INT32:
                baseDataTypeId = H5T_STD_I32LE;
                break;
            default:
                throw new InternalError();
        }
        final int dataTypeId = H5Tenum_create(baseDataTypeId);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Tclose(dataTypeId);
                }
            });
        switch (size)
        {
            case BYTE8:
                for (byte i = 0; i < names.length; ++i)
                {
                    insertMemberEnum(dataTypeId, names[i], i);
                }
                break;
            case SHORT16:
            {
                final short[] values = getLittleEndianSuccessiveShortValues(names);
                for (short i = 0; i < names.length; ++i)
                {
                    insertMemberEnum(dataTypeId, names[i], values[i]);
                }
                break;
            }
            case INT32:
            {
                final int[] values = getLittleEndianSuccessiveIntValues(names);
                for (int i = 0; i < names.length; ++i)
                {
                    insertMemberEnum(dataTypeId, names[i], values[i]);
                }
                break;
            }
        }
        return dataTypeId;
    }

    private short[] getLittleEndianSuccessiveShortValues(String[] names)
    {
        final short[] values = new short[names.length];
        for (short i = 0; i < names.length; ++i)
        {
            values[i] = i;
        }
        H5.H5Tconvert_to_little_endian(values);
        return values;
    }

    private int[] getLittleEndianSuccessiveIntValues(String[] names)
    {
        final int[] values = new int[names.length];
        for (short i = 0; i < names.length; ++i)
        {
            values[i] = i;
        }
        H5.H5Tconvert_to_little_endian(values);
        return values;
    }

    private void insertMemberEnum(int dataTypeId, String name, byte value)
    {
        assert dataTypeId >= 0;
        assert name != null;

        H5Tenum_insert(dataTypeId, name, value);
    }

    private void insertMemberEnum(int dataTypeId, String name, short value)
    {
        assert dataTypeId >= 0;
        assert name != null;

        H5Tenum_insert(dataTypeId, name, value);
    }

    private void insertMemberEnum(int dataTypeId, String name, int value)
    {
        assert dataTypeId >= 0;
        assert name != null;

        H5Tenum_insert(dataTypeId, name, value);
    }

    /** Returns the number of members of an enum type or a compound type. */
    public int getNumberOfMembers(int dataTypeId)
    {
        return H5Tget_nmembers(dataTypeId);
    }

    /**
     * Returns the name of an enum value or compound member for the given <var>index</var>.
     * <p>
     * Must not be called on a <var>dateTypeId</var> that is not an enum or compound type.
     */
    public String getNameForEnumOrCompoundMemberIndex(int dataTypeId, int index)
    {
        return H5Tget_member_name(dataTypeId, index);
    }

    /**
     * Returns the names of an enum value or compound members.
     * <p>
     * Must not be called on a <var>dateTypeId</var> that is not an enum or compound type.
     */
    public String[] getNamesForEnumOrCompoundMembers(int dataTypeId)
    {
        final int len = getNumberOfMembers(dataTypeId);
        final String[] values = new String[len];
        for (int i = 0; i < len; ++i)
        {
            values[i] = H5Tget_member_name(dataTypeId, i);
        }
        return values;
    }

    /**
     * Returns the index of an enum value or compound member for the given <var>name</var>. Works on
     * enum and compound data types.
     */
    public int getIndexForMemberName(int dataTypeId, String name)
    {
        checkMaxLength(name);
        return H5Tget_member_index(dataTypeId, name);
    }

    /**
     * Returns the data type id for a member of a compound data type, specified by index.
     */
    public int getDataTypeForIndex(int compoundDataTypeId, int index, ICleanUpRegistry registry)
    {
        final int memberTypeId = H5Tget_member_type(compoundDataTypeId, index);
        registry.registerCleanUp(new Runnable()
            {

                public void run()
                {
                    H5Tclose(memberTypeId);
                }
            });
        return memberTypeId;
    }

    /**
     * Returns the data type id for a member of a compound data type, specified by name.
     */
    public int getDataTypeForMemberName(int compoundDataTypeId, String memberName)
    {
        checkMaxLength(memberName);
        final int index = H5Tget_member_index(compoundDataTypeId, memberName);
        return H5Tget_member_type(compoundDataTypeId, index);
    }

    public Boolean tryGetBooleanValue(final int dataTypeId, final int intValue)
    {
        if (getClassType(dataTypeId) != H5T_ENUM)
        {
            return null;
        }
        final String value = getNameForEnumOrCompoundMemberIndex(dataTypeId, intValue);
        if ("TRUE".equalsIgnoreCase(value))
        {
            return true;
        } else if ("FALSE".equalsIgnoreCase(value))
        {
            return false;
        } else
        {
            return null;
        }
    }

    public int createDataTypeCompound(int lengthInBytes, ICleanUpRegistry registry)
    {
        final int dataTypeId = H5Tcreate(H5T_COMPOUND, lengthInBytes);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Tclose(dataTypeId);
                }
            });
        return dataTypeId;
    }

    public int createDataTypeOpaque(int lengthInBytes, String tag, ICleanUpRegistry registry)
    {
        checkMaxLength(tag);
        final int dataTypeId = H5Tcreate(H5T_OPAQUE, lengthInBytes);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Tclose(dataTypeId);
                }
            });
        H5Tset_tag(dataTypeId, tag.length() > H5T_OPAQUE_TAG_MAX ? tag.substring(0,
                H5T_OPAQUE_TAG_MAX) : tag);
        return dataTypeId;
    }

    public void commitDataType(int fileId, String name, int dataTypeId)
    {
        checkMaxLength(name);
        H5Tcommit(fileId, name, dataTypeId, lcplCreateIntermediateGroups, H5P_DEFAULT, H5P_DEFAULT);
    }

    public int openDataType(int fileId, String name, ICleanUpRegistry registry)
    {
        checkMaxLength(name);
        final int dataTypeId = H5Topen(fileId, name, H5P_DEFAULT);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Tclose(dataTypeId);
                }
            });
        return dataTypeId;
    }

    public boolean dataTypesAreEqual(int dataTypeId1, int dataTypeId2)
    {
        return H5Tequal(dataTypeId1, dataTypeId2);
    }

    public int getDataTypeForDataSet(int dataSetId, ICleanUpRegistry registry)
    {
        final int dataTypeId = H5Dget_type(dataSetId);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Tclose(dataTypeId);
                }
            });
        return dataTypeId;
    }

    public int getDataTypeForAttribute(int attributeId, ICleanUpRegistry registry)
    {
        final int dataTypeId = H5Aget_type(attributeId);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Tclose(dataTypeId);
                }
            });
        return dataTypeId;
    }

    public String tryGetOpaqueTag(int dataTypeId)
    {
        return H5Tget_tag(dataTypeId);
    }

    /**
     * Use this if <var>dataTypeId</var> can possibly be a bit field.
     */
    public int getNativeDataTypeCheckForBitField(int dataTypeId, ICleanUpRegistry registry)
    {
        // Workaround: calling H5Tget_native_type() on one of the bit field types in 1.8.2 throws an
        // "HDF5FunctionArgumentException: Invalid arguments to routine: Inappropriate type"
        if (H5Tdetect_class(dataTypeId, H5T_BITFIELD))
        {
            return dataTypeId;
        } else
        {
            return getNativeDataType(dataTypeId, registry);
        }
    }

    public int getNativeDataType(int dataTypeId, ICleanUpRegistry registry)
    {
        final int nativeDataTypeId = H5Tget_native_type(dataTypeId);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Tclose(nativeDataTypeId);
                }
            });
        return nativeDataTypeId;
    }

    public int getNativeDataTypeForDataSet(int dataSetId, ICleanUpRegistry registry)
    {
        final int dataTypeId = H5Dget_type(dataSetId);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Tclose(dataTypeId);
                }
            });
        return getNativeDataType(dataTypeId, registry);
    }

    public int getNativeDataTypeForDataSetCheckBitFields(int dataSetId, ICleanUpRegistry registry)
    {
        final int dataTypeId = H5Dget_type(dataSetId);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Tclose(dataTypeId);
                }
            });
        return getNativeDataTypeCheckForBitField(dataTypeId, registry);
    }

    public int getNativeDataTypeForAttribute(int attributeId, ICleanUpRegistry registry)
    {
        final int dataTypeId = H5Aget_type(attributeId);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Tclose(dataTypeId);
                }
            });
        return getNativeDataType(dataTypeId, registry);
    }

    public int getDataTypeSize(int dataTypeId)
    {
        return H5Tget_size(dataTypeId);
    }

    public long getDataTypeSizeLong(int dataTypeId) throws HDF5JavaException
    {
        return H5Tget_size_long(dataTypeId);
    }

    public boolean isVariableLengthString(int dataTypeId)
    {
        return H5Tis_variable_str(dataTypeId);
    }

    public int getClassType(int dataTypeId)
    {
        return H5Tget_class(dataTypeId);
    }
    
    public boolean hasClassType(int dataTypeId, int classTypeId)
    {
        return H5Tdetect_class(dataTypeId, classTypeId);
    }

    public String tryGetDataTypePath(int dataTypeId)
    {
        boolean isCommitted = H5Tcommitted(dataTypeId);
        if (isCommitted)
        {
            final String[] result = new String[1];
            final long len = H5Iget_name(dataTypeId, result, 32);
            if (len >= result[0].length())
            {
                final String[] finalResult = new String[1];
                H5Iget_name(dataTypeId, finalResult, len + 1);
                return finalResult[0];
            } else
            {
                return result[0];
            }
        } else
        {
            return null;
        }
    }

    //
    // Data Space
    //

    public int getDataSpaceForDataSet(int dataSetId, ICleanUpRegistry registry)
    {
        final int dataTypeId = H5Dget_space(dataSetId);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Sclose(dataTypeId);
                }
            });
        return dataTypeId;
    }

    public long[] getDataDimensions(final int dataSetId)
    {
        ICallableWithCleanUp<long[]> dataDimensionRunnable = new ICallableWithCleanUp<long[]>()
            {
                public long[] call(ICleanUpRegistry registry)
                {
                    final int dataSpaceId = H5Dget_space(dataSetId);
                    registry.registerCleanUp(new Runnable()
                        {
                            public void run()
                            {
                                H5Sclose(dataSpaceId);
                            }
                        });
                    final long[] dimensions = getDataSpaceDimensions(dataSpaceId);
                    return dimensions;
                }
            };
        return runner.call(dataDimensionRunnable);
    }

    public long[] getDataDimensionsForAttribute(final int atrributeId, ICleanUpRegistry registry)
    {
        final int dataSpaceId = H5Aget_space(atrributeId);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Sclose(dataSpaceId);
                }
            });
        final long[] dimensions = getDataSpaceDimensions(dataSpaceId);
        return dimensions;
    }

    public long[] getDataDimensions(final int dataSetId, ICleanUpRegistry registry)
    {
        final int dataSpaceId = H5Dget_space(dataSetId);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Sclose(dataSpaceId);
                }
            });
        final long[] dimensions = getDataSpaceDimensions(dataSpaceId);
        return dimensions;
    }

    public long[] getDataMaxDimensions(final int dataSetId)
    {
        ICallableWithCleanUp<long[]> dataDimensionRunnable = new ICallableWithCleanUp<long[]>()
            {
                public long[] call(ICleanUpRegistry registry)
                {
                    return getDataMaxDimensions(dataSetId, registry);
                }

            };
        return runner.call(dataDimensionRunnable);
    }

    private long[] getDataMaxDimensions(final int dataSetId, ICleanUpRegistry registry)
    {
        final int dataSpaceId = H5Dget_space(dataSetId);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Sclose(dataSpaceId);
                }
            });
        final long[] dimensions = getDataSpaceMaxDimensions(dataSpaceId);
        return dimensions;
    }

    public long[] getDataSpaceDimensions(int dataSpaceId)
    {
        final int rank = H5Sget_simple_extent_ndims(dataSpaceId);
        return getDataSpaceDimensions(dataSpaceId, rank);
    }

    public long[] getDataSpaceDimensions(int dataSpaceId, int rank)
    {
        assert dataSpaceId >= 0;
        assert rank >= 0;

        final long[] dimensions = new long[rank];
        H5Sget_simple_extent_dims(dataSpaceId, dimensions, null);
        return dimensions;
    }

    public long[] getDataSpaceMaxDimensions(int dataSpaceId)
    {
        final int rank = H5Sget_simple_extent_ndims(dataSpaceId);
        return getDataSpaceMaxDimensions(dataSpaceId, rank);
    }

    public long[] getDataSpaceMaxDimensions(int dataSpaceId, int rank)
    {
        assert dataSpaceId >= 0;
        assert rank >= 0;

        final long[] maxDimensions = new long[rank];
        H5Sget_simple_extent_dims(dataSpaceId, null, maxDimensions);
        return maxDimensions;
    }

    /**
     * @param dataSetOrAttributeId The id of either the data set or the attribute to get the
     *            dimensions for.
     * @param isAttribute If <code>true</code>, <var>dataSetOrAttributeId</var> will be interpreted
     *            as an attribute, otherwise as a data set.
     * @param dataSetInfo The info object to fill.
     */
    public void fillDataDimensions(final int dataSetOrAttributeId, final boolean isAttribute,
            final HDF5DataSetInformation dataSetInfo)
    {
        ICallableWithCleanUp<Object> dataDimensionRunnable = new ICallableWithCleanUp<Object>()
            {
                public long[][] call(ICleanUpRegistry registry)
                {
                    final int dataSpaceId =
                            isAttribute ? H5Aget_space(dataSetOrAttributeId)
                                    : H5Dget_space(dataSetOrAttributeId);
                    registry.registerCleanUp(new Runnable()
                        {
                            public void run()
                            {
                                H5Sclose(dataSpaceId);
                            }
                        });
                    final long[] dimensions = new long[H5S_MAX_RANK];
                    final long[] maxDimensions = new long[H5S_MAX_RANK];
                    final int rank =
                            H5Sget_simple_extent_dims(dataSpaceId, dimensions, maxDimensions);
                    final long[] realDimensions = new long[rank];
                    System.arraycopy(dimensions, 0, realDimensions, 0, rank);
                    final long[] realMaxDimensions = new long[rank];
                    System.arraycopy(maxDimensions, 0, realMaxDimensions, 0, rank);
                    dataSetInfo.setDimensions(realDimensions);
                    dataSetInfo.setMaxDimensions(realMaxDimensions);
                    if (isAttribute == false)
                    {
                        final long[] chunkSizes = new long[rank];
                        final int creationPropertyList =
                                getCreationPropertyList(dataSetOrAttributeId, registry);
                        final StorageLayout layout =
                                StorageLayout.fromId(H5Pget_layout(creationPropertyList));
                        dataSetInfo.setStorageLayout(layout);
                        if (layout == StorageLayout.CHUNKED)
                        {
                            H5Pget_chunk(creationPropertyList, rank, chunkSizes);
                            dataSetInfo.setChunkSizes(MDArray.toInt(chunkSizes));
                        }
                    }
                    return null; // Nothing to return
                }
            };
        runner.call(dataDimensionRunnable);
    }

    public int[] getArrayDimensions(int arrayTypeId)
    {
        final int rank = H5Tget_array_ndims(arrayTypeId);
        final int[] dims = new int[rank];
        H5Tget_array_dims(arrayTypeId, dims);
        return dims;
    }

    public int createSimpleDataSpace(long[] dimensions, ICleanUpRegistry registry)
    {
        final int dataSpaceId = H5Screate_simple(dimensions.length, dimensions, null);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Sclose(dataSpaceId);
                }
            });
        return dataSpaceId;
    }

    public void setHyperslabBlock(int dataSpaceId, long[] start, long[] count)
    {
        assert dataSpaceId >= 0;
        assert start != null;
        assert count != null;

        H5Sselect_hyperslab(dataSpaceId, H5S_SELECT_SET, start, null, count, null);
    }

    //
    // Properties
    //

    private int createLinkCreationPropertyList(boolean createIntermediateGroups,
            ICleanUpRegistry registry)
    {
        final int linkCreationPropertyList = H5Pcreate(H5P_LINK_CREATE);
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Pclose(linkCreationPropertyList);
                }
            });
        if (createIntermediateGroups)
        {
            H5Pset_create_intermediate_group(linkCreationPropertyList, true);
        }
        return linkCreationPropertyList;
    }

    private int createDataSetXferPropertyListAbortOverflow(ICleanUpRegistry registry)
    {
        final int datasetXferPropertyList = H5Pcreate_xfer_abort_overflow();
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Pclose(datasetXferPropertyList);
                }
            });
        return datasetXferPropertyList;
    }

    private int createDataSetXferPropertyListAbort(ICleanUpRegistry registry)
    {
        final int datasetXferPropertyList = H5Pcreate_xfer_abort();
        registry.registerCleanUp(new Runnable()
            {
                public void run()
                {
                    H5Pclose(datasetXferPropertyList);
                }
            });
        return datasetXferPropertyList;
    }

}
