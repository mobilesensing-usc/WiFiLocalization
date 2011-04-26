package com.project.datacollection2;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class allows a user to connect to a HRM BlueTooth device. This class can
 * initiate a connection with the device and send messages read from the device
 * to the message handler. As the data is read on a separate thread, a message
 * handler is required to process the messages. Message to the handles contains
 * an integer specifying the number of bytes read and a string containing the
 * data read. If no data is available, -1 is returned as number of bytes read.
 * These can be accessed using constant variables MESSAGE_READ and MESSAGE_SIZE.
 */
public class BluetoothConnectionHeart implements Runnable
{
	/**
	 * Constant string as key for sending message to the activity
	 */
	public static final String MESSAGE_READ = "READ_MESSAGE";

	/**
	 * Constant string as key for sending message size to the activity
	 */
	public static final String MESSAGE_SIZE = "MESSAGE_SIZE";

	// Constant to define buffer size
	public static final int CONST_BUFFER_SIZE = 256;

	// String that holds mac address of the device to be connected
	private String MacAddress;

	// Handler to which all the messages has to be passed
	private Handler mHandler;

	// This variable represents the device to be connected
	private BluetoothDevice mBluetoothDevice;

	// The socket used to create a connection and then exchange the data
	private BluetoothSocket mConnectionSocket;

	// Input stream for the bluetooth connection
	private InputStream mInputStream;

	// Flag to check if the connection is established or not.
	private boolean isConnected;

	/**
	 * Constructor to the BluetoothConnection class. Takes three parameters
	 * required for connecting to a device and its interaction with the
	 * activity.
	 * 
	 * @param macAddress
	 *            The name of the device to connect.
	 * @param context
	 *            The context for this thread.
	 * @param handler
	 *            Handler that handles the messages passed from this thread.
	 */
	public BluetoothConnectionHeart(String macAddress, Handler handler)
	{
		// Get mac address of device to be connected
		this.MacAddress = macAddress;

		// Get handler to pass messages to
		this.mHandler = handler;

		// Get default bluetooth adapter
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		// Get remote device
		mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(this.MacAddress);

		// Set socket and input stream to null
		mConnectionSocket = null;
		mInputStream = null;

		// Set that the connection is not yet made
		this.isConnected = false;
	}

	/**
	 * Initiates connection with the remote device. Throws Exception if
	 * connection does not succeeds.
	 */
	public void createConnection() throws Exception
	{
		if (!BluetoothConnectionHeart.this.isConnected)
		{
			try
			{
				// Try to obtain a socket to connect to the device
				mConnectionSocket = mBluetoothDevice
						.createRfcommSocketToServiceRecord(SharedVariables.CONST_CONNECTION_UUID);

				try
				{
					Log.i("DEVICE_CONNECT", "Creating Connection...");

					// Try connecting to the device through current socket
					mConnectionSocket.connect();

					Log.i("DEVICE_CONNECT", "Device is now connected !!!");

					// If connection succeeds, set it as connected
					isConnected = true;

				} catch (Exception ex)
				{
					// Connection failed
					Log.e("DEVICE_CONNECT", "Connection failed : "
							+ ex.getMessage());

					// Send error message to the activity

					try
					{
						// Try closing the socket since the connection failed
						mConnectionSocket.close();

					} catch (IOException e)
					{
						e.printStackTrace();

						throw new Exception(
								"Connection failed. Unable to close socket.");
					}

					throw new Exception("Connection failed");
				}

				try
				{
					// Check if the device is connected
					if (isConnected)
					{
						// Obtain the input stream
						mInputStream = mConnectionSocket.getInputStream();
					}

				} catch (Exception ex)
				{
					// Close the connection if unable to connect to the device
					mConnectionSocket.close();

					throw new Exception("Input stream not available.");
				}

			} catch (IOException ex)
			{
				throw new Exception("Failed to create new socket.");
			} catch (Exception ex)
			{
				throw ex;
			}
		} else
		{
			throw new Exception("Device is already connected");
		}
	}

	public void run()
	{
		// buffer store the incoming stream
		byte[] buffer = new byte[CONST_BUFFER_SIZE];

		// List used to store messages if messages arrived are broken
		LinkedList<Integer> dataList = new LinkedList<Integer>();

		// Array used to store final message that is passed to the main activity
		int[] message = new int[HXM.CONST_SIZE_MESSAGE];

		int bytes; // bytes returned from read()

		// Keep listening to the input stream until connection is closed
		while (isConnected)
		{
			Log.i("THREAD_RUN", "Running thread...");
			
			try
			{
				// Read from the InputStream
				bytes = mInputStream.read(buffer, 0, CONST_BUFFER_SIZE);

				for (int i = 0; i < bytes; i++)
				{
					// Convert signed byte into int so that we may get
					// unsigned byte
					dataList.addLast((int) (buffer[i] & 0xFF));
				}

				// Check if any complete packet is stored in the list
				while (!dataList.isEmpty())
				{
					if (dataList.get(HXM.CONST_POSITION_MESSAGE_START) == HXM.CONST_VALUE_MESSAGE_START)
					{
						// Check if the packet is complete
						if (dataList.size() >= HXM.CONST_SIZE_MESSAGE)
						{
							// If yes, then check if it is a valid packet
							if ((dataList.get(HXM.CONST_POSITION_MESSAGE_ID) == HXM.CONST_VALUE_MESSAGE_ID)
									&& (dataList
											.get(HXM.CONST_POSITION_MESSAGE_END) == HXM.CONST_VALUE_MESSAGE_END))
							{
								// Store the data into message
								for (int i = 0; i < HXM.CONST_SIZE_MESSAGE; i++)
								{
									message[i] = dataList.removeFirst();
								}

								// Send the obtained message to the UI Activity
								Message msg = mHandler.obtainMessage();
								Bundle b = new Bundle();
								b.putInt(BluetoothConnection.MESSAGE_SIZE,
										bytes);
								b.putIntArray(BluetoothConnection.MESSAGE_READ,
										message);
								msg.setData(b);
								mHandler.sendMessage(msg);
								
								Log.i("THREAD_RUN", "Message Sending...");
							} else
							{
								// Since, it is not a valid packet, we may
								// discard it until we have
								// encounter next possible packet
								while (dataList
										.get(HXM.CONST_POSITION_MESSAGE_START) != HXM.CONST_VALUE_MESSAGE_START)
								{
									dataList.removeFirst();

									Log.e("TEST_DATA", "Removing...");
								}
							}
						}
						// If packet is not complete, do nothing and break to
						// read next packet
						break;
					} else
					{
						// Else remove the read byte from list
						dataList.removeFirst();

						Log.e("TEST_DATA", "Removing...");
					}
				}
			} catch (Exception e)
			{
				Log.e("DATA_ERROR", "Unable to gather data from device");
			}
		}
	}

	/**
	 * Stop listening to the device and close the socket.
	 */
	public void disconnect()
	{
		try
		{
			// Set the device to be as not connected
			// This automatically terminates the running thread as condition is
			// set to false
			isConnected = false;

			// Close the socket
			mConnectionSocket.close();
		} catch (IOException e)
		{
			Log.e("DEVICE_DISCONNECT", e.getMessage());
		}
	}

	/**
	 * Returns boolean value indicating if the device is successfully connected.
	 */
	public boolean isDeviceConnected()
	{
		// Flag if the device is connected or not
		return isConnected;
	}
}
