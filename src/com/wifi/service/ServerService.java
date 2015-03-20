package com.wifi.service;

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

/**
 * author:_phy
 */

public class ServerService extends IntentService{
	int port;
	ServerSocket serversocket;
	String rec;
	Activity chat;

	public ServerService(String name) {
		super(name);
		port = 0;
		serversocket = null;
		chat = null;
		rec = "";
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		port = ((Integer) intent.getExtras().get("port")).intValue();
		
		int len = 0;
		OutputStream outputstream = null;
		InputStream inputstream = null;
		byte[] rece = new byte[1000];
		String reces = null;	
		
		try {
			serversocket = new ServerSocket(port);
			Socket socket = serversocket.accept();
			inputstream = socket.getInputStream();
            String name = getName();
            String date = getDate();
            int RId = R.layout.list_say_me_item;
			while(true){
			len = inputstream.read(rece);// 接受客户端消息
			if (len != 0) {
				reces = new String(rece, 0, len);
				rec = reces;
                //服务器接收消息更新主界面
			}
		}
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
