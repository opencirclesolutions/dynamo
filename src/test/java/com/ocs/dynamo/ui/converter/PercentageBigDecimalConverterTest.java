package com.ocs.dynamo.ui.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class PercentageBigDecimalConverterTest {

	/**
	 * Test conversion to model (for two separate locales)
	 */
	@Test
	public void testConvertToModel() {
		BigDecimalConverter converter = new PercentageBigDecimalConverter(2, false);
		BigDecimal result = converter.convertToModel("3,14%", BigDecimal.class, new Locale("nl"));
		Assert.assertEquals(new BigDecimal(3.14).setScale(2, RoundingMode.HALF_EVEN), result);

		// check that the percentage sign is optional
		result = converter.convertToModel("3,14", BigDecimal.class, new Locale("nl"));
		Assert.assertEquals(new BigDecimal(3.14).setScale(2, RoundingMode.HALF_EVEN), result);

		// check for a different locale
		converter = new PercentageBigDecimalConverter(2, false);
		result = converter.convertToModel("3.14%", BigDecimal.class, Locale.US);
		Assert.assertEquals(new BigDecimal(3.14).setScale(2, RoundingMode.HALF_EVEN), result);

		// null check
		Assert.assertNull(converter.convertToModel(null, BigDecimal.class, new Locale("nl")));
	}

	/**
	 * Test conversion to presentation (for two separate locales)
	 */
	@Test
	public void testConvertToPresentation() {
		BigDecimalConverter converter = new PercentageBigDecimalConverter(2, false);
		String result = converter.convertToPresentation(new BigDecimal(3.143), String.class,
				new Locale("nl"));
		Assert.assertEquals("3,14%", result);

		result = converter.convertToPresentation(new BigDecimal(3000.1434), String.class,
				new Locale("nl"));
		Assert.assertEquals("3000,14%", result);

		// test thousands grouping
		converter = new PercentageBigDecimalConverter(2, true);
		result = converter.convertToPresentation(new BigDecimal(3000.14), String.class, new Locale(
				"nl"));
		Assert.assertEquals("3.000,14%", result);

		converter = new PercentageBigDecimalConverter(2, false);
		result = converter.convertToPresentation(new BigDecimal(3.14), String.class, Locale.US);
		Assert.assertEquals("3.14%", result);

		// test thousands grouping
		converter = new PercentageBigDecimalConverter(2, true);
		result = converter.convertToPresentation(new BigDecimal(3000.14), String.class, Locale.US);
		Assert.assertEquals("3,000.14%", result);

		// null check
		Assert.assertNull(converter.convertToPresentation(null, String.class, new Locale("nl")));
	}
}
