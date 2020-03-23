package com.ocs.dynamo.ui.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.exception.OCSRuntimeException;

public class CurrencyDoubleConverterTest extends BaseConverterTest {

    private DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(new Locale("nl"));

    @Test
    public void testConvertToPresentation() {
        CurrencyDoubleConverter cv = new CurrencyDoubleConverter("message", 2, true, "€");

        String result = cv.convertToPresentation(123456.00, createContext());
        assertEquals(String.format("%s123%s456%s00", cv.getDecimalFormat(new Locale("nl")).getPositivePrefix(),
                symbols.getGroupingSeparator(), symbols.getMonetaryDecimalSeparator()), result);

        cv = new CurrencyDoubleConverter("message", 2, true, "$");
        result = cv.convertToPresentation(123456.00, createContext());
        assertEquals(String.format("%s123%s456%s00", cv.getDecimalFormat(null).getPositivePrefix(), symbols.getGroupingSeparator(),
                symbols.getMonetaryDecimalSeparator()), result);
    }

    @Test
    public void testConvertToModel() {
        CurrencyDoubleConverter cv = new CurrencyDoubleConverter("message", 2, true, "€");
        assertEquals(123456, cv.convertToModel(cv.getDecimalFormat(new Locale("nl")).getPositivePrefix() + "123456", createContext())
                .getOrThrow(r -> new OCSRuntimeException()).doubleValue(), 0.001);

        assertEquals(123, cv.convertToModel("123", createContext()).getOrThrow(r -> new OCSRuntimeException()).doubleValue(), 0.001);

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

        CurrencyDoubleConverter cv = new CurrencyDoubleConverter("message", 2, true, "$");
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
