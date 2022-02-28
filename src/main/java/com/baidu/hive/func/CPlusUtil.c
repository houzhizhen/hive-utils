// Save as "HelloJNI.c"
#include <jni.h>        // JNI header provided by JDK
#include <stdio.h>      // C Standard IO Header
#include "com_baidu_hive_func_CPlusUtil.h"   // Generated
 

JNIEXPORT jint JNICALL Java_com_baidu_hive_func_CPlusUtil_add
  (JNIEnv *env, jclass thisObj, jint a, jint b) {
   return a+b;
};