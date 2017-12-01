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

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.utils.SystemPropertyUtils;

public class CurrencyBigDecimalConverterTest {

	private DecimalFormatSymbols symbols = DecimalFormatSymbols
			.getInstance(new Locale(SystemPropertyUtils.getDefaultLocale()));

	@Test
	public void testConvertToPresentation() {
		CurrencyBigDecimalConverter cv = new CurrencyBigDecimalConverter(2, true, "€");

		String result = cv.convertToPresentation(new BigDecimal(123456), null, null);
		Assert.assertEquals(String.format("%s123%s456%s00", cv.getDecimalFormat(null).getPositivePrefix(),
				symbols.getGroupingSeparator(), symbols.getMonetaryDecimalSeparator()), result);

		cv = new CurrencyBigDecimalConverter(2, true, "$");
		result = cv.convertToPresentation(new BigDecimal(123456), null, null);
		Assert.assertEquals(String.format("%s123%s456%s00", cv.getDecimalFormat(null).getPositivePrefix(),
				symbols.getGroupingSeparator(), symbols.getMonetaryDecimalSeparator()), result);
	}

	@Test
	public void testConvertToModel() {
		CurrencyBigDecimalConverter cv = new CurrencyBigDecimalConverter(2, true, "€");
		Assert.assertEquals(123456,
				cv.convertToModel(cv.getDecimalFormat(null).getPositivePrefix() + "123456", BigDecimal.class, null)
						.doubleValue(),
				0.001);

		// test that the currency symbol is stripped when needed
		Assert.assertEquals(123456, cv
				.convertToModel("€ 123" + symbols.getGroupingSeparator() + "456", BigDecimal.class, null).doubleValue(),
				0.001);

		Assert.assertEquals(123456, cv.convertToModel("€ 123456", BigDecimal.class, null).doubleValue(), 0.001);

		Assert.assertEquals(123456.12,
				cv.convertToModel("€ 123456" + symbols.getDecimalSeparator() + "12", BigDecimal.class, null)
						.doubleValue(),
				0.001);
	}

	@Test
	public void testConvertToModelUSA() {

		DecimalFormatSymbols usa = DecimalFormatSymbols.getInstance(new Locale("us"));

		CurrencyBigDecimalConverter cv = new CurrencyBigDecimalConverter(2, true, "USD");
		Assert.assertEquals(123456, cv.convertToModel(cv.getDecimalFormat(null).getPositivePrefix() + "123456",
				BigDecimal.class, new Locale("us")).doubleValue(), 0.001);

		// test that the currency symbol is stripped when needed
		Assert.assertEquals(123456,
				cv.convertToModel("USD 123" + usa.getGroupingSeparator() + "456", BigDecimal.class, new Locale("us"))
						.doubleValue(),
				0.001);

		// simple value without separators
		Assert.assertEquals(123456, cv.convertToModel("USD 123456", BigDecimal.class, null).doubleValue(), 0.001);

		Assert.assertEquals(123456.12,
				cv.convertToModel("USD 123456" + usa.getDecimalSeparator() + "12", BigDecimal.class, new Locale("us"))
						.doubleValue(),
				0.001);
	}

}
