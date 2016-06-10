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

import org.junit.Assert;
import org.junit.Test;

public class CurrencyBigDecimalConverterTest {

    private DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();

    @Test
    public void testConvertToPresentation() {
        CurrencyBigDecimalConverter cv = new CurrencyBigDecimalConverter(2, true, "€");

        String result = cv.convertToPresentation(new BigDecimal(123456), null, null);
        Assert.assertEquals(
                String.format("%s123%s456%s00", cv.getDecimalFormat().getPositivePrefix(),
                        symbols.getGroupingSeparator(), symbols.getMonetaryDecimalSeparator()),
                result);

        cv = new CurrencyBigDecimalConverter(2, true, "$");
        result = cv.convertToPresentation(new BigDecimal(123456), null, null);
        Assert.assertEquals(
                String.format("%s123%s456%s00", cv.getDecimalFormat().getPositivePrefix(),
                        symbols.getGroupingSeparator(), symbols.getMonetaryDecimalSeparator()),
                result);
    }

    @Test
    public void testConvertToModel() {
        CurrencyBigDecimalConverter cv = new CurrencyBigDecimalConverter(2, true, "€");
        Assert.assertEquals(123456,
                cv.convertToModel(cv.getDecimalFormat().getPositivePrefix() + "123456", null, null)
                        .doubleValue(),
                0.001);

        // check that the currency symbol is added if it is not there
        Assert.assertEquals(123456,
                cv.convertToModel("123" + symbols.getGroupingSeparator() + "456", null, null)
                        .doubleValue(),
                0.001);
    }

}
