package com.monitor.account;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.monitor.account.DatabaseHelper.AccountTbColumns;

/***
 * 数据库操作接口
 * 
 * @author andy.xu
 * **/
public class DatabaseService {

	private DatabaseHelper databaseHelper;

	public DatabaseService(Context context) {

		databaseHelper = new DatabaseHelper(context);
	}

	/**
	 * 取得可读数据库
	 * 
	 * @return
	 */
	public SQLiteDatabase getReadableDatabase() {
		try {
			return databaseHelper.getReadableDatabase();
		} catch (Exception e) {
			Log.e("DatabaseService: ", e.toString());
		}
		return null;
	}

	/**
	 * 取得可写数据库
	 * 
	 * @return
	 */
	public SQLiteDatabase getWritableDatabase() {

		try {
			return databaseHelper.getWritableDatabase();
		} catch (Exception e) {
			Log.e("DatabaseService: ", e.toString());
		}
		return null;
	}

	/**
	 * <pre>
	 * 插入一条设备信息
	 * </pre>
	 * 
	 * @param info
	 *            [in] 设备的基本信息
	 * @return -1：失败 -2：账户已存在 >0:成功
	 */
	public int insertOneAccount(final AccountInfo info) {

		if (null == info)
			return -1;

		String whereStr = AccountTbColumns.COLUMN_DEVICENAME + " = '"
				+ info.deviceName + "'";

		SQLiteDatabase db = getWritableDatabase();
		if (null == db)
			return -1;

		Cursor cursor = db.query(AccountTbColumns.TB_NAME, null, whereStr,
				null, null, null, null);
		if (null != cursor && cursor.moveToFirst()) {
			cursor.close();
			db.close();
			return -2;
		}

		if (null != cursor)
			cursor.close();
		final int nRet = (int) db.insert(AccountTbColumns.TB_NAME, null,
				info.getContentValue());
		db.close();
		return nRet;
	}

	/**
	 * <pre>
	 * 删除一条设备信息
	 * </pre>
	 * 
	 * @param info
	 *            [in] 设备信息
	 */
	public void deleteOneAccount(final AccountInfo info) {
		if (null == info)
			return;

		SQLiteDatabase db = getWritableDatabase();
		if (null == db)
			return;

		db.delete(AccountTbColumns.TB_NAME, AccountTbColumns.COLUMN_ID + " = "
				+ info.nId, null);
		db.close();
	}

	/**
	 * <pre>
	 * 更新一条设备信息
	 * </pre>
	 * 
	 * @param info
	 *            [in] 设备信息
	 */
	public void upDateOneAccount(final AccountInfo info) {

		if (null == info)
			return;

		SQLiteDatabase db = getWritableDatabase();
		if (null == db)
			return;

		db.update(AccountTbColumns.TB_NAME, info.getContentValue(),
				AccountTbColumns.COLUMN_ID + " = " + info.nId, null);
		db.close();
	}

	/**
	 * <pre>
	 * 查询所有的账户信息
	 * </pre>
	 * 
	 * @return
	 */
	public List<AccountInfo> getAccountList() {

		SQLiteDatabase db = getReadableDatabase();
		if (null == db)
			return null;

		Cursor cursor = db.query(AccountTbColumns.TB_NAME, null, null, null,
				null, null, null);
		if (null == cursor || cursor.getCount() == 0) {
			db.close();
			return null;
		}

		List<AccountInfo> accountList = new ArrayList<AccountInfo>(
				cursor.getCount());
		while (cursor.moveToNext()) {
			AccountInfo info = new AccountInfo();
			info = info.setCursor(cursor);
			if (null != info)
				accountList.add(info);
		}

		cursor.close();
		db.close();
		return accountList;
	}
}
