package com.monitor;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.hikvision.netsdk.NET_DVR_TIME;
import com.monitor.account.AccountInfo;

/**
 * <pre>
 * 监控回放设置
 * </pre>
 * 
 * @author andy.xu
 * 
 */
public class PlayBackSetActivity extends Activity {

	private TextView mDeviceName = null;
	private EditText mStartDayView = null;
	private EditText mStartTimerView = null;
	private EditText mEndDayView = null;
	private EditText mEndTimerView = null;
	private Button mOkBtn = null;
	private AccountInfo mAccountInfo = null;
	private DatePickerDialog mDateDlg = null;
	private TimePickerDialog mTimerDlg = null;
	private int mTimerIndex = 0;
	private Calendar mStartCalendar = Calendar.getInstance();
	private Calendar mEndCalendar = Calendar.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.playback_timer_set);
		initUI();
		addListener();

		setSetInfo();
	}

	private void initUI() {

		mDeviceName = (TextView) findViewById(R.id.device_id);
		mStartDayView = (EditText) findViewById(R.id.start_day_id);
		mStartTimerView = (EditText) findViewById(R.id.start_timer_id);
		mEndDayView = (EditText) findViewById(R.id.end_day_id);
		mEndTimerView = (EditText) findViewById(R.id.end_timer_id);
		mOkBtn = (Button) findViewById(R.id.ok_btn_id);
	}

	private void addListener() {

		mOkBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getSetInfo();
			}
		});

		mStartDayView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				mStartCalendar.setTimeInMillis(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
				onSetDateDlg(mStartCalendar, 0);
			};
		});

		mStartTimerView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
				onSetTimerDlg(startDate, 0);
			}
		});

		mEndDayView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mEndCalendar.setTimeInMillis(System.currentTimeMillis());
				onSetDateDlg(mEndCalendar, 1);
			}
		});

		mEndTimerView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Date endDate = new Date();
				onSetTimerDlg(endDate, 1);
			}
		});
	}

	private void onSetTimerDlg(Date date, final int nTimerIndex) {

		if (null == date)
			return;

		new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

				Date date = new Date();
				date.setHours(hourOfDay);
				date.setMinutes(minute);
				String timer = DateFormat.formatDate(date, DateFormat.DEFAULT_TIME_FORMAT2);
				if (nTimerIndex == 0) {
					mStartCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
					mStartCalendar.set(Calendar.MINUTE, minute);
					mStartTimerView.setText(timer);
				} else if (nTimerIndex == 1) {
					mEndCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
					mEndCalendar.set(Calendar.MINUTE, minute);
					mEndTimerView.setText(timer);
				}
			}
		}, date.getHours(), date.getMinutes(), true).show();
	}

	private Calendar mCalendar = Calendar.getInstance();
	private int mHourOfDay = 0;
	private int mMinute = 0;
	private TimePickerDialog.OnTimeSetListener timerChangeListener = new OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

			mHourOfDay = hourOfDay;
			mMinute = minute;
		}
	};

	private void dealwithCurTimer(final int nDateIndex) {

		String timer = mHourOfDay + ":" + mMinute;
		if (nDateIndex == 0) {
			mStartTimerView.setText(timer);
		} else if (nDateIndex == 1) {
			mEndTimerView.setText(timer);
		}
	}

	private void onSetDateDlg(Calendar date, final int nDateIndex) {
		if (null == date)
			return;

		mDateDlg = new DatePickerDialog(PlayBackSetActivity.this, null, date.get(Calendar.YEAR), date.get(Calendar.MONTH),
				date.get(Calendar.DAY_OF_MONTH));
		mDateDlg.setButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				dealwithCurDate(nDateIndex);
			}
		});
		mDateDlg.show();
	}

	private void dealwithCurDate(final int nDateIndex) {
		DatePicker picker = mDateDlg.getDatePicker();

		Date date = new Date(picker.getYear() - 1900, picker.getMonth(), picker.getDayOfMonth());
		String curDay = DateFormat.formatDate(date, DateFormat.DEFAULT_DATE_FORMAT);
		if (nDateIndex == 0) {
			mStartCalendar.set(picker.getYear() - 1900, picker.getMonth(), picker.getDayOfMonth());
			mStartDayView.setText(curDay);
		} else if (nDateIndex == 1) {
			mEndCalendar.set(picker.getYear() - 1900, picker.getMonth(), picker.getDayOfMonth());
			mEndDayView.setText(curDay);
		}
	}

	private void getSetInfo() {

		Intent intent = new Intent();
		intent.putExtra("startTimer", mStartCalendar);
		intent.putExtra("endTimer", mEndCalendar);
		setResult(RESULT_OK, intent);
		finish();
	}

	private void setSetInfo() {

		mAccountInfo = (AccountInfo) getIntent().getSerializableExtra("deviceInfo");

		if (null != mAccountInfo)
			mDeviceName.setText(mAccountInfo.deviceName);

		mStartCalendar.setTimeInMillis(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
		Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
		String curDay = DateFormat.formatDate(startDate, DateFormat.DEFAULT_DATE_FORMAT);
		mStartDayView.setText(curDay);
		String curTimer = DateFormat.formatDate(startDate, DateFormat.DEFAULT_TIME_FORMAT2);
		mStartTimerView.setText(curTimer);

		mEndCalendar.setTimeInMillis(System.currentTimeMillis());
		String endDay = DateFormat.today();
		String endTimer = DateFormat.currentTime(DateFormat.DEFAULT_TIME_FORMAT2);
		mEndDayView.setText(endDay);
		mEndTimerView.setText(endTimer);

		//
		//
		// Date endDate = new Date();
		// String endDay = endDate.getYear() + 1900 + "-" + endDate.getMonth() +
		// 1 + "-" + endDate.getDate();
		// mEndDayView.setText(endDay);
		// String endTimer = endDate.getHours() + ":" + endDate.getMinutes();
		// mEndTimerView.setText(endTimer);
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
		Date startDate = new Date(new Date().getTime() - 24 * 60 * 60 * 1000);
		struBegin.dwYear = startDate.getYear() + 1900;
		struBegin.dwMonth = startDate.getMonth() + 1;
		struBegin.dwDay = startDate.getDate();
		struBegin.dwHour = startDate.getHours();
		struBegin.dwMinute = startDate.getMinutes();
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
		Date endDate = new Date();
		struEnd.dwYear = endDate.getYear() + 1900;
		struEnd.dwMonth = endDate.getMonth() + 1;
		struEnd.dwDay = endDate.getDate();
		struEnd.dwHour = endDate.getHours();
		struEnd.dwMinute = endDate.getMinutes();
		struEnd.dwSecond = 0;
		return struEnd;
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

}
