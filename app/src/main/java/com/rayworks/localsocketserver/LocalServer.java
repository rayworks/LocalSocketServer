package com.rayworks.localsocketserver;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import java.io.IOException;

public class LocalServer {
    public static final String RAY_LOCAL_SRV = "ray_local";
    public static final int PORT = 12346;

    private volatile LocalServerSocket serverSocket;
    private volatile boolean live = true;
    private volatile LocalSocket inComingSock;

    public LocalServer() {
        try {
            this.serverSocket = new LocalServerSocket(RAY_LOCAL_SRV);
            LocalSocketAddress address = serverSocket.getLocalSocketAddress();
            System.out.println(">>> addr: " + address.getName() + "|" + address.getNamespace());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** * Listens the incoming connections (blocking) */
    public void prepare() {
        if (serverSocket == null) {
            System.err.println(">>> LocalServerSocket not initialized.");
            return;
        }

        try {
            System.out.println(">>> server is started...");

            while (live) {
                // quit from 'accept' ?
                inComingSock = serverSocket.accept();

                byte[] bytes = "Hello from Java Srv!".getBytes();
                inComingSock.getOutputStream().write(bytes);

                /*inComingSock.getOutputStream().close();
                inComingSock.close();*/
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dispose() {
        try {
            live = false;
            if (inComingSock != null) {
                inComingSock.getOutputStream().close();
                inComingSock.close();
            }

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
