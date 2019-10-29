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

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;

/**
 * A BigDecimal converter that adds or removes a percentage sign
 * 
 * @author bas.rutten
 */
public class PercentageBigDecimalConverter extends BigDecimalConverter {

	private static final long serialVersionUID = 5566274434473612396L;

	/**
	 * Constructor
	 * 
	 * @param precision   the desired precision
	 * @param useGrouping whether to use thousands grouping
	 */
	public PercentageBigDecimalConverter(String message, int precision, boolean useGrouping) {
		super(message, precision, useGrouping);
	}

	@Override
	public String convertToPresentation(BigDecimal value, ValueContext context) {
		String result = super.convertToPresentation(value, context);
		return result == null ? null : result + "%";
	}

	@Override
	public Result<BigDecimal> convertToModel(String value, ValueContext context) {
		value = value == null ? null : value.replaceAll("%", "");
		return super.convertToModel(value, context);
	}
}
