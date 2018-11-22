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
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;
import com.vaadin.data.converter.StringToBigDecimalConverter;

/**
 * A converter for converting between Strings and BigDecimals
 * 
 * @author bas.rutten
 */
public class BigDecimalConverter extends StringToBigDecimalConverter {

	private static final long serialVersionUID = -6491010958762673241L;

	private String pattern;

	/**
	 * Whether to usea thousands grouping separator
	 */
	private boolean useGrouping;

	/**
	 * The desired decimal precision
	 */
	private int precision;

	/**
	 * Constructor - for use with a pattern
	 * 
	 * @param pattern will be applied to the decimalFormat of this converter.
	 */
	public BigDecimalConverter(String message, String pattern) {
		super(message);
		this.pattern = pattern;
	}

	/**
	 * Constructor - for use with a precision and grouping setting
	 */
	public BigDecimalConverter(String message, int precision, boolean useGrouping) {
		super(message);
		this.precision = precision;
		this.useGrouping = useGrouping;
	}

	@Override
	public Result<BigDecimal> convertToModel(String value, ValueContext context) {
		if (value == null) {
			return Result.ok(null);
		}

		Result<Number> number = convertToNumber(value, context);
		return number.flatMap(r -> {
			BigDecimal bd = r == null ? null
					: BigDecimal.valueOf(r.doubleValue()).setScale(precision, RoundingMode.HALF_UP);
			return Result.ok(bd);
		});
	}

	@Override
	protected NumberFormat getFormat(Locale locale) {
		return getDecimalFormat(locale);
	}

	/**
	 * Constructs the DecimalFormat to use for formatting the values
	 * 
	 * @param locale the desired locale to use for the formatting
	 * @return
	 */
	public DecimalFormat getDecimalFormat(Locale locale) {
		locale = locale != null ? locale : VaadinUtils.getLocale();
		DecimalFormat decimalFormat = constructFormat(locale);

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
