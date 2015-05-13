// Copyright 2011 Google Inc. All Rights Reserved.

package com.wifidirect.core;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(final Intent intent) {

			        Context context = getApplicationContext();
			        if (intent.getAction().equals(ACTION_SEND_FILE)) {
			            String[] fileUris = intent.getStringArrayExtra("imgUris");
			            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
			            
			            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
			            for(int i=0;i<fileUris.length;i++){
			            	Socket socket = new Socket();
					 try {
			                Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
			                socket.bind(null);
			                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

			                Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
			                OutputStream stream = socket.getOutputStream();
			                ContentResolver cr = context.getContentResolver();
			                InputStream is = null;
			                try {
			                    is = cr.openInputStream(Uri.parse("file://"+fileUris[i]));
			                } catch (FileNotFoundException e) {
			                    Log.d(WiFiDirectActivity.TAG, e.toString());
			                }
			                DeviceDetailFragment.copyFile(is, stream);
			                Log.d(WiFiDirectActivity.TAG, "Client: Data written");
			            } catch (IOException e) {
			                Log.e(WiFiDirectActivity.TAG, e.getMessage());
			            } finally {
			                if (socket != null) {
			                    if (socket.isConnected()) {
			                        try {
			                            socket.close();
			                        } catch (IOException e) {
			                            // Give up
			                            e.printStackTrace();
			                        }
			                    }
			                }
			            }
			        }
    }
    }
}

