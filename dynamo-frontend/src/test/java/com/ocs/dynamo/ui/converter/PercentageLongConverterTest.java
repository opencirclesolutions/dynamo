package com.ocs.dynamo.ui.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.vaadin.flow.data.binder.Result;

public class PercentageLongConverterTest extends BaseConverterTest {

    @Test
    public void testConvertToModel() {
        PercentageLongConverter cv = new PercentageLongConverter("message", true);
        assertNull(cv.convertToModel(null, createContext()).getOrThrow(r -> new OCSRuntimeException()));

        Result<Long> result = cv.convertToModel("4%", createContext());
        assertEquals(4L, result.getOrThrow(r -> new OCSRuntimeException()).longValue());

        result = cv.convertToModel("4", createContext());
        assertEquals(4L, result.getOrThrow(r -> new OCSRuntimeException()).longValue());
    }

    @Test
    public void testConvertToPresentation() {
        PercentageLongConverter cv = new PercentageLongConverter("message", true);
        assertNull(cv.convertToPresentation(null, createContext()));

        String result = cv.convertToPresentation(1234L, createContext());
        assertEquals("1.234%", result);
    }
}
