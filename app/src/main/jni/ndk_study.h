//
// Created by hans on 05-Dec-15.
//
#include <jni.h>

#ifndef NDKSTUDY_NDK_STUDY_H
#define NDKSTUDY_NDK_STUDY_H
    #ifdef __cplusplus
    extern "C" {
    #endif
    /*
     * Class:     com_dataforce_runabout_BaseActivity
     * Method:    getGoogleGeocodingAPIKey
     * Signature: ()Ljava/lang/String;
     */
    JNIEXPORT jstring JNICALL Java_com_pjwin_ndkstudy_MainActivity_getNDKString
            (JNIEnv *env, jobject);

    JNIEXPORT jstring JNICALL
        Java_com_pjwin_ndkstudy_MainActivity_getNDKTest(JNIEnv *env, jobject instance);

    JNIEXPORT void JNICALL
        Java_com_pjwin_ndkstudy_MainActivity_incrementCount(JNIEnv *env, jobject instance, jint inc);

    JNIEXPORT void JNICALL
        Java_com_pjwin_ndkstudy_MainActivity_setLabelStrNative(JNIEnv *env, jobject instance,
            jstring str_);

    JNIEXPORT void JNICALL
        Java_com_pjwin_ndkstudy_MainActivity_labelStrAppend(JNIEnv *env, jobject instance,
                                                            jstring appStr_);

    JNIEXPORT void JNICALL
        Java_com_pjwin_ndkstudy_MainActivity_updateCustomerNative(JNIEnv *env, jobject instance);

    JNIEXPORT jintArray JNICALL
        Java_com_pjwin_ndkstudy_ImageUtil_toImageReliefNative(JNIEnv *env, jobject instance,
            jintArray buff, jint width, jint height);

#ifdef __cplusplus
    }
    #endif
#endif //NDKSTUDY_NDK_STUDY_H
