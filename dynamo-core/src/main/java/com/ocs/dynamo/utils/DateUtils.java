/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.exception.OCSRuntimeException;

/**
 * Date utility class
 * 
 * @author bas.rutten
 */
public final class DateUtils {

	public static final String WEEK_CODE_PATTERN = "\\d{4}-\\d{2}";

	private static final String DATE_FORMAT = "ddMMyyyy";

	private static final String TIME_FORMAT = "HHmmss";

	private static final int FIRST_WEEK_NUMBER = 1;

	private static final int LAST_WEEK_NUMBER = 53;

	private DateUtils() {
	}

	/**
	 * Formats a date according to the specified format
	 * 
	 * @param date
	 *            the date
	 * @param format
	 *            the format
	 * @return
	 */
	public static String formatDate(Date date, String format) {
		if (date == null || format == null) {
			return null;
		}

		SimpleDateFormat df = new SimpleDateFormat(format);
		return df.format(date);
	}

	/**
	 * Creates a java.util.Date based on a string representation
	 * 
	 * @param dateStr
	 *            the string (in the format ddMMyyyy)
	 * @return
	 */
	public static Date createDate(String dateStr) {
		if (dateStr == null) {
			return null;
		}

		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
		try {
			return format.parse(dateStr);
		} catch (ParseException e) {
			throw new OCSRuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param timeStr
	 * @return
	 */
	public static Date createTime(String timeStr) {
		if (timeStr == null) {
			return null;
		}

		SimpleDateFormat format = new SimpleDateFormat(TIME_FORMAT);
		try {
			return format.parse(timeStr);
		} catch (ParseException e) {
			throw new OCSRuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Gets the number of the last week of a year
	 * 
	 * @param year
	 *            the year
	 * @return
	 */
	public static int getLastWeekOfYear(int year) {
		Date date = createDate("3112" + year);
		Calendar calendar = Calendar.getInstance(DynamoConstants.DEFAULT_LOCALE);
		calendar.setTime(date);

		// it is possible for the last day of a year to actually be part of the
		// first week of next year. We have to compensate for this
		int weekNumber = calendar.get(Calendar.WEEK_OF_YEAR);
		while (weekNumber == 1) {
			calendar.add(Calendar.DATE, -1);
			weekNumber = calendar.get(Calendar.WEEK_OF_YEAR);
		}
		return weekNumber;
	}

	/**
	 * Returns the next week code given an existing week code
	 * 
	 * @param weekCode
	 * @return
	 */
	public static String getNextWeekCode(String weekCode) {
		if (weekCode == null) {
			return null;
		}

		int year = getYearFromWeekCode(weekCode);
		int week = getWeekFromWeekCode(weekCode);

		String next = year + "-" + StringUtils.leftPad(Integer.toString(week + 1), 2, "0");
		if (isValidWeekCode(next)) {
			return next;
		} else {
			return (year + 1) + "-" + "01";
		}
	}

	/**
	 * Checks if a string represents a valid week code (yyyy-ww)
	 * 
	 * @param weekCode
	 *            the week code
	 * @return
	 */
	public static boolean isValidWeekCode(String weekCode) {
		if (weekCode == null) {
			return true;
		}

		// pattern must match
		if (!weekCode.matches(WEEK_CODE_PATTERN)) {
			return false;
		}

		int year = getYearFromWeekCode(weekCode);
		int week = getWeekFromWeekCode(weekCode);

		int lastWeekOfYear = getLastWeekOfYear(year);
		return FIRST_WEEK_NUMBER <= week && week <= lastWeekOfYear;
	}

	/**
	 * Returns the quarter of the year of a date, as an integer
	 * 
	 * @param date
	 * @return
	 */
	public static int getQuarter(Date date) {
		if (date == null) {
			return -1;
		}
		Calendar calendar = new GregorianCalendar(DynamoConstants.DEFAULT_LOCALE);
		calendar.setTime(date);
		return 1 + calendar.get(Calendar.MONTH) / 3;
	}

	/**
	 * Translates a week code (yyyy-ww) to the starting day (this is taken to be a Monday) of that
	 * week
	 * 
	 * @param weekCode
	 *            the week code
	 * @return the date
	 */
	public static Date getStartDateOfWeek(String weekCode) {
		if (weekCode != null && weekCode.matches(WEEK_CODE_PATTERN)) {
			int year = getYearFromWeekCode(weekCode);
			int week = getWeekFromWeekCode(weekCode);

			Calendar calendar = new GregorianCalendar(DynamoConstants.DEFAULT_LOCALE);
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.WEEK_OF_YEAR, week);
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

			return truncate(calendar).getTime();
		}
		return null;
	}

	/**
	 * Converts a date to its corresponding week code
	 * 
	 * @param date
	 *            the date
	 * @return
	 */
	public static String toWeekCode(Date date) {
		if (date != null) {
			Calendar calendar = new GregorianCalendar(DynamoConstants.DEFAULT_LOCALE);
			calendar.setTime(date);
			int year = calendar.get(Calendar.YEAR);
			int week = calendar.get(Calendar.WEEK_OF_YEAR);
			int month = calendar.get(Calendar.MONTH);

			// if the week number is reported as 1, but we are in December,
			// then we have an "overflow"
			if (week == FIRST_WEEK_NUMBER && month == Calendar.DECEMBER) {
				year++;
			}

			// if the week number is 53 but we are in January, then reduce the year by one
			if ((week == LAST_WEEK_NUMBER || week == LAST_WEEK_NUMBER - 1) && month == Calendar.JANUARY) {
				year--;
			}

			return year + "-" + StringUtils.leftPad(Integer.toString(week), 2, "0");
		}
		return null;
	}

	/**
	 * Truncates a calendar object, setting all time fields to zero
	 * 
	 * @param calendar
	 * @return
	 */
	public static Calendar truncate(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}

	/**
	 * Retrieves the date from a year
	 * 
	 * @param date
	 * @return
	 */
	public static Integer getYearFromDate(Date date) {
		if (date == null) {
			return null;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	private static int getYearFromWeekCode(String weekCode) {
		return Integer.parseInt(weekCode.substring(0, 4));
	}

	private static int getWeekFromWeekCode(String weekCode) {
		return Integer.parseInt(weekCode.substring(5));
	}
}
