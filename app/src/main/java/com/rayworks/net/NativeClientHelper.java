package com.rayworks.net;

public class NativeClientHelper {
    static {
        System.loadLibrary("net_client");
    }

    public static native void startClient();
    public static native void stopClient();
}
