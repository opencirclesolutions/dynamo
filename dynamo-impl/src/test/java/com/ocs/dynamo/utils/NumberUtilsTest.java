package com.ocs.dynamo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.test.BaseMockitoTest;

public class NumberUtilsTest extends BaseMockitoTest {

    @Test
    public void testIsNumeric() {
        assertFalse(NumberUtils.isNumeric(String.class));
        assertTrue(NumberUtils.isNumeric(byte.class));
        assertTrue(NumberUtils.isNumeric(int.class));
        assertTrue(NumberUtils.isNumeric(long.class));
        assertTrue(NumberUtils.isNumeric(short.class));
        assertTrue(NumberUtils.isNumeric(double.class));
        assertTrue(NumberUtils.isNumeric(float.class));
        assertTrue(NumberUtils.isNumeric(BigDecimal.class));
        assertTrue(NumberUtils.isNumeric(Integer.class));
        assertTrue(NumberUtils.isNumeric(Long.class));
    }

    @Test
    public void testFormat() {
        assertEquals("Test", NumberUtils.format("Test"));
        assertEquals("1234", NumberUtils.format(1234L));
        assertEquals(formatNumber("4,45"), NumberUtils.format(4.45));
        assertEquals(formatNumber("4,23"), NumberUtils.format(BigDecimal.valueOf(4.23)));
        assertEquals(formatNumber("2,04"), NumberUtils.format(2.04f));
    }

    @Test
    public void testIsLong() {
        assertTrue(NumberUtils.isLong(Long.class));
        assertTrue(NumberUtils.isLong(long.class));
        assertFalse(NumberUtils.isLong(Integer.class));
    }

    @Test
    public void testIsLong2() {
        assertFalse(NumberUtils.isLong(null));
        assertTrue(NumberUtils.isLong(1L));
        assertTrue(NumberUtils.isLong(Long.valueOf("123")));
        assertFalse(NumberUtils.isLong(new Object()));
        assertFalse(NumberUtils.isLong(12345));
    }

    @Test
    public void testIsInteger() {
        assertTrue(NumberUtils.isInteger(Integer.class));
        assertTrue(NumberUtils.isInteger(int.class));
        assertFalse(NumberUtils.isInteger(Long.class));
    }

    @Test
    public void testIsInteger2() {
        assertTrue(NumberUtils.isInteger(4));
        assertTrue(NumberUtils.isInteger(Integer.valueOf(12)));
        assertFalse(NumberUtils.isInteger(5L));
        assertFalse(NumberUtils.isInteger(null));
    }

    @Test
    public void testIsDouble() {
        assertTrue(NumberUtils.isDouble(double.class));
        assertTrue(NumberUtils.isDouble(Double.class));
    }

    @Test
    public void testIsDouble2() {
        assertTrue(NumberUtils.isDouble(4.2));
        assertTrue(NumberUtils.isDouble(Double.valueOf(12.13)));
        assertFalse(NumberUtils.isDouble(5L));
        assertFalse(NumberUtils.isDouble(null));
    }
}
