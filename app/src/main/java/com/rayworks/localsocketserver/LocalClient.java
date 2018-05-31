package com.rayworks.localsocketserver;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import java.io.IOException;
import java.io.InputStream;

public class LocalClient {
    volatile LocalSocket socket;

    public LocalClient() {
        socket = new LocalSocket();
    }

    public void dispose() {
        if (socket != null) {
            try {
                socket.getInputStream().close();
                socket.getOutputStream().close();

                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String connectAndRead() {
        try {

            LocalSocketAddress endpoint = new LocalSocketAddress(LocalServer.RAY_LOCAL_SRV);
            socket.connect(endpoint);

            byte[] buffer = new byte[512];
            InputStream inputStream = socket.getInputStream();

            int readCnt = inputStream.read(buffer);

            return new String(buffer, 0, readCnt);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "ERROR!";
    }
}
