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

/*
 *  This is a utility program used by the HDF Java-C wrapper layer to
 *  generate exceptions.  This may be called from any part of the
 *  Java-C interface.
 *
 */
#ifdef __cplusplus
extern "C" {
#endif

#include "hdf5.h"
#include <stdio.h>
#include "jni.h"
/*
#include "H5Eprivate.h"
*/
/*  These types are copied from H5Eprivate.h
 *  They should be moved to a public include file, and deleted from
 *  here.
 */
#define H5E_NSLOTS      32      /*number of slots in an error stack */
/*
* The list of error messages in the system is kept as an array of
* error_code/message pairs, one for major error numbers and another for
* minor error numbers.
*/
typedef struct H5E_major_mesg_t {
    H5E_major_t error_code;
    const char  *str;
} H5E_major_mesg_t;

typedef struct H5E_minor_mesg_t {
    H5E_minor_t error_code;
    const char  *str;
} H5E_minor_mesg_t;

/* major and minor error numbers */
typedef struct H5E_num_t {
    int maj_num;
    int min_num;
} H5E_num_t;

int getMajorErrorNumber();
int getMinorErrorNumber();

/* get the major and minor error numbers on the top of the erroe stack */
static
herr_t walk_error_callback(int n, H5E_error_t *err_desc, void *_err_nums)
{
    H5E_num_t *err_nums = (H5E_num_t *)_err_nums;

    if (err_desc) {
        err_nums->maj_num = err_desc->maj_num;
        err_nums->min_num = err_desc->min_num;
    }

    return 0;
}


char *defineHDF5LibraryException(int maj_num);


/*
 * Class:     ncsa_hdf_hdf5lib_exceptions_HDF5Library
 * Method:    H5error_off
 * Signature: ()I
 *
 */
JNIEXPORT jint JNICALL Java_ncsa_hdf_hdf5lib_H5_H5error_1off
  (JNIEnv *env, jclass clss )
{
    return H5Eset_auto(H5E_DEFAULT, NULL, NULL);
}


/*
 * Class:     ncsa_hdf_hdf5lib_exceptions_HDFLibraryException
 * Method:    printStackTrace0
 * Signature: (Ljava/lang/Object;)V
 *
 *  Call the HDF-5 library to print the HDF-5 error stack to 'file_name'.
 */
JNIEXPORT void JNICALL Java_ncsa_hdf_hdf5lib_exceptions_HDF5LibraryException_printStackTrace0
  (JNIEnv *env, jobject obj, jstring file_name)
{
    FILE *stream;
    char *file;

    if (file_name == NULL)
        H5Eprint(H5E_DEFAULT, stderr);
    else
    {
#ifdef __cplusplus
        file = (char *)env->GetStringUTFChars(file_name,0);
#else
        file = (char *)(*env)->GetStringUTFChars(env,file_name,0);
#endif
        stream = fopen(file, "a+");
        H5Eprint(H5E_DEFAULT, stream);
#ifdef __cplusplus
        env->ReleaseStringUTFChars(file_name, file);
#else
        (*env)->ReleaseStringUTFChars(env, file_name, file);
#endif
        if (stream) fclose(stream);
    }
}

/*
 * Class:     ncsa_hdf_hdf5lib_exceptions_HDFLibraryException
 * Method:    getMajorErrorNumber
 * Signature: ()I
 *
 *  Extract the HDF-5 major error number from the HDF-5 error stack.
 *
 *  Note:  This relies on undocumented, 'private' code in the HDF-5
 *  library.  Later releases will have a public interface for this
 *  purpose.
 */
JNIEXPORT jint JNICALL Java_ncsa_hdf_hdf5lib_exceptions_HDF5LibraryException_getMajorErrorNumber
  (JNIEnv *env, jobject obj)
{
    H5E_num_t err_nums;

    H5Ewalk(H5E_DEFAULT, H5E_WALK_DOWNWARD, walk_error_callback, &err_nums);

    return (int) err_nums.maj_num;
}

int getMajorErrorNumber()
{
    H5E_num_t err_nums;

    H5Ewalk(H5E_DEFAULT, H5E_WALK_DOWNWARD, walk_error_callback, &err_nums);

    return (int) err_nums.maj_num;
}

/*
 * Class:     ncsa_hdf_hdf5lib_exceptions_HDFLibraryException
 * Method:    getMinorErrorNumber
 * Signature: ()I
 *
 *  Extract the HDF-5 minor error number from the HDF-5 error stack.
 *
 *  Note:  This relies on undocumented, 'private' code in the HDF-5
 *  library.  Later releases will have a public interface for this
 *  purpose.
 */
JNIEXPORT jint JNICALL Java_ncsa_hdf_hdf5lib_exceptions_HDF5LibraryException_getMinorErrorNumber
  (JNIEnv *env, jobject obj)
{
    return (jint) getMinorErrorNumber();
}

int getMinorErrorNumber()
{
    H5E_num_t err_nums;

    H5Ewalk(H5E_DEFAULT, H5E_WALK_DOWNWARD, walk_error_callback, &err_nums);

    return (int) err_nums.min_num;
}

/*
 *  Routine to raise particular Java exceptions from C
 */

/*
 *  Create and throw an 'outOfMemoryException'
 *
 *  Note:  This routine never returns from the 'throw',
 *  and the Java native method immediately raises the
 *  exception.
 */
jboolean h5outOfMemory( JNIEnv *env, char *functName)
{
    jmethodID jm;
    jclass jc;
    char * args[2];
    jobject ex;
    jstring str;
    int rval;

#ifdef __cplusplus
    jc = env->FindClass("java/lang/OutOfMemoryError");
#else
    jc = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
#endif
    if (jc == NULL) {
        return JNI_FALSE;
    }
#ifdef __cplusplus
    jm = env->GetMethodID(jc, "<init>", "(Ljava/lang/String;)V");
#else
    jm = (*env)->GetMethodID(env, jc, "<init>", "(Ljava/lang/String;)V");
#endif
    if (jm == NULL) {
        return JNI_FALSE;
    }

#ifdef __cplusplus
    str = (env)->NewStringUTF(functName);
#else
    str = (*env)->NewStringUTF(env,functName);
#endif
    args[0] = (char *)str;
    args[1] = 0;

#ifdef __cplusplus
    ex = env->NewObjectA ( jc, jm, (jvalue *)args );

    rval = env->Throw( (jthrowable ) ex );
#else
    ex = (*env)->NewObjectA ( env, jc, jm, (jvalue *)args );

    rval = (*env)->Throw(env, ex );
#endif
    if (rval < 0) {
        fprintf(stderr, "FATAL ERROR:  OutOfMemoryError: Throw failed\n");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}


/*
 *  A fatal error in a JNI call
 *  Create and throw an 'InternalError'
 *
 *  Note:  This routine never returns from the 'throw',
 *  and the Java native method immediately raises the
 *  exception.
 */
jboolean h5JNIFatalError( JNIEnv *env, char *functName)
{
    jmethodID jm;
    jclass jc;
    char * args[2];
    jobject ex;
    jstring str;
    int rval;

#ifdef __cplusplus
    jc = env->FindClass("java/lang/InternalError");
#else
    jc = (*env)->FindClass(env, "java/lang/InternalError");
#endif
    if (jc == NULL) {
        return JNI_FALSE;
    }
#ifdef __cplusplus
    jm = env->GetMethodID(jc, "<init>", "(Ljava/lang/String;)V");
#else
    jm = (*env)->GetMethodID(env, jc, "<init>", "(Ljava/lang/String;)V");
#endif
    if (jm == NULL) {
        return JNI_FALSE;
    }

#ifdef __cplusplus
    str = env->NewStringUTF(functName);
#else
    str = (*env)->NewStringUTF(env,functName);
#endif
    args[0] = (char *)str;
    args[1] = 0;
#ifdef __cplusplus
    ex = env->NewObjectA ( jc, jm, (jvalue *)args );

    rval = env->Throw( (jthrowable) ex );
#else
    ex = (*env)->NewObjectA ( env, jc, jm, (jvalue *)args );

    rval = (*env)->Throw(env, ex );
#endif
    if (rval < 0) {
        fprintf(stderr, "FATAL ERROR:  JNIFatal: Throw failed\n");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 *  A NULL argument in an HDF5 call
 *  Create and throw an 'NullPointerException'
 *
 *  Note:  This routine never returns from the 'throw',
 *  and the Java native method immediately raises the
 *  exception.
 */
jboolean h5nullArgument( JNIEnv *env, char *functName)
{
    jmethodID jm;
    jclass jc;
    char * args[2];
    jobject ex;
    jstring str;
    int rval;

#ifdef __cplusplus
    jc = env->FindClass("java/lang/NullPointerException");
#else
    jc = (*env)->FindClass(env, "java/lang/NullPointerException");
#endif
    if (jc == NULL) {
        return JNI_FALSE;
    }
#ifdef __cplusplus
    jm = env->GetMethodID(jc, "<init>", "(Ljava/lang/String;)V");
#else
    jm = (*env)->GetMethodID(env, jc, "<init>", "(Ljava/lang/String;)V");
#endif
    if (jm == NULL) {
        return JNI_FALSE;
    }

#ifdef __cplusplus
    str = env->NewStringUTF(functName);
#else
    str = (*env)->NewStringUTF(env,functName);
#endif
    args[0] = (char *)str;
    args[1] = 0;
#ifdef __cplusplus
    ex = env->NewObjectA ( jc, jm, (jvalue *)args );

    rval = env->Throw((jthrowable) ex );
#else
    ex = (*env)->NewObjectA ( env, jc, jm, (jvalue *)args );

    rval = (*env)->Throw(env, ex );
#endif

    if (rval < 0) {
        fprintf(stderr, "FATAL ERROR:  NullPointer: Throw failed\n");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 *  A bad argument in an HDF5 call
 *  Create and throw an 'IllegalArgumentException'
 *
 *  Note:  This routine never returns from the 'throw',
 *  and the Java native method immediately raises the
 *  exception.
 */
jboolean h5badArgument( JNIEnv *env, char *functName)
{
    jmethodID jm;
    jclass jc;
    char * args[2];
    jobject ex;
    jstring str;
    int rval;

#ifdef __cplusplus
    jc = env->FindClass("java/lang/IllegalArgumentException");
#else
    jc = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
#endif
    if (jc == NULL) {
        return JNI_FALSE;
    }
#ifdef __cplusplus
    jm = env->GetMethodID(jc, "<init>", "(Ljava/lang/String;)V");
#else
    jm = (*env)->GetMethodID(env, jc, "<init>", "(Ljava/lang/String;)V");
#endif
    if (jm == NULL) {
        return JNI_FALSE;
    }

#ifdef __cplusplus
    str = env->NewStringUTF(functName);
#else
    str = (*env)->NewStringUTF(env,functName);
#endif
    args[0] = (char *)str;
    args[1] = 0;
#ifdef __cplusplus
    ex = env->NewObjectA ( jc, jm, (jvalue *)args );

    rval = env->Throw((jthrowable) ex );
#else
    ex = (*env)->NewObjectA ( env, jc, jm, (jvalue *)args );

    rval = (*env)->Throw(env, ex );
#endif
    if (rval < 0) {
        fprintf(stderr, "FATAL ERROR:  BadArgument: Throw failed\n");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 *  Some feature Not implemented yet
 *  Create and throw an 'UnsupportedOperationException'
 *
 *  Note:  This routine never returns from the 'throw',
 *  and the Java native method immediately raises the
 *  exception.
 */
jboolean h5unimplemented( JNIEnv *env, char *functName)
{
    jmethodID jm;
    jclass jc;
    char * args[2];
    jobject ex;
    jstring str;
    int rval;

#ifdef __cplusplus
    jc = env->FindClass("java/lang/UnsupportedOperationException");
#else
    jc = (*env)->FindClass(env, "java/lang/UnsupportedOperationException");
#endif
    if (jc == NULL) {
        return JNI_FALSE;
    }
#ifdef __cplusplus
    jm = env->GetMethodID(jc, "<init>", "(Ljava/lang/String;)V");
#else
    jm = (*env)->GetMethodID(env, jc, "<init>", "(Ljava/lang/String;)V");
#endif
    if (jm == NULL) {
        return JNI_FALSE;
    }

#ifdef __cplusplus
    str = env->NewStringUTF(functName);
#else
    str = (*env)->NewStringUTF(env,functName);
#endif
    args[0] = (char *)str;
    args[1] = 0;
#ifdef __cplusplus
    ex = env->NewObjectA ( jc, jm, (jvalue *)args );

    rval = env->Throw((jthrowable) ex );
#else
    ex = (*env)->NewObjectA ( env, jc, jm, (jvalue *)args );

    rval = (*env)->Throw(env, ex );
#endif
    if (rval < 0) {
        fprintf(stderr, "FATAL ERROR:  Unsupported: Throw failed\n");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 *  h5libraryError()   determines the HDF-5 major error code
 *  and creates and throws the appropriate sub-class of
 *  HDF5LibraryException().  This routine should be called
 *  whenever a call to the HDF-5 library fails, i.e., when
 *  the return is -1.
 *
 *  Note:  This routine never returns from the 'throw',
 *  and the Java native method immediately raises the
 *  exception.
 */
jboolean h5libraryError( JNIEnv *env )
{
    jmethodID jm;
    jclass jc;
    jvalue args[4];
    char *exception;
    jobject ex;
    jstring min_msg_str, maj_msg_str;
    char *min_msg, *maj_msg;
    int rval, min_num, maj_num;

    maj_num = (int)getMajorErrorNumber();
    maj_msg = (char *)H5Eget_major((H5E_major_t)maj_num);
    exception = (char *)defineHDF5LibraryException(maj_num);

#ifdef __cplusplus
    jc = env->FindClass(exception);
#else
    jc = (*env)->FindClass(env, exception);
#endif
    if (jc == NULL) {
        return JNI_FALSE;
    }
#ifdef __cplusplus
    jm = env->GetMethodID(jc, "<init>", "(ILjava/lang/String;ILjava/lang/String;)V");
#else
    jm = (*env)->GetMethodID(env, jc, "<init>", "(ILjava/lang/String;ILjava/lang/String;)V");
#endif
    if (jm == NULL) {
        fprintf(stderr, "FATAL ERROR:  h5libraryError: Cannot find constructor\n");
        return JNI_FALSE;
    }

    min_num = (int)getMinorErrorNumber();
    min_msg = (char *)H5Eget_minor((H5E_minor_t)min_num);
#ifdef __cplusplus
    maj_msg_str = env->NewStringUTF(maj_msg);
    min_msg_str = env->NewStringUTF(min_msg);
#else
    maj_msg_str = (*env)->NewStringUTF(env,maj_msg);
    min_msg_str = (*env)->NewStringUTF(env,min_msg);
#endif
    if (maj_msg_str == NULL || min_msg_str == NULL)
    {
        fprintf(stderr, "FATAL ERROR: h5libraryError: Out of Memory\n");
        return JNI_FALSE;
    }

    args[0].i = maj_num;
    args[1].l = maj_msg_str;
    args[2].i = min_num;
    args[3].l = min_msg_str;
#ifdef __cplusplus
    ex = env->NewObjectA ( jc, jm, args );

    rval = env->Throw((jthrowable) ex );
#else
    ex = (*env)->NewObjectA ( env, jc, jm, (jvalue *)args );

    rval = (*env)->Throw(env, ex );
#endif
    if (rval < 0) {
        fprintf(stderr, "FATAL ERROR:  h5libraryError: Throw failed\n");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}


/*
 *  A constant that is not defined in J2C throws an 'IllegalArgumentException'.
 *
 *  Note:  This routine never returns from the 'throw',
 *  and the Java native method immediately raises the
 *  exception.
 */
jboolean h5illegalConstantError(JNIEnv *env)
{
    jmethodID jm;
    jclass jc;
    char * args[2];
    jobject ex;
    jstring str;
    int rval;

#ifdef __cplusplus
    jc = env->FindClass("java/lang/IllegalArgumentException");
#else
    jc = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
#endif
    if (jc == NULL) {
        return JNI_FALSE;
    }
#ifdef __cplusplus
    jm = env->GetMethodID(jc, "<init>", "(Ljava/lang/String;)V");
#else
    jm = (*env)->GetMethodID(env, jc, "<init>", "(Ljava/lang/String;)V");
#endif
    if (jm == NULL) {
        return JNI_FALSE;
    }

#ifdef __cplusplus
    str = env->NewStringUTF("Illegal java constant");
#else
    str = (*env)->NewStringUTF(env,"Illegal java constant");
#endif
    args[0] = (char *)str;
    args[1] = 0;
#ifdef __cplusplus
    ex = env->NewObjectA ( jc, jm, (jvalue *)args );

    rval = env->Throw((jthrowable) ex );
#else
    ex = (*env)->NewObjectA ( env, jc, jm, (jvalue *)args );

    rval = (*env)->Throw(env, ex );
#endif
    if (rval < 0) {
        fprintf(stderr, "FATAL ERROR:  Unsupported: Throw failed\n");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*  raiseException().  This routine is called to generate
 *  an arbitrary Java exception with a particular message.
 *
 *  Note:  This routine never returns from the 'throw',
 *  and the Java native method immediately raises the
 *  exception.
 */
jboolean h5raiseException( JNIEnv *env, char *exception, char *message)
{
    jmethodID jm;
    jclass jc;
    char * args[2];
    jobject ex;
    jstring str;
    int rval;

#ifdef __cplusplus
    jc = env->FindClass(exception);
#else
    jc = (*env)->FindClass(env, exception);
#endif
    if (jc == NULL) {
        return JNI_FALSE;
    }
#ifdef __cplusplus
    jm = env->GetMethodID(jc, "<init>", "(Ljava/lang/String;)V");
#else
    jm = (*env)->GetMethodID(env, jc, "<init>", "(Ljava/lang/String;)V");
#endif
    if (jm == NULL) {
        return JNI_FALSE;
    }

#ifdef __cplusplus
    str = env->NewStringUTF(message);
#else
    str = (*env)->NewStringUTF(env,message);
#endif
    args[0] = (char *)str;
    args[1] = 0;
#ifdef __cplusplus
    ex = env->NewObjectA (  jc, jm, (jvalue *)args );

    rval = env->Throw( (jthrowable)ex );
#else
    ex = (*env)->NewObjectA ( env, jc, jm, (jvalue *)args );

    rval = (*env)->Throw(env, ex );
#endif
    if (rval < 0) {
        fprintf(stderr, "FATAL ERROR:  raiseException: Throw failed\n");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
jboolean buildException( JNIEnv *env, char *exception, jint HDFerr)
{
    jmethodID jm;
    jclass jc;
    int args[2];
    jobject ex;
    int rval;


    jc = (*env)->FindClass(env, exception);
    if (jc == NULL) {
        return JNI_FALSE;
    }
    jm = (*env)->GetMethodID(env, jc, "<init>", "(I)V");
    if (jm == NULL) {
        return JNI_FALSE;
    }
    args[0] = HDFerr;
    args[1] = 0;

    ex = (*env)->NewObjectA ( env, jc, jm, (jvalue *)args );

    rval = (*env)->Throw(env, ex );
    if (rval < 0) {
        fprintf(stderr, "FATAL ERROR:  raiseException: Throw failed\n");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}
*/

/*
 *  defineHDF5LibraryException()  returns the name of the sub-class
 *  which goes with an HDF-5 error code.
 */
char *defineHDF5LibraryException(int maj_num)
{
    H5E_major_t err_num = (H5E_major_t) maj_num;

    if (err_num == H5E_ARGS)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5FunctionArgumentException";
    else if (err_num == H5E_RESOURCE)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5ResourceUnavailableException";
    else if (err_num == H5E_INTERNAL)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5InternalErrorException";
    else if (err_num == H5E_FILE)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5FileInterfaceException";
    else if (err_num == H5E_IO)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5LowLevelIOException";
    else if (err_num == H5E_FUNC)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5FunctionEntryExitException";
    else if (err_num == H5E_ATOM)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5AtomException";
    else if (err_num == H5E_CACHE)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5MetaDataCacheException";
    else if (err_num == H5E_BTREE)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5BtreeException";
    else if (err_num == H5E_SYM)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5SymbolTableException";
    else if (err_num == H5E_HEAP)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5HeapException";
    else if (err_num == H5E_OHDR)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5ObjectHeaderException";
    else if (err_num == H5E_DATATYPE)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5DatatypeInterfaceException";
    else if (err_num == H5E_DATASPACE)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5DataspaceInterfaceException";
    else if (err_num == H5E_DATASET)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5DatasetInterfaceException";
    else if (err_num == H5E_STORAGE)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5DataStorageException";
    else if (err_num == H5E_PLIST)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5PropertyListInterfaceException";
    else if (err_num == H5E_ATTR)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5AttributeException";
    else if (err_num == H5E_PLINE)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5DataFiltersException";
    else if (err_num == H5E_EFL)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5ExternalFileListException";
    else if (err_num == H5E_REFERENCE)
        return "ncsa/hdf/hdf5lib/exceptions/HDF5ReferenceException";
    else
        return "ncsa/hdf/hdf5lib/exceptions/HDF5LibraryException";

}

#ifdef __cplusplus
}
#endif
