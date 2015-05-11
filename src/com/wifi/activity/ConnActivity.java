package com.wifi.activity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.quicky.wifi.R;
import com.wifi.service.WiFiServerBroadcastReceiver;

import android.net.wifi.WifiInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/*
 * author:_phy
 */
public class ConnActivity extends Activity {

	public final int fileRequestID = 55;

	public final int port = 7950;

	private WifiP2pManager wifiManager;

	private Channel wifichannel;

	private BroadcastReceiver wifiServerReceiver;

	private IntentFilter wifiServerReceiverIntentFilter;

	private String path;

	private File downloadTarget;

	private Intent serverServiceIntent;

	private boolean serverThreadActive;

	private WifiP2pDevice targetDevice;

	private WifiP2pInfo wifiInfo;

	String tag = "ConnActivity";

	WifiP2pInfo ServerWifiInfo = null;

	WifiP2pDevice ServerDevice = null;

    ServerSocket serverSocket = null;

	private Thread serverThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
        //���ϵͳ����
		wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		wifichannel = wifiManager.initialize(this, getMainLooper(), null);
		wifiServerReceiver = new WiFiServerBroadcastReceiver(wifiManager,
				wifichannel, this);
        //ע��㲥������
		wifiServerReceiverIntentFilter = new IntentFilter();
		;
		wifiServerReceiverIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		wifiServerReceiverIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		wifiServerReceiverIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		wifiServerReceiverIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		serverServiceIntent = null;
		serverThreadActive = false;
        //��̬ע��㲥������
		registerReceiver(wifiServerReceiver, wifiServerReceiverIntentFilter);
		startServer();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.conn, menu);
		return true;
	}

	//����������
	public void startServer() {

		new Thread(new Runnable() {
			public void run() {

				Log.v(tag, "Server������");

				String reces = null;
				int len = 0;
				Socket socket = null;
				OutputStream outputstream = null;
				InputStream inputstream = null;
				byte[] rece = new byte[1000];
				try {
					serverSocket = new ServerSocket(9527);
					socket = serverSocket.accept();
					inputstream = socket.getInputStream();// �õ�������
					outputstream = socket.getOutputStream();// �õ������
					// ���������׽��֣��˿�Ϊ9527
					Log.v(tag, "������");
					while (true) {
						len = inputstream.read(rece);// ���ܿͻ�����Ϣ
						if (len != 0) {
							reces = new String(rece, 0, len);
							if (reces.equals("1")) {
								outputstream.write("2".getBytes());
								Log.v(tag, "����˽��տͻ�����Ϣ�ɹ���2");
								Intent startchat = new Intent(
										ConnActivity.this, ChatActivity.class);
								startchat.putExtra("ServerWifiInfo", "localhost");
								ConnActivity.this.startActivity(startchat);
							} else if (reces.equals("3")) {
								Log.v(tag, "����˽��տͻ�����Ϣ�ɹ���3");
								inputstream.close();
								outputstream.close();
								socket.close();
							} else {
								Log.v(tag, "����˽��տͻ�����Ϣʧ�ܣ�");
							}
						}

					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						inputstream.close();
						outputstream.close();
						socket.close();// ��סһ��Ҫ�ر���Щ���룬��������׽���
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}).start();

	}

	public void stopServer(View view) {

		// stop download thread
		if (serverServiceIntent != null) {
			stopService(serverServiceIntent);

		}

	}

	// public void startClientActivity(View view) {
	//
	// stopServer(null);
	// Intent clientStartIntent = new Intent(this, ClientActivity.class);
	// startActivity(clientStartIntent);
	// }

	public void searchForPeers(View view) {

		// Discover peers, no call back method given
		// wifiManager.discoverPeers(wifichannel, null);
		wifiManager.discoverPeers(wifichannel,
				new WifiP2pManager.ActionListener() {
					@Override
					public void onSuccess() {
						TextView server_status_text = (TextView) findViewById(R.id.search_status);
						server_status_text.append("Device Finded!");

					}

					@Override
					public void onFailure(int reasonCode) {
						TextView server_status_text = (TextView) findViewById(R.id.search_status);
						server_status_text.append("No Device Finded!");
					}
				});

	}

	public void displayPeers(final WifiP2pDeviceList peers) {
		// Dialog to show errors/status
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("WiFi Direct File Transfer");

		// Get list view
		ListView peerView = (ListView) findViewById(R.id.device_list);

		// Make array list
		ArrayList<String> peersStringArrayList = new ArrayList<String>();

		// Fill array list with strings of peer names
		for (WifiP2pDevice wd : peers.getDeviceList()) {
			peersStringArrayList.add(wd.deviceName);
		}

		// Set list view as clickable
		peerView.setClickable(true);
		peerView.setBackgroundColor(Color.WHITE);

		// Make adapter to connect peer data to list view
		ArrayAdapter arrayAdapter = new ArrayAdapter(this,
				android.R.layout.simple_list_item_1,
				peersStringArrayList.toArray());

		// Show peer data in listview
		peerView.setAdapter(arrayAdapter);

		peerView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {

				// Get string from textview
				TextView tv = (TextView) view;

				WifiP2pDevice device = null;

				// Search all known peers for matching name
				for (WifiP2pDevice wd : peers.getDeviceList()) {
					if (wd.deviceName.equals(tv.getText()))
						device = wd;
				}

				if (device != null) {
					// Connect to selected peer
					connectToPeer(device);

					if (ServerWifiInfo != null) {
						startClient(ServerWifiInfo, ServerDevice);
					} else {
						new AlertDialog.Builder(ConnActivity.this)
								.setTitle("��ʾ").setMessage("����������......���Ժ����ԣ�")
								.setPositiveButton("ȷ��", null).show();
					}
				} else {
					dialog.setMessage("Failed");
					dialog.show();

				}
			}
			// TODO Auto-generated method stub
		});

	}

	public void connectToPeer(final WifiP2pDevice wifiPeer) {
		this.targetDevice = wifiPeer;

		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = wifiPeer.deviceAddress;
		wifiManager.connect(wifichannel, config,
				new WifiP2pManager.ActionListener() {
					public void onSuccess() {

					}

					public void onFailure(int reason) {

					}
				});

	}

	//�����ͻ���
	public void startClient(final WifiP2pInfo wifiInfo2, WifiP2pDevice device) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				Socket s = null;
				OutputStream outputstream = null;
				InputStream inputstream = null;
				byte[] rece = new byte[1000];
				int len = 0;
				// String races = null;
				try {
					//s = new Socket(wifiInfo2.groupOwnerAddress, 9527);
					s = new Socket("192.168.49.1", 9527);
					outputstream = s.getOutputStream();
					inputstream = s.getInputStream();
					outputstream.write("1".getBytes());// �������������Ϣ
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				while (true) {
					try {

						len = inputstream.read(rece);
						if (len != 0) {
							String races = new String(rece, 0, len);
							if (races.equals("2")) {
								outputstream.write("3".getBytes());
								
								Intent startchat = new Intent(
										ConnActivity.this, ChatActivity.class);
								startchat.putExtra("ServerWifiInfo", ServerWifiInfo.groupOwnerAddress.toString());
								ConnActivity.this.startActivity(startchat);
								
								Log.i(tag, "�ͻ��˽��շ������Ϣ�ɹ���");
//								outputstream.close();
//								inputstream.close();
//								s.close();
								break;
							} else {
								Log.i(tag, "�ͻ��˽��շ������Ϣʧ�ܣ�");
							}
						}

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// stopServer(null);
		// unregisterReceiver(wifiServerReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		stopServer(null);

		// stopService(serverServiceIntent);

		// Unregister broadcast receiver
		try {
			unregisterReceiver(wifiServerReceiver);
		} catch (IllegalArgumentException e) {
			// This will happen if the server was never running and the stop
			// button was pressed.
			// Do nothing in this case.
		}
	}

	public void setServiceStatus(String message) {
		TextView clientStatusText = (TextView) findViewById(R.id.search_status);
		clientStatusText.append(message);
	}

	public void setServerStatus(String message) {
		TextView server_status_text = (TextView) findViewById(R.id.search_status);
		server_status_text.append(message);
	}

	public void setServerWifiStatus(String msg) {
		TextView server_status_text = (TextView) findViewById(R.id.search_status);
		server_status_text.append(msg);
	}


	public void getWifiInfo(WifiP2pInfo wifiInfo2, WifiP2pDevice device) {

		ServerWifiInfo = wifiInfo2;
		ServerDevice = device;

	}

}
