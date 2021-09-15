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
package com.ocs.dynamo.ui.utils;

import java.util.Locale;

import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.grid.Grid;

import lombok.extern.slf4j.Slf4j;

/**
 * A template for handling a paste into a text field. The template will try to
 * split up the input into separate fields. The fields can be separated by any
 * kind of whitespace The template takes a locale as a constructor argument. Any
 * decimal separators in the input will be converted to the appropriate
 * separator for the provided locale
 * 
 * @author bas.rutten
 */
@Slf4j
public abstract class PasteTemplate<T> {

	private ValueChangeEvent<String> event;

	private Locale locale;

	private Grid<T> grid;

	private String value;

	/**
	 * @param locale
	 * @param event
	 */
	public PasteTemplate(Locale locale, Grid<T> grid, ValueChangeEvent<String> event, String value) {
		this.locale = locale;
		this.event = event;
		this.grid = grid;
		this.value = value;
	}

	public void execute() {
		String text = value;
		if (text != null) {
			// replace tabs and other whitespace
			String[] values = PasteUtils.split(text);
			if (values.length > 1) {
				// clear the source field
				clearSourceField(event);

				for (int i = 0; i < values.length; i++) {
					try {
						String temp = values[i];
						temp = PasteUtils.translateSeparators(temp, locale);
						// strip off any percent signs
						String s = temp.replaceAll("%", "");
						process(i, s);

					} catch (Exception ex) {
						log.error(ex.getMessage(), ex);
					}
				}
			}
		}
	}

	/**
	 * Processes a single value
	 * 
	 * @param index the index of the value in the list
	 * @param value
	 */
	protected abstract void process(int index, String value);

	/**
	 * Clears the source field. This can be used if the values are pasted in a cell
	 * that should be emptied after the paste action
	 * 
	 * @param event
	 */
	protected abstract void clearSourceField(ValueChangeEvent<String> event);

	public Grid<T> getGrid() {
		return grid;
	}

}
