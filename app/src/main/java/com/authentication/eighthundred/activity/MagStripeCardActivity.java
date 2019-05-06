package com.authentication.eighthundred.activity;

import com.authentication.eighthundred.R;
import com.authentication.utils.DataUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.MagStripeCardAPI;
import android_serialport_api.MagStripeCardAPI.OnMagStripeListener;

public class MagStripeCardActivity extends Activity implements OnClickListener ,OnMagStripeListener{
	private Button btnRead;
	private Button btnClear;
	private TextView one, two, three;
	private ProgressDialog progressDialog;
	private MagStripeCardAPI api;
	private Handler handler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.magstripecard);
		initView();
	}
	
	private void initView() {
		api = new MagStripeCardAPI(this);
		btnRead = (Button) findViewById(R.id.CircleRead_MsCard);
		btnRead.setOnClickListener(this);

		btnClear = (Button) findViewById(R.id.Clear_MsCard);
		btnClear.setOnClickListener(this);

		one = (TextView) findViewById(R.id.one);
		two = (TextView) findViewById(R.id.two);
		three = (TextView) findViewById(R.id.three);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.CircleRead_MsCard:
			showProgressDialog("Now it is reading......");
			api.readCard();
			break;
		case R.id.Clear_MsCard:
			ClearAll();
			break;
		default:
			break;
		}
	}

	private void ClearAll() {
		one.setText("");
		two.setText("");
		three.setText("");
	}

	private void showProgressDialog(String message) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(message);
		if (!progressDialog.isShowing()) {
			progressDialog.show();
		}
	}
	private void cancleProgressDialog() {
		if (this.progressDialog != null && this.progressDialog.isShowing()) {
			this.progressDialog.cancel();
			this.progressDialog = null;
		}
	}
	
	@Override
	protected void onRestart() {
		Log.d("jokey", "MagStripeCardActivity--->onRestart()");
		super.onRestart();
	}
	@Override
	protected void onResume() {
		Log.d("jokey", "MagStripeCardActivity--->onResume()");
		api.openMagStripeCard();
		super.onResume();
	}
	@Override
	protected void onPause() {
		Log.d("jokey", "MagStripeCardActivity--->onPause()");
		api.closeMagStripeCard();
		super.onPause();
	}
	/**
	 * Callback the magnetic stripe card data from api
	 */
	@Override
	public void callback(final byte[] buffer) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if(buffer == null){
					cancleProgressDialog();
					Toast.makeText(getApplicationContext(),"Read fail",Toast.LENGTH_SHORT).show();
				}else{
					String n = DataUtils.toHexString(buffer);
					cancleProgressDialog();
					ClearAll();
//					updateInfo(buffer);
					showCardData(buffer);
				}
				
			}
		});
	}
	
	/**
	 * Decode magnetic stripe card data and show it
	 */
	private void showCardData(byte[] buffer) {
		String str = DataUtils.toStringHex1(DataUtils.toHexString(buffer));
		Log.d("jokey", "data:"+str);
		int havaOne = str.indexOf("%");//25
		int havaTwo = str.indexOf(";");//3b
		int havaEndOne = str.indexOf("?");//3f
		if(havaOne != -1){
			if(havaEndOne != -1){
				this.one.setText(str.substring(havaOne, havaEndOne+1));
			}else{
				this.one.setText("");
			}
		}else{
			this.one.setText("");
		}
		if(havaTwo != -1){
			String twoStr = str.substring(havaTwo);
			Log.d("jokey", "twoStr:"+twoStr);
			int havaEndTwo = twoStr.indexOf("?");
			Log.d("jokey", "havaEndTwo:"+havaEndTwo);
			if(havaEndTwo != -1){
				this.two.setText(twoStr.substring(0, havaEndTwo+1));
				int haveThree = twoStr.indexOf(";",havaEndTwo);
				Log.d("jokey", "haveThree:"+haveThree);
				if(haveThree != -1){
					String threeStr = twoStr.substring(haveThree);
					Log.d("jokey", "threeStr:"+threeStr);
					int haveEndThree = threeStr.indexOf("?");
					if(haveEndThree != -1){
						this.three.setText(threeStr.substring(0, haveEndThree+1));
					}else{
						this.three.setText("");
					}
				}else{
					this.three.setText("");
				}
			} else {
				this.two.setText("");
				
			}
		} else {
			this.two.setText("");
			this.three.setText("");
		}
		if(!TextUtils.isEmpty(""+this.one.getText()+this.two.getText()+this.three.getText())){
			Toast.makeText(getApplicationContext(),"Read success!",Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(getApplicationContext(),"Data is not full!",Toast.LENGTH_SHORT).show();
		}
	}
}