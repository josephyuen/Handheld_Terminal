package com.authentication.eighthundred.asynctask;

import com.authentication.utils.DataUtils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android_serialport_api.IcCardOrderAPI;
import android_serialport_api.IcCardOrderAPI.Result;

public class AsynIcCardOrder extends Handler {
	private static final int SENDOPERCMD = 0x00;
	private static final int RESET = 0x01;
	private static final int CHECK = 0x02;

	private IcCardOrderAPI icCardOrderAPI;
	private Handler workerThreadHandler;

	private String result;

	public AsynIcCardOrder(Looper looper) {
		Log.i("cy", "Enter function AsynIcCardOrder-AsynIcCardOrder()");
		this.workerThreadHandler = createHandler(looper);
		this.icCardOrderAPI = new IcCardOrderAPI();
	}

	protected Handler createHandler(Looper looper) {
		Log.i("cy", "Enter function AsynIcCardOrder-createHandler()");
		return new WorkerHandler(looper);
	}

	public void sendOperCmd(byte[] cmd) {
		Log.i("cy", "Enter function AsynIcCardOrder-sendOperCmd()");
		this.workerThreadHandler.obtainMessage(SENDOPERCMD, cmd).sendToTarget();
	}

	public void reset(byte[] cmd) {
		Log.i("cy", "Enter function AsynIcCardOrder-reset()");
		this.workerThreadHandler.obtainMessage(RESET, cmd).sendToTarget();
	}
	public void checkCardPresent() {
		Log.i("cy", "Enter function AsynIcCardOrder-reset()");
		this.workerThreadHandler.obtainMessage(CHECK).sendToTarget();
	}

	protected int sendCmd(byte[] cmd) {
		Log.i("cy", "Enter function AsynIcCardOrder-sendCmd()");

		byte[] retInfo = AsynIcCardOrder.this.icCardOrderAPI.operateICApdu(cmd, cmd.length);
		if (null == retInfo) {
			return Result.FAILURE;
		}
		result = DataUtils.toHexString(retInfo);

		return Result.SUCCESS;

	}

	protected class WorkerHandler extends Handler {
		public WorkerHandler(Looper looper) {
			super(looper);
			Log.i("cy", "Enter function AsynIcCardOrder-WorkerHandler-WorkerHandler()");
		}

		public void handleMessage(Message msg) {
			Log.i("cy", "Enter function AsynIcCardOrder-WorkerHandler-handleMessage()");

			switch (msg.what) {
			case SENDOPERCMD:
				int ret = AsynIcCardOrder.this.sendCmd((byte[]) msg.obj);
				AsynIcCardOrder.this.obtainMessage(SENDOPERCMD, ret, -1, result).sendToTarget();
				break;
			case RESET:
				boolean result = icCardOrderAPI.reset((byte[]) msg.obj);
				AsynIcCardOrder.this.obtainMessage(RESET, result).sendToTarget();
				break;
			case CHECK:
				boolean res = icCardOrderAPI.checkCardPresent();
				AsynIcCardOrder.this.obtainMessage(CHECK, res).sendToTarget();
				break;

			default:
				break;

			}

		}

	}

	public interface OnSendOperCmdListener {
		void onSendOperCmdSuccess(String result);

		void onSendOperCmdFail(int resultCode);
	}

	public interface OnResetListener {
		void callback(boolean result);
	}
	public interface OnCheckListener {
		void callback(boolean result);
	}
	
	private OnCheckListener OnCheckListener;
	private OnResetListener onResetListener;
	private OnSendOperCmdListener onSendOperCmdListener;

	public void setOnCheckListener(OnCheckListener onCheckListener) {
		Log.i("cy", "Enter function AsynIcCardOrder-setOnSendOperCmdListener()");
		this.OnCheckListener = onCheckListener;
	}
	public void setOnResetListener(OnResetListener onResetListener) {
		Log.i("cy", "Enter function AsynIcCardOrder-setOnSendOperCmdListener()");
		this.onResetListener = onResetListener;
	}

	public void setOnSendOperCmdListener(OnSendOperCmdListener onSendOperCmdListener) {
		Log.i("cy", "Enter function AsynIcCardOrder-setOnSendOperCmdListener()");
		this.onSendOperCmdListener = onSendOperCmdListener;
	}

	public void handleMessage(Message msg) {
		Log.i("cy", "Enter function AsynIcCardOrder-handleMessage()");

		super.handleMessage(msg);
		switch (msg.what) {
		case SENDOPERCMD:
			if (null != this.onSendOperCmdListener) {
				if (0 != msg.arg1) {
					if (null != msg.obj) {
						this.onSendOperCmdListener.onSendOperCmdSuccess((String) msg.obj);
					}
				} else {
					this.onSendOperCmdListener.onSendOperCmdFail(msg.arg1);
				}
			}
			break;
		case RESET:
			onResetListener.callback((boolean) msg.obj);
			break;
		case CHECK:
			OnCheckListener.callback((boolean) msg.obj);
			break;

		default:
			break;
		}
	}
}