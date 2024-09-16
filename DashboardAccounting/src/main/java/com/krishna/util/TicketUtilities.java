package com.krishna.util;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krishna.DashboardAccountingApplication;

public class TicketUtilities {

	private static final Logger logger = LoggerFactory.getLogger(TicketUtilities.class);
	
	
	/*
	 * function for Loading Property file.
	 */
	public static Properties propertiesFileReader(String filePath) {

		Properties prop = null;
		InputStream input = null;
		try {
			input = DashboardAccountingApplication.class.getClassLoader().getResourceAsStream(filePath);
			prop = new Properties();
			prop.load(input);
		} catch (Exception e) {
			logger.error("Exception in Properties file Reading : " + e.getMessage());
			return null;
		}
		return prop;
	}

	public static boolean isNull(Object obj) {
		return (obj == null) ? true : false;
	}

	public static String currentDateAsString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(new Date());
	}
	
	public static boolean testPattern(String content, String inputpattern) {
		Pattern pattern = Pattern.compile(inputpattern);
		Matcher matcher = pattern.matcher(content);
		return matcher.matches();
	}
	
	public static boolean isValidDate(String inputDate, String formatString) {
		try {
			SimpleDateFormat format = new SimpleDateFormat(formatString);
			format.setLenient(false);
			format.parse(inputDate);
			return true;
		} catch (ParseException e) {
			logger.error(ConstantUtility.PARSE_EXCEPTION_STRING + e.getMessage());
			return false;
		} catch (IllegalArgumentException e) {
			logger.error("Illegal ArgumentException :: {} " + e.getMessage());
			return false;

		} catch (Exception e) {
			logger.error(ConstantUtility.EXCEPTION_STRING + e.getMessage());
			return false;
		}
	}

	public static int compareDates(String startDate, String endDate, String format) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			Date date1 = sdf.parse(startDate);
			Date date2 = sdf.parse(endDate);
			
			if (date1.compareTo(date2) > 0) {
				return 1;
			} else if (date1.compareTo(date2) < 0) {
				return -1;
			} else if (date1.compareTo(date2) == 0) {
				return 0;
			} else {
				logger.debug("inside unknown case");
				return -1;
			}
		} catch (ParseException ex) {
			logger.error(ConstantUtility.PARSE_EXCEPTION_STRING + ex.getMessage());
			return -1;
		}
	}
	
	public static Date getDateObject(String date, String format) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.parse(date);
		} catch (ParseException ex) {
			logger.error(ConstantUtility.PARSE_EXCEPTION_STRING + ex.getMessage());
			return null;
		} catch (Exception e) {
			logger.error(ConstantUtility.EXCEPTION_STRING + e.getMessage());
			return null;
		}
	}
	
	public static Integer toIntegerFromString(String str) {
		try {
			if (str != null)
				return Integer.valueOf(str);
		} catch (Exception ex) {
			logger.error(ConstantUtility.EXCEPTION_STRING + ex.getMessage());
		}
		return null;
	}

	public static Long toLongFromString(String str) {
		try {
			if (str != null)
				return Long.valueOf(str);
		} catch (Exception ex) {
			logger.error(ConstantUtility.PARSE_EXCEPTION_STRING + ex.getMessage());
		}
		return null;
	}
	
	public static String getPreviousMonth()
	{
		String previousMonth = String.valueOf(YearMonth.now().minusMonths(1L).getMonth()).toLowerCase();
		char ch = (char)previousMonth.charAt(0);
		ch = (char) (ch - 32);
		previousMonth = ch + previousMonth.substring(1);
		return previousMonth;
	}
	
	
}
