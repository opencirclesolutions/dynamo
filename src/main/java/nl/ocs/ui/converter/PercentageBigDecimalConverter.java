package nl.ocs.ui.converter;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * A BigDecimal converter that adds or removes a percentage sign
 * 
 * @author bas.rutten
 * 
 */
public class PercentageBigDecimalConverter extends BigDecimalConverter {

	private static final long serialVersionUID = 5566274434473612396L;

	/**
	 * Constructor
	 * @param precision
	 * @param useGrouping
	 */
	public PercentageBigDecimalConverter(int precision, boolean useGrouping) {
		super(precision, useGrouping);
	}

	@Override
	public String convertToPresentation(BigDecimal value, Class<? extends String> targetType,
			Locale locale) {
		String result = super.convertToPresentation(value, targetType, locale);
		return result == null ? null : result + "%";
	}

	@Override
	public BigDecimal convertToModel(String value, Class<? extends BigDecimal> targetType,
			Locale locale) {
		value = value == null ? null : value.replaceAll("%", "");
		return super.convertToModel(value, targetType, locale);
	}
}
