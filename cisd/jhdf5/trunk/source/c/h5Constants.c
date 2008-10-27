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
#ifdef __cplusplus
extern "C" {
#endif

#include "hdf5.h"
#include "h5Constants.h"
#include <jni.h>

/*
 * Class:     ncsa_hdf_hdf5lib_H5Header converts Java constants defined
 *            at ncsa.hdf.hdf5lib.HDF5Constants.java to HDF5 runtime global variables.
 * Method:    J2c
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_ncsa_hdf_hdf5lib_H5_J2C
  (JNIEnv *env, jclass clss, jint java_constant)
{
    switch (java_constant)
    {
        case JH5_SZIP_MAX_PIXELS_PER_BLOCK  : return  H5_SZIP_MAX_PIXELS_PER_BLOCK;
        case JH5_SZIP_NN_OPTION_MASK  : return  H5_SZIP_NN_OPTION_MASK;
        case JH5_SZIP_EC_OPTION_MASK  : return  H5_SZIP_EC_OPTION_MASK;
        case JH5_SZIP_ALLOW_K13_OPTION_MASK  : return  H5_SZIP_ALLOW_K13_OPTION_MASK;
        case JH5_SZIP_CHIP_OPTION_MASK  : return  H5_SZIP_CHIP_OPTION_MASK;
        case JH5D_ALLOC_TIME_DEFAULT  : return  H5D_ALLOC_TIME_DEFAULT;
        case JH5D_ALLOC_TIME_EARLY  : return  H5D_ALLOC_TIME_EARLY;
        case JH5D_ALLOC_TIME_ERROR  : return  H5D_ALLOC_TIME_ERROR;
        case JH5D_ALLOC_TIME_INCR  : return  H5D_ALLOC_TIME_INCR;
        case JH5D_ALLOC_TIME_LATE  : return  H5D_ALLOC_TIME_LATE;
        case JH5D_CHUNKED  : return  H5D_CHUNKED;
        case JH5D_COMPACT  : return  H5D_COMPACT;
        case JH5D_CONTIGUOUS  : return  H5D_CONTIGUOUS;
        case JH5D_FILL_TIME_ALLOC  : return  H5D_FILL_TIME_ALLOC;
        case JH5D_FILL_TIME_ERROR  : return  H5D_FILL_TIME_ERROR;
        case JH5D_FILL_TIME_NEVER  : return  H5D_FILL_TIME_NEVER;
        case JH5D_FILL_VALUE_DEFAULT  : return  H5D_FILL_VALUE_DEFAULT;
        case JH5D_FILL_VALUE_ERROR  : return  H5D_FILL_VALUE_ERROR;
        case JH5D_FILL_VALUE_UNDEFINED  : return  H5D_FILL_VALUE_UNDEFINED;
        case JH5D_FILL_VALUE_USER_DEFINED  : return  H5D_FILL_VALUE_USER_DEFINED;
        case JH5D_LAYOUT_ERROR  : return  H5D_LAYOUT_ERROR;
        case JH5D_NLAYOUTS  : return  H5D_NLAYOUTS;
        case JH5D_SPACE_STATUS_ALLOCATED  : return  H5D_SPACE_STATUS_ALLOCATED;
        case JH5D_SPACE_STATUS_ERROR  : return  H5D_SPACE_STATUS_ERROR;
        case JH5D_SPACE_STATUS_NOT_ALLOCATED  : return  H5D_SPACE_STATUS_NOT_ALLOCATED;
        case JH5D_SPACE_STATUS_PART_ALLOCATED  : return  H5D_SPACE_STATUS_PART_ALLOCATED;
        case JH5E_ALIGNMENT  : return  H5E_ALIGNMENT;
        case JH5E_ALREADYEXISTS  : return  H5E_ALREADYEXISTS;
        case JH5E_ALREADYINIT  : return  H5E_ALREADYINIT;
        case JH5E_ARGS  : return  H5E_ARGS;
        case JH5E_ATOM  : return  H5E_ATOM;
        case JH5E_ATTR  : return  H5E_ATTR;
        case JH5E_BADATOM  : return  H5E_BADATOM;
        case JH5E_BADFILE  : return  H5E_BADFILE;
        case JH5E_BADGROUP  : return  H5E_BADGROUP;
        case JH5E_BADMESG  : return  H5E_BADMESG;
        case JH5E_BADRANGE  : return  H5E_BADRANGE;
        case JH5E_BADSELECT  : return  H5E_BADSELECT;
        case JH5E_BADSIZE  : return  H5E_BADSIZE;
        case JH5E_BADTYPE  : return  H5E_BADTYPE;
        case JH5E_BADVALUE  : return  H5E_BADVALUE;
        case JH5E_BTREE  : return  H5E_BTREE;
        case JH5E_CACHE  : return  H5E_CACHE;
        case JH5E_CALLBACK  : return  H5E_CALLBACK;
        case JH5E_CANAPPLY  : return  H5E_CANAPPLY;
        /*case JH5E_CANTALLOC  : return  H5E_CANTALLOC; 
        case JH5E_CANTCHANGE  : return  H5E_CANTCHANGE; removed from 1.6.4*/
        case JH5E_CANTCLIP  : return  H5E_CANTCLIP;
        case JH5E_CANTCLOSEFILE  : return  H5E_CANTCLOSEFILE;
        case JH5E_CANTCONVERT  : return  H5E_CANTCONVERT;
        case JH5E_CANTCOPY  : return  H5E_CANTCOPY;
        case JH5E_CANTCOUNT  : return  H5E_CANTCOUNT;
        case JH5E_CANTCREATE  : return  H5E_CANTCREATE;
        case JH5E_CANTDEC  : return  H5E_CANTDEC;
        case JH5E_CANTDECODE  : return  H5E_CANTDECODE;
        case JH5E_CANTDELETE  : return  H5E_CANTDELETE;
        case JH5E_CANTENCODE  : return  H5E_CANTENCODE;
        case JH5E_CANTFLUSH  : return  H5E_CANTFLUSH;
        case JH5E_CANTFREE  : return  H5E_CANTFREE;
        case JH5E_CANTGET  : return  H5E_CANTGET;
        case JH5E_CANTINC  : return  H5E_CANTINC;
        case JH5E_CANTINIT  : return  H5E_CANTINIT;
        case JH5E_CANTINSERT  : return  H5E_CANTINSERT;
        case JH5E_CANTLIST  : return  H5E_CANTLIST;
        case JH5E_CANTLOAD  : return  H5E_CANTLOAD;
        case JH5E_CANTLOCK  : return  H5E_CANTLOCK;
        /*case JH5E_CANTMAKETREE  : return  H5E_CANTMAKETREE; removed from 1.8.0*/
        case JH5E_CANTNEXT  : return  H5E_CANTNEXT;
        case JH5E_CANTOPENFILE  : return  H5E_CANTOPENFILE;
        case JH5E_CANTOPENOBJ  : return  H5E_CANTOPENOBJ;
        /* case JH5E_CANTRECV  : return  H5E_CANTRECV; removed from 1.6.4*/
        case JH5E_CANTREGISTER  : return  H5E_CANTREGISTER;
        case JH5E_CANTRELEASE  : return  H5E_CANTRELEASE;
        case JH5E_CANTSELECT  : return  H5E_CANTSELECT;
        /* case JH5E_CANTSENDMDATA  : return  H5E_CANTSENDMDATA; removed from 1.6.4*/
        case JH5E_CANTSET  : return  H5E_CANTSET;
        case JH5E_CANTSPLIT  : return  H5E_CANTSPLIT;
        case JH5E_CANTUNLOCK  : return  H5E_CANTUNLOCK;
        case JH5E_CLOSEERROR  : return  H5E_CLOSEERROR;
        case JH5E_COMPLEN  : return  H5E_COMPLEN;
/* removed from HDF5 1.6.5
        case JH5E_CWG  : return  H5E_CWG;
*/
        case JH5E_DATASET  : return  H5E_DATASET;
        case JH5E_DATASPACE  : return  H5E_DATASPACE;
        case JH5E_DATATYPE  : return  H5E_DATATYPE;
        case JH5E_DUPCLASS  : return  H5E_DUPCLASS;
        case JH5E_EFL  : return  H5E_EFL;
        case JH5E_EXISTS  : return  H5E_EXISTS;
        case JH5E_FCNTL  : return  H5E_FCNTL;
        case JH5E_FILE  : return  H5E_FILE;
        case JH5E_FILEEXISTS  : return  H5E_FILEEXISTS;
        case JH5E_FILEOPEN  : return  H5E_FILEOPEN;
        /* case JH5E_FPHDF5  : return  H5E_FPHDF5; removed from 1.6.4*/
        case JH5E_FUNC  : return  H5E_FUNC;
        case JH5E_HEAP  : return  H5E_HEAP;
        case JH5E_INTERNAL  : return  H5E_INTERNAL;
        case JH5E_IO  : return  H5E_IO;
        case JH5E_LINK  : return  H5E_LINK;
        case JH5E_LINKCOUNT  : return  H5E_LINKCOUNT;
        case JH5E_MOUNT  : return  H5E_MOUNT;
        case JH5E_MPI  : return  H5E_MPI;
        case JH5E_MPIERRSTR  : return  H5E_MPIERRSTR;
        case JH5E_NOFILTER  : return  H5E_NOFILTER;
        case JH5E_NOIDS  : return  H5E_NOIDS;
        case JH5E_NONE_MAJOR  : return  H5E_NONE_MAJOR;
        case JH5E_NONE_MINOR  : return  H5E_NONE_MINOR;
        case JH5E_NOSPACE  : return  H5E_NOSPACE;
        case JH5E_NOTCACHED  : return  H5E_NOTCACHED;
        case JH5E_NOTFOUND  : return  H5E_NOTFOUND;
        case JH5E_NOTHDF5  : return  H5E_NOTHDF5;
        case JH5E_OHDR  : return  H5E_OHDR;
        case JH5E_OVERFLOW  : return  H5E_OVERFLOW;
        case JH5E_PLINE  : return  H5E_PLINE;
        case JH5E_PLIST  : return  H5E_PLIST;
        case JH5E_PROTECT  : return  H5E_PROTECT;
        case JH5E_READERROR  : return  H5E_READERROR;
        case JH5E_REFERENCE  : return  H5E_REFERENCE;
        case JH5E_RESOURCE  : return  H5E_RESOURCE;
        case JH5E_RS  : return  H5E_RS;
        case JH5E_SEEKERROR  : return  H5E_SEEKERROR;
        case JH5E_SETLOCAL  : return  H5E_SETLOCAL;
        /*case JH5E_SLINK  : return  H5E_SLINK; removed from 1.8.0*/
        case JH5E_STORAGE  : return  H5E_STORAGE;
        case JH5E_SYM  : return  H5E_SYM;
        /*case JH5E_TBBT  : return  H5E_TBBT; removed from 1.8.0*/
        case JH5E_TRUNCATED  : return  H5E_TRUNCATED;
        case JH5E_TST  : return  H5E_TST;
        case JH5E_UNINITIALIZED  : return  H5E_UNINITIALIZED;
        case JH5E_UNSUPPORTED  : return  H5E_UNSUPPORTED;
        case JH5E_VERSION  : return  H5E_VERSION;
        case JH5E_VFL  : return  H5E_VFL;
        case JH5E_WALK_DOWNWARD  : return  H5E_WALK_DOWNWARD;
        case JH5E_WALK_UPWARD  : return  H5E_WALK_UPWARD;
        case JH5E_WRITEERROR  : return  H5E_WRITEERROR;
        case JH5F_ACC_CREAT  : return  H5F_ACC_CREAT;
        case JH5F_ACC_DEBUG  : return  H5F_ACC_DEBUG;
        case JH5F_ACC_EXCL  : return  H5F_ACC_EXCL;
        case JH5F_ACC_RDONLY  : return  H5F_ACC_RDONLY;
        case JH5F_ACC_RDWR  : return  H5F_ACC_RDWR;
        case JH5F_ACC_TRUNC  : return  H5F_ACC_TRUNC;
        case JH5F_CLOSE_DEFAULT  : return  H5F_CLOSE_DEFAULT;
        case JH5F_CLOSE_SEMI  : return  H5F_CLOSE_SEMI;
        case JH5F_CLOSE_STRONG  : return  H5F_CLOSE_STRONG;
        case JH5F_CLOSE_WEAK  : return  H5F_CLOSE_WEAK;
        case JH5F_OBJ_ALL  : return  H5F_OBJ_ALL;
        case JH5F_OBJ_DATASET  : return  H5F_OBJ_DATASET;
        case JH5F_OBJ_DATATYPE  : return  H5F_OBJ_DATATYPE;
        case JH5F_OBJ_FILE  : return  H5F_OBJ_FILE;
        case JH5F_OBJ_ATTR  : return  H5F_OBJ_ATTR;
        case JH5F_OBJ_GROUP  : return  H5F_OBJ_GROUP;
        case JH5F_SCOPE_DOWN  : return  H5F_SCOPE_DOWN;
        case JH5F_SCOPE_GLOBAL  : return  H5F_SCOPE_GLOBAL;
        case JH5F_SCOPE_LOCAL  : return  H5F_SCOPE_LOCAL;
        case JH5F_UNLIMITED  : return  (int)H5F_UNLIMITED;
	case JH5F_LIBVER_EARLIEST  : return H5F_LIBVER_EARLIEST;
	case JH5F_LIBVER_LATEST  : return H5F_LIBVER_LATEST;
        case JH5G_DATASET  : return  H5G_DATASET;
        case JH5G_GROUP  : return  H5G_GROUP;
        case JH5G_LINK  : return  H5G_LINK;
        case JH5G_LINK_ERROR  : return  H5G_LINK_ERROR;
        case JH5G_LINK_HARD  : return  H5G_LINK_HARD;
        case JH5G_LINK_SOFT  : return  H5G_LINK_SOFT;
        case JH5G_NLIBTYPES  : return  H5G_NLIBTYPES;
        case JH5G_NTYPES  : return  H5G_NTYPES;
        case JH5G_NUSERTYPES  : return  H5G_NUSERTYPES;
        /*case JH5G_RESERVED_4  : return  H5G_RESERVED_4; removed from 1.8*/
        case JH5G_RESERVED_5  : return  H5G_RESERVED_5;
        case JH5G_RESERVED_6  : return  H5G_RESERVED_6;
        case JH5G_RESERVED_7  : return  H5G_RESERVED_7;
        case JH5G_SAME_LOC  : return  H5G_SAME_LOC;
        case JH5G_TYPE  : return  H5G_TYPE;
        case JH5G_UNKNOWN  : return  H5G_UNKNOWN;
        /*case JH5G_USERTYPE  : return  H5G_USERTYPE;*/
        case JH5I_ATTR  : return  H5I_ATTR;
        case JH5I_BADID  : return  H5I_BADID;
        case JH5I_DATASET  : return  H5I_DATASET;
        case JH5I_DATASPACE  : return  H5I_DATASPACE;
        case JH5I_DATATYPE  : return  H5I_DATATYPE;
        case JH5I_FILE  : return  H5I_FILE;
/* removed from HDF5 1.6.5
        case JH5I_FILE_CLOSING  : return  H5I_FILE_CLOSING;
*/
        case JH5I_GENPROP_CLS  : return  H5I_GENPROP_CLS;
        case JH5I_GENPROP_LST  : return  H5I_GENPROP_LST;
        case JH5I_GROUP  : return  H5I_GROUP;
        case JH5I_INVALID_HID  : return  H5I_INVALID_HID;
        /*case JH5I_NGROUPS  : return  H5I_NGROUPS; removed from 1.8*/
        case JH5I_REFERENCE  : return  H5I_REFERENCE;
        /* case JH5I_TEMPBUF  : return  H5I_TEMPBUF; removed from 1.6.4*/
        case JH5I_VFL  : return  H5I_VFL;
        case JH5O_TYPE_UNKNOWN : return H5O_TYPE_UNKNOWN;
        case JH5O_TYPE_GROUP : return H5O_TYPE_GROUP;
        case JH5O_TYPE_DATASET : return H5O_TYPE_DATASET;
        case JH5O_TYPE_NAMED_DATATYPE : return H5O_TYPE_NAMED_DATATYPE;
        case JH5O_TYPE_NTYPES : return H5O_TYPE_NTYPES;
        case JH5L_TYPE_ERROR : return H5L_TYPE_ERROR;
        case JH5L_TYPE_HARD : return H5L_TYPE_HARD;
        case JH5L_TYPE_SOFT : return H5L_TYPE_SOFT;
        case JH5L_TYPE_EXTERNAL : return H5L_TYPE_EXTERNAL;
        case JH5L_TYPE_MAX : return H5L_TYPE_MAX;
        case JH5P_DATASET_CREATE  : return  H5P_DATASET_CREATE;
        case JH5P_DATASET_CREATE_DEFAULT  : return  H5P_DATASET_CREATE_DEFAULT;
        case JH5P_DATASET_XFER  : return  H5P_DATASET_XFER;
        case JH5P_DATASET_XFER_DEFAULT  : return  H5P_DATASET_XFER_DEFAULT;
        case JH5P_FILE_ACCESS  : return  H5P_FILE_ACCESS;
        case JH5P_FILE_ACCESS_DEFAULT  : return  H5P_FILE_ACCESS_DEFAULT;
        case JH5P_FILE_CREATE  : return  H5P_FILE_CREATE;
        case JH5P_FILE_CREATE_DEFAULT  : return  H5P_FILE_CREATE_DEFAULT;
        case JH5P_DEFAULT  : return  H5P_DEFAULT;
        /*case JH5P_MOUNT  : return H5P_MOUNT;
        case JH5P_MOUNT_DEFAULT  : return  H5P_MOUNT_DEFAULT; removed from 1.8*/
        case JH5P_NO_CLASS  : return  H5P_NO_CLASS;
        /*case JH5P_NO_CLASS_DEFAULT  : return  H5P_NO_CLASS_DEFAULT; removed from 1.8*/
        case JH5P_ROOT : return H5P_ROOT;
        case JH5P_OBJECT_CREATE : return H5P_OBJECT_CREATE;
        case JH5P_DATASET_ACCESS : return H5P_DATASET_ACCESS;
        case JH5P_DATASET_ACCESS_DEFAULT : return H5P_DATASET_ACCESS_DEFAULT;
        case JH5P_FILE_MOUNT : return H5P_FILE_MOUNT;
        case JH5P_FILE_MOUNT_DEFAULT : return H5P_FILE_MOUNT_DEFAULT;
        case JH5P_GROUP_CREATE : return H5P_GROUP_CREATE;
        case JH5P_GROUP_CREATE_DEFAULT : return H5P_GROUP_CREATE_DEFAULT;
        case JH5P_GROUP_ACCESS : return H5P_GROUP_ACCESS;
        case JH5P_GROUP_ACCESS_DEFAULT : return H5P_GROUP_ACCESS_DEFAULT;
        case JH5P_DATATYPE_CREATE : return H5P_DATATYPE_CREATE;
        case JH5P_DATATYPE_CREATE_DEFAULT : return H5P_DATATYPE_CREATE_DEFAULT;
        case JH5P_DATATYPE_ACCESS : return H5P_DATATYPE_ACCESS;
        case JH5P_DATATYPE_ACCESS_DEFAULT : return H5P_DATATYPE_ACCESS_DEFAULT;
        case JH5P_STRING_CREATE : return H5P_STRING_CREATE;
        case JH5P_ATTRIBUTE_CREATE : return H5P_ATTRIBUTE_CREATE;
        case JH5P_ATTRIBUTE_CREATE_DEFAULT : return H5P_ATTRIBUTE_CREATE_DEFAULT;
        case JH5P_OBJECT_COPY : return H5P_OBJECT_COPY;
        case JH5P_OBJECT_COPY_DEFAULT : return H5P_OBJECT_COPY_DEFAULT;
        case JH5P_LINK_CREATE : return H5P_LINK_CREATE;
        case JH5P_LINK_CREATE_DEFAULT : return H5P_LINK_CREATE_DEFAULT;
        case JH5P_LINK_ACCESS : return H5P_LINK_ACCESS;
        case JH5P_LINK_ACCESS_DEFAULT : return H5P_LINK_ACCESS_DEFAULT;
        case JH5R_BADTYPE  : return  H5R_BADTYPE;
        case JH5R_DATASET_REGION  : return  H5R_DATASET_REGION;
        /*case JH5R_INTERNAL  : return  H5R_INTERNAL; removed from 1.8*/
        case JH5R_MAXTYPE  : return  H5R_MAXTYPE;
        case JH5R_OBJ_REF_BUF_SIZE  : return  H5R_OBJ_REF_BUF_SIZE;
        case JH5R_OBJECT  : return  H5R_OBJECT;
        case JH5S_ALL  : return  H5S_ALL;
        /*case JH5S_COMPLEX  : return  H5S_COMPLEX; removed from 1.8*/
        case JH5S_MAX_RANK  : return  H5S_MAX_RANK;
        case JH5S_NO_CLASS  : return  H5S_NO_CLASS;
        case JH5S_NULL : return H5S_NULL;
        case JH5S_SCALAR  : return  H5S_SCALAR;
        case JH5S_SEL_ALL  : return  H5S_SEL_ALL;
        case JH5S_SEL_ERROR  : return  H5S_SEL_ERROR;
        case JH5S_SEL_HYPERSLABS  : return  H5S_SEL_HYPERSLABS;
        case JH5S_SEL_N  : return  H5S_SEL_N;
        case JH5S_SEL_NONE  : return  H5S_SEL_NONE;
        case JH5S_SEL_POINTS  : return  H5S_SEL_POINTS;
        case JH5S_SELECT_AND  : return  H5S_SELECT_AND;
        case JH5S_SELECT_APPEND  : return  H5S_SELECT_APPEND;
        case JH5S_SELECT_INVALID  : return  H5S_SELECT_INVALID;
        case JH5S_SELECT_NOOP  : return  H5S_SELECT_NOOP;
        case JH5S_SELECT_NOTA  : return  H5S_SELECT_NOTA;
        case JH5S_SELECT_NOTB  : return  H5S_SELECT_NOTB;
        case JH5S_SELECT_OR  : return  H5S_SELECT_OR;
        case JH5S_SELECT_PREPEND  : return  H5S_SELECT_PREPEND;
        case JH5S_SELECT_SET  : return  H5S_SELECT_SET;
        case JH5S_SELECT_XOR  : return  H5S_SELECT_XOR;
        case JH5S_SIMPLE  : return  H5S_SIMPLE;
        case JH5S_UNLIMITED  : return  (int)H5S_UNLIMITED;
        case JH5T_ALPHA_B16  : return  H5T_ALPHA_B16;
        case JH5T_ALPHA_B32  : return  H5T_ALPHA_B32;
        case JH5T_ALPHA_B64  : return  H5T_ALPHA_B64;
        case JH5T_ALPHA_B8  : return  H5T_ALPHA_B8;
        case JH5T_ALPHA_F32  : return  H5T_ALPHA_F32;
        case JH5T_ALPHA_F64  : return  H5T_ALPHA_F64;
        case JH5T_ALPHA_I16  : return  H5T_ALPHA_I16;
        case JH5T_ALPHA_I32  : return  H5T_ALPHA_I32;
        case JH5T_ALPHA_I64  : return  H5T_ALPHA_I64;
        case JH5T_ALPHA_I8  : return  H5T_ALPHA_I8;
        case JH5T_ALPHA_U16  : return  H5T_ALPHA_U16;
        case JH5T_ALPHA_U32  : return  H5T_ALPHA_U32;
        case JH5T_ALPHA_U64  : return  H5T_ALPHA_U64;
        case JH5T_ALPHA_U8  : return  H5T_ALPHA_U8;
        case JH5T_ARRAY  : return  H5T_ARRAY;
        case JH5T_BITFIELD  : return  H5T_BITFIELD;
        case JH5T_BKG_NO  : return  H5T_BKG_NO;
        case JH5T_BKG_YES  : return  H5T_BKG_YES;
        case JH5T_C_S1  : return  H5T_C_S1;
        case JH5T_COMPOUND  : return  H5T_COMPOUND;
        case JH5T_CONV_CONV  : return  H5T_CONV_CONV;
        case JH5T_CONV_FREE  : return  H5T_CONV_FREE;
        case JH5T_CONV_INIT  : return  H5T_CONV_INIT;
        case JH5T_CSET_ASCII  : return  H5T_CSET_ASCII;
        case JH5T_CSET_ERROR  : return  H5T_CSET_ERROR;
        /*case JH5T_CSET_RESERVED_1  : return  H5T_CSET_RESERVED_1; removed from 1.8*/
        case JH5T_CSET_RESERVED_10  : return  H5T_CSET_RESERVED_10;
        case JH5T_CSET_RESERVED_11  : return  H5T_CSET_RESERVED_11;
        case JH5T_CSET_RESERVED_12  : return  H5T_CSET_RESERVED_12;
        case JH5T_CSET_RESERVED_13  : return  H5T_CSET_RESERVED_13;
        case JH5T_CSET_RESERVED_14  : return  H5T_CSET_RESERVED_14;
        case JH5T_CSET_RESERVED_15  : return  H5T_CSET_RESERVED_15;
        case JH5T_CSET_RESERVED_2  : return  H5T_CSET_RESERVED_2;
        case JH5T_CSET_RESERVED_3  : return  H5T_CSET_RESERVED_3;
        case JH5T_CSET_RESERVED_4  : return  H5T_CSET_RESERVED_4;
        case JH5T_CSET_RESERVED_5  : return  H5T_CSET_RESERVED_5;
        case JH5T_CSET_RESERVED_6  : return  H5T_CSET_RESERVED_6;
        case JH5T_CSET_RESERVED_7  : return  H5T_CSET_RESERVED_7;
        case JH5T_CSET_RESERVED_8  : return  H5T_CSET_RESERVED_8;
        case JH5T_CSET_RESERVED_9  : return  H5T_CSET_RESERVED_9;
        case JH5T_DIR_ASCEND  : return  H5T_DIR_ASCEND;
        case JH5T_DIR_DEFAULT  : return  H5T_DIR_DEFAULT;
        case JH5T_DIR_DESCEND  : return  H5T_DIR_DESCEND;
        case JH5T_ENUM  : return  H5T_ENUM;
        case JH5T_FLOAT  : return  H5T_FLOAT;
        case JH5T_FORTRAN_S1  : return  H5T_FORTRAN_S1;
        case JH5T_IEEE_F32BE  : return  H5T_IEEE_F32BE;
        case JH5T_IEEE_F32LE  : return  H5T_IEEE_F32LE;
        case JH5T_IEEE_F64BE  : return  H5T_IEEE_F64BE;
        case JH5T_IEEE_F64LE  : return  H5T_IEEE_F64LE;
        case JH5T_INTEGER  : return  H5T_INTEGER;
        case JH5T_INTEL_B16  : return  H5T_INTEL_B16;
        case JH5T_INTEL_B32  : return  H5T_INTEL_B32;
        case JH5T_INTEL_B64  : return  H5T_INTEL_B64;
        case JH5T_INTEL_B8  : return  H5T_INTEL_B8;
        case JH5T_INTEL_F32  : return  H5T_INTEL_F32;
        case JH5T_INTEL_F64  : return  H5T_INTEL_F64;
        case JH5T_INTEL_I16  : return  H5T_INTEL_I16;
        case JH5T_INTEL_I32  : return  H5T_INTEL_I32;
        case JH5T_INTEL_I64  : return  H5T_INTEL_I64;
        case JH5T_INTEL_I8  : return  H5T_INTEL_I8;
        case JH5T_INTEL_U16  : return  H5T_INTEL_U16;
        case JH5T_INTEL_U32  : return  H5T_INTEL_U32;
        case JH5T_INTEL_U64  : return  H5T_INTEL_U64;
        case JH5T_INTEL_U8  : return  H5T_INTEL_U8;
        case JH5T_MIPS_B16  : return  H5T_MIPS_B16;
        case JH5T_MIPS_B32  : return  H5T_MIPS_B32;
        case JH5T_MIPS_B64  : return  H5T_MIPS_B64;
        case JH5T_MIPS_B8  : return  H5T_MIPS_B8;
        case JH5T_MIPS_F32  : return  H5T_MIPS_F32;
        case JH5T_MIPS_F64  : return  H5T_MIPS_F64;
        case JH5T_MIPS_I16  : return  H5T_MIPS_I16;
        case JH5T_MIPS_I32  : return  H5T_MIPS_I32;
        case JH5T_MIPS_I64  : return  H5T_MIPS_I64;
        case JH5T_MIPS_I8  : return  H5T_MIPS_I8;
        case JH5T_MIPS_U16  : return  H5T_MIPS_U16;
        case JH5T_MIPS_U32  : return  H5T_MIPS_U32;
        case JH5T_MIPS_U64  : return  H5T_MIPS_U64;
        case JH5T_MIPS_U8  : return  H5T_MIPS_U8;
        case JH5T_NATIVE_B16  : return  H5T_NATIVE_B16;
        case JH5T_NATIVE_B32  : return  H5T_NATIVE_B32;
        case JH5T_NATIVE_B64  : return  H5T_NATIVE_B64;
        case JH5T_NATIVE_B8  : return  H5T_NATIVE_B8;
        case JH5T_NATIVE_CHAR  : return  H5T_NATIVE_CHAR;
        case JH5T_NATIVE_DOUBLE  : return  H5T_NATIVE_DOUBLE;
        case JH5T_NATIVE_FLOAT  : return  H5T_NATIVE_FLOAT;
        case JH5T_NATIVE_HADDR  : return  H5T_NATIVE_HADDR;
        case JH5T_NATIVE_HBOOL  : return  H5T_NATIVE_HBOOL;
        case JH5T_NATIVE_HERR  : return  H5T_NATIVE_HERR;
        case JH5T_NATIVE_HSIZE  : return  H5T_NATIVE_HSIZE;
        case JH5T_NATIVE_HSSIZE  : return  H5T_NATIVE_HSSIZE;
        case JH5T_NATIVE_INT  : return  H5T_NATIVE_INT;
        case JH5T_NATIVE_INT_FAST16  : return  H5T_NATIVE_INT_FAST16;
        case JH5T_NATIVE_INT_FAST32  : return  H5T_NATIVE_INT_FAST32;
        case JH5T_NATIVE_INT_FAST64  : return  H5T_NATIVE_INT_FAST64;
        case JH5T_NATIVE_INT_FAST8  : return  H5T_NATIVE_INT_FAST8;
        case JH5T_NATIVE_INT_LEAST16  : return  H5T_NATIVE_INT_LEAST16;
        case JH5T_NATIVE_INT_LEAST32  : return  H5T_NATIVE_INT_LEAST32;
        case JH5T_NATIVE_INT_LEAST64  : return  H5T_NATIVE_INT_LEAST64;
        case JH5T_NATIVE_INT_LEAST8  : return  H5T_NATIVE_INT_LEAST8;
        case JH5T_NATIVE_INT16  : return  H5T_NATIVE_INT16;
        case JH5T_NATIVE_INT32  : return  H5T_NATIVE_INT32;
        case JH5T_NATIVE_INT64  : return  H5T_NATIVE_INT64;
        case JH5T_NATIVE_INT8  : return  H5T_NATIVE_INT8;
        case JH5T_NATIVE_LDOUBLE  : return  H5T_NATIVE_LDOUBLE;
        case JH5T_NATIVE_LLONG  : return  H5T_NATIVE_LLONG;
        case JH5T_NATIVE_LONG  : return  H5T_NATIVE_LONG;
        case JH5T_NATIVE_OPAQUE  : return  H5T_NATIVE_OPAQUE;
        case JH5T_NATIVE_SCHAR  : return  H5T_NATIVE_SCHAR;
        case JH5T_NATIVE_SHORT  : return  H5T_NATIVE_SHORT;
        case JH5T_NATIVE_UCHAR  : return  H5T_NATIVE_UCHAR;
        case JH5T_NATIVE_UINT  : return  H5T_NATIVE_UINT;
        case JH5T_NATIVE_UINT_FAST16  : return  H5T_NATIVE_UINT_FAST16;
        case JH5T_NATIVE_UINT_FAST32  : return  H5T_NATIVE_UINT_FAST32;
        case JH5T_NATIVE_UINT_FAST64  : return  H5T_NATIVE_UINT_FAST64;
        case JH5T_NATIVE_UINT_FAST8  : return  H5T_NATIVE_UINT_FAST8;
        case JH5T_NATIVE_UINT_LEAST16  : return  H5T_NATIVE_UINT_LEAST16;
        case JH5T_NATIVE_UINT_LEAST32  : return  H5T_NATIVE_UINT_LEAST32;
        case JH5T_NATIVE_UINT_LEAST64  : return  H5T_NATIVE_UINT_LEAST64;
        case JH5T_NATIVE_UINT_LEAST8  : return  H5T_NATIVE_UINT_LEAST8;
        case JH5T_NATIVE_UINT16  : return  H5T_NATIVE_UINT16;
        case JH5T_NATIVE_UINT32  : return  H5T_NATIVE_UINT32;
        case JH5T_NATIVE_UINT64  : return  H5T_NATIVE_UINT64;
        case JH5T_NATIVE_UINT8  : return  H5T_NATIVE_UINT8;
        case JH5T_NATIVE_ULLONG  : return  H5T_NATIVE_ULLONG;
        case JH5T_NATIVE_ULONG  : return  H5T_NATIVE_ULONG;
        case JH5T_NATIVE_USHORT  : return  H5T_NATIVE_USHORT;
        case JH5T_NCLASSES  : return  H5T_NCLASSES;
        case JH5T_NO_CLASS  : return  H5T_NO_CLASS;
        case JH5T_NORM_ERROR  : return  H5T_NORM_ERROR;
        case JH5T_NORM_IMPLIED  : return  H5T_NORM_IMPLIED;
        case JH5T_NORM_MSBSET  : return  H5T_NORM_MSBSET;
        case JH5T_NORM_NONE  : return  H5T_NORM_NONE;
        case JH5T_NPAD  : return  H5T_NPAD;
        case JH5T_NSGN  : return  H5T_NSGN;
        case JH5T_OPAQUE  : return  H5T_OPAQUE;
        case JH5T_OPAQUE_TAG_MAX  : return  H5T_OPAQUE_TAG_MAX;
        case JH5T_ORDER_BE  : return  H5T_ORDER_BE;
        case JH5T_ORDER_ERROR  : return  H5T_ORDER_ERROR;
        case JH5T_ORDER_LE  : return  H5T_ORDER_LE;
        case JH5T_ORDER_NONE  : return  H5T_ORDER_NONE;
        case JH5T_ORDER_VAX  : return  H5T_ORDER_VAX;
        case JH5T_PAD_BACKGROUND  : return  H5T_PAD_BACKGROUND;
        case JH5T_PAD_ERROR  : return  H5T_PAD_ERROR;
        case JH5T_PAD_ONE  : return  H5T_PAD_ONE;
        case JH5T_PAD_ZERO  : return  H5T_PAD_ZERO;
        case JH5T_PERS_DONTCARE  : return  H5T_PERS_DONTCARE;
        case JH5T_PERS_HARD  : return  H5T_PERS_HARD;
        case JH5T_PERS_SOFT  : return  H5T_PERS_SOFT;
        case JH5T_REFERENCE  : return  H5T_REFERENCE;
        case JH5T_SGN_2  : return  H5T_SGN_2;
        case JH5T_SGN_ERROR  : return  H5T_SGN_ERROR;
        case JH5T_SGN_NONE  : return  H5T_SGN_NONE;
        case JH5T_STD_B16BE  : return  H5T_STD_B16BE;
        case JH5T_STD_B16LE  : return  H5T_STD_B16LE;
        case JH5T_STD_B32BE  : return  H5T_STD_B32BE;
        case JH5T_STD_B32LE  : return  H5T_STD_B32LE;
        case JH5T_STD_B64BE  : return  H5T_STD_B64BE;
        case JH5T_STD_B64LE  : return  H5T_STD_B64LE;
        case JH5T_STD_B8BE  : return  H5T_STD_B8BE;
        case JH5T_STD_B8LE  : return  H5T_STD_B8LE;
        case JH5T_STD_I16BE  : return  H5T_STD_I16BE;
        case JH5T_STD_I16LE  : return  H5T_STD_I16LE;
        case JH5T_STD_I32BE  : return  H5T_STD_I32BE;
        case JH5T_STD_I32LE  : return  H5T_STD_I32LE;
        case JH5T_STD_I64BE  : return  H5T_STD_I64BE;
        case JH5T_STD_I64LE  : return  H5T_STD_I64LE;
        case JH5T_STD_I8BE  : return  H5T_STD_I8BE;
        case JH5T_STD_I8LE  : return  H5T_STD_I8LE;
        case JH5T_STD_REF_DSETREG  : return  H5T_STD_REF_DSETREG;
        case JH5T_STD_REF_OBJ  : return  H5T_STD_REF_OBJ;
        case JH5T_STD_U16BE  : return  H5T_STD_U16BE;
        case JH5T_STD_U16LE  : return  H5T_STD_U16LE;
        case JH5T_STD_U32BE  : return  H5T_STD_U32BE;
        case JH5T_STD_U32LE  : return  H5T_STD_U32LE;
        case JH5T_STD_U64BE  : return  H5T_STD_U64BE;
        case JH5T_STD_U64LE  : return  H5T_STD_U64LE;
        case JH5T_STD_U8BE  : return  H5T_STD_U8BE;
        case JH5T_STD_U8LE  : return  H5T_STD_U8LE;
        case JH5T_STR_ERROR  : return  H5T_STR_ERROR;
        case JH5T_STR_NULLPAD  : return  H5T_STR_NULLPAD;
        case JH5T_STR_NULLTERM  : return  H5T_STR_NULLTERM;
        case JH5T_STR_RESERVED_10  : return  H5T_STR_RESERVED_10;
        case JH5T_STR_RESERVED_11  : return  H5T_STR_RESERVED_11;
        case JH5T_STR_RESERVED_12  : return  H5T_STR_RESERVED_12;
        case JH5T_STR_RESERVED_13  : return  H5T_STR_RESERVED_13;
        case JH5T_STR_RESERVED_14  : return  H5T_STR_RESERVED_14;
        case JH5T_STR_RESERVED_15  : return  H5T_STR_RESERVED_15;
        case JH5T_STR_RESERVED_3  : return  H5T_STR_RESERVED_3;
        case JH5T_STR_RESERVED_4  : return  H5T_STR_RESERVED_4;
        case JH5T_STR_RESERVED_5  : return  H5T_STR_RESERVED_5;
        case JH5T_STR_RESERVED_6  : return  H5T_STR_RESERVED_6;
        case JH5T_STR_RESERVED_7  : return  H5T_STR_RESERVED_7;
        case JH5T_STR_RESERVED_8  : return  H5T_STR_RESERVED_8;
        case JH5T_STR_RESERVED_9  : return  H5T_STR_RESERVED_9;
        case JH5T_STR_SPACEPAD  : return  H5T_STR_SPACEPAD;
        case JH5T_STRING  : return  H5T_STRING;
        case JH5T_TIME  : return  H5T_TIME;
        case JH5T_UNIX_D32BE  : return  H5T_UNIX_D32BE;
        case JH5T_UNIX_D32LE  : return  H5T_UNIX_D32LE;
        case JH5T_UNIX_D64BE  : return  H5T_UNIX_D64BE;
        case JH5T_UNIX_D64LE  : return  H5T_UNIX_D64LE;
        case JH5T_VARIABLE  : return H5T_VARIABLE;
        case JH5T_VLEN  : return  H5T_VLEN;
        case JH5Z_CB_CONT  : return  H5Z_CB_CONT;
        case JH5Z_CB_ERROR  : return  H5Z_CB_ERROR;
        case JH5Z_CB_FAIL  : return  H5Z_CB_FAIL;
        case JH5Z_CB_NO  : return  H5Z_CB_NO;
        case JH5Z_DISABLE_EDC  : return  H5Z_DISABLE_EDC;
        case JH5Z_ENABLE_EDC  : return  H5Z_ENABLE_EDC;
        case JH5Z_ERROR_EDC  : return  H5Z_ERROR_EDC;
        case JH5Z_FILTER_DEFLATE  : return  H5Z_FILTER_DEFLATE;
        case JH5Z_FILTER_ERROR  : return  H5Z_FILTER_ERROR;
        case JH5Z_FILTER_FLETCHER32  : return  H5Z_FILTER_FLETCHER32;
        case JH5Z_FILTER_MAX  : return  H5Z_FILTER_MAX;
        case JH5Z_FILTER_NONE  : return  H5Z_FILTER_NONE;
        case JH5Z_FILTER_RESERVED  : return  H5Z_FILTER_RESERVED;
        case JH5Z_FILTER_SHUFFLE  : return  H5Z_FILTER_SHUFFLE;
        case JH5Z_FILTER_SZIP  : return  H5Z_FILTER_SZIP;
        case JH5Z_FLAG_DEFMASK  : return  H5Z_FLAG_DEFMASK;
        case JH5Z_FLAG_INVMASK  : return  H5Z_FLAG_INVMASK;
        case JH5Z_FLAG_MANDATORY  : return  H5Z_FLAG_MANDATORY;
        case JH5Z_FLAG_OPTIONAL  : return  H5Z_FLAG_OPTIONAL;
        case JH5Z_FLAG_REVERSE  : return  H5Z_FLAG_REVERSE;
        case JH5Z_FLAG_SKIP_EDC  : return  H5Z_FLAG_SKIP_EDC;
        case JH5Z_MAX_NFILTERS  : return  H5Z_MAX_NFILTERS;
        case JH5Z_NO_EDC  : return  H5Z_NO_EDC;
        case JH5Z_FILTER_CONFIG_ENCODE_ENABLED  : return  H5Z_FILTER_CONFIG_ENCODE_ENABLED;
        case JH5Z_FILTER_CONFIG_DECODE_ENABLED  : return  H5Z_FILTER_CONFIG_DECODE_ENABLED;

        default:
            h5illegalConstantError(env);
	    return -1;
    }
}
#ifdef __cplusplus
}
#endif
