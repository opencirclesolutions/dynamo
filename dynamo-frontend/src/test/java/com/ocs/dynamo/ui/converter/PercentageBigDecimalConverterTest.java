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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;

public class PercentageBigDecimalConverterTest {

    /**
     * Test conversion to model (for two separate locales)
     */
    @Test
    public void testConvertToModel() {
        BigDecimalConverter converter = new PercentageBigDecimalConverter("message", 2, false);
        Result<BigDecimal> result = converter.convertToModel("3,14%", new ValueContext(new Locale("nl")));
        assertEquals(BigDecimal.valueOf(3.14).setScale(2, RoundingMode.HALF_EVEN), result.getOrThrow(r -> new OCSRuntimeException()));

        // check that the percentage sign is optional
        result = converter.convertToModel("3,14", new ValueContext(new Locale("nl")));
        assertEquals(BigDecimal.valueOf(3.14).setScale(2, RoundingMode.HALF_EVEN), result.getOrThrow(r -> new OCSRuntimeException()));

        // check for a different locale
        converter = new PercentageBigDecimalConverter("message", 2, false);
        result = converter.convertToModel("3.14%", new ValueContext(Locale.US));
        assertEquals(BigDecimal.valueOf(3.14).setScale(2, RoundingMode.HALF_EVEN), result.getOrThrow(r -> new OCSRuntimeException()));

        // null check
        assertNull(converter.convertToModel(null, new ValueContext(new Locale("nl"))).getOrThrow(r -> new OCSRuntimeException()));
    }

    /**
     * Test conversion to presentation (for two separate locales)
     */
    @Test
    public void testConvertToPresentation() {
        BigDecimalConverter converter = new PercentageBigDecimalConverter("message", 2, false);
        String result = converter.convertToPresentation(BigDecimal.valueOf(3.143), new ValueContext(new Locale("nl")));
        assertEquals("3,14%", result);

        result = converter.convertToPresentation(BigDecimal.valueOf(3000.1434), new ValueContext(new Locale("nl")));
        assertEquals("3000,14%", result);

        // test thousands grouping
        converter = new PercentageBigDecimalConverter("message", 2, true);
        result = converter.convertToPresentation(BigDecimal.valueOf(3000.14), new ValueContext(new Locale("nl")));
        assertEquals("3.000,14%", result);

        converter = new PercentageBigDecimalConverter("message", 2, false);
        result = converter.convertToPresentation(BigDecimal.valueOf(3.14), new ValueContext(Locale.US));
        assertEquals("3.14%", result);

        // test thousands grouping
        converter = new PercentageBigDecimalConverter("message", 2, true);
        result = converter.convertToPresentation(BigDecimal.valueOf(3000.14), new ValueContext(Locale.US));
        assertEquals("3,000.14%", result);

        // null check
        assertNull(converter.convertToPresentation(null, new ValueContext(new Locale("nl"))));
    }
}
