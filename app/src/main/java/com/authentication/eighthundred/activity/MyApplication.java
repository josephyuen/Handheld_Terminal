package com.authentication.eighthundred.activity;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.HandlerThread;
import android.util.Log;
import android_serialport_api.SerialPortManager;

public class MyApplication extends Application {
	private String rootPath;
	
	private SerialPortManager communicateManager=null;
	

	private HandlerThread handlerThread;
	public String getRootPath() {
		return rootPath;
	}

	public HandlerThread getHandlerThread() {
		return handlerThread;
	}

	public SerialPortManager getSerial(){
		return communicateManager;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		handlerThread = new HandlerThread("handlerThread",android.os.Process.THREAD_PRIORITY_BACKGROUND);
//		handlerThread = new HandlerThread("handlerThread");
		handlerThread.start();
		setRootPath();
	}

	private void setRootPath() {
		PackageManager manager = this.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			rootPath = info.applicationInfo.dataDir;
			Log.i("rootPath", "################rootPath=" + rootPath);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}