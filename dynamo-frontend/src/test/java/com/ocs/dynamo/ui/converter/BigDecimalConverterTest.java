/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.ui.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.vaadin.flow.data.binder.Result;

public class BigDecimalConverterTest extends BaseConverterTest {

	/**
	 * Test conversion to model (for two separate locales)
	 */
	@Test
	public void testConvertToModel() {

		// default using European locale
		BigDecimalConverter converter = new BigDecimalConverter("message", 2, false);
		Result<BigDecimal> result = converter.convertToModel("3,14", createContext());
		assertEquals(BigDecimal.valueOf(3.14).setScale(2, RoundingMode.HALF_EVEN),
				result.getOrThrow(x -> new OCSRuntimeException()));

		converter = new BigDecimalConverter("message", 2, false);
		result = converter.convertToModel("3.142", createUsContext());
		assertEquals(BigDecimal.valueOf(3.14).setScale(2, RoundingMode.HALF_EVEN),
				result.getOrThrow(x -> new OCSRuntimeException()));

		converter = new BigDecimalConverter("message", 3, false);
		result = converter.convertToModel("3.142", createUsContext());
		assertEquals(3.142, result.getOrThrow(x -> new OCSRuntimeException()).doubleValue(), 0.0001);

		// no decimals at all
		converter = new BigDecimalConverter("message", 0, false);
		result = converter.convertToModel("3.142", createUsContext());
		assertEquals(3, result.getOrThrow(x -> new OCSRuntimeException()).doubleValue(), 0.0001);
	}

	/**
	 * Test conversion to presentation (for two separate locales)
	 */
	@Test
	public void testConvertToPresentation() {

		// using default European locale
		BigDecimalConverter converter = new BigDecimalConverter("message", 2, false);
		String result = converter.convertToPresentation(BigDecimal.valueOf(3.14), createContext());
		assertEquals("3,14", result);

		converter = new BigDecimalConverter("message", 2, false);
		result = converter.convertToPresentation(BigDecimal.valueOf(3.14), createUsContext());
		assertEquals("3.14", result);

		converter = new BigDecimalConverter("message", 0, false);
		result = converter.convertToPresentation(BigDecimal.valueOf(3.14), createUsContext());
		assertEquals("3", result);

		converter = new BigDecimalConverter("message", 3, false);
		result = converter.convertToPresentation(BigDecimal.valueOf(3.14), createUsContext());
		assertEquals("3.140", result);
	}

}
