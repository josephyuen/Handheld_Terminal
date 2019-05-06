package com.authentication.eighthundred.activity;

import com.authentication.eighthundred.R;
import com.authentication.utils.ToastUtil;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android_serialport_api.PrinterAPI;
import android_serialport_api.PrinterAPI.printerStatusListener;

public class DanjuActivity extends Activity implements printerStatusListener{
	private LinearLayout ll_v;
	private Button mbtData,mbt,mbtSign;
	private PrinterAPI api = new PrinterAPI();
	private EditText mEditText;
	private ImageView ivData,ivSign;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.print);
		ivData = (ImageView) findViewById(R.id.iv_etdata);
		ll_v = (LinearLayout) findViewById(R.id.ll_view);
		mEditText=(EditText) findViewById(R.id.et_data);

		mbt = (Button) findViewById(R.id.bt_print);
		mbt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				api.printView(ll_v,DanjuActivity.this);
			}
		});
	
		mbtData=(Button) findViewById(R.id.bt_printData);
		mbtData.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String str = mEditText.getText().toString();
				ToastUtil.showToast(DanjuActivity.this, str.length()+"");
				if(str.length()<=1600){
					api.printView(mEditText,DanjuActivity.this);
				}else
					ToastUtil.showToast(DanjuActivity.this, getResources().getString(R.string.printer_length1600));
			}
		});
		mbtSign = (Button) findViewById(R.id.bt_sign);
		mbtSign.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
			}
		});
	}
	@Override
	protected void onStop() {
		api.close();
		super.onStop();
	}
	@Override
	public void hot() {
		Toast.makeText(getApplicationContext(), "hot", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void noPaper() {
		Toast.makeText(getApplicationContext(), "noPaper", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void end() {
		Toast.makeText(getApplicationContext(), "end", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void work() {
		Toast.makeText(getApplicationContext(), "The printer is working", Toast.LENGTH_SHORT).show();
	}
	
}