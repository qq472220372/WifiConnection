package com.wifi.activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.andriodmvc.R;
import com.wifi.entity.ChatMsgEntity;
import com.wifi.service.SendMessageService;
import com.wifi.service.ServerService;
import com.wifi.service.WiFiServerBroadcastReceiver;
import com.wifi.util.ChatMsgViewAdapter;

/*
 * author:phy
 */
public class ChatActivity extends Activity {
    
	private static final String TAG = ChatActivity.class.getSimpleName();;

    public static ListView talkView;
    
    private String server;

    private Button messageButton;

    public static EditText messageText;
    
    private IntentFilter wifiServerReceiverIntentFilter;
    // private ChatMsgViewAdapter myAdapter;

    public static ArrayList<ChatMsgEntity> list = new ArrayList<ChatMsgEntity>();
    
    public Handler handler = new Handler() {    //新建句柄动态改变界面
    	@Override
    	public void handleMessage(Message msg) {
           if(msg.what == 1){
        	   updateView("收到一张图片！");
           }
    	};
    };
    	
    BroadcastReceiver receiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if(intent.getStringExtra("Update").equals("update")){
    			handler.sendEmptyMessage(1);
    		}
    	}

    };
    	
	
    public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate >>>>>>");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //注册广播监听过滤器
		wifiServerReceiverIntentFilter = new IntentFilter();
		//添加Action
		wifiServerReceiverIntentFilter.addAction("com.wifi.broadcast");
		//注册广播监听器
		registerReceiver(receiver,wifiServerReceiverIntentFilter);
		
        server = "";
        Intent intent = getIntent();
        server = intent.getStringExtra("ServerWifiInfo");
        talkView = (ListView) findViewById(R.id.list);
        messageButton = (Button) findViewById(R.id.MessageButton);
        messageText = (EditText) findViewById(R.id.MessageText);
        messageButton.setClickable(false);
        
        Log.v(TAG, "ServerIP:"+server);
        
        if(!server.equals("localhost")){
        OnClickListener messageButtonListener = new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Log.v(TAG, "onclick >>>>>>>>");
                String name = getName();
                String date = getDate();
                String msgText = getText();
                int RId = R.layout.list_say_me_item;

                Intent intent1 = new Intent(ChatActivity.this,SendMessageService.class);
                intent1.putExtra("port", 8888);
                intent1.putExtra("ip", "192.168.49.1");
                intent1.putExtra("message", msgText);
                startService(intent1);
                updateView(msgText);
            }

        };
        messageButton.setOnClickListener(messageButtonListener);
        }
        else {
        	Intent intent2 = new Intent(ChatActivity.this,ServerService.class);
        	intent2.putExtra("port", 8888);
        	//intent2.putExtra("Handler", new HandleUtil(handler));
        	startService(intent2);
        }
        
    }
    
	//更新界面函数
    public void updateView(String msgText){
    	Log.i(TAG, "Activity更新主界面");
        String name = getName();
        String date = getDate();
        int RId = R.layout.list_say_me_item;
        ChatMsgEntity newMessage = new ChatMsgEntity(name, date, msgText, RId);
        list.add(newMessage);
        // list.add(d0);
        talkView.setAdapter(new ChatMsgViewAdapter(ChatActivity.this, list));
        messageText.setText("");
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

    // shuold be redefine in the future
    private String getText() {
        return messageText.getText().toString();
    }

    public void onDestroy() {
        Log.v(TAG, "onDestroy>>>>>>");
        // list = null;
        super.onDestroy();
    }
    
    public void sendMessage(){
    	
    }
}
