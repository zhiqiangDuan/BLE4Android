package com.david.bluetooth4demo;

import android.app.Activity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.david.bluetooth4demo.Version.CatConResult;
import com.david.bluetooth4demo.Version.NotifyThread;

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


public class Version extends Activity implements OnClickListener {


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
	private TextView tv_receiveData;
	private Button btn_sendMsg;
	//===================
	boolean isRecved = false;
	boolean shakeHand = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	setContentView(R.layout.version);
	getIntentData();
	init();
		}
	@SuppressLint("HandlerLeak")
	private Handler catHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// ���ӳɹ�
			tv_receiveData.setText(tv_receiveData.getText().toString() + "\n"
					+ "�豸���ӳɹ�" + "\n" + "�豸��ַ��" + bLEDevAddress+"\n");
		}
	};

	/**
	 * @Fields conn : Service��ʼ��bluetooth4.0
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
	 * ��BroadcastReceiver��Ҫ�ǽ���BluetoothLEService���͹����Ĺ㲥��Ϣ��
	 * Ȼ���ȡBluetoothLEService���͹��������ݽ�����ʾ��
	 * */
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			System.out.println("action = " + action);
			if (BluetoothLEService.ACTION_GATT_CONNECTED.equals(action)) {  //����������
				// mConnected = true;
				// updateConnectionState(R.string.connected);
				invalidateOptionsMenu();
				System.out.println("action = " + action);
			} else if (BluetoothLEService.ACTION_GATT_DISCONNECTED   //�����Ͽ�
					.equals(action)) {
				// mConnected = false;
				// updateConnectionState(R.string.disconnected);
				// invalidateOptionsMenu();
				// clearUI();
			} else if (BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED  // service �Ͽ�
					.equals(action)) {
				// Show all the supported services and characteristics on the
				// user interface.
				// displayGattServices(mBluetoothLeService
				// .getSupportedGattServices());
			} else if (BluetoothLEService.ACTION_DATA_AVAILABLE.equals(action)) {   //�յ� ���ݵ���Ϣ
				if(shakeHand == true)
				{
					btn_sendMsg.setClickable(true);
					shakeHand = false;
					Bundle extras = intent.getExtras();
					String data = extras.getString(BluetoothLEService.EXTRA_DATA);
					byte[] buf = data.getBytes();
					tv_receiveData.setText(tv_receiveData.getText().toString()+ buf[0]+ buf[1]+ buf[2]+ buf[3]);
				}
				
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
		tv_receiveData = (TextView) findViewById(R.id.backView);
		btn_sendMsg = (Button) findViewById(R.id.shake);
		btn_sendMsg.setOnClickListener(this);
		/*
		 * ����Ĳ����ص㡣ǰ��Ķ�������UI��û��ʲô�ص㡣�ص��Ǻ���ķ���
		 */
		Intent intent = new Intent(this, BluetoothLEService.class);
		bindService(intent, conn, BIND_AUTO_CREATE);
		CatConResult result = new CatConResult();
		result.start();

	}

	@Override
	
	
	/*
	 * �ú���ʵ����һ������Ķ�ʱ�������ڶ�ʱ�������������״̬
	 * ��������Ͽ����ӡ���ô�ͻ�������������
	 * ����֮����Ҫ�޸ĵ����ݾ��������޸ġ�
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
					System.out.println("resume!!"+connect);
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

	
	//��ȡ�����豸��
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
	 * @Description:���
	 * @author duan
	 * @date 2014��2��19�� ����10:07:41
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
	 * @Description: ��ȡbluetooth��Ϣ
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
	public void onClick(View v) {  //������Ͱ�ť����������
		shakeHand = true;
		//String sendStr = this.toStringHex(et_writeContent.getText().toString()); // ����HEX
		String sendStr = this.toStringHex("55020057"); // ���������ź�
		if (characteristics == null) {
			Toast.makeText(this, "bluetooth��ʼ��û�гɹ��������½���˽�����г�ʼ����Sorry��^-^",
					Toast.LENGTH_SHORT).show();
		} else {
			for (BluetoothGattCharacteristic characteristic : characteristics) {
				//String send = this.toStringHex(sendStr);
				
				mBluetoothLEService
						.writeCharacteristic(characteristic, sendStr);
			}
		}
		btn_sendMsg.setClickable(false);
		
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