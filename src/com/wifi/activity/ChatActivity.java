package com.wifi.activity;

import java.io.File;
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
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.andriodmvc.R;
import com.wifi.entity.ChatMsgEntity;
import com.wifi.entity.ImgEntity;
import com.wifi.service.SendMessageService;
import com.wifi.service.ServerService;
import com.wifi.service.WiFiServerBroadcastReceiver;
import com.wifi.util.ChatMsgViewAdapter;
import com.wifi.util.ImgViewAdapter;

/*
 * author:phy
 */
public class ChatActivity extends Activity {
	private int fileRequestID = 55;
    
	private static final String TAG = ChatActivity.class.getSimpleName();;

    public static ListView talkView;
    
	private String path;
	
	private File downloadTarget;
    
    private String server;

    private Button messageButton;
    
    private Button imgButton;

    public static EditText messageText;
    
    private IntentFilter wifiServerReceiverIntentFilter;
    // private ChatMsgViewAdapter myAdapter;

    public static ArrayList<ChatMsgEntity> list = new ArrayList<ChatMsgEntity>();
    
    public  ArrayList<ImgEntity> imgList = new ArrayList<ImgEntity>();
    
    public Handler handler = new Handler() {    //�½������̬�ı����
    	@Override
    	public void handleMessage(Message msg) {
           if(msg.what == 1){
        	   //updateView("�յ�һ��ͼƬ��");
        	   updateImg();
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
        //ע��㲥����������
		wifiServerReceiverIntentFilter = new IntentFilter();
		//���Action
		wifiServerReceiverIntentFilter.addAction("com.wifi.broadcast");
		//ע��㲥������
		registerReceiver(receiver,wifiServerReceiverIntentFilter);
		
        server = "";
        Intent intent = getIntent();
        server = intent.getStringExtra("ServerWifiInfo");
        talkView = (ListView) findViewById(R.id.list);
        messageButton = (Button) findViewById(R.id.MessageButton);
        imgButton = (Button) findViewById(R.id.ImgButton);
        //messageText = (EditText) findViewById(R.id.MessageText);
        messageButton.setVisibility(View.INVISIBLE);
        messageButton.setClickable(false);
        imgButton.setVisibility(View.INVISIBLE);
        
        Log.v(TAG, "ServerIP:"+server);
        
        if(!server.equals("localhost")){
        	messageButton.setVisibility(View.VISIBLE);
        	imgButton.setVisibility(View.VISIBLE);
        OnClickListener messageButtonListener = new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Log.v(TAG, "onclick >>>>>>>>");
                String name = getName();
                String date = getDate();
                String msgText = getText();
                int RId = R.layout.list_say_me_item;
                messageButton.setClickable(true);

                if(downloadTarget!=null){
                Intent intent1 = new Intent(ChatActivity.this,SendMessageService.class);
                intent1.putExtra("port", 8888);
                intent1.putExtra("ip", "192.168.49.1");
                intent1.putExtra("file", downloadTarget);
                intent1.putExtra("message", msgText);
                startService(intent1);
                updateView(msgText);
                }
                else{
                	
                }
            }

        };
        messageButton.setOnClickListener(messageButtonListener);
        
        OnClickListener imgButtonListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
		        Intent clientStartIntent = new Intent(ChatActivity.this, FileBrowser.class);
		        startActivityForResult(clientStartIntent, fileRequestID);  
				
			}
		};
        }
        else {
        	Intent intent2 = new Intent(ChatActivity.this,ServerService.class);
        	intent2.putExtra("port", 8888);
        	//intent2.putExtra("Handler", new HandleUtil(handler));
        	startService(intent2);
        }
        
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == fileRequestID) {
    		//Fetch result
    		File targetDir = (File) data.getExtras().get("file");
    		
    		if(targetDir.isDirectory())
    		{
    			if(targetDir.canWrite())
    			{
    				downloadTarget = targetDir;
	    	    	//TextView filePath = (TextView) findViewById(R.id.server_file_path);
	    	    	//filePath.setText(targetDir.getPath());
	    			//setServerFileTransferStatus("Download directory set to " + targetDir.getName());
	    			
    			}
    			else
    			{
	    			//setServerFileTransferStatus("You do not have permission to write to " + targetDir.getName());
    			}

    		}
    		else
    		{
    			//setServerFileTransferStatus("The selected file is not a directory. Please select a valid download directory.");
    		}

        }
	}
	
	//���½��溯��
    public void updateView(String msgText){
    	Log.i(TAG, "Activity����������");
        String name = getName();
        String date = getDate();
        int RId = R.layout.list_say_me_item;
        ChatMsgEntity newMessage = new ChatMsgEntity(name, date, msgText, RId);
        list.add(newMessage);
        // list.add(d0);
        talkView.setAdapter(new ChatMsgViewAdapter(ChatActivity.this, list));
        messageText.setText("");
    }

    public void updateImg(){
    	Log.i(TAG, "����ͼƬ��Ϣ");
    	String name = getName();
    	String date = getDate();
    	int RId = R.layout.list_img_layout;
    	ImgEntity imgEntity = new ImgEntity(name,date,null,RId);
    	imgList.add(imgEntity);
    	talkView.setAdapter(new ImgViewAdapter(ChatActivity.this, imgList));
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
