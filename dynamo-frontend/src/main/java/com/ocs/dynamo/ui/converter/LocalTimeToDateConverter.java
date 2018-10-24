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
import java.util.Locale;

import com.ocs.dynamo.utils.DateUtils;
import com.vaadin.v7.data.util.converter.Converter;

/**
 * Vaadin Converter for converting between LocalTime and java.util.Date
 * 
 * @author bas.rutten
 *
 */
public class LocalTimeToDateConverter implements Converter<Date, LocalTime> {

    private static final long serialVersionUID = -830307549693107753L;

    @Override
    public LocalTime convertToModel(Date value, Class<? extends LocalTime> targetType, Locale locale) {
        return DateUtils.toLocalTime(value);
    }

    @Override
    public Date convertToPresentation(LocalTime value, Class<? extends Date> targetType, Locale locale) {
        return DateUtils.toLegacyTime(value);
    }

    @Override
    public Class<LocalTime> getModelType() {
        return LocalTime.class;
    }

    @Override
    public Class<Date> getPresentationType() {
        return Date.class;
    }

}
