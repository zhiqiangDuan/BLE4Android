package com.david.bluetooth4demo;


//import android.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//import com.example.xiaomi.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

	public class Activity3 extends Activity {
		private String data[] = {"电机型号   默认值  ","电压参数   默认值",  "电流参数   默认值","温度参数   默认值", "转把参数   默认值","EBS功能   开关","限速功能   开关",
				"巡航功能   开关","倒车功能   开关","弱磁功能   开关","飙车功能   开关"};
		private ListView listView = null; // 定义ListView组件
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	
	//listView = new ListView(this);
	//this.listView = new ListView(this);
	//this.listView.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,this.data));
	//setContentView(this.listView);
	setContentView(R.layout.activity3);
	listView = (ListView) findViewById(R.id.datalist);
	listView.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_expandable_list_item_1, data));
	listView.setOnItemClickListener(new OnItemClickListenerImpl());
		}
	private class OnItemClickListenerImpl implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub
				switch(position)
				{
				case 0:
					Intent intent = new Intent();
		    		intent.setClass(Activity3.this, MotorSetting.class);
		    		startActivity(intent);
					break;
				}
			
		}
		
	}
}