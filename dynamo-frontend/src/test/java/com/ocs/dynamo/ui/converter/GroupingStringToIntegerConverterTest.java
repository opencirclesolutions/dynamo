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

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.vaadin.flow.data.binder.Result;

/**
 * 
 * @author Bas Rutten
 *
 */
public class GroupingStringToIntegerConverterTest extends BaseConverterTest {

    GroupingStringToIntegerConverter converter = new GroupingStringToIntegerConverter("message", false);

    @Test
    public void testToModel() {
        Result<Integer> value = converter.convertToModel("3000", createContext());
        assertEquals(3000, value.getOrThrow(r -> new OCSRuntimeException()).intValue());
    }

    /**
     * Make sure there is no grouping indicator
     */
    @Test
    public void testToPresentation() {
        String value = converter.convertToPresentation(3000, createContext());
        assertEquals("3000", value);
    }

    /**
     * Make sure there is a grouping indicator
     */
    @Test
    public void testToPresentationWithGrouping() {
        String value = new GroupingStringToIntegerConverter("message", true).convertToPresentation(3000, createContext());
        assertEquals("3.000", value);
    }

    @Test
    public void testToModelWithGrouping() {
        Result<Integer> value = new GroupingStringToIntegerConverter("message", true).convertToModel("3.000", createContext());
        assertEquals(3000, value.getOrThrow(r -> new OCSRuntimeException()).intValue());
    }
}
