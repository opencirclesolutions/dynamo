package com.ocs.dynamo.ui.converter;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

public class BigDecimalToDoubleConverterTest {

	@Test
	public void testConvertToModel() {
		BigDecimalToDoubleConverter converter = new BigDecimalToDoubleConverter();

		Assert.assertNull(converter.convertToModel(null, BigDecimal.class, null));

		BigDecimal bd = converter.convertToModel(1.2, BigDecimal.class, null);
		Assert.assertEquals(1.2, bd.doubleValue(), 0.001);
	}

	@Test
	public void testConvertToPresentation() {
		BigDecimalToDoubleConverter converter = new BigDecimalToDoubleConverter();

		Assert.assertNull(converter.convertToPresentation(null, Double.class, null));

		Double d = converter.convertToPresentation(BigDecimal.valueOf(7.01), Double.class, null);
		Assert.assertEquals(7.01, d, 0.001);
	}
}
