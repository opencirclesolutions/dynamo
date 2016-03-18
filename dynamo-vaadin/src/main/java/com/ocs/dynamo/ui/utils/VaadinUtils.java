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

import com.ocs.dynamo.constants.OCSConstants;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.converter.BigDecimalConverter;
import com.ocs.dynamo.ui.converter.ConverterFactory;
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

    private static final int DEFAULT_PRECISION = 2;

    private VaadinUtils() {
        // hidden constructor
    }

    /**
     * Check if all editable fields that are contained in a (fixed, non-lazy) table are valid. This
     * method is needed because simply calling table.isValid() will not take into account any
     * editable components within the table
     * 
     * @param table
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
     * Converts a BigDecimal to a string, using the built in Vaadin converter
     * 
     * @param percentage
     *            whether the value represents a percentage
     * @param useGrouping
     *            whether to use a thousands grouping separator
     * @Param value the value to convert
     * @return
     */
    public static String bigDecimalToString(boolean percentage, boolean useGrouping,
            BigDecimal value, Locale locale) {
        return bigDecimalToString(false, percentage, useGrouping, DEFAULT_PRECISION, value, locale);
    }

    /**
     * Converts a BigDecimal to a string, using the built in Vaadin converter
     * 
     * @param percentage
     *            whether the value represents a percentage
     * @param useGrouping
     *            whether to use a thousands grouping separator
     * @param precision
     *            the precision (number of decimals)
     * @Param value the value to convert
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
        VaadinSession vs = VaadinSession.getCurrent();
        if (vs == null) {
            return null;
        }
        String cs = (String) vs.getAttribute(OCSConstants.CURRENCY_SYMBOL);
        return cs != null ? cs : System.getProperty("default.currency.symbol");
    }

    /**
     * Retrieves an entity with a certain ID from the lazy query container
     * 
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
     * Returns the parent component of the specified component that has a certain class
     * 
     * @param c
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends Component> T getParentOfClass(Component c, Class<T> clazz) {
        while (c.getParent() != null) {
            c = c.getParent();
            if (clazz.isAssignableFrom(c.getClass())) {
                return (T) c;
            }

        }
        return null;
    }

    /**
     * Returns the first value from a session attribute that holds a map
     * 
     * @param attributeName
     *            the name of the attribute
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String getSessionAttributeValueFromMap(String attributeName, String key) {
        Map<String, Object> map = (Map<String, Object>) VaadinSession.getCurrent().getSession()
                .getAttribute(attributeName);
        return getSessionAttributeValueFromMap(map, key);
    }

    public static String getSessionAttributeValueFromMap(Map<String, Object> map, String key) {
        if (map != null) {
            Collection<?> col = (Collection<?>) map.get(key);
            if (col != null) {
                return col.iterator().next().toString();
            }
        }
        return null;
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
        return integerToString(grouping, value, VaadinSession.getCurrent().getLocale());
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
        return longToString(grouping, value, VaadinSession.getCurrent().getLocale());
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
            String value) {
        return stringToBigDecimal(percentage, useGrouping, value,
                VaadinSession.getCurrent().getLocale());
    }

    public static BigDecimal stringToBigDecimal(boolean percentage, boolean useGrouping,
            String value, Locale locale) {
        BigDecimalConverter converter = ConverterFactory.createBigDecimalConverter(false,
                percentage, useGrouping, DEFAULT_PRECISION, null);
        return converter.convertToModel(value, BigDecimal.class, locale);
    }

    /**
     * @param grouping
     * @param value
     * @return
     */
    public static Integer stringToInteger(boolean grouping, String value) {
        return stringToInteger(grouping, value, VaadinSession.getCurrent().getLocale());
    }

    /**
     * Converts a String to an Integer, using the Vaadin converters
     * 
     * @param grouping
     *            indicates whether the string could contain grouping separators
     * @param value
     * @return
     */
    public static Integer stringToInteger(boolean grouping, String value, Locale locale) {
        StringToIntegerConverter converter = ConverterFactory.createIntegerConverter(grouping);
        return converter.convertToModel(value, Integer.class, locale);
    }

    /**
     * Converts a String to a long
     * 
     * @param grouping
     *            indicates if a thousands separator is used
     * @param value
     *            the value to convert
     * @return
     */
    public static Long stringToLong(boolean grouping, String value) {
        StringToLongConverter converter = ConverterFactory.createLongConverter(grouping);
        return converter.convertToModel(value, Long.class, VaadinSession.getCurrent().getLocale());
    }

    /**
     * Converts a String to a long
     * 
     * @param grouping
     *            indicates if a thousands separator is used
     * @param value
     *            the value to convert
     * @param locale
     *            the locale
     * @return
     */
    public static Long stringToLong(boolean grouping, String value, Locale locale) {
        StringToLongConverter converter = ConverterFactory.createLongConverter(grouping);
        return converter.convertToModel(value, Long.class, locale);
    }

}
