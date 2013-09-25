package com.monitor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

public class Common {

	/**
	 * <pre>
	 * IP地址的格式合法性验证
	 * </pre>
	 * 
	 * @param ipAddr
	 * @return
	 */
	public static boolean isValidIp(final String ipAddr) {
		if (TextUtils.isEmpty(ipAddr))
			return false;

		Pattern patter = Pattern.compile("((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)");
		Matcher match = patter.matcher(ipAddr);
		return match.matches();
	}
}
