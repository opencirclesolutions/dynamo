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
