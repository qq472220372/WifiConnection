/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.location.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.location.main.LocationDeviceListFragment.DeviceActionListener;
import com.quicky.wifi.R;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class LocationDeviceDetailFragment extends Fragment implements ConnectionInfoListener {

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    
    public Handler myHandler = new Handler() {  
        public void handleMessage(Message msg) {
        	if(msg.what==1){
              //启动百度地图，并监听客户端的位置信息
              Intent intent = new Intent();
              Bundle bundle =new Bundle();
              bundle.putString("type", "server");
              intent.putExtras(bundle);
              intent.setClass(getActivity(), LocationDemo.class);
              startActivity(intent);
              return ;
        	}
            Bundle data = msg.getData();
            String result = data.getString("Uri");
          Intent intent = new Intent();
          intent.setAction(android.content.Intent.ACTION_VIEW);
          intent.setDataAndType(Uri.parse(result), "image/*");
          getActivity().startActivity(intent);
        }   
   }; 
   

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        Button startClient = (Button)mContentView.findViewById(R.id.btn_start_client);
        startClient.setText("发送位置信息");
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
                        );
                ((DeviceActionListener) getActivity()).connect(config);

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
//                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                        intent.setType("image/*");
//                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
//                    	Intent intent = new Intent();
//                    	intent.setClass(getActivity(), MainActivity.class);
//                    	startActivityForResult(intent,1);
                    	//开启百度地图定位，获取GPS坐标并发送给服务端
                		Intent intent = new Intent();
                		Bundle bundle = new Bundle();
                		bundle.putString("type", "client");
                		//bundle.putString("host", info.groupOwnerAddress.getHostAddress());
                		bundle.putString("host", "192.168.49.1");
                		intent.putExtras(bundle);
                		intent.setClass(getActivity(), LocationDemo.class);
                		startActivity(intent);
                    }
                });

        return mContentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
    	Bundle imgBundle = data.getExtras();
    	String[] uris = imgBundle.getStringArray("imglist");
//    	for(int i = 0,port = 8988;i < uris.length; i++,port++){
//        String uri = uris[i];
//        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
//        statusText.setText("Sending: " + uri);
//        Log.d(WiFiDirectActivity.TAG, "Intent----------- " + uri);
//        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
//        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
//        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, "file://"+uri.toString());
//        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
//                info.groupOwnerAddress.getHostAddress());
//        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, port);
//        getActivity().startService(serviceIntent);
//    	}
      TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
      statusText.setText("Sending: " + uris.toString());
      Log.d(LocationWiFiDirectActivity.TAG, "Intent----------- " + uris.toString());
      Intent serviceIntent = new Intent(getActivity(), LocationTransferService.class);
      serviceIntent.setAction(LocationTransferService.ACTION_SEND_FILE);
      //serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, "file://"+uri.toString());
      serviceIntent.putExtra("imgUris", uris);
      serviceIntent.putExtra(LocationTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
              info.groupOwnerAddress.getHostAddress());
      serviceIntent.putExtra(LocationTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
      getActivity().startService(serviceIntent);
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                        : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());

        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed && info.isGroupOwner) {
        	
//            new Thread(new Runnable(){
//
//				@Override
//				public void run() {
//		            new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text),8988)
//		            .execute();
//					
//				}
//            	
//            }).start();
//            new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text),8988)
//            .execute();
            
//            //启动百度地图，并监听客户端的位置信息
//            Intent intent = new Intent();
//            intent.setClass(getActivity(), LocationDemo.class);
//            startActivity(intent);
            myHandler.sendEmptyMessage(1);
            
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case, we enable the
            // get file button.
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
                    .getString(R.string.client_text));
        }

        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    /**
     * Updates the UI with device data
     * 
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;
        private int port;
        /**
         * @param context
         * @param statusText
         */
        public FileServerAsyncTask(Context context, View statusText,int port) {
            this.context = context;
            this.statusText = (TextView) statusText;
            this.port = port;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                Log.d(LocationWiFiDirectActivity.TAG, "Server: Socket opened");
                while(true){
                Socket client = serverSocket.accept();
                new Thread(new Task(client,context,statusText)).start();
                }
//                Log.d(WiFiDirectActivity.TAG, "Server: connection done");
//                final File f = new File(Environment.getExternalStorageDirectory() + "/"
//                        + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
//                        + ".jpg");
//
//                File dirs = new File(f.getParent());
//                if (!dirs.exists())
//                    dirs.mkdirs();
//                f.createNewFile();
//
//                Log.d(WiFiDirectActivity.TAG, "server: copying files " + f.toString());
//                InputStream inputstream = client.getInputStream();
//                copyFile(inputstream, new FileOutputStream(f));
//                }
//                //serverSocket.close();
//                //return f.getAbsolutePath();
            } catch (IOException e) {
                Log.e(LocationWiFiDirectActivity.TAG, e.getMessage());
                return null;
            }
                
        }
        
        

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("File copied - " + result);
//                Intent intent = new Intent();
//                intent.setAction(android.content.Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
//                context.startActivity(intent);

            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            statusText.setText("Opening a server socket");
        }

    }

   
    
    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(LocationWiFiDirectActivity.TAG, e.toString());
            return false;
        }
        return true;
    }
    
    //服務器接收線程
     class Task implements Runnable {  
    	   
        private Socket socket;  
        private Context context;
        private TextView statusText;
          
        public Task(Socket socket ,Context context,TextView textView) {  
           this.socket = socket;  
           this.context = context;
           this.statusText = textView;
        }  
          
        public void run() {  
        	try{
          Log.d(LocationWiFiDirectActivity.TAG, "Server: connection done");
          final File f = new File(Environment.getExternalStorageDirectory() + "/"
                  + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
                  + ".jpg");

          File dirs = new File(f.getParent());
          if (!dirs.exists())
              dirs.mkdirs();
          f.createNewFile();

          Log.d(LocationWiFiDirectActivity.TAG, "server: copying files " + f.toString());
          InputStream inputstream = socket.getInputStream();
          copyFile(inputstream, new FileOutputStream(f));
      	//statusText.setText("File copied - " + f.getName());
          Message msg = new Message();
          Bundle data = new Bundle();
          data.putString("Uri", "file://"+f.getAbsolutePath());
          msg.setData(data);
          myHandler.sendMessage(msg);
      	socket.close();
        }catch (IOException e) {
            Log.e(LocationWiFiDirectActivity.TAG, e.getMessage());
        }
        }
          //serverSocket.close();
          //return f.getAbsolutePath();
        }
    
    }


