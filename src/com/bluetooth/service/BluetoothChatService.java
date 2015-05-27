package com.bluetooth.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.UUID;

import com.bluetooth.activity.BluetoothChatActivity;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.widget.EditText;
public class BluetoothChatService{
	private EditText PhoneEdit;
	private EditText MessageEdit;
	
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;

    private static final String NAME = "BluetoothChat";

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");	//change by chongqing jinou	

    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2; 
    public static final int STATE_CONNECTED = 3;
    
    private Context context;
    public BluetoothChatService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
        this.context=context;
    }

  
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        mHandler.obtainMessage(BluetoothChatActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return mState;
    }

   
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

  
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "连接到：" + device);

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

  
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "连接");

        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}

        mConnectedThread = new ConnectedThread(socket,this.context);
        mConnectedThread.start();

        Message msg = mHandler.obtainMessage(BluetoothChatActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChatActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

   
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        setState(STATE_NONE);
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
    }

    public void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }


    private void connectionFailed() {
        setState(STATE_LISTEN);
        
        Message msg = mHandler.obtainMessage(BluetoothChatActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChatActivity.TOAST, "无法连接设备");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    
    private void connectionLost() {
    	int i=0;
        Message msg = mHandler.obtainMessage(BluetoothChatActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChatActivity.TOAST, "设备断开连接");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification nt = new Notification();
        nt.defaults = Notification.DEFAULT_SOUND;
        int soundId = new Random(System.currentTimeMillis()).nextInt(Integer.MAX_VALUE);
        mgr.notify(soundId, nt);
    }

   
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;

            while (mState != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() 失败", e);
                    break;
                }

                
                if (socket != null) {
                    synchronized (BluetoothChatService.this) {
                        switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                          
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "不能关闭这些连接", e);
                            }
                            break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "结束mAcceptThread");
        }

        public void cancel() {
            if (D) Log.d(TAG, "取消 " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "关闭失败", e);
            }
        }
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() 失败", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "开始mConnectThread");
            setName("ConnectThread");

            mAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "关闭连接失败", e2);
                }
                BluetoothChatService.this.start();
                return;
            }

            synchronized (BluetoothChatService.this) {
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "关闭连接失败", e);
            }
        }
    }

   
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private Context context;

        public ConnectedThread(BluetoothSocket socket, Context context) {
            Log.d(TAG, "创建 ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            this.context=context;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            int bytes;
            String str1 = "";            
            while (true) {
                try {
                	byte[] buffer = new byte[256];
                	
                    bytes = mmInStream.read(buffer);                    
                    String readStr = new String(buffer, 0, bytes);
                    String str = bytes2HexString(buffer).replaceAll("00","").trim();
                    if(bytes>0)
                    {	
                    	if (str.endsWith("OD")) {          	
                    		String phone = PhoneEdit.getText().toString();
                    		String message = MessageEdit.getText().toString();
                    		byte[] buffer1 = (str1+readStr).getBytes();
                    		mHandler.obtainMessage(BluetoothChatActivity.MESSAGE_READ, buffer1.length, -1, buffer1)
                            .sendToTarget();
                    		str1 = "";
                    		Intent intent = new Intent("call.broastcast");
                    		intent.putExtra("phone", phone);
                    		intent.putExtra("message", message);
                    		this.context.sendBroadcast(intent);
						}
                    	else{
                    		if (!str.contains("0A")) {
                    			str1 = str1+readStr;
							}else{
								if (!str.equals("0A")&&str.endsWith("0A")) {
									mHandler.obtainMessage(BluetoothChatActivity.MESSAGE_READ, bytes, -1, buffer)
		                            .sendToTarget();
								}
							}
						}
	                    
                    }
                    else
                    {
                        Log.e(TAG, "disconnected");
                        connectionLost();
                        
                        if(mState != STATE_NONE)
                        {
                            Log.e(TAG, "disconnected");
                        	BluetoothChatService.this.start();
                        }
                        break;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    
                    if(mState != STATE_NONE)
                    {
                    	BluetoothChatService.this.start();
                    }
                    break;
                }
            }
        }


		
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                mHandler.obtainMessage(BluetoothChatActivity.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
   
    public static String bytes2HexString(byte[] b) {
  	  String ret = "";
  	  for (int i = 0; i < b.length; i++) {
  	   String hex = Integer.toHexString(b[ i ] & 0xFF);
  	   if (hex.length() == 1) {
  	    hex = '0' + hex;
  	   }
  	   ret += hex.toUpperCase();
  	  }
  	  return ret;
  	}
    
}
