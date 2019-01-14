package com.ocs.dynamo.utils;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.test.BaseMockitoTest;

public class NumberUtilsTest extends BaseMockitoTest {

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
		Assert.assertEquals(formatNumber("4,45"), NumberUtils.format(4.45));
		Assert.assertEquals(formatNumber("4,23"), NumberUtils.format(BigDecimal.valueOf(4.23)));
		Assert.assertEquals(formatNumber("2,04"), NumberUtils.format(2.04f));
	}

	@Test
	public void testIsLong() {
		Assert.assertTrue(NumberUtils.isLong(Long.class));
		Assert.assertTrue(NumberUtils.isLong(long.class));
		Assert.assertFalse(NumberUtils.isLong(Integer.class));
	}

	@Test
	public void testIsLong2() {
		Assert.assertFalse(NumberUtils.isLong(null));
		Assert.assertTrue(NumberUtils.isLong(1L));
		Assert.assertTrue(NumberUtils.isLong(Long.valueOf("123")));

		Assert.assertFalse(NumberUtils.isLong(new Object()));
		Assert.assertFalse(NumberUtils.isLong(12345));
	}

	@Test
	public void testIsInteger() {
		Assert.assertTrue(NumberUtils.isInteger(Integer.class));
		Assert.assertTrue(NumberUtils.isInteger(int.class));
		Assert.assertFalse(NumberUtils.isInteger(Long.class));
	}

	@Test
	public void testIsInteger2() {
		Assert.assertTrue(NumberUtils.isInteger(4));
		Assert.assertTrue(NumberUtils.isInteger(new Integer(12)));
		Assert.assertFalse(NumberUtils.isInteger(5L));
		Assert.assertFalse(NumberUtils.isInteger(null));
	}

	@Test
	public void testIsDouble() {
		Assert.assertTrue(NumberUtils.isDouble(double.class));
		Assert.assertTrue(NumberUtils.isDouble(Double.class));
	}
	
	@Test
	public void testIsDouble2() {
		Assert.assertTrue(NumberUtils.isDouble(4.2));
		Assert.assertTrue(NumberUtils.isDouble(new Double(12.23)));
		Assert.assertFalse(NumberUtils.isDouble(5L));
		Assert.assertFalse(NumberUtils.isDouble(null));
	}
}
