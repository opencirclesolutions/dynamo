package com.ocs.dynamo.ui.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.vaadin.flow.data.binder.Result;

public class IntToDoubleConverterTest extends BaseConverterTest {

    @Test
    public void testConvertToModel() {
        IntToDoubleConverter cv = new IntToDoubleConverter();
        Result<Integer> result = cv.convertToModel(null, createContext());
        assertNull(result.getOrThrow(r -> new OCSRuntimeException()));
        result = cv.convertToModel(1234.56, createContext());
        assertEquals(1234, result.getOrThrow(r -> new OCSRuntimeException()).longValue());
    }

    @Test
    public void testConvertToPresentation() {
        IntToDoubleConverter cv = new IntToDoubleConverter();
        assertNull(cv.convertToPresentation(null, createContext()));
        Double result = cv.convertToPresentation(1234, createContext());
        assertEquals(1234.00, result.doubleValue(), 0.001);
    }
}
