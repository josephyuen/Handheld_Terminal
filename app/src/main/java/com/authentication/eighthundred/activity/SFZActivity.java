package com.authentication.eighthundred.activity;

import com.authentication.eighthundred.R;
import com.authentication.eighthundred.asynctask.AsyncParseSFZ;
import com.authentication.utils.DataUtils;
import com.authentication.utils.ToastUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android_serialport_api.SFZAPI;
import android_serialport_api.SFZAPI.People;
import android_serialport_api.SerialPortManager;

public class SFZActivity extends Activity implements OnClickListener {

	private TextView sfz_name,sfz_sex,sfz_nation,sfz_year,sfz_mouth,sfz_day,sfz_address,sfz_id;
	private ImageView sfz_photo;
	private TextView resultInfo;
	private Button read_button,read_continue,read_stop,read_clear,mBtGetUID;

	private MediaPlayer mediaPlayer = null;
	private MyApplication application;
	private AsyncParseSFZ asyncParseSFZ;
	private int readTime = 0;
	private int readFailTime = 0;
	private int readTimeout = 0;
	private int readSuccessTime = 0;
	private boolean isDestroy = false;
	private boolean isContinue = false;
	private ProgressDialog progressDialog;

	private Handler mhandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AsyncParseSFZ.READ_UID_SUCCESS:
				byte[] uid = (byte[]) msg.obj;
				resultInfo.setText("uid: "+ DataUtils.toHexString(uid));
				break;
			case AsyncParseSFZ.READ_UID_FAIL:
				int code = (int) msg.obj;
				resultInfo.setText(getResources().getString(R.string.sfz_error_code) + code);
				break;
			case AsyncParseSFZ.FIND_CARD_SUCCESS:
				if(!isDestroy){
					cancleProgressDialog();
					updateInfo((People)msg.obj);
					readSuccessTime++;
					continueRead(isContinue);
				}
				break;
			case AsyncParseSFZ.FIND_CARD_FAIL:
				cancleProgressDialog();
				int confirmationCode = msg.arg1;
				if (confirmationCode == SFZAPI.Result.FIND_FAIL&&!isDestroy) {
					ToastUtil.showToast(SFZActivity.this, getResources().getString(R.string.sfz_unfound_have_data));
				} else if (confirmationCode == SFZAPI.Result.TIME_OUT&&!isDestroy) {
					ToastUtil.showToast(SFZActivity.this, getResources().getString(R.string.sfz_time_out));
					readTimeout++;
				} else if (confirmationCode == SFZAPI.Result.OTHER_EXCEPTION&&!isDestroy) {
					ToastUtil.showToast(SFZActivity.this, getResources().getString(R.string.sfz_other_error));
				}
				if(!isDestroy){
					readFailTime++;
					continueRead(isContinue);
				}
				break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_sfz);

		init();
	}
	
	@Override
	protected void onResume() {
		Log.d("jokey", "SFZ--->onResume()");
		isDestroy = false;
		try {
		SerialPortManager.getInstance().openSerialPortIDCard();
		} catch (Exception e) {
			showProgressDialog(e+"");
			e.printStackTrace();
		}
		super.onResume();
	}
	
	private void init() {
		sfz_name = (TextView) findViewById(R.id.sfz_name);
		sfz_sex = (TextView) findViewById(R.id.sfz_sex);
		sfz_nation = (TextView) findViewById(R.id.sfz_nation);
		sfz_year = (TextView) findViewById(R.id.sfz_year);
		sfz_mouth = (TextView) findViewById(R.id.sfz_mouth);
		sfz_day = (TextView) findViewById(R.id.sfz_day);
		sfz_address = (TextView) findViewById(R.id.sfz_address);
		sfz_id = (TextView) findViewById(R.id.sfz_id);
		sfz_photo = (ImageView) findViewById(R.id.sfz_photo);
		resultInfo = ((TextView) findViewById(R.id.resultInfo));
		
		read_button = (Button) findViewById(R.id.read_sfz);
		read_button.setOnClickListener(this);
		read_continue = (Button) findViewById(R.id.read_continue);
		read_continue.setOnClickListener(this);
		read_stop = (Button) findViewById(R.id.read_stop);
		read_stop.setOnClickListener(this);
		read_clear = (Button) findViewById(R.id.read_clear);
		read_clear.setOnClickListener(this);
		mBtGetUID = (Button) findViewById(R.id.bt_getUID);
		mBtGetUID.setOnClickListener(this);
		
		mediaPlayer = MediaPlayer.create(this, R.raw.ok);
		application = (MyApplication) this.getApplicationContext();
		asyncParseSFZ = new AsyncParseSFZ(mhandler,application.getHandlerThread().getLooper(), this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.read_sfz:
			clear();
			isContinue = false;
			resultInfo.setText("");
			showProgressDialog(getResources().getString(R.string.sfz_now_reading));
			asyncParseSFZ.readSFZ();
			break;
		case R.id.read_continue:
			clear();
			clearCount();
			read_continue.setEnabled(false);
			read_button.setEnabled(false);
			isContinue = true;
			showProgressDialog(getResources().getString(R.string.sfz_now_reading));
			asyncParseSFZ.readSFZ();
			break;
		case R.id.read_stop:
			isContinue = false;
			read_button.setEnabled(true);
			read_continue.setEnabled(true);
			break;
		case R.id.read_clear:
			clearCount();
			clear();
			
			break;
		case R.id.bt_getUID:
			asyncParseSFZ.readUID();
			break;
		}
	}
	
	private void continueRead(boolean isContinueRead) {
		if(isContinueRead){
			readTime++;
			String result = getResources().getString(R.string.sfz_count_total) 
							+ readTime + getResources().getString(R.string.sfz_count_success)
							+ readSuccessTime + getResources().getString(R.string.sfz_count_fail)
							+ readFailTime + "\n " +getResources().getString(R.string.sfz_count_time_out)+ readTimeout;
			Log.i("jokey", "result=" + result);
			resultInfo.setText(result);
			new Thread(){
				Runnable runable = new Runnable() {
					@Override
					public void run() {
						if(!isDestroy){
							showProgressDialog(getResources().getString(R.string.sfz_now_reading));
						}
					}
				};
				@Override
				public void run() {
					try {
						Thread.sleep(1500);
						if(isContinue){
							runOnUiThread(runable);
							asyncParseSFZ.readSFZ();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	private void updateInfo(People people) {
		mediaPlayer.release();
		mediaPlayer = null;
		mediaPlayer = MediaPlayer.create(this, R.raw.ok);
		mediaPlayer.start();

		sfz_address.setText(people.getPeopleAddress());
		sfz_day.setText(people.getPeopleBirthday().substring(6));
		sfz_id.setText(people.getPeopleIDCode());
		sfz_mouth.setText(people.getPeopleBirthday().substring(4, 6));
		sfz_name.setText(people.getPeopleName());
		sfz_nation.setText(people.getPeopleNation());
		sfz_sex.setText(people.getPeopleSex());
		sfz_year.setText(people.getPeopleBirthday().substring(0, 4));
		if (people.getPhoto() != null) {
			Bitmap photo = BitmapFactory.decodeByteArray(people.getPhoto(), 0,
					people.getPhoto().length);
			Log.d("jokey", "bitmapSize:"+photo.getByteCount()+people.getPhoto().length);
			sfz_photo.setImageBitmap(photo);
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

	private void clear() {
		sfz_address.setText("");
		sfz_day.setText("");
		sfz_id.setText("");
		sfz_mouth.setText("");
		sfz_name.setText("");
		sfz_nation.setText("");
		sfz_sex.setText("");
		sfz_year.setText("");
		sfz_photo.setBackgroundColor(0);
		sfz_photo.setImageBitmap(null);
		resultInfo.setText("");
	}
	private void clearCount() {
		readFailTime = 0;
		readSuccessTime = 0;
		readTime = 0;
		readTimeout = 0;
	}
	/**
	 * 息屏停止连读
	 * Stop continues Read ID card when screen is off
	 */
	@Override
	protected void onStop() {
		Log.d("jokey", "SFZ--->onPause");
		cancleProgressDialog();
		isContinue = false;
		isDestroy = true;
		read_button.setEnabled(true);
		read_continue.setEnabled(true);
		SerialPortManager.getInstance().closeSerialPort(3);
		super.onPause();
	}

}