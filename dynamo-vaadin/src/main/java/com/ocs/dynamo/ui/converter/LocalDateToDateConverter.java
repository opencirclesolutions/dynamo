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

import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;

import com.ocs.dynamo.utils.DateUtils;
import com.vaadin.data.util.converter.Converter;

public class LocalDateToDateConverter implements Converter<Date, LocalDate> {

	private static final long serialVersionUID = -830307549693107753L;

	@Override
	public LocalDate convertToModel(Date value, Class<? extends LocalDate> targetType, Locale locale) {
		return DateUtils.toLocalDate(value);
	}

	@Override
	public Date convertToPresentation(LocalDate value, Class<? extends Date> targetType, Locale locale) {
		return DateUtils.toLegacyDate(value);
	}

	@Override
	public Class<LocalDate> getModelType() {
		return LocalDate.class;
	}

	@Override
	public Class<Date> getPresentationType() {
		return Date.class;
	}

}
