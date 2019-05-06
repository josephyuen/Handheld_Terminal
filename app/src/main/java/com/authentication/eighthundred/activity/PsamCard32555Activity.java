package com.authentication.eighthundred.activity;

import com.authentication.eighthundred.R;
import com.authentication.utils.ToastUtil;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android_serialport_api.PsamCard32555API;
import android_serialport_api.PsamCardStm32API.OnPsamCardListener;
import android_serialport_api.SerialPortManager;

public class PsamCard32555Activity extends Activity implements OnClickListener,OnPsamCardListener{
	private Button btnReset, btnGetRandom, btnPowerOff;
	private EditText etShow;
	private Handler mHandler;
	private PsamCard32555API api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_psam_card32555);
		
		initViews();
		mHandler = new Handler();
	}

	private void initViews() {
		etShow = (EditText) findViewById(R.id.psam_etshow);
		btnReset = (Button) findViewById(R.id.psam_btreset);
		btnGetRandom = (Button) findViewById(R.id.psam_getrandom);
		btnPowerOff = (Button) findViewById(R.id.psam_clear);
		btnReset.setOnClickListener(this);
		btnGetRandom.setOnClickListener(this);
		btnPowerOff.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.psam_btreset:
			Log.d("jokey", "activity--->reset");
			api.reset();
			setEnable(false);
			break;
		case R.id.psam_getrandom:
			Log.d("jokey", "activity--->random");
			api.getRandom();
			setEnable(false);
			break;
		case R.id.psam_clear:
			Log.d("jokey", "activity--->off");
			etShow.setText("");
			break;
		}
	}
	
	@Override
	public void callback(final String str, final String data) {
		mHandler.post(new Runnable() {
			public void run() {
				switch (str) {
				case PsamCard32555API.RESET_OK:
					etShow.setText(
							getResources().getString(R.string.psam_reset_success) + data + "\n" + etShow.getText());
					break;
				case PsamCard32555API.RESET_FAIL:
					etShow.setText(getResources().getString(R.string.psam_reset_fail) + data + "\n" + etShow.getText());
					break;
				case PsamCard32555API.GET_RANDOM_Fail:
					etShow.setText(getResources().getString(R.string.psam_get_random_number_fail) + data + "\n"
							+ etShow.getText());
					break;
				case PsamCard32555API.GET_RANDOM_OK:
					etShow.setText(
							getResources().getString(R.string.psam_random_number_is) + data + "\n" + etShow.getText());
					break;
				}
				setEnable(true);
			}
		});
	}
	
	@Override
	protected void onResume() {
		if (!SerialPortManager.getInstance().isOpen() && !SerialPortManager.getInstance().openSerialPort()) {
			ToastUtil.showToast(this, R.string.open_serial_fail);
		}
		api=new PsamCard32555API(this);
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		SerialPortManager.getInstance().closeSerialPort(1);
		super.onDestroy();
	}
	
	private void setEnable(boolean enable) {
		btnReset.setEnabled(enable);
		btnGetRandom.setEnabled(enable);
	}
}