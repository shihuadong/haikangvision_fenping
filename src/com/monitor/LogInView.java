package com.monitor;

import com.monitor.account.AccountInfo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * <pre>
 * 用户登录界面
 * </pre>
 * 
 * @author andy.xu
 * @version 0.1
 * 
 */
public class LogInView extends LinearLayout {

	private LayoutInflater mLayoutInflater;
	private Context mContext = null;
	private EditText mIpEdit = null;
	private EditText mPortEdit = null;
	private EditText mUserEdit = null;
	private EditText mPasswordEdit = null;

	public LogInView(Context context) {
		super(context);
		mContext = context;
		initUI();
	}

	public LogInView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initUI();
	}

	public LogInView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initUI();
	}

	private void initUI() {
		mLayoutInflater = LayoutInflater.from(mContext);
		mLayoutInflater.inflate(R.layout.add_account_activity, this);

		mIpEdit = (EditText) findViewById(R.id.ip_edit_id);
		mPortEdit = (EditText) findViewById(R.id.port_edit_id);
		mUserEdit = (EditText) findViewById(R.id.user_edit_id);
		mPasswordEdit = (EditText) findViewById(R.id.password_edit_id);
	}

	/**
	 * <pre>
	 * 获取用户输入的信息
	 * </pre>
	 * 
	 * @return
	 */
	public AccountInfo getCurAccountInfo() {

		AccountInfo account = new AccountInfo();
		account.ipAddr = mIpEdit.getText().toString();
		account.port = mPortEdit.getText().toString();
		account.userName = mUserEdit.getText().toString();
		account.password = mPasswordEdit.getText().toString();
		return account;
	}

}
