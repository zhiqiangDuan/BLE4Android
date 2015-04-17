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
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

	public class Activity3 extends Activity implements OnGestureListener {
		private String data[] = {"电机型号   默认值  ","电压参数   默认值",  "电流参数   默认值","温度参数   默认值", "转把参数   默认值","EBS功能   开关","限速功能   开关",
				"巡航功能   开关","倒车功能   开关","弱磁功能   开关","飙车功能   开关"};
		private ListView listView = null; // 定义ListView组件
		
		private GestureDetector gestureScanner;
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
	
	gestureScanner = new GestureDetector(this);
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
	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e1.getX() > e2.getX()) { //向右滑动 
			//startActivity(new Intent(this, Activity3.class));
			Activity3.this.finish(); // 结束当前Activity
			overridePendingTransition(R.anim.slide_left, R.anim.close); 
		}
		else if(e1.getX() < e2.getX())// 向左滑动
		{
			//startActivity(new Intent(this, Activity3.class));
			Activity3.this.finish();
			overridePendingTransition(R.anim.slide_right, R.anim.close);
		}
		else
		{
			return false;
		}
		return true;
	}
	public boolean onTouchEvent(MotionEvent event) {
		return gestureScanner.onTouchEvent(event);
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}