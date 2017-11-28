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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.UI;

/**
 * Vaadin converter for converting between legacy date and
 * java.time.LocalDateTime. Uses the time zone from the browser to make this
 * work on all servers
 * 
 * @author bas.rutten
 *
 */
public class LocalDateTimeToDateConverter implements Converter<Date, LocalDateTime> {

	private static final long serialVersionUID = -830307549693107753L;

	@Override
	public LocalDateTime convertToModel(Date value, Class<? extends LocalDateTime> targetType, Locale locale) {
		if (value == null) {
			return null;
		}
		ZoneId tz = VaadinUtils.getTimeZone(UI.getCurrent()).toZoneId();
		return value.toInstant().atZone(tz).toLocalDateTime();
	}

	@Override
	public Date convertToPresentation(LocalDateTime value, Class<? extends Date> targetType, Locale locale) {
		if (value == null) {
			return null;
		}
		ZoneId tz = VaadinUtils.getTimeZone(UI.getCurrent()).toZoneId();
		return Date.from(value.atZone(tz).toInstant());
	}

	@Override
	public Class<LocalDateTime> getModelType() {
		return LocalDateTime.class;
	}

	@Override
	public Class<Date> getPresentationType() {
		return Date.class;
	}
}
