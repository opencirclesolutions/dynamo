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
