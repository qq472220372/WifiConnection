package com.blutooth.service;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class CallBroadCast extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
				Log.e("CallBroadCast", "111");
		    	String phone=arg1.getStringExtra("phone");
				String message=arg1.getStringExtra("message");
				Intent intent = new Intent();
				if(phone.trim().length()!=0){
			
					intent.setAction(Intent.ACTION_CALL);
					intent.setData(Uri.parse("tel:"+phone));
					Log.e("BluetoothChatActivity.java", "tel  cal");
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					arg0.startActivity(intent);
					if(message.trim().length()!=0){	
						SmsManager smsManager = SmsManager.getDefault();
						try{
							PendingIntent mPI=PendingIntent.getBroadcast(arg0, 0, new Intent(),0);
							smsManager.sendTextMessage(phone, null, message, mPI,null);
						}catch(Exception e){
							e.printStackTrace();
						}
						Toast.makeText(arg0, "发送成功!", Toast.LENGTH_SHORT).show();
						}	
					else{
						Toast.makeText(arg0, "短信不能输入为空", Toast.LENGTH_LONG).show();
						}	
					}
				else{
					Toast.makeText(arg0, "号码不能输入为空", Toast.LENGTH_LONG).show();
					}	
		    }

	

}
