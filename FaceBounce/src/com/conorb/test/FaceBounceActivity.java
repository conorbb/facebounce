package com.conorb.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class FaceBounceActivity extends Activity {
	
	private FBView fbv;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        Log.v("FB Act","In Oncreate");
        
        fbv = (FBView)findViewById(R.id.FBView);
        
        
    }
    
    @Override 
    protected void onPause(){
    	super.onPause();
    	Log.v("FB Act","In OnPause");
    	finish();
    }

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Log.v("FB Act","In OnRestart");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.v("FB Act","In OnResume");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.v("FB Act","In OnStart");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.v("FB Act","In OnStop");
	}
    
	protected void onDestroy(){
		super.onDestroy();
		Log.v("FB Act","In OnDestroy");
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_MENU) {
	    	Log.v("FB Act", "Menu key pressed!");
	    	fbv.togglePause();
	    	return true;
	    }
	    else{
	    	return super.onKeyUp(keyCode, event);
	    }
	    
	}
//    
    
}