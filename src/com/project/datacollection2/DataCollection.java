package com.project.datacollection2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;


public class DataCollection extends Activity {
	private static final int windowSize=2048;
	private static int time=11000; //milliseconds
	private Complex[] accTotal=new Complex[2048];
	private Complex[] accTemp=new Complex[2048];
	private int accCount=0;
	private FFTCalculation fftCalculation=null;
	public static final String PREFS_NAME = "LocalizationPrefsFile";
	
    private TextView timestamp1,txtfirefly,txtheart;
    double lat,lng,speed,x,y,z,ox,oy,oz,ax_firefly,ay_firefly,az_firefly,gx_firefly,gy_firefly,gz_firefly,mx_firefly,my_firefly,mz_firefly;
	private SensorManager sensorMan,sensorOrientation;
	private Handler mHandler = new Handler();
	private Spinner activityType;
	private SensorService mHRMService;
	private HeartRateMonitorService mHeartService;
	private boolean isBound;
	public String fileName;
	private ServiceConnection mConnection=null;
	private ServiceConnection mHeartConnection=null;
	private int fftStart=0;
	private int fftEnd=0;
	public static final int START_ID = Menu.FIRST;
	public static final int STOP_ID = Menu.FIRST+1;
	public static final int EXIT_ID = Menu.FIRST+2;
	private TextView txtCalibration;
	public WifiManager wifiManager;
	File f;	 
	FileWriter fstream;
	BufferedWriter out;
	int fftcount=0, size=2048; 
	File f_firefly;	 
	FileWriter fstream_firefly;
	BufferedWriter out_firefly;

	File f_heart;
	FileWriter fstream_heart;
	BufferedWriter out_heart;
	
	File f_Walk;
	FileWriter fstream_Walk;
	BufferedWriter out_Walk;
	int count=0;
	final MediaPlayer mp=new MediaPlayer();
	
	private Cursor mDataCursor;
	private String deviceDetails="";
	private String fireflyinfo;
	private int playSound=0;
	Complex[] compx=new Complex[windowSize];
	Complex[] compy=new Complex[windowSize];
	Complex[] compz=new Complex[windowSize];
	private double[] weightAnupam={21.8119,21.8346,21.6762,21.0613}; //lefthip,;leftpocket,righthip,rightpocket
	private double[] weightAntal={15.0911,19.9948,19.7095,17.0578};
	private double distConstant=0,totalDistance=0;
	private Spinner spinnerPerson,spinnerPosition;
	private long timeStamp=0;
	private WifiReceiver receiver;
	private EditText timeText;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
     // Extract device information from the intent extras
		Bundle extras = getIntent().getExtras();
		try{
		deviceDetails = extras
				.getString(SharedVariables.CONST_DEVICE_INFORMATION);
		}catch(Exception e){
			Log.i("deviceDetailsCreate", deviceDetails);
		}
		fireflyinfo=deviceDetails;
       

		txtCalibration=(TextView)findViewById(R.id.txtCalibration);
        timestamp1=(TextView)findViewById(R.id.timestamp1);
        txtfirefly=(TextView)findViewById(R.id.txtfirefly);
        txtheart=(TextView)findViewById(R.id.txtheart);
        //---use the LocationManager class to obtain GPS locations---
        // Get the location manager
        LocationManager locMan;
        locMan =
           (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
                                25, 0, gpsListener);  
         
		activityType=(Spinner)findViewById(R.id.spinner);
		ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this,R.array.activitytype,android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    activityType.setAdapter(adapter);
	    
	    spinnerPerson=(Spinner)findViewById(R.id.spinnerPerson);
	    ArrayAdapter<CharSequence> adapter1=ArrayAdapter.createFromResource(this,R.array.person,android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinnerPerson.setAdapter(adapter1);
	    
	    spinnerPosition=(Spinner)findViewById(R.id.spinnerPosition);
	    ArrayAdapter<CharSequence> adapter2=ArrayAdapter.createFromResource(this,R.array.position,android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinnerPosition.setAdapter(adapter2);
	    
        //accelerometer
        sensorMan = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensorMan.registerListener(sensorlistener,
        sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        SensorManager.SENSOR_DELAY_FASTEST); 
        sensorMan.registerListener(sensorlistener,	sensorMan.getDefaultSensor(Sensor.TYPE_ORIENTATION),
        		SensorManager.SENSOR_DELAY_FASTEST);
     
        //orientation sensor
        wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        
        //time text box
        timeText=(EditText)findViewById(R.id.timeText);
        
        
    }
    
    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		
		if(deviceDetails.startsWith("FireFly")){
			Log.i("mConnection", "initializing");
			 mConnection = new ServiceConnection()
				{
					public void onServiceConnected(ComponentName className,
							IBinder service)
					{
						// This is called when the connection with the service has been
						// established, giving us the service object we can use to
						// interact with the service. Because we have bound to a
						// explicit
						// service that we know is running in our own process, we can
						// cast its IBinder to a concrete class and directly access it.
						
						mHRMService = ((SensorService.LocalBinder) service)
								.getService();

						mHRMService.initialize(deviceDetails, activityHandler);
		   
						mHRMService.connectBluetooth();

						if (mHRMService.isConnected())
						{
							mHRMService.runService();

							//btnReconnect.setEnabled(false);

							// Create our Preview view and set it as the content of our
							// Activity
							
							//setContentView(mGLSurfaceView);

							// Tell the user about this for our demo.
							Toast.makeText(DataCollection.this,
									"Data Reading Started..", Toast.LENGTH_SHORT)
									.show();
						}
					}

					public void onServiceDisconnected(ComponentName className)
					{
						// This is called when the connection with the service has been
						// unexpectedly disconnected -- that is, its process crashed.
						// Because it is running in our same process, we should never
						// see this happen.
						mHRMService = null;
						Toast.makeText(DataCollection.this,
								"Data Reading Stopped !", Toast.LENGTH_SHORT).show();
					}
				};
		}
		try {
			mp.setDataSource(Environment.getExternalStorageDirectory()+"/beep.wav");
			
			mp.prepare();
		 
			//mp.start();
			
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		
		
		//mp.setLooping(true);
    	Log.i("Media Player", mp.isLooping()+""+mp.isPlaying());
    	super.onStart();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
    	boolean result= super.onCreateOptionsMenu(menu);
		menu.add(0,START_ID,0,R.string.menu_start);
		menu.add(0,STOP_ID,0,R.string.menu_stop);
		//menu.add(0,4,0,"Connect Heart Sensor");
		menu.add(0,EXIT_ID,0,R.string.menu_exit);
		return result;
	}
	@Override
	
	
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
        case START_ID:
        	receiver=new WifiReceiver(this);
        	registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        	try {
        		mHandler.postDelayed(mUpdateTimeTask, 0);
            	doBindService();
				Thread.sleep(8000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	Calendar c=Calendar.getInstance();
            Date dt=c.getTime();
            fileName=(dt.getMonth()+1)+"_"+dt.getDate()+"_"+(1900+dt.getYear())+""+"_"+dt.getHours()+"_"+dt.getMinutes()+"_"+dt.getSeconds();
            try
    	   	 {
       		 f=new File(Environment.getExternalStorageDirectory()+"/"+fileName+"_Phone");
       	      if(!f.exists())
       	      {
       	    	  f.createNewFile();
       	      }
       		 fstream=new FileWriter(Environment.getExternalStorageDirectory()+"/"+fileName+"_Phone",true);
    	   	 }
    	   	 catch(Exception e)
    	   	 {
    	   		 Log.e("OnOptionsItemSelected",e.toString());
    	   	 }
    	   	 
    	   	 /*try
    	   	 {
      		 f_firefly=new File(Environment.getExternalStorageDirectory()+"/"+fileName+"_FireFly");
      	      if(!f_firefly.exists())
      	      {
      	    	  f_firefly.createNewFile();
      	      }
      		 fstream_firefly=new FileWriter(Environment.getExternalStorageDirectory()+"/"+fileName+"_FireFly",true);
    	   	 }
    	   	 catch(Exception e)
    	   	 {
    	   		 e.printStackTrace();
    	   	 }
    	   	 /*try
    	   	 {
      		 f_heart=new File(Environment.getExternalStorageDirectory()+"/"+fileName+"_Heart");
      	      if(!f_heart.exists())
      	      {
      	    	  f_heart.createNewFile();
      	      }
      		 fstream_heart=new FileWriter(Environment.getExternalStorageDirectory()+"/"+fileName+"_Heart",true);
    	   	 }
    	   	 catch(Exception e)
    	   	 {
    	   		 e.printStackTrace();
    	   	 }*/
        	out=new BufferedWriter(fstream);
        	
        	
        	try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	mp.start();
    	   /*	 try{
    	   	f_Walk=new File(Environment.getExternalStorageDirectory()+"/Walk");
			fstream_Walk=new FileWriter(f_Walk,true);
			out_Walk=new BufferedWriter(fstream_Walk);
    	   	 }catch(Exception e){
    	   		 
    	   	 }
        	out_firefly=new BufferedWriter(fstream_firefly);
        	//out_heart=new BufferedWriter(fstream_heart);
        	accCount=0;
        	*/
        	wifiManager.startScan();
        	new Thread(new Runnable(){

				public void run() {
				
					// TODO Auto-generated method stub
					try {
						
						Thread.sleep(Integer.parseInt(timeText.getText().toString()));
						
			        	
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch(IllegalArgumentException e){
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

					
					mHandler.removeCallbacks(mUpdateTimeTask);
		    
		        	//doUnbindService();    	
		        	//alertbox("Enter Steps and Distance", "Please enter numbers separated by space:");
		        	//uploadFiles();
		        	unregisterReceiver(receiver);
		        	receiver.closeWriter();
		        	runOnUiThread(new Runnable(){

						public void run() {
							// TODO Auto-generated method stub
							alertbox("Enter Steps and Distance", "Please enter numbers separated by space:");
						}
		        		
		        	});
		        	mp.start();
					
				}       		
        	}).start(); 
        	timeStamp=System.currentTimeMillis();
			/*if(spinnerPerson.getSelectedItemPosition()==1){
				//anupam
				distConstant=weightAnupam[spinnerPosition.getSelectedItemPosition()]/28;
			}
			else if(spinnerPosition.getSelectedItemPosition()==0){
				//antal
				distConstant=weightAntal[spinnerPerson.getSelectedItemPosition()]/28;
			}
			System.out.println(distConstant);
			*/
            return true;
        case STOP_ID:
        	mp.start();
			mHandler.removeCallbacks(mUpdateTimeTask);
        	
        	//doUnbindService();    	
        	alertbox("Enter Steps and Distance", "Please enter numbers separated by space:");
        	//uploadFiles();
        	unregisterReceiver(receiver);
        	receiver.closeWriter();
        	return true;
        case 4:
        	Intent i=new Intent(this, ActivityDiscover.class);
        	startActivityForResult(i, 1);
        	return true;
        case EXIT_ID:
        	finish();
        	return true;
        }
		return super.onOptionsItemSelected(item);
	}
		
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

    	if(requestCode==1){
    		Bundle extras1 = data.getExtras();
    		final String deviceDetails2 = extras1
    				.getString(SharedVariables.CONST_DEVICE_INFORMATION);
    		mHeartConnection = new ServiceConnection()
			{
				public void onServiceConnected(ComponentName className,
						IBinder service)
				{
					// This is called when the connection with the service has been
					// established, giving us the service object we can use to
					// interact with the service. Because we have bound to a
					// explicit
					// service that we know is running in our own process, we can
					// cast its IBinder to a concrete class and directly access it.
					mHeartService = ((HeartRateMonitorService.LocalBinder) service)
							.getService();

					mHeartService.initialize(deviceDetails2, activityHandlerHeart);

					mHeartService.connectBluetooth();
					
					if (mHeartService.isConnected())
					{
						mHeartService.runService();
						
						//btnReconnect.setEnabled(false);
						
						// Tell the user about this for our demo.
						Toast.makeText(DataCollection.this,
								"Data Reading Started..", Toast.LENGTH_SHORT).show();
					}
				}

				public void onServiceDisconnected(ComponentName className)
				{
					// This is called when the connection with the service has been
					// unexpectedly disconnected -- that is, its process crashed.
					// Because it is running in our same process, we should never
					// see this happen.
					mHeartService = null;
					Toast.makeText(DataCollection.this,
							"Data Reading Stopped !", Toast.LENGTH_SHORT).show();
				}
			};
	
			if(deviceDetails.startsWith("FireFly")){
				 mConnection = new ServiceConnection()
					{
						public void onServiceConnected(ComponentName className,
								IBinder service)
						{
							// This is called when the connection with the service has been
							// established, giving us the service object we can use to
							// interact with the service. Because we have bound to a
							// explicit
							// service that we know is running in our own process, we can
							// cast its IBinder to a concrete class and directly access it.
							
							mHRMService = ((SensorService.LocalBinder) service)
									.getService();

							mHRMService.initialize(deviceDetails, activityHandler);
			   
							mHRMService.connectBluetooth();

							if (mHRMService.isConnected())
							{
								mHRMService.runService();

								//btnReconnect.setEnabled(false);

								// Create our Preview view and set it as the content of our
								// Activity
								
								//setContentView(mGLSurfaceView);

								// Tell the user about this for our demo.
								Toast.makeText(DataCollection.this,
										"Data Reading Started..", Toast.LENGTH_SHORT)
										.show();
							}
						}

						public void onServiceDisconnected(ComponentName className)
						{
							// This is called when the connection with the service has been
							// unexpectedly disconnected -- that is, its process crashed.
							// Because it is running in our same process, we should never
							// see this happen.
							mHRMService = null;
							Toast.makeText(DataCollection.this,
									"Data Reading Stopped !", Toast.LENGTH_SHORT).show();
						}
					};
			}
    	}
		//super.onActivityResult(requestCode, resultCode, data);
	}

	private SensorEventListener sensorlistener = new SensorEventListener(){
   	 
 	   public void onAccuracyChanged(Sensor arg0, int arg1){}

 	   public void onSensorChanged(SensorEvent evt)
 	   {
 		  if(evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
         {
	      float vals[] = evt.values;
	      x=vals[0];
	      y=vals[1];
	      z=vals[2];	      
         }
 		 if(evt.sensor.getType() == Sensor.TYPE_ORIENTATION)
         {
 	      float vals[] = evt.values;
 	      ox=vals[0];
	      oy=vals[1];
	      oz=vals[2];      
         }
 	   }
 	};
 	
 	private LocationListener gpsListener = new LocationListener(){
 	      
 	      public void onLocationChanged(Location location)
 	      {
 	    	
 	    	if(location!=null){
 	    			 lat=location.getLatitude();
 	                 lng=location.getLongitude();
 	                 speed=location.getSpeed();
 	     		}
 	    	
 	      }
 	      public void onProviderDisabled(String provider){}
 	      public void onProviderEnabled(String provider){}
 	      public void onStatusChanged(String provider, int status, Bundle extras){}
 	};
 	
 	int index=0;
 	private Runnable mUpdateTimeTask = new Runnable() {
 	   public void run() {
  	       long millis = System.currentTimeMillis(); 	                
 	       timestamp1.setText(Long.toString(millis));
 	      Calendar c=Calendar.getInstance();
          Date dt=c.getTime();
     		  mHandler.postDelayed(mUpdateTimeTask,25);
     		  try {
     			// Log.i("ACC VALUES", x+","+y+","+","+z+","+index++);
				out.write((dt.getMonth()+1)+"/"+dt.getDate()+"/"+(1900+dt.getYear())+" "+dt.getHours()+":"+dt.getMinutes()+":"+dt.getSeconds()+
						" "+x+" "+ 
						   y+" "+ 
						   z+" "+ 
						   ox+" "+
						   oy+" "+
						   oz+" "+
						   lat+" "+
						   lng+" "+
						   speed+" "+
						   activityType.getSelectedItem().toString()+" "+spinnerPerson.getSelectedItem().toString()+" "
						   +spinnerPosition.getSelectedItem().toString()+" "+
						   receiver.robotlab+" "+
						   receiver.robotlab_tc+" "+
						   receiver.linksys_uber+" "+
						   receiver.CLMC+" "+
						   receiver.pr1009LAN+" "+
						   receiver.robotlab_conf+" "+
						   receiver.qinlab+" "+
						   receiver.USC+" "+
						   receiver.BrainBodyDynamics+" "+
						   receiver.Sukhatme+" "+
						   receiver.SENSOID+" "+
						   receiver.ENL+" "+
						   receiver.SAAN+" "+
						   receiver.LittleDog+" "+
						   receiver.Fusion
					
						 +"\n");
				out.flush(); 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("Error",e.getMessage()); 
			}
   	
 	   }
     };
    protected void alertbox(String title, String mymessage)  
	   {  
    	 final EditText trackName = new EditText(DataCollection.this);
		   new AlertDialog.Builder(DataCollection.this)  
		   .setMessage(mymessage)  
		   .setTitle(title)  
		   .setCancelable(true)  
		   .setNeutralButton(android.R.string.ok,  
				   new DialogInterface.OnClickListener() {  
			   public void onClick(DialogInterface dialog, int whichButton){
				   Log.i("Edit Text", trackName.getText().toString());
				   try {
					out.write("NumberofSteps:"+trackName.getText().toString().trim());
					//out_firefly.write("NumberofSteps:"+trackName.getText().toString().trim());
					//out_heart.write("NumberofSteps:"+trackName.getText().toString().trim());
					//out_heart.close();
					//out_firefly.close();
					out.close();
					fstream.close();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			   }  
		   		}).setView(trackName)
		   .show();  
	   }
    void doBindService()
 	{
 		// Establish a connection with the service. We use an explicit
 		// class name because we want a specific service implementation that
 		// we know will be running in our own process (and thus won't be
 		// supporting component replacement by other applications).
    	try{
    		Log.i("mConnection", deviceDetails);
    	if(mConnection!=null){
    		Log.i("mConnection", "Coming here...inside..");
 		bindService(new Intent(DataCollection.this, SensorService.class),
 				mConnection, Context.BIND_AUTO_CREATE);
 		timeStamp=System.currentTimeMillis();
    	}
 		if(mHeartConnection!=null){
 			bindService(new Intent(DataCollection.this,
 					HeartRateMonitorService.class), mHeartConnection,
 					Context.BIND_AUTO_CREATE);
 		}
    	
    	}catch(Exception e){
    		e.printStackTrace();
    	}
 		isBound = true;
 	}
 	void doUnbindService()
 	{
 		if (isBound)
 		{
 			// Detach our existing connection.
 			unbindService(mConnection);
 			if(mHeartConnection!=null){
 				//unbindService(mHeartConnection);
 			}
 			isBound = false;
 		}
 	}
 	private final Handler activityHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{       	
			Calendar c=Calendar.getInstance();
	        Date dt=c.getTime();
			try
			{
				Log.i("Message", msg.toString());
				ax_firefly = msg.getData().getInt(SensorService.CONST_ACCELERATION_X);
				ay_firefly = msg.getData().getInt(SensorService.CONST_ACCELERATION_Y);
				az_firefly = msg.getData().getInt(SensorService.CONST_ACCELERATION_Z);
				
				gx_firefly = msg.getData().getFloat(SensorService.CONST_GYROSCOPE_X);
				gy_firefly = msg.getData().getFloat(SensorService.CONST_GYROSCOPE_Y);
				gz_firefly = msg.getData().getFloat(SensorService.CONST_GYROSCOPE_Z);
				timestamp1.setText(""+gx_firefly);
				mx_firefly=msg.getData().getInt(SensorService.CONST_COMPASS_X);
				my_firefly=msg.getData().getInt(SensorService.CONST_COMPASS_Y);
				mz_firefly=msg.getData().getInt(SensorService.CONST_COMPASS_Z);
				
				txtfirefly.setText(ax_firefly+","+ay_firefly+","+az_firefly);
				out_firefly.write((dt.getMonth()+1)+"/"+dt.getDate()+"/"+(1900+dt.getYear())+" "+dt.getHours()+":"+dt.getMinutes()+":"+dt.getSeconds()+
						" "+ax_firefly+" "+ 
						   ay_firefly+" "+ 
						   az_firefly+" "+ 
						   gx_firefly+" "+ 
						   gy_firefly+" "+ 
						   gz_firefly+" "+ 
						   mx_firefly+" "+
						   my_firefly+" "+
						   mz_firefly+" "+
						   activityType.getSelectedItem().toString()+" "+
						   spinnerPerson.getSelectedItem().toString()+" "+
						   spinnerPosition.getSelectedItem().toString()+" "
						   
						   
						 +"\n");
							
				
				double acc=Math.sqrt(Math.pow(ax_firefly, 2)+Math.pow(ay_firefly, 2)+Math.pow(az_firefly,2));
				accTotal[accCount]=new Complex(acc,0);
				/*
				long currentTimeStamp=System.currentTimeMillis()-timeStamp;
				//Log.i("ACC TOTAL", accTotal[accCount]+"  count="+accCount+"timeStamp"+timeStamp+" current"+currentTimeStamp);
				if(currentTimeStamp>=time){
					for(int i=0;i<=accCount;i++){
						accTemp[i]=new Complex(accTotal[i].re, 0);
					}
					for(int i=accCount+1;i<2048;i++){
						accTemp[i]=new Complex(0,0);
					}
					int j=0;
					for(int i=accCount/2;i<=accCount;i++){
						accTotal[j]=new Complex(accTotal[i].re,0);
						j++;
					}
					accCount=accCount/2;
					new Thread(new Runnable(){

							public void run() {
								// TODO Auto-generated method stub
								try{
								Complex[] accFFT=new Complex[windowSize];
								for(int i=0;i<windowSize;i++){
									accFFT[i]=new Complex(accTemp[i].re,0);
								}
								//Log.e("Calculating FFT", accFFT[0]+" "+accFFT[windowSize-1]);
							fftCalculation=new FFTCalculation(accTemp,windowSize);
							final double fftPeak=fftCalculation.calculateFFT(0);
							//get the calibration factor the preferences
					        //SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
					        //float calibrationFactor = settings.getFloat("CalibrationFactor", 0);
					        //Log.i("Step Frequency", stepFrequency+","+calibrationFactor+","+time);
					        //final double distance=stepFrequency*calibrationFactor*time; //in meters
							out_Walk=new BufferedWriter(fstream_Walk);
							int tempTime=0;
							if(totalDistance==0){
								tempTime=11000;
							}else{
								tempTime=5500;
							}
							totalDistance+=fftPeak*distConstant*tempTime/1000;
							out_Walk.write(fftPeak+" "+distConstant+" "+tempTime);
							out_Walk.flush();
					        Log.e("FFT PEAK", fftPeak+"");
					        runOnUiThread(new Runnable() {
								
								public void run() {
									// TODO Auto-generated method stub
									
									txtCalibration.setText(totalDistance+" "+fftPeak);
								}
							});
							}catch(Exception e){
								e.printStackTrace();
							}
							Log.i("Calculating FFT", ",.....................");
						}
						
					}).start();
					        timeStamp=System.currentTimeMillis();
				}				time=5500;
				accCount++;*/
			} catch (Exception e)
			{
				Log.e("WRITE_ERROR1234", e.toString());
			}
		}
	};
	private final Handler activityHandlerHeart = new Handler()
	{
		public void handleMessage(Message msg)
		{	
			Calendar c=Calendar.getInstance();
	        Date dt=c.getTime();
			try
			{
				
				String data=msg.getData().getString(
									HeartRateMonitorService.CONST_MESSAGE_STRING);
				txtheart.setText(data);
				String[] values=data.split("\n");
				String[] intValues=new String[values.length];
				String[] temp=new String[2];
				for(int i=0;i<values.length;i++){
					temp=values[i].split(":");
					intValues[i]=temp[1].trim();
					
				}
				out_heart.write((dt.getMonth()+1)+"/"+dt.getDate()+"/"+(1900+dt.getYear())+" "+dt.getHours()+":"+dt.getMinutes()+":"+dt.getSeconds()+
							" "+intValues[4]+" "+intValues[5]+" "+intValues[21]+" "+intValues[22]+" "+intValues[23]+" "+intValues[24]+" "+
							   activityType.getSelectedItem().toString()
							 +"\n");

			} catch (Exception e)
			{
				Log.e("WRITE_ERROR567", e.toString());
			}
		}
	};
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Log.i("WRITING", "On Destroy called");
		//doUnbindService();
	}
	private void uploadFiles(){
		Session session = null;
		Channel channel = null;
		try {
		 JSch ssh = new JSch();
		 ssh.setKnownHosts("/home/mobilesensing/.ssh/known_hosts");
		 session = ssh.getSession("mobilesensing", "128.125.124.130", 22);
		 session.setPassword("anoop123");
		 session.connect();
		 channel = session.openChannel("sftp");
		 channel.connect();
		 ChannelSftp sftp = (ChannelSftp) channel;
		 sftp.put(Environment.getExternalStorageDirectory()+"/"+fileName+"_Phone", "/home/mobilesensing");
		 } catch (JSchException e) {
		  e.printStackTrace();
		 } catch (SftpException e) {
		  e.printStackTrace();
		 }
		 finally {
		  if (channel != null)
		  {
		   channel.disconnect();
		  }
		  if (session != null)
		  {
		   session.disconnect();
		  }
		 }
		
		
	}
}
