package com.project.datacollection2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.wifi.ScanResult;
import android.os.Environment;
import android.util.Log;

public class WifiReceiver extends BroadcastReceiver{

	private DataCollection wifi;
	private BufferedWriter out;
	File f;
	FileWriter fstream = null;
	int count=0;
	String direction="East";
	public int robotlab=0;
	public int robotlab_tc=0;
	public int linksys_uber=0;
	public int CLMC=0;
	public int pr1009LAN=0;
	public int robotlab_conf=0;
    public int qinlab=0;
	public int USC=0;
	public int BrainBodyDynamics=0;
	public int Sukhatme=0;
	public int SENSOID=0;
	public int ENL=0;
	public int SAAN=0;
	public int LittleDog=0;
	public int Fusion=0;
    
	/**
	 * Test upload.
	 * @param wifiDisplay
	 */
	public WifiReceiver(DataCollection wifiDisplay){
		wifi=wifiDisplay;
		
		Calendar c=Calendar.getInstance();
        Date dt=c.getTime();
        String fileName=(dt.getMonth()+1)+"_"+dt.getDate()+"_"+(1900+dt.getYear())+""+"_"+dt.getHours()+"_"+dt.getMinutes()+"_Wifi";
		//fileName=wifi.spinnerPosition.getSelectedItem().toString();
       // fileName="wifi1.arff";
        /*try
	   	 {
  		 f=new File(Environment.getExternalStorageDirectory()+"/"+fileName+"");
  	      if(!f.exists())
  	      {
  	    	  f.createNewFile();
  	      }
  		 fstream=new FileWriter(Environment.getExternalStorageDirectory()+"/"+fileName+"",true);
	   	 }
	   	 catch(Exception e)
	   	 {
	   		 Log.e("OnOptionsItemSelected",e.toString());
	   	 }*/
	   	 //out=new BufferedWriter(fstream);
	}
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		try{
		List<ScanResult> accessPoints=wifi.wifiManager.getScanResults();
		//Log.i("WIFI", accessPoints.get(0)+""+accessPoints.size());
		Calendar c=Calendar.getInstance();
        Date dt=c.getTime();
        String[] wifiAccessPoints=new String[accessPoints.size()];
      
        
		for(int i=0;i<accessPoints.size();i++){
			wifiAccessPoints[i]=(dt.getMonth()+1)+"/"+dt.getDate()+"/"+(1900+dt.getYear())+" "+dt.getHours()+":"+dt.getMinutes()+":"+dt.getSeconds()+
			" "+accessPoints.get(i).BSSID+" "+accessPoints.get(i).SSID+" "+accessPoints.get(i).level+"\n";
			
			Log.i("WifiAccessPoints", wifiAccessPoints+"");
			if(accessPoints.get(i).SSID.equals("robotlab")){
				robotlab=accessPoints.get(i).level;
			}else if(accessPoints.get(i).SSID.equals("robotlab-tc")){
				robotlab_tc=accessPoints.get(i).level;
			}else if(accessPoints.get(i).SSID.equals("linksys_uber")){
				linksys_uber=accessPoints.get(i).level;
			}else if(accessPoints.get(i).SSID.equals("CLMC")){
				CLMC=accessPoints.get(i).level;
			}else if(accessPoints.get(i).SSID.equals("pr1009LAN")){
				pr1009LAN=accessPoints.get(i).level;
			}else if(accessPoints.get(i).SSID.equals("robotlab-conf")){
				robotlab_conf=accessPoints.get(i).level;
			}else if(accessPoints.get(i).SSID.equals("qinlab")){
				qinlab=accessPoints.get(i).level;
			}else if(accessPoints.get(i).SSID.equals("USC Wireless")){
				USC=accessPoints.get(i).level;
			}else if(accessPoints.get(i).SSID.equals("BrainBodyDynamics")){
				BrainBodyDynamics=accessPoints.get(i).level;
			}else if(accessPoints.get(i).SSID.equals("Sukhatme Office Network")){
				Sukhatme=accessPoints.get(i).level;
			}else if(accessPoints.get(i).SSID.equals("SENSOID")){
				SENSOID=accessPoints.get(i).level;
			}else if(accessPoints.get(i).SSID.equals("ENL")){
				ENL=accessPoints.get(i).level;
			}else if(accessPoints.get(i).SSID.equals("SAAN")){
				SAAN=accessPoints.get(i).level;
			}else if(accessPoints.get(i).SSID.equals("LittleDog")){
				LittleDog=accessPoints.get(i).level;
			}else if(accessPoints.get(i).SSID.equals("Fusion")){
				Fusion=accessPoints.get(i).level;
			}
		}
		//try {
			//out.write((dt.getMonth()+1)+"/"+dt.getDate()+"/"+(1900+dt.getYear())+" "+dt.getHours()+":"+dt.getMinutes()+":"+dt.getSeconds()+" "+
			//		robotlab+" "+robotlab_tc+" "+linksys_uber+" "+CLMC+" "+pr1009LAN
				//	+" "+robotlab_conf+" "+qinlab+" "+USC+" "+BrainBodyDynamics+" "+
				//	Sukhatme+" "+SENSOID+" "+ENL+" "+SAAN+" "+LittleDog+" "+Fusion+
					//" "+wifi.locationNo.getText().toString()+" "+direction+
				//	"\n");
		//} catch (IOException e1) {
			// TODO Auto-generated catch block
		//	e1.printStackTrace();
	//	}

	/*	ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(arg0,
                android.R.layout.simple_list_item_1, wifiAccessPoints);
		wifi.listWifi.setAdapter(adapter1);*/
	/*	count++;
		if(count==15){
			direction="North";
			playbeepsound();
		}else if(count==30){
			direction="West";
			playbeepsound();
		}else if(count==45){
			direction="South";
			playbeepsound();
		}
		if(count>=60){
			playbeepsound();
			closeWriter();
			count=0;
		}else{
			
		}*/
		wifi.wifiManager.startScan();
		}catch(Exception e){
			wifi.wifiManager.startScan();
		}
		
	}
	public void openWriter(){
		 //out=new BufferedWriter(fstream);
	}
	public void closeWriter(){
		/*try {
		//	out.close();
		//	fstream.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	public void playbeepsound(){
		try {
			MediaPlayer mp=new MediaPlayer();
			mp.setDataSource(Environment.getExternalStorageDirectory()+"/beep.wav");
			
			mp.prepare();
		 
			mp.start();
			
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
	}
	
}
