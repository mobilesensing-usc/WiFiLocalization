package com.project.datacollection2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class FFTCalculation {

	private int windowSize=2048;
	private static final int samplingRate=50; //Hz
	private Complex[] accTotal; 
	private Double[] DFFT=new Double[windowSize];
	FFTCalculation(Complex[] compx,int windowsize){
		accTotal=compx;
		windowSize=windowsize;
	}
	@SuppressWarnings("unused")
	public double calculateFFT(int startingPoint){

		double fq=0;
		try {
		  BufferedWriter out=new BufferedWriter(new FileWriter(new File(Environment.getExternalStorageDirectory()+"/FFT"),true));
		  for(int i=0;i<windowSize;i++){
			  out.write(accTotal[i]+" ");
		  }
		  out.write("\n");
		  fft(accTotal);
	    
	      for(int i=0;i<windowSize;i++){
				
				DFFT[i]=accTotal[i].abs()/windowSize;
				out.write(DFFT[i]+" ");
		  }
	      out.write("\n");
	      out.close();
	     // dispose all the resources after using them
	      int maxi=20;
	      Double mxm = DFFT[maxi];
	      for (int i=20; i<windowSize*0.2; i++) {
	      if (DFFT[i]>mxm) {
	      mxm = DFFT[i];
	      maxi=i;
	      }
	      }
	      Log.i("FREQUENCY", maxi+"");
	      fq=(double)(maxi*(samplingRate/2))/(windowSize/2);			 
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    return fq;
	}
   protected void fft(Complex[] x) {

		// check that length is a power of 2
		int N = x.length;
		if (Integer.highestOneBit(N) != N) {
			throw new RuntimeException("N is not a power of 2");
		}
		Complex[] out=new Complex[N];
		 for (int f = 0; f < N; f++) {  // For each output element
		        double sumreal = 0;
		        double sumimag = 0;
		        for (int t = 0; t < N; t++) {  // For each input element
		            sumreal += x[t].re*Math.cos(2*Math.PI * t * f / N) - x[t].im*Math.sin(2*Math.PI * t * f / N);
		            sumimag += x[t].re*Math.sin(2*Math.PI * t * f / N) + x[t].im*Math.cos(2*Math.PI * t * f / N);
		        }
		        out[f]=new Complex(sumreal,sumimag);
		        
		    }
		 accTotal=out;
	}
   
}
