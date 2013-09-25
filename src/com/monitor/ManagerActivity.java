package com.monitor;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.monitor.LoginDialog.LoginClickListener;
import com.monitor.account.AccountInfo;
import com.monitor.account.DatabaseService;

/**
 * <pre>
 * 设备管理界面
 * </pre>
 * 
 * @author andy.xu
 * 
 */
public class ManagerActivity extends Activity {

	private TextView mAddDeviceBtn = null;
	private ListView mDeviceListView = null;
	private DeviceItemAdapter mAdapter = null;
	private List<AccountInfo> mAccountList = null;
	private DatabaseService mDBService = null;
	private LoginDialog mAddAccountDlg = null;
	private LoginDialog mModifyAccountDlg = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_manager_activity);

		initUI();
		addListener();
		getDeviceAccount();
	}

	private void initUI() {

		mAddDeviceBtn = (TextView) findViewById(R.id.manager_add_device_id);
		mDeviceListView = (ListView) findViewById(R.id.manager_listview_id);

		mAccountList = new ArrayList<AccountInfo>();
		mAdapter = new DeviceItemAdapter(this, mAccountList);
		mDeviceListView.setAdapter(mAdapter);

		mDBService = new DatabaseService(this);
		mAddAccountDlg = new LoginDialog(this, R.style.transparent);
	}

	private void addListener() {

		mAddDeviceBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (null != mAddAccountDlg)
					mAddAccountDlg.show();
			}
		});

		mDeviceListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				onShowOperDlg(arg2);
			}
		});

		mAddAccountDlg.setOnLogInListener(new LoginClickListener() {
			@Override
			public void onLoginClick(AccountInfo account) {

				inSertOneAccount(account);
			}
		});

	}

	/**
	 * <pre>
	 * 添加一个设备信息
	 * </pre>
	 * 
	 * @param account
	 *            [in] 设备的信息
	 */
	private void inSertOneAccount(final AccountInfo account) {

		if (null == account) {
			showErrorToast("账户信息为空!");
			return;
		}

		if (TextUtils.isEmpty(account.deviceName)) {
			showErrorToast("设备名称为空!");
			return;
		}

		if (TextUtils.isEmpty(account.ipAddr)) {
			showErrorToast("IP地址为空!");
			return;
		}

		if (!Common.isValidIp(account.ipAddr)) {
			showErrorToast("IP地址格式不正确!");
			return;
		}

		if (TextUtils.isEmpty(account.userName)) {
			showErrorToast("账户名称为空!");
			return;
		}

		if (TextUtils.isEmpty(account.password)) {
			showErrorToast("账户密码为空!");
			return;
		}

		final int nRet = mDBService.insertOneAccount(account);
		if (nRet == -1) {
			showErrorToast("添加失败");
		} else if (nRet == -2) {
			showErrorToast("设备已经存在");
		} else {
			mAccountList.add(account);
			mAdapter.notifyDataSetChanged();
			showErrorToast("添加成功");
			sendAddBroadcast();
		}

		mAddAccountDlg.dismiss();
	}

	/**
	 * <pre>
	 * 删除一个设备信息
	 * </pre>
	 * 
	 * @param accountInfo
	 *            [in] 设备的信息
	 */
	private void deleteAccount(final AccountInfo accountInfo, int nPos) {
		if (null == accountInfo)
			return;

		mDBService.deleteOneAccount(accountInfo);
		mAccountList.remove(accountInfo);
		mAdapter.notifyDataSetChanged();
		sendDeleteBroadcast(accountInfo);
	}

	/**
	 * <pre>
	 * 修改一个设备信息
	 * </pre>
	 * 
	 * @param accountInfo
	 */
	private void modifyAccount(AccountInfo accountInfo, final int nPos) {
		if (null == accountInfo)
			return;

		if (null == mModifyAccountDlg)
			mModifyAccountDlg = new LoginDialog(this, R.style.transparent, accountInfo);
		else
			mModifyAccountDlg.modifyAccount(accountInfo);
		mModifyAccountDlg.setOnLogInListener(new LoginClickListener() {
			@Override
			public void onLoginClick(AccountInfo account) {

				if (null == account) {
					mModifyAccountDlg.dismiss();
					return;
				}

				if (TextUtils.isEmpty(account.deviceName)) {
					showErrorToast("设备名称为空!");
					return;
				}

				if (TextUtils.isEmpty(account.ipAddr)) {
					showErrorToast("IP地址为空!");
					return;
				}

				if (!Common.isValidIp(account.ipAddr)) {
					showErrorToast("IP地址格式不正确!");
					return;
				}

				if (TextUtils.isEmpty(account.userName)) {
					showErrorToast("账户名称为空!");
					return;
				}

				if (TextUtils.isEmpty(account.password)) {
					showErrorToast("账户密码为空!");
					return;
				}

				mDBService.upDateOneAccount(account);
				mAccountList.set(nPos, account);
				mAdapter.notifyDataSetChanged();
				sendAddBroadcast();
				mModifyAccountDlg.dismiss();
			}
		});
		mModifyAccountDlg.show();
	}

	private void showErrorToast(final String str) {
		if (TextUtils.isEmpty(str))
			return;

		Toast toast = Toast.makeText(this, str, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	private AlertDialog mAlertDialog = null;

	private void onShowOperDlg(final int nPos) {

		final AccountInfo account = mAccountList.get(nPos);
		if (null == account)
			return;

		// if (null != mAlertDialog) {
		// mAlertDialog.show();
		// return;
		// }

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("设备管理");
		builder.setItems(R.array.device_manager_array, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				if (which == 0) {
					// 修改
					modifyAccount(account, nPos);
				} else if (which == 1) {
					// 删除
					deleteAccount(account, nPos);
				}
				dialog.dismiss();
			}
		});
		builder.setPositiveButton("取消", null);
		mAlertDialog = builder.create();
		mAlertDialog.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
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

	// 发送故事发生改变的广播
	private void sendAddBroadcast() {
		Intent intent = new Intent("refresh");
		intent.putExtra("refresh", 1);
		sendBroadcast(intent);
	}

	private void sendDeleteBroadcast(final AccountInfo accountInfo) {
		Intent intent = new Intent("refresh");
		intent.putExtra("refresh", 2);
		intent.putExtra("accountinfo", accountInfo);
		sendBroadcast(intent);
	}

}
