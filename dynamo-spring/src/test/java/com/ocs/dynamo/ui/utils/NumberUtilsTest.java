package com.ocs.dynamo.ui.utils;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.utils.NumberUtils;

public class NumberUtilsTest {

    @Test
    public void testIsNumeric() {
        Assert.assertFalse(NumberUtils.isNumeric(String.class));
        Assert.assertTrue(NumberUtils.isNumeric(byte.class));
        Assert.assertTrue(NumberUtils.isNumeric(int.class));
        Assert.assertTrue(NumberUtils.isNumeric(long.class));
        Assert.assertTrue(NumberUtils.isNumeric(short.class));
        Assert.assertTrue(NumberUtils.isNumeric(double.class));
        Assert.assertTrue(NumberUtils.isNumeric(float.class));
        Assert.assertTrue(NumberUtils.isNumeric(BigDecimal.class));
        Assert.assertTrue(NumberUtils.isNumeric(Integer.class));
        Assert.assertTrue(NumberUtils.isNumeric(Long.class));
    }

    @Test
    public void testFormat() {
        Assert.assertEquals("Test", NumberUtils.format("Test"));
        Assert.assertEquals("1234", NumberUtils.format(1234L));
        Assert.assertEquals("4,45", NumberUtils.format(4.45));
        Assert.assertEquals("4,23", NumberUtils.format(BigDecimal.valueOf(4.23)));
        Assert.assertEquals("2,04", NumberUtils.format(2.04f));
    }
}
