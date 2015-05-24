package com.example.wificam;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.main.activity.TestUIActivity;
import com.quicky.wifi.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class WificamActivity extends Activity {

	private TextView show;
	private ImageButton goButton;
	private ImageButton quitButton;
	private ImageButton ledButton;

	int my_num = 0;

	private Thread myThread;
	private Thread downThread;

	private boolean startChange = true;
	private boolean ledChange = false;
	private boolean ledState = true;
	private boolean isStart = false;

	private ImageView image;

	private int picNum = 0;

	String fileName = Environment.getExternalStorageDirectory() + "/"
			+ "mypic.jpg";
	
	public final static int TIMEOUT_CONNETCT = 500;
	public final static int TIMEOUT_READ = 300;
	private final int UPDATE_UI = 1;
	private final int PHOTO = 2;

	private final String TAG = "WifiSoftAP";
	public WifiManager wifiManager;
	public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";

	public static final int WIFI_AP_STATE_DISABLING = 0;
	public static final int WIFI_AP_STATE_DISABLED = 1;
	public static final int WIFI_AP_STATE_ENABLING = 2;
	public static final int WIFI_AP_STATE_ENABLED = 3;
	public static final int WIFI_AP_STATE_FAILED = 4;
	

	public SocketAddress sourceAddr = null;
	private Socket mySocket = null;
	private InetSocketAddress mySocketAddr;
	private int picSize = 0;	
	
	private int mode = 1; // 模式选择 0：infra 1：adhoc
	private boolean debug = false; // 是否调试输出
	
	
	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) { // 处理消息
			switch (msg.what) {
			case UPDATE_UI: {
				showImage();
				// MainActivity.this.show.setText(my_num + "");// UI界面更新
				break;
			}
			case PHOTO: {
				saveImage();
				break;
			}
			default: {
				break;
			}
			}
			super.handleMessage(msg);
		}

	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) { // 返回按键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent();
			intent.setClass(WificamActivity.this, TestUIActivity.class);
			startActivity(intent);
			return true;
			}
		return super.onKeyDown(keyCode, event);
		
	}

	public static String GetSystemVersion() {
		return android.os.Build.VERSION.RELEASE;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wificam);

		// 兼容android不同版本
		String strVer = GetSystemVersion();
		strVer = strVer.substring(0, 3).trim();
		float fv = Float.valueOf(strVer);
		if (fv > 2.3) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectDiskReads().detectDiskWrites().detectNetwork()
					.penaltyLog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
					.build());
		}

		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		show = (TextView) findViewById(R.id.text);
		image = (ImageView) findViewById(R.id.imageView);

		goButton = (ImageButton) findViewById(R.id.goButton);
		ledButton = (ImageButton) findViewById(R.id.ledButton);
		quitButton = (ImageButton) findViewById(R.id.quitButton);

		if (mode == 0) { // infra 模式
			if (!isApEnabled()) {
				setWifiApEnabled(true); // 打开热点
			}
			if (myThread == null) {
				myThread = new Thread(udpRunnable);
				myThread.start();
			}
		} else { // adhoc模式
			if (myThread == null) {
				myThread = new Thread(downloadRunnable);
				myThread.start();
			}
		}

		Toast toast = Toast.makeText(WificamActivity.this, "正在启动.....",
				Toast.LENGTH_LONG);
		toast.show();

		goButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startChange = !startChange;
				if (startChange == true) {
					goButton.setImageResource(R.drawable.go1);
				} else {
					goButton.setImageResource(R.drawable.stop);
				}
				isStart = true;
			}
		});

		ledButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ledChange = true;
				ledState = !ledState;
				if (ledState == true) {
					ledButton.setImageResource(R.drawable.ledon);
				} else {
					ledButton.setImageResource(R.drawable.ledoff);
				}
			}
		});

		quitButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				dialog();
			}
		});

	}

	/**************************************
	 * 退出对话框
	 *************************************/
	public void dialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(
				WificamActivity.this);
		builder.setMessage("亲，真的要退出吗？")
				.setPositiveButton("真的", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						if (isApEnabled()) {
							setWifiApEnabled(false);
						}
						System.exit(0);
					}
				})
				.setNegativeButton("不是", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
					}
				});
		AlertDialog ad = builder.create();
		ad.show();
	}

	/**********************************************
	 * 打开设置ap
	 *************************************************/
	public boolean setWifiApEnabled(boolean enabled) {
		if (enabled) {
			wifiManager.setWifiEnabled(false);
		}
		try {
			WifiConfiguration apConfig = new WifiConfiguration();

			apConfig.allowedAuthAlgorithms.clear();
			apConfig.allowedGroupCiphers.clear();
			apConfig.allowedKeyManagement.clear();
			apConfig.allowedPairwiseCiphers.clear();
			apConfig.allowedProtocols.clear();

			apConfig.SSID = "wificamera"; // 手机热点的名字
			apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

			Method method = wifiManager.getClass().getMethod(
					"setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);

			return (Boolean) method.invoke(wifiManager, apConfig, enabled);
		} catch (Exception e) {
			Log.e(TAG, "Cannot set WiFi AP state", e);
			return false;
		}
	}

	/*************************************************
	 * 获取wifiap 状态
	 **************************************************/
	public int getWifiApState() {
		try {
			Method method = wifiManager.getClass().getMethod("getWifiApState");
			return (Integer) method.invoke(wifiManager);
		} catch (Exception e) {
			Log.e(TAG, "Cannot get WiFi AP state", e);
			return WIFI_AP_STATE_FAILED;
		}
	}

	public boolean isApEnabled() {
		int state = getWifiApState();
		return WIFI_AP_STATE_ENABLING == state
				|| WIFI_AP_STATE_ENABLED == state;
	}

	/***************************************************
	 * UDP服务
	 ********************************************************/
	public void udpServer() {
		int tmp = 0;
		if (debug) {
			Log.v("system", "enter udpServer");
		}
		try {
			DatagramSocket serSocket = new DatagramSocket(8080);
			byte data[] = new byte[1024];
			DatagramPacket pack = new DatagramPacket(data, data.length);
			try {
				do {
					serSocket.receive(pack);
					sourceAddr = pack.getSocketAddress();
					tmp = pack.getLength();
					String recvData = new String(data, 0, tmp);
					Log.v("system", "received(" + sourceAddr + "): " + recvData);
					if (downThread == null) {
						downThread = new Thread(downloadRunnable);
						downThread.start();
					}
				} while (tmp > 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**********************
	 * 显示图片
	 *********************/
	private void showImage() {
    	BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		Bitmap bm = BitmapFactory.decodeFile(fileName, options);
		int height =image.getMeasuredHeight();  
		int width =image.getMeasuredWidth();  
		Bitmap newBm = Bitmap.createScaledBitmap(bm , width,height, true);
		image.setImageBitmap(newBm);
		  
	}
	
	/////////////////////////
	/**********************
	 * 保存图片
	 *********************/
	private void saveImage() {
    	BitmapFactory.Options options = new BitmapFactory.Options();
		Bitmap bm = BitmapFactory.decodeFile(fileName, options);
		image.setImageBitmap(bm);
		ImageTools.savePhotoToSDCard(bm, Environment.getExternalStorageDirectory().getAbsolutePath(), String.valueOf(System.currentTimeMillis()));  
		File file = new File(fileName);
		file.delete(); 
	}
	
	
	/*******************
	 * 获取图片
	 *****************/
	@SuppressWarnings("resource")
	private int loadImage() {
		
		/*File file = new File(fileName);		
		if (file.exists()) {			
			 file.delete();
		}*/    
		if (mySocket == null || mySocket.isClosed()) { // 创建套接字

			mySocket = new Socket();
			if (sourceAddr != null) {
				mySocketAddr = new InetSocketAddress( // infra模式
						((InetSocketAddress) sourceAddr).getAddress()
								.getHostAddress(), 8888);
			} else {
				mySocketAddr = new InetSocketAddress("192.168.10.10", 8888); // /adhoc模式

			}

			try { // 连接
				mySocket.connect(mySocketAddr, TIMEOUT_CONNETCT);
			} catch (IOException e2) {
				if (debug) {
					Log.w("TCP", "timeout ");
				}
				try {
					mySocket.close();
					return 0;
				} catch (IOException e) {
					e.printStackTrace();
				}
				e2.printStackTrace();
			}

		}

		OutputStream outputstream = null; // 打开输出流
		try {
			outputstream = mySocket.getOutputStream();
		} catch (IOException e) {
			Log.e("TCP", "! getOutputStream");
			e.printStackTrace();
		}

		if (ledChange == true) {
			ledChange = false;
			try {
				Log.v("TCP", "LED");
				outputstream.write("LED".getBytes()); // 发送命令：开关灯
				return 0;
			} catch (IOException e) {
				Log.e("TCP", "outputstream.write");
				try {
					mySocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		} else {

			try {
				if (startChange == false) {
					if (debug) {
						Log.v("TCP", "GP");
					}
					outputstream.write("GP".getBytes()); // 发送命令：播放
				} else {
					return 0; // 暂停
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			FileOutputStream fos = null; // 文件输出流
			try {
				fos = new FileOutputStream(fileName, true);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			
			InputStream inputstream = null;
			try {
				inputstream = mySocket.getInputStream();
			} catch (IOException e) {
				Log.v("TCP", "! getInputStream");
				e.printStackTrace();
			}

			try { // 设置接收超时
				mySocket.setSoTimeout(TIMEOUT_READ);
			} catch (SocketException e1) {
				Log.v("TCP", "! setSoTimeout");
				e1.printStackTrace();
			}

			
			
			byte buffer[] = new byte[1455 * 20];
			int temp = 0;
			int picSizeTmp = 0;
			byte bufferSize[] = new byte[1455];		

			/******************
			 * *获取图片实际大小
			 ******************/

			try {
				int tmp0 = inputstream.read(bufferSize, 0, 1455);
				Log.e("MainActivity","tmp0="+tmp0);
				if (tmp0 == 1455) {
					picSize = (bufferSize[1] & 0xff) * 256
							+ (bufferSize[0] & 0xff);
					if (debug) {
						Log.v("TCP", "picSzie = " + picSize);
					}
					
					
				} else {
					Log.w("TCP", "tmp0 = " + tmp0);
					return 0;
				}
			} catch (IOException e2) {
				Log.e("TCP", "inputStream_read  time_out");
				e2.printStackTrace();
				try {
					mySocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {				
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return 0;
			}

			/*****************
			 * *获取图片固定大小
			 *****************/

			// picSize = 1455*6;

			
			while (true) { // 获取图片数据
				try {
					temp = inputstream.read(buffer);				
				} catch (IOException e) {
					try {
						if (mySocket != null) {
							Log.v("TCP", "inputstream.read time_out2 ");
							Log.v("TCP", "close mySocket");
							mySocket.close();
							fos.close();
							return 0;
						}

					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}

				try {
					fos.write(buffer, 0, temp);
				} catch (IOException e) {
					Log.e("TCP", "! write ");
					e.printStackTrace();
				}
				
				/**********************/
				
				String str = new String(buffer);
				int b = str.indexOf("key", 0);
				if(b == 0){		
					Toast toast = Toast.makeText(WificamActivity.this, "拍照",
						Toast.LENGTH_LONG);
					toast.show();
					myHandler.obtainMessage(PHOTO).sendToTarget();
				}
				
				/**********************/
				
				picSizeTmp = picSizeTmp + temp;	
				if (picSizeTmp >= picSize) { // 收到完整的图片
					break;
				}
			}
		}
		return 1;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_items, menu);
		return true;
	}

	/**************************
	 * 线程：获取图片
	 **************************/
	Runnable downloadRunnable = new Runnable() {
		public void run() {
			int sucess = loadImage();
			if (sucess == 1) {
				if (debug) {
					Log.v("TCP", "load success");
				}
				myHandler.obtainMessage(UPDATE_UI).sendToTarget(); // 发送更新ui的消息
			}
			myHandler.postDelayed(this, 1); // 间隔一段时间，再请求下一帧
			my_num++;
			Log.v("TCP", "" + my_num);
		}
	};

	/************************
	 * 线程： 接收UDP广播
	 **************************/
	Runnable udpRunnable = new Runnable() {
		public void run() {
			udpServer();
		}
	};

}
