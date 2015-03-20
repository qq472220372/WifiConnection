package com.wifi.activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

public class ChatActivity extends Activity {
    
	private static final String TAG = ChatActivity.class.getSimpleName();;

    public static ListView talkView;
    
    private String server;

    private Button messageButton;

    public static EditText messageText;

    // private ChatMsgViewAdapter myAdapter;

    public static ArrayList<ChatMsgEntity> list = new ArrayList<ChatMsgEntity>();
    
	
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate >>>>>>");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        server = "";
        Intent intent = getIntent();
        server = intent.getStringExtra("ServerWifiInfo");
        talkView = (ListView) findViewById(R.id.list);
        messageButton = (Button) findViewById(R.id.MessageButton);
        messageText = (EditText) findViewById(R.id.MessageText);
        messageButton.setClickable(false);
        
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
                intent1.putExtra("ip", server);
                intent1.putExtra("message", msgText);
                startService(intent1);
                ChatMsgEntity newMessage = new ChatMsgEntity(name, date, msgText, RId);
                list.add(newMessage);
                // list.add(d0);
                talkView.setAdapter(new ChatMsgViewAdapter(ChatActivity.this, list));
                messageText.setText("");
                // myAdapter.notifyDataSetChanged();
            }

        };
        messageButton.setOnClickListener(messageButtonListener);
        }
        else {
        	Intent intent2 = new Intent(ChatActivity.this,ServerService.class);
        	intent2.putExtra("port", 8888);
        	startService(intent2);
        }
        
    }
    
    public void updateView(String name,String date,String msgText,int RId){
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
