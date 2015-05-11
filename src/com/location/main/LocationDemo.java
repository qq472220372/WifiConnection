package com.location.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.quicky.wifi.R;

/**
 * 此demo用来展示如何结合定位SDK实现定位，并使用MyLocationOverlay绘制定位位置 同时展示如何使用自定义图标绘制并点击时弹出泡泡
 * 
 */
public class LocationDemo extends Activity {

	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	BitmapDescriptor mCurrentMarker;
	String type = null;
	String host = null;

	MapView mMapView;
	BaiduMap mBaiduMap;

	// UI相关
	OnCheckedChangeListener radioButtonListener;
	Button requestLocButton;
	boolean isFirstLoc = true;// 是否首次定位

	public Handler myHandler = new Handler(){
		public void handleMessage(Message msg){
				//定义Maker坐标点  
				LatLng point = new LatLng(msg.getData().getDouble("Latitude"), msg.getData().getDouble("Longitude"));  
				//构建Marker图标  
				BitmapDescriptor bitmap = BitmapDescriptorFactory  
				    .fromResource(R.drawable.icon_marka);  
				//构建MarkerOption，用于在地图上添加Marker  
				OverlayOptions option1 = new MarkerOptions()  
				    .position(point)  
				    .icon(bitmap);  
				//在地图上添加Marker，并显示  
				mBaiduMap.addOverlay(option1);			
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getIntent().getExtras();
		type = bundle.getString("type");
		host = bundle.getString("host");
		setContentView(R.layout.activity_location);
		requestLocButton = (Button) findViewById(R.id.button1);
		mCurrentMode = LocationMode.NORMAL;
		requestLocButton.setText("普通");
		OnClickListener btnClickListener = new OnClickListener() {
			public void onClick(View v) {
				switch (mCurrentMode) {
				case NORMAL:
					requestLocButton.setText("跟随");
					mCurrentMode = LocationMode.FOLLOWING;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case COMPASS:
					requestLocButton.setText("普通");
					mCurrentMode = LocationMode.NORMAL;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case FOLLOWING:
					requestLocButton.setText("罗盘");
					mCurrentMode = LocationMode.COMPASS;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				}
			}
		};
		requestLocButton.setOnClickListener(btnClickListener);

		RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup);
		radioButtonListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.defaulticon) {
					// 传入null则，恢复默认图标
					mCurrentMarker = null;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, null));
				}
				if (checkedId == R.id.customicon) {
					// 修改为自定义marker
					mCurrentMarker = BitmapDescriptorFactory
							.fromResource(R.drawable.icon_geo);
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
				}
			}
		};
		group.setOnCheckedChangeListener(radioButtonListener);

		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();
		
		if(type.equals("server")){

			Toast.makeText(getApplicationContext(), "接收者就绪",
					Toast.LENGTH_SHORT).show();
			new Thread(new Runnable(){

				@Override
				public void run() {
					try {
						ServerSocket serverSocket = new ServerSocket(9999);
					while(true){
						Socket socket = serverSocket.accept();
					    new Thread(new Task2(socket)).start();
		          		} 
					}catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}

			}).start();
		
		}
		else if(type.equals("client")){
			
			Toast.makeText(getApplicationContext(), "发送者就绪",
					Toast.LENGTH_SHORT).show();

		}
		else{
			Toast.makeText(getApplicationContext(), "定位服务启动失败",
					Toast.LENGTH_SHORT).show();
		}
	
	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
			Toast.makeText(getApplicationContext(), location.getLatitude()+","+
					location.getLongitude(),
					Toast.LENGTH_SHORT).show();
			//开启一个发送位置信息的线程
			if(type.equals("client"))
			new Thread(new Task1(location.getLatitude(),location.getLongitude(),host)).start();
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
	}

//发送者任务
class Task1 implements Runnable {

	double Latitude;
	double Longitude;
	String host;
	
	public Task1(double a,double b,String host){
		Latitude = a;
		Longitude = b;
		this.host = host;
	}
	@Override
	public void run() {
		if(host!=null){
        Socket socket;
		try {
			socket = new Socket(host, 9999);
	
        // 向服务端程序发送数据
        OutputStream ops  = socket.getOutputStream();        
        OutputStreamWriter opsw = new OutputStreamWriter(ops);
        BufferedWriter bw = new BufferedWriter(opsw);
        
        bw.write(Latitude+","+Longitude);
        bw.flush();
		socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
	}
	
}

//接收者任务
class Task2 implements Runnable {
    Socket socket;
    public Task2(Socket socket){
    	this.socket = socket;
    }
	@Override
	public void run() {
		
        //接收数据
        InputStream ips;
		try {
			ips = socket.getInputStream();
        InputStreamReader ipsr = new InputStreamReader(ips);
        BufferedReader br = new BufferedReader(ipsr);
        String s = "";        
        while((s = br.readLine()) != null)   {  
         String[] tude = s.split(",");
		Bundle data =new Bundle();
		data.putDouble("Latitude", Double.parseDouble(tude[0]));
		data.putDouble("Longitude", Double.parseDouble(tude[1]));
		Message msg = new Message();
		msg.setData(data);
		myHandler.sendMessage(msg);
		
        }
		socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

}
