package com.authentication.eighthundred.activity;

import com.authentication.eighthundred.R;
import com.example.iccardapi.IcInterface;
import com.zchr.jni.fromnative.PbocEmvInterface;
import com.zchr.jni.tonative.TermInterface;
import com.zchr.util.DefineFinal;
import com.zchr.util.MyUtil;
import com.zchr.util.ReturnStringData;
import com.zchr.util.ScreenShowInfo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DebitCreditTransReadCardActivity extends Activity implements OnClickListener {

	//屏幕行数
	public static final int SCREEN_SHOW_LINE_MAX = 8;

	//屏幕列数
	public static final int SCREEN_SHOW_COL_MAX = 32;

	//屏幕显示内容
	private static String[] m_astrScreenShow;

	//上下文
	public Activity m_activityMain;

	//显示TextView
	public static TextView m_textViewScreenShow;

	//处理状态,0-准备、1-处理中
	private static int m_iProcState;
	private TextView tvTitle;
	
	private int iLine ;
	
	public static void setM_iProcState(int m_iProcState) {
		DebitCreditTransReadCardActivity.m_iProcState = m_iProcState;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_debit_credit_read_card);

		//初始化Activity
		InitActivity();

		//数据初始化
		DataInit();

	}
	@Override
	protected void onResume() {
		IcInterface.setGpioFlg(true);
		IcInterface.openSerialPort();
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		m_iProcState = 0;
		IcInterface.closeSerialPort();
		super.onDestroy();
	}
	//初始化Activity
	public void InitActivity() {
		m_iProcState = 0;

		TermInterface.SetContext(this);

		m_activityMain = this;

		m_textViewScreenShow = (TextView) findViewById(R.id.screen_show);

		((ImageView) findViewById(R.id.image_back)).setOnClickListener(this);
		((Button) findViewById(R.id.trans_btn)).setOnClickListener(this);
		((Button) findViewById(R.id.clear_btn)).setOnClickListener(this);

		m_textViewScreenShow.setText("");

		m_astrScreenShow = new String[SCREEN_SHOW_LINE_MAX];
		tvTitle = (TextView) findViewById(R.id.tv_title);
		if(DefineFinal.iIccInterface == 1){
			tvTitle.setText(R.string.card_read_contact);
		}else if(DefineFinal.iIccInterface == 2){
			tvTitle.setText(R.string.card_read_uncontact);
		}
	}

	//重写onClick事件
	@Override
	public void onClick(View view) {


		switch (view.getId()) {
		case R.id.image_back:
			finish();
			break;

		case R.id.trans_btn:
			
			//置空显示
			clearShowInfo();

			//借贷记读卡
			DebitCreditDemo();

			break;
		case R.id.clear_btn:
			m_textViewScreenShow.setText("");
			break;
		}
	}

	//借贷记Demo
	public void DebitCreditDemo() {

		if (m_iProcState == 1) {
			Toast.makeText(this, getResources().getString(R.string.card_being_dealt_with), Toast.LENGTH_SHORT).show();
			return;
		}
		m_iProcState = 1;
		//显示屏信息处理
		setScreenShow(255, 2, getResources().getString(R.string.card_insert_contact_card), ScreenShowInfo.SCREEN_SHOW_ALIGN_CENTER);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				iLine = 1;
				try {
					if (PbocEmvInterface.PbocEmvDebitCreditReadCard() != 0) {
						m_iProcState = 0;
						setScreenShow(255, 2, getResources().getString(R.string.card_read_fail), ScreenShowInfo.SCREEN_SHOW_ALIGN_CENTER);
						return;
					}

					ReturnStringData returnStringData = new ReturnStringData();
					Log.d("jokey", "returnStringData"+returnStringData.toString());

					if (PbocEmvInterface.FindAllTag(0x5A, returnStringData) == 0) {
						Log.d("jokey", "卡号 : " + returnStringData.getStringData());
						setScreenShow(255, 1, getResources().getString(R.string.card_read_success), ScreenShowInfo.SCREEN_SHOW_ALIGN_CENTER);
						setScreenShow(0, 3, getResources().getString(R.string.card_card_number) + returnStringData.getStringData(), ScreenShowInfo.SCREEN_SHOW_ALIGN_CENTER);
					}else{
						setScreenShow(255, 2, getResources().getString(R.string.card_read_fail), ScreenShowInfo.SCREEN_SHOW_ALIGN_CENTER);
					}

					if (PbocEmvInterface.FindAllTag(0x57, returnStringData) == 0) {
						Log.d("jokey", "二磁道 : " + returnStringData.getStringData());
						setScreenShow(0, 5, getResources().getString(R.string.card_second_track) + returnStringData.getStringData(), ScreenShowInfo.SCREEN_SHOW_ALIGN_CENTER);
					}else{
						setScreenShow(255, 2, getResources().getString(R.string.card_read_fail), ScreenShowInfo.SCREEN_SHOW_ALIGN_CENTER);
					}

					if (PbocEmvInterface.FindAllTag(0x5F20, returnStringData) == 0) {
						String name = new String(MyUtil.HexStringToByteArray(returnStringData.getStringData()), "gbk");
						setScreenShow(0, 7, getResources().getString(R.string.card_user_name) + name, ScreenShowInfo.SCREEN_SHOW_ALIGN_CENTER);
					}
					m_iProcState = 0;
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
				Looper.loop();
			}
		});
		thread.start();
	}
	/**
	 * Set the info for Showing
	 */
	public static void setScreenShow(int mClearLine,int mShowLine,String mShowInfo,int mAlignMode){
		ScreenShowInfo screenShowInfo = new ScreenShowInfo();
		screenShowInfo.setClearLine(mClearLine);
		screenShowInfo.setShowLine(mShowLine);
		screenShowInfo.setShowInfo(mShowInfo);
		screenShowInfo.setAlignMode(mAlignMode);
		Message message = null;
		message = new Message();
		message.what = 0xF1;
		message.obj = screenShowInfo;
		m_ActivityHandler.sendMessage(message);
	}
	//NFC读写处理
	public void NfcReadWriteProc() {

		//IC卡处理
		//DebitCreditDemo();
	}

	public static Handler m_ActivityHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0xF1:
				//显示屏信息
				ScreenShowInfo scrnShowInfo = (ScreenShowInfo) msg.obj;

				//显示屏信息处理
				ShowScreenInfoProc(scrnShowInfo);

				break;
			}
			super.handleMessage(msg);
		}
	};

	//数据初始化
	public void DataInit() {

		if (MyUtil.GetSysConfig((Activity) this) != 0) {
			Toast.makeText(this, getResources().getString(R.string.card_no_configuration_info), Toast.LENGTH_SHORT).show();
		}
	}

	//显示屏信息处理
	public static void ShowScreenInfoProc(ScreenShowInfo i_sScreenShowInfo) {
		int i;

		if (i_sScreenShowInfo.getClearLine() == 255) {
			for (i = 0; i < SCREEN_SHOW_LINE_MAX; i++) {
				SetLineSpaceInfo(i);
			}
		} else if (i_sScreenShowInfo.getClearLine() <= SCREEN_SHOW_LINE_MAX && i_sScreenShowInfo.getClearLine() > 0) {
			SetLineSpaceInfo(i_sScreenShowInfo.getClearLine() - 1);
		}

		String strShowInfo = "";
		strShowInfo = i_sScreenShowInfo.getShowInfo();

		m_astrScreenShow[i_sScreenShowInfo.getShowLine() - 1] = strShowInfo;

		String strScreenInfo = "";
		for (i = 0; i < SCREEN_SHOW_LINE_MAX; i++) {
			strScreenInfo += m_astrScreenShow[i] + "\r\n";
		}

		m_textViewScreenShow.setText(strScreenInfo);
	}

	//置空行信息
	public static void SetLineSpaceInfo(int i_iLineNum) {

		int i;

		m_astrScreenShow[i_iLineNum] = "";

		for (i = 0; i < SCREEN_SHOW_COL_MAX; i++) {
			m_astrScreenShow[i_iLineNum] += " ";
		}
	}
	
	//清空显示信息
	public void clearShowInfo() {
		int i;
		for (i = 0; i < SCREEN_SHOW_LINE_MAX; i++) {
			m_astrScreenShow[i] = "";
		}
	}
}