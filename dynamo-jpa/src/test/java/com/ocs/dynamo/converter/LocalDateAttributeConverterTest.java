package com.ocs.dynamo.converter;

import java.sql.Date;
import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.utils.DateUtils;

public class LocalDateAttributeConverterTest {

	@Test
	public void testConvertToDatabaseColumn() {
		LocalDateAttributeConverter cv = new LocalDateAttributeConverter();

		Assert.assertNull(cv.convertToDatabaseColumn(null));

		Date d = cv.convertToDatabaseColumn(DateUtils.createLocalDate("15032016"));
		Assert.assertEquals("2016-03-15", d.toString());
	}

	@Test
	public void testConvertToEntityAttribute() {
		LocalDateAttributeConverter cv = new LocalDateAttributeConverter();

		Assert.assertNull(cv.convertToEntityAttribute(null));

		LocalDate d = cv.convertToEntityAttribute(Date.valueOf("1984-06-05"));
		Assert.assertEquals("1984-06-05", d.toString());
	}
}
