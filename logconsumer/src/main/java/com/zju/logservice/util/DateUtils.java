package com.zju.logservice.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DateUtils {

	public static final SimpleDateFormat longDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static final SimpleDateFormat shortDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	public static final SimpleDateFormat zoneDateFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss +0000");

	public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static String format(Date date, String format) {
		return null == date ? "" : new SimpleDateFormat(format).format(date);
	}
	
	public static Date parse(String date, String format) throws ParseException {
		Date test=null == date ? new Date() : new SimpleDateFormat(format).parse(date);
		System.out.println(DateUtils.format(test));
		return test;
	}

	public static String format(Date date) {
		return format(date, DEFAULT_FORMAT);
	}
}
