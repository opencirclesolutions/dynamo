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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vaadin.addons.lazyquerycontainer.CompositeItem;
import org.vaadin.addons.lazyquerycontainer.NestingBeanItem;
import org.vaadin.dialogs.ConfirmDialog;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.container.pivot.PivotItem;
import com.ocs.dynamo.ui.converter.BigDecimalConverter;
import com.ocs.dynamo.ui.converter.ConverterFactory;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.data.util.converter.StringToLongConverter;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;

/**
 * Utility class for Vaadin-related functionality
 * 
 * @author bas.rutten
 */
public final class VaadinUtils {

	/**
	 * Add attribute from attributemodel to container when not in container already.
	 *
	 * @param container
	 * @param attributeModel
	 */
	public static void addPropertyIdToContainer(Container container, AttributeModel attributeModel) {
		if (container != null && attributeModel != null && attributeModel.isVisibleInTable()
				&& !container.getContainerPropertyIds().contains(attributeModel.getPath())) {
			container.addContainerProperty(attributeModel.getPath(), attributeModel.getType(),
					attributeModel.getDefaultValue());
		}
	}

    /**
	 * Add attribute from entitymodel to container when not in container already.
	 *
	 * @param container
	 * @param entityModel
	 * @param attributeName
	 */
	public static <T> void addPropertyIdToContainer(Container container, EntityModel<T> entityModel,
			String attributeName) {
		if (entityModel != null) {
			AttributeModel attributeModel = entityModel.getAttributeModel(attributeName);
			addPropertyIdToContainer(container, attributeModel);
		}
	}

	/**
	 * Check if all editable fields that are contained in a (fixed, non-lazy) table are valid. This method is needed
	 * because simply calling table.isValid() will not take into account any editable components within the table
	 *
	 * @param table
	 *            the table
	 * @return
	 */
    public static boolean allFixedTableFieldsValid(Table table) {
        boolean allValid = true;
        Iterator<Component> component = table.iterator();
        while (component.hasNext() && allValid) {
            Component next = component.next();
            if (next instanceof Field) {
                allValid &= ((Field<?>) next).isValid();
            }
        }
        return allValid;
    }

	/**
	 * Check if all editable fields that are contained in a (fixed, non-lazy) table
	 * are valid. This method is needed because simply calling table.isValid() will
	 * not take into account any editable components within the table
	 *
	 * @param table
	 *            the table
	 * @return
	 */

	/**
	 * Converts a BigDecimal value to a String
	 *
	 * @param percentage
	 *            whether the value represents a percentage
	 * @param useGrouping
	 *            whether to use a thousand grouping
	 * @param value
	 *            the value
	 * @param locale
	 *            the locale to use
	 * @return
	 */
	public static String bigDecimalToString(boolean percentage, boolean useGrouping, BigDecimal value) {
		return bigDecimalToString(false, percentage, useGrouping, SystemPropertyUtils.getDefaultDecimalPrecision(),
				value, getLocale());
	}

	/**
	 *
	 * Converts a BigDecimal value to a String (shortcut for values that are not
	 * currency and not percentage)
	 *
	 * @param percentage
	 *            whether the value represents a percentage
	 * @param useGrouping
	 *            whether to use a thousand grouping
	 * @param value
	 *            the value
	 * @return
	 */
	public static String bigDecimalToString(boolean currency, boolean percentage, boolean useGrouping,
			BigDecimal value) {
		return bigDecimalToString(currency, percentage, useGrouping, SystemPropertyUtils.getDefaultDecimalPrecision(),
				value, getLocale());
	}

	/**
	 * * Converts a BigDecimal value to a String
	 *
	 * @param currency
	 *            whether the value represents a currency
	 * @param percentage
	 *            whether the value represents a percentage
	 * @param useGrouping
	 *            whether to use a thousand grouping
	 * @param value
	 *            the value
	 * @param locale
	 *            the locale to use
	 * @return
	 */
	public static String bigDecimalToString(boolean currency, boolean percentage, boolean useGrouping, int precision,
			BigDecimal value, Locale locale) {
		return bigDecimalToString(currency, percentage, useGrouping, precision, value, locale, getCurrencySymbol());
	}

	/**
	 *
	 * @param currency
	 * @param percentage
	 * @param useGrouping
	 * @param precision
	 * @param value
	 * @param locale
	 * @param currencySymbol
	 * @return
	 */
	public static String bigDecimalToString(boolean currency, boolean percentage, boolean useGrouping, int precision,
			BigDecimal value, Locale locale, String currencySymbol) {
		BigDecimalConverter converter = ConverterFactory.createBigDecimalConverter(currency, percentage, useGrouping,
				precision, currencySymbol);
		return converter.convertToPresentation(value, String.class, locale);
	}

	/**
	 * Enables copy/paste behavior for Internet Explorer
	 */
	public static void enableCopyPaste() {
		String js = "$(document).ready(function() {" + "  $('input').off('paste'); "
				+ "  $('input').on('paste', function(e) { " + "    if (clipboardData) { "
				+ "      setTimeout(function() {" + "        var pasted = clipboardData.getData('text');"
				+ "        pasted = pasted.replace(/(\\r\\n|\\r|\\n)/g,'\t');"
				+ "        clipboardData.setData('text', pasted); $(e.target).val(pasted);} " + "      , 100);  "
				+ "    }" + "  }) " + "});";
		Page.getCurrent().getJavaScript().execute(js);
	}

	/**
	 * Returns the currency symbol to be used - by default this is looked up from
	 * the session, with a fallback to the system property "default.currency.symbol"
	 *
	 * @return
	 */
	public static String getCurrencySymbol() {
		String cs = SystemPropertyUtils.getDefaultCurrencySymbol();

		VaadinSession vs = VaadinSession.getCurrent();
		if (vs != null && vs.getAttribute(DynamoConstants.CURRENCY_SYMBOL) != null) {
			cs = (String) vs.getAttribute(DynamoConstants.CURRENCY_SYMBOL);
		}
		return cs;
	}

	/**
	 * Retrieves an entity with a certain ID from a container
	 *
	 * @param container
	 *            the container
	 * @param id
	 *            the ID of the entity
	 * @return
	 */
	public static <ID, T> T getEntityFromContainer(Container container, ID id) {
		Object obj = container.getItem(id);
		return getEntityFromItem(obj);
	}

	/**
	 * Extract an entity from a Vaadin Item object
	 *
	 * @param obj
	 *            the Item object (can be either a CompositeItem or a BeanItem)
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getEntityFromItem(Object obj) {
		if (obj instanceof CompositeItem) {
			CompositeItem item = (CompositeItem) obj;
			NestingBeanItem<T> bi = (NestingBeanItem<T>) item.getItem("bean");
			return bi.getBean();
		} else if (obj instanceof BeanItem) {
			BeanItem<T> bi = (BeanItem<T>) obj;
			return bi.getBean();
		}
		return null;
	}

	/**
	 * Extracts an entity from a PivotItem
	 *
	 * @param item
	 *            the PivotItem
	 * @param column
	 *            the name of the column to extract
	 * @return
	 */
	public static <T> T getEntityFromPivotItem(Item item, String column) {
		PivotItem pi = (PivotItem) item;
		Item nested = pi.getColumn(column);
		return getEntityFromItem(nested);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Component> T getFirstChildOfClass(Layout layout, Class<T> clazz) {
		Iterator<Component> it = layout.iterator();
		while (it.hasNext()) {
			Component c = it.next();
			if (clazz.isAssignableFrom(c.getClass())) {
				return (T) c;
			}
		}
		return null;
	}

	/**
	 * Extracts the first value from a map entry that contains a collection
	 *
	 * @param map
	 *            the map
	 * @param key
	 *            the map key
	 * @return
	 */
	public static String getFirstValueFromCollection(Map<String, Object> map, String key) {
		if (map != null) {
			Collection<?> col = (Collection<?>) map.get(key);
			if (col != null) {
				return col.iterator().next().toString();
			}
		}
		return null;
	}

	/**
	 * Returns the locale associated with the current Vaadin session
	 *
	 * @return
	 */
	public static Locale getLocale() {
		if (VaadinSession.getCurrent() != null) {
			return VaadinSession.getCurrent().getLocale();
		}
		return new Locale(SystemPropertyUtils.getDefaultLocale());
	}

	/**
	 * Returns the locale to be used inside date picker components. This checks for
	 * the presence of the DynamoConstants.DATE_LOCALE setting on the session. If
	 * this is not set, it falls back to the normal locale mechanism
	 * 
	 * @return
	 */
	public static Locale getDateLocale() {
		if (VaadinSession.getCurrent() != null
				&& VaadinSession.getCurrent().getAttribute(DynamoConstants.DATE_LOCALE) != null) {
			return new Locale((String) VaadinSession.getCurrent().getAttribute(DynamoConstants.DATE_LOCALE));
		}
		return getLocale();
	}

	/**
	 * Reads the desired locale for formatting date fields from the system
	 * properties and stores it in the session
	 */
	public static void storeDateLocale() {
		VaadinSession.getCurrent().setAttribute(DynamoConstants.DATE_LOCALE,
				SystemPropertyUtils.getDefaultDateLocale());
	}

	/**
	 * Stores the default locale configured in the system properties in the Vaadin
	 * session
	 */
	public static void storeLocale() {
		VaadinSession.getCurrent().setLocale(new Locale(SystemPropertyUtils.getDefaultLocale()));
	}

	/**
	 * Returns the tab index (zero based) of the tab with the specified caption
	 * 
	 * @param tabs
	 *            the tab sheet
	 * @param caption
	 *            the caption
	 * @return
	 */
	public static int getTabIndex(TabSheet tabs, String caption) {
		int index = 0;
		for (int i = 0; i < tabs.getComponentCount(); i++) {
			Tab t = tabs.getTab(i);
			if (t.getCaption().equals(caption)) {
				index = i;
				break;
			}
		}
		return index;
	}

	/**
	 * Returns the first parent component of the specified component that is a
	 * subclass of the specified class
	 *
	 * @param component
	 *            the component
	 * @param clazz
	 *            the class
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getParentOfClass(Component component, Class<T> clazz) {
		while (component.getParent() != null) {
			component = component.getParent();
			if (clazz.isAssignableFrom(component.getClass())) {
				return (T) component;
			}
		}
		return null;
	}

	/**
	 * Returns the first value from a session attribute that contains a map
	 *
	 * @param attributeName
	 *            the name of the attribute that holds the map
	 * @param key
	 *            the map key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String getSessionAttributeValueFromMap(String attributeName, String key) {
		Map<String, Object> map = (Map<String, Object>) VaadinSession.getCurrent().getSession()
				.getAttribute(attributeName);
		return getFirstValueFromCollection(map, key);
	}

	/**
	 * Returns the user's time zone (based on the offset). The result of this method
	 * can be passed to a date converter or simple date format to make sure the date
	 * is properly formatted
	 *
	 * @return
	 */
	public static TimeZone getTimeZone(UI ui) {
		if (ui != null) {
			int offset = ui.getPage().getWebBrowser().getRawTimezoneOffset();
			boolean dst = ui.getPage().getWebBrowser().isDSTInEffect();

			String[] zones = TimeZone.getAvailableIDs(offset);
			if (zones != null && zones.length > 0) {
				for (String zone : zones) {
					TimeZone tz = TimeZone.getTimeZone(zone);
					// look for the first time zone with the right offset that
					// also
					// has a matching DST setting
					if (tz.inDaylightTime(new Date()) == dst) {
						return tz;
					}
				}
			}
		}
		return TimeZone.getTimeZone("CET");
	}

	/**
	 * Converts an Integer to a String, using the Vaadin converters
	 *
	 * @param grouping
	 *            indicates whether grouping separators must be used
	 * @param value
	 *            the value to convert
	 * @return
	 */
	public static String integerToString(boolean grouping, boolean percentage, Integer value) {
		return integerToString(grouping, percentage, value, getLocale());
	}

	/**
	 * Converts an Integer to a String, using the Vaadin converters
	 *
	 * @param grouping
	 *            indicates whether grouping separators must be used
	 * @param value
	 *            the value to convert
	 * @param locale
	 *            the locale
	 * @return
	 */
	public static String integerToString(boolean grouping, boolean percentage, Integer value, Locale locale) {
		StringToIntegerConverter converter = ConverterFactory.createIntegerConverter(grouping, percentage);
		return converter.convertToPresentation(value, String.class, locale);
	}

	/**
	 * Executes javascript loaded into a page by code
	 *
	 * @param id
	 *            the ID of the element that will hold the contents
	 * @param originalInput
	 *            the HTML contents of the report
	 * @param requireExternalScript
	 *            indicates if there is a external library required
	 * @param execute
	 *            indicates if the relevant external library is already loaded
	 */
	public static void loadScript(String id, String originalInput, boolean requireExternalScript, boolean execute) {
		// replace whitespace and dubious constructions (including an ESCAPED newline,
		// see the 4
		// backspaces)
		String input;
		if (!requireExternalScript) {

			input = originalInput.replaceAll("'", "\"").replaceAll("\r\n", "\\\\r\\\\n").replaceAll("\n", "\\\\n")
					.replaceAll("\\s+", " ").trim();

			// no need to load an external script first
			Page.getCurrent().getJavaScript().execute("$('#" + id + "').html('" + input + "');");
		} else {
			input = originalInput.replaceAll("'", "\"").replaceAll("\r\n", "").replaceAll("\n", "")
					.replaceAll("\\s+", " ").replaceAll("\\\\n", "-").trim();

			// find the first script tag that points to an external location
			Pattern pattern = Pattern.compile("<script.*?src=(.*?)></script>");
			Matcher matcher = pattern.matcher(input);

			if (matcher.find()) {
				String script = matcher.group(0);

				// replace HTTP by HTTPS to prevent mixed mode warnings
				String url = matcher.group(1).replaceAll("\"", "").replaceAll("http://", "https://");

				// remove the external script tag
				input = input.replace(script, "");

				if (!execute) {
					// execute external library and load the rest of the contents in the callback
					Page.getCurrent().getJavaScript()
							.execute("$.getScript('" + url + "', function(){$('#" + id + "').html('" + input + "');})");
				} else {
					Page.getCurrent().getJavaScript().execute("$('#" + id + "').html('" + input + "');");
				}
			}
		}
	}

	/**
	 * Converts an Long to a String, using the Vaadin converters
	 *
	 * @param grouping
	 *            indicates whether grouping separators must be used
	 * @param value
	 *            the value to convert
	 * @return
	 */
	public static String longToString(boolean grouping, boolean percentage, Long value) {
		return longToString(grouping, percentage, value, getLocale());
	}

	/**
	 * Converts an Long to a String, using the Vaadin converters
	 *
	 * @param grouping
	 *            indicates whether grouping separators must be used
	 * @param value
	 *            the value to convert
	 * @param locale
	 *            the locale
	 * @return
	 */
	public static String longToString(boolean grouping, boolean percentage, Long value, Locale locale) {
		StringToLongConverter converter = ConverterFactory.createLongConverter(grouping, percentage);
		return converter.convertToPresentation(value, String.class, locale);
	}

	@SuppressWarnings("unchecked")
	public static <T> String numberToString(AttributeModel attributeModel, Class<?> type, T value, boolean grouping,
			Locale locale) {
		Converter<String, T> cv = (Converter<String, T>) ConverterFactory.createConverterFor(type, attributeModel,
				grouping);
		if (cv != null) {
			return cv.convertToPresentation(value, String.class, locale);
		}
		return null;
	}

	public static void removeValidatorsOfType(Field<?> field, Class<?> validatorClass) {
		for (Validator v : field.getValidators()) {
			if (v.getClass().equals(validatorClass)) {
				field.removeValidator(v);
			}
		}
	}

	/**
	 * Displays a confirmation dialog
	 *
	 * @param messageService
	 * @param question
	 *            the question to be displayed in the dialog
	 * @param whenConfirmed
	 *            the code to execute when the user confirms the dialog
	 */
	public static void showConfirmDialog(MessageService messageService, String question, final Runnable whenConfirmed) {
		if (UI.getCurrent() != null) {
			ConfirmDialog.show(UI.getCurrent(), messageService.getMessage("ocs.confirm", getLocale()), question,
					messageService.getMessage("ocs.yes", getLocale()), messageService.getMessage("ocs.no", getLocale()),
					dialog -> {
						if (dialog.isConfirmed()) {
							whenConfirmed.run();
						}
					});
		} else {
			whenConfirmed.run();
		}
	}

    /**
     * Displays a confirmation dialog
     *
     * @param messageService
     * @param question
     *            the question to be displayed in the dialog
     * @param whenConfirmed
     *            the code to execute when the user confirms the dialog
     * @param whenCanceled
     *            the code to execute when the user cancels the dialog
     */
    public static void showConfirmDialog(MessageService messageService, String question, final Runnable whenConfirmed, final Runnable whenCanceled) {
        if (UI.getCurrent() != null) {
            ConfirmDialog.show(UI.getCurrent(), messageService.getMessage("ocs.confirm", getLocale()), question,
                    messageService.getMessage("ocs.yes", getLocale()), messageService.getMessage("ocs.no", getLocale()),
                    dialog -> {
                        if (dialog.isConfirmed()) {
                            whenConfirmed.run();
                        } else {
                            whenCanceled.run();
                        }
                    });
        } else {
            whenConfirmed.run();
        }
    }

	/**
	 * Converts a string to a big decimal
	 *
	 * @param percentage
	 *            is it a percentage value
	 * @param useGrouping
	 *            use thousands grouping
	 * @param currency
	 *            is it a currency value
	 * @param value
	 *            the value
	 * @param locale
	 *            the locale
	 * @return
	 */
	public static BigDecimal stringToBigDecimal(boolean percentage, boolean useGrouping, boolean currency,
			int precision, String value, Locale locale) {
		BigDecimalConverter converter = ConverterFactory.createBigDecimalConverter(currency, percentage, useGrouping,
				precision, VaadinUtils.getCurrencySymbol());
		return converter.convertToModel(value, BigDecimal.class, locale);
	}

	/**
	 * Converts a string to a BigDecimal using the built in Vaadin converter
	 *
	 * @param percentage
	 *            the percentage
	 * @param value
	 *            the value to be converted
	 * @return
	 */
	public static BigDecimal stringToBigDecimal(boolean percentage, boolean useGrouping, boolean currency,
			String value) {
		return stringToBigDecimal(percentage, useGrouping, currency, SystemPropertyUtils.getDefaultDecimalPrecision(),
				value, getLocale());
	}

	/**
	 * Converts a String to an Integer
	 *
	 * @param grouping
	 *            whether to include a thousands grouping separator
	 * @param value
	 *            the String to convert
	 * @return
	 */
	public static Integer stringToInteger(boolean grouping, String value) {
		return stringToInteger(grouping, value, getLocale());
	}

	/**
	 * Converts a String to an Integer
	 *
	 * @param grouping
	 *            indicates whether the string could contain grouping separators
	 * @param value
	 *            the String to convert
	 * @param locale
	 *            the locale to use for the conversion
	 * @return
	 */
	public static Integer stringToInteger(boolean grouping, String value, Locale locale) {
		StringToIntegerConverter converter = ConverterFactory.createIntegerConverter(grouping, false);
		return converter.convertToModel(value, Integer.class, locale);
	}

	/**
	 * Converts a String to a Long
	 *
	 * @param grouping
	 *            indicates if a thousands separator is used
	 * @param value
	 *            the String to convert
	 * @return
	 */
	public static Long stringToLong(boolean grouping, String value) {
		StringToLongConverter converter = ConverterFactory.createLongConverter(grouping, false);
		return converter.convertToModel(value, Long.class, getLocale());
	}

	/**
	 * Converts a String to a Long
	 *
	 * @param grouping
	 *            indicates if a thousands separator is used
	 * @param value
	 *            the String to convert to convert
	 * @param locale
	 *            the locale
	 * @return
	 */
	public static Long stringToLong(boolean grouping, String value, Locale locale) {
		StringToLongConverter converter = ConverterFactory.createLongConverter(grouping, false);
		return converter.convertToModel(value, Long.class, locale);
	}

	/**
	 * Wraps the provided component inside a form layout
	 *
	 * @param c
	 * @return
	 */
	public static FormLayout wrapInFormLayout(Component c) {
		FormLayout fl = new FormLayout();
		fl.setMargin(false);
		fl.addComponent(c);
		return fl;
	}

	private VaadinUtils() {
		// hidden constructor
	}
}
