package com.main.activity;



import com.bluetooth.activity.SecondActivity;
import com.example.andriodmvc.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class MainLogon extends Activity {
	private CheckBox savepassword;
	private Button btnLogon;
	private EditText editName;
	private EditText editPass;
	SharedPreferences mShared = null;
	private boolean Checkd;
	public final static String SHARED_MAIN="main";
	public final static String KEY_NAME="name";
	public final static String KEY_PASS="pass";
	public final static String DATA_URL="/data/data";
	public final static String SHARED_MAIN_XML="main.xml";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logon);
        btnLogon = (Button) findViewById(R.id.btnLogon);
		editName = (EditText) findViewById(R.id.editName);
		editPass = (EditText) findViewById(R.id.editPass);
		savepassword = (CheckBox) findViewById(R.id.savepassword);
		btnLogon.setOnClickListener(listener);
		mShared=getSharedPreferences(SHARED_MAIN, Context.MODE_PRIVATE);
		Checkd=mShared.getBoolean("isCheckd",false);
		savepassword.setChecked(Checkd);
		String name=mShared.getString(KEY_NAME, "");
		String pass=mShared.getString(KEY_PASS, "");
		editName.setText(name);
		editPass.setText(pass);
		
		
		savepassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {		
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					String pass = editPass.getText().toString();
					Editor editor = mShared.edit();
					editor.putString(KEY_PASS, pass);
					editor.putBoolean("isCheckd", true);
					editor.commit();//把信息保存在文件中
				}	
				if(!isChecked){
					Editor editor = mShared.edit();
					editor.putString(KEY_PASS, "");
					editor.putBoolean("isCheckd", false);
					editor.commit();//把信息保存在文件中
				}
			}
		});
       
    }

	private OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String name = editName.getText().toString();
			String pass = editPass.getText().toString();
			Editor editor = mShared.edit();
			editor.putString(KEY_NAME,name);
			editor.commit();//把信息保存在文件中
			if((name.trim().length()!=0) && (pass.trim().length()!=0)){
				Intent intent = new Intent();
				intent.setClass(MainLogon.this, SecondActivity.class);
				startActivity(intent);//启动intent
			}
			else{
				Toast.makeText(MainLogon.this, "用户名和密码不能输入为空！", Toast.LENGTH_SHORT).show();
			}
				
		}
	};


   

}
