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

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class GroupingStringToLongConverterTest {

    GroupingStringToLongConverter converter = new GroupingStringToLongConverter(false);

    @Test
    public void testToModel() {
        Long value = converter.convertToModel("3000", Long.class, new Locale("nl"));
        Assert.assertEquals(3000L, value.longValue());
    }

    /**
     * Make sure there is no grouping indicator
     */
    @Test
    public void testToPresentation() {
        String value = converter.convertToPresentation(3000L, String.class, new Locale("nl"));
        Assert.assertEquals("3000", value);
    }

    /**
     * Make sure there is a grouping indicator
     */
    @Test
    public void testToPresentationWithGrouping_ToPresentation() {
        String value = new GroupingStringToLongConverter(true).convertToPresentation(3000L,
                String.class, new Locale("nl"));
        Assert.assertEquals("3.000", value);
    }

    /**
     * Make sure there is a grouping indicator
     */
    @Test
    public void testToPresentationWithGrouping_ToModel() {
        Long value = new GroupingStringToLongConverter(true).convertToModel("3.000", Long.class,
                new Locale("nl"));
        Assert.assertEquals(3000L, value.longValue());
    }

}
