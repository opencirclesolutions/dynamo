package com.ocs.dynamo.ui.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.util.converter.StringToBigDecimalConverter;
import com.vaadin.server.VaadinSession;

/**
 * A converter for converting between Strings and BigDecimals
 * 
 * @author bas.rutten
 */
public class BigDecimalConverter extends StringToBigDecimalConverter {

	private static final long serialVersionUID = -6491010958762673241L;

	private DecimalFormat decimalFormat;

	private String pattern;

	private boolean useGrouping;

	private int precision;

	/**
	 * Constructor - for use with a pattern
	 * 
	 * @param pattern
	 *            will be applied to the decimalFormat of this converter.
	 */
	public BigDecimalConverter(final String pattern) {
		this.pattern = pattern;
	}

	/**
	 * Constructor - for use with a precision and grouping setting
	 */
	public BigDecimalConverter(int precision, boolean useGrouping) {
		this.precision = precision;
		this.useGrouping = useGrouping;
	}

	@Override
	public BigDecimal convertToModel(String value, Class<? extends BigDecimal> targetType,
	        Locale locale) {
		// the original Vaadin code curiously returns a Double here and casts
		// that to a BigDecimal.
		// That is not correct, so we use this additional step here
		Number number = convertToNumber(value, BigDecimal.class, locale);
		return number == null ? null
		        : BigDecimal.valueOf(number.doubleValue()).setScale(precision,
		                RoundingMode.HALF_UP);
	}

	@Override
	protected NumberFormat getFormat(Locale locale) {
		return getDecimalFormat(locale);
	}

	public DecimalFormat getDecimalFormat() {
		return getDecimalFormat(Locale.getDefault());
	}

	/**
	 * @param locale
	 * @return
	 */
	public DecimalFormat getDecimalFormat(Locale locale) {

		if (locale == null) {
			if (VaadinSession.getCurrent() != null) {
				locale = VaadinSession.getCurrent().getLocale();
			} else {
				locale = Locale.getDefault();
			}
		}
		decimalFormat = constructFormat(locale);

		if (!StringUtils.isEmpty(pattern)) {
			decimalFormat.applyPattern(pattern);
		} else {
			decimalFormat.setGroupingUsed(useGrouping);
			decimalFormat.setMaximumFractionDigits(precision);
			decimalFormat.setMinimumFractionDigits(precision);
		}
		return decimalFormat;
	}

	/**
	 * Constructs the number format - overwrite in subclasses if needed
	 * 
	 * @param locale
	 * @return
	 */
	protected DecimalFormat constructFormat(Locale locale) {
		return (DecimalFormat) DecimalFormat.getInstance(locale);
	}
}