#include <jni.h>
// Native ABI self-check for the 32-bit user interface.
JNIEXPORT jstring JNICALL Java_df_root_MainActivity_nativeArchitecture(JNIEnv *env, jclass clazz) {
    (void)clazz;
#if defined(__arm__) && !defined(__aarch64__)
    return (*env)->NewStringUTF(env, "ARMv7 (32-bit)");
#else
#error "Expected 32-bit ARM architecture"
#endif
}
