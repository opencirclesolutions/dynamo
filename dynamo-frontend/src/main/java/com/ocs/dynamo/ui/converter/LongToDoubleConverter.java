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

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

/**
 * Converter for converting between Long and Double. primarily used for Slider
 * component
 * 
 * @author Bas Rutten
 *
 */
public class LongToDoubleConverter implements Converter<Double, Long> {

	private static final long serialVersionUID = 8666806617896024450L;

	@Override
	public Result<Long> convertToModel(Double value, ValueContext context) {
		if (value == null) {
			return Result.ok(null);
		}
		return Result.ok(value.longValue());
	}

	@Override
	public Double convertToPresentation(Long value, ValueContext context) {
		if (value == null) {
			return null;
		}
		return value.doubleValue();
	}

}
