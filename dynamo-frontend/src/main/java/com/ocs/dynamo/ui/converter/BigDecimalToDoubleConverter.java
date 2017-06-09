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
import java.util.Locale;

import com.vaadin.data.util.converter.Converter;

/**
 * Converter for coverting between double and BigDecimal Description of BigDecimalToDoubleConverter.
 * 
 * @author bas.rutten
 *
 */
public class BigDecimalToDoubleConverter implements Converter<Double, BigDecimal> {

	private static final long serialVersionUID = -2473928682501898867L;

	@Override
	public Class<Double> getPresentationType() {
		return Double.class;
	}

	@Override
	public BigDecimal convertToModel(Double value, Class<? extends BigDecimal> targetType, Locale locale) {
		if (value == null) {
			return null;
		}
		return BigDecimal.valueOf(value);
	}

	@Override
	public Double convertToPresentation(BigDecimal value, Class<? extends Double> targetType, Locale locale) {
		if (value == null) {
			return null;
		}
		return value.doubleValue();
	}

	@Override
	public Class<BigDecimal> getModelType() {
		return BigDecimal.class;
	}

}
