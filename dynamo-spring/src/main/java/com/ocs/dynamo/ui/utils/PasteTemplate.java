package com.ocs.dynamo.ui.utils;

import java.util.Locale;

import org.apache.log4j.Logger;

import com.ocs.dynamo.utils.PasteUtils;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.ui.Table;

/**
 * A template for handling a paste into a text field. The template will try to
 * split up the input into separate fields. The fields can be separated by any
 * kind of whitespace
 * The template takes a locale as a constructor argument. Any decimal separators
 * in the input will be converted to the appropriate separator for the provided
 * locale
 * 
 * @author bas.rutten
 */
public abstract class PasteTemplate {

	private static final Logger LOG = Logger.getLogger(PasteTemplate.class);

	private TextChangeEvent event;

	private Locale locale;

	private Table table;

	/**
	 * @param locale
	 * @param event
	 */
	public PasteTemplate(Locale locale, Table table, TextChangeEvent event) {
		this.locale = locale;
		this.event = event;
		this.table = table;
	}

	public void execute() {
		String text = event.getText();
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
				if (table != null) {
					table.refreshRowCache();
				}
			}
		}
	}

	/**
	 * Processes a single value
	 * 
	 * @param index
	 *            the index of the value in the list
	 * @param value
	 */
	protected abstract void process(int index, String value);

	/**
	 * Clears the source field. This can be used if the values are pasted in a
	 * cell that should be emptied after the paste action
	 * 
	 * @param event
	 */
	protected abstract void clearSourceField(TextChangeEvent event);

	public Table getTable() {
		return table;
	}

}
