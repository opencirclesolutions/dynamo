package com.ocs.dynamo.ui.converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.vaadin.flow.data.binder.Result;

public class ZonedDateTimeToLocalDateTimeConverterTest extends BaseConverterTest {

	private ZonedDateTimeToLocalDateTimeConverter converter = new ZonedDateTimeToLocalDateTimeConverter(
			ZoneId.systemDefault());

	@Test
	public void testConvertToModel() {
		Assert.assertNull(converter.convertToModel(null, createContext()).getOrThrow(r -> new OCSRuntimeException()));

		LocalDateTime ldt = LocalDateTime.of(2018, 4, 4, 3, 3);
		Result<ZonedDateTime> zdt = converter.convertToModel(ldt, createContext());

		ZonedDateTime t = zdt.getOrThrow(r -> new OCSRuntimeException());
		Assert.assertEquals(ldt, t.toLocalDateTime());
	}

	@Test
	public void testConvertToPresentation() {
		Assert.assertNull(converter.convertToPresentation(null, createContext()));

		ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.of(LocalDate.of(2014, 12, 11), LocalTime.of(12, 11)),
				ZoneId.systemDefault());
		LocalDateTime ldt = converter.convertToPresentation(zdt, createContext());
		Assert.assertEquals(zdt.toLocalDateTime(), ldt);
	}

}
