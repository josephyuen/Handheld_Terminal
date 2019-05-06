package com.authentication.eighthundred.activity;

import com.authentication.eighthundred.R;
import com.authentication.utils.DataUtils;
import com.authentication.utils.ToastUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android_serialport_api.M1CardAPI;
import android_serialport_api.M1CardAPI.OnM1CardListener;

public class M1Activity extends Activity implements OnClickListener, OnM1CardListener {
	private Button mBtGetCard, mBtWriteData, mBtReadData;
	private EditText mEtCardPwd, mEtCardID, mEtSectorNum, mEtBlockNum, mEtWriteData, mEtReadData, mEtCardType;
	private Spinner spinner;
	private ArrayAdapter<String> adapter;
	private String[] m;
	private M1CardAPI api;
	private ProgressDialog progressDialog;
	private static byte cardType = M1CardAPI.TYPE_A;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_m1);

		init();
		initData();
	}

	private void init() {
		api = new M1CardAPI(getApplicationContext(), this);

		mBtGetCard = (Button) findViewById(R.id.btn_getCardNum);
		mBtGetCard.setOnClickListener(this);

		mBtWriteData = (Button) findViewById(R.id.btn_write);
		mBtWriteData.setOnClickListener(this);

		mBtReadData = (Button) findViewById(R.id.btn_read);
		mBtReadData.setOnClickListener(this);

		mEtCardPwd = (EditText) findViewById(R.id.et_pwd);
		mEtCardID = (EditText) findViewById(R.id.ed_card_num);
		mEtSectorNum = (EditText) findViewById(R.id.et_ss);
		mEtBlockNum = (EditText) findViewById(R.id.ed_block_num);
		mEtWriteData = (EditText) findViewById(R.id.ed_write_block);
		mEtReadData = (EditText) findViewById(R.id.ed_read_block);
		mEtCardType = (EditText) findViewById(R.id.ed_card_type);
		spinner = (Spinner) findViewById(R.id.m1_spinner);
	}

	private void initData() {
		m = this.getResources().getStringArray(R.array.m1_pwd_type);

		// 将可选内容与ArrayAdapter连接起来
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m);

		// 设置下拉列表的风格
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// 将adapter 添加到spinner中
		spinner.setAdapter(adapter);

		// 添加事件Spinner事件监听
		spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
	}

	// 使用数组形式操作
	class SpinnerSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
			Log.i("whw", "position=" + position);
			switch (position) {
			case 0:
				cardType = M1CardAPI.TYPE_A;
				break;
			case 1:
				cardType = M1CardAPI.TYPE_B;
				break;
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_getCardNum:
			try {
				showProgressDialog("Getting cardNum...");
				mEtCardID.setText("");
				api.getCardID();
				mBtGetCard.setEnabled(false);
			} catch (Exception e) {
				cancleProgressDialog();
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.m1_str_get_fail),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_write:
			try {
				if (!isEmpty() && !isWriteDataEmpty()&&isValidity()) {
					showProgressDialog("Writting Data...");
					api.writeData(mEtWriteData.getText().toString(), cardType,
							Integer.parseInt(mEtSectorNum.getText().toString()),
							Integer.parseInt(mEtBlockNum.getText().toString()), mEtCardPwd.getText().toString());
				}
			} catch (Exception e) {
				cancleProgressDialog();
				ToastUtil.showToast(this,
						getResources().getString(R.string.m1_str_write_fail) + " - " + e.getMessage());
			}
			break;
		case R.id.btn_read:
			try {
				if (!isEmpty()&&isValidity()) {
					showProgressDialog("Reading Data...");
					api.readData(cardType, Integer.parseInt(mEtSectorNum.getText().toString()),
							Integer.parseInt(mEtBlockNum.getText().toString()), mEtCardPwd.getText().toString());
				}
			} catch (Exception e) {
				cancleProgressDialog();
				ToastUtil.showToast(this, getResources().getString(R.string.m1_str_read_fail) + " - " + e.getMessage());
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 判断扇区号和块号的填写的有效性
	 * 
	 * @return
	 */
	private boolean isValidity() {
		int shan = Integer.parseInt(mEtSectorNum.getText().toString());
		int kuai = Integer.parseInt(mEtBlockNum.getText().toString());
		if (shan > 255 || shan < 0) {
			ToastUtil.showToast(this, R.string.m1_str_block_shan);
			return false;
		} else if (kuai > 255 || kuai < 0) {
			ToastUtil.showToast(this, R.string.m1_str_block_kuai);
			return false;
		} else{
			return true;
		}
		
	}

	/**
	 * 判断扇区号或块号是否为空
	 * 
	 * @return
	 */
	private boolean isEmpty() {
		if (TextUtils.isEmpty(mEtSectorNum.getText().toString())
				|| TextUtils.isEmpty(mEtBlockNum.getText().toString())) {
			ToastUtil.showToast(this, R.string.m1_str_block_not_empty);
			return true;
		} else {
			return false;
		}
	}

	private boolean isWriteDataEmpty() {
		String write = mEtWriteData.getText().toString();
		String regex = "[a-fA-F0-9]{0,32}";
		if (TextUtils.isEmpty(mEtWriteData.getText().toString())) {
			ToastUtil.showToast(this, R.string.m1_str_block_write_not_empty);
			return true;
		} else if (!write.matches(regex)) {
			ToastUtil.showToast(this, R.string.m1_str_block_write_error);
			return true;
		} else {
			return false;
		}
	}

	private void showProgressDialog(String str) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(str);
		progressDialog.setCanceledOnTouchOutside(true);
		progressDialog.show();
	}

	private void cancleProgressDialog() {
		if (null != this.progressDialog && this.progressDialog.isShowing()) {
			this.progressDialog.cancel();
			this.progressDialog = null;
		}
	}

	@Override
	protected void onResume() {
		api.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		api.close();
		super.onPause();
	}

	@Override
	public void getCardIDFail() {
		cancleProgressDialog();
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.m1_str_get_fail), Toast.LENGTH_SHORT)
				.show();
		mBtGetCard.setEnabled(true);
	}

	@Override
	public void getCardIDSuccess(String data) {
		if (data.length() == 8)
			mEtCardID.setText(data);
		else if (data.length() == 14) {
			String num = data.substring(0, 8);
			String type = data.substring(8);
			mEtCardID.setText(num);
			mEtCardType.setText(type);
		}
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.m1_str_get_success),
				Toast.LENGTH_SHORT).show();
		cancleProgressDialog();
		mBtGetCard.setEnabled(true);
	}

	@Override
	public void writeDataFail(int errorcode) {
		cancleProgressDialog();
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.m1_str_write_fail) + errorcode,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void writeDataSuccess() {
		cancleProgressDialog();
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.m1_str_write_success),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void readDataFail() {
		cancleProgressDialog();
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.m1_str_read_fail), Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public void readDataSuccess(byte[] data) {
		cancleProgressDialog();
		mEtReadData.setText(DataUtils.toHexString(data));
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.m1_str_read_success),
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void generalFail() {

	}

	@Override
	public void generalSuccess() {

	}
}