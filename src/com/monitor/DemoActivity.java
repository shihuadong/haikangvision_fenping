/**
 * <p>DemoActivity Class</p>
 * @author huyf 2011-05-22
 * @version V1.0  
 * @modificationHistory
 * @modify by user: 
 * @modify by reason:
*/
package com.monitor;

import org.MediaPlayer.PlayM4.Player;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_CLIENTINFO;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_NETCFG_V30;
import com.hikvision.netsdk.NET_DVR_PLAYBACK_INFO;
import com.hikvision.netsdk.NET_DVR_TIME;
import com.hikvision.netsdk.PlaybackCallBack;
import com.hikvision.netsdk.RealPlayCallBack;
/**
 * <pre>
 *  ClassName  DemoActivity Class
 * </pre>
 * 
 * @author huyf
 * @version V1.0
 * @modificationHistory
 */
public class DemoActivity extends Activity implements Callback
{
	private Player 			m_oPlayerSDK			= null;
	private HCNetSDK		m_oHCNetSDK				= null;
	
	private Button          m_oLoginBtn         	= null;
	private Button          m_oPreviewBtn           = null;
	private Button			m_oPlaybackBtn			= null;
	private Button			m_oParamCfgBtn			= null;
	private SurfaceView 	m_osurfaceView			= null;
	private EditText		m_oDNSServer1			= null;
	private EditText        m_oIPAddr				= null;
	private EditText		m_oPort					= null;
	private EditText		m_oUser					= null;
	private EditText		m_oPsd					= null;
	
	private NET_DVR_DEVICEINFO_V30 m_oNetDvrDeviceInfoV30 = null;
	
	private int				m_iLogID				= -1;				// return by NET_DVR_Login_v30
	private int 			m_iPlayID				= -1;				// return by NET_DVR_RealPlay_V30
	private int				m_iPlaybackID			= -1;				// return by NET_DVR_PlayBackByTime	
	private byte			m_byGetFlag				= 1;				// 1-get net cfg, 0-set net cfg 
	private int				m_iPort					= -1;				// play port
	private	NET_DVR_NETCFG_V30 NetCfg = new NET_DVR_NETCFG_V30();		//netcfg struct
	
	private final String 	TAG						= "DemoActivity";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if (!initeSdk())
        {
        	this.finish();
        	return;
        }
        
        if (!initeActivity())
        {
        	this.finish();
        	return;
        }
    }
    
    //@Override    
    public void surfaceCreated(SurfaceHolder holder) {  
    	Log.i(TAG, "surface is created" + m_iPort); 
        Surface surface = holder.getSurface();
        if (null != m_oPlayerSDK && true == surface.isValid()) {
        	if (false == m_oPlayerSDK.setVideoWindow(m_iPort, 0, surface)) {	
        		Log.e(TAG, "Player setVideoWindow failed!");
        	}	
    	}        
    }       
        
    //@Override  
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {   
    }  
        
    //@Override  
    public void surfaceDestroyed(SurfaceHolder holder) {  
    	Log.i(TAG, "Player setVideoWindow release!" + m_iPort);
        if (null != m_oPlayerSDK && true == holder.getSurface().isValid()) {
        	if (false == m_oPlayerSDK.setVideoWindow(m_iPort, 0, null)) {	
        		Log.e(TAG, "Player setVideoWindow failed!");
        	}
        }
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
		Log.i(TAG, "onRestoreInstanceState" ); 
	}  
    
    /** 
     * @fn initeSdk
     * @author huyf
     * @brief SDK init
     * @param NULL [in]
     * @param NULL [out]
     * @return true - success;false - fail
     */
    private boolean initeSdk()
	{
		// get an instance and init net sdk
		m_oHCNetSDK = new HCNetSDK();
    	if (null == m_oHCNetSDK)
    	{
    		Log.e(TAG, "m_oHCNetSDK new is failed!");
    		return false;
    	}
    	
    	if (!m_oHCNetSDK.NET_DVR_Init())
    	{
    		Log.e(TAG, "HCNetSDK init is failed!");
    		return false;
    	}
    	
    	// init player
    	m_oPlayerSDK = Player.getInstance();
    	if (m_oPlayerSDK == null)
    	{
    		Log.e(TAG,"PlayCtrl getInstance failed!");
    		return false;
    	}
    	
    	return true;
	}
    

    // GUI init
    private boolean initeActivity()
    {
    	findViews();
    	
    	m_osurfaceView.getHolder().addCallback(this);
    	setListeners();
    	return true;
    }
    
    // get controller instance
    private void findViews()
    {
    	m_oLoginBtn = (Button) findViewById(R.id.btn_Login);
    	m_oPreviewBtn = (Button) findViewById(R.id.btn_Preview);
    	m_oPlaybackBtn = (Button) findViewById(R.id.btn_Playback);
    	m_oParamCfgBtn = (Button) findViewById(R.id.btn_ParamCfg);
    	m_osurfaceView = (SurfaceView) findViewById(R.id.Sur_Player);    	
    	m_oDNSServer1 = (EditText) findViewById(R.id.EDT_DNSServer1);
    	m_oIPAddr = (EditText) findViewById(R.id.EDT_IPAddr);
    	m_oPort = (EditText) findViewById(R.id.EDT_Port);
    	m_oUser = (EditText) findViewById(R.id.EDT_User);
    	m_oPsd = (EditText) findViewById(R.id.EDT_Psd);
    }
    
    // listen
    private void setListeners()
    {
    	m_oLoginBtn.setOnClickListener(Login_Listener);
    	m_oPreviewBtn.setOnClickListener(Preview_Listener);
    	m_oPlaybackBtn.setOnClickListener(Playback_Listener);
    	m_oParamCfgBtn.setOnClickListener(ParamCfg_Listener);
    }
    //playback listener
    private Button.OnClickListener Playback_Listener = new Button.OnClickListener()
    {
		public void onClick(View v)
		{
			try
			{
				if(m_iLogID < 0)
				{
					Log.e(TAG,"please login on device first");
					return ;
				}
				
				if(m_iPlaybackID < 0)
				{					
					if(m_iPlayID >= 0 )
					{
						Log.i(TAG, "Please stop preview first");
						return;
					}
					PlaybackCallBack fPlaybackCallBack = getPlayerbackPlayerCbf();
					if (fPlaybackCallBack == null)
					{
					    Log.e(TAG, "fPlaybackCallBack object is failed!");
			            return;
					}
					NET_DVR_TIME struBegin = new NET_DVR_TIME();
					NET_DVR_TIME struEnd = new NET_DVR_TIME();
					
					struBegin.dwYear = 2012;
					struBegin.dwMonth = 6;
					struBegin.dwDay = 14;
					struBegin.dwHour = 9;
					struBegin.dwMinute = 0;
					struBegin.dwSecond = 0;
					
					struEnd.dwYear = 2012;
					struEnd.dwMonth = 6;
					struEnd.dwDay = 14;
					struEnd.dwHour = 17;
					struEnd.dwMinute = 10;
					struEnd.dwSecond = 0;
					
					m_iPlaybackID = m_oHCNetSDK.NET_DVR_PlayBackByTime(m_iLogID, 1, struBegin, struEnd);
					if(m_iPlaybackID >= 0)
					{
						if(!m_oHCNetSDK.NET_DVR_SetPlayDataCallBack(m_iPlaybackID, fPlaybackCallBack))
						{
							Log.e(TAG, "Set playback callback failed!");
							return ;
						}
						NET_DVR_PLAYBACK_INFO struPlaybackInfo = null ;
						if(!m_oHCNetSDK.NET_DVR_PlayBackControl_V40(m_iPlaybackID, HCNetSDK.NET_DVR_PLAYSTART, null, 0, struPlaybackInfo))
						{
							Log.e(TAG, "net sdk playback start failed!");
							return ;
						}
						m_oPlaybackBtn.setText("Stop");
					}
					else
					{
						Log.i(TAG, "NET_DVR_PlayBackByTime failed, error code: " + m_oHCNetSDK.NET_DVR_GetLastError());
					}
				}
				else
				{
					if(!m_oHCNetSDK.NET_DVR_StopPlayBack(m_iPlaybackID))
					{
						Log.e(TAG, "net sdk stop playback failed");
						return;
					}
					// player stop play
					if (!m_oPlayerSDK.stop(m_iPort)) 
			        {
			            Log.e(TAG, "player_stop is failed!");
			            return;
			        }	
					if(!m_oPlayerSDK.closeStream(m_iPort))
					{
			            Log.e(TAG, "closeStream is failed!");
			            return;
			        }
					if(!m_oPlayerSDK.freePort(m_iPort))
					{
			            Log.e(TAG, "freePort is failed!");
			            return;
			        }
					m_iPort = -1;
					m_oPlaybackBtn.setText("Playback");
					m_iPlaybackID = -1;
				}
			} 
			catch (Exception err)
			{
				Log.e(TAG, "error: " + err.toString());
			}
		}    	
    };
    
    //login listener
    private Button.OnClickListener Login_Listener = new Button.OnClickListener() 
	{
		public void onClick(View v) 
		{
			try
			{
				if(m_iLogID < 0)
				{
					// login on the device
					m_iLogID = loginDevice();
					if (m_iLogID < 0)
					{
						Log.e(TAG, "This device logins failed!");
						return;
					}
					// get instance of exception callback and set
					ExceptionCallBack oexceptionCbf = getExceptiongCbf();
					if (oexceptionCbf == null)
					{
					    Log.e(TAG, "ExceptionCallBack object is failed!");
					    return ;
					}
					
					if (!m_oHCNetSDK.NET_DVR_SetExceptionCallBack(oexceptionCbf))
				    {
				        Log.e(TAG, "NET_DVR_SetExceptionCallBack is failed!");
				        return;
				    }
					
					m_oLoginBtn.setText("Logout");
					Log.i(TAG, "Login sucess ****************************1***************************");
				}
				else
				{
					// whether we have logout
					if (!m_oHCNetSDK.NET_DVR_Logout_V30(m_iLogID))
					{
						Log.e(TAG, " NET_DVR_Logout is failed!");
						return;
					}
					m_oLoginBtn.setText("Logon");
					m_iLogID = -1;
				}		
			} 
			catch (Exception err)
			{
				Log.e(TAG, "error: " + err.toString());
			}
		}
	};
	
	// Preview listener 
    private Button.OnClickListener Preview_Listener = new Button.OnClickListener() 
	{
		public void onClick(View v) 
		{
			try
			{
				if(m_iLogID < 0)
				{
					Log.e(TAG,"please login on device first");
					return ;
				}
				if(m_iPlayID < 0)
				{	
					if(m_iPlaybackID >= 0)
					{
						Log.i(TAG, "Please stop palyback first");
						return;
					}
					RealPlayCallBack fRealDataCallBack = getRealPlayerCbf();
					if (fRealDataCallBack == null)
					{
					    Log.e(TAG, "fRealDataCallBack object is failed!");
			            return;
					}
					
					int iFirstChannelNo = m_oNetDvrDeviceInfoV30.byStartChan;// get start channel no
					
					Log.i(TAG, "iFirstChannelNo:" +iFirstChannelNo);
					
					NET_DVR_CLIENTINFO ClientInfo = new NET_DVR_CLIENTINFO();
			        ClientInfo.lChannel =  iFirstChannelNo; 	// start channel no + preview channel
			        ClientInfo.lLinkMode = (1<<31);  			// bit 31 -- 0,main stream;1,sub stream
			        										// bit 0~30 -- link type,0-TCP;1-UDP;2-multicast;3-RTP 
			        ClientInfo.sMultiCastIP = null;
			        
					// net sdk start preview
			        m_iPlayID = m_oHCNetSDK.NET_DVR_RealPlay_V30(m_iLogID, ClientInfo, fRealDataCallBack, true);
					if (m_iPlayID < 0)
					{
					 	Log.e(TAG, "NET_DVR_RealPlay is failed!Err:" + m_oHCNetSDK.NET_DVR_GetLastError());
					 	return;
					}
					
					Log.i(TAG, "NetSdk Play sucess ***********************3***************************");
										
					m_oPreviewBtn.setText("Stop");
				}
				else
				{
					stopPlay();
					m_oPreviewBtn.setText("Preview");
				}				
			} 
			catch (Exception err)
			{
				Log.e(TAG, "error: " + err.toString());
			}
		}
	};
	 
	// configuration listener
	private Button.OnClickListener ParamCfg_Listener = new Button.OnClickListener() 
	{
		public void onClick(View v)
		{
			try
			{
				paramCfg(m_iLogID);
			}
			catch (Exception err)
			{
				Log.e(TAG, "error: " + err.toString());
			}
		}
	};
	
	/** 
     * @fn stopPlay
     * @author huyf
     * @brief stop preview
     * @param NULL [in]
     * @param NULL [out]
     * @return NULL
     */
	private void stopPlay()
	{
		if ( m_iPlayID < 0)
		{
			Log.e(TAG, "m_iPlayID < 0");
			return;
		}
		
		//  net sdk stop preview
		if (!m_oHCNetSDK.NET_DVR_StopRealPlay(m_iPlayID))
		{
			Log.e(TAG, "StopRealPlay is failed!Err:" + m_oHCNetSDK.NET_DVR_GetLastError());
			return;
		}
		
		// player stop play
		if (!m_oPlayerSDK.stop(m_iPort)) 
        {
            Log.e(TAG, "stop is failed!");
            return;
        }	
		
		if(!m_oPlayerSDK.closeStream(m_iPort))
		{
            Log.e(TAG, "closeStream is failed!");
            return;
        }
		if(!m_oPlayerSDK.freePort(m_iPort))
		{
            Log.e(TAG, "freePort is failed!");
            return;
        }
		m_iPort = -1;
		// set id invalid
		m_iPlayID = -1;		
	}
	
	/** 
     * @fn loginDevice
     * @author huyf
     * @brief login on device
     * @param NULL [in]
     * @param NULL [out]
     * @return login ID
     */
	private int loginDevice()
	{
		// get instance
		m_oNetDvrDeviceInfoV30 = new NET_DVR_DEVICEINFO_V30();
		if (null == m_oNetDvrDeviceInfoV30)
		{
			Log.e(TAG, "HKNetDvrDeviceInfoV30 new is failed!");
			return -1;
		}
		String strIP = m_oIPAddr.getText().toString();
		int	nPort = Integer.parseInt(m_oPort.getText().toString());
		String strUser = m_oUser.getText().toString();
		String strPsd = m_oPsd.getText().toString();
		// call NET_DVR_Login_v30 to login on, port 8000 as default
		int iLogID = m_oHCNetSDK.NET_DVR_Login_V30(strIP, nPort, strUser, strPsd, m_oNetDvrDeviceInfoV30);
		if (iLogID < 0)
		{
			Log.e(TAG, "NET_DVR_Login is failed!Err:" + m_oHCNetSDK.NET_DVR_GetLastError());
			return -1;
		}
		
		Log.i(TAG, "NET_DVR_Login is Successful!");
		
		return iLogID;
	}
	
	/** 
     * @fn paramCfg
     * @author huyf
     * @brief configuration
     * @param iUserID - login ID [in]
     * @param NULL [out]
     * @return NULL
     */
	private void paramCfg(final int iUserID)
	{
		// whether have logined on
		if (iUserID < 0)
		{
			Log.e(TAG, "iUserID < 0");
			return;
		}		
		
		if(m_byGetFlag == 1)
		{
			if (!m_oHCNetSDK.NET_DVR_GetDVRConfig(iUserID, HCNetSDK.NET_DVR_GET_NETCFG_V30, 0, NetCfg))
			{
				Log.e(TAG, "get net cfg faied!"+ " err: " + m_oHCNetSDK.NET_DVR_GetLastError());
				m_oDNSServer1.setText("get net cfg failed");
			}
			else
			{
				Log.i(TAG, "get net cfg succ!");
				String strIP =  new String(NetCfg.struDnsServer1IpAddr.sIpV4);
				m_oDNSServer1.setText(strIP.trim());
				m_oParamCfgBtn.setText("Set Netcfg");
				m_byGetFlag = 0;
			}		
		}
		else
		{
			byte [] byIP = m_oDNSServer1.getText().toString().getBytes();	
			NetCfg.struDnsServer1IpAddr.sIpV4 = new byte[16];
			System.arraycopy(byIP, 0, NetCfg.struDnsServer1IpAddr.sIpV4, 0, byIP.length);			
			if (!m_oHCNetSDK.NET_DVR_SetDVRConfig(iUserID, HCNetSDK.NET_DVR_SET_NETCFG_V30, 0, NetCfg))
			{
				Log.e(TAG, "Set net cfg faied!"+ " err: " + m_oHCNetSDK.NET_DVR_GetLastError());
			}
			else
			{
				Log.i(TAG, "Set net cfg succ!");
				m_oParamCfgBtn.setText("Get Netcfg");
				m_byGetFlag = 1;
			}
		}	
	}
	
	/**
     * @fn getExceptiongCbf
     * @author huyf
     * @brief process exception
     * @param NULL [in]
     * @param NULL [out]
     * @return exception instance
     */
	private ExceptionCallBack getExceptiongCbf()
	{
	    ExceptionCallBack oExceptionCbf = new ExceptionCallBack()
        {
            public void fExceptionCallBack(int iType, int iUserID, int iHandle)
            {
            	;// you can add process here
            }
        };
        return oExceptionCbf;
	}
	
	/** 
     * @fn getRealPlayerCbf
     * @author huyf
     * @brief get realplay callback instance
     * @param NULL [in]
     * @param NULL [out]
     * @return callback instance
     */
	private RealPlayCallBack getRealPlayerCbf()
	{
	    RealPlayCallBack cbf = new RealPlayCallBack()
        {
             public void fRealDataCallBack(int iRealHandle, int iDataType, byte[] pDataBuffer, int iDataSize)
             {
            	// player channel 1
            	DemoActivity.this.processRealData(1, iDataType, pDataBuffer, iDataSize, Player.STREAM_REALTIME); 
             }
        };
        return cbf;
	}
	
	/** 
     * @fn getPlayerbackPlayerCbf
     * @author Jerry
     * @brief get Playback instance
     * @param NULL [in]
     * @param NULL [out]
     * @return callback instance
     */
	private PlaybackCallBack getPlayerbackPlayerCbf()
	{
		PlaybackCallBack cbf = new PlaybackCallBack()
        {            
			@Override
			public void fPlayDataCallBack(int iPlaybackHandle, int iDataType, byte[] pDataBuffer, int iDataSize)
			{
				// player channel 1
            	DemoActivity.this.processRealData(1, iDataType, pDataBuffer, iDataSize, Player.STREAM_FILE);	
			}
        };
        return cbf;
	}
	
	/** 
     * @fn processRealData
     * @author huyf
     * @brief process real data
     * @param iPlayViewNo - player channel [in]
     * @param iDataType	  - data type [in]
     * @param pDataBuffer - data buffer [in]
     * @param iDataSize   - data size [in]
     * @param iStreamMode - stream mode [in]
     * @param NULL [out]
     * @return NULL
     */
	public void processRealData(int iPlayViewNo, int iDataType, byte[] pDataBuffer, int iDataSize, int iStreamMode)
	{
		int i = 0;
	  ///  Log.i(TAG, "iPlayViewNo:" + iPlayViewNo + "iDataType:" + iDataType + "iDataSize:" + iDataSize);
	    try
        {
	    	switch (iDataType)
	    	{
	    		case HCNetSDK.NET_DVR_SYSHEAD:    		
	    			if(m_iPort >= 0)
	    			{
	    				break;
	    			}	    			
	    			m_iPort = m_oPlayerSDK.getPort();	
	    			if(m_iPort == -1)
	    			{
	    				Log.e(TAG, "getPort is failed!");
	    				break;
	    			}
	    			if (iDataSize > 0)
	    			{
	    				if (!m_oPlayerSDK.setStreamOpenMode(m_iPort, iStreamMode))  //set stream mode
	    				{
	    					Log.e(TAG, "setStreamOpenMode failed");
	    					break;
	    				}
	    				if(!m_oPlayerSDK.setSecretKey(m_iPort, 1, "ge_security_3477".getBytes(), 128))
	    				{
	    					Log.e(TAG, "setSecretKey failed");
	    					break;
	    				}
	    				if (!m_oPlayerSDK.openStream(m_iPort, pDataBuffer, iDataSize, 2*1024*1024)) //open stream
	    				{
	    					Log.e(TAG, "openStream failed");
	    					break;
	    				}

	    				if (!m_oPlayerSDK.play(m_iPort, m_osurfaceView.getHolder().getSurface())) 
	    				{
	    					Log.e(TAG, "play failed");
	    					break;
	    				}
	    			}
	    			break;	
	    		case HCNetSDK.NET_DVR_STREAMDATA:
	    		case HCNetSDK.NET_DVR_STD_AUDIODATA:
	    		case HCNetSDK.NET_DVR_STD_VIDEODATA:	    		
	    			if (iDataSize > 0 && m_iPort != -1)
	    			{
	    				for(i = 0; i < 400; i++)
	    				{
	    					if (m_oPlayerSDK.inputData(m_iPort, pDataBuffer, iDataSize))
		    				{
		    					break;
		    				} 
	    					Thread.sleep(10);
	    				}
	    				if(i == 400)
	    				{
	    					Log.e(TAG, "inputData failed");
	    				}
	    			}
	    			break;
	    		default:
	    			break;
	    	}
        }
        catch (Exception e)
        {
            Log.e(TAG, "processRealData Exception!err:" + e.toString());
        }
	}
		
	/** 
     * @fn Cleanup
     * @author huyf
     * @brief cleanup
     * @param NULL [in]
     * @param NULL [out]
     * @return NULL
     */
    public void Cleanup()
    {
        // release player resource
    	
        m_oPlayerSDK.freePort(m_iPort);
        
        // release net SDK resource
	    m_oHCNetSDK.NET_DVR_Cleanup();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
         switch (keyCode)
         {
         case KeyEvent.KEYCODE_BACK:
        	 	
        	  stopPlay();
        	  Cleanup();
              android.os.Process.killProcess(android.os.Process.myPid());
              break;
         default:
              break;
         }
     
         return true;
    }
}
