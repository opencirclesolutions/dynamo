package com.ocs.dynamo.ui.converter;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.vaadin.data.Result;

public class PercentageLongConverterTest extends BaseConverterTest {

	@Test
	public void testConvertToModel() {
		PercentageLongConverter cv = new PercentageLongConverter("message", true);
		Assert.assertNull(cv.convertToModel(null, createContext()).getOrThrow(r -> new OCSRuntimeException()));

		Result<Long> result = cv.convertToModel("4%", createContext());
		Assert.assertEquals(4L, result.getOrThrow(r -> new OCSRuntimeException()).longValue());

		result = cv.convertToModel("4", createContext());
		Assert.assertEquals(4L, result.getOrThrow(r -> new OCSRuntimeException()).longValue());
	}

	@Test
	public void testConvertToPresentation() {
		PercentageLongConverter cv = new PercentageLongConverter("message", true);
		Assert.assertNull(cv.convertToPresentation(null, createContext()));

		String result = cv.convertToPresentation(1234L, createContext());
		Assert.assertEquals("1.234%", result);
	}
}
