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
package com.ocs.dynamo.ui.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class PasteUtilsTest {

    @Test
    public void testSplit() {
        assertNull(PasteUtils.split(null));

        String[] result = PasteUtils.split("3");
        assertEquals("3", result[0]);

        result = PasteUtils.split("3 4 5");
        assertEquals("3", result[0]);
        assertEquals("4", result[1]);
        assertEquals("5", result[2]);

        result = PasteUtils.split("3\t4\n5");
        assertEquals("3", result[0]);
        assertEquals("4", result[1]);
        assertEquals("5", result[2]);

        assertTrue(StringUtils.isWhitespace("\t"));
    }

    @Test
    public void testSplitComplex() {
        String[] result = PasteUtils.split("\t\t3");
        assertEquals("", result[0]);
        assertEquals("", result[1]);
        assertEquals("3", result[2]);

        result = PasteUtils.split("\t\t\t3\t\t\t4");
        assertEquals("", result[0]);
        assertEquals("", result[1]);
        assertEquals("", result[2]);
        assertEquals("3", result[3]);
        assertEquals("", result[4]);
        assertEquals("", result[5]);
        assertEquals("4", result[6]);

        result = PasteUtils.split("   3   4");
        assertEquals("", result[0]);
        assertEquals("", result[1]);
        assertEquals("", result[2]);
        assertEquals("3", result[3]);
        assertEquals("", result[4]);
        assertEquals("", result[5]);
        assertEquals("4", result[6]);
    }

    @Test
    public void testToInt() {
        assertEquals(1234, PasteUtils.toInt("1234"));
        assertEquals(1234, PasteUtils.toInt("1,234"));
        assertEquals(1234, PasteUtils.toInt("1.234"));
    }

    @Test
    public void testTranslateSeparators() {
        assertEquals("2,3", PasteUtils.translateSeparators("2,3", new Locale("nl")));
        assertEquals("2,3", PasteUtils.translateSeparators("2.3", new Locale("nl")));
        assertEquals("2.3", PasteUtils.translateSeparators("2,3", new Locale("us")));
        assertEquals("2.3", PasteUtils.translateSeparators("2.3", new Locale("us")));
    }
}
