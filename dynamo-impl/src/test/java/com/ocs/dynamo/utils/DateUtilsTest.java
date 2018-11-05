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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.exception.OCSRuntimeException;

public class DateUtilsTest {

	@Test
	public void testCreateDate() {
		Assert.assertNull(DateUtils.createDate(null));
		Date d = DateUtils.createDate("01012015");
		Assert.assertEquals("01-01-2015", DateUtils.formatDate(d, "dd-MM-yyyy"));

		d = DateUtils.createDate("28022015");
		Assert.assertEquals("28-02-2015", DateUtils.formatDate(d, "dd-MM-yyyy"));
	}

	@Test
	public void testCreateDateTime() {
		Assert.assertNull(DateUtils.createDate(null));
		Date d = DateUtils.createDateTime("01012015 070809");
		Assert.assertTrue(d.toString().startsWith("Thu Jan 01 07:08:09"));

		d = DateUtils.createDateTime("29022012 070809");
		Assert.assertTrue(d.toString().startsWith("Wed Feb 29 07:08:09"));
	}

	@Test
	public void testCreateJava8Date() {
		Assert.assertNull(DateUtils.createJava8Date(LocalDate.class, null, "dd-MM-yyyy"));
		Assert.assertNull(DateUtils.createJava8Date(Date.class, "01-01-1980", "dd-MM-yyyy"));

		LocalDate d = DateUtils.createJava8Date(LocalDate.class, "01-01-1980", "dd-MM-yyyy");
		Assert.assertEquals("1980-01-01", d.toString());

		LocalTime lt = DateUtils.createJava8Date(LocalTime.class, "14:15:16", "HH:mm:ss");
		Assert.assertEquals("14:15:16", lt.toString());

		LocalDateTime ldt = DateUtils.createJava8Date(LocalDateTime.class, "01-02-1981 14:15:16",
				"dd-MM-yyyy HH:mm:ss");
		Assert.assertEquals("1981-02-01T14:15:16", ldt.toString());

		ZonedDateTime zdt = DateUtils.createJava8Date(ZonedDateTime.class, "01-02-1981 14:15:16+0100",
				"dd-MM-yyyy HH:mm:ssZ");
		Assert.assertEquals("1981-02-01T14:15:16+01:00", zdt.toString());
	}

	@Test
	public void testCreateLocalDate() {
		Assert.assertNull(DateUtils.createLocalDate(null));

		LocalDate ld = DateUtils.createLocalDate("01012015");
		Assert.assertEquals("2015-01-01", ld.toString());

		ld = DateUtils.createLocalDate("29022012");
		Assert.assertEquals("2012-02-29", ld.toString());

		ld = DateUtils.createLocalDate("29-02-2012", "dd-MM-yyyy");
		Assert.assertEquals("2012-02-29", ld.toString());
	}

	@Test
	public void testCreateLocalDateTime() {
		Assert.assertNull(DateUtils.createLocalDateTime(null));

		LocalDateTime ld = DateUtils.createLocalDateTime("01012015 121314");
		Assert.assertEquals("2015-01-01T12:13:14", ld.toString());

		ld = DateUtils.createLocalDateTime("29022012 151617");
		Assert.assertEquals("2012-02-29T15:16:17", ld.toString());

		ld = DateUtils.createLocalDateTime("29-02-2012 15:16:17", "dd-MM-yyyy HH:mm:ss");
		Assert.assertEquals("2012-02-29T15:16:17", ld.toString());
	}

	@Test
	public void testCreateLocalTime() {
		Assert.assertNull(DateUtils.createLocalTime(null));

		LocalTime lt = DateUtils.createLocalTime("121314");
		Assert.assertEquals("12:13:14", lt.toString());

		lt = DateUtils.createLocalTime("151617");
		Assert.assertEquals("15:16:17", lt.toString());

		lt = DateUtils.createLocalTime("15:16:17", "HH:mm:ss");
		Assert.assertEquals("15:16:17", lt.toString());
	}

	@Test
	public void testCreateTime() {
		Assert.assertNull(DateUtils.createTime(null));
		Assert.assertTrue(DateUtils.createTime("041234").toString().contains("04:12:34"));

		// invalid time
		try {
			DateUtils.createTime("256374").toString();
			Assert.fail();
		} catch (OCSRuntimeException ex) {
			// expected
		}
	}

	@Test
	public void testCreateZonedDateTime() {
		Assert.assertNull(DateUtils.createZonedDateTime(null));

		ZonedDateTime zdt = DateUtils.createZonedDateTime("01-01-2017 12:13:14+0100", "dd-MM-yyyy HH:mm:ssZ");
		Assert.assertEquals("2017-01-01T12:13:14+01:00", zdt.toString());

		zdt = DateUtils.createZonedDateTime("01-01-2017 12:13:14Z", "dd-MM-yyyy HH:mm:ssz");
		Assert.assertEquals("2017-01-01T12:13:14Z", zdt.toString());

		// with default
		zdt = DateUtils.createZonedDateTime("01-01-2017 12:13:14+0000");
		Assert.assertEquals("2017-01-01T12:13:14Z", zdt.toString());
	}

	@Test
	public void testFormatDate() {
		Date date = DateUtils.createDate("01042015");

		Assert.assertNull(DateUtils.formatDate((Date) null, "dd-MM-yyyy"));
		Assert.assertNull(DateUtils.formatDate(date, null));

		Assert.assertEquals("01-04-2015", DateUtils.formatDate(date, "dd-MM-yyyy"));
		Assert.assertEquals("01/04/15", DateUtils.formatDate(date, "dd/MM/yy"));
	}

	@Test
	public void testFormatLocalDateTime() {
		LocalDateTime date = DateUtils.createLocalDateTime("01122013 111415");

		Assert.assertNull(DateUtils.formatDateTime((LocalDateTime) null, "dd-MM-yyyy"));
		Assert.assertNull(DateUtils.formatDateTime(date, null));

		Assert.assertEquals("01-12-2013 11:14:15", DateUtils.formatDateTime(date, "dd-MM-yyyy HH:mm:ss"));
		Assert.assertEquals("01/12/2013 11:14:15", DateUtils.formatDateTime(date, "dd/MM/yyyy HH:mm:ss"));
	}

	@Test
	public void testFormatZonedDateTime() {
		ZonedDateTime date = DateUtils.createZonedDateTime("01-12-2013 11:14:15+0550");

		Assert.assertNull(DateUtils.formatZonedDateTime(null, "dd-MM-yyyy HH:mm:ssz"));
		Assert.assertNull(DateUtils.formatZonedDateTime(date, null));

		Assert.assertEquals("01-12-2013 11:14:15+05:50", DateUtils.formatZonedDateTime(date, "dd-MM-yyyy HH:mm:ssz"));
		Assert.assertEquals("01-12-2013 11:14:15+0550", DateUtils.formatZonedDateTime(date, "dd-MM-yyyy HH:mm:ssZ"));
	}

	@Test
	public void testFormatJava8Date() {
		Assert.assertEquals("31-03-2014",
				DateUtils.formatJava8Date(LocalDate.class, DateUtils.createLocalDate("31032014"), "dd-MM-yyyy"));

		Assert.assertEquals("31-03-2014 05:06:07", DateUtils.formatJava8Date(LocalDateTime.class,
				DateUtils.createLocalDateTime("31032014 050607"), "dd-MM-yyyy HH:mm:ss"));

		Assert.assertEquals("05:06:07",
				DateUtils.formatJava8Date(LocalTime.class, DateUtils.createLocalTime("050607"), "HH:mm:ss"));

		Assert.assertNull(DateUtils.formatJava8Date(Date.class, DateUtils.createDate("31032014"), "dd-MM-yyyy"));
	}

	@Test
	public void testFormatLocalDate() {
		Date date = DateUtils.createDate("01042015");

		Assert.assertNull(DateUtils.formatDate((LocalDate) null, "dd-MM-yyyy"));
		Assert.assertNull(DateUtils.formatDate(date, null));

		Assert.assertEquals("01-04-2015", DateUtils.formatDate(date, "dd-MM-yyyy"));
		Assert.assertEquals("01/04/15", DateUtils.formatDate(date, "dd/MM/yy"));
	}

	@Test
	public void testFormatLocalTime() {
		LocalTime t = DateUtils.createLocalTime("111415");
		Assert.assertNull(DateUtils.formatTime(null, "dd-MM-yyyy"));
		Assert.assertNull(DateUtils.formatTime(t, null));
		Assert.assertEquals("11:14:15", DateUtils.formatTime(t, "HH:mm:ss"));
		Assert.assertEquals("11.14.15", DateUtils.formatTime(t, "HH.mm.ss"));
	}

	@Test
	public void testGetLastWeekOfYear() {
		Assert.assertEquals(52, DateUtils.getLastWeekOfYear(2017));
		Assert.assertEquals(52, DateUtils.getLastWeekOfYear(2016));
		Assert.assertEquals(53, DateUtils.getLastWeekOfYear(2015));
		Assert.assertEquals(53, DateUtils.getLastWeekOfYear(2009));
	}

	@Test
	public void testGetNextWeekCode() {
		Assert.assertEquals("2015-02", DateUtils.getNextWeekCode("2015-01"));
		Assert.assertEquals("2015-03", DateUtils.getNextWeekCode("2015-02"));
		Assert.assertEquals("2015-53", DateUtils.getNextWeekCode("2015-52"));
		Assert.assertEquals("2016-01", DateUtils.getNextWeekCode("2015-53"));
		Assert.assertEquals("2017-01", DateUtils.getNextWeekCode("2016-52"));
	}

	@Test
	public void testGetQuarter() {
		int quarter = DateUtils.getQuarter((Date) null);
		Assert.assertEquals(-1, quarter);

		quarter = DateUtils.getQuarter(DateUtils.createDate("01012014"));
		Assert.assertEquals(1, quarter);
		quarter = DateUtils.getQuarter(DateUtils.createDate("01042014"));
		Assert.assertEquals(2, quarter);
		quarter = DateUtils.getQuarter(DateUtils.createDate("01072014"));
		Assert.assertEquals(3, quarter);
		quarter = DateUtils.getQuarter(DateUtils.createDate("01112014"));
		Assert.assertEquals(4, quarter);

		quarter = DateUtils.getQuarter(DateUtils.createLocalDate("01012014"));
		Assert.assertEquals(1, quarter);
		quarter = DateUtils.getQuarter(DateUtils.createLocalDate("31032014"));
		Assert.assertEquals(1, quarter);
		quarter = DateUtils.getQuarter(DateUtils.createLocalDate("01042014"));
		Assert.assertEquals(2, quarter);
		quarter = DateUtils.getQuarter(DateUtils.createLocalDate("01072014"));
		Assert.assertEquals(3, quarter);
		quarter = DateUtils.getQuarter(DateUtils.createLocalDate("01112014"));
		Assert.assertEquals(4, quarter);
	}

	@Test
	public void testGetStartDateOfWeek() {
		
		System.out.println("2014-01".matches("\\d{4}-\\d{2}"));
		
		Assert.assertEquals(DateUtils.createLocalDate("30122013"), DateUtils.toStartDateOfWeek("2014-01"));
		Assert.assertEquals(DateUtils.createLocalDate("06012014"), DateUtils.toStartDateOfWeek("2014-02"));
		Assert.assertEquals(DateUtils.createLocalDate("29122014"), DateUtils.toStartDateOfWeek("2015-01"));
		Assert.assertEquals(DateUtils.createLocalDate("05012015"), DateUtils.toStartDateOfWeek("2015-02"));
	}

	@Test
	public void testGetStartDayOfWeek() {
		LocalDate ld = DateUtils.getStartDateOfWeek("2013-51");
		Assert.assertEquals("2013-12-16", ld.toString());

		ld = DateUtils.getStartDateOfWeek("2013-52");
		Assert.assertEquals("2013-12-23", ld.toString());

		ld = DateUtils.getStartDateOfWeek("2014-01");
		Assert.assertEquals("2013-12-30", ld.toString());

		ld = DateUtils.getStartDateOfWeek("2015-53");
		Assert.assertEquals("2015-12-28", ld.toString());
	}

	@Test
	public void testGetYear() {
		Assert.assertNull(DateUtils.getYearFromDate(null));

		Date date = DateUtils.createDate("01042010");

		Assert.assertEquals(2010, DateUtils.getYearFromDate(date).intValue());

		Assert.assertEquals(2015, DateUtils.getYearFromDate(DateUtils.createDate("31122015")).intValue());
		Assert.assertEquals(2016, DateUtils.getYearFromDate(DateUtils.createDate("01012016")).intValue());
	}

	@Test
	public void testIsJava8DateType() {
		Assert.assertTrue(DateUtils.isJava8DateType(LocalDate.class));
		Assert.assertTrue(DateUtils.isJava8DateType(LocalTime.class));
		Assert.assertTrue(DateUtils.isJava8DateType(LocalDateTime.class));

		Assert.assertFalse(DateUtils.isJava8DateType(Date.class));
		Assert.assertFalse(DateUtils.isJava8DateType(java.sql.Date.class));
		Assert.assertFalse(DateUtils.isJava8DateType(Timestamp.class));
	}

	@Test
	public void testIsValidWeekCode() {

		Assert.assertTrue(DateUtils.isValidWeekCode(null));

		Assert.assertFalse(DateUtils.isValidWeekCode("abc"));
		Assert.assertFalse(DateUtils.isValidWeekCode("2013"));
		Assert.assertFalse(DateUtils.isValidWeekCode("2013-1"));

		Assert.assertFalse(DateUtils.isValidWeekCode("2013-00"));
		Assert.assertTrue(DateUtils.isValidWeekCode("2013-01"));
		Assert.assertTrue(DateUtils.isValidWeekCode("2013-02"));
		Assert.assertTrue(DateUtils.isValidWeekCode("2013-52"));
		Assert.assertFalse(DateUtils.isValidWeekCode("2013-53"));
		Assert.assertFalse(DateUtils.isValidWeekCode("2014-53"));

		// 2015 actually has a week 53
		Assert.assertTrue(DateUtils.isValidWeekCode("2015-53"));
		Assert.assertFalse(DateUtils.isValidWeekCode("2015-54"));
	}

	@Test
	public void testToLegacyDate1() {
		Assert.assertNull(DateUtils.toLegacyDate((LocalDate) null));

		LocalDate ld = DateUtils.createLocalDate("14102015");
		Assert.assertEquals(DateUtils.createDate("14102015"), DateUtils.toLegacyDate(ld));
	}

	@Test
	public void testToLegacyDate2() {
		Assert.assertNull(DateUtils.toLegacyDate((LocalDateTime) null));

		LocalDateTime ldt = DateUtils.createLocalDateTime("14102015 170323");
		Assert.assertEquals(DateUtils.createDateTime("14102015 170323"), DateUtils.toLegacyDate(ldt));
	}

	@Test
	public void testToLegacyDate3() {
		Date d = DateUtils.toLegacyDate(LocalTime.of(12, 13));
		Assert.assertTrue(d.toString().contains("12:13:00"));

		d = DateUtils.toLegacyDate(LocalTime.of(23, 04));
		Assert.assertTrue(d.toString().contains("23:04:00"));
	}

	@Test
	public void testToLegacyDate4() {
		Date d = DateUtils.toLegacyDate(ZonedDateTime.of(LocalDateTime.of(2014, 3, 1, 12, 13), ZoneId.of("UTC")));
		ZonedDateTime zdt = DateUtils.toZonedDateTime(d);
		Assert.assertEquals("2014-03-01T12:13Z", zdt.toString());
	}

	@Test
	public void testToLegacyTime() {
		Assert.assertNull(DateUtils.toLegacyTime(null));

		LocalTime lt = DateUtils.createLocalTime("111213");
		Date d = DateUtils.toLegacyTime(lt);
		Assert.assertTrue(d.toString().contains("11:12:13"));
	}

	@Test
	public void testToLocalDate() {
		Assert.assertNull(DateUtils.toLocalDate(null));

		Date date = DateUtils.createDate("01042010");
		LocalDate ld = DateUtils.toLocalDate(date);
		Assert.assertEquals("2010-04-01", ld.toString());

		date = DateUtils.createDate("29022012");
		ld = DateUtils.toLocalDate(date);
		Assert.assertEquals("2012-02-29", ld.toString());

		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
		ld = DateUtils.toLocalDate(sqlDate);
		Assert.assertEquals("2012-02-29", ld.toString());
	}

	@Test
	public void testToLocalDateTime() {
		Assert.assertNull(DateUtils.toLocalDateTime(null));

		Date date = DateUtils.createDate("01042010");
		LocalDateTime ld = DateUtils.toLocalDateTime(date);
		Assert.assertEquals("2010-04-01T00:00", ld.toString());

		date = DateUtils.createDateTime("01042010 110405");
		ld = DateUtils.toLocalDateTime(date);
		Assert.assertEquals("2010-04-01T11:04:05", ld.toString());

		date = DateUtils.createDateTime("01042010 222324");
		ld = DateUtils.toLocalDateTime(date);
		Assert.assertEquals("2010-04-01T22:23:24", ld.toString());

		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
		ld = DateUtils.toLocalDateTime(sqlDate);
		Assert.assertEquals("2010-04-01T22:23:24", ld.toString());
	}

	@Test
	public void testToLocalTime() {
		Assert.assertNull(DateUtils.toLocalTime(null));

		Date date = DateUtils.createTime("161718");
		LocalTime lt = DateUtils.toLocalTime(date);
		Assert.assertEquals("16:17:18", lt.toString());

		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
		lt = DateUtils.toLocalTime(sqlDate);
		Assert.assertEquals("16:17:18", lt.toString());
	}

	@Test
	public void testToWeekCode() {

		Assert.assertEquals("2013-51", DateUtils.toWeekCode(DateUtils.createDate("16122013")));
		Assert.assertEquals("2013-52", DateUtils.toWeekCode(DateUtils.createDate("23122013")));
		Assert.assertEquals("2014-01", DateUtils.toWeekCode(DateUtils.createDate("30122013")));
		Assert.assertEquals("2014-02", DateUtils.toWeekCode(DateUtils.createDate("06012014")));

		Assert.assertEquals("2015-52", DateUtils.toWeekCode(DateUtils.createDate("21122015")));
		Assert.assertEquals("2015-53", DateUtils.toWeekCode(DateUtils.createDate("28122015")));
		Assert.assertEquals("2016-01", DateUtils.toWeekCode(DateUtils.createDate("04012016")));
		Assert.assertEquals("2016-02", DateUtils.toWeekCode(DateUtils.createDate("11012016")));
	}
}
