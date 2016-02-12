package nl.ocs.ui.converter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * A converter for a BigDecimal field that includes a currency symbol.
 * 
 * @author bas.rutten
 *
 */
public class CurrencyBigDecimalConverter extends BigDecimalConverter {

	private static final long serialVersionUID = -8785156070280947096L;

	private String currencySymbol;

	public CurrencyBigDecimalConverter(int precision, boolean useGrouping, String currencySymbol) {
		super(precision, useGrouping);
		this.currencySymbol = currencySymbol;
	}

	@Override
	public BigDecimal convertToModel(String value, Class<? extends BigDecimal> targetType,
			Locale locale) {
		if (value != null && !value.startsWith(currencySymbol)) {
			value = currencySymbol + " " + value.trim();
		}
		return super.convertToModel(value, targetType, locale);
	}

	@Override
	protected DecimalFormat constructFormat(Locale locale) {
		// ignore the locale that is passed as a parameter, and use the default
		// locale instead so
		// that the number formatting is always the same
		DecimalFormat nf = (DecimalFormat) DecimalFormat.getCurrencyInstance(locale);
		DecimalFormatSymbols s = nf.getDecimalFormatSymbols();
		s.setCurrencySymbol(currencySymbol);
		nf.setDecimalFormatSymbols(s);
		return nf;
	}
}
