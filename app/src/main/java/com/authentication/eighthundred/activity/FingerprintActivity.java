package com.authentication.eighthundred.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.authentication.eighthundred.R;
import com.authentication.eighthundred.asynctask.AsyncFingerprint;
import com.authentication.eighthundred.asynctask.AsyncFingerprint.OnCalibrationListener;
import com.authentication.eighthundred.asynctask.AsyncFingerprint.OnEmptyListener;
import com.authentication.utils.ToastUtil;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android_serialport_api.FingerprintAPI;
import android_serialport_api.SerialPortManager;

public class FingerprintActivity extends BaseActivity implements OnClickListener {
	/**
	 * 注册指纹 Regist finger print
	 */
	private Button btnRegister;
	/**
	 * 验证指纹 Verify finger print
	 */
	private Button btnValidate;
	/**
	 * 连续注册指纹 Continues regist finger print
	 */
	private Button btnRegister2;
	/**
	 * 连续验证指纹 Continues verify finger print
	 */
	private Button btnValidate2;
	/**
	 * 清空指纹 Clear finger print
	 */
	private Button btnClear;
	/**
	 * 校准 Calibration
	 */
	private Button btnCalibration;
	private Button btnBack;
	/**
	 * finger print image
	 */
	private ImageView imgFinger;

	private byte[] model;
	private Spinner spinner;
	private ArrayAdapter<String> adapter;
	private String[] m;
	private MyApplication application;
	private ProgressDialog progressDialog;
	private AsyncFingerprint asyncFingerprint;
	private Timer mtimer;

	private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Log.i("cy", "Enter function FingerprintActivity-Handler-handleMessage()");
			super.handleMessage(msg);

			switch (msg.what) {

			case AsyncFingerprint.SHOW_PROGRESSDIALOG:
				cancleProgressDialog();
				showProgressDialog((Integer) msg.obj);
				break;

			case AsyncFingerprint.SHOW_FINGER_IMAGE:
				showFingerImage(msg.arg1, (byte[]) msg.obj);
				break;

			case AsyncFingerprint.SHOW_FINGER_MODEL:
				FingerprintActivity.this.model = (byte[]) msg.obj;
				if (null != FingerprintActivity.this.model) {
					Log.i("cy", "The length of Finger model is " + FingerprintActivity.this.model.length);
				}
				cancleProgressDialog();
				ToastUtil.showToast(FingerprintActivity.this, "pageId = " + msg.arg1 + "  store = " + msg.arg2);
				break;

			case AsyncFingerprint.REGISTER_SUCCESS:
				cancleProgressDialog();
				if (null != msg.obj) {
					Integer id = (Integer) msg.obj;
					ToastUtil.showToast(FingerprintActivity.this,
							getString(R.string.register_success) + "  pageId = " + id);
				} else {
					ToastUtil.showToast(FingerprintActivity.this, R.string.register_success);
				}
				break;

			case AsyncFingerprint.REGISTER_FAIL:
				cancleProgressDialog();
				ToastUtil.showToast(FingerprintActivity.this, R.string.register_fail);
				break;

			case AsyncFingerprint.VALIDATE_RESULT1:
				cancleProgressDialog();
				showValidateResult((Boolean) msg.obj);
				break;

			case AsyncFingerprint.VALIDATE_RESULT2:
				cancleProgressDialog();
				Integer ret = (Integer) msg.obj;
				if (-1 != ret) {
					ToastUtil.showToast(FingerprintActivity.this,
							getString(R.string.verifying_through) + "  pageId=" + ret);
				} else {
					showValidateResult(false);
				}
				break;

			case AsyncFingerprint.UP_IMAGE_RESULT:
				cancleProgressDialog();
				ToastUtil.showToast(FingerprintActivity.this, (Integer) msg.obj);
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fingerprint);
		initView();
		initData();
		showProgressDialog(R.string.isopenGpio);
		mtimer.schedule(new TimerTask() {

			@Override
			public void run() {
				cancleProgressDialog();
			}
		}, 1500);
	}

	@Override
	protected void onResume() {
		if (!SerialPortManager.getInstance().isPrintOpen()
				&& !SerialPortManager.getInstance().openSerialPortPrinter()) {
			ToastUtil.showToast(FingerprintActivity.this, R.string.open_serial_fail);
		}
		super.onResume();
	}

	private void initView() {
		spinner = (Spinner) findViewById(R.id.spinner);
		btnRegister = (Button) findViewById(R.id.Register_Finger);
		btnRegister.setOnClickListener(this);

		btnValidate = (Button) findViewById(R.id.Validate_Finger);
		btnValidate.setOnClickListener(this);

		btnRegister2 = (Button) findViewById(R.id.Register2_Finger);
		btnRegister2.setOnClickListener(this);

		btnValidate2 = (Button) findViewById(R.id.Validate2_Finger);
		btnValidate2.setOnClickListener(this);

		btnClear = (Button) findViewById(R.id.ClearFlash_Finger);
		btnClear.setOnClickListener(this);

		btnCalibration = (Button) findViewById(R.id.Calibration_Finger);
		btnCalibration.setOnClickListener(this);

		btnBack = (Button) findViewById(R.id.BackReg_Finger);
		btnBack.setOnClickListener(this);

		imgFinger = (ImageView) findViewById(R.id.Image_Finger);

		mtimer = new Timer();
	}

	private void initData() {
		application = (MyApplication) this.getApplicationContext();
		asyncFingerprint = new AsyncFingerprint(application.getHandlerThread().getLooper(), handler);

		asyncFingerprint.setFingerprintType(FingerprintAPI.BIG_FINGERPRINT_SIZE);

		asyncFingerprint.setOnEmptyListener(new OnEmptyListener() {

			@Override
			public void onEmptySuccess() {
				ToastUtil.showToast(FingerprintActivity.this, R.string.clear_flash_success);
			}

			@Override
			public void onEmptyFail() {
				ToastUtil.showToast(FingerprintActivity.this, R.string.clear_flash_fail);
			}
		});

		asyncFingerprint.setOnCalibrationListener(new OnCalibrationListener() {

			@Override
			public void onCalibrationSuccess() {
				ToastUtil.showToast(FingerprintActivity.this, R.string.calibration_success);
			}

			@Override
			public void onCalibrationFail() {
				ToastUtil.showToast(FingerprintActivity.this, R.string.calibration_fail);
			}
		});
		m = this.getResources().getStringArray(R.array.fingerprint_size);

		// 将可选内容与ArrayAdapter连接起来
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, m);

		// 设置下拉列表的风格
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// 将adapter 添加到spinner中
		spinner.setAdapter(adapter);

		// 添加事件Spinner事件监听
		spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
	}
	// 使用数组形式操作
		class SpinnerSelectedListener implements OnItemSelectedListener {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Log.i("whw", "position=" + position);
				switch (position) {
				case 0:
					asyncFingerprint
							.setFingerprintType(FingerprintAPI.SMALL_FINGERPRINT_SIZE);
					break;
				case 1:
					asyncFingerprint
							.setFingerprintType(FingerprintAPI.BIG_FINGERPRINT_SIZE);
					break;
				default:
					break;
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.Register_Finger:// 注册指纹
			asyncFingerprint.setStop(false);
			asyncFingerprint.register();
			break;
		case R.id.Validate_Finger:// 验证指纹
			if (model != null) {
				asyncFingerprint.validate(model);
			} else {
				ToastUtil.showToast(FingerprintActivity.this, R.string.first_register);
			}
			break;
		case R.id.Register2_Finger:// 连续注册指纹
			asyncFingerprint.setStop(false);
			asyncFingerprint.register2();
			break;
		case R.id.Validate2_Finger:// 连续验证指纹
			asyncFingerprint.validate2();
			break;
		case R.id.Calibration_Finger:// 校准
			asyncFingerprint.PS_Calibration();
			break;
		case R.id.ClearFlash_Finger:// 清空指纹库
			asyncFingerprint.PS_Empty();
			break;
		case R.id.BackReg_Finger:// 退出
			finish();
			break;
		default:
			break;
		}
	}

	private void showValidateResult(boolean matchResult) {
		if (matchResult) {
			ToastUtil.showToast(FingerprintActivity.this, R.string.verifying_through);
		} else {
			ToastUtil.showToast(FingerprintActivity.this, R.string.verifying_fail);
		}
	}

	@SuppressWarnings("deprecation")
	private void showFingerImage(int fingerType, byte[] data) {
		Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
		if (image.getHeight() != 360) {
			// ToastUtil.showToast(FingerprintActivity.this,"WIDTH=" +
			// image.getWidth() + " HEIGHT=" + image.getHeight());
		}
		imgFinger.setBackgroundDrawable(new BitmapDrawable(image));
		writeToFile(data);
	}

	private void writeToFile(byte[] data) {
		Log.i("cy", "Enter function FingerprintActivity-writeToFile()");
		String dir = rootPath + File.separator + "fingerprint_image";
		File dirPath = new File(dir);
		if (!dirPath.exists()) {
			dirPath.mkdir();
		}

		String filePath = dir + "/" + System.currentTimeMillis() + ".bmp";
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}

		FileOutputStream fos = null;
		try {
			file.createNewFile();
			fos = new FileOutputStream(file);
			fos.write(data);
			fos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != fos) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void showProgressDialog(int resId) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getResources().getString(resId)); // 通过ID获取对应的值
		progressDialog.setCanceledOnTouchOutside(false);

		progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (KeyEvent.KEYCODE_BACK == keyCode) {
					asyncFingerprint.setStop(true);
				}
				return false;
			}
		});
		progressDialog.show();
	}

	private void cancleProgressDialog() {
		if (null != progressDialog && progressDialog.isShowing()) {
			progressDialog.cancel();
			progressDialog = null;
		}
	}

	@Override
	protected void onDestroy() {
		cancleProgressDialog();
		asyncFingerprint.setStop(true);
		handler.removeCallbacksAndMessages(null);
		SerialPortManager.getInstance().closeSerialPort(2);
		super.onDestroy();
	}
	@Override
	protected void onPause() {
		super.onPause();
		cancleProgressDialog();
		asyncFingerprint.setStop(true);
		Log.i("whw", "onPause");
	}
}