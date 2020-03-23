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
package com.ocs.dynamo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.TestEntity;

public class StringUtilsTest {

    @Test
    public void testCamelCaseToHumanFriendly() {
        assertNull(StringUtils.camelCaseToHumanFriendly(null, true));
        assertEquals("Slightly Complex", StringUtils.camelCaseToHumanFriendly("slightlyComplex", true));
        assertEquals("Simple", StringUtils.camelCaseToHumanFriendly("simple", true));
        assertEquals("Multiple Words Camel", StringUtils.camelCaseToHumanFriendly("multiple wordsCamel", true));

        assertEquals("Slightly complex", StringUtils.camelCaseToHumanFriendly("slightlyComplex", false));
        assertEquals("Simple", StringUtils.camelCaseToHumanFriendly("simple", false));
        assertEquals("Multiple words camel", StringUtils.camelCaseToHumanFriendly("multiple wordsCamel", false));
    }

    @Test
    public void testRestrictToMaxFieldLength() {

        assertNull(StringUtils.restrictToMaxFieldLength(null, TestEntity.class, "name"));

        // a value that is too long is truncated
        String result = StringUtils.restrictToMaxFieldLength("longlonglonglonglonglonglonglonglonglonglong", TestEntity.class, "name");
        assertEquals(25, result.length());

        // a short value is left alone
        result = StringUtils.restrictToMaxFieldLength("shortshort", TestEntity.class, "name");
        assertEquals(10, result.length());

        // no restriction on the field means no change
        result = StringUtils.restrictToMaxFieldLength("longlonglonglonglonglonglonglonglonglonglong", TestEntity.class, "somestring");
        assertEquals(44, result.length());
    }

    @Test
    public void testIsValidEmail() {
        assertFalse(StringUtils.isValidEmail(" "));
        assertFalse(StringUtils.isValidEmail(""));
        assertFalse(StringUtils.isValidEmail("@"));
        assertTrue(StringUtils.isValidEmail("a@b.com"));
        assertFalse(StringUtils.isValidEmail("a@b"));
        assertTrue(StringUtils.isValidEmail("kevin@opencirclesolutions.nl"));
    }

    @Test
    public void testPrependProtocol() {
        assertEquals("http://www.google.nl", StringUtils.prependProtocol("http://www.google.nl"));
        assertEquals("http://www.google.nl", StringUtils.prependProtocol("www.google.nl"));
    }
}
