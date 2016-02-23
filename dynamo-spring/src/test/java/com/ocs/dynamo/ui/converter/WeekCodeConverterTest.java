package com.ocs.dynamo.ui.converter;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.utils.DateUtils;

/**
 * Test cases for the WeekCodeConverter
 * 
 * @author bas.rutten
 * 
 */
public class WeekCodeConverterTest {

	public WeekCodeConverter converter = new WeekCodeConverter();

	@Test
	public void testToModel() {
		Date date = converter.convertToModel(null, null, null);
		Assert.assertNull(date);

		date = converter.convertToModel("2014-52", null, null);
		Assert.assertEquals(DateUtils.createDate("22122014"), date);

		date = converter.convertToModel("2015-01", null, null);
		Assert.assertEquals(DateUtils.createDate("29122014"), date);

		date = converter.convertToModel("2015-02", null, null);
		Assert.assertEquals(DateUtils.createDate("05012015"), date);

		date = converter.convertToModel("2015-52", null, null);
		Assert.assertEquals(DateUtils.createDate("21122015"), date);

		date = converter.convertToModel("2015-53", null, null);
		Assert.assertEquals(DateUtils.createDate("28122015"), date);

		date = converter.convertToModel("2016-01", null, null);
		Assert.assertEquals(DateUtils.createDate("04012016"), date);
	}

	@Test
	public void testToPresentation() {
		String str = converter.convertToPresentation(null, null, null);
		Assert.assertNull(str);

		str = converter.convertToPresentation(DateUtils.createDate("22122014"), null, null);
		Assert.assertEquals("2014-52", str);

		str = converter.convertToPresentation(DateUtils.createDate("29122014"), null, null);
		Assert.assertEquals("2015-01", str);
	}
}
