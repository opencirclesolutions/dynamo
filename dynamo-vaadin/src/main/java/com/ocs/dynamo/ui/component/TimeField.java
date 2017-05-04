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

import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A custom field for displaying a time - renders an hour combo box and a minute combo box
 * 
 * @author bas.rutten
 */
//TODO java.util.Date -> java.time.LocalDateTime
public class TimeField extends CustomField<Date> {

	private static final long serialVersionUID = -676425827861766118L;

	private boolean use24HourClock = true;
	private Locale givenLocale = null;

	private final NativeSelect hourSelect;
	private final NativeSelect minuteSelect;
	private final NativeSelect secondSelect;

	private Resolution resolution = Resolution.MINUTE;
	private int intervalMinutes = 1;
	private int minHours = 0;
	private int maxHours = 23;

	private boolean maskInternalValueChange = false;

	private HorizontalLayout root;

	public TimeField(String caption) {
		this();
		setCaption(caption);
	}

	public TimeField() {

		hourSelect = getSelect();
		minuteSelect = getSelect();
		secondSelect = getSelect();

		root = new HorizontalLayout();
		root.setHeight(null);
		root.setWidth(null);

		fillHours();
		root.addComponent(hourSelect);

		fillMinutes();
		root.addComponent(minuteSelect);

		fillSeconds();
		root.addComponent(secondSelect);

		// when setValue() is called, update selects
		addValueChangeListener((ValueChangeListener) event -> {
            if (maskInternalValueChange) {
                return;
            }
            updateFields();
        });

		addStyleName("timefield");
		setSizeUndefined();

		updateFields();
	}

	/**
	 * Returns hour value in 24-hour format (0-23)
	 */
	@SuppressWarnings("deprecation")
	public int getHours() {
		Date d = getValue();
		return d.getHours();
	}

	/**
	 * Set hour in 24-hour format (0-23).
	 * 
	 * @param hours
	 * @throws IllegalArgumentException
	 *             If the hour parameter is outside the bounds marked by {@link #getHourMin()} and
	 *             {@link #getHourMax()}
	 */
	public void setHours(int hours) {

		if (hours < minHours || hours > maxHours) {
			throw new IllegalArgumentException("Value '" + hours + "' is outside bounds '" + minHours + "' - '"
			        + maxHours + "'");
		}

		hourSelect.setValue(hours);
	}

	@SuppressWarnings("deprecation")
	public int getMinutes() {
		Date d = getValue();
		return d.getMinutes();
	}

	/**
	 * @param minutes
	 * @throws IllegalArgumentException
	 *             If the minute parameter is not compatible with {@link #getMinuteInterval()}
	 */
	public void setMinutes(int minutes) {
		if (minutes % intervalMinutes != 0) {
			throw new IllegalArgumentException("Value '" + minutes + "' is not compatible with interval '"
			        + intervalMinutes + "'");
		}
		minuteSelect.setValue(minutes);
	}

	@SuppressWarnings("deprecation")
	public int getSeconds() {
		Date d = getValue();
		return d.getSeconds();
	}

	public void setSeconds(int seconds) {
		secondSelect.setValue(seconds);
	}

	private void fillHours() {

		hourSelect.removeAllItems();

		for (int i = minHours; i <= maxHours; i++) {
			hourSelect.addItem(i);
			if (!use24HourClock) {
				Integer val = i == 0 || i == 12 ? 12 : i % 12;

				String suffix;
				if (i < 12) {
					suffix = " am";
				} else {
					suffix = " pm";
				}
				hourSelect.setItemCaption(i, val + suffix);
			}
		}
	}

	private void fillMinutes() {

		minuteSelect.removeAllItems();
		for (int i = 0; i < 60; i++) {
			if (i % intervalMinutes == 0) {
				minuteSelect.addItem(i);
				minuteSelect.setItemCaption(i, i < 10 ? "0" + i : Integer.toString(i));
			}
		}
	}

	private void fillSeconds() {

		secondSelect.removeAllItems();
		for (int i = 0; i < 60; i++) {
			secondSelect.addItem(i);
			secondSelect.setItemCaption(i, i < 10 ? "0" + i : Integer.toString(i));
		}
	}

	private NativeSelect getSelect() {
		NativeSelect select = new NativeSelect();
		select.setImmediate(true);
		select.setNullSelectionAllowed(false);
		select.addValueChangeListener((ValueChangeListener) event -> {
            if (maskInternalValueChange) {
                return;
            }
            maskInternalValueChange = true;
            updateValue();
            TimeField.this.fireValueChange(true);
            maskInternalValueChange = false;

        });
		return select;
	}

	/**
	 * This method gets called when any of the internal Select components are called.
	 */
	@SuppressWarnings("deprecation")
	private void updateValue() {

		// if the value of any select is null at this point, we need to init it
		// to zero to prevent exceptions.

		if (secondSelect.getValue() == null) {
			secondSelect.setValue(0);
		}
		if (minuteSelect.getValue() == null) {
			minuteSelect.setValue(0);
		}
		if (hourSelect.getValue() == null) {
			hourSelect.setValue(0);
		}

		Date newDate = new Date(0);
		newDate.setSeconds(0);
		newDate.setMinutes(0);
		newDate.setHours(0);

		int val;
		if (resolution.ordinal() <= Resolution.HOUR.ordinal()) {
			val = (Integer) hourSelect.getValue();

			newDate.setHours(val);
		}
		if (resolution.ordinal() < Resolution.HOUR.ordinal()) {
			val = (Integer) minuteSelect.getValue();
			newDate.setMinutes(val);
		}
		if (resolution.ordinal() < Resolution.MINUTE.ordinal()) {
			val = (Integer) secondSelect.getValue();
			newDate.setSeconds(val);
		}

		setValue(newDate);
	}

	/**
	 * This method gets called when we update the actual value (Date) of this TimeField.
	 */
	@SuppressWarnings("deprecation")
	private void updateFields() {

		maskInternalValueChange = true;

		minuteSelect.setVisible(resolution.ordinal() < Resolution.HOUR.ordinal());
		secondSelect.setVisible(resolution.ordinal() < Resolution.MINUTE.ordinal());

		Date val = getValue();

		// make sure to update alternatives, wont spoil value above
		fillHours();
		fillMinutes();

		if (val == null) {
			// clear values
			hourSelect.setValue(null);
			minuteSelect.setValue(null);
			secondSelect.setValue(null);

			maskInternalValueChange = false;
			return;
		}

		// check hour bounds
		if (val.getHours() < minHours) {
			// guess
			int compatibleVal = val.getHours();
			while (compatibleVal < 24) {
				if (compatibleVal >= minHours) {
					break;
				}
				compatibleVal++;
			}
			if (compatibleVal >= minHours) {
				val.setHours(compatibleVal);
			}
		} else if (val.getHours() > maxHours) {
			// guess
			int compatibleVal = val.getHours();
			while (compatibleVal > 0) {
				if (compatibleVal <= maxHours) {
					break;
				}
				compatibleVal--;
			}
			if (compatibleVal <= maxHours) {
				val.setHours(compatibleVal);
			}
		}
		hourSelect.setValue(val.getHours());

		// check minute interval
		if (val.getMinutes() % intervalMinutes != 0) {
			// guess
			int compatibleVal = val.getMinutes();
			while (compatibleVal > 0) {
				if (compatibleVal % intervalMinutes == 0) {
					break;
				}
				compatibleVal--;
			}
			val.setMinutes(compatibleVal);
		}
		minuteSelect.setValue(val.getMinutes());

		secondSelect.setValue(val.getSeconds());

		maskInternalValueChange = false;
	}

	/**
	 * Sets locale that is used for time format detection. Defaults to browser locale.
	 */
	@Override
	public void setLocale(Locale l) {
		givenLocale = l;

		DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, givenLocale);
		String time = df.format(new Date());

		if (time.contains("am") || time.contains("AM") || time.contains("pm") || time.contains("PM")) {
			use24HourClock = false;
		} else {
			use24HourClock = true;
		}
		updateFields();
	}

	/**
	 * Only resolutions of HOUR, MIN, SECOND are supported. Default is {@link Resolution#MINUTE}
	 * 
	 * @see {@link com.vaadin.ui.DateField#getResolution()}
	 * @param resolution
	 */
	public void setResolution(Resolution resolution) {

		if (this.resolution.ordinal() < resolution.ordinal()) {
			if (resolution.ordinal() > Resolution.SECOND.ordinal()) {
				secondSelect.setValue(0);
			}
			if (resolution.ordinal() > Resolution.MINUTE.ordinal()) {
				minuteSelect.setValue(0);
			}
		}
		this.resolution = resolution;
		updateFields();
	}

	/**
	 * @see {@link com.vaadin.ui.DateField#getResolution()}
	 * @return
	 */
	public Resolution getResolution() {
		return resolution;
	}

	@Override
	public void attach() {
		super.attach();

		// if there isn't a given locale at this point, use the one from the
		// browser
		if (givenLocale == null) {
			// we have access to application after attachment
			@SuppressWarnings("deprecation")
			Locale locale = getUI().getSession().getBrowser().getLocale();
			givenLocale = locale;
		}
	}

	public void set24HourClock(boolean use24hourclock) {
		use24HourClock = use24hourclock;
		updateFields();
	}

	public boolean is24HourClock() {
		return use24HourClock;
	}

	@Override
	public Class<? extends Date> getType() {
		return Date.class;
	}

	@Override
	protected Component initContent() {

		return root;
	}

	@Override
	public void setTabIndex(int tabIndex) {
		hourSelect.setTabIndex(tabIndex);
		minuteSelect.setTabIndex(tabIndex);
		secondSelect.setTabIndex(tabIndex);
	}

	@Override
	public int getTabIndex() {
		return hourSelect.getTabIndex();
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		hourSelect.setReadOnly(readOnly);
		minuteSelect.setReadOnly(readOnly);
		secondSelect.setReadOnly(readOnly);
		super.setReadOnly(readOnly);
	}

	public int getMinuteInterval() {
		return intervalMinutes;
	}

	/**
	 * Set the interval to be used in minute select. Default is 1 minute intervals.
	 * 
	 * @param interval
	 */
	public void setMinuteInterval(int interval) {
		if (interval < 0) {
			interval = 0;
		}
		intervalMinutes = interval;
		updateFields();
	}

	public int getHourMin() {
		return minHours;
	}

	/**
	 * Set the minimum allowed value for the hour, in 24h format. Defaults to 0.
	 * <p>
	 * Changing this setting so that the field has a value outside the new bounds will lead to the
	 * field resetting its value to the first bigger value that is valid.
	 */
	public void setHourMin(int minHours) {

		if (minHours < 0) {
			minHours = 0;
		}
		if (minHours > 23) {
			minHours = 23;
		}

		this.minHours = minHours;
		updateFields();
	}

	public int getHourMax() {
		return maxHours;
	}

	/**
	 * Set the maximum allowed value for the hour, in 24h format. Defaults to 23.
	 * <p>
	 * Changing this setting so that the field has a value outside the new bounds will lead to the
	 * field resetting its value to the first smaller value that is valid.
	 */
	public void setHourMax(int maxHours) {

		if (maxHours < 0) {
			maxHours = 0;
		}
		if (maxHours > 23) {
			maxHours = 23;
		}

		this.maxHours = maxHours;
		updateFields();
	}
}
