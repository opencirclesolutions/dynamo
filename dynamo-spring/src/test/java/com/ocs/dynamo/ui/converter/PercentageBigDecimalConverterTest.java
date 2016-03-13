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

public class PercentageBigDecimalConverterTest {

    /**
     * Test conversion to model (for two separate locales)
     */
    @Test
    public void testConvertToModel() {
        BigDecimalConverter converter = new PercentageBigDecimalConverter(2, false);
        BigDecimal result = converter.convertToModel("3,14%", BigDecimal.class, new Locale("nl"));
        Assert.assertEquals(BigDecimal.valueOf(3.14).setScale(2, RoundingMode.HALF_EVEN), result);

        // check that the percentage sign is optional
        result = converter.convertToModel("3,14", BigDecimal.class, new Locale("nl"));
        Assert.assertEquals(BigDecimal.valueOf(3.14).setScale(2, RoundingMode.HALF_EVEN), result);

        // check for a different locale
        converter = new PercentageBigDecimalConverter(2, false);
        result = converter.convertToModel("3.14%", BigDecimal.class, Locale.US);
        Assert.assertEquals(BigDecimal.valueOf(3.14).setScale(2, RoundingMode.HALF_EVEN), result);

        // null check
        Assert.assertNull(converter.convertToModel(null, BigDecimal.class, new Locale("nl")));
    }

    /**
     * Test conversion to presentation (for two separate locales)
     */
    @Test
    public void testConvertToPresentation() {
        BigDecimalConverter converter = new PercentageBigDecimalConverter(2, false);
        String result = converter.convertToPresentation(BigDecimal.valueOf(3.143), String.class,
                new Locale("nl"));
        Assert.assertEquals("3,14%", result);

        result = converter.convertToPresentation(BigDecimal.valueOf(3000.1434), String.class,
                new Locale("nl"));
        Assert.assertEquals("3000,14%", result);

        // test thousands grouping
        converter = new PercentageBigDecimalConverter(2, true);
        result = converter.convertToPresentation(BigDecimal.valueOf(3000.14), String.class,
                new Locale("nl"));
        Assert.assertEquals("3.000,14%", result);

        converter = new PercentageBigDecimalConverter(2, false);
        result = converter.convertToPresentation(BigDecimal.valueOf(3.14), String.class, Locale.US);
        Assert.assertEquals("3.14%", result);

        // test thousands grouping
        converter = new PercentageBigDecimalConverter(2, true);
        result = converter.convertToPresentation(BigDecimal.valueOf(3000.14), String.class,
                Locale.US);
        Assert.assertEquals("3,000.14%", result);

        // null check
        Assert.assertNull(converter.convertToPresentation(null, String.class, new Locale("nl")));
    }
}
