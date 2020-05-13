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
import java.util.TimeZone;

import com.google.common.collect.Lists;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.WrapMode;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.provider.ListDataProvider;

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

    private ComboBox<String> timeZone;

    public ZonedDateTimePicker(Locale locale) {
        datePicker = new DatePicker();
        datePicker.setLocale(locale);
        timePicker = new TimePicker();
        timePicker.setLocale(locale);

        timeZone = new ComboBox<>();
        ListDataProvider<String> provider = new ListDataProvider<>(Lists.newArrayList(TimeZone.getAvailableIDs()));
        timeZone.setDataProvider(provider);

        FlexLayout hor = new FlexLayout();
        hor.setWrapMode(WrapMode.WRAP);
        hor.add(datePicker);
        hor.add(timePicker);
        hor.add(timeZone);
        add(hor);
    }

    @Override
    protected ZonedDateTime generateModelValue() {
        if (datePicker.getValue() == null) {
            return null;
        } else {
            LocalTime time = timePicker.getValue() == null ? LocalTime.of(0, 0) : timePicker.getValue();
            ZoneId zone = timeZone.getValue() == null ? ZoneId.systemDefault() : ZoneId.of(timeZone.getValue());
            return ZonedDateTime.of(datePicker.getValue(), time, zone);
        }
    }

    @Override
    protected void setPresentationValue(ZonedDateTime value) {
        if (value == null) {
            datePicker.setValue(null);
            timePicker.setValue(null);
            timeZone.setValue(ZoneId.systemDefault().toString());
        } else {
            datePicker.setValue(value.toLocalDate());
            timePicker.setValue(value.toLocalTime());
            timeZone.setValue(value.getZone().toString());
        }
    }

    @Override
    public void clear() {
        datePicker.clear();
        timePicker.clear();
        timeZone.setValue(ZoneId.systemDefault().toString());
    }

}
