package com.ocs.dynamo.ui.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.vaadin.flow.data.binder.Result;

public class LongToDoubleConverterTest extends BaseConverterTest {

    @Test
    public void testConvertToModel() {
        LongToDoubleConverter cv = new LongToDoubleConverter();
        Result<Long> result = cv.convertToModel(null, createContext());
        assertNull(result.getOrThrow(r -> new OCSRuntimeException()));

        result = cv.convertToModel(1234.56, createContext());
        assertEquals(1234L, result.getOrThrow(r -> new OCSRuntimeException()).longValue());
    }

    @Test
    public void testConvertToPresentation() {
        LongToDoubleConverter cv = new LongToDoubleConverter();
        Double result = cv.convertToPresentation(null, createContext());
        assertNull(result);

        result = cv.convertToPresentation(1234L, createContext());
        assertEquals(1234.00, result.doubleValue(), 0.001);
    }
}
