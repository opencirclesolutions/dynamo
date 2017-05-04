package com.ocs.dynamo.ui.converter;

import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.utils.DateUtils;

public class LocalDateWeekCodeConverterTest {

    private LocalDateWeekCodeConverter converter = new LocalDateWeekCodeConverter();

    @Test
    public void testToModel() {
        LocalDate date = converter.convertToModel(null, null, null);
        Assert.assertNull(date);

        date = converter.convertToModel("2014-52", null, null);
        Assert.assertEquals(DateUtils.createLocalDate("22122014"), date);

        date = converter.convertToModel("2015-01", null, null);
        Assert.assertEquals(DateUtils.createLocalDate("29122014"), date);

        date = converter.convertToModel("2015-02", null, null);
        Assert.assertEquals(DateUtils.createLocalDate("05012015"), date);

        date = converter.convertToModel("2015-52", null, null);
        Assert.assertEquals(DateUtils.createLocalDate("21122015"), date);

        date = converter.convertToModel("2015-53", null, null);
        Assert.assertEquals(DateUtils.createLocalDate("28122015"), date);

        date = converter.convertToModel("2016-01", null, null);
        Assert.assertEquals(DateUtils.createLocalDate("04012016"), date);
    }

    @Test
    public void testToPresentation() {
        String str = converter.convertToPresentation(null, null, null);
        Assert.assertNull(str);

        str = converter.convertToPresentation(DateUtils.createLocalDate("22122014"), null, null);
        Assert.assertEquals("2014-52", str);

        str = converter.convertToPresentation(DateUtils.createLocalDate("29122014"), null, null);
        Assert.assertEquals("2015-01", str);

        str = converter.convertToPresentation(DateUtils.createLocalDate("05012015"), null, null);
        Assert.assertEquals("2015-02", str);
    }
}
