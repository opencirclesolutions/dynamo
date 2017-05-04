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
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.ocs.dynamo.utils.DateUtils;
import com.vaadin.data.util.converter.Converter;

/**
 * Converter for converting between a String (representing a week code) and a LocalDate
 * 
 * @author bas.rutten
 *
 */
public class LocalDateWeekCodeConverter implements Converter<String, LocalDate> {

    private static final long serialVersionUID = -3286439685884522679L;

    @Override
    public LocalDate convertToModel(String value, Class<? extends LocalDate> targetType, Locale locale) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return DateUtils.toStartDateOfWeek(value);
    }

    @Override
    public String convertToPresentation(LocalDate value, Class<? extends String> targetType, Locale locale) {
        if (value == null) {
            return null;
        }
        return DateUtils.toWeekCode(value);
    }

    @Override
    public Class<LocalDate> getModelType() {
        return LocalDate.class;
    }

    @Override
    public Class<String> getPresentationType() {
        return String.class;
    }

}
