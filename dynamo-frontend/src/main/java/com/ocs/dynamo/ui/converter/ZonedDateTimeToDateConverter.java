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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import com.ocs.dynamo.utils.DateUtils;
import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;

/**
 * 
 * @author bas.rutten
 *
 */
public class ZonedDateTimeToDateConverter implements Converter<Date, ZonedDateTime> {

	private static final long serialVersionUID = -830307549693107753L;

	@Override
	public Result<ZonedDateTime> convertToModel(Date value, ValueContext context) {
		if (value == null) {
			return null;
		}
		return Result.ok(DateUtils.convertSQLDate(value).toInstant().atZone(ZoneId.systemDefault()));
	}

	@Override
	public Date convertToPresentation(ZonedDateTime value, ValueContext context) {
		if (value == null) {
			return null;
		}
		return Date.from(value.toInstant());
	}

}
