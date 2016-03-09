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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.data.util.converter.StringToLongConverter;

public class ConverterFactoryTest {

    @Test
    public void testCreateBigDecimalConverter() {
        BigDecimalConverter cv = ConverterFactory.createBigDecimalConverter(false, false, false, 2,
                null);
        Assert.assertFalse(cv instanceof PercentageBigDecimalConverter);
    }

    @Test
    public void testCreateBigDecimalConverter2() {
        BigDecimalConverter cv = ConverterFactory.createBigDecimalConverter(false, true, false, 2,
                null);
        Assert.assertTrue(cv instanceof PercentageBigDecimalConverter);
    }

    @Test
    public void testCreateBigDecimalConverter3() {
        BigDecimalConverter cv = ConverterFactory.createBigDecimalConverter(true, false, false, 2,
                "EUR");
        Assert.assertTrue(cv instanceof CurrencyBigDecimalConverter);
    }

    @Test
    public void testCreateIntegerConverter() {
        StringToIntegerConverter cv = ConverterFactory.createIntegerConverter(true);
        Assert.assertTrue(cv instanceof GroupingStringToIntegerConverter);
    }

    @Test
    public void testCreateLongConverter() {
        StringToLongConverter cv = ConverterFactory.createLongConverter(true);
        Assert.assertTrue(cv instanceof GroupingStringToLongConverter);
    }
}
