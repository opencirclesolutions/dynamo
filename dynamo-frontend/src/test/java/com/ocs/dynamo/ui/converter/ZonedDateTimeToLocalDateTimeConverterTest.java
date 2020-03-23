package com.ocs.dynamo.ui.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.vaadin.flow.data.binder.Result;

public class ZonedDateTimeToLocalDateTimeConverterTest extends BaseConverterTest {

	private ZonedDateTimeToLocalDateTimeConverter converter = new ZonedDateTimeToLocalDateTimeConverter(
			ZoneId.systemDefault());

	@Test
	public void testConvertToModel() {
		assertNull(converter.convertToModel(null, createContext()).getOrThrow(r -> new OCSRuntimeException()));

		LocalDateTime ldt = LocalDateTime.of(2018, 4, 4, 3, 3);
		Result<ZonedDateTime> zdt = converter.convertToModel(ldt, createContext());

		ZonedDateTime t = zdt.getOrThrow(r -> new OCSRuntimeException());
		assertEquals(ldt, t.toLocalDateTime());
	}

	@Test
	public void testConvertToPresentation() {
		assertNull(converter.convertToPresentation(null, createContext()));

		ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.of(LocalDate.of(2014, 12, 11), LocalTime.of(12, 11)),
				ZoneId.systemDefault());
		LocalDateTime ldt = converter.convertToPresentation(zdt, createContext());
		assertEquals(zdt.toLocalDateTime(), ldt);
	}

}
