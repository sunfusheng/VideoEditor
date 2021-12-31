#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_sunfusheng_video_editor_NativeLib_stringFromJNI(
        JNIEnv *env,
        jobject) {
    std::string hello = "Hello from video editor C++";
    return env->NewStringUTF(hello.c_str());
}