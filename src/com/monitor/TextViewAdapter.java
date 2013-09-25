package com.monitor;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * <pre>
 * 设备列表显示
 * </pre>
 * 
 * @author andy.xu
 * 
 */
public class TextViewAdapter extends BaseAdapter {

	private Context mContext = null;
	private List<String> mData;
	private int mColor = 0;
	private int mTextSize = 12;

	public TextViewAdapter(Context context, final List<String> data) {

		mContext = context;
		mData = data;
	}

	public void setTextColor(final int nColor) {
		mColor = nColor;
	}

	public void setTextSize(final int nSize) {
		mTextSize = nSize;
	}

	@Override
	public int getCount() {
		return null == mData ? 0 : mData.size();
	}

	@Override
	public Object getItem(int position) {
		return null == mData ? null : mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	private Holder mHolder = null;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (null == mData || mData.isEmpty())
			return null;

		if (null == convertView) {

			convertView = LinearLayout.inflate(mContext, R.layout.textview_item_adapter, null);

			mHolder = new Holder();
			mHolder.textView = (TextView) convertView.findViewById(R.id.textview_id);
			if (mColor != 0)
				mHolder.textView.setTextColor(mColor);
			mHolder.textView.setTextSize(mTextSize);
			convertView.setTag(mHolder);
		} else {
			mHolder = (Holder) convertView.getTag();
		}

		if (position >= mData.size())
			return null;

		String str = mData.get(position);
		if (TextUtils.isEmpty(str) == false)
			mHolder.textView.setText(str);
		return convertView;
	}

	private class Holder {

		public TextView textView;

		public Holder() {
			textView = null;
		}
	}
}
