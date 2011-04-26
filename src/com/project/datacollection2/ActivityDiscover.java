package com.project.datacollection2;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.View;
import android.view.ViewDebug.FlagToString;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ActivityDiscover extends ListActivity
{
	BluetoothAdapter mBluetoothAdapter;

	// All codes that relate to BlueTooth start with 1*
	private static final int CODE_REQUEST_ENABLE_BLUETOOTH = 10;

	private ProgressDialog mProgressDialog;
	private ArrayAdapter<String> mArrayAdapter;

	Button btnRefresh;

	// Register the BroadcastReceiver
	IntentFilter filterDeviceFound = new IntentFilter(
			BluetoothDevice.ACTION_FOUND);
	IntentFilter filterDiscoveryStarted = new IntentFilter(
			BluetoothAdapter.ACTION_DISCOVERY_STARTED);
	IntentFilter filterDiscoveryFinished = new IntentFilter(
			BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();

			// When discovery is started
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
			{
				// Display the progress dialog
				mProgressDialog = ProgressDialog.show(ActivityDiscover.this,
						"Discovering Bluetooth Devices",
						"Please wait while other bluetooth devices in range are being searched. "
								+ "Press back to cancel discovery", true, true,
						new OnCancelListener()
						{
							public void onCancel(DialogInterface dialog)
							{
								// Cancel the discovery. Just to be
								// safe
								mBluetoothAdapter.cancelDiscovery();
							}
						});
			}

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action.trim()))
			{
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if (device != null)
				{
			
					try{
					if (device.getName().startsWith("FireFly"))
					{
						Toast.makeText(ActivityDiscover.this,
								device.getName() + "\n" + device.getAddress(),
								Toast.LENGTH_SHORT).show();
						// Add the name and address to an array adapter to show
						// in a
						// listView

						mArrayAdapter.add(device.getName() + "\n"
								+ device.getAddress());
					}
					if (device.getName().startsWith("HXM"))
					{
						Toast.makeText(ActivityDiscover.this,
								device.getName() + "\n" + device.getAddress(),
								Toast.LENGTH_SHORT).show();
						// Add the name and address to an array adapter to show
						// in a
						// listView

						mArrayAdapter.add(device.getName() + "\n"
								+ device.getAddress());
					}
					}catch(Exception e){
						
					}
				}
			}

			// When discovery is finished
			if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
			{
				if (mProgressDialog != null)
				{
					// Display the progress dialog
					mProgressDialog.dismiss();
				}

				// Cancel the discovery. Just to be safe
				mBluetoothAdapter.cancelDiscovery();
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_discover);

		mArrayAdapter = new ArrayAdapter<String>(ActivityDiscover.this,
				android.R.layout.simple_list_item_1);

		setListAdapter(mArrayAdapter);

		btnRefresh = (Button) findViewById(R.id.buttonDiscoverRefresh);

		btnRefresh.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				ActivityDiscover.this.mArrayAdapter.clear();

				ActivityDiscover.this.mBluetoothAdapter.startDiscovery();
			}
		});

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBluetoothAdapter == null)
		{
			Toast.makeText(this, "Device does not support bluetooth",
					Toast.LENGTH_SHORT).show();
			this.finish();
		}

		if (!mBluetoothAdapter.isEnabled())
		{
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent,
					CODE_REQUEST_ENABLE_BLUETOOTH);
		}

		// Register a receiver to start listening for discovery start
		registerReceiver(mReceiver, filterDiscoveryStarted);

		// Start the discovery for the devices
		mBluetoothAdapter.startDiscovery();

		// Register a receiver if a device is found
		registerReceiver(mReceiver, filterDeviceFound);

		// Register a receiver if device discovery is finished
		registerReceiver(mReceiver, filterDiscoveryFinished);
	}

	@Override
	protected void onResume()
	{
		getListView().setEnabled(true);
		ActivityDiscover.this.btnRefresh.setEnabled(true);

		super.onResume();
	}

	@Override
	protected void onDestroy()
	{
		unregisterReceiver(mReceiver);
		mBluetoothAdapter.cancelDiscovery();

		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == CODE_REQUEST_ENABLE_BLUETOOTH)
		{
			if (resultCode == RESULT_OK)
			{

			} else if (resultCode == RESULT_CANCELED)
			{
				this.finish();
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		l.setEnabled(false);
		ActivityDiscover.this.btnRefresh.setEnabled(false);
		String deviceDetails=l.getItemAtPosition(position).toString();
		if(deviceDetails.startsWith("HXM")){
			Bundle extras=new Bundle();
			extras.putString(SharedVariables.CONST_DEVICE_INFORMATION, deviceDetails);
			setResult(RESULT_OK,(new Intent()).setAction("inline-data").putExtras(extras));
			finish();
		}
		if(deviceDetails.startsWith("FireFly")){
			Intent newIntent = new Intent(this,
					DataCollection.class);
			
			newIntent.putExtra(SharedVariables.CONST_DEVICE_INFORMATION, l
					.getItemAtPosition(position).toString());
			newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(newIntent);
		}
		
	}

	@Override
	protected void onRestoreInstanceState(Bundle state)
	{
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(state);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}
}