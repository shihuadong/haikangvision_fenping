package com.monitor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.MediaPlayer.PlayM4.Player;
import org.apache.http.conn.util.InetAddressUtils;

import android.R.integer;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_CLIENTINFO;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_NETCFG_V30;
import com.hikvision.netsdk.NET_DVR_PLAYBACK_INFO;
import com.hikvision.netsdk.NET_DVR_TIME;
import com.hikvision.netsdk.PTZCommand;
import com.hikvision.netsdk.PlaybackCallBack;
import com.hikvision.netsdk.RealPlayCallBack;
import com.hikvision.netsdk.VoiceDataCallBack;
import com.monitor.account.AccountInfo;
import com.monitor.account.DatabaseService;

public class MainActivity extends BasicActivity implements Callback {

	private Player m_oPlayerSDK = null;

	private HCNetSDK m_oHCNetSDK = null;
	private TextView mCurVideoText = null;
	private ListView mDeviceListView = null;
	private ImageView mLeftBtn = null;
	private ImageView mRightBtn = null;
	private ImageView mTopBtn = null;
	private ImageView mBottomBtn = null;
	private ImageView mCenterBtn = null;
	private Button mFirstBtn = null;
	private Button mSecondBtn = null;
	private Button mManagerBtn = null;
	private Button mPlayBackBtn = null;
	private SurfaceView m_osurfaceView = null;
	private SurfaceView m_osurfaceView1 = null;

	private DeviceItemAdapter mAdapter = null;
	private List<AccountInfo> mAccountList = null;
	private DatabaseService mDBService = null;

	private NET_DVR_DEVICEINFO_V30 m_oNetDvrDeviceInfoV30 = null;

	private int m_iLogID = -1; // return by NET_DVR_Login_v30

	private int m_iPlayID = -1; // return by NET_DVR_RealPlay_V30
	private int m_iPlayID1 = -1; // return by NET_DVR_RealPlay_V30

	private int m_iPlaybackID = -1; // return by NET_DVR_PlayBackByTime

	private byte m_byGetFlag = 1; // 1-get net cfg, 0-set net cfg

	private int m_iPort = -1; // play port
	private int m_iPort1 = -1; // play port

	private int m_iVoiceID = -1; // return by NET_DVR_StartVoiceCom_MR_V30

	private NET_DVR_NETCFG_V30 NetCfg = new NET_DVR_NETCFG_V30(); // netcfg
																	// struct

	private final String TAG = "DemoActivity";
	private AccountInfo mCurAccountInfo = null;

	/***************************************** add by fujun **********************************************/
	// arrays for playId, port and surfaceViews
	private int[] m_iPlayIDs = new int[9];
	private int[] m_iPorts = new int[9];
	private ArrayList<SurfaceView> m_osurfaceViews = new ArrayList<SurfaceView>(
			9);
	// hou much channels, which can be 1, 4, 9
	private int m_iChanNum;
	// pref 相关的常量，用于记录信息
	private static final String PREF_NAME = "shared_prefs";
	private static final String PREF_CHANNEL_NUM = "pref_channel_num";

	/********************************************************************************************************/

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		
//		Log.i(TAG, "surface is created" + m_iPort);
//		Surface surface = holder.getSurface();
//		if (null != m_oPlayerSDK && true == surface.isValid()) {
//			if (false == m_oPlayerSDK.setVideoWindow(m_iPort, 0, surface)) {
//				Log.e(TAG, "Player setVideoWindow failed!");
//			}
//		}
		 
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		/*
		 * Log.i(TAG, "Player setVideoWindow release!" + m_iPort); if (null !=
		 * m_oPlayerSDK && true == holder.getSurface().isValid()) { if (false ==
		 * m_oPlayerSDK.setVideoWindow(m_iPort, 0, null)) { Log.e(TAG,
		 * "Player setVideoWindow failed!"); } }
		 */
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("m_iPort", m_iPort);
		super.onSaveInstanceState(outState);
		Log.i(TAG, "onSaveInstanceState");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		m_iPort = savedInstanceState.getInt("m_iPort");
		m_oPlayerSDK = Player.getInstance();
		super.onRestoreInstanceState(savedInstanceState);
		Log.i(TAG, "onRestoreInstanceState");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.new_main_activity);

		if (!initeSdk()) {
			this.finish();
			return;
		}
		
		m_iChanNum = getIntent().getIntExtra(SelectChannelNumActivity.CHANNEL_NUM_EXTRA, 1);

		initUI();
		addListener();

		registerBroadcast();
		showNetDlg();

		ongetIp();
	}

	private void ongetIp() {

		new Thread() {
			@Override
			public void run() {

				try {
					InetAddress addr = InetAddress.getByName("www.baidu.com");
					String ip = addr.getHostAddress();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}.start();
	}

	private void showNetDlg() {
		if (!NetWorkMonitor.isConnect(this)) {
			NetWorkMonitor.showSettingDlg(this);
		}
	}

	/**
	 * 初始化UI
	 */
	private void initUI() {

		mCurVideoText = (TextView) findViewById(R.id.sufaceview_content_view_id);
		/*
		 * m_osurfaceView = (SurfaceView) findViewById(R.id.surfaceview_id);
		 * m_osurfaceView1 = (SurfaceView) findViewById(R.id.surfaceview_id1);
		 */
		mDeviceListView = (ListView) findViewById(R.id.list_view_id);
		mFirstBtn = (Button) findViewById(R.id.button1_id);
		mSecondBtn = (Button) findViewById(R.id.button2_id);
		mManagerBtn = (Button) findViewById(R.id.manager_btn_id);
		mPlayBackBtn = (Button) findViewById(R.id.playback_btn_id);
		mLeftBtn = (ImageView) findViewById(R.id.center_left_btn);
		mRightBtn = (ImageView) findViewById(R.id.center_right_btn);
		mTopBtn = (ImageView) findViewById(R.id.center_up_btn);
		mBottomBtn = (ImageView) findViewById(R.id.center_down_btn);
		mCenterBtn = (ImageView) findViewById(R.id.center_btn);

		mDBService = new DatabaseService(this);

		mAccountList = new ArrayList<AccountInfo>();
		mAdapter = new DeviceItemAdapter(this, mAccountList);
		mDeviceListView.setAdapter(mAdapter);

		// m_osurfaceView.getHolder().addCallback(this);

		initLayout();

		getDeviceAccount();
	}

	private void addListener() {

		mFirstBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// if (null != mAccountList && !mAccountList.isEmpty()) {
				// login(mAccountList.get(0));
				// }

				startOpenDoor();
			}
		});

		// 对讲
		mSecondBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				startVoiceConnect();
			}
		});

		// PlayBack
		mPlayBackBtn.setOnClickListener(Playback_Listener);
		mManagerBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this,
						ManagerActivity.class));
			}
		});

		mLeftBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onTranLeft();
			}
		});

		mRightBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onTranRight();
			}
		});

		mTopBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onTranUp();
			}
		});

		mBottomBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onTranDown();
			}
		});

		mCenterBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onStopTranPos();
			}
		});

		mDeviceListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (null != mAccountList && !mAccountList.isEmpty()) {
					login(mAccountList.get(arg2));
				}
			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unRegisterBroadcast();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	/**
	 * @fn initeSdk
	 * @author huyf
	 * @brief SDK init
	 * @param NULL
	 *            [in]
	 * @param NULL
	 *            [out]
	 * @return true - success;false - fail
	 */
	private boolean initeSdk() {
		// get an instance and init net sdk
		m_oHCNetSDK = new HCNetSDK();
		if (null == m_oHCNetSDK) {
			Log.e(TAG, "m_oHCNetSDK new is failed!");
			return false;
		}

		if (!m_oHCNetSDK.NET_DVR_Init()) {
			Log.e(TAG, "HCNetSDK init is failed!");
			return false;
		}

		// init player
		m_oPlayerSDK = Player.getInstance();
		if (m_oPlayerSDK == null) {
			Log.e(TAG, "PlayCtrl getInstance failed!");
			return false;
		}

		return true;
	}

	// playback listener
	private Button.OnClickListener Playback_Listener = new Button.OnClickListener() {
		public void onClick(View v) {
			try {
				if (m_iLogID < 0) {
					Log.e(TAG, "please login on device first");
					return;
				}

				if (!NetWorkMonitor.isConnect(MainActivity.this)) {
					NetWorkMonitor.showSettingDlg(MainActivity.this);
					return;
				}

				if (m_iPlaybackID < 0) {
					// onStartPlayback();
					onGotoPlaybackSet();
				} else {
					onStopPlayback();
				}
			} catch (Exception err) {
				Log.e(TAG, "error: " + err.toString());
			}
		}
	};

	private void onGotoPlaybackSet() {

		Intent intent = new Intent(this, PlayBackSetActivity.class);
		intent.putExtra("deviceInfo", mCurAccountInfo);
		startActivityForResult(intent, 10000);
	}

	private Calendar mStart, mEnd;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 10000 && resultCode == RESULT_OK) {

			mStart = (Calendar) data.getSerializableExtra("startTimer");
			mEnd = (Calendar) data.getSerializableExtra("endTimer");
			onStartPlayback();
		}
	}

	/***
	 * 开始回放
	 */
	private void onStartPlayback() {

		if (m_iPlayID >= 0) {
			Log.i(TAG, "Please stop preview first");
			stopPlay();
			// return;
		}
		PlaybackCallBack fPlaybackCallBack = getPlayerbackPlayerCbf();
		if (fPlaybackCallBack == null) {
			Log.e(TAG, "fPlaybackCallBack object is failed!");
			return;
		}
		NET_DVR_TIME struBegin = getStartPlaybackTimer();
		NET_DVR_TIME struEnd = getEndPlaybackTimer();

		m_iPlaybackID = m_oHCNetSDK.NET_DVR_PlayBackByTime(m_iLogID, 1,
				struBegin, struEnd);
		if (m_iPlaybackID >= 0) {

			if (!m_oHCNetSDK.NET_DVR_SetPlayDataCallBack(m_iPlaybackID,
					fPlaybackCallBack)) {
				Log.e(TAG, "Set playback callback failed!");
				return;
			}
			NET_DVR_PLAYBACK_INFO struPlaybackInfo = null;
			if (!m_oHCNetSDK.NET_DVR_PlayBackControl_V40(m_iPlaybackID,
					HCNetSDK.NET_DVR_PLAYSTART, null, 0, struPlaybackInfo)) {
				Log.e(TAG, "net sdk playback start failed!");
				return;
			}
			mPlayBackBtn.setText("停止");
		} else {
			Log.i(TAG, "NET_DVR_PlayBackByTime failed, error code: "
					+ m_oHCNetSDK.NET_DVR_GetLastError());
		}
	}

	/**
	 * <pre>
	 * 获取回放开始时间
	 * </pre>
	 * 
	 * @return
	 */
	private NET_DVR_TIME getStartPlaybackTimer() {

		NET_DVR_TIME struBegin = new NET_DVR_TIME();
		// Date startDate = new Date(new Date().getTime() - 24 * 60 * 60 *
		// 1000);
		struBegin.dwYear = mStart.get(Calendar.YEAR);
		struBegin.dwMonth = mStart.get(Calendar.MONTH) + 1;
		struBegin.dwDay = mStart.get(Calendar.DAY_OF_MONTH);
		struBegin.dwHour = mStart.get(Calendar.HOUR_OF_DAY);
		struBegin.dwMinute = mStart.get(Calendar.MINUTE);
		struBegin.dwSecond = 0;
		return struBegin;
	}

	/**
	 * <pre>
	 * 获取回放终止时间
	 * </pre>
	 * 
	 * @return
	 */
	private NET_DVR_TIME getEndPlaybackTimer() {

		NET_DVR_TIME struEnd = new NET_DVR_TIME();
		// Date endDate = new Date();
		struEnd.dwYear = mEnd.get(Calendar.YEAR);
		struEnd.dwMonth = mEnd.get(Calendar.MONTH) + 1;
		struEnd.dwDay = mEnd.get(Calendar.DAY_OF_MONTH);
		struEnd.dwHour = mEnd.get(Calendar.HOUR_OF_DAY);
		struEnd.dwMinute = mEnd.get(Calendar.MINUTE);
		struEnd.dwSecond = 0;
		return struEnd;
	}

	/***
	 * 停止回放
	 */
	private void onStopPlayback() {

		if (m_iPlaybackID == -1 || m_iPort == -1)
			return;

		if (!m_oHCNetSDK.NET_DVR_StopPlayBack(m_iPlaybackID)) {
			Log.e(TAG, "net sdk stop playback failed");
			return;
		}
		// player stop play
		if (m_iPort == -1 || !m_oPlayerSDK.stop(m_iPort)) {
			Log.e(TAG, "player_stop is failed!");
			return;
		}
		if (!m_oPlayerSDK.closeStream(m_iPort)) {
			Log.e(TAG, "closeStream is failed!");
			return;
		}
		if (!m_oPlayerSDK.freePort(m_iPort)) {
			Log.e(TAG, "freePort is failed!");
			return;
		}
		m_iPort = -1;
		mPlayBackBtn.setText("回放");
		m_iPlaybackID = -1;
	}

	/**
	 * 登录
	 */
	private void login(final AccountInfo account) {

		if (null == account)
			return;

		if (!NetWorkMonitor.isConnect(this)) {
			NetWorkMonitor.showSettingDlg(this);
			return;
		}

		mCurAccountInfo = account;

		if (!TextUtils.isEmpty(account.deviceName))
			mCurVideoText.setText(account.deviceName);

		if (m_iLogID < 0) {
			if (logInCurAccount(account))
				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						onPreView();
						return null;
					}
				}.execute();

			// if (!m_oHCNetSDK.NET_DVR_Logout_V30(m_iLogID)) {
			// Log.e(TAG, " NET_DVR_Logout is failed!");
			// return;
			// } else {
			// stopPlay();
			// if (logInCurAccount(account))
			// onPreView();
			// }
		}
	}

	/**
	 * 预览
	 */
	private void onPreView() {
		// 屏蔽了之前的代码。修改为顺次显示。 add by fujun.

		try {
			if (m_iLogID < 0) {
				Log.e(TAG, "please login on device first");
				return;
			}

			// get start channel no
			int iFirstChannelNo = m_oNetDvrDeviceInfoV30.byStartChan;
			Log.i(TAG, "iFirstChannelNo:" + iFirstChannelNo);

			for (int i = 0; i < m_iChanNum; i++) {
				Log.e("fujun", "m_iChanNum: " + m_iChanNum);
				RealPlayCallBack fRealDataCallBack = getRealPlayerCbf();
				if (fRealDataCallBack == null) {
					Log.e(TAG, "fRealDataCallBack object is failed!");
					return;
				}

				NET_DVR_CLIENTINFO ClientInfo = new NET_DVR_CLIENTINFO();
				ClientInfo.lChannel = iFirstChannelNo + i; // start channel no +
															// preview channel
				ClientInfo.lLinkMode = (1 << 31); // bit 31 -- 0,main
													// stream;1,sub stream
				// bit 0~30 -- link type,0-TCP;1-UDP;2-multicast;3-RTP
				ClientInfo.sMultiCastIP = null;

				// net sdk start preview
				m_iPlayIDs[i] = m_oHCNetSDK.NET_DVR_RealPlay_V30(m_iLogID,
						ClientInfo, fRealDataCallBack, true);
				if (m_iPlayIDs[i] < 0) {
					Log.e(TAG,
							"NET_DVR_RealPlay is failed!Err:"
									+ m_oHCNetSDK.NET_DVR_GetLastError() + i);
					return;
				}
			}
			/*
			 * if (m_iPlayID < 0) { if (m_iPlaybackID >= 0) { Log.i(TAG,
			 * "Please stop palyback first"); onStopPlayback(); // 停止回放 //
			 * return; } RealPlayCallBack fRealDataCallBack =
			 * getRealPlayerCbf(); if (fRealDataCallBack == null) { Log.e(TAG,
			 * "fRealDataCallBack object is failed!"); return; } // set channel
			 * NO. add by fujun int iFirstChannelNo =
			 * m_oNetDvrDeviceInfoV30.byStartChan; get start channel no
			 * 
			 * 
			 * Log.i(TAG, "iFirstChannelNo:" + iFirstChannelNo);
			 * 
			 * NET_DVR_CLIENTINFO ClientInfo = new NET_DVR_CLIENTINFO();
			 * ClientInfo.lChannel = iFirstChannelNo; // start channel no + //
			 * preview channel ClientInfo.lLinkMode = (1 << 31); // bit 31 --
			 * 0,main // stream;1,sub stream // bit 0~30 -- link
			 * type,0-TCP;1-UDP;2-multicast;3-RTP ClientInfo.sMultiCastIP =
			 * null;
			 * 
			 * // net sdk start preview m_iPlayID =
			 * m_oHCNetSDK.NET_DVR_RealPlay_V30(m_iLogID, ClientInfo,
			 * fRealDataCallBack, true); if (m_iPlayID < 0) { Log.e(TAG,
			 * "NET_DVR_RealPlay is failed!Err:" +
			 * m_oHCNetSDK.NET_DVR_GetLastError()); return; }
			 * 
			 * Log.i(TAG,
			 * "NetSdk Play sucess ***********************3***************************"
			 * ); } else if (m_iPlayID1 < 0) { Log.e("fujun", "in 1"); if
			 * (m_iPlaybackID >= 0) { Log.i(TAG, "Please stop palyback first");
			 * onStopPlayback(); // 停止回放 // return; } RealPlayCallBack
			 * fRealDataCallBack = getRealPlayerCbf(); if (fRealDataCallBack ==
			 * null) { Log.e(TAG, "fRealDataCallBack object is failed!");
			 * return; } // set channel NO. add by fujun int iFirstChannelNo =
			 * m_oNetDvrDeviceInfoV30.byStartChan; get start channel no
			 * 
			 * 
			 * Log.i(TAG, "iFirstChannelNo:" + iFirstChannelNo);
			 * 
			 * NET_DVR_CLIENTINFO ClientInfo = new NET_DVR_CLIENTINFO();
			 * ClientInfo.lChannel = iFirstChannelNo + 1; // start channel no +
			 * // preview channel ClientInfo.lLinkMode = (1 << 31); // bit 31 --
			 * 0,main // stream;1,sub stream // bit 0~30 -- link
			 * type,0-TCP;1-UDP;2-multicast;3-RTP ClientInfo.sMultiCastIP =
			 * null;
			 * 
			 * // net sdk start preview m_iPlayID1 =
			 * m_oHCNetSDK.NET_DVR_RealPlay_V30(m_iLogID, ClientInfo,
			 * fRealDataCallBack, true); if (m_iPlayID1 < 0) { Log.e(TAG,
			 * "NET_DVR_RealPlay is failed!Err:" +
			 * m_oHCNetSDK.NET_DVR_GetLastError()); return; }
			 * 
			 * Log.i(TAG,
			 * "NetSdk Play sucess ***********************3***************************"
			 * ); }
			 */
		} catch (Exception err) {
			Log.e(TAG, "error: " + err.toString());
		}
	}

	// // login listener
	// private Button.OnClickListener Login_Listener = new
	// Button.OnClickListener() {
	// public void onClick(View v) {
	// try {
	// if (m_iLogID < 0) {
	// // login on the device
	// m_iLogID = loginDevice();
	// if (m_iLogID < 0) {
	// Log.e(TAG, "This device logins failed!");
	// return;
	// }
	// // get instance of exception callback and set
	// ExceptionCallBack oexceptionCbf = getExceptiongCbf();
	// if (oexceptionCbf == null) {
	// Log.e(TAG, "ExceptionCallBack object is failed!");
	// return;
	// }
	//
	// if (!m_oHCNetSDK.NET_DVR_SetExceptionCallBack(oexceptionCbf)) {
	// Log.e(TAG, "NET_DVR_SetExceptionCallBack is failed!");
	// return;
	// }
	//
	// // m_oLoginBtn.setText("Logout");
	// Log.i(TAG,
	// "Login sucess ****************************1***************************");
	// } else {
	// // whether we have logout
	// if (!m_oHCNetSDK.NET_DVR_Logout_V30(m_iLogID)) {
	// Log.e(TAG, " NET_DVR_Logout is failed!");
	// return;
	// }
	// // m_oLoginBtn.setText("Logon");
	// m_iLogID = -1;
	// }
	// } catch (Exception err) {
	// Log.e(TAG, "error: " + err.toString());
	// }
	// }
	// };
	//
	// // Preview listener
	// private Button.OnClickListener Preview_Listener = new
	// Button.OnClickListener() {
	// public void onClick(View v) {
	// try {
	// if (m_iLogID < 0) {
	// Log.e(TAG, "please login on device first");
	// return;
	// }
	// if (m_iPlayID < 0) {
	// if (m_iPlaybackID >= 0) {
	// Log.i(TAG, "Please stop palyback first");
	// return;
	// }
	// RealPlayCallBack fRealDataCallBack = getRealPlayerCbf();
	// if (fRealDataCallBack == null) {
	// Log.e(TAG, "fRealDataCallBack object is failed!");
	// return;
	// }
	//
	// int iFirstChannelNo = m_oNetDvrDeviceInfoV30.byStartChan;// get
	// // start
	// // channel
	// // no
	//
	// Log.i(TAG, "iFirstChannelNo:" + iFirstChannelNo);
	//
	// NET_DVR_CLIENTINFO ClientInfo = new NET_DVR_CLIENTINFO();
	// ClientInfo.lChannel = iFirstChannelNo; // start channel no +
	// // preview channel
	// ClientInfo.lLinkMode = (1 << 31); // bit 31 -- 0,main
	// // stream;1,sub stream
	// // bit 0~30 -- link type,0-TCP;1-UDP;2-multicast;3-RTP
	// ClientInfo.sMultiCastIP = null;
	//
	// // net sdk start preview
	// m_iPlayID = m_oHCNetSDK.NET_DVR_RealPlay_V30(m_iLogID, ClientInfo,
	// fRealDataCallBack, true);
	// if (m_iPlayID < 0) {
	// Log.e(TAG, "NET_DVR_RealPlay is failed!Err:" +
	// m_oHCNetSDK.NET_DVR_GetLastError());
	// return;
	// }
	//
	// Log.i(TAG,
	// "NetSdk Play sucess ***********************3***************************");
	//
	// // mThirdBtn.setText("停止");
	// // m_oPreviewBtn.setText("Stop");
	// } else {
	// stopPlay();
	// // m_oPreviewBtn.setText("Preview");
	// // mThirdBtn.setText("预览");
	// }
	// } catch (Exception err) {
	// Log.e(TAG, "error: " + err.toString());
	// }
	// }
	// };
	//
	// // configuration listener
	// private Button.OnClickListener ParamCfg_Listener = new
	// Button.OnClickListener() {
	// public void onClick(View v) {
	// try {
	// paramCfg(m_iLogID);
	// } catch (Exception err) {
	// Log.e(TAG, "error: " + err.toString());
	// }
	// }
	// };

	/**
	 * @fn stopPlay
	 * @author huyf
	 * @brief stop preview
	 * @param NULL
	 *            [in]
	 * @param NULL
	 *            [out]
	 * @return NULL
	 */
	// modified by fujun. 修改为数组循环
	private void stopPlay() {
		for (int i = 0; i < 9; i++) {
			if (m_iPlayIDs[i] >= 0) {
				if (m_oHCNetSDK.NET_DVR_StopRealPlay(m_iPlayIDs[i])) {
					if (!m_oPlayerSDK.stop(m_iPorts[i])) {
						if (!m_oPlayerSDK.closeStream(m_iPorts[i])) {
							if (!m_oPlayerSDK.freePort(m_iPorts[i])) {
								m_iPorts[i] = -1;
								m_iPlayIDs[i] = -1;
							}
						}
					}
				}

			}
		}

		// net sdk stop preview

		// player stop play

	}

	private boolean logInCurAccount(AccountInfo account) {

		if (null == account) {
			showErrorToast("账户信息为空!");
			return false;
		}

		if (TextUtils.isEmpty(account.ipAddr)) {
			showErrorToast("IP地址为空!");
			return false;
		}

		if (TextUtils.isEmpty(account.userName)) {
			showErrorToast("账户名称为空!");
			return false;
		}

		if (TextUtils.isEmpty(account.password)) {
			showErrorToast("账户密码为空!");
			return false;
		}

		if (null == m_oNetDvrDeviceInfoV30)
			m_oNetDvrDeviceInfoV30 = new NET_DVR_DEVICEINFO_V30();
		if (null == m_oNetDvrDeviceInfoV30) {
			Log.e(TAG, "HKNetDvrDeviceInfoV30 new is failed!");
			return false;
		}

		int nPort = 8000;

		if (!TextUtils.isEmpty(account.port))
			nPort = Integer.valueOf(account.port);

		m_iLogID = m_oHCNetSDK.NET_DVR_Login_V30(account.ipAddr, nPort,
				account.userName, account.password, m_oNetDvrDeviceInfoV30);
		if (m_iLogID < 0) {
			Log.e(TAG,
					"NET_DVR_Login is failed!Err:"
							+ m_oHCNetSDK.NET_DVR_GetLastError());
			return false;
		}
		return true;
	}

	private void showErrorToast(final String str) {
		if (TextUtils.isEmpty(str))
			return;

		Toast toast = Toast.makeText(this, str, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	/**
	 * @fn loginDevice
	 * @author huyf
	 * @brief login on device
	 * @param NULL
	 *            [in]
	 * @param NULL
	 *            [out]
	 * @return login ID
	 */
	private int loginDevice() {
		// get instance
		m_oNetDvrDeviceInfoV30 = new NET_DVR_DEVICEINFO_V30();
		if (null == m_oNetDvrDeviceInfoV30) {
			Log.e(TAG, "HKNetDvrDeviceInfoV30 new is failed!");
			return -1;
		}

		Log.i(TAG, "NET_DVR_Login is Successful!");

		return 1;
	}

	/**
	 * @fn paramCfg
	 * @author huyf
	 * @brief configuration
	 * @param iUserID
	 *            - login ID [in]
	 * @param NULL
	 *            [out]
	 * @return NULL
	 */
	private void paramCfg(final int iUserID) {
		// whether have logined on
		if (iUserID > 0) {
			if (m_byGetFlag == 1) {

				if (!m_oHCNetSDK.NET_DVR_GetDVRConfig(iUserID,
						HCNetSDK.NET_DVR_GET_NETCFG_V30, 0, NetCfg)) {

					Log.e(TAG,
							"get net cfg faied!" + " err: "
									+ m_oHCNetSDK.NET_DVR_GetLastError());
				} else {
					Log.i(TAG, "get net cfg succ!");
					String strIP = new String(NetCfg.struDnsServer1IpAddr.sIpV4);

					m_byGetFlag = 0;
				}
			} else {
				// byte [] byIP = m_oDNSServer1.getText().toString().getBytes();
				byte[] byIP = null;
				NetCfg.struDnsServer1IpAddr.sIpV4 = new byte[16];
				System.arraycopy(byIP, 0, NetCfg.struDnsServer1IpAddr.sIpV4, 0,
						byIP.length);
				if (!m_oHCNetSDK.NET_DVR_SetDVRConfig(iUserID,
						HCNetSDK.NET_DVR_SET_NETCFG_V30, 0, NetCfg)) {
					Log.e(TAG,
							"Set net cfg faied!" + " err: "
									+ m_oHCNetSDK.NET_DVR_GetLastError());
				} else {
					Log.i(TAG, "Set net cfg succ!");
					// m_oParamCfgBtn.setText("Get Netcfg");
					m_byGetFlag = 1;
				}
			}
		}

	}

	/**
	 * @fn getRealPlayerCbf
	 * @author huyf
	 * @brief get realplay callback instance
	 * @param NULL
	 *            [in]
	 * @param NULL
	 *            [out]
	 * @return callback instance
	 */
	private RealPlayCallBack getRealPlayerCbf() {
		RealPlayCallBack cbf = new RealPlayCallBack() {
			public void fRealDataCallBack(int iRealHandle, int iDataType,
					byte[] pDataBuffer, int iDataSize) {
				// player channel 1
				processRealData(iRealHandle, iDataType, pDataBuffer, iDataSize,
						Player.STREAM_REALTIME);
			}
		};
		return cbf;
	}

	/**
	 * @fn getPlayerbackPlayerCbf
	 * @author Jerry
	 * @brief get Playback instance
	 * @param NULL
	 *            [in]
	 * @param NULL
	 *            [out]
	 * @return callback instance
	 */
	private PlaybackCallBack getPlayerbackPlayerCbf() {
		PlaybackCallBack cbf = new PlaybackCallBack() {
			@Override
			public void fPlayDataCallBack(int iPlaybackHandle, int iDataType,
					byte[] pDataBuffer, int iDataSize) {
				// player channel 1
				processRealData(1, iDataType, pDataBuffer, iDataSize,
						Player.STREAM_FILE);
			}
		};
		return cbf;
	}

	/**
	 * @fn processRealData
	 * @author huyf
	 * @brief process real data
	 * @param iPlayViewNo
	 *            - player channel [in]
	 * @param iDataType
	 *            - data type [in]
	 * @param pDataBuffer
	 *            - data buffer [in]
	 * @param iDataSize
	 *            - data size [in]
	 * @param iStreamMode
	 *            - stream mode [in]
	 * @param NULL
	 *            [out]
	 * @return NULL
	 */
	public void processRealData(int iPlayViewNo, int iDataType,
			byte[] pDataBuffer, int iDataSize, int iStreamMode) {
		int i = 0;
		try {
			switch (iDataType) {
			case HCNetSDK.NET_DVR_SYSHEAD:
				// if (m_iPorts[iPlayViewNo] >= 0) {
				// break;
				// }
				m_iPorts[iPlayViewNo] = m_oPlayerSDK.getPort();
				if (m_iPorts[iPlayViewNo] == -1) {
					Log.e(TAG, "getPort is failed!");
					break;
				}
				if (iDataSize > 0) {
					if (!m_oPlayerSDK.setStreamOpenMode(m_iPorts[iPlayViewNo],
							iStreamMode)) // set
					// stream
					// mode
					{
						Log.e(TAG, "setStreamOpenMode failed");
						break;
					}
					if (!m_oPlayerSDK.setSecretKey(m_iPorts[iPlayViewNo], 1,
							"ge_security_3477".getBytes(), 128)) {
						Log.e(TAG, "setSecretKey failed");
						break;
					}
					if (!m_oPlayerSDK.openStream(m_iPorts[iPlayViewNo],
							pDataBuffer, iDataSize, 2 * 1024 * 1024)) // open
					// stream
					{
						Log.e(TAG, "openStream failed");
						break;
					}

					if (!m_oPlayerSDK.play(m_iPorts[iPlayViewNo],
							m_osurfaceViews.get(iPlayViewNo).getHolder()
									.getSurface())) {
						Log.e(TAG, "play failed");
						break;
					}
				}
				break;
			case HCNetSDK.NET_DVR_STREAMDATA:
			case HCNetSDK.NET_DVR_STD_AUDIODATA:
			case HCNetSDK.NET_DVR_STD_VIDEODATA:
				if (iDataSize > 0 && m_iPorts[iPlayViewNo] != -1) {
					for (i = 0; i < 400; i++) {
						if (m_oPlayerSDK.inputData(m_iPorts[iPlayViewNo],
								pDataBuffer, iDataSize)) {
							break;
						}
						Thread.sleep(10);
					}
					if (i == 400) {
						Log.e(TAG, "inputData failed");
					}
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			Log.e(TAG, "processRealData Exception!err:" + e.toString());
		}
	}

	/**
	 * @fn Cleanup
	 * @author huyf
	 * @brief cleanup
	 * @param NULL
	 *            [in]
	 * @param NULL
	 *            [out]
	 * @return NULL
	 */
	public void Cleanup() {
		// release player resource

		m_oPlayerSDK.freePort(m_iPort);

		// release net SDK resource
		m_oHCNetSDK.NET_DVR_Cleanup();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:

			stopPlay();
			Cleanup();
			android.os.Process.killProcess(android.os.Process.myPid());
			break;
		default:
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 0:
				mDeviceListView.requestLayout();
				if (null != mAdapter)
					mAdapter.notifyDataSetChanged();
				break;
			case 1:
				Toast.makeText(MainActivity.this, "RealPlayCallBack", 1).show();
				break;
			case 2:
				Toast.makeText(MainActivity.this, "PlaybackCallBack", 1).show();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	private void getDeviceAccount() {

		new Thread() {
			@Override
			public void run() {
				synchronized (ManagerActivity.class) {
					mAccountList.clear();
					List<AccountInfo> temp = mDBService.getAccountList();
					if (null != temp && temp.isEmpty() == false) {
						mAccountList.addAll(temp);
						if (null != mHandler)
							mHandler.sendEmptyMessage(0);
						return;
					}
				}
			}
		}.start();
	}

	private BroadcastReceiver mBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.hasExtra("refresh")) {
				int nFlag = intent.getIntExtra("refresh", -1);
				if (nFlag == 1) { // 账户增加
					getDeviceAccount();
				} else if (nFlag == 2) { // 账户删除
					AccountInfo accountInfo = (AccountInfo) intent
							.getSerializableExtra("accountinfo");
					if (null != accountInfo) {
						if (null != mCurAccountInfo
								&& mCurAccountInfo.equals(accountInfo)) {
							stopPlay();
							getDeviceAccount();
						}
					}
				}
			}
		}
	};

	public void registerBroadcast() {

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("refresh");
		registerReceiver(mBroadcast, intentFilter);
	}

	public void unRegisterBroadcast() {
		unregisterReceiver(mBroadcast);
	}

	// ////////////////////////////////////////////////////////////////////
	// ////////////////////////移动摄像头的角度和位置///////////////////////

	private int mCurPTZCommand = -1;

	/**
	 * 停止摄像头转动
	 */
	private void onStopTranPos() {

		if (null == m_oHCNetSDK || m_iPlayID == -1)
			return;

		if (mCurPTZCommand != -1)
			m_oHCNetSDK.NET_DVR_PTZControl(m_iPlayID, mCurPTZCommand, 1);
		mCurPTZCommand = -1;
	}

	/***
	 * 云台左转
	 */
	private void onTranLeft() {
		if (null == m_oHCNetSDK || m_iPlayID == -1)
			return;

		if (mCurPTZCommand != -1)
			m_oHCNetSDK.NET_DVR_PTZControl(m_iPlayID, mCurPTZCommand, 1);
		m_oHCNetSDK.NET_DVR_PTZControl(m_iPlayID, PTZCommand.PAN_LEFT, 0);
		mCurPTZCommand = PTZCommand.PAN_LEFT;
	}

	/***
	 * 云台右转
	 */
	private void onTranRight() {

		if (null == m_oHCNetSDK || m_iPlayID == -1)
			return;
		if (mCurPTZCommand != -1)
			m_oHCNetSDK.NET_DVR_PTZControl(m_iPlayID, mCurPTZCommand, 1);
		m_oHCNetSDK.NET_DVR_PTZControl(m_iPlayID, PTZCommand.PAN_RIGHT, 0);
		mCurPTZCommand = PTZCommand.PAN_RIGHT;
	}

	/**
	 * 云台上仰
	 */
	private void onTranUp() {
		if (null == m_oHCNetSDK || m_iPlayID == -1)
			return;
		if (mCurPTZCommand != -1)
			m_oHCNetSDK.NET_DVR_PTZControl(m_iPlayID, mCurPTZCommand, 1);
		m_oHCNetSDK.NET_DVR_PTZControl(m_iPlayID, PTZCommand.TILT_UP, 0);
		mCurPTZCommand = PTZCommand.TILT_UP;
	}

	/**
	 * 云台下俯
	 */
	private void onTranDown() {
		if (null == m_oHCNetSDK || m_iPlayID == -1)
			return;
		if (mCurPTZCommand != -1)
			m_oHCNetSDK.NET_DVR_PTZControl(m_iPlayID, mCurPTZCommand, 1);
		m_oHCNetSDK.NET_DVR_PTZControl(m_iPlayID, PTZCommand.TILT_DOWN, 0);
		mCurPTZCommand = PTZCommand.TILT_DOWN;
	}

	// ////////////////////////////////////////////////////

	/**
	 * 开门
	 */
	private void startOpenDoor() {

		if (m_iLogID < 0) {
			Log.e(TAG, "please login on device first");
			showErrorToast("请先选择一个设备");
			return;
		}

		boolean bFlag = m_oHCNetSDK.NET_DVR_SetAlarmOut(m_iLogID, 1, 1);
		if (bFlag) {
			try {
				Thread.sleep(2 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		m_oHCNetSDK.NET_DVR_SetAlarmOut(m_iLogID, 1, 0);
	}

	/**
	 * 开始进行语音对讲
	 */
	private void startVoiceConnect() {

		if (m_iLogID < 0) {
			Log.e(TAG, "please login on device first");
			showErrorToast("请先选择一个设备");
			return;
		}

		if (m_iVoiceID >= 0) {
			stopVoiceConnect();
			return;
		}

		VoiceDataCallBack voiceCallback = getVoiceDataCallBack();
		if (null == voiceCallback) {
			Log.e(TAG, "get voiceData Call Back error");
			return;
		}
		m_iVoiceID = m_oHCNetSDK.NET_DVR_StartVoiceCom_MR_V30(m_iLogID, 1,
				voiceCallback);
		if (m_iVoiceID < 0) {
			Log.e(TAG, "create voice handler error");
			return;
		}

		mSecondBtn.setText("停止");
	}

	/**
	 * 停止语音对讲
	 */
	private void stopVoiceConnect() {

		if (m_iVoiceID < 0)
			return;

		m_oHCNetSDK.NET_DVR_StopVoiceCom(m_iVoiceID);
		m_iVoiceID = -1;
		mSecondBtn.setText("对讲");
	}

	/**
	 * @param nVoiceHandler
	 *            [] - Voice talk handle, the return value of
	 *            NET_DVR_StartVoiceCom_MR_V30
	 * @param buffer
	 *            [] Voice data buffer
	 * @param buffersize
	 *            [] Buffer size
	 * @param arg3
	 *            [] - Always 1
	 * 
	 * @return
	 */
	private VoiceDataCallBack getVoiceDataCallBack() {

		VoiceDataCallBack voiceCallBack = new VoiceDataCallBack() {
			@Override
			public void fVoiceDataCallBack(int nVoiceHandler, byte[] buffer,
					int buffersize, int arg3) {
				// 此处的音频数据是由设备发送的编码后数据，也可以是本地采集并编码后的数据
				m_oHCNetSDK.NET_DVR_VoiceComSendData(nVoiceHandler, buffer, 80);
			}
		};
		return voiceCallBack;
	}

	/**************************************************** add by fujun ***********************************/

	// 切换场景用
	private void initLayout() {
		
		int startID = 0;
		View one_item = findViewById(R.id.one_item_layout);
		View four_item = findViewById(R.id.four_item_layout);
		View nine_item = findViewById(R.id.nine_item_layout);
		switch (m_iChanNum) {
		case 1:
			four_item.setVisibility(View.GONE);
			nine_item.setVisibility(View.GONE);
			one_item.setVisibility(View.VISIBLE);
			startID = R.id.surfaceview_one_1;
			break;
		case 4:
			one_item.setVisibility(View.GONE);
			nine_item.setVisibility(View.GONE);
			four_item.setVisibility(View.VISIBLE);
			startID = R.id.surfaceview_four_1;
			break;
		case 9:
			one_item.setVisibility(View.GONE);
			four_item.setVisibility(View.GONE);
			nine_item.setVisibility(View.VISIBLE);
			startID = R.id.surfaceview_1;
			break;
		default:
			break;
		}

		m_osurfaceViews.clear();
		for (int i = 0; i < 9; i++) {
			m_iPlayIDs[i] = -1;
			m_iPorts[i] = -1;
		}

		SurfaceView surfaceView = null;
		for (int i = 0; i < m_iChanNum; i++) {
			surfaceView = (SurfaceView) findViewById(startID + i);
			surfaceView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 点击事件
				}
			});
			m_osurfaceViews.add(surfaceView);
		}

	}

	private void setChannelNum(int num) {
		if (num != 1 && num != 4 && num != 9) {
			Log.e(TAG, "setChannelNum: ilegeal ChannelNum " + num);
			return;
		}
		m_iChanNum = num;
		SharedPreferences preferences = getSharedPreferences(PREF_NAME,
				MODE_PRIVATE);
		preferences.edit().putInt(PREF_CHANNEL_NUM, m_iChanNum).commit();
	}

	// menu 键的处理，用来切换通道数。
	private static final int MENU_ONE_ID = 0;
	private static final int MENU_FOUR_ID = 1;
	private static final int MENU_NINE_ID = 2;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ONE_ID, 0, getString(R.string.switch_to_one));
		menu.add(0, MENU_FOUR_ID, 0, getString(R.string.switch_to_four));
		menu.add(0, MENU_NINE_ID, 0, getString(R.string.switch_to_nine));
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		switch (m_iChanNum) {
		case 1:
			menu.findItem(MENU_ONE_ID).setVisible(false);
			menu.findItem(MENU_FOUR_ID).setVisible(true);
			menu.findItem(MENU_NINE_ID).setVisible(true);
			break;
		case 4:
			menu.findItem(MENU_ONE_ID).setVisible(true);
			menu.findItem(MENU_FOUR_ID).setVisible(false);
			menu.findItem(MENU_NINE_ID).setVisible(true);
			break;
		case 9:
			menu.findItem(MENU_ONE_ID).setVisible(true);
			menu.findItem(MENU_FOUR_ID).setVisible(true);
			menu.findItem(MENU_NINE_ID).setVisible(false);
			break;
		default:
			Log.e(TAG, "onPrepareOptionsMenu: ilegeal channelnum " + m_iChanNum);
			break;
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ONE_ID:
			setChannelNum(1);
			break;
		case MENU_FOUR_ID:
			setChannelNum(4);
			break;
		case MENU_NINE_ID:
			setChannelNum(9);
			break;
		default:
			Log.e(TAG,
					"onOptionsItemSelected: ilegeal menuitem "
							+ item.getItemId());
			break;
		}

		stopPlay();
		initLayout();
		onPreView();
		return true;
	}
}
