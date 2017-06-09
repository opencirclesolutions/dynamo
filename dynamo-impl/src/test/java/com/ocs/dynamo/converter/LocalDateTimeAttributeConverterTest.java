package com.ocs.dynamo.converter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.converter.LocalDateTimeAttributeConverter;
import com.ocs.dynamo.utils.DateUtils;

public class LocalDateTimeAttributeConverterTest {

	@Test
	public void testConvertToDatabaseColumn() {
		LocalDateTimeAttributeConverter cv = new LocalDateTimeAttributeConverter();

		Assert.assertNull(cv.convertToDatabaseColumn(null));

		Timestamp t = cv.convertToDatabaseColumn(DateUtils.createLocalDateTime("15072016 111213"));
		Assert.assertEquals("2016-07-15 11:12:13.0", t.toString());
	}

	@Test
	public void testConvertToEntityAttribute() {
		LocalDateTimeAttributeConverter cv = new LocalDateTimeAttributeConverter();

		Assert.assertNull(cv.convertToEntityAttribute(null));

		LocalDateTime ldt = cv.convertToEntityAttribute(Timestamp.valueOf("1984-06-05 14:15:16"));
		Assert.assertEquals("1984-06-05T14:15:16", ldt.toString());
	}

}
