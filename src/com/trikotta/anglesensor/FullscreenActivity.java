package com.trikotta.anglesensor;

import com.trikotta.anglesensor.util.SystemUiHider;

//import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
//import android.os.PowerManager;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import android.hardware.*;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity  {
	
	/**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = false;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_FULLSCREEN; // SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    
    public static final String PREF_FILE = "com.trikotta.anglesensor.prefs";
    private SharedPreferences mPrefs;
            
    private static final int DEFAULT_PERIOD_SEC = 30;  // 10 s
    private PendingIntent pendingIntent;
    
    private SensorManager mSensorManager;    
    private OrientationSensorEventListener mEventListener;
    
    private Context appContext;
    
    
    public void saveFieldsToPrefs(){
    	SharedPreferences.Editor ed = mPrefs.edit();
    	
    	ed.putBoolean("mIsStarted", ((ToggleButton)findViewById(R.id.toggleButton1)).isChecked());
    	
        ed.putString("phoneToCall", ((EditText)findViewById(R.id.phoneToCall)).getText().toString());
        ed.putInt("checkPeriod",(Integer.parseInt(((EditText)findViewById(R.id.checkPeriod)).getText().toString())));
        ed.putInt("minPhoneCallDiff",(Integer.parseInt(((EditText)findViewById(R.id.minPhoneCallDiff)).getText().toString())));
                
        ed.putFloat("angleBottom0",(Float.parseFloat(((EditText)findViewById(R.id.angleBottom0)).getText().toString())));
        ed.putFloat("angleBottom1",(Float.parseFloat(((EditText)findViewById(R.id.angleBottom1)).getText().toString())));
        ed.putFloat("angleBottom2",(Float.parseFloat(((EditText)findViewById(R.id.angleBottom2)).getText().toString())));
        
        ed.putFloat("angleTop0",(Float.parseFloat(((EditText)findViewById(R.id.angleTop0)).getText().toString())));
        ed.putFloat("angleTop1",(Float.parseFloat(((EditText)findViewById(R.id.angleTop1)).getText().toString())));
        ed.putFloat("angleTop2",(Float.parseFloat(((EditText)findViewById(R.id.angleTop2)).getText().toString())));
                
        ed.commit();
    }
    
    public void loadFieldsFromPrefs(){
    	
    	((ToggleButton)findViewById(R.id.toggleButton1)).setChecked(mPrefs.getBoolean("mIsStarted", false));
        
    	((EditText)findViewById(R.id.angleBottom0)).setText(String.valueOf(mPrefs.getFloat("angleBottom0",-400)));
        ((EditText)findViewById(R.id.angleBottom1)).setText(String.valueOf(mPrefs.getFloat("angleBottom1",-400)));
        ((EditText)findViewById(R.id.angleBottom2)).setText(String.valueOf(mPrefs.getFloat("angleBottom2",-400)));
    
    	((EditText)findViewById(R.id.angleTop0)).setText(String.valueOf(mPrefs.getFloat("angleTop0",400)));
        ((EditText)findViewById(R.id.angleTop1)).setText(String.valueOf(mPrefs.getFloat("angleTop1",400)));
        ((EditText)findViewById(R.id.angleTop2)).setText(String.valueOf(mPrefs.getFloat("angleTop2",400)));
            
        ((EditText)findViewById(R.id.phoneToCall)).setText(mPrefs.getString("phoneToCall",getString(R.string.default_phone)));
        ((EditText)findViewById(R.id.checkPeriod)).setText(String.valueOf(mPrefs.getInt("checkPeriod",DEFAULT_PERIOD_SEC)));
        ((EditText)findViewById(R.id.minPhoneCallDiff)).setText(String.valueOf(mPrefs.getInt("minPhoneCallDiff",30)));        
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      // Save UI state changes to the savedInstanceState.
      // This bundle will be passed to onCreate if the process is
      // killed and restarted.
      saveFieldsToPrefs();		      
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      // Restore UI state from the savedInstanceState.
      // This bundle has also been passed to onCreate.
      loadFieldsFromPrefs();
    }
            
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
                
        
        mPrefs = this.getApplicationContext().getSharedPreferences(PREF_FILE, 0);
        loadFieldsFromPrefs();
                
        
        final TextView mTextAngle = (TextView) findViewById(R.id.textAngleView);                        
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);                
        mEventListener = new OrientationSensorEventListener() {
            public void onReactToOrientationChange(){
            	String orientStr="Current Angles: ";  
            	for (float angle: mValuesOrientation) {  
            		orientStr += String.format("%6.2f,	", angle);  
            	} 
            	//Log.d("Activity Sensor ORIENTATION Values",orientStr);
            	mTextAngle.setText(orientStr);
            }                                   
        };        
        //The mEventListener still needs to be registered for updates
        //use registerOrientationSensorForUpdates(register)
        
                
        //////////////////////////////////////        
        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });
        

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.buttonSave).setOnTouchListener(mDelayHideTouchListener);
        //findViewById(R.id.buttonSave).setOnClickListener(onClickListener);
        //findViewById(R.id.buttonTestCall).setOnTouchListener(mDelayHideTouchListener);
        ((Button)findViewById(R.id.buttonTestCall)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(final View v) {
            	         phoneCall();                                
            }            
        });
        
        //Intent alarmIntent=new Intent(context, OnAlarmReceiver.class);
        appContext = this.getApplicationContext();                                    
    	pendingIntent=PendingIntent.getBroadcast(appContext, 0, new Intent(appContext, OnAlarmReceiver.class), 0);               
        
        final ToggleButton buttonOnOff = (ToggleButton)findViewById(R.id.toggleButton1);
        
        buttonOnOff.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        	@Override
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String toastText;
        		if (isChecked) {
                	onTurnOn();
                	toastText = getString(R.string.angle_sensor_alarm_on);
                } else {
                	onTurnOff();
                	toastText = getString(R.string.angle_sensor_alarm_off);
                }
                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();        		
            }
        	
        	public void onTurnOn() {
        		saveFieldsToPrefs();                	
            	//registerOrientationSensorForUpdates(false);
            	
            	//Zero the lastCall timer
            	SharedPreferences.Editor ed = mPrefs.edit();
        		ed.putLong("lastPhoneCall",0);
        		ed.commit();
            	
            	AlarmManager mgr=(AlarmManager)appContext.getSystemService(Context.ALARM_SERVICE);                	
            	long period = mPrefs.getInt("checkPeriod",DEFAULT_PERIOD_SEC)*1000;
            	Log.d("Start Monitor, period:",String.valueOf(period));
            	mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime()+period, period, 
                        pendingIntent);
            }
        	
        	public void onTurnOff() {
        		saveFieldsToPrefs();                	            	
        		AlarmManager mgr=(AlarmManager)appContext.getSystemService(Context.ALARM_SERVICE);            	
            	mgr.cancel(pendingIntent);                  	
            	//registerOrientationSensorForUpdates(true);
            }
        	        	
        });
        
        
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	loadFieldsFromPrefs();
    	registerOrientationSensorForUpdates(true);
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	saveFieldsToPrefs();
    	registerOrientationSensorForUpdates(false);
    }
    
    private void registerOrientationSensorForUpdates(Boolean register){
    	if (register){
    		mSensorManager.registerListener(mEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
                SensorManager.SENSOR_DELAY_NORMAL);
    		mSensorManager.registerListener(mEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 
                SensorManager.SENSOR_DELAY_NORMAL);                                
    		//mSensorManager.registerListener(mEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), 
    		//        SensorManager.SENSOR_DELAY_NORMAL);
    	} else {
        	mSensorManager.unregisterListener(mEventListener);                	            
    	}
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    
    
    private void phoneCall() {
    	Log.d("FullScreenActivity.phoneCall","enter");
    	try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            String phone = mPrefs.getString("phoneToCall", getString(R.string.default_phone));
            String uri = "tel:"+phone;           
            Log.d("Call uri:",uri);
            callIntent.setData(Uri.parse(uri));
            Log.d("FullScreenActivity.phoneCall","startActivity");        	
            startActivity(callIntent);            
        } catch (ActivityNotFoundException e) {
            Log.e("FullScreenActivity.phoneCall", "Call failed", e);
        }    
    }
    
    
}
