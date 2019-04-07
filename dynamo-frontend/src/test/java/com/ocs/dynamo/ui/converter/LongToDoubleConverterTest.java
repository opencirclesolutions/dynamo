package com.ocs.dynamo.ui.converter;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.vaadin.data.Result;

public class LongToDoubleConverterTest extends BaseConverterTest {

	@Test
	public void testConvertToModel() {
		LongToDoubleConverter cv = new LongToDoubleConverter();
		Result<Long> result = cv.convertToModel(null, createContext());
		Assert.assertNull(result.getOrThrow(r -> new OCSRuntimeException()));

		result = cv.convertToModel(1234.56, createContext());
		Assert.assertEquals(1234L, result.getOrThrow(r -> new OCSRuntimeException()).longValue());
	}

	@Test
	public void testConvertToPresentation() {
		LongToDoubleConverter cv = new LongToDoubleConverter();
		Double result = cv.convertToPresentation(null, createContext());
		Assert.assertNull(result);

		result = cv.convertToPresentation(1234L, createContext());
		Assert.assertEquals(1234.00, result.doubleValue(), 0.001);
	}
}