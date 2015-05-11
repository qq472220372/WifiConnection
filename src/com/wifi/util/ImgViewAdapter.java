package com.wifi.util;

import java.util.ArrayList;

import com.quicky.wifi.R;
import com.wifi.entity.ImgEntity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImgViewAdapter extends BaseAdapter{
	private static final String TAG = ChatMsgViewAdapter.class.getSimpleName();
	
	private ArrayList<ImgEntity> imglist;
	
	private Context context;

	public ImgViewAdapter(Context ctx, ArrayList<ImgEntity> list){
		this.imglist = list;
		this.context = ctx;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return imglist.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return imglist.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.v(TAG, "getView>>>>>>>");
		ImgEntity imgentity = imglist.get(position);
		int itemLayout = imgentity.getLayoutID();
		
		LinearLayout layout = new LinearLayout(context);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        vi.inflate(itemLayout, layout, true);
		if(itemLayout == R.layout.list_img_layout){
        TextView tvName = (TextView) layout.findViewById(R.id.messagedetail_row_name1);
        tvName.setText(imgentity.getName());

        TextView tvDate = (TextView) layout.findViewById(R.id.messagedetail_row_date1);
        tvDate.setText(imgentity.getDate());
        
        ImageView imageView = (ImageView) layout.findViewById(R.id.img_rec);
        imageView.setImageBitmap(imgentity.getImg());
		}
		else {
	        TextView tvName = (TextView) layout.findViewById(R.id.messagedetail_row_name2);
	        tvName.setText(imgentity.getName());

	        TextView tvDate = (TextView) layout.findViewById(R.id.messagedetail_row_date2);
	        tvDate.setText(imgentity.getDate());
	        
	        ImageView imageView = (ImageView) layout.findViewById(R.id.img_rec2);
	        imageView.setImageBitmap(imgentity.getImg());
		}

        return layout;
	}

}
