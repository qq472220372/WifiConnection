package com.wifi.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.wifi.activity.ChatActivity;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * author:_phy
 */

public class SendMessageService extends IntentService{
	
	private static final String TAG = ChatActivity.class.getSimpleName();;
	int port;
	String ip;
	String message;
	File file;
	Socket socket;

	public SendMessageService() {
		super("SendMessageService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		ip = intent.getStringExtra("ip");
		port = ((Integer) intent.getExtras().get("port")).intValue();
		message = intent.getExtras().getString("message");
		file = (File)intent.getExtras().get("file");
		
			int len = 0;
			OutputStream outputstream = null;
			InputStream inputstream = null;
			byte[] rece = new byte[1000];
			String reces = null;		
			Log.v(TAG, "Send Message");
			try {
				//ServerSocket serverSocket = new ServerSocket(port);
				socket = new Socket(ip, port);
				inputstream = socket.getInputStream();
				outputstream = socket.getOutputStream();// 得到输出流
				outputstream.write(message.getBytes());
				while(true){
					len = inputstream.read(rece);// 接受服务端消息
					if (len != 0) {
						reces = new String(rece, 0, len);
						if(reces.equals("close")){
							Log.v(TAG, "SendService Close");
							this.onDestory();
						}
					}
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		
	}

	public void onDestory(){
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.stopSelf();
	}
}
