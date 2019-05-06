package com.authentication.eighthundred.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.authentication.eighthundred.R;
import com.authentication.eighthundred.asynctask.AsynIcCardOrder;
import com.authentication.eighthundred.asynctask.AsynIcCardOrder.OnCheckListener;
import com.authentication.eighthundred.asynctask.AsynIcCardOrder.OnResetListener;
import com.authentication.eighthundred.asynctask.AsynIcCardOrder.OnSendOperCmdListener;
import com.authentication.utils.DataUtils;
import com.authentication.utils.ToastUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android_serialport_api.SerialPortManager;

/**
 * 定制卡
 */
public class IcCardOrderActivity extends Activity implements OnClickListener {
	private EditText edTxtOperCmd;
	private Button btnResetCmd;
	private Button btnOperCmd;
	private Button btnClear;
	private Button btnCheck;
	private TextView txtRetInfo;

	private ProgressDialog progressDialog;
	private MyApplication application;
	private AsynIcCardOrder asynIcCardOrder;
	public static final byte[] resetContact = {0x00,0x00,0x00,0x00};//接触
	public static final byte[] resetUncontact = {0x01,0x00,0x00,0x01};//非接触
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.iccardorder);
		
		initView();
		initData();
	}
	@Override
	protected void onResume() {
		if(!SerialPortManager.getInstance().isOpen()
				&& !SerialPortManager.getInstance().openSerialPort()) {
			ToastUtil.showToast(this, R.string.open_serial_fail);
		}
		super.onResume();
	}
	private void initView() {
		edTxtOperCmd = (EditText) findViewById(R.id.OperCmd_ICCard);
		btnResetCmd = (Button) findViewById(R.id.Reset_ICCard);
		btnResetCmd.setOnClickListener(this);
		btnOperCmd = (Button) findViewById(R.id.SendOperCmd_ICCard);
		btnOperCmd.setOnClickListener(this);
		btnCheck = (Button) findViewById(R.id.CheckCardPresent_ICCard);
		btnCheck.setOnClickListener(this);
		
		this.btnClear = (Button) findViewById(R.id.ClearOrder_ICCard);
		this.btnClear.setOnClickListener(this);

		this.txtRetInfo = (TextView) findViewById(R.id.ReturnInfo_ICCard);
		if(DataUtils.isContactless){
			btnCheck.setVisibility(View.GONE);
		}else{
			btnCheck.setVisibility(View.VISIBLE);
		}
	}

	private void initData() {
		application = (MyApplication) getApplicationContext();
		asynIcCardOrder = new AsynIcCardOrder(application.getHandlerThread()
				.getLooper());

		asynIcCardOrder.setOnSendOperCmdListener(new OnSendOperCmdListener() {

			@Override
			public void onSendOperCmdSuccess(String result) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardOrderActivity.this, "Gets the return value successfully");
				IcCardOrderActivity.this.txtRetInfo.setText(result);
			}

			@Override
			public void onSendOperCmdFail(int resultCode) {
				cancleProgressDialog();
				ToastUtil.showToast(IcCardOrderActivity.this, "Gets the return value failed");
			}
		});
		asynIcCardOrder.setOnResetListener(new OnResetListener() {
			
			@Override
			public void callback(boolean result) {
				if(result){
					txtRetInfo.setText("Reset OK");
				}else{
					txtRetInfo.setText("Reset Fail");
				}
			}
		});
		asynIcCardOrder.setOnCheckListener(new OnCheckListener() {
			
			@Override
			public void callback(boolean result) {
				if(result){
					txtRetInfo.setText("IC card present");
				}else{
					txtRetInfo.setText("IC card absent");
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.SendOperCmd_ICCard:
			showProgressDialog("Now, sending the command...");
			if (TextUtils.isEmpty(this.edTxtOperCmd.getText())) {
				ToastUtil.showToast(this, "Input can not be empty ");
				cancleProgressDialog();
				return;
			}
			if (!isHexStrNumber(this.edTxtOperCmd.getText().toString())) {
				ToastUtil.showToast(this, "Input is not valid");
				cancleProgressDialog();
				return;
			}

			byte[] tempCmd = hexToByteArray(this.edTxtOperCmd.getText()
					.toString());

			this.asynIcCardOrder.sendOperCmd(tempCmd);
			break;
		case R.id.Reset_ICCard:
			if(!DataUtils.isContactless){
				Log.d("jokey", "resetContact");
				this.asynIcCardOrder.reset(resetContact);
			}else{
				Log.d("jokey", "resetUncontact");
				this.asynIcCardOrder.reset(resetUncontact);
			}
			break;
		case R.id.CheckCardPresent_ICCard:
			asynIcCardOrder.checkCardPresent();
			break;
		case R.id.ClearOrder_ICCard:
			clear();
			break;
		default:
			break;
		}
	}

	public static boolean isHexStrNumber(String s) {
		Matcher m = Pattern.compile("^[0-9A-Fa-f]+$").matcher(s);
		return m.matches();
	}

	static public byte[] hexToByteArray(String inHex)// hex字符串转字节数组
	{
		int hexlen = inHex.length();
		byte[] result;
		if (isOdd(hexlen) == 1) {// 奇数
			hexlen++;
			result = new byte[(hexlen / 2)];
			inHex = "0" + inHex;
		} else {// 偶数
			result = new byte[(hexlen / 2)];
		}

		int j = 0;
		for (int i = 0; i < hexlen; i += 2) {
			result[j] = hexToByte(inHex.substring(i, i + 2));
			j++;
		}
		return result;
	}

	static public int isOdd(int num) {
		return num & 0x1;
	}

	static public byte hexToByte(String inHex)// Hex字符串转byte
	{
		return (byte) Integer.parseInt(inHex, 16);
	}

	private void clear() {
		this.txtRetInfo.setText("");
		this.edTxtOperCmd.setText("");
	}

	private void showProgressDialog(String message) {
		this.progressDialog = new ProgressDialog(this);
		this.progressDialog.setMessage(message);
		if (!this.progressDialog.isShowing()) {
			this.progressDialog.show();
		}
	}

	private void cancleProgressDialog() {
		if (null != this.progressDialog && this.progressDialog.isShowing()) {
			this.progressDialog.cancel();
			this.progressDialog = null;
		}
	}

	@Override
	protected void onDestroy() {
		SerialPortManager.getInstance().closeSerialPort(1);
		super.onDestroy();
	}
}