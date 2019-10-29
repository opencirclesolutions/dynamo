package com.ocs.dynamo.ui.converter;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.vaadin.flow.data.binder.Result;

public class BigDecimalToDoubleConverterTest extends BaseConverterTest {

	@Test
	public void testConvertToModel() {
		BigDecimalToDoubleConverter converter = new BigDecimalToDoubleConverter();

		Assert.assertNull(converter.convertToModel(null, createContext()).getOrThrow(r -> new OCSRuntimeException()));

		Result<BigDecimal> bd = converter.convertToModel(1.2, createContext());
		Assert.assertEquals(1.2, bd.getOrThrow(r -> new OCSRuntimeException()).doubleValue(), 0.001);
	}

	@Test
	public void testConvertToPresentation() {
		BigDecimalToDoubleConverter converter = new BigDecimalToDoubleConverter();

		Assert.assertNull(converter.convertToPresentation(null, createContext()));

		Double d = converter.convertToPresentation(BigDecimal.valueOf(7.01), createContext());
		Assert.assertEquals(7.01, d, 0.001);
	}
}
