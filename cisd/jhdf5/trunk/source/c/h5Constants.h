/*
 *  The Java constants defined at ncsa.hdf.hdf5lib.HDF5Constants.java
 *
 *  The values are arbitrary, but it is very important that this
 *  file has the same values as HDF5Constants.java.
 */

#include "hdf5.h"

#define JH5_SZIP_MAX_PIXELS_PER_BLOCK 1000
#define JH5_SZIP_NN_OPTION_MASK 1010
#define JH5_SZIP_EC_OPTION_MASK 1020
#define JH5_SZIP_ALLOW_K13_OPTION_MASK 1021
#define JH5_SZIP_CHIP_OPTION_MASK 1022
#define JH5D_ALLOC_TIME_DEFAULT 1030
#define JH5D_ALLOC_TIME_EARLY 1040
#define JH5D_ALLOC_TIME_ERROR 1050
#define JH5D_ALLOC_TIME_INCR 1060
#define JH5D_ALLOC_TIME_LATE 1070
#define JH5D_CHUNKED 1080
#define JH5D_COMPACT 1090
#define JH5D_CONTIGUOUS 1100
#define JH5D_FILL_TIME_ALLOC 1110
#define JH5D_FILL_TIME_ERROR 1120
#define JH5D_FILL_TIME_NEVER 1130
#define JH5D_FILL_VALUE_DEFAULT 1140
#define JH5D_FILL_VALUE_ERROR 1150
#define JH5D_FILL_VALUE_UNDEFINED 1160
#define JH5D_FILL_VALUE_USER_DEFINED 1170
#define JH5D_LAYOUT_ERROR 1180
#define JH5D_NLAYOUTS 1190
#define JH5D_SPACE_STATUS_ALLOCATED 1200
#define JH5D_SPACE_STATUS_ERROR 1210
#define JH5D_SPACE_STATUS_NOT_ALLOCATED 1220
#define JH5D_SPACE_STATUS_PART_ALLOCATED 1230
#define JH5E_ALIGNMENT 1240
#define JH5E_ALREADYEXISTS 1250
#define JH5E_ALREADYINIT 1260
#define JH5E_ARGS 1270
#define JH5E_ATOM 1280
#define JH5E_ATTR 1290
#define JH5E_BADATOM 1300
#define JH5E_BADFILE 1310
#define JH5E_BADGROUP 1320
#define JH5E_BADMESG 1330
#define JH5E_BADRANGE 1340
#define JH5E_BADSELECT 1350
#define JH5E_BADSIZE 1360
#define JH5E_BADTYPE 1370
#define JH5E_BADVALUE 1380
#define JH5E_BTREE 1390
#define JH5E_CACHE 1400
#define JH5E_CALLBACK 1410
#define JH5E_CANAPPLY 1420
/*#define JH5E_CANTALLOC 1430 removed from 1.6.4 */
/*#define JH5E_CANTCHANGE 1440 removed from 1.6.4 */
#define JH5E_CANTCLIP 1450
#define JH5E_CANTCLOSEFILE 1460
#define JH5E_CANTCONVERT 1470
#define JH5E_CANTCOPY 1480
#define JH5E_CANTCOUNT 1490
#define JH5E_CANTCREATE 1500
#define JH5E_CANTDEC 1510
#define JH5E_CANTDECODE 1520
#define JH5E_CANTDELETE 1530
#define JH5E_CANTENCODE 1540
#define JH5E_CANTFLUSH 1550
#define JH5E_CANTFREE 1560
#define JH5E_CANTGET 1570
#define JH5E_CANTINC 1580
#define JH5E_CANTINIT 1590
#define JH5E_CANTINSERT 1600
#define JH5E_CANTLIST 1610
#define JH5E_CANTLOAD 1620
#define JH5E_CANTLOCK 1630
#define JH5E_CANTMAKETREE 1640
#define JH5E_CANTNEXT 1650
#define JH5E_CANTOPENFILE 1660
#define JH5E_CANTOPENOBJ 1670
/*#define JH5E_CANTRECV 1680 removed from 1.6.4 */
#define JH5E_CANTREGISTER 1690
#define JH5E_CANTRELEASE 1700
#define JH5E_CANTSELECT 1710
/*#define JH5E_CANTSENDMDATA 1720 removed from 1.6.4 */
#define JH5E_CANTSET 1730
#define JH5E_CANTSPLIT 1740
#define JH5E_CANTUNLOCK 1750
#define JH5E_CLOSEERROR 1760
#define JH5E_COMPLEN 1770
/*#define JH5E_CWG 1780 removed from 1.6.5 */
#define JH5E_DATASET 1790
#define JH5E_DATASPACE 1800
#define JH5E_DATATYPE 1810
#define JH5E_DUPCLASS 1820
#define JH5E_EFL 1830
#define JH5E_EXISTS 1840
#define JH5E_FCNTL 1850
#define JH5E_FILE 1860
#define JH5E_FILEEXISTS 1870
#define JH5E_FILEOPEN 1880
/*#define JH5E_FPHDF5 1890 removed from 1.6.4 */
#define JH5E_FUNC 1900
#define JH5E_HEAP 1910
#define JH5E_INTERNAL 1920
#define JH5E_IO 1930
#define JH5E_LINK 1940
#define JH5E_LINKCOUNT 1950
#define JH5E_MOUNT 1960
#define JH5E_MPI 1970
#define JH5E_MPIERRSTR 1980
#define JH5E_NOFILTER 1990
#define JH5E_NOIDS 2000
#define JH5E_NONE_MAJOR 2010
#define JH5E_NONE_MINOR 2020
#define JH5E_NOSPACE 2030
#define JH5E_NOTCACHED 2040
#define JH5E_NOTFOUND 2050
#define JH5E_NOTHDF5 2060
#define JH5E_OHDR 2070
#define JH5E_OVERFLOW 2080
#define JH5E_PLINE 2090
#define JH5E_PLIST 2100
#define JH5E_PROTECT 2110
#define JH5E_READERROR 2120
#define JH5E_REFERENCE 2130
#define JH5E_RESOURCE 2140
#define JH5E_RS 2150
#define JH5E_SEEKERROR 2160
#define JH5E_SETLOCAL 2170
/*#define JH5E_SLINK 2180 removed from 1.8.0 */
#define JH5E_STORAGE 2190
#define JH5E_SYM 2200
/*#define JH5E_TBBT 2210 removed from 1.8.0 */
#define JH5E_TRUNCATED 2220
#define JH5E_TST 2230
#define JH5E_UNINITIALIZED 2240
#define JH5E_UNSUPPORTED 2250
#define JH5E_VERSION 2260
#define JH5E_VFL 2270
#define JH5E_WALK_DOWNWARD 2280
#define JH5E_WALK_UPWARD 2290
#define JH5E_WRITEERROR 2300
#define JH5F_ACC_CREAT 2310
#define JH5F_ACC_DEBUG 2320
#define JH5F_ACC_EXCL 2330
#define JH5F_ACC_RDONLY 2340
#define JH5F_ACC_RDWR 2350
#define JH5F_ACC_TRUNC 2360
#define JH5F_CLOSE_DEFAULT 2370
#define JH5F_CLOSE_SEMI 2380
#define JH5F_CLOSE_STRONG 2390
#define JH5F_CLOSE_WEAK 2400
#define JH5F_OBJ_ALL 2410
#define JH5F_OBJ_ATTR 2415
#define JH5F_OBJ_DATASET 2420
#define JH5F_OBJ_DATATYPE 2430
#define JH5F_OBJ_FILE 2440
#define JH5F_OBJ_GROUP 2450
#define JH5F_OBJ_LOCAL 2455
#define JH5F_SCOPE_DOWN 2460
#define JH5F_SCOPE_GLOBAL 2470
#define JH5F_SCOPE_LOCAL 2480
#define JH5F_UNLIMITED 2490
#define JH5F_LIBVER_EARLIEST 2494
#define JH5F_LIBVER_LATEST 2495
#define JH5G_DATASET 2500
#define JH5G_GROUP 2510
#define JH5G_LINK 2520
#define JH5G_LINK_ERROR 2530
#define JH5G_LINK_HARD 2540
#define JH5G_LINK_SOFT 2550
#define JH5G_NLIBTYPES 2560
#define JH5G_NTYPES 2570
#define JH5G_NUSERTYPES 2580
/*#define JH5G_RESERVED_4 2590 removed from 1.8.0 */
#define JH5G_RESERVED_5 2600
#define JH5G_RESERVED_6 2610
#define JH5G_RESERVED_7 2620
#define JH5G_SAME_LOC 2630
#define JH5G_TYPE 2640
#define JH5G_UNKNOWN 2650
#define JH5G_USERTYPE 2660
#define JH5I_ATTR 2670
#define JH5I_BADID 2680
#define JH5I_DATASET 2690
#define JH5I_DATASPACE 2700
#define JH5I_DATATYPE 2710
#define JH5I_FILE 2720
#define JH5I_FILE_CLOSING 2730
#define JH5I_GENPROP_CLS 2740
#define JH5I_GENPROP_LST 2750
#define JH5I_GROUP 2760
#define JH5I_INVALID_HID 2770
/*#define JH5I_NGROUPS 2780 removed from 1.8.0 */
#define JH5I_REFERENCE 2790
/*#define JH5I_TEMPBUF 2800 removed from 1.8.0 */
#define JH5I_VFL 2810
#define JH5O_TYPE_UNKNOWN 5510
#define JH5O_TYPE_GROUP 5520
#define JH5O_TYPE_DATASET 5530
#define JH5O_TYPE_NAMED_DATATYPE 5540
#define JH5O_TYPE_NTYPES 5550
#define JH5L_TYPE_ERROR 5560
#define JH5L_TYPE_HARD 5570
#define JH5L_TYPE_SOFT 5580
#define JH5L_TYPE_EXTERNAL 5590
#define JH5L_TYPE_MAX 5600
#define JH5P_DATASET_CREATE 2820
#define JH5P_DATASET_CREATE_DEFAULT 2830
#define JH5P_DATASET_XFER 2840
#define JH5P_DATASET_XFER_DEFAULT 2850
#define JH5P_DEFAULT 2860
#define JH5P_FILE_ACCESS 2870
#define JH5P_FILE_ACCESS_DEFAULT 2880
#define JH5P_FILE_CREATE 2890
#define JH5P_FILE_CREATE_DEFAULT 2900
/*#define JH5P_MOUNT 2910
#define JH5P_MOUNT_DEFAULT 2920 removed from 1.8.0 */
#define JH5P_NO_CLASS 2930
/*#define JH5P_NO_CLASS_DEFAULT 2940 removed from 1.8.0 */
#define JH5P_ROOT 6000
#define JH5P_OBJECT_CREATE 6010
#define JH5P_DATASET_ACCESS 6020
#define JH5P_DATASET_ACCESS_DEFAULT 6030
#define JH5P_FILE_MOUNT 6040
#define JH5P_FILE_MOUNT_DEFAULT 6050
#define JH5P_GROUP_CREATE 6060
#define JH5P_GROUP_CREATE_DEFAULT 6070
#define JH5P_GROUP_ACCESS 6080
#define JH5P_GROUP_ACCESS_DEFAULT 6090
#define JH5P_DATATYPE_CREATE 6100
#define JH5P_DATATYPE_CREATE_DEFAULT 6110
#define JH5P_DATATYPE_ACCESS 6120
#define JH5P_DATATYPE_ACCESS_DEFAULT 6130
#define JH5P_STRING_CREATE 6140
#define JH5P_ATTRIBUTE_CREATE 6150
#define JH5P_ATTRIBUTE_CREATE_DEFAULT 6160
#define JH5P_OBJECT_COPY 6170
#define JH5P_OBJECT_COPY_DEFAULT 6180 
#define JH5P_LINK_CREATE 6190
#define JH5P_LINK_CREATE_DEFAULT 6200
#define JH5P_LINK_ACCESS 6210
#define JH5P_LINK_ACCESS_DEFAULT 6220
#define JH5R_BADTYPE 2950
#define JH5R_DATASET_REGION 2960
/*#define JH5R_INTERNAL 2970 removed from 1.8.0 */
#define JH5R_MAXTYPE 2980
#define JH5R_OBJ_REF_BUF_SIZE 2990
#define JH5R_OBJECT 3000
#define JH5S_ALL 3010
/*#define JH5S_COMPLEX 3020 removed from 1.8.0 */
#define JH5S_MAX_RANK 3030
#define JH5S_NO_CLASS 3040
#define JH5S_NULL 3045
#define JH5S_SCALAR 3050
#define JH5S_SEL_ALL 3060
#define JH5S_SEL_ERROR 3070
#define JH5S_SEL_HYPERSLABS 3080
#define JH5S_SEL_N 3090
#define JH5S_SEL_NONE 3100
#define JH5S_SEL_POINTS 3110
#define JH5S_SELECT_AND 3120
#define JH5S_SELECT_APPEND 3130
#define JH5S_SELECT_INVALID 3140
#define JH5S_SELECT_NOOP 3150
#define JH5S_SELECT_NOTA 3160
#define JH5S_SELECT_NOTB 3170
#define JH5S_SELECT_OR 3180
#define JH5S_SELECT_PREPEND 3190
#define JH5S_SELECT_SET 3200
#define JH5S_SELECT_XOR 3210
#define JH5S_SIMPLE 3220
#define JH5S_UNLIMITED 3230
#define JH5T_ALPHA_B16 3240
#define JH5T_ALPHA_B32 3250
#define JH5T_ALPHA_B64 3260
#define JH5T_ALPHA_B8 3270
#define JH5T_ALPHA_F32 3280
#define JH5T_ALPHA_F64 3290
#define JH5T_ALPHA_I16 3300
#define JH5T_ALPHA_I32 3310
#define JH5T_ALPHA_I64 3320
#define JH5T_ALPHA_I8 3330
#define JH5T_ALPHA_U16 3340
#define JH5T_ALPHA_U32 3350
#define JH5T_ALPHA_U64 3360
#define JH5T_ALPHA_U8 3370
#define JH5T_ARRAY 3380
#define JH5T_BITFIELD 3390
#define JH5T_BKG_NO 3400
#define JH5T_BKG_YES 3410
#define JH5T_C_S1 3420
#define JH5T_COMPOUND 3430
#define JH5T_CONV_CONV 3440
#define JH5T_CONV_FREE 3450
#define JH5T_CONV_INIT 3460
#define JH5T_CSET_ASCII 3470
#define JH5T_CSET_ERROR 3480
/*#define JH5T_CSET_RESERVED_1 3490 removed from 1.8.0 */
#define JH5T_CSET_RESERVED_10 3500
#define JH5T_CSET_RESERVED_11 3510
#define JH5T_CSET_RESERVED_12 3520
#define JH5T_CSET_RESERVED_13 3530
#define JH5T_CSET_RESERVED_14 3540
#define JH5T_CSET_RESERVED_15 3550
#define JH5T_CSET_RESERVED_2 3560
#define JH5T_CSET_RESERVED_3 3570
#define JH5T_CSET_RESERVED_4 3580
#define JH5T_CSET_RESERVED_5 3590
#define JH5T_CSET_RESERVED_6 3600
#define JH5T_CSET_RESERVED_7 3610
#define JH5T_CSET_RESERVED_8 3620
#define JH5T_CSET_RESERVED_9 3630
#define JH5T_DIR_ASCEND 3640
#define JH5T_DIR_DEFAULT 3650
#define JH5T_DIR_DESCEND 3660
#define JH5T_ENUM 3670
#define JH5T_FLOAT 3680
#define JH5T_FORTRAN_S1 3690
#define JH5T_IEEE_F32BE 3700
#define JH5T_IEEE_F32LE 3710
#define JH5T_IEEE_F64BE 3720
#define JH5T_IEEE_F64LE 3730
#define JH5T_INTEGER 3740
#define JH5T_INTEL_B16 3750
#define JH5T_INTEL_B32 3760
#define JH5T_INTEL_B64 3770
#define JH5T_INTEL_B8 3780
#define JH5T_INTEL_F32 3790
#define JH5T_INTEL_F64 3800
#define JH5T_INTEL_I16 3810
#define JH5T_INTEL_I32 3820
#define JH5T_INTEL_I64 3830
#define JH5T_INTEL_I8 3840
#define JH5T_INTEL_U16 3850
#define JH5T_INTEL_U32 3860
#define JH5T_INTEL_U64 3870
#define JH5T_INTEL_U8 3880
#define JH5T_MIPS_B16 3890
#define JH5T_MIPS_B32 3900
#define JH5T_MIPS_B64 3910
#define JH5T_MIPS_B8 3920
#define JH5T_MIPS_F32 3930
#define JH5T_MIPS_F64 3940
#define JH5T_MIPS_I16 3950
#define JH5T_MIPS_I32 3960
#define JH5T_MIPS_I64 3970
#define JH5T_MIPS_I8 3980
#define JH5T_MIPS_U16 3990
#define JH5T_MIPS_U32 4000
#define JH5T_MIPS_U64 4010
#define JH5T_MIPS_U8 4020
#define JH5T_NATIVE_B16 4030
#define JH5T_NATIVE_B32 4040
#define JH5T_NATIVE_B64 4050
#define JH5T_NATIVE_B8 4060
#define JH5T_NATIVE_CHAR 4070
#define JH5T_NATIVE_DOUBLE 4080
#define JH5T_NATIVE_FLOAT 4090
#define JH5T_NATIVE_HADDR 4100
#define JH5T_NATIVE_HBOOL 4110
#define JH5T_NATIVE_HERR 4120
#define JH5T_NATIVE_HSIZE 4130
#define JH5T_NATIVE_HSSIZE 4140
#define JH5T_NATIVE_INT 4150
#define JH5T_NATIVE_INT_FAST16 4160
#define JH5T_NATIVE_INT_FAST32 4170
#define JH5T_NATIVE_INT_FAST64 4180
#define JH5T_NATIVE_INT_FAST8 4190
#define JH5T_NATIVE_INT_LEAST16 4200
#define JH5T_NATIVE_INT_LEAST32 4210
#define JH5T_NATIVE_INT_LEAST64 4220
#define JH5T_NATIVE_INT_LEAST8 4230
#define JH5T_NATIVE_INT16 4240
#define JH5T_NATIVE_INT32 4250
#define JH5T_NATIVE_INT64 4260
#define JH5T_NATIVE_INT8 4270
#define JH5T_NATIVE_LDOUBLE 4280
#define JH5T_NATIVE_LLONG 4290
#define JH5T_NATIVE_LONG 4300
#define JH5T_NATIVE_OPAQUE 4310
#define JH5T_NATIVE_SCHAR 4320
#define JH5T_NATIVE_SHORT 4330
#define JH5T_NATIVE_UCHAR 4340
#define JH5T_NATIVE_UINT 4350
#define JH5T_NATIVE_UINT_FAST16 4360
#define JH5T_NATIVE_UINT_FAST32 4370
#define JH5T_NATIVE_UINT_FAST64 4380
#define JH5T_NATIVE_UINT_FAST8 4390
#define JH5T_NATIVE_UINT_LEAST16 4400
#define JH5T_NATIVE_UINT_LEAST32 4410
#define JH5T_NATIVE_UINT_LEAST64 4420
#define JH5T_NATIVE_UINT_LEAST8 4430
#define JH5T_NATIVE_UINT16 4440
#define JH5T_NATIVE_UINT32 4450
#define JH5T_NATIVE_UINT64 4460
#define JH5T_NATIVE_UINT8 4470
#define JH5T_NATIVE_ULLONG 4480
#define JH5T_NATIVE_ULONG 4490
#define JH5T_NATIVE_USHORT 4500
#define JH5T_NCLASSES 4510
#define JH5T_NO_CLASS 4520
#define JH5T_NORM_ERROR 4530
#define JH5T_NORM_IMPLIED 4540
#define JH5T_NORM_MSBSET 4550
#define JH5T_NORM_NONE 4560
#define JH5T_NPAD 4570
#define JH5T_NSGN 4580
#define JH5T_OPAQUE 4590
#define JH5T_OPAQUE_TAG_MAX 4595
#define JH5T_ORDER_BE 4600
#define JH5T_ORDER_ERROR 4610
#define JH5T_ORDER_LE 4620
#define JH5T_ORDER_NONE 4630
#define JH5T_ORDER_VAX 4640
#define JH5T_PAD_BACKGROUND 4650
#define JH5T_PAD_ERROR 4660
#define JH5T_PAD_ONE 4670
#define JH5T_PAD_ZERO 4680
#define JH5T_PERS_DONTCARE 4690
#define JH5T_PERS_HARD 4700
#define JH5T_PERS_SOFT 4710
#define JH5T_REFERENCE 4720
#define JH5T_SGN_2 4730
#define JH5T_SGN_ERROR 4740
#define JH5T_SGN_NONE 4750
#define JH5T_STD_B16BE 4760
#define JH5T_STD_B16LE 4770
#define JH5T_STD_B32BE 4780
#define JH5T_STD_B32LE 4790
#define JH5T_STD_B64BE 4800
#define JH5T_STD_B64LE 4810
#define JH5T_STD_B8BE 4820
#define JH5T_STD_B8LE 4830
#define JH5T_STD_I16BE 4840
#define JH5T_STD_I16LE 4850
#define JH5T_STD_I32BE 4860
#define JH5T_STD_I32LE 4870
#define JH5T_STD_I64BE 4880
#define JH5T_STD_I64LE 4890
#define JH5T_STD_I8BE 4900
#define JH5T_STD_I8LE 4910
#define JH5T_STD_REF_DSETREG 4920
#define JH5T_STD_REF_OBJ 4930
#define JH5T_STD_U16BE 4940
#define JH5T_STD_U16LE 4950
#define JH5T_STD_U32BE 4960
#define JH5T_STD_U32LE 4970
#define JH5T_STD_U64BE 4980
#define JH5T_STD_U64LE 4990
#define JH5T_STD_U8BE 5000
#define JH5T_STD_U8LE 5010
#define JH5T_STR_ERROR 5020
#define JH5T_STR_NULLPAD 5030
#define JH5T_STR_NULLTERM 5040
#define JH5T_STR_RESERVED_10 5050
#define JH5T_STR_RESERVED_11 5060
#define JH5T_STR_RESERVED_12 5070
#define JH5T_STR_RESERVED_13 5080
#define JH5T_STR_RESERVED_14 5090
#define JH5T_STR_RESERVED_15 5100
#define JH5T_STR_RESERVED_3 5110
#define JH5T_STR_RESERVED_4 5120
#define JH5T_STR_RESERVED_5 5130
#define JH5T_STR_RESERVED_6 5140
#define JH5T_STR_RESERVED_7 5150
#define JH5T_STR_RESERVED_8 5160
#define JH5T_STR_RESERVED_9 5170
#define JH5T_STR_SPACEPAD 5180
#define JH5T_STRING 5190
#define JH5T_TIME 5200
#define JH5T_UNIX_D32BE 5210
#define JH5T_UNIX_D32LE 5220
#define JH5T_UNIX_D64BE 5230
#define JH5T_UNIX_D64LE 5240
#define JH5T_VARIABLE 5245
#define JH5T_VLEN 5250
#define JH5Z_CB_CONT 5260
#define JH5Z_CB_ERROR 5270
#define JH5Z_CB_FAIL 5280
#define JH5Z_CB_NO 5290
#define JH5Z_DISABLE_EDC 5300
#define JH5Z_ENABLE_EDC 5310
#define JH5Z_ERROR_EDC 5320
#define JH5Z_FILTER_DEFLATE 5330
#define JH5Z_FILTER_ERROR 5340
#define JH5Z_FILTER_FLETCHER32 5350
#define JH5Z_FILTER_MAX 5360
#define JH5Z_FILTER_NONE 5370
#define JH5Z_FILTER_RESERVED 5380
#define JH5Z_FILTER_SHUFFLE 5390
#define JH5Z_FILTER_SZIP 5400
#define JH5Z_FLAG_DEFMASK 5410
#define JH5Z_FLAG_INVMASK 5420
#define JH5Z_FLAG_MANDATORY 5430
#define JH5Z_FLAG_OPTIONAL 5440
#define JH5Z_FLAG_REVERSE 5450
#define JH5Z_FLAG_SKIP_EDC 5460
#define JH5Z_MAX_NFILTERS 5470
#define JH5Z_NO_EDC 5480
#define JH5Z_FILTER_CONFIG_ENCODE_ENABLED 5490
#define JH5Z_FILTER_CONFIG_DECODE_ENABLED 5500
