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

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.utils.StringUtils;

public class StringUtilsTest {

    @Test
    public void testCamelCaseToHumanFriendly() {
        Assert.assertNull(StringUtils.camelCaseToHumanFriendly(null, true));
        Assert.assertEquals("Slightly Complex", StringUtils.camelCaseToHumanFriendly("slightlyComplex", true));
        Assert.assertEquals("Simple", StringUtils.camelCaseToHumanFriendly("simple", true));
        Assert.assertEquals("Multiple Words Camel", StringUtils.camelCaseToHumanFriendly("multiple wordsCamel", true));

        Assert.assertEquals("Slightly complex", StringUtils.camelCaseToHumanFriendly("slightlyComplex", false));
        Assert.assertEquals("Simple", StringUtils.camelCaseToHumanFriendly("simple", false));
        Assert.assertEquals("Multiple words camel", StringUtils.camelCaseToHumanFriendly("multiple wordsCamel", false));
    }

    @Test
    public void testRestrictToMaxFieldLength() {

        Assert.assertNull(StringUtils.restrictToMaxFieldLength(null, TestEntity.class, "name"));

        // a value that is too long is truncated
        String result = StringUtils.restrictToMaxFieldLength("longlonglonglonglonglonglonglonglonglonglong", TestEntity.class, "name");
        Assert.assertEquals(25, result.length());

        // a short value is left alone
        result = StringUtils.restrictToMaxFieldLength("shortshort", TestEntity.class, "name");
        Assert.assertEquals(10, result.length());

        // no restriction on the field means no change
        result = StringUtils.restrictToMaxFieldLength("longlonglonglonglonglonglonglonglonglonglong", TestEntity.class, "somestring");
        Assert.assertEquals(44, result.length());
    }

    @Test
    public void testIsValidEmail() {
        Assert.assertFalse(StringUtils.isValidEmail(" "));
        Assert.assertFalse(StringUtils.isValidEmail(""));
        Assert.assertFalse(StringUtils.isValidEmail("@"));
        Assert.assertTrue(StringUtils.isValidEmail("a@b.com"));
        Assert.assertFalse(StringUtils.isValidEmail("a@b"));
        Assert.assertTrue(StringUtils.isValidEmail("kevin@opencirclesolutions.nl"));
    }

    @Test
    public void testPrependProtocol() {
        Assert.assertEquals("http://www.google.nl", StringUtils.prependProtocol("http://www.google.nl"));
        Assert.assertEquals("http://www.google.nl", StringUtils.prependProtocol("www.google.nl"));
    }
}
