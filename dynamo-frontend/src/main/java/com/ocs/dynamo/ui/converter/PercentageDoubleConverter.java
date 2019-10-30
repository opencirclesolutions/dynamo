package com.ocs.dynamo.ui.converter;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;

public class PercentageDoubleConverter extends GroupingStringToDoubleConverter {

	private static final long serialVersionUID = 5566274434473612396L;

	/**
	 * Constructor
	 * 
	 * @param precision   the desired precision
	 * @param useGrouping whether to use thousands grouping
	 */
	public PercentageDoubleConverter(String message, int precision, boolean useGrouping) {
		super(message, precision, useGrouping);
	}

	@Override
	public String convertToPresentation(Double value, ValueContext context) {
		String result = super.convertToPresentation(value, context);
		return result == null ? null : result + "%";
	}

	@Override
	public Result<Double> convertToModel(String value, ValueContext context) {
		value = value == null ? null : value.replace("%", "");
		return super.convertToModel(value, context);
	}
}
