package com.ocs.dynamo.ui.converter;

import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.utils.DateUtils;

public class ZonedDateTimeToDateConverterTest {

	private ZonedDateTimeToDateConverter converter = new ZonedDateTimeToDateConverter();

	@Test
	public void testConvertToModel() {
		Date date = DateUtils.createDateTime("01122014 111213");
		ZonedDateTime zdt = converter.convertToModel(date, ZonedDateTime.class, null);
		Assert.assertNotNull(zdt);
	}
}
