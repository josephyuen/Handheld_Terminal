package com.authentication.eighthundred.activity;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android_serialport_api.GetVersion;
import android_serialport_api.SerialPortManager;
import com.authentication.eighthundred.R;

public class GetVersionActivity extends Activity implements OnClickListener {
	private Button mBtApk, mBtStm, mBt3255;
	private TextView mTvApk, mTvStm, mTv32550;
	private GetVersion api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_version);

		init();
	}

	private void init() {
		api = new GetVersion();
		mBtApk = (Button) findViewById(R.id.bt_getApk);
		mBtApk.setOnClickListener(this);

		mBtStm = (Button) findViewById(R.id.bt_getStmVersion);
		mBtStm.setOnClickListener(this);

		mBt3255 = (Button) findViewById(R.id.bt_get3255Version);
		mBt3255.setOnClickListener(this);

		mTvApk = (TextView) findViewById(R.id.tv_apkVersion);
		mTvStm = (TextView) findViewById(R.id.tv_stmVersion);
		mTv32550 = (TextView) findViewById(R.id.tv_32550Version);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_getApk:
			mTvApk.setText(api.getApkVersion(getApplicationContext()));
			break;
		case R.id.bt_getStmVersion:
			SerialPortManager.getInstance().openSerialPortPrinter();
			mTvStm.setText(api.getStm32Version());
			SerialPortManager.getInstance().closeSerialPort(2);
			break;
		case R.id.bt_get3255Version:
			SerialPortManager.getInstance().openSerialPort();
			try {
				mTv32550.setText(api.get32550Version());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			SerialPortManager.getInstance().closeSerialPort(1);
			break;
		default:
			break;
		}
	}
}