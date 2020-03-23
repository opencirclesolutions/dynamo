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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.jupiter.api.Test;

public class DateUtilsTest {

    @Test
    public void testCreateJava8Date() {
        assertNull(DateUtils.createJava8Date(LocalDate.class, null, "dd-MM-yyyy"));
        assertNull(DateUtils.createJava8Date(Date.class, "01-01-1980", "dd-MM-yyyy"));

        LocalDate d = DateUtils.createJava8Date(LocalDate.class, "01-01-1980", "dd-MM-yyyy");
        assertEquals("1980-01-01", d.toString());

        LocalTime lt = DateUtils.createJava8Date(LocalTime.class, "14:15:16", "HH:mm:ss");
        assertEquals("14:15:16", lt.toString());

        LocalDateTime ldt = DateUtils.createJava8Date(LocalDateTime.class, "01-02-1981 14:15:16", "dd-MM-yyyy HH:mm:ss");
        assertEquals("1981-02-01T14:15:16", ldt.toString());

        ZonedDateTime zdt = DateUtils.createJava8Date(ZonedDateTime.class, "01-02-1981 14:15:16+0100", "dd-MM-yyyy HH:mm:ssZ");
        assertEquals("1981-02-01T14:15:16+01:00", zdt.toString());
    }

    @Test
    public void testCreateLocalDate() {
        assertNull(DateUtils.createLocalDate(null));

        LocalDate ld = DateUtils.createLocalDate("01012015");
        assertEquals("2015-01-01", ld.toString());

        ld = DateUtils.createLocalDate("29022012");
        assertEquals("2012-02-29", ld.toString());

        ld = DateUtils.createLocalDate("29-02-2012", "dd-MM-yyyy");
        assertEquals("2012-02-29", ld.toString());
    }

    @Test
    public void testCreateLocalDateTime() {
        assertNull(DateUtils.createLocalDateTime(null));

        LocalDateTime ld = DateUtils.createLocalDateTime("01012015 121314");
        assertEquals("2015-01-01T12:13:14", ld.toString());

        ld = DateUtils.createLocalDateTime("29022012 151617");
        assertEquals("2012-02-29T15:16:17", ld.toString());

        ld = DateUtils.createLocalDateTime("29-02-2012 15:16:17", "dd-MM-yyyy HH:mm:ss");
        assertEquals("2012-02-29T15:16:17", ld.toString());
    }

    @Test
    public void testCreateLocalTime() {
        assertNull(DateUtils.createLocalTime(null));

        LocalTime lt = DateUtils.createLocalTime("121314");
        assertEquals("12:13:14", lt.toString());

        lt = DateUtils.createLocalTime("151617");
        assertEquals("15:16:17", lt.toString());

        lt = DateUtils.createLocalTime("15:16:17", "HH:mm:ss");
        assertEquals("15:16:17", lt.toString());
    }

    @Test
    public void testCreateZonedDateTime() {
        assertNull(DateUtils.createZonedDateTime(null));

        ZonedDateTime zdt = DateUtils.createZonedDateTime("01-01-2017 12:13:14+0100", "dd-MM-yyyy HH:mm:ssZ");
        assertEquals("2017-01-01T12:13:14+01:00", zdt.toString());

        zdt = DateUtils.createZonedDateTime("01-01-2017 12:13:14Z", "dd-MM-yyyy HH:mm:ssz");
        assertEquals("2017-01-01T12:13:14Z", zdt.toString());

        // with default
        zdt = DateUtils.createZonedDateTime("01-01-2017 12:13:14+0000");
        assertEquals("2017-01-01T12:13:14Z", zdt.toString());
    }

    @Test
    public void testFormatDate() {
        LocalDate date = DateUtils.createLocalDate("01042015");

        assertNull(DateUtils.formatDate(null, "dd-MM-yyyy"));
        assertNull(DateUtils.formatDate(date, null));

        assertEquals("01-04-2015", DateUtils.formatDate(date, "dd-MM-yyyy"));
        assertEquals("01/04/15", DateUtils.formatDate(date, "dd/MM/yy"));
    }

    @Test
    public void testFormatLocalDateTime() {
        LocalDateTime date = DateUtils.createLocalDateTime("01122013 111415");

        assertNull(DateUtils.formatDateTime(null, "dd-MM-yyyy"));
        assertNull(DateUtils.formatDateTime(date, null));

        assertEquals("01-12-2013 11:14:15", DateUtils.formatDateTime(date, "dd-MM-yyyy HH:mm:ss"));
        assertEquals("01/12/2013 11:14:15", DateUtils.formatDateTime(date, "dd/MM/yyyy HH:mm:ss"));
    }

    @Test
    public void testFormatZonedDateTime() {
        ZonedDateTime date = DateUtils.createZonedDateTime("01-12-2013 11:14:15+0550");

        assertNull(DateUtils.formatZonedDateTime(null, "dd-MM-yyyy HH:mm:ssz"));
        assertNull(DateUtils.formatZonedDateTime(date, null));

        assertEquals("01-12-2013 11:14:15+05:50", DateUtils.formatZonedDateTime(date, "dd-MM-yyyy HH:mm:ssz"));
        assertEquals("01-12-2013 11:14:15+0550", DateUtils.formatZonedDateTime(date, "dd-MM-yyyy HH:mm:ssZ"));
    }

    @Test
    public void testFormatJava8Date() {
        assertEquals("31-03-2014", DateUtils.formatJava8Date(LocalDate.class, DateUtils.createLocalDate("31032014"), "dd-MM-yyyy"));

        assertEquals("31-03-2014 05:06:07",
                DateUtils.formatJava8Date(LocalDateTime.class, DateUtils.createLocalDateTime("31032014 050607"), "dd-MM-yyyy HH:mm:ss"));

        assertEquals("05:06:07", DateUtils.formatJava8Date(LocalTime.class, DateUtils.createLocalTime("050607"), "HH:mm:ss"));
    }

    @Test
    public void testFormatLocalTime() {
        LocalTime t = DateUtils.createLocalTime("111415");
        assertNull(DateUtils.formatTime(null, "dd-MM-yyyy"));
        assertNull(DateUtils.formatTime(t, null));
        assertEquals("11:14:15", DateUtils.formatTime(t, "HH:mm:ss"));
        assertEquals("11.14.15", DateUtils.formatTime(t, "HH.mm.ss"));
    }

    @Test
    public void testGetLastWeekOfYear() {
        assertEquals(52, DateUtils.getLastWeekOfYear(2017));
        assertEquals(52, DateUtils.getLastWeekOfYear(2016));
        assertEquals(53, DateUtils.getLastWeekOfYear(2015));
        assertEquals(53, DateUtils.getLastWeekOfYear(2009));
    }

    @Test
    public void testGetNextWeekCode() {
        assertEquals("2015-02", DateUtils.getNextWeekCode("2015-01"));
        assertEquals("2015-03", DateUtils.getNextWeekCode("2015-02"));
        assertEquals("2015-53", DateUtils.getNextWeekCode("2015-52"));
        assertEquals("2016-01", DateUtils.getNextWeekCode("2015-53"));
        assertEquals("2017-01", DateUtils.getNextWeekCode("2016-52"));
    }

    @Test
    public void testGetQuarter() {
        int quarter = DateUtils.getQuarter(null);
        assertEquals(-1, quarter);

        quarter = DateUtils.getQuarter(DateUtils.createLocalDate("01012014"));
        assertEquals(1, quarter);
        quarter = DateUtils.getQuarter(DateUtils.createLocalDate("01042014"));
        assertEquals(2, quarter);
        quarter = DateUtils.getQuarter(DateUtils.createLocalDate("01072014"));
        assertEquals(3, quarter);
        quarter = DateUtils.getQuarter(DateUtils.createLocalDate("01112014"));
        assertEquals(4, quarter);

        quarter = DateUtils.getQuarter(DateUtils.createLocalDate("01012014"));
        assertEquals(1, quarter);
        quarter = DateUtils.getQuarter(DateUtils.createLocalDate("31032014"));
        assertEquals(1, quarter);
        quarter = DateUtils.getQuarter(DateUtils.createLocalDate("01042014"));
        assertEquals(2, quarter);
        quarter = DateUtils.getQuarter(DateUtils.createLocalDate("01072014"));
        assertEquals(3, quarter);
        quarter = DateUtils.getQuarter(DateUtils.createLocalDate("01112014"));
        assertEquals(4, quarter);
    }

    @Test
    public void testGetStartDateOfWeek() {
        assertEquals(DateUtils.createLocalDate("30122013"), DateUtils.getStartDateOfWeek("2014-01"));
        assertEquals(DateUtils.createLocalDate("06012014"), DateUtils.getStartDateOfWeek("2014-02"));
        assertEquals(DateUtils.createLocalDate("29122014"), DateUtils.getStartDateOfWeek("2015-01"));
        assertEquals(DateUtils.createLocalDate("05012015"), DateUtils.getStartDateOfWeek("2015-02"));
    }

    @Test
    public void testGetStartDayOfWeek() {
        LocalDate ld = DateUtils.getStartDateOfWeek("2013-51");
        assertEquals("2013-12-16", ld.toString());

        ld = DateUtils.getStartDateOfWeek("2013-52");
        assertEquals("2013-12-23", ld.toString());

        ld = DateUtils.getStartDateOfWeek("2014-01");
        assertEquals("2013-12-30", ld.toString());

        ld = DateUtils.getStartDateOfWeek("2015-53");
        assertEquals("2015-12-28", ld.toString());
    }

    @Test
    public void testIsJava8DateType() {
        assertTrue(DateUtils.isJava8DateType(LocalDate.class));
        assertTrue(DateUtils.isJava8DateType(LocalTime.class));
        assertTrue(DateUtils.isJava8DateType(LocalDateTime.class));

        assertFalse(DateUtils.isJava8DateType(Date.class));
        assertFalse(DateUtils.isJava8DateType(java.sql.Date.class));
        assertFalse(DateUtils.isJava8DateType(Timestamp.class));
    }

    @Test
    public void testIsValidWeekCode() {

        assertTrue(DateUtils.isValidWeekCode(null));

        assertFalse(DateUtils.isValidWeekCode("abc"));
        assertFalse(DateUtils.isValidWeekCode("2013"));
        assertFalse(DateUtils.isValidWeekCode("2013-1"));

        assertFalse(DateUtils.isValidWeekCode("2013-00"));
        assertTrue(DateUtils.isValidWeekCode("2013-01"));
        assertTrue(DateUtils.isValidWeekCode("2013-02"));
        assertTrue(DateUtils.isValidWeekCode("2013-52"));
        assertFalse(DateUtils.isValidWeekCode("2013-53"));
        assertFalse(DateUtils.isValidWeekCode("2014-53"));

        // 2015 actually has a week 53
        assertTrue(DateUtils.isValidWeekCode("2015-53"));
        assertFalse(DateUtils.isValidWeekCode("2015-54"));
    }

    @Test
    public void testToWeekCode() {

        assertEquals("2013-51", DateUtils.toWeekCode(DateUtils.createLocalDate("16122013")));
        assertEquals("2013-52", DateUtils.toWeekCode(DateUtils.createLocalDate("23122013")));
        assertEquals("2014-01", DateUtils.toWeekCode(DateUtils.createLocalDate("30122013")));
        assertEquals("2014-02", DateUtils.toWeekCode(DateUtils.createLocalDate("06012014")));

        assertEquals("2015-52", DateUtils.toWeekCode(DateUtils.createLocalDate("21122015")));
        assertEquals("2015-53", DateUtils.toWeekCode(DateUtils.createLocalDate("28122015")));
        assertEquals("2016-01", DateUtils.toWeekCode(DateUtils.createLocalDate("04012016")));
        assertEquals("2016-02", DateUtils.toWeekCode(DateUtils.createLocalDate("11012016")));
    }
}
