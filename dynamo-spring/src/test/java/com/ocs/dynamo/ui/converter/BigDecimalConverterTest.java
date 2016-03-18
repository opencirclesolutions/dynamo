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
import java.math.RoundingMode;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class BigDecimalConverterTest {

    /**
     * Test conversion to model (for two separate locales)
     */
    @Test
    public void testConvertToModel() {
        BigDecimalConverter converter = new BigDecimalConverter(2, false);
        BigDecimal result = converter.convertToModel("3,14", BigDecimal.class, new Locale("nl"));
        Assert.assertEquals(BigDecimal.valueOf(3.14).setScale(2, RoundingMode.HALF_EVEN), result);

        converter = new BigDecimalConverter(2, false);
        result = converter.convertToModel("3.142", BigDecimal.class, Locale.US);
        Assert.assertEquals(BigDecimal.valueOf(3.14).setScale(2, RoundingMode.HALF_EVEN), result);

        converter = new BigDecimalConverter(3, false);
        result = converter.convertToModel("3.142", BigDecimal.class, Locale.US);
        Assert.assertEquals(3.142, result.doubleValue(), 0.0001);

        // no decimals at all
        converter = new BigDecimalConverter(0, false);
        result = converter.convertToModel("3.142", BigDecimal.class, Locale.US);
        Assert.assertEquals(3, result.doubleValue(), 0.0001);
    }

    /**
     * Test conversion to presentation (for two separate locales)
     */
    @Test
    public void testConvertToPresentation() {
        BigDecimalConverter converter = new BigDecimalConverter(2, false);
        String result = converter.convertToPresentation(BigDecimal.valueOf(3.14), String.class,
                new Locale("nl"));
        Assert.assertEquals("3,14", result);

        converter = new BigDecimalConverter(2, false);
        result = converter.convertToPresentation(BigDecimal.valueOf(3.14), String.class, Locale.US);
        Assert.assertEquals("3.14", result);

        converter = new BigDecimalConverter(0, false);
        result = converter.convertToPresentation(BigDecimal.valueOf(3.14), String.class, Locale.US);
        Assert.assertEquals("3", result);

        converter = new BigDecimalConverter(3, false);
        result = converter.convertToPresentation(BigDecimal.valueOf(3.14), String.class, Locale.US);
        Assert.assertEquals("3.140", result);
    }

    /**
     * Test that a specifically set pattern overrules the other settings
     */
    @Test
    public void testDecimalFormat() {
        BigDecimalConverter converter = new BigDecimalConverter("#,##0.00");

        Assert.assertEquals("1.234,56", converter.convertToPresentation(BigDecimal.valueOf(1234.56),
                String.class, new Locale("nl")));

        Assert.assertEquals("123.456,00", converter
                .convertToPresentation(BigDecimal.valueOf(123456), String.class, new Locale("nl")));

        Assert.assertEquals("123,456.00", converter
                .convertToPresentation(BigDecimal.valueOf(123456), String.class, new Locale("us")));
    }
}
