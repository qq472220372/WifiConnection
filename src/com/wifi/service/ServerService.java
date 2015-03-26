package com.wifi.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;

import com.example.andriodmvc.R;
import com.wifi.activity.ChatActivity;
import com.wifi.entity.ChatMsgEntity;
import com.wifi.util.ChatMsgViewAdapter;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * author:_phy
 */

public class ServerService extends IntentService{
	
	private static final String TAG = ChatActivity.class.getSimpleName();;
	int port = 0;
	ServerSocket serversocket = null;
	String rec = "";
	Activity chat = null;
	//HandleUtil handleUtil;
	
	public ServerService() {
		super("ServerService");
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		port = ((Integer) intent.getExtras().get("port")).intValue();
		//handleUtil = (HandleUtil)intent.getSerializableExtra("Handler");
		//新建Action
		intent = new Intent("com.wifi.broadcast"); 
		
		int len = 0;
		OutputStream outputstream = null;
		InputStream inputstream = null;
		byte[] rece = new byte[1000];
		String reces = null;	
		Log.v(TAG, "Server监听中");
		try {
			serversocket = new ServerSocket(port);
			Socket socket = serversocket.accept();
			inputstream = socket.getInputStream();
//			outputstream = socket.getOutputStream();
            
//			while(true){
//			len = inputstream.read(rece);// 接受客户端消息
//			if (len != 0) {
//				//reces = new String(rece, 0, len);
//				//rec = reces;
//                //服务器接收消息更新主界面
//				Log.v(TAG, "Service更新主界面");
//				intent.putExtra("Update", "update");
//				//发送更新界面广播
//				sendBroadcast(intent);
//				outputstream.write("close".getBytes());
//				break;
//			}
//		}
			
			String savedAs = "WDFL_File_" + System.currentTimeMillis();
		    File file = new File("./sdcard", savedAs);
			
		    byte[] buffer = new byte[4096];
		    int bytesRead;
		    
		    FileOutputStream fos = new FileOutputStream(file);
		    BufferedOutputStream bos = new BufferedOutputStream(fos);
		    
		    while(true)
		    {
			    bytesRead = inputstream.read(buffer, 0, buffer.length);
			    if(bytesRead == -1)
			    {
					Log.v(TAG, "Service更新主界面");
					intent.putExtra("Update", "update");
					//发送更新界面广播
					sendBroadcast(intent);
			    	break;
			    }			    
			    bos.write(buffer, 0, bytesRead);
			    bos.flush();

		    }
		    
		    bos.close();
		    socket.close();
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
    // shuold be redefine in the future
    private String getName() {
        return getResources().getString(R.string.myDisplayName);
    }

    // shuold be redefine in the future
    private String getDate() {
        Calendar c = Calendar.getInstance();
        String date = String.valueOf(c.get(Calendar.YEAR)) + "-"
                + String.valueOf(c.get(Calendar.MONTH)) + "-" + String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        return date;
    }

}
