package com.wifi.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.IntentService;
import android.content.Intent;

/**
 * author:_phy
 */

public class SendMessageService extends IntentService{
	
	int port;
	String ip;
	String message;
	Socket socket;

	public SendMessageService(String name) {
		super(name);
		port = 0;
		ip = "";
		message = "";

	}


	@Override
	protected void onHandleIntent(Intent intent) {
		ip = intent.getStringExtra("ip");
		port = ((Integer) intent.getExtras().get("port")).intValue();
		message = intent.getExtras().getString("message");
		
			int len = 0;
			OutputStream outputstream = null;
			InputStream inputstream = null;
			byte[] rece = new byte[1000];
			String reces = null;			
			try {
				//ServerSocket serverSocket = new ServerSocket(port);
				socket = new Socket(ip, port);
				inputstream = socket.getInputStream();
				outputstream = socket.getOutputStream();// 得到输出流
//				while(true){
//					len = inputstream.read(rece);// 接受客户端消息
//					if (len != 0) {
//						reces = new String(rece, 0, len);
//					}
//				}
				outputstream.write(message.getBytes());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				
				try {
					inputstream.close();
					outputstream.close();
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			

		
	}

}
