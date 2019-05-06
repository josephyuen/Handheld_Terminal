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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android_serialport_api.PsamCardStm32API;
import android_serialport_api.PsamCardStm32API.OnPsamCardListener;
import android_serialport_api.SerialPortManager;

public class PsamCardStm32Activity extends Activity implements OnClickListener, OnPsamCardListener {
	private RadioGroup mRadioGroup;
	private RadioButton rbPsam0;
	private RadioButton rbPsam1;
	private EditText etShow;
	private Button btnReset, btnGetRandom, btnPowerOff;
	private PsamCardStm32API api;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_psam_card);
		initViews();
		mHandler = new Handler();
	}

	private void initViews() {
		mRadioGroup = (RadioGroup) findViewById(R.id.psam_rb);
		rbPsam0 = (RadioButton) findViewById(R.id.psam0);
		rbPsam1 = (RadioButton) findViewById(R.id.psam1);
		etShow = (EditText) findViewById(R.id.psam_etshow);
		btnReset = (Button) findViewById(R.id.psam_btreset);
		btnGetRandom = (Button) findViewById(R.id.psam_getrandom);
		btnPowerOff = (Button) findViewById(R.id.psam_clear);
		btnReset.setOnClickListener(this);
		btnGetRandom.setOnClickListener(this);
		btnPowerOff.setOnClickListener(this);
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.psam0:
					Log.d("jokey", "activity--->psam0");
					api.psam0();
					setEnable(false);
					break;
				case R.id.psam1:
					Log.d("jokey", "psam1");
					api.psam1();
					setEnable(false);
					break;
				}
			}
		});
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
	protected void onResume() {
		if (!SerialPortManager.getInstance().isOpen() && !SerialPortManager.getInstance().openSerialPortPrinter()) {
			ToastUtil.showToast(this, R.string.open_serial_fail);
		}
		api = new PsamCardStm32API(this);
		if (rbPsam0.isChecked())
			api.psam0();
		else
			api.psam1();

		super.onResume();
	}

	@Override
	protected void onDestroy() {
		api.powerOff();
		SerialPortManager.getInstance().closeSerialPort(2);
		super.onDestroy();
	}

	@Override
	public void callback(final String str, final String data) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				switch (str) {
				case PsamCardStm32API.RESET_OK:
					etShow.setText(
							getResources().getString(R.string.psam_reset_success) + data + "\n" + etShow.getText());
					break;
				case PsamCardStm32API.RESET_FAIL:
					etShow.setText(getResources().getString(R.string.psam_reset_fail) + data + "\n" + etShow.getText());
					break;
				case PsamCardStm32API.GET_RANDOM_Fail:
					etShow.setText(getResources().getString(R.string.psam_get_random_number_fail) + data + "\n"
							+ etShow.getText());
					break;
				case PsamCardStm32API.GET_RANDOM_OK:
					etShow.setText(
							getResources().getString(R.string.psam_random_number_is) + data + "\n" + etShow.getText());
					break;
				case PsamCardStm32API.SELECT_PSAM_OK:
					etShow.setText(data + "\n" + etShow.getText());
					break;
				case PsamCardStm32API.SELECT_PSAM_FAIL:
					etShow.setText(data + "\n" + etShow.getText());
					break;
				case PsamCardStm32API.TEST:
					etShow.setText(data + "\n" + etShow.getText());
					break;
				}
				setEnable(true);
			}
		});
	}

	private void setEnable(boolean enable) {
		btnReset.setEnabled(enable);
		btnGetRandom.setEnabled(enable);
		rbPsam1.setEnabled(enable);
		rbPsam0.setEnabled(enable);
	}
}