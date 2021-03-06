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

/**
 * 
 * @author bas.rutten
 *
 */
public class PercentageLongConverter extends GroupingStringToLongConverter {

	private static final long serialVersionUID = 2538721945689033561L;

	public PercentageLongConverter(String message, boolean useGrouping) {
		super(message, useGrouping);
	}

	@Override
	public String convertToPresentation(Long value, ValueContext context) {
		String result = super.convertToPresentation(value, context);
		return result == null ? null : result + "%";
	}

	@Override
	public Result<Long> convertToModel(String value, ValueContext context) {
		value = value == null ? null : value.replace("%", "");
		return super.convertToModel(value, context);
	}

}
