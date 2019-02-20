package com.genzzhang.demo.ringtone;

import android.telephony.PhoneNumberUtils;


public final class PhoneUtil {

	final static String[] PREFIX_NUMBER = { "-", "+86","0086", "12593", "17909", "17951", "17911", "10193", "12583", "12520", "96688" };


	public static String removePrefix(String number) {
		if (number != null && number.length() > 2) {
			for (String prefix : PREFIX_NUMBER) {
				if (number.startsWith(prefix)) {
					number = number.substring(prefix.length());
					break;
				}
			}
		}
		return number;
	}


	public static String stripSeparators(String phoneNumber) {
		if (phoneNumber != null) {
			phoneNumber = PhoneNumberUtils.stripSeparators(phoneNumber);
			phoneNumber = phoneNumber.replace("-", "").replace(" ", "").trim();
		}
		return phoneNumber;
	}

	/**
	 * 是否为合法号码，长度至少为3，并且仅能使用下面的字符
	 * 0123456789+-
	 */
	public static boolean isValid(String s) {
		// 判断长度
		if (s==null || s.length()<3) {
			return false;
		}
		// 判断内容
		for (int i=s.length()-1; i>=0; --i) {
			char c = s.charAt(i);
			if ( ! (c>='0' && c<='9' || c=='+' || c=='-')) {
				return false;
			}
			if (c=='+' && i!=0) {
				return false;
			}
		}
		return true;
	}

	
	/**
	 * 去空格 去前缀额号码
	 */
	public static String getFormatNumber(String rawNumber){
		return PhoneUtil.removePrefix(PhoneUtil
				.stripSeparators(rawNumber));
	}
}
