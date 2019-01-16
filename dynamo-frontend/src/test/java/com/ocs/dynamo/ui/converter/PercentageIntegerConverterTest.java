package com.ocs.dynamo.ui.converter;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.vaadin.data.Result;

public class PercentageIntegerConverterTest extends BaseConverterTest {

	@Test
	public void testConvertToModel() {
		PercentageIntegerConverter cv = new PercentageIntegerConverter("message", true);
		Assert.assertNull(cv.convertToModel(null, createContext()).getOrThrow(r -> new OCSRuntimeException()));

		Result<Integer> result = cv.convertToModel("4%", createContext());
		Assert.assertEquals(4L, result.getOrThrow(r -> new OCSRuntimeException()).longValue());

		result = cv.convertToModel("4", createContext());
		Assert.assertEquals(4L, result.getOrThrow(r -> new OCSRuntimeException()).longValue());
	}

	@Test
	public void testConvertToPresentation() {
		PercentageIntegerConverter cv = new PercentageIntegerConverter("message", true);
		Assert.assertNull(cv.convertToPresentation(null, createContext()));
		String result = cv.convertToPresentation(1234, createContext());
		Assert.assertEquals("1.234%", result);
	}
}
