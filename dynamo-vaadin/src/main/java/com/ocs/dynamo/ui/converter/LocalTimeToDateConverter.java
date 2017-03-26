package com.ocs.dynamo.ui.converter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.Locale;

import com.ocs.dynamo.utils.DateUtils;
import com.vaadin.data.util.converter.Converter;

public class LocalTimeToDateConverter implements Converter<Date, LocalTime> {

	private static final long serialVersionUID = -830307549693107753L;

	@Override
	public LocalTime convertToModel(Date value, Class<? extends LocalTime> targetType, Locale locale) {
		return DateUtils.toLocalTime(value);
	}

	@Override
	public Date convertToPresentation(LocalTime value, Class<? extends Date> targetType, Locale locale) {
		return DateUtils.toLegacyTime(value);
	}

	@Override
	public Class<LocalTime> getModelType() {
		return LocalTime.class;
	}

	@Override
	public Class<Date> getPresentationType() {
		return Date.class;
	}

}
