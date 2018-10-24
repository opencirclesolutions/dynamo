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

import java.time.LocalTime;
import java.util.Date;

import com.ocs.dynamo.utils.DateUtils;
import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;

/**
 * Vaadin Converter for converting between LocalTime and java.util.Date
 * 
 * @author bas.rutten
 *
 */
public class LocalTimeToDateConverter implements Converter<Date, LocalTime> {

	private static final long serialVersionUID = -830307549693107753L;

	@Override
	public Result<LocalTime> convertToModel(Date value, ValueContext context) {
		return Result.ok(DateUtils.toLocalTime(value));
	}

	@Override
	public Date convertToPresentation(LocalTime value, ValueContext context) {
		return DateUtils.toLegacyTime(value);
	}

}
