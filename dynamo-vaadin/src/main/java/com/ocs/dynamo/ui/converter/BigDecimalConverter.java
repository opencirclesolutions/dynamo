/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.ui.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.util.converter.StringToBigDecimalConverter;

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
	public BigDecimal convertToModel(String value, Class<? extends BigDecimal> targetType, Locale locale) {
		// the original Vaadin code curiously returns a Double here and casts
		// that to a BigDecimal.
		// That is not correct, so we use this additional step here
		Number number = convertToNumber(value, BigDecimal.class, locale);
		return number == null ? null : BigDecimal.valueOf(number.doubleValue()).setScale(precision,
		        RoundingMode.HALF_UP);
	}

	@Override
	protected NumberFormat getFormat(Locale locale) {
		return getDecimalFormat(locale);
	}

	/**
	 * @param locale
	 * @return
	 */
	public DecimalFormat getDecimalFormat(Locale locale) {
		locale = locale != null ? locale : VaadinUtils.getLocale();
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
