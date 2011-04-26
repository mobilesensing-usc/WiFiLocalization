package com.project.datacollection2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;

/**
 * This class is used to process data from an HXM device.
 */
public class HXM
{
	/**
	 * Value indicating start of the message
	 */
	public static final int CONST_VALUE_MESSAGE_START = 0X02;

	/**
	 * Value indicating end of the message
	 */
	public static final int CONST_VALUE_MESSAGE_END = 0X03;

	/**
	 * Unique message ID fr HXM devices
	 */
	public static final int CONST_VALUE_MESSAGE_ID = 0X26;

	/**
	 * Position from where a message starts
	 */
	public static final int CONST_POSITION_MESSAGE_START = 0;

	/**
	 * Position where a message ends
	 */
	public static final int CONST_POSITION_MESSAGE_END = 59;

	/**
	 * Position where message ID is stored
	 */
	public static final int CONST_POSITION_MESSAGE_ID = 1;

	/**
	 * Length of the message
	 */
	public static final int CONST_SIZE_MESSAGE = 60;

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
	private int dataLengthCode;
	private int firmwareID;
	private String firmwareVersion;
	private int hardwareID;
	private String hardwareVersion;
	private int batteryChargeIndicator;
	private int heartRate;
	private int heartBeatNumber;
	private int[] timeStamps;
	private int distance;
	private int instantaneousSpeed;
	private int strides;
	private int cadence;
	private int crc;

	/**
	 * Constructor to HXM class. Takes two arguments as device name and device
	 * address.
	 * 
	 * @param name
	 *            Name of the HXM device
	 * @param address
	 *            Mac address of the HXM device
	 */
	public HXM(String name, String address)
	{
		timeStamps = new int[15];
		HXM.this.deviceName = name;
		HXM.this.deviceAddress = address;
	}

	/**
	 * Parses the HXM message in readable form. This function should be used
	 * before trying to get any value from the message.
	 * 
	 * @param message
	 *            Integer array storing the data read from the sensor
	 */
	public void parseMessage(int[] message)
	{
		// Parse the message

		dataLengthCode = message[2];

		// Combine the firmware version and ID into a string
		firmwareID = message[3] + (message[4] << 8);
		firmwareVersion = "";
		firmwareVersion += ((char) message[5]) + ((char) message[6]);

		// Combine the firmware version and ID into a string
		hardwareID = message[7] + (message[8] << 8);
		hardwareVersion = "";
		hardwareVersion += ((char) message[9]) + ((char) message[10]);

		batteryChargeIndicator = message[11];
		heartRate = message[12];
		heartBeatNumber = message[13];

		for (int i = 0; i < 15; i++)
		{
			timeStamps[i] = message[14 + (i * 2)]
					+ (message[15 + (i * 2)] << 8);
		}

		distance = (message[50] + (message[51] << 8));
		instantaneousSpeed = message[52] + (message[53] << 8);
		strides = message[54];
		cadence = message[56] + (message[57] << 8);
		crc = message[58];
	}

	/**
	 * Get the firmware information. The information is returned as string in
	 * the format 9500.XXXX.Vyz
	 * 
	 * @return String containing firmware information
	 */
	public String getFirmwareInformation()
	{
		String firmwareInformation;
		int value = firmwareID;
		firmwareInformation = "9500." + Integer.toString(value) + ".V";
		firmwareInformation += firmwareVersion;
		return firmwareInformation;
	}

	/**
	 * Get the hardware information. The information is returned as string in
	 * the format 9800.XXXX.Vyz
	 * 
	 * @return String containing hardware information
	 */
	public String getHardwareInformation()
	{
		String hardwareInformation;
		int value = hardwareID;
		hardwareInformation = "9500." + Integer.toString(value) + ".V";
		hardwareInformation += hardwareVersion;
		return hardwareInformation;
	}

	/**
	 * Gets the charge present in the battery
	 * 
	 * @return Battery charge in percentage
	 */
	public int getBatteryCharge()
	{
		return batteryChargeIndicator;
	}

	/**
	 * Gets the Heart Rate. Valid heart rate is 30bpm to 240bpm. 0 is returned
	 * otherwise
	 * 
	 * @return Heart Rate in beats per minute
	 */
	public int getHeartRate()
	{
		return heartRate;
	}

	/**
	 * Gets the heart beat number since the device has started. heart beat
	 * number rolls over every 255 beats.
	 * 
	 * @return Heart Beat number
	 */
	public int getHeartBeatNumber()
	{
		return heartBeatNumber;
	}

	/**
	 * Returns an array containing heart beat time stamps for latest 15 heart
	 * beats. This time rolls over every 65535 milliseconds.
	 * 
	 * @return Time stamp for 15 heart beats in milliseconds
	 */
	public int[] getHeartBeatTimeStamps()
	{
		return timeStamps;
	}

	/**
	 * Distance traveled since the device has been turned on
	 * 
	 * @return Distance traveled in meters.
	 */
	public double getDistanceInMeters()
	{
		return ((double) distance / 16.0);
	}

	/**
	 * Instantaneous speed in meters per second
	 * 
	 * @return Speed in mps
	 */
	public double getSpeedInMetersPerSecond()
	{
		return ((double) instantaneousSpeed / 256.0);
	}

	/**
	 * Gets the strides
	 * 
	 * @return Strides
	 */
	public int getStrides()
	{
		return strides;
	}

	/**
	 * Gets the cadence
	 * 
	 * @return Cadence
	 */
	public int getCadence()
	{
		return cadence;
	}

	/**
	 * Writes the data in the class in informative manner.
	 * 
	 * @return String containing whole data gathered from the device.
	 */
	public String toString()
	{
		String printString = "Data Length Code : "
				+ Integer.toString(dataLengthCode) + "\n";
		printString += "Firmware Information : " + getFirmwareInformation()
				+ "\n";
		printString += "Hardware Information : " + getHardwareInformation()
				+ "\n";
		printString += "Battery Charge : "
				+ Integer.toString(batteryChargeIndicator) + "\n";
		printString += "Heart Rate : " + Integer.toString(heartRate) + "\n";
		printString += "HeartBeatNumber : " + Integer.toString(heartBeatNumber)
				+ "\n";

		for (int i = 0; i < 15; i++)
		{
			printString += "TimeStamp # " + Integer.toString(i) + " : "
					+ Integer.toString(timeStamps[i]) + "\n";
		}

		printString += "Distance (in meteres) : "
				+ Double.toString(getDistanceInMeters()) + "\n";
		printString += "Speed (in meters / second) : "
				+ Double.toString(getSpeedInMetersPerSecond()) + "\n";
		printString += "Strides : " + Integer.toString(strides) + "\n";
		printString += "Cadence : " + Integer.toString(cadence) + "\n";

		return printString;
	}

	public void checkForCRC()
	{

	}

	/**
	 * Logs current data of the object to the sdcard. This file is stored as
	 * /sdcard/log.txt. Throws Exception if file is not opened before calling
	 * this function.
	 */
	public void writeLog() throws Exception
	{
		if (out == null)
		{
			throw new Exception("Log file not opened");
		}
		String logString = Integer.toString(heartRate) + ",";
		logString += Integer.toString(heartBeatNumber) + ",";
		logString += Integer.toString(timeStamps[0]) + ",";
		logString += Double.toString(getDistanceInMeters()) + ",";
		logString += Double.toString(getSpeedInMetersPerSecond()) + "\n";

		try
		{
			if (!isVersionWritten)
			{
				out.write("Firmware Information : "
						+ HXM.this.getFirmwareInformation() + "\n");
				out.write("Hardware Information : "
						+ HXM.this.getHardwareInformation() + "\n");
				out.write("Heart Rate, Heart Beat Number, Time Stamp "
						+ "(newest heartbeat), Distance (in meters), "
						+ "Speed (in meters/second)\n");

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
		File file = new File("/sdcard/log.txt");

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

			fstream = new FileWriter("/sdcard/log.txt");
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
	 * @return Device name
	 */
	public String getDeviceName()
	{
		return deviceName;
	}
	
	/**
	 * Gets the mac address of the device
	 * @return MAC address of the device
	 */
	public String getDeviceMacAddress()
	{
		return deviceAddress;
	}
}
