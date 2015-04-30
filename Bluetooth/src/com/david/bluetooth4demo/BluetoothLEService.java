package com.david.bluetooth4demo;
/*
 * 
 * 该类主要是对蓝牙进行操作的类。到时候可以直接拿来用就行了！！
 * */
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

import android.R.integer;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BluetoothLEService extends Service {

	private final static String TAG = BluetoothLEService.class.getSimpleName();

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;
	private int mConnectionState = STATE_DISCONNECTED;

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;
	private static final int MESSAGE_DATA_0 = 0;
	private static final int MESSAGE_DATA_32 = 32;

	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
	public final static String DATA_4 = "com.example.bluetooth.le.DATA_4";
	public final static String DATA_32 = "com.example.bluetooth.le.DATA_32";
	public final static String CHECK_TRUE = "true";
	public final static String CHECK_FALSE = "false";

	public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID
			.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
	//当收到的数据长度是36的时候数据会分两次到达，count 判断是数据的头还是数据的尾
	public int count = 0;
	public int  isHead =  0; //判断数据是不是数据头
	public String buf = null;
	// Various callback methods defined by the BLE API.
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			String intentAction;
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				intentAction = ACTION_GATT_CONNECTED;
				mConnectionState = STATE_CONNECTED;
				broadcastUpdate(intentAction);
				Log.i(TAG, "Connected to GATT server.");
				Log.i(TAG, "Attempting to start service discovery:"
						+ mBluetoothGatt.discoverServices());

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				intentAction = ACTION_GATT_DISCONNECTED;
				mConnectionState = STATE_DISCONNECTED;
				Log.i(TAG, "Disconnected from GATT server.");
				broadcastUpdate(intentAction);
			}
		}
		@Override
		// New services discovered
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}
		}

//		@Override
//		// Result of a characteristic read operation
//		public void onCharacteristicRead(BluetoothGatt gatt,
//				BluetoothGattCharacteristic characteristic, int status) {
//			if (status == BluetoothGatt.GATT_SUCCESS) {
//				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//			}
//		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {

			System.out.println("onDescriptorWriteonDescriptorWrite = " + status
					+ ", descriptor =" + descriptor.getUuid().toString());
		}
		@Override
		//===================================收到数据！！==========================
		public void onCharacteristicChanged(BluetoothGatt gatt, 
				BluetoothGattCharacteristic characteristic) {
			//System.out.println("=========");
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
		}
		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			System.out.println("rssi = " + rssi);
		}
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			System.out.println("--------write success----- status:" + status);

		};
	};
	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}
	private void broadcastUpdate(final String action,
			final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(action);
		if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
			String value = characteristic.getStringValue(0);
			byte[] hha = characteristic.getValue(); // 如果可以直接获取那我的工作不就白费了！！！
			//characteristic.
			//===========check if the data is right===========
			printStr(value);
			System.out.println(value.length());
			if(checkRight(value))
			{
				if(isHead == 0)// 如果是数据头，就应该计算数据的长度如果长度是32就继续接受。如果长度是4 就check。
				{
					if(checkLength(value) == MESSAGE_DATA_0) //数据的长度是0
					{
						if(checkSum(value) == true)  //校验成功
						{
							System.out.println("333333");
								intent.putExtra(DATA_4, CHECK_TRUE);
								sendBroadcast(intent);
						}
					}
					else {
						isHead = 1;
						buf += value;
					}
				}
				else { // 如果不是数据头，那就将这次的数据与上次的数据拼接起来。
					buf += value;
					count = 0;
					if(checkLength(value) == MESSAGE_DATA_32) //数据的长度是0
					{
						if(checkSum(value) == true)  //校验成功
						{
								System.out.println("32!!!");
								intent.putExtra(DATA_32, CHECK_TRUE);
								sendBroadcast(intent);
						}
					}
				}
				if(checkSum(value) == true)
					if(checkLength(value) == MESSAGE_DATA_0)
					{
						intent.putExtra(DATA_4, CHECK_TRUE);
						sendBroadcast(intent);
					}
				
				
			} 
		}
		else {
			// For all other profiles, writes the data formatted in HEX.
			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0) {
				final StringBuilder stringBuilder = new StringBuilder(
						data.length);
				for (byte byteChar : data)
					stringBuilder.append(String.format("%02X ", byteChar));
				intent.putExtra(EXTRA_DATA, new String(data) + "\n"
						+ stringBuilder.toString());
			}
		}
	}
	public boolean initBluetoothParam() {
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Toast.makeText(this, "bluetooth初始化失败", Toast.LENGTH_SHORT)
						.show();
				return false;
			}
		}
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "不能获得bluetoothAdapter", Toast.LENGTH_SHORT)
					.show();
			return false;
		}
		return true;
	}

	public boolean connect(String address) {
		if (mBluetoothAdapter == null || address == null) {
			return false;
		}
		if (mBluetoothDeviceAddress != null
				&& address.equals(mBluetoothDeviceAddress)
				&& mBluetoothGatt != null) {
			Log.d(TAG,
					"Trying to use an existing mBluetoothGatt for connection.");
			if (mBluetoothGatt.connect()) {
				mConnectionState = STATE_CONNECTING;
				return true;
			} else {
				return false;
			}
		}
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		if (device == null) {
			Log.v("device of null", "device of null");
			return false;
		}
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		mConnectionState = STATE_CONNECTING;
		return true;
	}

	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.disconnect();
	}

	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.readCharacteristic(characteristic);
	}
	
	public void writeCharacteristic(BluetoothGattCharacteristic mWriteCaracteristic,String data){
		if(UUID_HEART_RATE_MEASUREMENT.equals(mWriteCaracteristic.getUuid())){
			byte[] values = data.getBytes();
			mWriteCaracteristic.setValue(values);
			mBluetoothGatt.writeCharacteristic(mWriteCaracteristic);
		}
	}
	public void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
		// BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
		// UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
		// descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		// mBluetoothGatt.writeDescriptor(descriptor);

		// This is specific to Heart Rate Measurement.
		if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
			BluetoothGattDescriptor descriptor = characteristic
					.getDescriptor(UUID
							.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
			descriptor
					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
		}
	}

	public class LocalBinder extends Binder {
		BluetoothLEService getService() {
			return BluetoothLEService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		close();
		return super.onUnbind(intent);
	}

	public List<BluetoothGattService> getServices() {
		if (mBluetoothGatt == null)
			return null;
		return mBluetoothGatt.getServices();
	}

	public BluetoothGattService getservice(UUID uuid) {
		BluetoothGattService service = null;
		if (mBluetoothGatt != null) {
			service = mBluetoothGatt.getService(uuid);
		}
		return service;
	}

	// public void getRssi(){
	// if(mBluetoothGatt==null) return;
	// mBluetoothGatt.;
	// }
	public boolean checkRight(String str)
	{
		byte[] temp = str.getBytes();
		int len = temp.length;
		int sum = 0;
		for(int i = 0;i < len-1;i++)
		{
			sum += (int)temp[i] & 0xff;
		}
		if(sum == 0)
		{
			return false; // 不是正常数据
		}
		return true; // 正常数据
	}
	public int  checkLength(String input)
	{
		byte[] temp = input.getBytes();
		if((((int)temp[0]&0xff) == 0x55) && (((int)temp[2]&0xff) == 0) && (temp.length == 4))
		{
			return MESSAGE_DATA_0;
		}
		return MESSAGE_DATA_32;
		
	}
	public  boolean checkSum(String data)
	{
		int sum = 0;
		byte[] input = data.getBytes();
		int len = input.length;
		for(int i = 0;i < len-1;i++)
		{
			sum += (int)input[i] & 0xff;
		}
		if((sum & 0xff) == input[input.length - 1])
		{
			return true;
		}
		return false;
	}
	public void printStr(String str)
	{
		byte[] temp  = null;
		char[] temp2 = null;
		int ci = 0;
		for(int i = 0;i< str.length();i++)
		{
			//temp2[i] = str.charAt(i);
			ci = (int)str.charAt(i) & 0xff;
			System.out.print(ci+"+");
		}
		System.out.println("|");
		int i = 0;
		try {
			temp = str.getBytes("gbk");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(str.length()+"====="+temp.length+"=====================");
		for(byte a : temp)
		{
			i = a;
			System.out.print(a+ "-");
			System.out.format( "%x ", ((int)a & 0xff) );
		}
		System.out.println(" ");
		
	}
}
