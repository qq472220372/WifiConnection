package com.finding.main;

import com.gc.materialdesign.views.ButtonRectangle;
import com.quicky.wifi.R;
import com.quicky.wifi.R.id;
import com.quicky.wifi.R.layout;
import com.quicky.wifi.R.menu;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class FindingActivity extends Activity {

	private ButtonRectangle button1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_finding);
		final ButtonRectangle button1 = (ButtonRectangle)findViewById(R.id.finding_button);
		button1.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				//handler.sendEmptyMessage(1);
				button1.setText("防走失已开启");
			}
			
		});
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
	
}
