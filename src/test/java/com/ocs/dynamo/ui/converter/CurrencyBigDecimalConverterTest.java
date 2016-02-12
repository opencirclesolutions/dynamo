package com.ocs.dynamo.ui.converter;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

public class CurrencyBigDecimalConverterTest {

	@Test
	public void testConvertToPresentation() {
		CurrencyBigDecimalConverter cv = new CurrencyBigDecimalConverter(2, true, "€");

		String result = cv.convertToPresentation(new BigDecimal(123456), null, null);
		Assert.assertEquals("€ 123.456,00", result);

		cv = new CurrencyBigDecimalConverter(2, true, "$");
		result = cv.convertToPresentation(new BigDecimal(123456), null, null);
		Assert.assertEquals("$ 123.456,00", result);
	}

	@Test
	public void testConvertToModel() {
		CurrencyBigDecimalConverter cv = new CurrencyBigDecimalConverter(2, true, "€");
		Assert.assertEquals(123456, cv.convertToModel("€ 123456", null, null).doubleValue(), 0.001);

		// check that the currency symbol is added if it is not there
		Assert.assertEquals(123456, cv.convertToModel("123.456", null, null).doubleValue(), 0.001);
	}

}
