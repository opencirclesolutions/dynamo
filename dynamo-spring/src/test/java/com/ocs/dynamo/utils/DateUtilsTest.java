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

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class DateUtilsTest {

    @Test
    public void testGetStartDateOfWeek() {

        Assert.assertEquals(DateUtils.createDate("30122013"),
                DateUtils.getStartDateOfWeek("2014-01"));
        Assert.assertEquals(DateUtils.createDate("06012014"),
                DateUtils.getStartDateOfWeek("2014-02"));

        Assert.assertEquals(DateUtils.createDate("29122014"),
                DateUtils.getStartDateOfWeek("2015-01"));
        Assert.assertEquals(DateUtils.createDate("05012015"),
                DateUtils.getStartDateOfWeek("2015-02"));
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
    public void testGetNextWeekCode() {

        Assert.assertEquals("2015-02", DateUtils.getNextWeekCode("2015-01"));
        Assert.assertEquals("2015-03", DateUtils.getNextWeekCode("2015-02"));

        Assert.assertEquals("2015-53", DateUtils.getNextWeekCode("2015-52"));
        Assert.assertEquals("2016-01", DateUtils.getNextWeekCode("2015-53"));
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

    @Test
    public void testFormat() {
        Date date = DateUtils.createDate("01042015");

        Assert.assertNull(DateUtils.formatDate(null, "dd-MM-yyyy"));
        Assert.assertNull(DateUtils.formatDate(date, null));

        Assert.assertEquals("01-04-2015", DateUtils.formatDate(date, "dd-MM-yyyy"));
        Assert.assertEquals("01/04/15", DateUtils.formatDate(date, "dd/MM/yy"));
    }
}
