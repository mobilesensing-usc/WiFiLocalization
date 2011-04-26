package com.project.datacollection2;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

public class WifiDisplay extends Activity {
	public WifiManager wifiManager;
	
	private Button btnCapture;
	public ListView listWifi;
	public Spinner spinnerPosition;
	public EditText locationNo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifidisplay);
		btnCapture=(Button)findViewById(R.id.Capture);
		listWifi=(ListView)findViewById(R.id.wifiList);
		//spinnerPosition=(Spinner)findViewById(R.id.spinnerPosition);
		//ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this,R.array.wifiPositions,android.R.layout.simple_spinner_item);
		//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    //spinnerPosition.setAdapter(adapter);
		locationNo=(EditText)findViewById(R.id.locationNo);
		wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        
        btnCapture.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//WifiReceiver receiver=new WifiReceiver(WifiDisplay.this);
				 //registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
				
				wifiManager.startScan();
			}
		});
	}

}
