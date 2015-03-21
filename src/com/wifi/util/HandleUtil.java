package com.wifi.util;

import java.io.Serializable;

import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

public class HandleUtil implements Parcelable  {
	Handler hd;

	public HandleUtil(Handler handler) {
		hd = handler;
	}

	public Handler getHd() {
		return hd;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	
}
