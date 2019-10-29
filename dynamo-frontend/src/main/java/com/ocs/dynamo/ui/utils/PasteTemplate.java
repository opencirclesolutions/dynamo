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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.grid.Grid;

/**
 * A template for handling a paste into a text field. The template will try to
 * split up the input into separate fields. The fields can be separated by any
 * kind of whitespace The template takes a locale as a constructor argument. Any
 * decimal separators in the input will be converted to the appropriate
 * separator for the provided locale
 * 
 * @author bas.rutten
 */
public abstract class PasteTemplate<T> {

	private static final Logger LOG = LoggerFactory.getLogger(PasteTemplate.class);

	private ValueChangeEvent<String> event;

	private Locale locale;

	private Grid<T> grid;

	/**
	 * @param locale
	 * @param event
	 */
	public PasteTemplate(Locale locale, Grid<T> grid, ValueChangeEvent<String> event) {
		this.locale = locale;
		this.event = event;
		this.grid = grid;
	}

	public void execute() {
		String text = event.getValue();
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
						LOG.error(ex.getMessage(), ex);
					}
				}
				if (grid != null) {
					//grid.markAsDirty();
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
