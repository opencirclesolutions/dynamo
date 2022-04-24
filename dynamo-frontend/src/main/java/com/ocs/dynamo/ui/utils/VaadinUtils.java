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
import java.time.ZoneId;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.ui.component.CustomEntityField;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.component.EntityComboBox;
import com.ocs.dynamo.ui.component.QuickAddEntityField;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.converter.BigDecimalConverter;
import com.ocs.dynamo.ui.converter.ConverterFactory;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.converter.StringToLongConverter;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.server.VaadinSession;

/**
 * Utility class for Vaadin-related functionality
 * 
 * @author bas.rutten
 */
public final class VaadinUtils {

	/**
	 * Converts a BigDecimal value to a String
	 *
	 * @param percentage  whether the value represents a percentage
	 * @param useGrouping whether to use a thousand grouping
	 * @param value       the value
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
	 * @param currency    whether the value represents a currency
	 * @param percentage  whether the value represents a percentage
	 * @param useGrouping whether to use a thousand grouping
	 * @param value       the value
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
	 * @param currency    whether the value represents a currency
	 * @param percentage  whether the value represents a percentage
	 * @param useGrouping whether to use a thousand grouping
	 * @param value       the value
	 * @param locale      the locale to use
	 * @return
	 */
	public static String bigDecimalToString(boolean currency, boolean percentage, boolean useGrouping, int precision,
			BigDecimal value, Locale locale) {
		return bigDecimalToString(currency, percentage, useGrouping, precision, value, getCurrencySymbol(), locale);
	}

	/**
	 * Converts a BigDecimal to a String
	 * 
	 * @param currency       whether to include a currency symbol
	 * @param percentage     whether to include a percentage sign
	 * @param useGrouping    whether to use a thousands grouping separator
	 * @param precision      the desired precision
	 * @param value          the value to convert
	 * @param locale         the locale to use
	 * @param currencySymbol the currency symbol to use
	 * @return
	 */
	public static String bigDecimalToString(boolean currency, boolean percentage, boolean useGrouping, int precision,
			BigDecimal value, String currencySymbol, Locale locale) {
		return NumberUtils.bigDecimalToString(currency, percentage, useGrouping, precision, value, locale,
				currencySymbol);
	}

	public static String bigDecimalToString(AttributeModel am, BigDecimal value) {
		return bigDecimalToString(am.isCurrency(), am.isPercentage(), am.useThousandsGroupingInViewMode(),
				am.getPrecision(), value, am.getCurrencySymbol(), getDateLocale());
	}

	/**
	 * Creates an image based on an image named stored in the "frontend/images"
	 * directory
	 * 
	 * @param imageName the name of the file
	 * @return
	 */
	public static Image createImage(String imageName) {
		String resolvedImage = VaadinServletService.getCurrent().resolveResource("frontend://images/" + imageName,
				VaadinSession.getCurrent().getBrowser());
		return new Image(resolvedImage, "");
	}

	/**
	 * Creates an overflow layout of a certain height
	 * 
	 * @param height
	 * @return
	 */
	public static VerticalLayout createOverflowLayout(String height) {
		VerticalLayout restricted = new DefaultVerticalLayout(false, false);
		restricted.getStyle().set("overflow", "auto");
		restricted.setHeight(height);
		return restricted;
	}

	/**
	 * Converts a double to a String
	 * 
	 * @param currency    whether to include a currency symbol
	 * @param percentage  whether to include a percentage sign
	 * @param useGrouping whether to use a thousands grouping separator
	 * @param precision   the desired precision
	 * @param value       the value to convert
	 * @param locale      the locale to use
	 * @return
	 */
	public static String doubleToString(boolean currency, boolean percentage, boolean useGrouping, int precision,
			Double value, Locale locale) {
		return doubleToString(currency, percentage, useGrouping, precision, value, locale, getCurrencySymbol());
	}

	/**
	 * Converts a double to a String
	 * 
	 * @param currency       whether to include a currency symbol
	 * @param percentage     whether to include a percentage sign
	 * @param useGrouping    whether to use a thousands grouping separator
	 * @param precision      the desired precision
	 * @param value          the value to convert
	 * @param locale         the locale to use
	 * @param currencySymbol the currency symbol to use
	 * @return
	 */
	public static String doubleToString(boolean currency, boolean percentage, boolean useGrouping, int precision,
			Double value, Locale locale, String currencySymbol) {
		return NumberUtils.doubleToString(currency, percentage, useGrouping, precision, value, locale, currencySymbol);
	}

	/**
	 * Returns the currency symbol to be used - by default this is looked up from
	 * the session, with a fallback to the system property "default.currency.symbol"
	 *
	 * @return
	 */
	public static String getCurrencySymbol() {
		String currencySymbol = SystemPropertyUtils.getDefaultCurrencySymbol();

		VaadinSession vs = VaadinSession.getCurrent();
		if (vs != null && vs.getAttribute(DynamoConstants.CURRENCY_SYMBOL) != null) {
			currencySymbol = (String) vs.getAttribute(DynamoConstants.CURRENCY_SYMBOL);
		}
		return currencySymbol;
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
			return (Locale) VaadinSession.getCurrent().getAttribute(DynamoConstants.DATE_LOCALE);
		}
		return new Locale(SystemPropertyUtils.getDefaultDateLocale());
	}

	/**
	 * Returns the first child of the provide component that is of the specified
	 * class (or is a subclass of the specified class)
	 * 
	 * @param component
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getFirstChildOfClass(Component component, Class<T> clazz) {
		return (T) component.getChildren().filter(c -> clazz.isAssignableFrom(c.getClass())).findFirst().orElse(null);
	}

	/**
	 * Extracts the first value from a map entry that contains a collection
	 *
	 * @param map the map
	 * @param key the map key
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

	public static Object getFromSession(String name) {
		VaadinSession current = VaadinSession.getCurrent();
		if (current != null) {
			return current.getAttribute(name);
		}
		return null;
	}

	/**
	 * Returns the locale associated with the current Vaadin session
	 *
	 * @return
	 */
	public static Locale getLocale() {
		if (VaadinSession.getCurrent() != null && VaadinSession.getCurrent().getLocale() != null) {
			return VaadinSession.getCurrent().getLocale();
		}
		return new Locale(SystemPropertyUtils.getDefaultLocale());
	}

	/**
	 * Returns the first parent component of the specified component that is a
	 * subclass of the specified class
	 *
	 * @param component the component
	 * @param clazz     the class
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getParentOfClass(Component component, Class<T> clazz) {
		while (component.getParent().isPresent()) {
			component = component.getParent().get();
			if (clazz.isAssignableFrom(component.getClass())) {
				return (T) component;
			}
		}
		return null;
	}

	/**
	 * Returns the first value from a session attribute that contains a map
	 *
	 * @param attributeName the name of the attribute that holds the map
	 * @param key           the map key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String getSessionAttributeValueFromMap(String attributeName, String key) {
		Map<String, Object> map = (Map<String, Object>) VaadinSession.getCurrent().getSession()
				.getAttribute(attributeName);
		return getFirstValueFromCollection(map, key);
	}

	/**
	 * Returns the time zone ID that is set for the session
	 * 
	 * @return
	 */
	public static ZoneId getTimeZoneId() {
		if (VaadinSession.getCurrent() != null && VaadinSession.getCurrent().getAttribute("zoneId") != null) {
			return (ZoneId) VaadinSession.getCurrent().getAttribute("zoneId");
		}
		return ZoneId.systemDefault();
	}

	/**
	 * Converts an Integer to a String, using the Vaadin converters
	 *
	 * @param grouping indicates whether grouping separators must be used
	 * @param value    the value to convert
	 * @return
	 */
	public static String integerToString(boolean grouping, boolean percentage, Integer value) {
		return integerToString(grouping, percentage, value, getLocale());
	}

	/**
	 * Converts an Integer to a String, using the Vaadin converters
	 *
	 * @param grouping indicates whether grouping separators must be used
	 * @param value    the value to convert
	 * @param locale   the locale
	 * @return
	 */
	public static String integerToString(boolean grouping, boolean percentage, Integer value, Locale locale) {
		return NumberUtils.integerToString(grouping, percentage, value, locale);
	}

	/**
	 * Converts an Long to a String, using the Vaadin converters
	 *
	 * @param grouping indicates whether grouping separators must be used
	 * @param value    the value to convert
	 * @return
	 */
	public static String longToString(boolean grouping, boolean percentage, Long value) {
		return longToString(grouping, percentage, value, getLocale());
	}

	/**
	 * Converts an Long to a String, using the Vaadin converters
	 *
	 * @param grouping indicates whether grouping separators must be used
	 * @param value    the value to convert
	 * @param locale   the locale
	 * @return
	 */
	public static String longToString(boolean grouping, boolean percentage, Long value, Locale locale) {
		return NumberUtils.longToString(grouping, percentage, value, locale);
	}

	/**
	 * Converts a number to a String
	 * 
	 * @param am
	 * @param type
	 * @param value
	 * @param grouping
	 * @param locale
	 * @param currencySymbol
	 * @return
	 */
	public static <T> String numberToString(AttributeModel am, T value, boolean grouping, Locale locale,
			String currencySymbol) {
		return NumberUtils.numberToString(am, value, grouping, locale, currencySymbol);
	}

	/**
	 * Removes an object from the Vaadin session
	 * 
	 * @param name the name of the attribute to clear
	 */
	public static void removeFromSession(String name) {
		VaadinSession current = VaadinSession.getCurrent();
		if (current != null) {
			current.setAttribute(name, null);
		}
	}

	/**
	 * Sets the label on the provided field
	 * 
	 * @param component the field
	 * @param label     the text of the label
	 */
	public static void setLabel(Component component, String label) {
		if (component instanceof TextField) {
			((TextField) component).setLabel(label);
		} else if (component instanceof IntegerField) {
			((IntegerField) component).setLabel(label);
		} else if (component instanceof Checkbox) {
			((Checkbox) component).setLabel(label);
		} else if (component instanceof CustomField) {
			((CustomField<?>) component).setLabel(label);
		} else if (component instanceof ComboBox) {
			((ComboBox<?>) component).setLabel(label);
		} else if (component instanceof TextArea) {
			((TextArea) component).setLabel(label);
		} else if (component instanceof DatePicker) {
			((DatePicker) component).setLabel(label);
		} else if (component instanceof TimePicker) {
			((TimePicker) component).setLabel(label);
		} else if (component instanceof EmailField) {
			((EmailField) component).setLabel(label);
		} else if (component instanceof DateTimePicker) {
			((DateTimePicker) component).setLabel(label);
		} else if (component instanceof PasswordField) {
			((PasswordField) component).setLabel(label);
		}
	}

	/**
	 * Set the "clear button visible" setting for a component
	 * 
	 * @param component the component
	 * @param visible   the desired visibility
	 */
	public static void setClearButtonVisible(Component component, boolean visible) {
		if (component instanceof TextField) {
			((TextField) component).setClearButtonVisible(visible);
		} else if (component instanceof IntegerField) {
			((IntegerField) component).setClearButtonVisible(visible);
		} else if (component instanceof ComboBox) {
			((ComboBox<?>) component).setClearButtonVisible(visible);
		} else if (component instanceof TextArea) {
			((TextArea) component).setClearButtonVisible(visible);
		} else if (component instanceof DatePicker) {
			((DatePicker) component).setClearButtonVisible(visible);
		} else if (component instanceof TimePicker) {
			((TimePicker) component).setClearButtonVisible(visible);
		} else if (component instanceof EmailField) {
			((EmailField) component).setClearButtonVisible(visible);
		} else if (component instanceof QuickAddEntityField) {
			((QuickAddEntityField<?, ?, ?>) component).setClearButtonVisible(visible);
		} else if (component instanceof PasswordField) {
			((PasswordField) component).setClearButtonVisible(visible);
		}
	}

	/**
	 * Sets the placeholder for the specified component
	 * 
	 * @param component   the component
	 * @param placeHolder the placeholder to set
	 */
	public static void setPlaceHolder(Component component, String placeHolder) {
		if (component instanceof TextField) {
			TextField textField = (TextField) component;
			textField.setPlaceholder(placeHolder);
		} else if (component instanceof DatePicker) {
			// set a separate format for a date field
			DatePicker dateField = (DatePicker) component;
			dateField.setPlaceholder(placeHolder);
		} else if (component instanceof TimePicker) {
			((TimePicker) component).setPlaceholder(placeHolder);
		} else if (component instanceof EntityComboBox) {
			((EntityComboBox<?, ?>) component).setPlaceholder(placeHolder);
		} else if (component instanceof TextArea) {
			((TextArea) component).setPlaceholder(placeHolder);
		} else if (component instanceof CustomEntityField) {
			((CustomEntityField<?, ?, ?>) component).setPlaceholder(placeHolder);
		} else if (component instanceof URLField) {
			((URLField) component).setPlaceholder(placeHolder);
		} else if (component instanceof ComboBox) {
			((ComboBox<?>) component).setPlaceholder(placeHolder);
		} else if (component instanceof EmailField) {
			((EmailField) component).setPlaceholder(placeHolder);
		}
	}

	/**
	 * Sets a tool tip on a component
	 * 
	 * @param field   the component
	 * @param tooltip the tool tip to set
	 */
	public static void setTooltip(Component component, String tooltip) {
		component.getElement().setProperty("title", tooltip);
	}

	/**
	 * Displays a confirmation dialog that runs code when confirmed
	 *
	 * @param question      the question to be displayed in the dialog
	 * @param whenConfirmed the code to execute when the user confirms the dialog
	 */
	public static void showConfirmDialog(String question, Runnable whenConfirmed) {
		VaadinUtils.showConfirmDialog(question, whenConfirmed, null);
	}

	/**
	 * Displays a confirmation dialog that runs code both when confirmed and
	 * canceled
	 *
	 * @param question      the question to be displayed in the dialog
	 * @param whenConfirmed the code to execute when the user confirms the dialog
	 * @param whenCanceled  the code to execute when the user cancels the dialog
	 */
	public static void showConfirmDialog(String question, Runnable whenConfirmed, Runnable whenCanceled) {
		if (UI.getCurrent() != null) {
			com.ocs.dynamo.ui.composite.dialog.ConfirmDialog dialog = new com.ocs.dynamo.ui.composite.dialog.ConfirmDialog(
					question, whenConfirmed, whenCanceled);
			dialog.buildAndOpen();
		} else {
			whenConfirmed.run();
		}
	}

	/**
	 * Shows an error message
	 * 
	 * @param message the message to show
	 */
	public static void showErrorNotification(String message) {
		showNotification(message, Position.MIDDLE, NotificationVariant.LUMO_ERROR);
	}

	/**
	 * Shows a notification message
	 * 
	 * @param message  the message
	 * @param position the desired position
	 * @param variant  the variant (indicates the style, e.g. error or warning)
	 */
	public static void showNotification(String message, Position position, NotificationVariant variant) {
		Notification.show(message, SystemPropertyUtils.getDefaultMessageDisplayTime(), position)
				.addThemeVariants(variant);
	}

	/**
	 * Shows a tray message
	 * 
	 * @param message the message to show
	 */
	public static void showTrayNotification(String message) {
		showNotification(message, Position.BOTTOM_END, NotificationVariant.LUMO_SUCCESS);
	}

	/**
	 * Stores the desired date locale in the session
	 * 
	 * @param locale the locale
	 */
	public static void storeDateLocale(Locale locale) {
		VaadinSession.getCurrent().setAttribute(DynamoConstants.DATE_LOCALE, locale);
	}

	/**
	 * Stores an object in the Vaadin session
	 * 
	 * @param name  the name under which to store
	 * @param value the value to store
	 */
	public static void storeInSession(String name, Object value) {
		VaadinSession current = VaadinSession.getCurrent();
		if (current != null) {
			current.setAttribute(name, value);
		}
	}

	/**
	 * Stores the default locale configured in the system properties in the Vaadin
	 * session
	 */
	public static void storeLocale(Locale locale) {
		VaadinSession.getCurrent().setLocale(locale);
	}

	/**
	 * 
	 * @param zoneId
	 */
	public static void storeTimeZone(ZoneId zoneId) {
		VaadinSession.getCurrent().setAttribute("zoneId", zoneId);
	}

	/**
	 * Converts a String to a BigDecimal
	 * 
	 * @param percentage  whether a percentage sign might be included
	 * @param useGrouping whether a thousands grouping separator might be included
	 * @param currency    whether a currency symbol might be included
	 * @param precision   the precision
	 * @param value       the String value to convert
	 * @param locale      the locale to use
	 * @return
	 */
	public static BigDecimal stringToBigDecimal(boolean percentage, boolean useGrouping, boolean currency,
			int precision, String value, Locale locale) {
		BigDecimalConverter converter = ConverterFactory.createBigDecimalConverter(currency, percentage, useGrouping,
				precision, VaadinUtils.getCurrencySymbol());
		return converter.convertToModel(value, new ValueContext(locale)).getOrThrow(r -> new OCSRuntimeException());
	}

	/**
	 * Converts a String to a BigDecimal
	 * 
	 * @param percentage  whether a percentage sign might be included
	 * @param useGrouping whether a thousands grouping separator might be included
	 * @param currency    whether a currency symbol might be included
	 * @param value       the value to include
	 * @return
	 */
	public static BigDecimal stringToBigDecimal(boolean percentage, boolean useGrouping, boolean currency,
			String value) {
		return stringToBigDecimal(percentage, useGrouping, currency, SystemPropertyUtils.getDefaultDecimalPrecision(),
				value, getLocale());
	}

	/**
	 * Converts a String to a Double
	 * 
	 * @param percentage  whether a percentage sign might be included
	 * @param useGrouping whether a thousands grouping separator might be included
	 * @param currency    whether a currency symbol might be included
	 * @param precision   the precision
	 * @param value       the String value to convert
	 * @param locale      the locale to use
	 * @return
	 */
	public static Double stringToDouble(boolean percentage, boolean useGrouping, boolean currency, int precision,
			String value, Locale locale) {
		StringToDoubleConverter converter = ConverterFactory.createDoubleConverter(currency, percentage, useGrouping,
				precision, VaadinUtils.getCurrencySymbol());
		return converter.convertToModel(value, new ValueContext(locale)).getOrThrow(r -> new OCSRuntimeException());
	}

	/**
	 * Converts a String to an Integer
	 *
	 * @param grouping whether to include a thousands grouping separator
	 * @param value    the String to convert
	 * @return
	 */
	public static Integer stringToInteger(boolean grouping, String value) {
		return stringToInteger(grouping, value, getLocale());
	}

	/**
	 * Converts a String to an Integer
	 *
	 * @param grouping indicates whether the string could contain grouping
	 *                 separators
	 * @param value    the String to convert
	 * @param locale   the locale to use for the conversion
	 * @return
	 */
	public static Integer stringToInteger(boolean grouping, String value, Locale locale) {
		StringToIntegerConverter converter = ConverterFactory.createIntegerConverter(grouping, false);
		return converter.convertToModel(value, new ValueContext(locale)).getOrThrow(r -> new OCSRuntimeException());
	}

	/**
	 * Converts a String to a Long
	 *
	 * @param grouping indicates if a thousands separator is used
	 * @param value    the String to convert
	 * @return
	 */
	public static Long stringToLong(boolean grouping, String value) {
		return stringToLong(grouping, value, getLocale());
	}

	/**
	 * Converts a String to a Long
	 *
	 * @param grouping indicates if a thousands separator is used
	 * @param value    the String to convert to convert
	 * @param locale   the locale to use
	 * @return
	 */
	public static Long stringToLong(boolean grouping, String value, Locale locale) {
		StringToLongConverter converter = ConverterFactory.createLongConverter(grouping, false);
		return converter.convertToModel(value, new ValueContext(locale)).getOrThrow(r -> new OCSRuntimeException());
	}

	private VaadinUtils() {
		// hidden constructor
	}

}
