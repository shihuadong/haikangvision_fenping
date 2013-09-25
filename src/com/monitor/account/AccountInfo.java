package com.monitor.account;

import java.io.Serializable;

import com.monitor.account.DatabaseHelper.AccountTbColumns;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

/**
 * <pre>
 * 账户信息
 * </pre>
 * 
 * @author andy.xu
 * 
 */
public class AccountInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6678587046358796951L;

	/**
	 * 设备的唯一标识
	 */
	public int nId;

	/**
	 * 设备名称
	 */
	public String deviceName;

	/**
	 * IP的模式 : 0:ip, 1:ipservice, 2:ddns, 3:域名
	 */
	public int nIpModel;

	/**
	 * IP地址
	 */
	public String ipAddr;

	/**
	 * 端口号
	 */
	public String port;

	/**
	 * 账户名称
	 */
	public String userName;

	/**
	 * 账户密码
	 */
	public String password;

	/**
	 * 登录成功标记
	 */
	public boolean bLogInSuccessFlag;

	public AccountInfo() {
		ipAddr = null;
		port = "8000";
		userName = null;
		password = null;
		bLogInSuccessFlag = false;
		deviceName = null;
		nIpModel = 0;
	}

	public AccountInfo setCursor(final Cursor cursor) {
		if (null == cursor)
			return null;

		ipAddr = cursor.getString(cursor.getColumnIndex(AccountTbColumns.COLUMN_IP));
		port = cursor.getString(cursor.getColumnIndex(AccountTbColumns.COLUMN_PORT));
		deviceName = cursor.getString(cursor.getColumnIndex(AccountTbColumns.COLUMN_DEVICENAME));
		nIpModel = cursor.getInt(cursor.getColumnIndex(AccountTbColumns.COLUMN_IP_MODEL));
		userName = cursor.getString(cursor.getColumnIndex(AccountTbColumns.COLUMN_USERNAME));
		password = cursor.getString(cursor.getColumnIndex(AccountTbColumns.COLUMN_PASSWORD));
		nId = cursor.getInt(cursor.getColumnIndex(AccountTbColumns.COLUMN_ID));
		return this;
	}

	public ContentValues getContentValue() {
		ContentValues value = new ContentValues();
		value.put(AccountTbColumns.COLUMN_IP, ipAddr);
		value.put(AccountTbColumns.COLUMN_IP_MODEL, nIpModel);
		value.put(AccountTbColumns.COLUMN_DEVICENAME, deviceName);
		value.put(AccountTbColumns.COLUMN_USERNAME, userName);
		value.put(AccountTbColumns.COLUMN_PASSWORD, password);
		value.put(AccountTbColumns.COLUMN_PORT, port);
		return value;
	}

	/**
	 * <pre>
	 * 获取账户的信息
	 * </pre>
	 * 
	 * @param context
	 * @return
	 */
	public AccountInfo getAccountInfo(final Context context) {

		if (null == context)
			return null;

		SharedPreferences pre = context.getSharedPreferences("accountinfo", 0);
		if (null == pre)
			return null;

		ipAddr = pre.getString("ipAddr", null);
		port = pre.getString("port", null);
		userName = pre.getString("userName", null);
		password = pre.getString("password", null);
		bLogInSuccessFlag = pre.getBoolean("loginFlag", false);
		return this;
	}

	/**
	 * <pre>
	 * 保存账户信息
	 * </pre>
	 * 
	 * @param context
	 */
	public void saveAccountInfo(final Context context) {

		if (null == context)
			return;

		SharedPreferences.Editor edit = context.getSharedPreferences("accountinfo", 0).edit();
		if (null == edit)
			return;

		edit.putString("ipAddr", ipAddr);
		edit.putString("port", port);
		edit.putString("userName", userName);
		edit.putString("password", password);
		edit.putBoolean("loginFlag", bLogInSuccessFlag);
		edit.commit();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((deviceName == null) ? 0 : deviceName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccountInfo other = (AccountInfo) obj;
		if (deviceName == null) {
			if (other.deviceName != null)
				return false;
		} else if (!deviceName.equals(other.deviceName))
			return false;
		return true;
	}
	
	

}
