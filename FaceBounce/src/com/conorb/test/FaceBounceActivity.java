package com.conorb.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class FaceBounceActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.v("FB Act","In Oncreate");
    }
}