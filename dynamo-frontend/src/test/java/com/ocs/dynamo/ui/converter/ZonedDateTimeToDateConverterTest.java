package com.ocs.dynamo.ui.converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.utils.DateUtils;

public class ZonedDateTimeToDateConverterTest {

	private ZonedDateTimeToDateConverter converter = new ZonedDateTimeToDateConverter();

	@Test
	public void testConvertToModel() {
		Assert.assertNull(converter.convertToModel(null, ZonedDateTime.class, null));

		Date date = DateUtils.createDateTime("01122014 111213");
		ZonedDateTime zdt = converter.convertToModel(date, ZonedDateTime.class, null);
		Assert.assertNotNull(zdt);
	}

	@Test
	public void testConvertToPresentation() {
		Assert.assertNull(converter.convertToPresentation(null, Date.class, null));

		ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.of(LocalDate.of(2014, 12, 11), LocalTime.of(12, 11)),
				ZoneId.systemDefault());
		Date d = converter.convertToPresentation(zdt, Date.class, null);
		Assert.assertNotNull(d);
	}

}
