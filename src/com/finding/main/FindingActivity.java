package com.finding.main;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.widgets.Dialog;
import com.location.main.LocationDemo.MyLocationListenner;
import com.quicky.wifi.R;
import com.quicky.wifi.R.id;
import com.quicky.wifi.R.layout;
import com.quicky.wifi.R.menu;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class FindingActivity extends Activity {

	private ButtonRectangle button1;
	private LocationInfo locationInfo = new LocationInfo();
	private BootstrapEditText editText1;
	private BootstrapEditText editText2;
	private BootstrapEditText editText3;
	private BootstrapEditText editText4;
	
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	boolean isFirstLoc = true;// 是否首次定位
	boolean isCorrect = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_finding);
		editText1 = (BootstrapEditText)findViewById(R.id.bootstrapText1);
		editText2 = (BootstrapEditText)findViewById(R.id.bootstrapText2);
		editText3 = (BootstrapEditText)findViewById(R.id.bootstrapText3);
		editText4 = (BootstrapEditText)findViewById(R.id.bootstrapText4);
		final ButtonRectangle button1 = (ButtonRectangle)findViewById(R.id.finding_button);
		button1.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				String a,b,c;
				a = editText1.getText().toString();
				b = editText2.getText().toString();
				c = editText3.getText().toString();
				double d = 0;
				d = Double.parseDouble(editText4.getText().toString());
				if(a!=null&&b!=null&&c!=null&&d!=0){
				locationInfo.setName(a);
				locationInfo.setHomeAddress(b);
				locationInfo.setPhoneNumber(c);
				locationInfo.setDistance(d);
				button1.setText("防走失已开启");
				}
				else{
					Toast.makeText(getApplicationContext(), "请补全防走失信息卡",
							Toast.LENGTH_LONG).show();
				}
			}
			
		});
		
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();
		
	}

	
	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {

			Toast.makeText(getApplicationContext(), location.getLatitude()+","+
					location.getLongitude(),
					Toast.LENGTH_SHORT).show();
			
			LatLng p1LL = new LatLng(location.getLatitude(), location.getLongitude());
			LatLng p2LL = new LatLng(locationInfo.getLatitude(), locationInfo.getLongitude());
			double distance = DistanceUtil.getDistance(p1LL, p2LL);
			if(distance>locationInfo.getDistance()*1000){
				//发送短信给指定号码
				doSendSMSTo(locationInfo.getPhoneNumber(),locationInfo.getName()+"已离开安全距离，现在位置(经纬度)："+
				"("+locationInfo.getLatitude()+","+locationInfo.getLongitude()+")");
			}
			
			if (isFirstLoc) {
				isFirstLoc = false;
				Toast.makeText(getApplicationContext(), "正在获取当前位置信息",
						Toast.LENGTH_LONG).show();
			}
			else if(location.getLatitude()!=4.9E-324&&isCorrect){
				isCorrect = false;
				locationInfo.setLatitude(location.getLatitude());
				locationInfo.setLongitude(location.getLongitude());	
				Toast.makeText(getApplicationContext(), "已取得当前位置信息",
						Toast.LENGTH_LONG).show();
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.finding, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 调起系统发短信功能
	 * @param phoneNumber
	 * @param message
	 */
	public void doSendSMSTo(String phoneNumber,String message){
		if(PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)){
			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+phoneNumber));          
			intent.putExtra("sms_body", message);          
			startActivity(intent);
		}
	}

	
}
