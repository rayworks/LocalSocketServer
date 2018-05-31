package com.rayworks.localsocketserver;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.rayworks.net.NativeClientHelper;

public class MainActivity extends AppCompatActivity {

    private volatile boolean active = false;
    private Handler handler = new Handler();
    private TextView textView;
    private Thread threadSrv;
    private Thread threadClient;
    private LocalClient client;

    private LocalServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);

        threadSrv =
                new Thread("SrvThread") {

                    @Override
                    public void run() {
                        server = new LocalServer();
                        server.prepare();

                        System.out.println(">>> server thread exiting");
                    }
                };
        threadSrv.start();
    }

    public void onClicked(View view) {
        threadClient =
                new Thread("ClientThread") {
                    @Override
                    public void run() {

                        NativeClientHelper.startClient();

                        /*client = new LocalClient();
                        final String str = client.connectAndRead();

                        handler.post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        textView.setText(str);
                                    }
                                });*/

                        NativeClientHelper.stopClient();
                        System.out.println(">>> client thread exiting");
                    }
                };
        threadClient.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (server != null) server.dispose();
        if (client != null) client.dispose();
    }
}
