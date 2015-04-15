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
		private String data[] = {"����ͺ�   Ĭ��ֵ  ","��ѹ����   Ĭ��ֵ",  "��������   Ĭ��ֵ","�¶Ȳ���   Ĭ��ֵ", "ת�Ѳ���   Ĭ��ֵ","EBS����   ����","���ٹ���   ����",
				"Ѳ������   ����","��������   ����","���Ź���   ����","쭳�����   ����"};
		private ListView listView = null; // ����ListView���
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