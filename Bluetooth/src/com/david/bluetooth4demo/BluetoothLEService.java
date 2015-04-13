package com.david.bluetooth4demo;
/*
 * 
 * ������Ҫ�Ƕ��������в������ࡣ��ʱ�����ֱ�������þ����ˣ���
 * */
import java.util.List;
import java.util.UUID;

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

	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

	public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID
			.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

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
		public void onCharacteristicChanged(BluetoothGatt gatt, //data coming.....
				BluetoothGattCharacteristic characteristic) {
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			if (characteristic.getValue() != null) {

				System.out.println(characteristic.getStringValue(0));
			}
			System.out.println("--------onCharacteristicChanged-----");
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

		// This is special handling for the Heart Rate Measurement profile.
		// Data parsing is carried out as per profile specifications.
		if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//			int flag = characteristic.getProperties();
//			int format = -1;
//			if ((flag & 0x01) != 0) {
//				format = BluetoothGattCharacteristic.FORMAT_UINT16;
//				Log.d(TAG, "Heart rate format UINT16.");
//			} else {
//				format = BluetoothGattCharacteristic.FORMAT_UINT8;
//				Log.d(TAG, "Heart rate format UINT8.");
//			}
//			final int heartRate = characteristic.getIntValue(format, 1);
//			Log.d(TAG, String.format("Received heart rate: %d", heartRate));
//			intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
			String value = characteristic.getStringValue(0);
			intent.putExtra(EXTRA_DATA, value);
		} else {
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
		sendBroadcast(intent);
	}

	public boolean initBluetoothParam() {
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Toast.makeText(this, "bluetooth��ʼ��ʧ��", Toast.LENGTH_SHORT)
						.show();
				return false;
			}
		}
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "���ܻ��bluetoothAdapter", Toast.LENGTH_SHORT)
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

}
