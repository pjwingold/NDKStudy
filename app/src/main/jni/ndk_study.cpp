//
// Created by hans on 05-Dec-15.
//
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <android/bitmap.h>
#include <android/log.h>

#include "ndk_study.h"
#include "graphic.color.h"

#define APPNAME "ndk_study"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, APPNAME, __VA_ARGS__);

JNIEXPORT jstring JNICALL
Java_com_pjwin_ndkstudy_MainActivity_getNDKString(JNIEnv * env, jobject jobj) {
    return env->NewStringUTF("NDK test");
}

JNIEXPORT jstring JNICALL
Java_com_pjwin_ndkstudy_MainActivity_getNDKTest(JNIEnv *env, jobject instance) {
    return env->NewStringUTF("getNDKTest");
}

JNIEXPORT void JNICALL
Java_com_pjwin_ndkstudy_MainActivity_incrementCount(JNIEnv *env, jobject instance, jint inc) {
    jclass clz = (env)->GetObjectClass(instance);

    jfieldID fId = (env)->GetFieldID(clz, "count", "I");

    jint count = (jint) (env)->GetIntField(instance, fId);

    count += inc;

    (env)->SetIntField(instance, fId, count);
}

JNIEXPORT void JNICALL
Java_com_pjwin_ndkstudy_MainActivity_setLabelStrNative(JNIEnv *env, jobject instance,
                                                       jstring str_) {
    const char *str = env->GetStringUTFChars(str_, 0);

    if (str == NULL) {
        return;
    }

    jclass clz = (env)->GetObjectClass(instance);

    jfieldID fId = (env)->GetFieldID(clz, "labelStr", "Ljava/lang/String;");

    //jstring labelStr = (jstring) (env)->GetObjectField(instance, fId);

    /*jboolean iscopy;
    const char* tmpStr = (env)->GetStringUTFChars(str_, &iscopy);
    char *tmpStr;
    strcpy(tmpStr, tmpStr);
*/
    (env)->SetObjectField(instance, fId, str_);
    env->ReleaseStringUTFChars(str_, str);
}

JNIEXPORT void JNICALL
Java_com_pjwin_ndkstudy_MainActivity_labelStrAppend(JNIEnv *env, jobject instance,
                                                    jstring appStr_) {
    const char *appStr = env->GetStringUTFChars(appStr_, 0);

    if (appStr == NULL) {
        return;
    }

    jclass clz = env->GetObjectClass(instance);

    jfieldID fId = env->GetFieldID(clz, "labelStr", "Ljava/lang/String;");
    jstring currentJStr = (jstring) env->GetObjectField(instance, fId);
    const char* currentStr = env->GetStringUTFChars(currentJStr, JNI_FALSE);

    const char* paramStr = env->GetStringUTFChars(appStr_, JNI_FALSE);


    //const char* labelStr

    char tmpStr[20];
    strcpy(tmpStr, currentStr);
    strcat(tmpStr, paramStr);

    env->SetObjectField(instance, fId, env->NewStringUTF(tmpStr));

    //env->ReleaseStringUTFChars(appStr_, appStr);
}

JNIEXPORT void JNICALL
Java_com_pjwin_ndkstudy_MainActivity_updateCustomerNative(JNIEnv *env, jobject instance) {

}

JNIEXPORT jintArray JNICALL
Java_com_pjwin_ndkstudy_ImageUtil_toImageReliefNative(JNIEnv *env, jobject instance,
                                                      jintArray buff, jint width, jint height) {
    jint *source = env->GetIntArrayElements(buff, NULL);

    int newSize = width * height;

    //need to actually allocate memory in case of large size image
    jint* dest = (jint *) malloc(newSize * sizeof(jint));
    //jint dest[newSize];//can cause stack overflow error

    LOGI("new size is %d", newSize);

    int before = 0, after = 0, i, j;
    before = source[0];

    for (i = 0; i < width; i++) {
        for (j = 0; j < height; j++) {
            int index = j * width + i;
            int current = source[index];
            int r = red(current) - red(after) + 127;
            int g = green(current) - green(after) + 127;
            int b = blue(current) - blue(after) + 127;
            int a = alpha(current);

            int colour = argb(a, r, g, b);
            dest[index] = colour;
            after = before;
            before = current;
        }
    }

    jintArray result = env->NewIntArray(newSize);

    //copy from dest to result
    env->SetIntArrayRegion(result, 0, newSize, dest);

    env->ReleaseIntArrayElements(buff, source, 0);

    free(dest);

    return result;
}