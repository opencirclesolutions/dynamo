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

import java.util.Locale;

import com.vaadin.data.util.converter.Converter;

/**
 * Converter for converting between integers and doubles - used to be able to use
 * 
 * @author bas.rutten
 *
 */
public class IntToDoubleConverter implements Converter<Double, Integer> {

	private static final long serialVersionUID = -7643776654167662622L;

	@Override
	public Integer convertToModel(Double value, Class<? extends Integer> targetType, Locale locale) {
		if (value == null) {
			return null;
		}
		return value.intValue();
	}

	@Override
	public Double convertToPresentation(Integer value, Class<? extends Double> targetType, Locale locale) {
		if (value == null) {
			return null;
		}
		return value.doubleValue();
	}

	@Override
	public Class<Integer> getModelType() {
		return Integer.class;
	}

	@Override
	public Class<Double> getPresentationType() {
		return Double.class;
	}

}
