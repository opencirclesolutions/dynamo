package nl.ocs.ui.converter;

import java.text.NumberFormat;
import java.util.Locale;

import com.vaadin.data.util.converter.StringToIntegerConverter;

/**
 * A string to integer converter that allows support the user to specify whether
 * a thousands separator must be used
 * 
 * @author bas.rutten
 *
 */
public class GroupingStringToIntegerConverter extends StringToIntegerConverter {

	private static final long serialVersionUID = -281060172465120956L;

	private boolean useGrouping;

	public GroupingStringToIntegerConverter(boolean useGrouping) {
		this.useGrouping = useGrouping;
	}

	@Override
	protected NumberFormat getFormat(Locale locale) {
		NumberFormat format = super.getFormat(locale);
		format.setGroupingUsed(useGrouping);
		return format;
	}
}
