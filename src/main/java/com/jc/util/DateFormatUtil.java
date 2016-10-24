package com.jc.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtil {
	public static final String PATTERN_FULL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static final Date parse(String time) {
		try {
			return new SimpleDateFormat(PATTERN_FULL_DATE_FORMAT).parse(time);
		} catch (ParseException e) {
			return null;
		}
	}

}
