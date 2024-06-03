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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.StringToDoubleConverter;

/**
 * A converter for converting between Strings and Doubles
 * 
 * @author bas.rutten
 */
public class GroupingStringToDoubleConverter extends StringToDoubleConverter {

	private static final long serialVersionUID = -6491010958762673241L;

	/**
	 * Whether to use a thousand grouping separator
	 */
	private final boolean useGrouping;

	/**
	 * The desired decimal precision
	 */
	private final int precision;

	public GroupingStringToDoubleConverter(String message, int precision, boolean useGrouping) {
		super(message);
		this.precision = precision;
		this.useGrouping = useGrouping;
	}

	@Override
	public Result<Double> convertToModel(String value, ValueContext context) {
		if (value == null) {
			return Result.ok(null);
		}
		Result<Number> number = convertToNumber(value, context);
		return number.flatMap(r -> {
			Double d = r == null ? null : r.doubleValue();
			return Result.ok(d);
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
		decimalFormat.setGroupingUsed(useGrouping);
		decimalFormat.setMaximumFractionDigits(precision);
		decimalFormat.setMinimumFractionDigits(precision);
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
