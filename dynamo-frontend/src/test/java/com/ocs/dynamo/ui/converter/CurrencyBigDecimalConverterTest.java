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
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ocs.dynamo.exception.OCSRuntimeException;

public class CurrencyBigDecimalConverterTest extends BaseConverterTest {

    private DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(new Locale("nl"));

    @BeforeEach
    public void setup() {
        Locale.setDefault(new Locale("nl"));
    }

    @Test
    public void testConvertToPresentation() {
        CurrencyBigDecimalConverter cv = new CurrencyBigDecimalConverter("message", 2, true, "€");

        String result = cv.convertToPresentation(new BigDecimal(123456), createContext());
        assertEquals(String.format("%s123%s456%s00", cv.getDecimalFormat(new Locale("nl")).getPositivePrefix(),
                symbols.getGroupingSeparator(), symbols.getMonetaryDecimalSeparator()), result);

        cv = new CurrencyBigDecimalConverter("message", 2, true, "$");
        result = cv.convertToPresentation(new BigDecimal(123456), createContext());
        assertEquals(String.format("%s123%s456%s00", cv.getDecimalFormat(null).getPositivePrefix(), symbols.getGroupingSeparator(),
                symbols.getMonetaryDecimalSeparator()), result);
    }

    @Test
    public void testConvertToModel() {
        CurrencyBigDecimalConverter cv = new CurrencyBigDecimalConverter("message", 2, true, "€");
        assertEquals(123456, cv.convertToModel(cv.getDecimalFormat(new Locale("nl")).getPositivePrefix() + "123456", createContext())
                .getOrThrow(r -> new OCSRuntimeException()).doubleValue(), 0.001);

        assertEquals(123456, cv.convertToModel("123456", createContext()).getOrThrow(r -> new OCSRuntimeException()).doubleValue(), 0.001);

        // test that the currency symbol is stripped when needed
        assertEquals(123456, cv.convertToModel("€ 123" + symbols.getGroupingSeparator() + "456", createContext())
                .getOrThrow(r -> new OCSRuntimeException()).doubleValue(), 0.001);

        assertEquals(123456, cv.convertToModel("€ 123456", createContext()).getOrThrow(r -> new OCSRuntimeException()).doubleValue(),
                0.001);

        assertEquals(123456.12, cv.convertToModel("€ 123456" + symbols.getDecimalSeparator() + "12", createContext())
                .getOrThrow(r -> new OCSRuntimeException()).doubleValue(), 0.001);
    }

    @Test
    public void testConvertToModelUSA() {

        DecimalFormatSymbols usa = DecimalFormatSymbols.getInstance(Locale.US);

        CurrencyBigDecimalConverter cv = new CurrencyBigDecimalConverter("message", 2, true, "$");
        assertEquals(123456, cv.convertToModel(cv.getDecimalFormat(Locale.US).getPositivePrefix() + "123456", createUsContext())
                .getOrThrow(r -> new OCSRuntimeException()).doubleValue(), 0.001);

        // test that the currency symbol is stripped when needed
        assertEquals(123456, cv.convertToModel("$123456", createUsContext()).getOrThrow(r -> new OCSRuntimeException()).doubleValue(),
                0.001);

        // simple value without separators
        assertEquals(123456, cv.convertToModel("$123456", createUsContext()).getOrThrow(r -> new OCSRuntimeException()).doubleValue(),
                0.001);

        // value with decimal separators
        assertEquals(123456.12,
                cv.convertToModel("$123" + usa.getGroupingSeparator() + "456" + usa.getDecimalSeparator() + "12", createUsContext())
                        .getOrThrow(r -> new OCSRuntimeException()).doubleValue(),
                0.001);
    }

}
