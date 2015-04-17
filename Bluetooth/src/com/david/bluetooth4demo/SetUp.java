package com.david.bluetooth4demo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SetUp extends Activity implements OnClickListener, OnGestureListener {
//file operation
	private final static String TAG = BluetoothLEService.class.getSimpleName();
	private Context context;
	private final String filename = "/ruichi/argu.log";
	private boolean sdCard = false;   //判断是否检测到SD卡
	// UIParamter
	ToggleButton tb_on_off;
	Button btn_searchDev, btn_aboutUs;
	ListView lv_bleList;

	// bluetoothAdapter蓝牙适配器
	private BluetoothAdapter mBluetoothAdapter;
	private DeviceListAdapter mDevListAdapter;
	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 10000;// 扫描周期
	private Handler mHandler;
//=========手势检测========
	GestureDetector gestureScanner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
		mHandler = new Handler();  //  定时检测蓝牙是佛连接。
		/*
		 * 开机需要做四件事
		 * 1  判断此设备是否支持4.0设备
		 * 2 初始化BluetoothAdapter
		 * 3 判断 蓝牙是否打开，如果没打开，打开蓝牙
		 * */
		// 判断此设备是否支持蓝牙4.0设备
		bluetoothInit();
		initViews();
		GestureDetector gestureScanner = new GestureDetector(this);
	}

	private void bluetoothInit() {
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
		if(mBluetoothAdapter.isEnabled())
		{
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);	
			
		}	
		//==============修改到这个地方了。==================
		scanLeDevice(true);

	}

	/**
	 * @Title: initParamter
	 * @Description: 初始化参数
	 * @param
	 * @return void
	 * @throws
	 */
	private void initViews() {
		tb_on_off = (ToggleButton) findViewById(R.id.tb_on_off);
		btn_searchDev = (Button) findViewById(R.id.btn_searchDev);
		btn_aboutUs = (Button) findViewById(R.id.btn_aboutUs);
		lv_bleList = (ListView) findViewById(R.id.lv_bleList);

		btn_aboutUs.setOnClickListener(this);
		btn_searchDev.setOnClickListener(this);
		mDevListAdapter = new DeviceListAdapter();
		lv_bleList.setAdapter(mDevListAdapter);

		// 根据蓝牙是否开启设置toggleButton状态
		if (mBluetoothAdapter.isEnabled()) {
			tb_on_off.setChecked(true);
		} else {
			tb_on_off.setChecked(false);
		}

		tb_on_off.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					Intent enableBtIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				} else {
					mBluetoothAdapter.disable();
				}
			}
		});
		lv_bleList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mDevListAdapter.getCount() > 0) {
					BluetoothDevice device = mDevListAdapter.getItem(position);
					Intent intent = new Intent(SetUp.this,
							DeviceControlActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("BLEDevName", device.getName());
					bundle.putString("BLEDevAddress", device.getAddress());
					intent.putExtras(bundle);
					SetUp.this.startActivity(intent);
				}
			}
		});
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_searchDev:
			scanLeDevice(true);
			break;
		case R.id.btn_aboutUs:
			break;
		}
	}

	/**
	 * @Title: scanLeDevice
	 * @Description: 搜索设备
	 * @param @param enable
	 * @return void
	 * @throws
	 */
	private void scanLeDevice(final boolean enable) {
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

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new LeScanCallback() {

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

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
	}

	/**
	 * @ClassName: DeviceListAdapter
	 * @Description: 设备列表适配器
	 * @author duan
	 * @date 2015年4月10日16:22:37
	 * 
	 */
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
				convertView = LayoutInflater.from(SetUp.this).inflate(
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

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e1.getX() != e2.getX()) {
			
			//===========打开新的activity==========
			//startActivity(new Intent(this, Activity3.class)); 
			//overridePendingTransition(R.anim.hold, R.anim.close); //主activity消失的动作，新activity出现的动作
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
