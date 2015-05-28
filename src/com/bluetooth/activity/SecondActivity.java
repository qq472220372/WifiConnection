package com.bluetooth.activity;

import com.quicky.wifi.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;



public class SecondActivity extends Activity {
	private Button btnContacts;
	private Button btnSharePictures;
	private Button btnLocation;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.secondlogon);
		btnContacts=(Button)findViewById(R.id.btnContacts);
		btnSharePictures=(Button)findViewById(R.id.btnSharePictures);
		btnLocation=(Button)findViewById(R.id.btnLocation);
		
		btnContacts.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(SecondActivity.this, BluetoothChatActivity.class);
				startActivity(intent);			
			}
		});
//		btnSharePictures.setOnClickListener(new OnClickListener() {		
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent();
//				intent.setClass(SecondActivity.this, SetContacts.class);
//				startActivity(intent);//启动intent				
//			}
//		});
//		btnLocation.setOnClickListener(new OnClickListener() {		
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent();
//				intent.setClass(SecondActivity.this, SetContacts.class);
//				startActivity(intent);//启动intent				
//			}
//		});
//	        
	 }
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_BACK){
    		moveTaskToBack(true);
    		return true;
    		}
    	return super.onKeyDown(keyCode, event);
    	}
}

