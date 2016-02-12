package nl.ocs.ui.converter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.vaadin.data.util.converter.StringToDateConverter;

/**
 * A converter for converting between Strings and Dates, using a predetermined
 * format
 * 
 * @author bas.rutten
 * 
 */
public class FormattedStringToDateConverter extends StringToDateConverter {

	private static final long serialVersionUID = -6772145828567618014L;

	/**
	 * The date format
	 */
	private String format;

	/**
	 * The time zone
	 */
	private TimeZone timeZone;

	/**
	 * Constructor
	 * 
	 * @param timeZone
	 *            the time zone
	 * @param format
	 *            the desired date format
	 */
	public FormattedStringToDateConverter(TimeZone timeZone, String format) {
		this.format = format;
		this.timeZone = timeZone;
	}

	@Override
	protected DateFormat getFormat(Locale locale) {
		SimpleDateFormat df = locale == null ? new SimpleDateFormat(format) : new SimpleDateFormat(
				format, locale);
		if (timeZone != null) {
			df.setTimeZone(timeZone);
		}
		return df;
	}
}
