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

import org.vaadin.addons.lazyquerycontainer.CompositeItem;
import org.vaadin.addons.lazyquerycontainer.NestingBeanItem;
import org.vaadin.dialogs.ConfirmDialog;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.converter.BigDecimalConverter;
import com.ocs.dynamo.ui.converter.ConverterFactory;
import com.ocs.dynamo.utils.SystemPropertyUtils;
import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.data.util.converter.StringToLongConverter;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;

/**
 * Utility class for Vaadin-related functionality
 * 
 * @author bas.rutten
 */
public final class VaadinUtils {

    private VaadinUtils() {
        // hidden constructor
    }

    /**
     * Check if all editable fields that are contained in a (fixed, non-lazy) table are valid. This
     * method is needed because simply calling table.isValid() will not take into account any
     * editable components within the table
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
    public static String bigDecimalToString(boolean percentage, boolean useGrouping,
            BigDecimal value) {
        return bigDecimalToString(false, percentage, useGrouping,
                SystemPropertyUtils.getDefaultDecimalPrecision(), value, getLocale());
    }

    /**
     * 
     * Converts a BigDecimal value to a String (shortcut for values that are not currency and not
     * percentage)
     * 
     * @param percentage
     *            whether the value represents a percentage
     * @param useGrouping
     *            whether to use a thousand grouping
     * @param value
     *            the value
     * @return
     */
    public static String bigDecimalToString(boolean currency, boolean percentage,
            boolean useGrouping, BigDecimal value) {
        return bigDecimalToString(currency, percentage, useGrouping,
                SystemPropertyUtils.getDefaultDecimalPrecision(), value, getLocale());
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
    public static String bigDecimalToString(boolean currency, boolean percentage,
            boolean useGrouping, int precision, BigDecimal value, Locale locale) {
        BigDecimalConverter converter = ConverterFactory.createBigDecimalConverter(currency,
                percentage, useGrouping, precision, getCurrencySymbol());
        return converter.convertToPresentation(value, String.class, locale);
    }

    /**
     * Enables copy/paste behavior for Internet Explorer
     */
    public static void enableCopyPaste() {
        String js = "$(document).ready(function() {" + "  $('input').off('paste'); "
                + "  $('input').on('paste', function(e) { " + "    if (clipboardData) { "
                + "      setTimeout(function() {"
                + "        var pasted = clipboardData.getData('text');"
                + "        pasted = pasted.replace(/(\\r\\n|\\r|\\n)/g,'\t');"
                + "        clipboardData.setData('text', pasted); $(e.target).val(pasted);} "
                + "      , 100);  " + "    }" + "  }) " + "});";
        Page.getCurrent().getJavaScript().execute(js);
    }

    /**
     * Returns the currency symbol to be used - by default this is looked up from the session, with
     * a fallback to the system property "default.currency.symbol"
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
    @SuppressWarnings("unchecked")
    public static <ID, T> T getEntityFromContainer(Container container, ID id) {
        Object obj = container.getItem(id);
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
     * Returns the first parent component of the specified component that is a subclass of the
     * specified class
     * 
     * @param component
     *            the component
     * @param clazz
     *            the class
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends Component> T getParentOfClass(Component component, Class<T> clazz) {
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
     * Returns the user's time zone (based on the offset). The result of this method can be passed
     * to a date converter or simple date format to make sure the date is properly formatted
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
    public static String integerToString(boolean grouping, Integer value) {
        return integerToString(grouping, value, getLocale());
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
    public static String integerToString(boolean grouping, Integer value, Locale locale) {
        StringToIntegerConverter converter = ConverterFactory.createIntegerConverter(grouping);
        return converter.convertToPresentation(value, String.class, locale);
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
    public static String longToString(boolean grouping, Long value) {
        return longToString(grouping, value, getLocale());
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
    public static String longToString(boolean grouping, Long value, Locale locale) {
        StringToLongConverter converter = ConverterFactory.createLongConverter(grouping);
        return converter.convertToPresentation(value, String.class, locale);
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
    public static void showConfirmDialog(MessageService messageService, String question,
            final Runnable whenConfirmed) {
        if (UI.getCurrent() != null) {
            ConfirmDialog.show(UI.getCurrent(), messageService.getMessage("ocs.confirm"), question,
                    messageService.getMessage("ocs.yes"), messageService.getMessage("ocs.no"),
                    new ConfirmDialog.Listener() {

                        private static final long serialVersionUID = 7993938332012882422L;

                        @Override
                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {
                                whenConfirmed.run();
                            }
                        }
                    });
        } else {
            whenConfirmed.run();
        }
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
    public static BigDecimal stringToBigDecimal(boolean percentage, boolean useGrouping,
            boolean currency, String value) {
        return stringToBigDecimal(percentage, useGrouping, currency,
                SystemPropertyUtils.getDefaultDecimalPrecision(), value, getLocale());
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
    public static BigDecimal stringToBigDecimal(boolean percentage, boolean useGrouping,
            boolean currency, int precision, String value, Locale locale) {
        BigDecimalConverter converter = ConverterFactory.createBigDecimalConverter(currency,
                percentage, useGrouping, precision, VaadinUtils.getCurrencySymbol());
        return converter.convertToModel(value, BigDecimal.class, locale);
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
        StringToIntegerConverter converter = ConverterFactory.createIntegerConverter(grouping);
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
        StringToLongConverter converter = ConverterFactory.createLongConverter(grouping);
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
        StringToLongConverter converter = ConverterFactory.createLongConverter(grouping);
        return converter.convertToModel(value, Long.class, locale);
    }

}
