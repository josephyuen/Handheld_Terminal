package com.authentication.eighthundred.asynctask;

import com.authentication.utils.IBroadcastAction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
/**
 * barcode
 * @author YJJ
 */
public class AsyncSoftBarCode {
	private Handler mhandler;
	private IBarCodeListener listener;
	private Context mContext;
	private SoftBarCodeReceiver receiver;
	private boolean isLoop = false;
	public AsyncSoftBarCode(Context context,IBarCodeListener listener) {
		Log.w("jokey", "Time5==  "+System.currentTimeMillis());
		mhandler = new Handler();
		this.listener = listener;
		this.mContext = context;
		receiver = new SoftBarCodeReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(IBroadcastAction.ACTION_DECODE_DATA);
		filter.setPriority(Integer.MAX_VALUE);
		filter.addAction(IBroadcastAction.TIME_OUT);
		mContext.registerReceiver(receiver, filter);
	}
	private class SoftBarCodeReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(IBroadcastAction.ACTION_DECODE_DATA.equals(action)){
				String data = intent.getStringExtra(IBroadcastAction.INTENT_BARCODE_DATA);
				Log.w("jokey", "Time2==  "+System.currentTimeMillis());
				listener.scanCodeSuccess(data);
			}else if(IBroadcastAction.TIME_OUT.equals(action)){
				listener.scanCodeFail();
			}
		}
		
	}
	public void checkSPstatus() {
		Intent intent = new Intent(IBroadcastAction.ACTION_CHECK_SERIAL_PORT,null);
		mContext.sendOrderedBroadcast(intent,null);
	}
	/**
	 * start scan
	 */
	public void startScanner() {
		Intent intent = new Intent(IBroadcastAction.ACTION_START_DECODE,null);
		mContext.sendOrderedBroadcast(intent,null);
	}
	/**
	 * stop scan
	 */
	public void stopScanner() {
		stopThread();
		Intent intent = new Intent(IBroadcastAction.ACTION_STOP_DECODE,null);
		mContext.sendOrderedBroadcast(intent,null);
	}
	/**
	 * Continuous scanning
	 */
	public void ContinuousScanning() {
		startThread();
	}
	/**
	 * close Continuous scanning
	 */
	public void CloseScanning() {
		stopThread();
		Intent intent = new Intent(IBroadcastAction.ACTION_STOP_DECODE,null);
		intent.putExtra("isLoop", isLoop);
		mContext.sendOrderedBroadcast(intent,null);
	}
	
	private InnerThread continuesThread;
	
	private void startThread() {
		if (continuesThread == null) {
			continuesThread = new InnerThread();
			isLoop = true;
			continuesThread.start();
		}
	}
	
	private void stopThread() {
		if (continuesThread != null) {
			isLoop = false;
			continuesThread = null;
		}
	}
	private class InnerThread extends Thread{
		@Override
		public void run() {
			while(isLoop){
				Intent intent = new Intent(IBroadcastAction.ACTION_START_DECODE,null);
				intent.putExtra("isLoop", isLoop);
				mContext.sendOrderedBroadcast(intent,null);
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * power on stm32
	 */
	public void upGpio() {
		Intent intent = new Intent(IBroadcastAction.ACTION_OPEN_SCAN);
		mContext.sendBroadcast(intent);
	}
	/**
	 * power off
	 */
	public void downGpio() {
		stopThread();
		Intent intent = new Intent(IBroadcastAction.ACTION_CLOSE_SCAN);
		mContext.sendBroadcast(intent);
	}	

	public interface IBarCodeListener {
		void scanCodeFail();

		void scanCodeSuccess(String data);
	}
}