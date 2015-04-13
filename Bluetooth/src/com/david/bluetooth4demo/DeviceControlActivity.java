package com.david.bluetooth4demo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class DeviceControlActivity extends Activity implements OnClickListener {

	private final static String TAG = DeviceControlActivity.class
			.getSimpleName();
	public BluetoothLEService mBluetoothLEService;
	private Bundle data;
	private String bleDevName;
	private String bLEDevAddress;
	private Handler mHandler;
	public boolean connect;
	private ProgressDialog progressDialog;
	public ArrayList<BluetoothGattCharacteristic> characteristics;
	public BluetoothGattCharacteristic mNotifyCharacteristic;
	TextView tv_devName, tv_receiveData;
	EditText et_writeContent;
	Button btn_sendMsg;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ble_communication);
		getIntentData();
		init();
	}

	@SuppressLint("HandlerLeak")
	private Handler catHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// 连接成功
			tv_receiveData.setText(tv_receiveData.getText().toString() + "\n"
					+ "设备连接成功" + "\n" + "设备地址：" + bLEDevAddress+"\n");
		}
	};

	/**
	 * @Fields conn : Service初始化bluetooth4.0
	 */
	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBluetoothLEService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBluetoothLEService = ((BluetoothLEService.LocalBinder) service)
					.getService();
			if (!mBluetoothLEService.initBluetoothParam()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			if (mBluetoothLEService == null) {
				finish();
			}
		}
	};
	
	/*
	 * 该BroadcastReceiver主要是接受BluetoothLEService发送过来的广播消息。
	 * 然后获取BluetoothLEService传送过来的数据进行显示。
	 * */

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			System.out.println("action = " + action);
			if (BluetoothLEService.ACTION_GATT_CONNECTED.equals(action)) {  //蓝牙已连接
				// mConnected = true;
				// updateConnectionState(R.string.connected);
				invalidateOptionsMenu();
				System.out.println("action = " + action);
			} else if (BluetoothLEService.ACTION_GATT_DISCONNECTED   //蓝牙断开
					.equals(action)) {
				// mConnected = false;
				// updateConnectionState(R.string.disconnected);
				// invalidateOptionsMenu();
				// clearUI();
			} else if (BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED  // service 断开
					.equals(action)) {
				// Show all the supported services and characteristics on the
				// user interface.
				// displayGattServices(mBluetoothLeService
				// .getSupportedGattServices());
			} else if (BluetoothLEService.ACTION_DATA_AVAILABLE.equals(action)) {   //收到 数据的消息
				Bundle extras = intent.getExtras();
				String data = extras.getString(BluetoothLEService.EXTRA_DATA);
				byte[] buf = data.getBytes();
				//for(int i = 0;i < buf.length;i++)
			//	{
					//System.out.println(buf[i]);
				//	tv_receiveData.setText(buf[i]+ " ");
			//	}
				//tv_receiveData.setText("111");
				//tv_receiveData.setText("\n");
				tv_receiveData.setText(tv_receiveData.getText().toString() +"data!\n"+ buf[0]+ buf[1]+ buf[2]+ buf[3]);
			}
		}
	};

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLEService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLEService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLEService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

	private void init() {
		mHandler = new Handler();
		tv_devName = (TextView) findViewById(R.id.tv_devName);
		tv_receiveData = (TextView) findViewById(R.id.tv_receiveData);
		et_writeContent = (EditText) findViewById(R.id.et_writeContent);
		btn_sendMsg = (Button) findViewById(R.id.btn_sendMsg);
		tv_devName.setText(bleDevName);
		btn_sendMsg.setOnClickListener(this);
		Intent intent = new Intent(this, BluetoothLEService.class);
		bindService(intent, conn, BIND_AUTO_CREATE);
		CatConResult result = new CatConResult();
		result.start();

	}

	@Override
	
	
	/*
	 * 该函数实现了一个两秒的定时器，用于定时检车蓝牙的连接状态
	 * 如果蓝牙断开连接。那么就会重新连接蓝牙
	 * 重连之后需要修改的数据就在这里修改。
	 * */
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		progressDialog = ProgressDialog.show(this, "Connect-bluetooth4.0",
				"Connecting devivce...");
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mBluetoothLEService != null) {
					connect = mBluetoothLEService.connect(bLEDevAddress);
					if (connect) {
						NotifyThread thread = new NotifyThread();
						thread.execute();
					}
				}
			}
		}, 2000);
	}

	class NotifyThread extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				Thread.sleep(2000);
				characteristics = getCharacteristic();
				System.out.print(characteristics);
				setNotifyReceive(characteristics);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (connect) {
				Log.e("successful", "connectting success");
				progressDialog.dismiss();
			}
			super.onPostExecute(result);
		}

	}

	public void setNotifyReceive(
			ArrayList<BluetoothGattCharacteristic> characteristics) {
		if (characteristics != null && characteristics.size() > 0) {
			for (BluetoothGattCharacteristic characteristic : characteristics) {
				int flage = characteristic.getProperties();
				if ((flage | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
					// If there is an active notification on a characteristic,
					// clear
					// it first so it doesn't update the data field on the user
					// interface.
					if (mNotifyCharacteristic != null) {
						mBluetoothLEService.setCharacteristicNotification(
								mNotifyCharacteristic, false);
						mNotifyCharacteristic = null;
					}
					mBluetoothLEService.readCharacteristic(characteristic);
				}
				if ((flage | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
					mNotifyCharacteristic = characteristic;
					mBluetoothLEService.setCharacteristicNotification(
							mNotifyCharacteristic, true);
				}
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}

	
	//获取蓝牙设备。
	public ArrayList<BluetoothGattCharacteristic> getCharacteristic() {
		ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();
		List<BluetoothGattService> services = mBluetoothLEService.getServices();
		for (int i = 0; i < services.size(); i++) {
			BluetoothGattService gattService = services.get(i);
			List<BluetoothGattCharacteristic> characteristics = gattService
					.getCharacteristics();
			for (BluetoothGattCharacteristic bluetoothGattCharacteristic : characteristics) {
				charas.add(bluetoothGattCharacteristic);
			}
		}
		return charas;
	}

	/**
	 * @ClassName: CatConResult
	 * @Description:监控
	 * @author duan
	 * @date 2014年2月19日 上午10:07:41
	 * 
	 */
	class CatConResult extends Thread {
		private boolean isRun = true;

		@Override
		public void run() {
			super.run();
			while (isRun) {
				if (connect) {
					catHandler.sendEmptyMessage(0);
					isRun = false;
				}
			}

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBluetoothLEService.disconnect();
		mBluetoothLEService.close();
		unbindService(conn);
		mBluetoothLEService = null;

	}

	/**
	 * @Title: getIntentData
	 * @Description: 获取bluetooth信息
	 * @param
	 * @return void
	 * @throws
	 */
	private void getIntentData() {
		Intent intent = this.getIntent();
		data = intent.getExtras();
		bleDevName = data.getString("BLEDevName");
		bLEDevAddress = data.getString("BLEDevAddress");
	}

	@Override
	public void onClick(View v) {
		String sendStr = this.toStringHex(et_writeContent.getText().toString()); // 发送HEX
		if (characteristics == null) {
			Toast.makeText(this, "bluetooth初始化没有成功，请重新进入此界面进行初始化！Sorry，^-^",
					Toast.LENGTH_SHORT).show();
		} else {
			for (BluetoothGattCharacteristic characteristic : characteristics) {
				//String send = this.toStringHex(sendStr);
				
				mBluetoothLEService
						.writeCharacteristic(characteristic, sendStr);
			}
		}
	}
	public String toStringHex(String s) 
	{ 
		byte[] baKeyword = new byte[s.length()/2]; 
		for(int i = 0; i < baKeyword.length; i++) 
		{ 
			try 
			{ 
				baKeyword[i] = (byte)(0xff & Integer.parseInt(s.substring(i*2, i*2+2),16)); 
			} 	
			catch(Exception e) 
			{ 
				e.printStackTrace(); 
			} 
		} 
		try 
		{
			s = new String(baKeyword, "utf-8");//UTF-16le:Not 
		} 	
		catch (Exception e1) 
		{ 
			e1.printStackTrace(); 
		}
		return s; 
	} 
}
