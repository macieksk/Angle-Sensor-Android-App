/***
  Copyright (c) 2008-2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  From _The Busy Coder's Guide to Advanced Android Development_
    http://commonsware.com/AdvAndroid
*/

package com.trikotta.anglesensor;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.*;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;

/*
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
*/

public class AppService extends WakefulIntentService {
  
  private SharedPreferences mPrefs;  
  private static final int SLEEP_FOR_SENSORS_TIME=1000;  //in ms
  private static final int DEFAULT_PHONE_CALL_DIFF = 30; //30s
  
  public AppService() {
	    super("AppService");    
  }
	   
  @Override
  protected void doWakefulWork(Intent intent) {
	
	Log.d("AppService", "DoingWakeFulWork");	
    
	mPrefs = getSharedPreferences(FullscreenActivity.PREF_FILE,0);
	
	SensorManager mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);        
    
    final OrientationSensorEventListener mEventListener = new OrientationSensorEventListener() {
        public void onReactToOrientationChange(){
        	reactToOrientationChange(mValuesOrientation);
        }                                   
    };
    
    mSensorManager.registerListener(mEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
            SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.registerListener(mEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 
            SensorManager.SENSOR_DELAY_NORMAL);        
    //mSensorManager.registerListener(mEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), 
    //        SensorManager.SENSOR_DELAY_NORMAL);

    android.os.SystemClock.sleep(SLEEP_FOR_SENSORS_TIME);
    
    mSensorManager.unregisterListener(mEventListener);
    Log.d("AppService", "Finish DoingWakeFulWork");	
    
    /*  
    File log=new File(Environment.getExternalStorageDirectory(),
                      "AlarmLog.txt");
    
    try {
      BufferedWriter out=new BufferedWriter(
                            new FileWriter(log.getAbsolutePath(),
                                            log.exists()));
      
      out.write(new Date().toString());
      out.write("\n");
      out.close();
    }
    catch (IOException e) {
      Log.e("AppService", "Exception appending to log file", e);
    }*/
  }
  
  private void reactToOrientationChange(float[] mValuesOrientation){
	  String orientStr="Angles: ";  
  	  for (int i=0;  i<mValuesOrientation.length;  i++) {  
  		orientStr += mValuesOrientation[i]+",";  
  	  } 
  	  Log.d("React To ORIENTATION Change:",orientStr);
  	  
  	  Log.d("reactToOrientChange mPrefs",mPrefs.toString());
  	  
  	  float[] mEditBottom = {-400,-400,-400};
	  mEditBottom[0] = mPrefs.getFloat("angleBottom0",-400);
	  mEditBottom[1] = mPrefs.getFloat("angleBottom1",-40);
	  mEditBottom[2] = mPrefs.getFloat("angleBottom2",-400);					
	 
	  float[] mEditTop = {400,400,400};
	  mEditTop[0] = mPrefs.getFloat("angleTop0",400);
	  mEditTop[1] = mPrefs.getFloat("angleTop1",40);
	  mEditTop[2] = mPrefs.getFloat("angleTop2",400);
	  
       if (mValuesOrientation[0]<mEditBottom[0] || mValuesOrientation[0]>mEditTop[0]
    	 ||mValuesOrientation[1]<mEditBottom[1] || mValuesOrientation[1]>mEditTop[1]
         ||mValuesOrientation[2]<mEditBottom[2] || mValuesOrientation[2]>mEditTop[2]){
    	   long minDiff = mPrefs.getInt("minPhoneCallDiff",DEFAULT_PHONE_CALL_DIFF)*1000;
    	   if ((android.os.SystemClock.elapsedRealtime()-mPrefs.getLong("lastPhoneCall",0))>minDiff){
        		Log.d("phoneCall diff",String.valueOf(android.os.SystemClock.elapsedRealtime()-mPrefs.getLong("lastPhoneCall",0)));
        		SharedPreferences.Editor ed = mPrefs.edit();
        		ed.putLong("lastPhoneCall",android.os.SystemClock.elapsedRealtime());
        		ed.commit();
        		phoneCall();
    	   }
       }                            	
   }  	  
    
  
  private void phoneCall() {
  	Log.d("phoneCall","enter");
      try {
          Intent callIntent = new Intent(Intent.ACTION_CALL);
          String phone = mPrefs.getString("phoneToCall", getString(R.string.default_phone));
          String uri = "tel:"+phone;
          Log.d("uri:",uri);
          callIntent.setData(Uri.parse(uri));
          callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          Log.d("Call","startActivity");        	
          startActivity(callIntent);            
      } catch (ActivityNotFoundException e) {
          Log.e("AppService", "Call failed", e);
      }   
  }

  
}