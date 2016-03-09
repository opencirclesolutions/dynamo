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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.vaadin.data.util.converter.StringToDateConverter;

/**
 * A converter for converting between Strings and Dates, using a predetermined format
 * 
 * @author bas.rutten
 */
public class FormattedStringToDateConverter extends StringToDateConverter {

    private static final long serialVersionUID = -6772145828567618014L;

    /**
     * The date format
     */
    private String format;

    /**
     * The time zone
     */
    private TimeZone timeZone;

    /**
     * Constructor
     * 
     * @param timeZone
     *            the time zone
     * @param format
     *            the desired date format
     */
    public FormattedStringToDateConverter(TimeZone timeZone, String format) {
        this.format = format;
        this.timeZone = timeZone;
    }

    @Override
    protected DateFormat getFormat(Locale locale) {
        SimpleDateFormat df = locale == null ? new SimpleDateFormat(format)
                : new SimpleDateFormat(format, locale);
        if (timeZone != null) {
            df.setTimeZone(timeZone);
        }
        return df;
    }
}
