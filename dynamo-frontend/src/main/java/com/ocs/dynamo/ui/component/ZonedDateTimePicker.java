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

import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.provider.ListDataProvider;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A simple component for entering a date with a time stamp. This is a very basic implementation that simply places a DatePicker and a TimePicker behind
 * each other
 * 
 * @author Bas Rutten
 *
 */
public class ZonedDateTimePicker extends CustomField<ZonedDateTime> {

	private static final long serialVersionUID = 8711426577974057547L;

	private final DatePicker datePicker;

	private final TimePicker timePicker;

	private final ComboBox<String> timeZone;

	public ZonedDateTimePicker(Locale locale) {
		datePicker = new DatePicker();
		datePicker.setLocale(locale);
		timePicker = new TimePicker();
		timePicker.setLocale(locale);

		timeZone = new ComboBox<>();
		ListDataProvider<String> provider = new ListDataProvider<>(List.of(TimeZone.getAvailableIDs()));
		timeZone.setItems(provider);
		timeZone.setValue(VaadinUtils.getTimeZoneId().toString());

		FlexLayout flexLayout = new FlexLayout();
		flexLayout.setFlexWrap(FlexWrap.WRAP);
		flexLayout.add(datePicker);
		flexLayout.add(timePicker);
		flexLayout.add(timeZone);
		add(flexLayout);
	}

	@Override
	public void clear() {
		datePicker.clear();
		timePicker.clear();
		timeZone.setValue(VaadinUtils.getTimeZoneId().toString());
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

	public void setI18n(DatePickerI18n i18n) {
		datePicker.setI18n(i18n);
	}

	@Override
	protected void setPresentationValue(ZonedDateTime value) {
		if (value == null) {
			datePicker.clear();
			timePicker.clear();
			timeZone.setValue(ZoneId.systemDefault().toString());
		} else {
			datePicker.setValue(value.toLocalDate());
			timePicker.setValue(value.toLocalTime());
			timeZone.setValue(value.getZone().toString());
		}
	}

}
