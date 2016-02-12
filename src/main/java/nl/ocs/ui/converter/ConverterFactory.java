package nl.ocs.ui.converter;

import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.data.util.converter.StringToLongConverter;

public final class ConverterFactory {

	private ConverterFactory() {
		// hidden constructor
	}

	/**
	 * Creates a BigDecimal converter
	 * 
	 * @param percentage
	 *            indicates whether the converter must take percentage signs
	 *            into account
	 * @return
	 */
	public static BigDecimalConverter createBigDecimalConverter(boolean currency,
			boolean percentage, boolean useGrouping, int precision, String currencySymbol) {
		if (currency) {
			return new CurrencyBigDecimalConverter(precision, useGrouping, currencySymbol);
		}
		if (percentage) {
			return new PercentageBigDecimalConverter(precision, useGrouping);
		}
		return new BigDecimalConverter(precision, useGrouping);
	}

	/**
	 * Creates a converter for converting between integer and String
	 * 
	 * @param useGrouping
	 * @return
	 */
	public static StringToIntegerConverter createIntegerConverter(boolean useGrouping) {
		return new GroupingStringToIntegerConverter(useGrouping);
	}

	/**
	 * Creates a converter for converting between long and String
	 * 
	 * @param useGrouping
	 * @return
	 */
	public static StringToLongConverter createLongConverter(boolean useGrouping) {
		return new GroupingStringToLongConverter(useGrouping);
	}
}
