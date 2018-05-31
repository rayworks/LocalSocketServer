//
// Created by Sean Zhou on 5/30/18.
//
//

#include <jni.h>
#include <android/log.h>
#include <sys/socket.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <sys/un.h>
#include <linux/errno.h>
#include <errno.h>

#define LOCAL_SOCKET_SERVER_NAME "ray_local"

namespace com_rayworks_net_native_client {

    static const char *const tag = "build client";
    static const int BUF_SIZE = 512;
    static int sock;

    static void destroy_client(JNIEnv *env, jclass clazz) {
        if (sock >= 0) {
            __android_log_print(ANDROID_LOG_DEBUG, tag, ">>> closing socket ");
            close(sock);
        }
    }

    static void build_client(JNIEnv *env, jclass clazz) {
        __android_log_print(ANDROID_LOG_INFO, tag, ">>> ready to build");

        struct sockaddr_un addr;
        socklen_t len;

        addr.sun_family = AF_LOCAL;

        // also check : https://stackoverflow.com/questions/7516018/android-localserversocket

        /*
         * Note: The path in this case is *not* supposed to be
         * '\0'-terminated. ("man 7 unix" for the gory details.)
         */
        addr.sun_path[0] = '\0';

        strcpy(&addr.sun_path[1], LOCAL_SOCKET_SERVER_NAME);
        size_t name_len = strlen(LOCAL_SOCKET_SERVER_NAME);
        len = name_len + offsetof(struct sockaddr_un, sun_path) + 1;

        sock = socket(PF_LOCAL, SOCK_STREAM, 0);
        if (sock < 0) {
            __android_log_print(ANDROID_LOG_ERROR, tag, "socket failure");
            return;
        }

        if (connect(sock, (struct sockaddr *) &addr, len) < 0) {
            __android_log_print(ANDROID_LOG_ERROR, tag, "connect failure");
            return;
        }

        ssize_t num_read;
        char buf[BUF_SIZE];
        while ((num_read = read(sock, buf, BUF_SIZE)) > 0) {
            if (write(STDOUT_FILENO, buf, num_read) != num_read) {
                __android_log_print(ANDROID_LOG_ERROR, tag, "write failure");
                return;
            } else {
                __android_log_print(ANDROID_LOG_DEBUG, tag, "Received: %s", buf);
            }
        }

        if (num_read == 0) {
            __android_log_print(ANDROID_LOG_ERROR, tag, "EOF reached");
        } else if (num_read == -1) {
            if (errno == EIO) {
                __android_log_print(ANDROID_LOG_ERROR, tag, "IO error during read");
            } else if (errno == ECONNRESET) {
                __android_log_print(ANDROID_LOG_ERROR, tag, "RESET by peer");
            } else {
                __android_log_print(ANDROID_LOG_ERROR, tag, "Error no %d", errno);
            }
        }

    }

    static JNINativeMethod method_table[] = {
            {"startClient", "()V", (void *) build_client},
            {"stopClient",  "()V", (void *) destroy_client}
    };
}

using namespace com_rayworks_net_native_client;
extern "C" jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    jclass clazz = env->FindClass("com/rayworks/net/NativeClientHelper");
    if (clazz) {
        if (env->RegisterNatives(clazz, method_table,
                                 sizeof(method_table) / sizeof(method_table[0])) != JNI_OK) {
            return -1;
        }

        env->DeleteLocalRef(clazz);

        return JNI_VERSION_1_6;
    }

    return -1;

}