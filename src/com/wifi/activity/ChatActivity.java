package com.wifi.activity;

import java.util.ArrayList;
import java.util.Calendar;

import com.example.andriodmvc.R;
import com.wifi.entity.ChatMsgEntity;
import com.wifi.util.ChatMsgViewAdapter;


import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ChatActivity extends Activity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
//    }
//
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
    
	private static final String TAG = ChatActivity.class.getSimpleName();;

    private ListView talkView;

    private Button messageButton;

    private EditText messageText;

    // private ChatMsgViewAdapter myAdapter;

    private ArrayList<ChatMsgEntity> list = new ArrayList<ChatMsgEntity>();

    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate >>>>>>");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        talkView = (ListView) findViewById(R.id.list);
        messageButton = (Button) findViewById(R.id.MessageButton);
        messageText = (EditText) findViewById(R.id.MessageText);
        OnClickListener messageButtonListener = new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Log.v(TAG, "onclick >>>>>>>>");
                String name = getName();
                String date = getDate();
                String msgText = getText();
                int RId = R.layout.list_say_he_item;

                ChatMsgEntity newMessage = new ChatMsgEntity(name, date, msgText, RId);
                list.add(newMessage);
                // list.add(d0);
                talkView.setAdapter(new ChatMsgViewAdapter(ChatActivity.this, list));
                messageText.setText("");
                // myAdapter.notifyDataSetChanged();
            }

        };
        messageButton.setOnClickListener(messageButtonListener);
    }

    // shuold be redefine in the future
    private String getName() {
        return getResources().getString(R.string.myDisplayName);
    }

    // shuold be redefine in the future
    private String getDate() {
        Calendar c = Calendar.getInstance();
        String date = String.valueOf(c.get(Calendar.YEAR)) + "-"
                + String.valueOf(c.get(Calendar.MONTH)) + "-" + c.get(c.get(Calendar.DAY_OF_MONTH));
        return date;
    }

    // shuold be redefine in the future
    private String getText() {
        return messageText.getText().toString();
    }

    public void onDestroy() {
        Log.v(TAG, "onDestroy>>>>>>");
        // list = null;
        super.onDestroy();
    }
}
