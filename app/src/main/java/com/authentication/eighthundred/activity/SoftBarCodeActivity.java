package com.authentication.eighthundred.activity;

import com.authentication.eighthundred.R;
import com.authentication.eighthundred.asynctask.AsyncSoftBarCode;
import com.authentication.eighthundred.asynctask.AsyncSoftBarCode.IBarCodeListener;
import com.authentication.utils.IBroadcastAction;
import com.authentication.utils.ToastUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android_serialport_api.SerialPortManager;
/**
 * barcode
 * @author YJJ
 */
public class SoftBarCodeActivity extends Activity implements OnClickListener,
		IBarCodeListener ,IBroadcastAction{
	private String longstr;
	private AsyncSoftBarCode api;
	private Button scan_button,scaning,stopScan,checkSerialPort;
	private EditText str, line;
	private ProgressDialog progressDialog;
	private int n;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.barcode_activity);
		initView();
	}

	private void initView() {
		scan_button = (Button) findViewById(R.id.scan);
		scan_button.setOnClickListener(this);
		scaning = (Button) findViewById(R.id.scaning);
		scaning.setOnClickListener(this);
		stopScan = (Button) findViewById(R.id.stop_scaning);
		stopScan.setOnClickListener(this);
		checkSerialPort = (Button) findViewById(R.id.read_serial_port);
		checkSerialPort.setOnClickListener(this);
		str = (EditText) findViewById(R.id.et_str);
		line = (EditText) findViewById(R.id.et_line);
		line.setSelection(line.length());
		api = new AsyncSoftBarCode(getApplication(),this);
		longstr = "";
		n = 1;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.scan:
//			showProgressDialog("Scanning...");
			api.startScanner();
			break;
		case R.id.scaning:
//			showProgressDialog("Continuous Scanning...");
			api.ContinuousScanning();
			scan_button.setEnabled(false);
			scaning.setEnabled(false);
			break;
		case R.id.stop_scaning:
			api.CloseScanning();
			scan_button.setEnabled(true);
			scaning.setEnabled(true);
			break;
		case R.id.read_serial_port:
			api.checkSPstatus();
			break;
		default:
			break;
		}
	}

	private void cancleProgressDialog() {
		if (null != progressDialog && progressDialog.isShowing()) {
			progressDialog.cancel();
			progressDialog = null;
		}
	}

	private void showProgressDialog(String message) {
		this.progressDialog = new ProgressDialog(this);
		this.progressDialog.setMessage(message);
		if (!this.progressDialog.isShowing()) {
			this.progressDialog.show();
		}
	}

	@Override
	protected void onDestroy() {
		api.stopScanner();
		api.downGpio();
		super.onDestroy();
	}
	@Override
	protected void onResume() {
		Log.d("jokey", "softBarcodeActivity--->onResume()");
		if(!SerialPortManager.getInstance().isPrintOpen()
				&& !SerialPortManager.getInstance().openSerialPortPrinter()) {
			ToastUtil.showToast(this, R.string.open_serial_fail);
		}
		super.onResume();
	}
	/**
	 * 息屏停止连扫
	 * Stop continues scanning when screen is off
	 */
	@Override
	protected void onPause() {
		api.CloseScanning();
		scan_button.setEnabled(true);
		scaning.setEnabled(true);
		super.onPause();
	}
	@Override
	public void scanCodeFail() {
		cancleProgressDialog();
	}
	/**
	 * scan code success and show the code.
	 */
	@Override
	public void scanCodeSuccess(String data) {
		Log.w("jokey", "Time3==  "+System.currentTimeMillis());
		cancleProgressDialog();
		str.setText("");
		str.setText(data);
		longstr = "No" + n + ":" + data + "\n" + longstr;
		line.setText(longstr);
		n++;
		if(n>=200){
			str.setText("");
			line.setText("");
			n=0;
			longstr="";
		}
	}

}