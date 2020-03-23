package com.ocs.dynamo.ui.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;

public class PercentageDoubleConverterTest {

	/**
	 * Test conversion to model (for two separate locales)
	 */
	@Test
	public void testConvertToModel() {
		PercentageDoubleConverter converter = new PercentageDoubleConverter("message", 2, false);
		Result<Double> result = converter.convertToModel("3,14%", new ValueContext(new Locale("nl")));
		assertEquals(3.14, result.getOrThrow(r -> new OCSRuntimeException()), 0.001);

		// check that the percentage sign is optional
		result = converter.convertToModel("3,14", new ValueContext(new Locale("nl")));
		assertEquals(3.14, result.getOrThrow(r -> new OCSRuntimeException()), 0.001);

		// check for a different locale
		converter = new PercentageDoubleConverter("message", 2, false);
		result = converter.convertToModel("3.14%", new ValueContext(Locale.US));
		assertEquals(3.14, result.getOrThrow(r -> new OCSRuntimeException()), 0.001);

		// null check
		assertNull(converter.convertToModel(null, new ValueContext(new Locale("nl")))
				.getOrThrow(r -> new OCSRuntimeException()));
	}

	/**
	 * Test conversion to presentation (for two separate locales)
	 */
	@Test
	public void testConvertToPresentation() {
		PercentageDoubleConverter converter = new PercentageDoubleConverter("message", 2, false);
		String result = converter.convertToPresentation(3.143, new ValueContext(new Locale("nl")));
		assertEquals("3,14%", result);

		result = converter.convertToPresentation(3000.1434, new ValueContext(new Locale("nl")));
		assertEquals("3000,14%", result);

		// test thousands grouping
		converter = new PercentageDoubleConverter("message", 2, true);
		result = converter.convertToPresentation(3000.14, new ValueContext(new Locale("nl")));
		assertEquals("3.000,14%", result);

		converter = new PercentageDoubleConverter("message", 2, false);
		result = converter.convertToPresentation(3.14, new ValueContext(Locale.US));
		assertEquals("3.14%", result);

		// test thousands grouping
		converter = new PercentageDoubleConverter("message", 2, true);
		result = converter.convertToPresentation(3000.14, new ValueContext(Locale.US));
		assertEquals("3,000.14%", result);

		// null check
		assertNull(converter.convertToPresentation(null, new ValueContext(new Locale("nl"))));
	}
}
