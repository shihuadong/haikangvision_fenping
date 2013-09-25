package com.monitor.account;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite帮助类，帮助创建数据库和数据库版本管理
 * 
 * @author andy.xu
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	// 数据库名称
	private final static String DB_NAME = "monitor.db";

	// 数据库版本号
	public final static int DB_VERSION = 1;

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	/**
	 * 创建数据表，只在创建的时候调用一次
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		if (null == db) {
			return;
		}

		createAccountTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (null == db) {
			return;
		}
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}

	/**
	 * 创建设备信息表
	 * 
	 * @param db
	 */
	private void createAccountTable(SQLiteDatabase db) {
		//
		StringBuffer plugindb = new StringBuffer();
		plugindb.append("CREATE TABLE IF NOT EXISTS ");
		plugindb.append(AccountTbColumns.TB_NAME);
		plugindb.append("(");
		plugindb.append(AccountTbColumns.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
		plugindb.append(AccountTbColumns.COLUMN_DEVICENAME + " TEXT, ");
		plugindb.append(AccountTbColumns.COLUMN_IP + " TEXT, ");
		plugindb.append(AccountTbColumns.COLUMN_PORT + " TEXT, ");
		plugindb.append(AccountTbColumns.COLUMN_IP_MODEL + " INTEGER Default 0, ");
		plugindb.append(AccountTbColumns.COLUMN_USERNAME + " TEXT, ");
		plugindb.append(AccountTbColumns.COLUMN_PASSWORD + " TEXT ");
		plugindb.append(")");
		db.execSQL(plugindb.toString());

		String indexStr = "CREATE INDEX IDX_PRE_NUM ON TB_ACCOUNT (ID)";
		db.execSQL(indexStr);
	}

	/**
	 * 插件表名、字段相关信息
	 */
	public interface AccountTbColumns {

		/**
		 * 表名称
		 */
		String TB_NAME = "TB_ACCOUNT";

		/**
		 * id唯一标识
		 */
		String COLUMN_ID = "ID";

		/**
		 * IP地址
		 */
		String COLUMN_IP = "IP";

		/**
		 * 端口号
		 */
		String COLUMN_PORT = "PORT";

		/**
		 * 用户名称
		 */
		String COLUMN_USERNAME = "USER";

		/**
		 * 设备名称
		 */
		String COLUMN_DEVICENAME = "DEVICE_NAME";

		/**
		 * 设备密码
		 */
		String COLUMN_PASSWORD = "PASSWORD";

		/**
		 * ip的模式 : 1:ip, 2:ipservice, 3:ddns, 4:域名
		 */
		String COLUMN_IP_MODEL = "IP_MODEL";

	}
}
