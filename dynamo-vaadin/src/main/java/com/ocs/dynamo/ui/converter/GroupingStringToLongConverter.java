package com.ocs.dynamo.ui.converter;

import java.text.NumberFormat;
import java.util.Locale;

import com.vaadin.data.util.converter.StringToLongConverter;

/**
 * A converter from String to Long that allows the user to specify whether a
 * grouping separator must be used
 * 
 * @author bas.rutten
 */
public class GroupingStringToLongConverter extends StringToLongConverter {

	private static final long serialVersionUID = -281060172465120956L;

	private boolean useGrouping;

	public GroupingStringToLongConverter(boolean useGrouping) {
		this.useGrouping = useGrouping;
	}

	@Override
	protected NumberFormat getFormat(Locale locale) {
		NumberFormat format = super.getFormat(locale);
		format.setGroupingUsed(useGrouping);
		return format;
	}
}
