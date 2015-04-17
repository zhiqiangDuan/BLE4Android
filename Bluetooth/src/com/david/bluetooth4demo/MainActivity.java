package com.david.bluetooth4demo;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
	
	public class MainActivity extends Activity implements OnGestureListener, android.view.View.OnClickListener 
	{
		public BluetoothAdapter mBluetoothAdapter;
		private DeviceListAdapter mDevListAdapter;
		private static final int REQUEST_ENABLE_BT = 1;
		private static final long SCAN_PERIOD = 10000;// 扫描周期
		private Handler mHandler;
		private TextView tv_QQ;
		private TextView tv_Number;
		private TextView tv_Taobao;
		private TextView tv_Weixin;
		private Button but = null;
		private ListView lv_bleList;
		public ClipboardManager clipboard;
		private GestureDetector gestureScanner;
		@Override
	
		protected void onCreate(Bundle savedInstanceState) 
		{
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			gestureScanner = new GestureDetector(this);
			but = (Button)findViewById(R.id.serial);
			lv_bleList = (ListView) findViewById(R.id.lv_bleList);
			tv_QQ = (TextView)findViewById(R.id.textViewQQ2) ;// 复制QQ的内容
			tv_Number = (TextView)findViewById(R.id.textViewNum2) ;// 复制电话的内容
			tv_Weixin = (TextView)findViewById(R.id.textViewWeixin2) ;// 复制微信的内容
			tv_Taobao = (TextView)findViewById(R.id.textViewTaobao2) ;// 复制淘宝的内容
			tv_Number.setOnClickListener(this);
			tv_QQ.setOnClickListener(this);
			tv_Weixin.setOnClickListener(this);
			tv_Taobao.setOnClickListener(this);
			but.setOnClickListener(new ButtonListener1());
			mDevListAdapter = new DeviceListAdapter();
			lv_bleList.setAdapter(mDevListAdapter);
			clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			mHandler = new Handler();
			bluetoothInit();
		}
		class DeviceListAdapter extends BaseAdapter {

			private List<BluetoothDevice> mBleArray;
			private ViewHolder viewHolder;

			public DeviceListAdapter() {
				mBleArray = new ArrayList<BluetoothDevice>();
			}

			public void addDevice(BluetoothDevice device) {
				if (!mBleArray.contains(device)) {
					mBleArray.add(device);
				}
			}

			@Override
			public int getCount() {
				return mBleArray.size();
			}

			@Override
			public BluetoothDevice getItem(int position) {
				return mBleArray.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
					convertView = LayoutInflater.from(MainActivity.this).inflate(
							R.layout.item_list, null);
					viewHolder = new ViewHolder();
					viewHolder.tv_devName = (TextView) convertView
							.findViewById(R.id.tv_devName);
					viewHolder.tv_devAddress = (TextView) convertView
							.findViewById(R.id.tv_devAddress);
					convertView.setTag(viewHolder);
				} else {
					convertView.getTag();
				}

				// add-Parameters
				BluetoothDevice device = mBleArray.get(position);
				String devName = device.getName();
				if (devName != null && devName.length() > 0) {
					viewHolder.tv_devName.setText(devName);
				} else {
					viewHolder.tv_devName.setText("unknow-device");
				}
				viewHolder.tv_devAddress.setText(device.getAddress());

				return convertView;
			}

		}
		class ViewHolder {
			TextView tv_devName, tv_devAddress;
		}

		private void bluetoothInit()
		{
			// TODO Auto-generated method stub
			if (!getPackageManager().hasSystemFeature(
					PackageManager.FEATURE_BLUETOOTH_LE)) {
				Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
						.show();
				finish();
			}
			final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			mBluetoothAdapter = bluetoothManager.getAdapter();

			// Is adapter null?
			if (mBluetoothAdapter == null) {
				Toast.makeText(this, "bluetooth4.0 is not supported!",
						Toast.LENGTH_SHORT).show();
				this.finish();
				return;
			}
			
			if(!mBluetoothAdapter.isEnabled())
			{
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);	
				
			}
			//==============修改到这个地方了。==================
			scanLeDevice(true);
			lv_bleList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (mDevListAdapter.getCount() > 0) {
						BluetoothDevice device = mDevListAdapter.getItem(position);
						Intent intent = new Intent(MainActivity.this,
								Version.class);
						Bundle bundle = new Bundle();
						bundle.putString("BLEDevName", device.getName());
						bundle.putString("BLEDevAddress", device.getAddress());
						intent.putExtras(bundle);
						MainActivity.this.startActivity(intent);
					}
				}
			});
		}
		private void scanLeDevice(final boolean enable) 
		{
			if (enable) {
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						mBluetoothAdapter.stopLeScan(mLeScanCallback);
					}
				}, SCAN_PERIOD);

				mBluetoothAdapter.startLeScan(mLeScanCallback);
			} else {
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
			}
		}
		private BluetoothAdapter.LeScanCallback mLeScanCallback = new LeScanCallback() 
		{

			@Override
			public void onLeScan(final BluetoothDevice device, int rssi,
					byte[] scanRecord) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mDevListAdapter.addDevice(device);
						mDevListAdapter.notifyDataSetChanged();
					}
				});
			}
		};
		
		private class ButtonListener1 implements OnClickListener, android.view.View.OnClickListener
		{
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Intent intent2 = new Intent();
		    		intent2.setClass(MainActivity.this, Version.class);
		    		startActivity(intent2);
				}

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					
				}
		    }
		@Override
		public boolean onDown(MotionEvent arg0) {
			// TODO Auto-generated method stub
			return false;
		}
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			//不管左右滑动，都是打开Activity3只是让Activity3的出现的方式不一样而已
			if (e1.getX() > e2.getX()) { //向右滑动 
				startActivity(new Intent(this, Activity3.class)); 
				overridePendingTransition(R.anim.slide_left, R.anim.close); 
			}
			else if(e1.getX() < e2.getX())// 向左滑动
			{
				startActivity(new Intent(this, Activity3.class)); 
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
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			ClipData clip;
			switch (v.getId()) {
			case R.id.textViewQQ2:
				//copy  QQ to clipboard
		         clip = ClipData.newPlainText("text label", tv_QQ.getText().toString());
		         clipboard.setPrimaryClip(clip);
		         Toast.makeText(getApplicationContext(), "QQ已经复制到粘贴板",
		         Toast.LENGTH_SHORT).show();
				break;
			case R.id.textViewTaobao2:
				String url = "http://www.taobao.com";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
				break;
			case R.id.textViewWeixin2:
				clip = ClipData.newPlainText("text label", tv_Weixin.getText().toString());
		         clipboard.setPrimaryClip(clip);
		         Toast.makeText(getApplicationContext(), "微信号已经复制到粘贴板",
		         Toast.LENGTH_SHORT).show();
				break;
			case R.id.textViewNum2:
				clip = ClipData.newPlainText("text label", tv_Number.getText().toString());
		         clipboard.setPrimaryClip(clip);
		         Toast.makeText(getApplicationContext(), "电话号已经复制到粘贴板",
		         Toast.LENGTH_SHORT).show();
				break;	
			}
			
		}

		
}
