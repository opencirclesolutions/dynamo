package com.ocs.dynamo.ui.converter;

import java.util.Locale;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.utils.DateUtils;

/**
 * Test cases for the FormattedStringToDateConverter
 * 
 * @author bas.rutten
 */
public class FormattedStringToDateConverterTest {

	private FormattedStringToDateConverter converter = new FormattedStringToDateConverter(
	        TimeZone.getTimeZone("CET"), "dd-MM-yyyy");

	private FormattedStringToDateConverter converter2 = new FormattedStringToDateConverter(
	        TimeZone.getTimeZone("CET"), "d/M/yyyy");

	@Test
	public void testToPresentation() {
		String s = converter.convertToPresentation(DateUtils.createDate("31052015"), null, null);
		Assert.assertEquals("31-05-2015", s);
	}

	@Test
	public void testToPresentation2() {
		String s = converter2.convertToPresentation(DateUtils.createDate("31052015"), String.class,
		        null);
		Assert.assertEquals("31/5/2015", s);
	}

	@Test
	public void testToPresentation3() {
		String s = converter2.convertToPresentation(DateUtils.createDate("31052015"), String.class,
		        new Locale("nl"));
		Assert.assertEquals("31/5/2015", s);
	}

}
