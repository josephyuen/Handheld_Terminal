package com.authentication.eighthundred.asynctask;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android_serialport_api.SFZAPI;
import android_serialport_api.SFZAPI.Result;
import android_serialport_api.SFZAPI.UIDResult;

public class AsyncParseSFZ {
	public static final int READ_CARD_ID = 999;
	public static final int READ_SFZ = 1000;
	public static final int READ_MODULE = 2000;
	public static final int READ_UID = 3000;
	public static final int FIND_CARD_SUCCESS = 1001;
	public static final int FIND_CARD_FAIL = 1002;
	
	public static final int READ_UID_SUCCESS = 1;
	public static final int READ_UID_FAIL = 2;
	
	public static final int DATA_SIZE = 1295;

	private SFZAPI parseAPI;

	private Handler mWorkerThreadHandler;
	private Handler mHandler;

	public AsyncParseSFZ(Handler mHandler,Looper looper,Context context) {
		parseAPI = new SFZAPI(context);
		mWorkerThreadHandler = createHandler(looper);
		this.mHandler = mHandler;
	}

	protected Handler createHandler(Looper looper) {
		return new WorkerHandler(looper);
	}

	protected class WorkerHandler extends Handler {
		public WorkerHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case READ_SFZ:
				Result resultSFZ = parseAPI.readSFZ();
				if (resultSFZ.confirmationCode == Result.SUCCESS) {
					mHandler.obtainMessage(FIND_CARD_SUCCESS, resultSFZ.resultInfo).sendToTarget();
				} else {
					mHandler.obtainMessage(FIND_CARD_FAIL, resultSFZ.confirmationCode, -1).sendToTarget();
				}
				break;
			case READ_UID:
				UIDResult result = parseAPI.getUID();
				if(result.code == 1){
					mHandler.obtainMessage(READ_UID_SUCCESS, result.result).sendToTarget();
				}else{
					mHandler.obtainMessage(READ_UID_FAIL,result.code).sendToTarget();
				}
				break;
			}
		}
	}

	public void readSFZ() {
		mWorkerThreadHandler.obtainMessage(READ_SFZ).sendToTarget();
	}

	public void readModuleNum() {
		mWorkerThreadHandler.obtainMessage(READ_MODULE).sendToTarget();
	}

	public void readCardID() {
		mWorkerThreadHandler.obtainMessage(READ_CARD_ID).sendToTarget();
	}
	public void readUID() {
		mWorkerThreadHandler.obtainMessage(READ_UID).sendToTarget();
	}

}