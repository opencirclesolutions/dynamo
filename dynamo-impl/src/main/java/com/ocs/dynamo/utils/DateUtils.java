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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

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
	 * Creates a java.time.LocalDate based on a String representation
	 * 
	 * @param dateStr the String (in the format ddMMyyyy)
	 * @return the LocalDate
	 */
	public static LocalDate createLocalDate(String dateStr) {
		return createLocalDate(dateStr, DATE_FORMAT);
	}

	/**
	 * Creates a java.time.LocalDate based on a String representation
	 * 
	 * @param dateStr the String
	 * @param format  the desired format
	 * @return the LocalDate
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
	 * @param dateTimeStr the String representation (ddMMyyyy HHmmss)
	 * @return the constructed LocalDateTime
	 */
	public static LocalDateTime createLocalDateTime(String dateTimeStr) {
		return createLocalDateTime(dateTimeStr, DATE_TIME_FORMAT);
	}

	/**
	 * Creates a java.time.LocalDateTime based on a String representation
	 * 
	 * @param dateTimeStr the String representation
	 * @param format      the desired format
	 * @return the constructed LocalDateTime
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
	 * @param timeStr the String representation
	 * @return the constructed LocalTime
	 */
	public static LocalTime createLocalTime(String timeStr) {
		return createLocalTime(timeStr, TIME_FORMAT);
	}

	/**
	 * Creates a java.time.LocalTime based on a String representation
	 * 
	 * @param timeStr the String representation
	 * @param format  the desired format
	 * @return the constructed LocalTime
	 */
	public static LocalTime createLocalTime(String timeStr, String format) {
		if (timeStr == null) {
			return null;
		}
		DateTimeFormatter fmt = new DateTimeFormatterBuilder().appendPattern(format).parseStrict().toFormatter();
		return LocalTime.from(fmt.parse(timeStr));
	}

	/**
	 * Create a ZonedDateTime based from a String, using the default format
	 * 
	 * @param dateTimeStr the String to parse
	 * @return the constructed ZonedDateTime
	 */
	public static ZonedDateTime createZonedDateTime(String dateTimeStr) {
		return createZonedDateTime(dateTimeStr, SystemPropertyUtils.getDefaultDateTimeWithTimezoneFormat());
	}

	/**
	 * Creates a ZonedDateTime from a String, using the provided format
	 * 
	 * @param dateTimeStr the String
	 * @param format      the format
	 * @return the constructed ZonedDateTime
	 */
	public static ZonedDateTime createZonedDateTime(String dateTimeStr, String format) {
		if (dateTimeStr == null) {
			return null;
		}
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
		return ZonedDateTime.from(fmt.parse(dateTimeStr));
	}

	/**
	 * Formats a LocalDate according to the specified format
	 * 
	 * @param date   the date
	 * @param format the format
	 * @return the String representation of the date
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
	 * @param dateTime the DateTime to format
	 * @param format   the desired format
	 * @return the String representation of the datetime
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
	 * @param clazz  the class of the object to format
	 * @param value  the value
	 * @param format the desired format
	 * @return the resulting String
	 */
	public static String formatJava8Date(Class<?> clazz, Object value, String format, ZoneId zoneId) {
		if (LocalDate.class.equals(clazz)) {
			return formatDate((LocalDate) value, format);
		} else if (LocalDateTime.class.equals(clazz)) {
			return formatDateTime((LocalDateTime) value, format);
		} else if (LocalTime.class.equals(clazz)) {
			return formatTime((LocalTime) value, format);
		} else if (ZonedDateTime.class.equals(clazz)) {
			return formatZonedDateTime((ZonedDateTime) value, format, zoneId);
		}
		return null;
	}

	/**
	 * Formats a java.time.LocalTime according to the specified format
	 * 
	 * @param time   the objec to format
	 * @param format the desired format
	 * @return the resulting String
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
	 * @param dateTime the DateTime to format
	 * @param format   the desired format
	 * @return the resulting String
	 */
	public static String formatZonedDateTime(ZonedDateTime dateTime, String format, ZoneId zoneId) {
		if (dateTime == null || format == null) {
			return null;
		}
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format).withZone(zoneId);
		return dateTime.format(fmt);
	}

	/**
	 * Return the week number (1 - 53) of the last week of the specified year
	 * 
	 * @param year the year
	 * @return the resulting String
	 */
	public static int getLastWeekOfYear(int year) {
		LocalDate date = createLocalDate("3112" + year);
		WeekFields weekFields = WeekFields.ISO;
		int weekNumber = date.get(weekFields.weekOfWeekBasedYear());
		while (weekNumber == 1) {
			date = date.minusDays(1);
			weekNumber = date.get(weekFields.weekOfWeekBasedYear());
		}
		return weekNumber;
	}

	/**
	 * Returns the next week code given an existing week code
	 * 
	 * @param weekCode the week code
	 * @return the resulting String
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
	 * @param date the date
	 * @return the resulting String
	 */
	public static int getQuarter(LocalDate date) {
		if (date == null) {
			return -1;
		}
		return 1 + date.getMonth().ordinal() / 3;
	}

	/**
	 * Translates a week code (yyyy-ww) to the starting day (this is taken to be a
	 * Monday) of that week
	 *
	 * @param weekCode the week code
	 * @return the date
	 */
	public static LocalDate getStartDateOfWeek(String weekCode) {
		if (weekCode == null) {
			return null;
		}
		if (weekCode.matches(WEEK_CODE_PATTERN)) {
			int year = getYearFromWeekCode(weekCode);
			int week = getWeekFromWeekCode(weekCode);
			WeekFields weekFields = WeekFields.ISO;
			return LocalDate.ofYearDay(year, 1).with(weekFields.weekOfYear(), week)
					.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		}
		throw new OCSRuntimeException("Cannot convert");
	}

	private static int getWeekFromWeekCode(String weekCode) {
		return Integer.parseInt(weekCode.substring(5));
	}

	/**
	 * Retrieves the year part from a week code (yyyy-ww)
	 * 
	 * @param weekCode the week code
	 * @return the year
	 */
	private static int getYearFromWeekCode(String weekCode) {
		return Integer.parseInt(weekCode.substring(0, YEAR_STRING_LENGTH));
	}

	/**
	 * Checks whether a class represents a Java 8 date or time type
	 * 
	 * @param clazz the class
	 * @return true if this is the case, false otherwise
	 */
	public static boolean isJava8DateType(Class<?> clazz) {
		return LocalDate.class.equals(clazz) || LocalDateTime.class.equals(clazz) || LocalTime.class.equals(clazz)
				|| ZonedDateTime.class.equals(clazz);
	}

	/**
	 * Checks if a class represents a supported Date time. This includes the Java 8
	 * date types and the legacy java.time.LocalTime
	 *
	 * @param clazz the class to check
	 * @return true if the
	 */
	public static boolean isSupportedDateType(Class<?> clazz) {
		return isJava8DateType(clazz);
	}

	/**
	 * Checks if a string represents a valid week code (yyyy-ww). An empty String is
	 * considered valid
	 * 
	 * @param weekCode the week code
	 * @return true if the value is a valid week code, false otherwise
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
	 * Converts a LocalDate to a legacy java.util.Date
	 * 
	 * @param date the LocalDate to convert
	 * @return the value converted to a java.util.Date
	 */
	public static Date toLegacyDate(LocalDate date) {
		if (date == null) {
			return null;
		}
		return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Converts a java.time.LocalDateTime to a java.util.Date
	 * 
	 * @param ldt the LocalDatetime to convert
	 * @return the value converted to a java.util.Date
	 */
	public static Date toLegacyDate(LocalDateTime ldt) {
		if (ldt == null) {
			return null;
		}
		return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Converts a date to its corresponding week code
	 * 
	 * @param date the date
	 * @return the resulting week code
	 */
	public static String toWeekCode(LocalDate date) {
		if (date != null) {
			WeekFields weekFields = WeekFields.ISO;
			int year = date.getYear();
			int week = date.get(weekFields.weekOfWeekBasedYear());
			Month month = date.getMonth();

			// if the week number is reported as 1, but we are in December,
			// then we have an "overflow"
			if (week == FIRST_WEEK_NUMBER && month == Month.DECEMBER) {
				year++;
			}

			// if the week number is 53, but we are in January, then reduce the
			// year by one
			if ((week == LAST_WEEK_NUMBER || week == LAST_WEEK_NUMBER - 1) && month == Month.JANUARY) {
				year--;
			}

			return year + "-" + StringUtils.leftPad(Integer.toString(week), 2, "0");
		}
		return null;
	}

}
