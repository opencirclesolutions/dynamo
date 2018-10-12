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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.util.SystemPropertyUtils;

/**
 * Date utility class
 * 
 * @author bas.rutten
 */
public final class DateUtils {

	public static final String WEEK_CODE_PATTERN = "\\d{4}-\\d{2}";

	private static final String DATE_FORMAT = "ddMMyyyy";

	private static final String DATE_TIME_FORMAT = "ddMMyyyy HHmmss";

	private static final String TIME_FORMAT = "HHmmss";

	private static final int FIRST_WEEK_NUMBER = 1;

	private static final int LAST_WEEK_NUMBER = 53;

	private static final int YEAR_STRING_LENGTH = 4;

	private DateUtils() {
		// hidden constructor
	}

	public static Date convertSQLDate(Date d) {
		// toInstance is not supported on java.sql.Date, so convert to actual
		// date
		if (d instanceof java.sql.Date) {
			Date temp = new Date();
			temp.setTime(d.getTime());
			return temp;
		}
		return d;
	}

	/**
	 * Creates a java.util.Date based on a String representation
	 * 
	 * @param dateStr
	 *            the string (in the format ddMMyyyy)
	 * @return
	 */
	public static Date createDate(String dateStr) {
		return toLegacyDate(createLocalDate(dateStr));
	}

	/**
	 * Creates a java.util.Date based on a String representation
	 * 
	 * @param dateTimeStr
	 *            the String (in the format ddMMyyyy HHmmss)
	 * @return
	 */
	public static Date createDateTime(String dateTimeStr) {
		return toLegacyDate(createLocalDateTime(dateTimeStr));
	}

	@SuppressWarnings("unchecked")
	public static <T> T createJava8Date(Class<T> clazz, String dateStr, String format) {
		if (LocalDate.class.equals(clazz)) {
			return (T) createLocalDate(dateStr, format);
		} else if (LocalDateTime.class.equals(clazz)) {
			return (T) createLocalDateTime(dateStr, format);
		} else if (LocalTime.class.equals(clazz)) {
			return (T) createLocalTime(dateStr, format);
		} else if (ZonedDateTime.class.equals(clazz)) {
			return (T) createZonedDateTime(dateStr, format);
		}
		return null;
	}

	/**
	 * Creates a java.time.LocalTime based on a String representation
	 * 
	 * @param dateStr
	 *            the String (in the format ddMMyyyy)
	 * @return
	 */
	public static LocalDate createLocalDate(String dateStr) {
		return createLocalDate(dateStr, DATE_FORMAT);
	}

	/**
	 * Creates a java.time.LocalTime based on a String representation
	 * 
	 * @param dateStr
	 *            the String
	 * @param format
	 *            the desired format
	 * @return
	 */
	public static LocalDate createLocalDate(String dateStr, String format) {
		if (dateStr == null) {
			return null;
		}
		DateTimeFormatter fmt = new DateTimeFormatterBuilder().appendPattern(format).parseStrict().toFormatter();
		return LocalDate.from(fmt.parse(dateStr));
	}

	/**
	 * Creates a java.time.LocalDateTime based on a String representation
	 * 
	 * @param dateTimeStr
	 *            the String representation (ddMMyyyy HHmmss)
	 * @return
	 */
	public static LocalDateTime createLocalDateTime(String dateTimeStr) {
		return createLocalDateTime(dateTimeStr, DATE_TIME_FORMAT);
	}

	/**
	 * Creates a java.time.LocalDateTime based on a String representation
	 * 
	 * @param dateTimeStr
	 *            the String representation
	 * @param format
	 *            the desired format
	 * @return
	 */
	public static LocalDateTime createLocalDateTime(String dateTimeStr, String format) {
		if (dateTimeStr == null) {
			return null;
		}
		DateTimeFormatter fmt = new DateTimeFormatterBuilder().appendPattern(format).parseStrict().toFormatter();
		return LocalDateTime.from(fmt.parse(dateTimeStr));
	}

	/**
	 * Creates a java.time.LocalTime based on a String representation
	 * 
	 * @param timeStr
	 *            the String representation
	 * @return
	 */
	public static LocalTime createLocalTime(String timeStr) {
		return createLocalTime(timeStr, TIME_FORMAT);
	}

	/**
	 * Creates a java.time.LocalTime based on a String representation
	 * 
	 * @param timeStr
	 *            the String representation
	 * @param format
	 *            the desired format
	 * @return
	 */
	public static LocalTime createLocalTime(String timeStr, String format) {
		if (timeStr == null) {
			return null;
		}
		DateTimeFormatter fmt = new DateTimeFormatterBuilder().appendPattern(format).parseStrict().toFormatter();
		return LocalTime.from(fmt.parse(timeStr));
	}

	/**
	 * Creates a Date that hold a time based on a String representation
	 * 
	 * @param timeStr
	 *            the String representation (HHmmss)
	 * @return
	 */
	public static Date createTime(String timeStr) {
		if (timeStr == null) {
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat(TIME_FORMAT);
		format.setLenient(false);
		try {
			return format.parse(timeStr);
		} catch (ParseException e) {
			throw new OCSRuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Create a ZonedDateTime based from a String, using the default format
	 * 
	 * @param dateTimeStr
	 *            the String to parse
	 * @return
	 */
	public static ZonedDateTime createZonedDateTime(String dateTimeStr) {
		return createZonedDateTime(dateTimeStr, SystemPropertyUtils.getDefaultDateTimeWithTimezoneFormat());
	}

	/**
	 * Creates a ZonedDateTime from a String, using the provided format
	 * 
	 * @param dateTimeStr
	 *            the String
	 * @param format
	 *            the format
	 * @return
	 */
	public static ZonedDateTime createZonedDateTime(String dateTimeStr, String format) {
		if (dateTimeStr == null) {
			return null;
		}
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
		return ZonedDateTime.from(fmt.parse(dateTimeStr));
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
		return formatDate(toLocalDate(date), format);
	}

	/**
	 * Formats a date and time according to the specified format
	 *
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatDateTime(Date date, String format) {
		return formatDateTime(toLocalDateTime(date), format);
	}

	/**
	 * Formats a LocalDate according to the specified format
	 * 
	 * @param date
	 *            the date
	 * @param format
	 *            the format
	 * @return
	 */
	public static String formatDate(LocalDate date, String format) {
		if (date == null || format == null) {
			return null;
		}
		DateTimeFormatter fmt = new DateTimeFormatterBuilder().appendPattern(format).toFormatter();
		return date.format(fmt);
	}

	/**
	 * Formats a LocalDateTime according to the specified format
	 * 
	 * @param dateTime
	 *            the DateTime to format
	 * @param format
	 *            the desired format
	 * @return
	 */
	public static String formatDateTime(LocalDateTime dateTime, String format) {
		if (dateTime == null || format == null) {
			return null;
		}
		DateTimeFormatter fmt = new DateTimeFormatterBuilder().appendPattern(format).toFormatter();
		return dateTime.format(fmt);
	}

	/**
	 * Formats a Java 8 date/time/datetime based on the specified format
	 * 
	 * @param clazz
	 *            the class of the object to format
	 * @param value
	 *            the value
	 * @param format
	 *            the desired foratm
	 * @return
	 */
	public static String formatJava8Date(Class<?> clazz, Object value, String format) {
		if (LocalDate.class.equals(clazz)) {
			return formatDate((LocalDate) value, format);
		} else if (LocalDateTime.class.equals(clazz)) {
			return formatDateTime((LocalDateTime) value, format);
		} else if (LocalTime.class.equals(clazz)) {
			return formatTime((LocalTime) value, format);
		} else if (ZonedDateTime.class.equals(clazz)) {
			return formatZonedDateTime((ZonedDateTime) value, format);
		}
		return null;
	}

	/**
	 * Formats a java.time.LocalTime according to the specified format
	 * 
	 * @param time
	 *            the objec to format
	 * @param format
	 *            the desired format
	 * @return
	 */
	public static String formatTime(LocalTime time, String format) {
		if (time == null || format == null) {
			return null;
		}
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
		return time.format(fmt);
	}

	/**
	 * Formats a ZonedDateTime according to the specified format
	 * 
	 * @param dateTime
	 *            the DateTime to format
	 * @param format
	 *            the desired format
	 * @return
	 */
	public static String formatZonedDateTime(ZonedDateTime dateTime, String format) {
		if (dateTime == null || format == null) {
			return null;
		}
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
		return dateTime.format(fmt);
	}

	/**
	 * Return the week nuymber (1 - 53) of the last week of the specified year
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
	 *            the week code
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
	 * Returns the quarter of the year of a date, as an integer (1 to 4). Returns -1
	 * in case the argument passed to this function is null
	 * 
	 * @param date
	 *            the date
	 * @return
	 */
	public static int getQuarter(Date date) {
		return getQuarter(toLocalDate(date));
	}

	/**
	 * Returns the quarter of the year of a date, as an integer (1 to 4). Returns -1
	 * in case the argument passed to this function is null
	 * 
	 * @param date
	 *            the date
	 * @return
	 */
	public static int getQuarter(LocalDate date) {
		if (date == null) {
			return -1;
		}
		return 1 + date.getMonth().ordinal() / 3;
	}

	/**
	 * 
	 * @param weekCode
	 * @return
	 */
	public static LocalDate getStartDateOfWeek(String weekCode) {
		return toLocalDate(getStartDateOfWeekLegacy(weekCode));
	}

	/**
	 * Translates a week code (yyyy-ww) to the starting day (this is taken to be a
	 * Monday) of that week
	 * 
	 * @param weekCode
	 *            the week code
	 * @return the date
	 */
	public static Date getStartDateOfWeekLegacy(String weekCode) {
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

	private static int getWeekFromWeekCode(String weekCode) {
		return Integer.parseInt(weekCode.substring(5));
	}

	/**
	 * Retrieves the date from a year
	 * 
	 * @param date
	 *            the date
	 * @return
	 */
	public static Integer getYearFromDate(Date date) {
		if (date == null) {
			return null;
		}
		return toLocalDate(date).getYear();
	}

	/**
	 * Retrieves the year part from a week code (yyyy-ww)
	 * 
	 * @param weekCode
	 *            the week code
	 * @return
	 */
	private static int getYearFromWeekCode(String weekCode) {
		return Integer.parseInt(weekCode.substring(0, YEAR_STRING_LENGTH));
	}

	/**
	 * Çhecks whether a class represents a Java 8 date or time type
	 * 
	 * @param clazz
	 *            the class
	 * @return
	 */
	public static boolean isJava8DateType(Class<?> clazz) {
		return LocalDate.class.equals(clazz) || LocalDateTime.class.equals(clazz) || LocalTime.class.equals(clazz)
				|| ZonedDateTime.class.equals(clazz);
	}

	/**
	 * Checks if a class represents a supported Date time. This includes the Java 8
	 * date types and the legacy java.util.Date
	 * 
	 * @param clazz
	 *            the class to check
	 * @return
	 */
	public static boolean isSupportedDateType(Class<?> clazz) {
		return Date.class.isAssignableFrom(clazz) || isJava8DateType(clazz);
	}

	/**
	 * Checks if a string represents a valid week code (yyyy-ww). An empty String is
	 * considered valid
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
	 * Converts a java.time.LocalDate to a java.util.Date
	 * 
	 * @param d
	 *            the date
	 * @return
	 */
	public static Date toLegacyDate(LocalDate d) {
		if (d == null) {
			return null;
		}
		return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Converts a java.time.LocalDateTime to a java.util.Date
	 * 
	 * @param d
	 *            the LocalDatetime to convert
	 * @return
	 */
	public static Date toLegacyDate(LocalDateTime ldt) {
		if (ldt == null) {
			return null;
		}
		return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Converts any of the Java 8 date type to a legacy java.util.Date
	 * 
	 * @param t
	 *            the date to convert
	 * @return
	 */
	public static Date toLegacyDate(Temporal t) {
		if (t instanceof LocalDate) {
			return toLegacyDate((LocalDate) t);
		} else if (t instanceof LocalDateTime) {
			return toLegacyDate((LocalDateTime) t);
		} else if (t instanceof LocalTime) {
			return toLegacyTime((LocalTime) t);
		} else if (t instanceof ZonedDateTime) {
			return toLegacyDate((ZonedDateTime) t);
		}
		return null;
	}

	/**
	 * Converts a java.time.LocalDateTime to a java.util.Date
	 * 
	 * @param d
	 *            the LocalDatetime to convert
	 * @return
	 */
	public static Date toLegacyDate(ZonedDateTime zdt) {
		if (zdt == null) {
			return null;
		}
		return Date.from(zdt.toInstant());
	}

	/**
	 * Converts a java.time.LocalTime to a legacy date
	 * 
	 * @param lt
	 *            the LocalTime to convert
	 * @return
	 */
	public static Date toLegacyTime(LocalTime lt) {
		if (lt == null) {
			return null;
		}
		return Date.from(lt.atDate(createLocalDate("01012000")).atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Converts a java.util.Date to a LocalDate
	 * 
	 * @param d
	 *            the date to convert
	 * @return
	 */
	public static LocalDate toLocalDate(Date d) {
		if (d == null) {
			return null;
		}

		return convertSQLDate(d).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	/**
	 * Converts a java.util.Date to a LocalDateTime
	 * 
	 * @param d
	 *            the date
	 * @return
	 */
	public static LocalDateTime toLocalDateTime(Date d) {
		if (d == null) {
			return null;
		}
		return convertSQLDate(d).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	public static LocalTime toLocalTime(Date d) {
		if (d == null) {
			return null;
		}
		return convertSQLDate(d).toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
	}

	/**
	 * Creates a LocalDate that represents the first day of the week corresponding
	 * to the provided week code
	 * 
	 * @param weekCode
	 *            the week code
	 * @return
	 */
	public static LocalDate toStartDateOfWeek(String weekCode) {
		return toLocalDate(toStartDateOfWeekLegacy(weekCode));
	}

	/**
	 * Translates a week code (yyyy-ww) to the starting day (this is taken to be a
	 * Monday) of that week
	 * 
	 * @param weekCode
	 *            the week code
	 * @return the date
	 */
	public static Date toStartDateOfWeekLegacy(String weekCode) {
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

			// if the week number is 53 but we are in January, then reduce the
			// year by one
			if ((week == LAST_WEEK_NUMBER || week == LAST_WEEK_NUMBER - 1) && month == Calendar.JANUARY) {
				year--;
			}

			return year + "-" + StringUtils.leftPad(Integer.toString(week), 2, "0");
		}
		return null;
	}

	/**
	 * Converts the provided LocalDate to a week code
	 * 
	 * @param d
	 *            date
	 * @return
	 */
	public static String toWeekCode(LocalDate d) {
		return toWeekCode(toLegacyDate(d));
	}

	/**
	 * 
	 * @param d
	 * @return
	 */
	public static ZonedDateTime toZonedDateTime(Date d) {
		if (d == null) {
			return null;
		}
		return convertSQLDate(d).toInstant().atOffset(ZoneOffset.UTC).toZonedDateTime();
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

}
