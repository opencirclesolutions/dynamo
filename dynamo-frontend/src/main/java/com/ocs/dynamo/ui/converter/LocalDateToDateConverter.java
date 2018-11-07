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

import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;
import com.vaadin.ui.UI;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Vaadin converter for converting a legacy date to a LocalDate TODO: is this
 * still needed in Vaadin 8
 * 
 * @author bas.rutten
 *
 */
public class LocalDateToDateConverter implements Converter<Date, LocalDate> {

	private static final long serialVersionUID = -830307549693107753L;

	@Override
	public Date convertToPresentation(LocalDate value, ValueContext context) {
		if (value == null) {
			return null;
		}
		ZoneId tz = VaadinUtils.getTimeZone(UI.getCurrent());
		return Date.from(value.atStartOfDay(tz).toInstant());
	}

	@Override
	public Result<LocalDate> convertToModel(Date value, ValueContext context) {
		if (value == null) {
			return null;
		}
		ZoneId tz = VaadinUtils.getTimeZone(UI.getCurrent());
		return Result.ok(value.toInstant().atZone(tz).toLocalDate());
	}

}
