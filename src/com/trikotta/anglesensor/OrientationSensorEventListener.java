/**
 * 
 */
package com.trikotta.anglesensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 */
public abstract class OrientationSensorEventListener implements SensorEventListener {

	  public float[] mGravity;
	  public float[] mMagnetic;
	  public float[] mValuesOrientation = {0,0,0};
	  		  
	  private float[] getDirection() 
	  {
	     
	      //float[] temp = new float[9];
	      float[] R = new float[9];
	      //Load rotation matrix into R
	      SensorManager.getRotationMatrix(R, null,
	              mGravity, mMagnetic);
	     
	      //Remap to camera's point-of-view
	      /*SensorManager.remapCoordinateSystem(temp,
	              SensorManager.AXIS_X,
	              SensorManager.AXIS_Z, R);
	      */
	      //Return the orientation values
	      float[] values = new float[3];
	      SensorManager.getOrientation(R, values);
	     
	      
	      //Convert to degrees
	      for (int i=0; i < values.length; i++) {
	          Double degrees = (values[i] * 180) / Math.PI;
	          values[i] = degrees.floatValue();
	      }

	      //return values[0];
	      return values;
	     
	  }

	//public OrientationSensorEventListener() {
	//	// TODO Auto-generated constructor stub
	//}
	

	@Override	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
	
	@Override    
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
               
        case Sensor.TYPE_ACCELEROMETER:
            mGravity = event.values.clone();
            Log.d("Sensor","ACCEL");
            break;
        case Sensor.TYPE_MAGNETIC_FIELD:
        	Log.d("Sensor","MAGNET");
        	mMagnetic = event.values.clone();
            break;
        case Sensor.TYPE_ORIENTATION:
        	mValuesOrientation = event.values.clone();
        	Log.d("Sensor","ORIENTATION");
        	break;
        default:
        	 Log.d("Sensor","default");                 
            
        }
        if(mGravity != null && mMagnetic != null) {
        	mValuesOrientation = getDirection();                            
        }                        
        if (mValuesOrientation[0]!=0 || mValuesOrientation[1]!=0 || mValuesOrientation[2]!=0 ){
        		onReactToOrientationChange();
        }
    }
	
	abstract public void onReactToOrientationChange();		

}
