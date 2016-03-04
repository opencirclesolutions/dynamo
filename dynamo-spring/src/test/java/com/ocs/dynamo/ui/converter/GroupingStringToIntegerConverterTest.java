package com.ocs.dynamo.ui.converter;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for the PlainStringToIntegerConverter
 * 
 * @author bas.rutten
 */
public class GroupingStringToIntegerConverterTest {

	GroupingStringToIntegerConverter converter = new GroupingStringToIntegerConverter(false);

	@Test
	public void testToModel() {
		Integer value = converter.convertToModel("3000", Integer.class, new Locale("nl"));
		Assert.assertEquals(3000, value.intValue());
	}

	/**
	 * Make sure there is no grouping indicator
	 */
	@Test
	public void testToPresentation() {
		String value = converter.convertToPresentation(3000, String.class, new Locale("nl"));
		Assert.assertEquals("3000", value);
	}

	/**
	 * Make sure there is a grouping indicator
	 */
	@Test
	public void testToPresentationWithGrouping() {
		String value = new GroupingStringToIntegerConverter(true).convertToPresentation(3000,
		        String.class, new Locale("nl"));
		Assert.assertEquals("3.000", value);
	}

	@Test
	public void testToModelWithGrouping() {
		Integer value = new GroupingStringToIntegerConverter(true).convertToModel("3.000",
		        Integer.class, new Locale("nl"));
		Assert.assertEquals(3000, value.intValue());
	}
}
