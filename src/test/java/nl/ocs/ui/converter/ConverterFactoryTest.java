package nl.ocs.ui.converter;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.data.util.converter.StringToLongConverter;

public class ConverterFactoryTest {

	@Test
	public void testCreateBigDecimalConverter() {
		BigDecimalConverter cv = ConverterFactory.createBigDecimalConverter(false, false, false, 2,
				null);
		Assert.assertFalse(cv instanceof PercentageBigDecimalConverter);
	}

	@Test
	public void testCreateBigDecimalConverter2() {
		BigDecimalConverter cv = ConverterFactory.createBigDecimalConverter(false, true, false, 2,
				null);
		Assert.assertTrue(cv instanceof PercentageBigDecimalConverter);
	}

	@Test
	public void testCreateBigDecimalConverter3() {
		BigDecimalConverter cv = ConverterFactory.createBigDecimalConverter(true, false, false, 2,
				"EUR");
		Assert.assertTrue(cv instanceof CurrencyBigDecimalConverter);
	}
	
	@Test
	public void testCreateIntegerConverter() {
		StringToIntegerConverter cv = ConverterFactory.createIntegerConverter(true);
		Assert.assertTrue(cv instanceof GroupingStringToIntegerConverter);
	}
	
	@Test
	public void testCreateLongConverter() {
		StringToLongConverter cv = ConverterFactory.createLongConverter(true);
		Assert.assertTrue(cv instanceof GroupingStringToLongConverter);
	}
}
