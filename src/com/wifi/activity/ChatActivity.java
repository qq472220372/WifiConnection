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
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Toast;

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
	
	private File fileToSend;
    
    private String server;
    
    private String imgPath;

    private Button messageButton;
    
    private Button imgButton;

    public static EditText messageText;
    
    private IntentFilter wifiServerReceiverIntentFilter;
    // private ChatMsgViewAdapter myAdapter;

    public static ArrayList<ChatMsgEntity> list = new ArrayList<ChatMsgEntity>();
    
    public  ArrayList<ImgEntity> imgList = new ArrayList<ImgEntity>();
    
    public Handler handler = new Handler() {    //新建句柄动态改变界面
    	@Override
    	public void handleMessage(Message msg) {
           if(msg.what == 1 && BitmapFactory.decodeFile(imgPath)!=null){
        	   //updateView("收到一张图片！");
        	   updateImg(BitmapFactory.decodeFile(imgPath),R.layout.list_img_layout_income,"安卓设备");
           }
           else {
        	   Log.i(TAG, "img is null");
        	   try {
        		   Thread.sleep(5000);
                	   updateImg(BitmapFactory.decodeFile(imgPath),R.layout.list_img_layout_income,"安卓设备");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           }
    	};
    };
    	
    BroadcastReceiver receiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if(intent.getStringExtra("Update").equals("update")){
    			imgPath = intent.getStringExtra("imgPath");
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
                //String msgText = getText();
                int RId = R.layout.list_say_me_item;
                messageButton.setClickable(true);

                if(fileToSend!=null){
                Intent intent1 = new Intent(ChatActivity.this,SendMessageService.class);
                intent1.putExtra("port", 8888);
                intent1.putExtra("ip", "192.168.49.1");
                intent1.putExtra("file", fileToSend);
                intent1.putExtra("suffix", fileToSend.getName().split("\\.")[1]);
                Log.i(TAG, fileToSend.getName() + " selected for file transfer");
                //intent1.putExtra("message", msgText);
                startService(intent1);
                //updateView(msgText);
                updateImg(BitmapFactory.decodeFile(fileToSend.getPath()),R.layout.list_img_layout,"我");
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
		imgButton.setOnClickListener(imgButtonListener);
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
        		
        		if(targetDir.isFile())
        		{
        			if(targetDir.canRead())
        			{
        				if(targetDir.getName().split("\\.")[1].equals("jpg")){
        				fileToSend = targetDir;
        				//filePathProvided = true;
        				
        				//setTargetFileStatus(targetDir.getName() + " selected for file transfer");
        				Log.i(TAG, targetDir.getName() + " selected for file transfer");
        				}
        				else{
        					new AlertDialog.Builder(ChatActivity.this)
							.setTitle("提示").setMessage("请选择jpg格式的图片")
							.setPositiveButton("确定", null).show();
        				}
        			}
        			else
        			{
        				//filePathProvided = false;
        				//setTargetFileStatus("You do not have permission to read the file " + targetDir.getName());
        				Log.i(TAG, "You do not have permission to read the file " + targetDir.getName());
        			}

        		}
        		else
        		{
    				//filePathProvided = false;
        			//setTargetFileStatus("You may not transfer a directory, please select a single file");
        			Log.i(TAG, "You may not transfer a directory, please select a single file");
        		}

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

    public void updateImg(Bitmap image,int id,String name1){
    	Log.i(TAG, "更新图片信息");
    	String name = name1;
    	String date = getDate();
    	//Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.download);
    	Bitmap img = image;
    	int RId = id;
    	ImgEntity imgEntity = new ImgEntity(name,date,img,RId);
    	imgList.add(imgEntity);
    	talkView.setAdapter(new ImgViewAdapter(ChatActivity.this, imgList));
    	//messageText.setText("");
    	
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
