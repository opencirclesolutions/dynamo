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
package com.ocs.dynamo.ui.component;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.timepicker.TimePicker;

/**
 * A simple component for entering a date with a time stamp. This is a very bare
 * bones implementation that simply places a DatePicker and a TimePicker behind
 * each other
 * 
 * @author Bas Rutten
 *
 */
public class ZonedDateTimePicker extends CustomField<ZonedDateTime> {

    private static final long serialVersionUID = 8711426577974057547L;

    private DatePicker datePicker;

    private TimePicker timePicker;

    public ZonedDateTimePicker(Locale locale) {
        datePicker = new DatePicker();
        datePicker.setLocale(locale);
        timePicker = new TimePicker();
        timePicker.setLocale(locale);

        FormLayout hor = new FormLayout();
        hor.add(datePicker);
        hor.add(timePicker);
        add(hor);
    }

    @Override
    protected ZonedDateTime generateModelValue() {
        if (datePicker.getValue() == null) {
            return null;
        } else {
            LocalTime time = timePicker.getValue() == null ? LocalTime.of(0, 0) : timePicker.getValue();
            return ZonedDateTime.of(datePicker.getValue(), time, ZoneId.systemDefault());
        }
    }

    @Override
    protected void setPresentationValue(ZonedDateTime value) {
        if (value == null) {
            datePicker.setValue(null);
            timePicker.setValue(null);
        } else {
            datePicker.setValue(value.toLocalDate());
            timePicker.setValue(value.toLocalTime());
        }
    }

    @Override
    public void clear() {
        datePicker.clear();
        timePicker.clear();
    }

}
