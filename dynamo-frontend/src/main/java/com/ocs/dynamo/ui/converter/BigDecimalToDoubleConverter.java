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
import com.vaadin.flow.data.converter.Converter;

/**
 * Converter for converting between double and BigDecimal Description of
 * BigDecimalToDoubleConverter.
 * 
 * @author bas.rutten
 *
 */
public class BigDecimalToDoubleConverter implements Converter<Double, BigDecimal> {

	private static final long serialVersionUID = -2473928682501898867L;

	@Override
	public Result<BigDecimal> convertToModel(Double value, ValueContext context) {
		if (value == null) {
			return Result.ok(null);
		}
		return Result.ok(BigDecimal.valueOf(value));
	}

	@Override
	public Double convertToPresentation(BigDecimal value, ValueContext context) {
		if (value == null) {
			return null;
		}
		return value.doubleValue();
	}

}
