package com.ocs.dynamo.converter;

import java.time.LocalTime;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.converter.LocalTimeAttributeConverter;
import com.ocs.dynamo.utils.DateUtils;

public class LocalTimeAttributeConverterTest {

    @Test
    public void testConvertToDatabaseColumn() {
        LocalTimeAttributeConverter cv = new LocalTimeAttributeConverter();

        Assert.assertNull(cv.convertToDatabaseColumn(null));

        Date d = cv.convertToDatabaseColumn(DateUtils.createLocalTime("121314"));
        Assert.assertTrue(d.toString().contains("12:13:14"));
    }

    @Test
    public void testConvertToEntityAttribute() {
        LocalTimeAttributeConverter cv = new LocalTimeAttributeConverter();

        Assert.assertNull(cv.convertToEntityAttribute(null));

        LocalTime t = cv.convertToEntityAttribute(DateUtils.createTime("121314"));
        Assert.assertEquals("12:13:14", t.toString());
    }

}
