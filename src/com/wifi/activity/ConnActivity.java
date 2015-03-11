package com.wifi.activity;


import java.io.File;

import com.example.andriodmvc.R;
import com.wifi.service.ServerService;
import com.wifi.service.WiFiServerBroadcastReceiver;

import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
		
		wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        wifichannel = wifiManager.initialize(this, getMainLooper(), null);
        wifiServerReceiver = new WiFiServerBroadcastReceiver(wifiManager, wifichannel, this);
              
        wifiServerReceiverIntentFilter = new IntentFilter();;
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    	serverServiceIntent = null; 
    	serverThreadActive = false;
    	
        registerReceiver(wifiServerReceiver, wifiServerReceiverIntentFilter);
        startServer(R.id.search_status);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.conn,menu);
		return true;
	}

public void startServer(final int statusId) {
    	
    	//If server is already listening on port or transfering data, do not attempt to start server service 
    	if(!serverThreadActive)
    	{
	    	//Create new thread, open socket, wait for connection, and transfer file 
	
	    	serverServiceIntent = new Intent(this, ServerService.class);
	    	serverServiceIntent.putExtra("saveLocation", downloadTarget);
	    	serverServiceIntent.putExtra("port", new Integer(port));
	    	serverServiceIntent.putExtra("serverResult", new ResultReceiver(null) {
	    	    @Override
	    	    protected void onReceiveResult(int resultCode, final Bundle resultData) {
	    	    	
	    	    	if(resultCode == port )
	    	    	{
		    	        if (resultData == null) {
		    	           //Server service has shut down. Download may or may not have completed properly. 
		    	        	serverThreadActive = false;	
		    	        	
		    	        			    	        	
//		    	        	final TextView server_status_text = (TextView) findViewById(R.id.server_status_text);
//		    	        	server_status_text.post(new Runnable() {
//		    	                public void run() {
//				    	        	server_status_text.setText(R.string.server_stopped);
//		    	                }
//		    	        	});	
		    	        	final TextView text = (TextView)findViewById(statusId);
		    	        	text.post(new Runnable() {
		    	                public void run() {
				    	        	text.setText("stopped!");
		    	                }
				    	        	});	
		 
		    	        			    	        			    	        	
		    	        }
		    	        else
		    	        {    	        	
//		    	        	final TextView server_file_status_text = (TextView) findViewById(R.id.server_file_transfer_status);
//
//		    	        	server_file_status_text.post(new Runnable() {
//		    	                public void run() {
//		    	                	server_file_status_text.setText((String)resultData.get("message"));
//		    	                }
//		    	        	});		    	   		    	        	
		    	        }
	    	    	}
	    	           	        
	    	    }
	    	});
	    		    		
	    	serverThreadActive = true;
	        startService(serverServiceIntent);
	
	    	//Set status to running
//	    	TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
//	    	serverServiceStatus.setText(R.string.server_running);
	    	
	    }
    	else
    	{
	    	//Set status to already running
//	    	TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
//	    	serverServiceStatus.setText("The server is already running");
    		
    	}
    }
    
    public void stopServer(View view) {
    		
    	
    	//stop download thread 
    	if(serverServiceIntent != null)
    	{
    		stopService(serverServiceIntent);
    	
    	}
       	
    }
     
    
//    public void startClientActivity(View view) {
//    	
//    	stopServer(null);
//        Intent clientStartIntent = new Intent(this, ClientActivity.class);
//        startActivity(clientStartIntent);    		
//    }   
    
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        //stopServer(null);
        //unregisterReceiver(wifiServerReceiver);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        stopServer(null);
        
        //stopService(serverServiceIntent);
        
        //Unregister broadcast receiver		
		try {
			unregisterReceiver(wifiServerReceiver);
		} catch (IllegalArgumentException e) {
			// This will happen if the server was never running and the stop
			// button was pressed.
			// Do nothing in this case.
		}      
    }
}
