package com.monitor;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.monitor.account.AccountInfo;

public class LoginDialog extends Dialog {

	private Context mContext = null;
	private EditText mIpEdit = null;
	private EditText mPortEdit = null;
	private EditText mUserEdit = null;
	private EditText mPasswordEdit = null;
	private EditText mDeviceEdit = null;
	private Spinner mIpModelView = null;
	private Button mLoginBtn = null;
	private Button mCancelBtn = null;
	private AccountInfo mAccountInfo = null;
	private TextViewAdapter mAdapter = null;
	private List<String> mSpinnerList = null;

	public LoginDialog(Context context, int theme) {
		super(context, theme);
		mContext = context;
	}

	public LoginDialog(Context context) {
		super(context);
		mContext = context;
	}

	public LoginDialog(Context context, AccountInfo account) {
		super(context);
		mContext = context;
		mAccountInfo = account;
	}

	public LoginDialog(Context context, int theme, AccountInfo account) {
		super(context, theme);
		mContext = context;
		mAccountInfo = account;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.add_account_activity);
		initUI();
	}

	private void initUI() {

		mDeviceEdit = (EditText) findViewById(R.id.device_edit_id);
		mIpModelView = (Spinner) findViewById(R.id.ip_model_id);
		mIpEdit = (EditText) findViewById(R.id.ip_edit_id);
		mPortEdit = (EditText) findViewById(R.id.port_edit_id);
		mUserEdit = (EditText) findViewById(R.id.user_edit_id);
		mPasswordEdit = (EditText) findViewById(R.id.password_edit_id);
		mLoginBtn = (Button) findViewById(R.id.ok_btn_id);
		mCancelBtn = (Button) findViewById(R.id.cancel_btn_id);

		mSpinnerList = new ArrayList<String>(4);
		mSpinnerList.add("IP");
		mSpinnerList.add("IPService");
		mSpinnerList.add("DDNS");
		mSpinnerList.add("域名");
		mAdapter = new TextViewAdapter(mContext, mSpinnerList);
		mIpModelView.setAdapter(mAdapter);
		mIpModelView.setSelection(0);

		addListener();
		initModifyData();
	}

	private void initModifyData() {

		if (null != mAccountInfo) {

			if (null != mDeviceEdit)
				mDeviceEdit.setText(mAccountInfo.deviceName);
			if (null != mIpEdit)
				mIpEdit.setText(mAccountInfo.ipAddr);
			if (null != mPortEdit)
				mPortEdit.setText(mAccountInfo.port);
			if (null != mUserEdit)
				mUserEdit.setText(mAccountInfo.userName);
			if (null != mPasswordEdit)
				mPasswordEdit.setText(mAccountInfo.password);
			if (null != mIpModelView)
				mIpModelView.setSelection(mAccountInfo.nIpModel);
		}
	}

	private void addListener() {

		mLoginBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (null != mListen)
					mListen.onLoginClick(getCurAccountInfo());
			}
		});

		mCancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

	public void modifyAccount(AccountInfo accountInfo) {

		mAccountInfo = accountInfo;
		initModifyData();
	}

	/**
	 * <pre>
	 * 获取用户输入的信息
	 * </pre>
	 * 
	 * @return
	 */
	private AccountInfo getCurAccountInfo() {

		AccountInfo account = new AccountInfo();
		account.ipAddr = mIpEdit.getText().toString();
		account.port = mPortEdit.getText().toString();
		account.userName = mUserEdit.getText().toString();
		account.password = mPasswordEdit.getText().toString();
		account.deviceName = mDeviceEdit.getText().toString();
		account.nIpModel = mIpModelView.getSelectedItemPosition();
		if (null != mAccountInfo)
			account.nId = mAccountInfo.nId;
		return account;
	}

	private LoginClickListener mListen = null;

	public void setOnLogInListener(final LoginClickListener listen) {
		mListen = listen;
	}

	public interface LoginClickListener {
		void onLoginClick(AccountInfo account);
	}

}
