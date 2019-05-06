package com.authentication.eighthundred.activity;

import java.io.UnsupportedEncodingException;

import com.authentication.eighthundred.R;
import com.authentication.utils.PaintView;
import com.authentication.utils.ToastUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;
import android_serialport_api.PrinterAPI;
import android_serialport_api.PrinterAPI.printerStatusListener;
import android_serialport_api.SerialPortManager;

public class PrinterActivity extends Activity
		implements OnClickListener, OnItemSelectedListener, OnCheckedChangeListener, printerStatusListener {

	private String path;
	private Button mBtPrint, mBtInit, mBtPrintBC, mBtPrintQC, mBtPrintPic, mBtPrintp, mBtSelectImg, mbtTest;
	private EditText mEdPrintstr, mEdPrintBC, mEdPrintQC;
	private CheckBox mCbHeight, mCbWeight, mCbBold, mCbUnderLine;
	private Spinner mSAlign, mSBarcodeType, mSQrcodeType, mSFlash;
	private View include,includeSign;
	private ScrollView mScrollView;
	private LinearLayout ll_v;
	private Button mbtData,mbt,mbtSign,mbtOk,mbtClear;
	private EditText mEditText;
	
	private PrinterAPI api;

	private boolean mBoolTimesHeight = false;
	private boolean mBoolTimesWeight = false;
	private boolean mBoolTimesCrude = false;
	private boolean mBoolTimesUnderLine = false;
	private int mBarcodeType;
	private int mQrcodeType;
	private int mFlashImageType = 0;

	private Bitmap mBitmap,mBitmapSign;
	private PaintView mView;
	private ImageView ivSign;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_printer);

		init();

	}

	private void init() {
		mCbHeight = (CheckBox) findViewById(R.id.timesHeight_check);
		mCbHeight.setOnCheckedChangeListener(this);
		mCbWeight = (CheckBox) findViewById(R.id.timesWeight_check);
		mCbWeight.setOnCheckedChangeListener(this);
		mCbBold = (CheckBox) findViewById(R.id.setBold_check);
		mCbBold.setOnCheckedChangeListener(this);
		mCbUnderLine = (CheckBox) findViewById(R.id.setUnderLine_check);
		mCbUnderLine.setOnCheckedChangeListener(this);

		mBtPrint = (Button) findViewById(R.id.paperPrint_printBtn);
		mBtPrint.setOnClickListener(this);
		mBtInit = (Button) findViewById(R.id.init_printBtn);
		mBtInit.setOnClickListener(this);
		mBtPrintBC = (Button) findViewById(R.id.barcode_printBtn);
		mBtPrintBC.setOnClickListener(this);
		mBtPrintQC = (Button) findViewById(R.id.qrcode_printBtn);
		mBtPrintQC.setOnClickListener(this);
		mBtPrintPic = (Button) findViewById(R.id.flashPic_printBtn);
		mBtPrintPic.setOnClickListener(this);
		mBtPrintp = (Button) findViewById(R.id.bt_printerPic);
		mBtPrintp.setOnClickListener(this);
		mBtSelectImg = (Button) findViewById(R.id.bt_selsectImage);
		mBtSelectImg.setOnClickListener(this);
		mbtTest = (Button) findViewById(R.id.bt_printTest);
		mbtTest.setOnClickListener(this);

		mEdPrintstr = (EditText) findViewById(R.id.inputData_printBtn);
		mEdPrintBC = (EditText) findViewById(R.id.inputBarData_printBtn);
		mEdPrintQC = (EditText) findViewById(R.id.inputQrData_printBtn);

		mSAlign = (Spinner) findViewById(R.id.alignType_check);
		mSAlign.setOnItemSelectedListener(this);
		mSBarcodeType = (Spinner) findViewById(R.id.barcodeType_check);
		mSBarcodeType.setOnItemSelectedListener(this);
		mSQrcodeType = (Spinner) findViewById(R.id.qrcodeType_check);
		mSQrcodeType.setOnItemSelectedListener(this);
		mSFlash = (Spinner) findViewById(R.id.flashPicType_check);
		mSFlash.setOnItemSelectedListener(this);

		include = findViewById(R.id.include);
		includeSign = findViewById(R.id.include_sign);
		mScrollView = (ScrollView) findViewById(R.id.sv_scrollview);
		ll_v = (LinearLayout) findViewById(R.id.ll_view);
		mEditText=(EditText) findViewById(R.id.et_data);
		ivSign = (ImageView)findViewById(R.id.iv_sign);
				
		mbt = (Button) findViewById(R.id.bt_print);
		mbt.setOnClickListener(this);
		mbtData = (Button) findViewById(R.id.bt_printData);
		mbtData.setOnClickListener(this);
		mbtSign = (Button) findViewById(R.id.bt_sign);
		mbtSign.setOnClickListener(this);
		mbtOk = (Button) findViewById(R.id.tablet_ok); 
		mbtOk.setOnClickListener(this);
		mbtClear = (Button) findViewById(R.id.tablet_clear);
		mbtClear.setOnClickListener(this);
		
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.tablet_view);

		mView = new PaintView(this);
		frameLayout.addView(mView);
		mView.requestFocus();
	}

	@Override
	protected void onResume() {
		// api = PrinterAPI.getInstance();
		if(api == null){
			Log.d("jokey", "new API");
			api = new PrinterAPI();
		}
		// add by YJJ 20161124
//		Log.d("jokey", "isOpen==  " + SerialPortManager.getInstance().isOpen());
		if (!SerialPortManager.getInstance().isPrintOpen()
				&& !SerialPortManager.getInstance().openSerialPortPrinter()) {
			ToastUtil.showToast(this, R.string.open_serial_fail);
		}
		// end
//		Log.d("jokey", "bt_selsectImage2  " + PrinterAPI.isFlag());
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.paperPrint_printBtn:
			try{
				if (mEdPrintstr.getText().toString().length() == 0) {
					Toast.makeText(this, "Input can not be empty", Toast.LENGTH_SHORT).show();
				} else {
					String str = mEdPrintstr.getText().toString();
					int len = 0;
					try {
						len = str.getBytes("GBK").length;
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					if (len >= 14000) {
						Toast.makeText(this, "Current number of bytes:" + len + ",Can not be more than 14000",
								Toast.LENGTH_SHORT).show();
					} else {
						api.printPaper(str + "\n", PrinterActivity.this);
						Toast.makeText(this, "字符: " + str.length() + " GBK: " + len, Toast.LENGTH_SHORT).show();
					}
				}
			}catch (Exception e) {
				Toast.makeText(getApplicationContext(),"Error: - "+e.getMessage(),Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.init_printBtn:
			api.initPrint(this);
			mCbHeight.setChecked(false);
			mCbWeight.setChecked(false);
			mCbBold.setChecked(false);
			mCbUnderLine.setChecked(false);
			break;
		case R.id.barcode_printBtn:
			try {
				printBarcode(mEdPrintBC.getText().toString(), mBarcodeType);
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e + "", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.qrcode_printBtn:
			try {
				printQrcode(mEdPrintQC.getText().toString(), mQrcodeType);
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e + "", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.flashPic_printBtn:
			api.printFlashImage(mFlashImageType, this);
			break;
		case R.id.bt_selsectImage:
			if (!PrinterAPI.isFlag()) {
				path = null;
				Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, 110);
			} else {
				this.work();
			}
			break;
		case R.id.bt_printerPic:
			if (path == null) {
				ToastUtil.showToast(getApplicationContext(), "Select a picture to print");
			} else {
				if (mBitmap == null) {
					mBitmap = BitmapFactory.decodeFile(path);
					api.printPic(mBitmap, this);
				}
			}
			break;
		case R.id.bt_printTest:
			if (include.getVisibility() == View.GONE) {
				mScrollView.setVisibility(View.GONE);
				include.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.bt_print:
			api.printView(ll_v,PrinterActivity.this);
			break;
		case R.id.bt_printData:
			String str = mEditText.getText().toString();
			ToastUtil.showToast(PrinterActivity.this, str.length()+"");
			if(str.length()<=1600){
				api.printView(mEditText,PrinterActivity.this);
			}else
				ToastUtil.showToast(PrinterActivity.this, getResources().getString(R.string.printer_length1600));
			break;
		case R.id.bt_sign:
			if(includeSign.getVisibility() == View.GONE){
				include.setVisibility(View.GONE);
				includeSign.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.tablet_clear:
			mView.clear();
			break;
		case R.id.tablet_ok:
			mBitmapSign = mView.getCachebBitmap();
			ivSign.setImageBitmap(mBitmapSign);
			includeSign.setVisibility(View.GONE);
			include.setVisibility(View.VISIBLE);
			break;
		}
	}

	/**
	 * 打印条码
	 * 
	 * @param str
	 *            要打印的数据
	 * @param codeType
	 *            条码类型
	 * @throws WriterException
	 */
	private void printBarcode(String str, int codeType) throws WriterException {
		switch (codeType) {
		case 0:
			api.printPic(api.createBarCode(getApplicationContext(), str, BarcodeFormat.UPC_A, 300, 200, true), this);
			break;
		case 1:
			api.printPic(api.createBarCode(getApplicationContext(), str, BarcodeFormat.EAN_8, 300, 200, true), this);
			break;
		case 2:
			if (str.length() <= 26)
				api.printPic(api.createBarCode(getApplicationContext(), str, BarcodeFormat.CODE_39, 384, 200, true),
						this);
			else
				ToastUtil.showToast(this, getResources().getString(R.string.printer_length26));
			break;
		case 3:
			if (str.length() <= 26)
				api.printPic(api.createBarCode(getApplicationContext(), str, BarcodeFormat.CODE_128, 300, 200, true),
						this);
			else
				ToastUtil.showToast(this, getResources().getString(R.string.printer_length26));
			break;
		case 4:
			if (str.length() <= 26)
				api.printPic(api.createBarCode(getApplicationContext(), str, BarcodeFormat.CODABAR, 300, 200, true),
						this);
			else
				ToastUtil.showToast(this, getResources().getString(R.string.printer_length26));
			break;
		case 5:
			if (str.length() <= 26)
				api.printPic(api.createBarCode(getApplicationContext(), str, BarcodeFormat.ITF, 300, 200, true), this);
			else
				ToastUtil.showToast(this, getResources().getString(R.string.printer_length26));
			break;
		}
	}

	/**
	 * 打印二维码
	 * 
	 * @param str
	 *            要打印的数据
	 * @param codeType
	 *            二维码类型
	 * @throws WriterException
	 */
	private void printQrcode(String str, int codeType) throws WriterException {
		switch (codeType) {
		case 0:
			api.printPic(api.createQRCode(str, BarcodeFormat.QR_CODE, 384, "GBK"), this);
			break;
		case 1:
			api.printPic(api.createQRCode(str, BarcodeFormat.AZTEC, 384, "GBK"), this);
			break;
		default:
			break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
		case R.id.alignType_check:
			api.setAlighType(position, this);
			break;
		case R.id.barcodeType_check:
			mBarcodeType = position;
			break;
		case R.id.qrcodeType_check:
			mQrcodeType = position;
			break;
		case R.id.flashPicType_check:
			mFlashImageType = position;
			break;
		default:
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.timesHeight_check:
			mBoolTimesHeight = isChecked;
			api.setKGCU(mBoolTimesWeight, mBoolTimesHeight, mBoolTimesCrude, mBoolTimesUnderLine, this);
			break;
		case R.id.timesWeight_check:
			mBoolTimesWeight = isChecked;
			api.setKGCU(mBoolTimesWeight, mBoolTimesHeight, mBoolTimesCrude, mBoolTimesUnderLine, this);
			break;
		case R.id.setBold_check:
			mBoolTimesCrude = isChecked;
			api.setKGCU(mBoolTimesWeight, mBoolTimesHeight, mBoolTimesCrude, mBoolTimesUnderLine, this);
			break;
		case R.id.setUnderLine_check:
			mBoolTimesUnderLine = isChecked;
			api.setKGCU(mBoolTimesWeight, mBoolTimesHeight, mBoolTimesCrude, mBoolTimesUnderLine, this);
			break;

		}
	}

	@Override
	public void onBackPressed() {
		if (include.getVisibility() == View.VISIBLE) {
			include.setVisibility(View.GONE);
			mScrollView.setVisibility(View.VISIBLE);
		} else if(includeSign.getVisibility() == View.VISIBLE){
			includeSign.setVisibility(View.GONE);
			include.setVisibility(View.VISIBLE);
		}else{
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		Log.d("jokey", "onDestroy()");
		api.closePrint();
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		mBitmap = null;
		api.close();
		super.onStop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 110:
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				Cursor cursor = getContentResolver().query(uri, null, null, null, null);
				if (cursor != null && cursor.moveToFirst()) {
					path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);

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
		mBitmap = null;
		Toast.makeText(getApplicationContext(), "end", Toast.LENGTH_SHORT).show();
	}
	@Override
	public void work() {
		Toast.makeText(getApplicationContext(), "The printer is working", Toast.LENGTH_SHORT).show();
	}
}