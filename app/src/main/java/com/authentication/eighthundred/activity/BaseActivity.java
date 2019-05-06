package com.authentication.eighthundred.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.HandlerThread;

public class BaseActivity extends Activity {
	protected MyApplication application;
	protected HandlerThread handlerThread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = (MyApplication) getApplicationContext();
	}
	@Override
	protected void onResume() {
		super.onResume();
		handlerThread = application.getHandlerThread();
	}
	@Override
	protected void onPause() {
		super.onPause();
		handlerThread = null;
	}
}