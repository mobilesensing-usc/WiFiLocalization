package com.project.datacollection2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;

/**
 * This class is used to process data from an HarshSensor device.
 */
public class HarshSensor
{
	/**
	 * Value indicating start of the message
	 */
	public static final int CONST_VALUE_MESSAGE_START = 170;

	/**
	 * Value indicating end of the message
	 */
	public static final int CONST_VALUE_MESSAGE_END = 170;

	/**
	 * Unique message ID fr HarshSensor devices
	 */
	public static final int CONST_VALUE_MESSAGE_ID = 85;

	/**
	 * Position from where a message starts
	 */
	public static final int CONST_POSITION_MESSAGE_START = 0;

	/**
	 * Position where a message ends
	 */
	public static final int CONST_POSITION_MESSAGE_END = 0;

	/**
	 * Position where message ID is stored
	 */
	public static final int CONST_POSITION_MESSAGE_ID = 1;

	/**
	 * Length of the message
	 */
	public static final int CONST_SIZE_MESSAGE = 0;

	/**
	 * Name of the device
	 */
	private String deviceName;

	/**
	 * Mac Address of the device
	 */
	private String deviceAddress;

	/*
	 * Variables to handle file Used to maintain log of the data
	 */
	private FileWriter fstream;
	private BufferedWriter out;
	private boolean isVersionWritten;

	/*
	 * Variables to hold data
	 */
	private int startCode;
	private int messageCode;
	private int accX;
	private int accY;
	private int accZ;
	private float gyroX;
	private float gyroY;
	private float gyroZ;
	private int compX;
	private int compY;
	private int compZ;

	/**
	 * Constructor to HarshSensor class. Takes two arguments as device name and
	 * device address.
	 * 
	 * @param name
	 *            Name of the HarshSensor device
	 * @param address
	 *            Mac address of the HarshSensor device
	 */
	public HarshSensor(String name, String address)
	{
		HarshSensor.this.deviceName = name;
		HarshSensor.this.deviceAddress = address;
	}

	/**
	 * Parses the HarshSensor message in readable form. This function should be
	 * used before trying to get any value from the message.
	 * 
	 * @param message
	 *            Integer array storing the data read from the sensor
	 */
	public void parseMessage(String message)
	{
		/*
		String[] parsedString = message.split(" ");
		startCode = Integer.parseInt(parsedString[0]);
		messageCode = Integer.parseInt(parsedString[1]);
		accX = Integer.parseInt(parsedString[2]);
		accY = Integer.parseInt(parsedString[3]);
		accZ = Integer.parseInt(parsedString[4]);
		gyroX = Integer.parseInt(parsedString[5]);
		gyroY = Integer.parseInt(parsedString[6]);
		gyroZ = Integer.parseInt(parsedString[7]);
		compX = Integer.parseInt(parsedString[8]);
		compY = Integer.parseInt(parsedString[9]);
		compZ = Integer.parseInt(parsedString[10]);
		*/
		String[] parseString = message.split(":");
		String[] gyro=parseString[1].split(",");
		gyroX=Float.parseFloat(gyro[0]);
		gyroY=Float.parseFloat(gyro[1]);
		gyroZ=Float.parseFloat(gyro[2]);
		Log.e("GYROX", gyroX+"");
		//Log.i("GYROY", gyroY+"");
		//Log.i("GYROZ", gyroZ+"");
	}

	/**
	 * Writes the data in the class in informative manner.
	 * 
	 * @return String containing whole data gathered from the device.
	 */
	public String toString()
	{
		String printString = "MessageStart : " + Integer.toString(startCode)
				+ "\n";
		printString += "MessageCode : " + Integer.toString(messageCode) + "\n";
		printString += "Acceleration X : " + Integer.toString(accX) + "\n";
		printString += "Acceleration Y : " + Integer.toString(accY) + "\n";
		printString += "Acceleration Z : " + Integer.toString(accZ) + "\n";
		printString += "Gyroscope X : " + Float.toString(gyroX) + "\n";
		printString += "Gyroscope Y : " + Float.toString(gyroY) + "\n";
		printString += "Gyroscope Z : " + Float.toString(gyroZ) + "\n";
		printString += "Compass X : " + Integer.toString(compX) + "\n";
		printString += "Compass Y : " + Integer.toString(compY) + "\n";
		printString += "Compass Z : " + Integer.toString(compZ) + "\n";

		return printString;
	}

	/**
	 * Logs current data of the object to the sdcard. This file is stored as
	 * /sdcard/log.txt. Throws Exception if file is not opened before calling
	 * this function.
	 */
	public void writeLog() throws Exception
	{
		String logString = "";

		if (out == null)
		{
			throw new Exception("Log file not opened");
		}

		logString += Integer.toString(startCode) + ","
				+ Integer.toString(messageCode) + "," + Integer.toString(accX)
				+ "," + Integer.toString(accY) + "," + Integer.toString(accZ)
				+ "," + Float.toString(gyroX) + "," + Float.toString(gyroY)
				+ "," + Float.toString(gyroZ) + "\n";

		try
		{
			if (!isVersionWritten)
			{
				out.write("Start Code, Message Code, AccX, AccY, AccZ\n");

				isVersionWritten = true;
			}

			out.write(logString);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Opens the file to log the data. Use this function before logging the
	 * data.
	 */
	public void fileOpen()
	{
		File file = new File("/sdcard/log_harsh.txt");

		try
		{
			if (!file.createNewFile())
			{
				Log.e("FILE_WARNING",
						"File already exists. Will be overwritten");
			} else
			{
				Log.e("FILE_SUCCESS", "File successfully created");
			}

			fstream = new FileWriter("/sdcard/log_harsh.txt");
			out = new BufferedWriter(fstream);

			isVersionWritten = false;
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Closes the file. Make sure to close this file while an activity is
	 * destroyed.
	 */
	public void fileClose()
	{
		try
		{
			out.close();
			fstream.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * Returns the device name
	 * 
	 * @return Device name
	 */
	public String getDeviceName()
	{
		return deviceName;
	}

	/**
	 * Gets the mac address of the device
	 * 
	 * @return MAC address of the device
	 */
	public String getDeviceMacAddress()
	{
		return deviceAddress;
	}

	/**
	 * Gets the gyroscope reading on x-axis
	 * 
	 * @return Gyroscope reading on X-axis
	 */
	public float getGyroX()
	{
		return gyroX;
	}

	/**
	 * Gets the gyroscope reading on Y-axis
	 * 
	 * @return Gyroscope reading on Y-axis
	 */
	public float getGyroY()
	{
		return gyroY;
	}

	/**
	 * Gets the gyroscope reading on Z-axis
	 * 
	 * @return Gyroscope reading on Z-axis
	 */
	public float getGyroZ()
	{
		return gyroZ;
	}

	/**
	 * Gets the accelerometer reading on x-axis
	 * 
	 * @return Accelerometer reading on X-axis
	 */
	public int getAccX()
	{
		return accX;
	}

	/**
	 * Gets the accelerometer reading on Y-axis
	 * 
	 * @return Accelerometer reading on Y-axis
	 */
	public int getAccY()
	{
		return accY;
	}

	/**
	 * Gets the accelerometer reading on Z-axis
	 * 
	 * @return Accelerometer reading on Z-axis
	 */
	public int getAccZ()
	{
		return accZ;
	}

	/**
	 * Gets the compass reading on x-axis
	 * 
	 * @return Compass reading on X-axis
	 */
	public int getCompX()
	{
		return compX;
	}

	/**
	 * Gets the compass reading on Y-axis
	 * 
	 * @return Compass reading on Y-axis
	 */
	public int getCompY()
	{
		return compY;
	}

	/**
	 * Gets the compass reading on Z-axis
	 * 
	 * @return Compass reading on Z-axis
	 */
	public int getCompZ()
	{
		return compZ;
	}

}
