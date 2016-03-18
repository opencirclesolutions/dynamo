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

public class StringUtilsTest {

    @Test
    public void testRestrictToMaxFieldLength() {

        Assert.assertNull(StringUtil.restrictToMaxFieldLength(null, TestEntity.class, "name"));

        // a value that is too long is truncated
        String result = StringUtil.restrictToMaxFieldLength(
                "longlonglonglonglonglonglonglonglonglonglong", TestEntity.class, "name");
        Assert.assertEquals(25, result.length());

        // a short value is left alone
        result = StringUtil.restrictToMaxFieldLength("shortshort", TestEntity.class, "name");
        Assert.assertEquals(10, result.length());

        // no restriction on the field means no change
        result = StringUtil.restrictToMaxFieldLength("longlonglonglonglonglonglonglonglonglonglong",
                TestEntity.class, "somestring");
        Assert.assertEquals(44, result.length());
    }

    @Test
    public void testReplaceHtmlBreaks() {
        Assert.assertNull(StringUtil.replaceHtmlBreaks(null));

        Assert.assertEquals("", StringUtil.replaceHtmlBreaks("<br/>"));
        Assert.assertEquals("a, b", StringUtil.replaceHtmlBreaks("a<br/>b"));
        Assert.assertEquals("a, b", StringUtil.replaceHtmlBreaks("a<br/>b<br/>"));
    }

    @Test
    public void testIsValidEmail() {
        Assert.assertFalse(StringUtil.isValidEmail(""));
        Assert.assertFalse(StringUtil.isValidEmail("@"));
        Assert.assertTrue(StringUtil.isValidEmail("a@b"));
        Assert.assertTrue(StringUtil.isValidEmail("kevin@opencirclesolutions.nl"));
    }
}
