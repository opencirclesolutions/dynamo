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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

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
		Assert.assertEquals("Thu Jan 01 07:08:09 CET 2015", d.toString());

		d = DateUtils.createDateTime("29022012 070809");
		Assert.assertEquals("Wed Feb 29 07:08:09 CET 2012", d.toString());
	}

	@Test
	public void testCreateLocalDate() {
		Assert.assertNull(DateUtils.createLocalDate(null));

		LocalDate ld = DateUtils.createLocalDate("01012015");
		Assert.assertEquals("2015-01-01", ld.toString());

		ld = DateUtils.createLocalDate("29022012");
		Assert.assertEquals("2012-02-29", ld.toString());
	}

	@Test
	public void testCreateLocalDateTime() {
		Assert.assertNull(DateUtils.createLocalDateTime(null));

		LocalDateTime ld = DateUtils.createLocalDateTime("01012015 121314");
		Assert.assertEquals("2015-01-01T12:13:14", ld.toString());

		ld = DateUtils.createLocalDateTime("29022012 151617");
		Assert.assertEquals("2012-02-29T15:16:17", ld.toString());
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
	public void testFormatLocalDate() {
		Date date = DateUtils.createDate("01042015");

		Assert.assertNull(DateUtils.formatDate((LocalDate) null, "dd-MM-yyyy"));
		Assert.assertNull(DateUtils.formatDate(date, null));

		Assert.assertEquals("01-04-2015", DateUtils.formatDate(date, "dd-MM-yyyy"));
		Assert.assertEquals("01/04/15", DateUtils.formatDate(date, "dd/MM/yy"));
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
	public void testGetLastWeekOfYear() {

		Assert.assertEquals(52, DateUtils.getLastWeekOfYear(2017));
		Assert.assertEquals(52, DateUtils.getLastWeekOfYear(2016));
		Assert.assertEquals(53, DateUtils.getLastWeekOfYear(2015));
		Assert.assertEquals(53, DateUtils.getLastWeekOfYear(2009));
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
		quarter = DateUtils.getQuarter(DateUtils.createLocalDate("01042014"));
		Assert.assertEquals(2, quarter);
		quarter = DateUtils.getQuarter(DateUtils.createLocalDate("01072014"));
		Assert.assertEquals(3, quarter);
		quarter = DateUtils.getQuarter(DateUtils.createLocalDate("01112014"));
		Assert.assertEquals(4, quarter);
	}

	@Test
	public void testGetStartDateOfWeek() {
		Assert.assertEquals(DateUtils.createLocalDate("30122013"), DateUtils.toStartDateOfWeek("2014-01"));
		Assert.assertEquals(DateUtils.createLocalDate("06012014"), DateUtils.toStartDateOfWeek("2014-02"));
		Assert.assertEquals(DateUtils.createLocalDate("29122014"), DateUtils.toStartDateOfWeek("2015-01"));
		Assert.assertEquals(DateUtils.createLocalDate("05012015"), DateUtils.toStartDateOfWeek("2015-02"));
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
	public void testIsValidWeekCode() {

		Assert.assertTrue(DateUtils.isValidWeekCode(null));

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
	public void testToLocalDate() {
		Assert.assertNull(DateUtils.toLocalDate(null));

		Date date = DateUtils.createDate("01042010");
		LocalDate ld = DateUtils.toLocalDate(date);
		Assert.assertEquals("2010-04-01", ld.toString());

		date = DateUtils.createDate("29022012");
		ld = DateUtils.toLocalDate(date);
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
