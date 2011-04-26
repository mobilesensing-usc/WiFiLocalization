package com.project.datacollection2;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class HeartRateMonitorService extends Service
{
	public static final String CONST_MESSAGE_STRING = "data";
	
	private HXM myHXMDevice;

	private BluetoothConnectionHeart myBluetoothConnection;
	
	private Handler activityHandler;
		
	/**
	 * Class for clients to access.
	 */
	public class LocalBinder extends Binder
	{
		HeartRateMonitorService getService()
		{
			return HeartRateMonitorService.this;
		}
	}
	
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

	private final Handler serviceHandler = new Handler()
	{
		public void handleMessage(Message message)
		{
			Log.i("WRITE_SUCCESS87988980", "Trying to write : " + message.getData().getIntArray(BluetoothConnection.MESSAGE_READ).toString() );
			
			try
			{
				
				
				int messageSize = message.getData().getInt(
						BluetoothConnection.MESSAGE_SIZE);

				myHXMDevice.parseMessage(message.getData().getIntArray(
						BluetoothConnection.MESSAGE_READ));

				if (messageSize != -1)
				{
					// Send the obtained message to the UI Activity
					Message messageActivity = activityHandler.obtainMessage();
					Bundle b = new Bundle();
					b.putString(HeartRateMonitorService.CONST_MESSAGE_STRING,
							myHXMDevice.toString());
					messageActivity.setData(b);
					activityHandler.sendMessage(messageActivity);
					
					Log.i("WRITE_SUCCESS", "Writing to the file...");
					
					// Send information to activity
					//myHXMDevice.writeLog();
				}
			} catch (Exception e)
			{
				Log.e("WRITE_ERROR76767887", "Er" +
						"" +
						"ror : " +e.getMessage());
			}
		}
	};
	
	@Override
	public void onCreate()
	{	
	
	}
	
	public void initialize(String deviceDetails, Handler handler)
	{
		String[] splitString = deviceDetails.split("\n");
		
		myHXMDevice = new HXM(splitString[0], splitString[1]);
		
		// Create an object to establish Bluetooth connection
		myBluetoothConnection = new BluetoothConnectionHeart(myHXMDevice.getDeviceMacAddress(), serviceHandler);
		
		// Get the device name and address
		myHXMDevice.fileOpen();
		
		activityHandler = handler;
	}
	
	@Override
	public void onDestroy()
	{
		myBluetoothConnection.disconnect();
		myHXMDevice.fileClose();
		
		Log.i("LocalService", "Service closed");
		
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return mBinder;
	}
	
	public void connectBluetooth()
	{
		try
		{
			myBluetoothConnection.createConnection();
			
		} catch (Exception e)
		{
			Log.i("BLUETOOTH_CONNECTION", e.getMessage());
		}
	}
	
	public boolean isConnected()
	{
		return myBluetoothConnection.isDeviceConnected();
	}
	
	public void runService()
	{
		new Thread(myBluetoothConnection).start();		
	}
}
