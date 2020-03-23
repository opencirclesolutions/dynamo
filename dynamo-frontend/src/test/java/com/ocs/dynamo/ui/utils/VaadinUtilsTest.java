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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.server.WebBrowser;

public class VaadinUtilsTest extends BaseMockitoTest {

    private static final Locale LOCALE = new Locale("nl");

    @Mock
    private MessageService messageService;

    @Mock
    private BaseService<Integer, TestEntity> service;

    @Mock
    private UI ui;

    @Mock
    private Page page;

    @Mock
    private WebBrowser browser;

    @BeforeAll
    public static void beforeClass() {
        System.setProperty(DynamoConstants.SP_DEFAULT_LOCALE, "de");
        System.setProperty(DynamoConstants.SP_SERVICE_LOCATOR_CLASS_NAME, "com.ocs.dynamo.ui.SpringTestServiceLocator");
    }

    @Test
    public void testGetParentOfClass() {

        VerticalLayout vert = new VerticalLayout();
        HorizontalLayout hor = new HorizontalLayout();
        Button button = new Button();

        vert.add(hor);
        hor.add(button);

        assertEquals(hor, VaadinUtils.getParentOfClass(button, HorizontalLayout.class));
        assertEquals(vert, VaadinUtils.getParentOfClass(button, VerticalLayout.class));
    }

    @Test
    public void testBigDecimalToString() {
        assertEquals("1.234,56%", VaadinUtils.bigDecimalToString(false, true, true, SystemPropertyUtils.getDefaultDecimalPrecision(),
                BigDecimal.valueOf(1234.56), LOCALE));
        assertEquals("1.234,56", VaadinUtils.bigDecimalToString(false, false, true, SystemPropertyUtils.getDefaultDecimalPrecision(),
                BigDecimal.valueOf(1234.56), LOCALE));
        assertEquals("1234,51", VaadinUtils.bigDecimalToString(false, false, false, SystemPropertyUtils.getDefaultDecimalPrecision(),
                BigDecimal.valueOf(1234.512), LOCALE));
        assertEquals("1234,512", VaadinUtils.bigDecimalToString(false, false, false, 3, BigDecimal.valueOf(1234.512), LOCALE));

        assertEquals("1,234.56%", VaadinUtils.bigDecimalToString(false, true, true, SystemPropertyUtils.getDefaultDecimalPrecision(),
                BigDecimal.valueOf(1234.56), Locale.US));
    }

    @Test
    public void testIntegerToString() {
        assertEquals("123.456", VaadinUtils.integerToString(true, false, 123456, LOCALE));
        assertEquals("123456", VaadinUtils.integerToString(false, false, 123456, LOCALE));
        assertEquals("123,456", VaadinUtils.integerToString(true, false, 123456, Locale.US));
        assertEquals("123,456%", VaadinUtils.integerToString(true, true, 123456, Locale.US));
    }

    @Test
    public void testLongToString() {
        assertEquals("123.456", VaadinUtils.longToString(true, false, 123456L, LOCALE));
        assertEquals("123456", VaadinUtils.longToString(false, false, 123456L, LOCALE));
        assertEquals("123,456", VaadinUtils.longToString(true, false, 123456L, Locale.US));
    }

    @Test
    public void testDoubleToString() {
        // Dutch locale
        assertEquals("123456,00", VaadinUtils.doubleToString(false, false, false, 2, 123456.00, LOCALE));
        // grouping
        assertEquals("123.456,00", VaadinUtils.doubleToString(false, false, true, 2, 123456.00, LOCALE));
        // percentage
        assertEquals("123.456,00%", VaadinUtils.doubleToString(false, true, true, 2, 123456.00, LOCALE));

        // US locale
        assertEquals("123456.00", VaadinUtils.doubleToString(false, false, false, 2, 123456.00, Locale.US));
        // grouping
        assertEquals("123,456.00", VaadinUtils.doubleToString(false, false, true, 2, 123456.00, Locale.US));
        // percentage
        assertEquals("123,456.00%", VaadinUtils.doubleToString(false, true, true, 2, 123456.00, Locale.US));
    }

    @Test
    public void testStringToInteger() {
        // default locale (Central Europe)
        assertEquals(1234, VaadinUtils.stringToInteger(true, "1.234").intValue());
        assertEquals(1234, VaadinUtils.stringToInteger(false, "1234", LOCALE).intValue());
        assertEquals(1234, VaadinUtils.stringToInteger(true, "1.234", LOCALE).intValue());
    }

    @Test
    public void testStringToBigDecimal() {
        // test defaults (European locale and 2 decimals)
        assertEquals(1234.34, VaadinUtils.stringToBigDecimal(false, false, false, "1234,341").doubleValue(), 0.001);

        assertEquals(1234.34, VaadinUtils.stringToBigDecimal(false, false, false, 2, "1234,34", LOCALE).doubleValue(), 0.001);
        assertEquals(1234.3415, VaadinUtils.stringToBigDecimal(false, false, false, 4, "1234,3415", LOCALE).doubleValue(), 0.001);
        assertEquals(1234, VaadinUtils.stringToBigDecimal(false, true, false, 2, "1.234", LOCALE).doubleValue(), 0.001);
        assertEquals(1234, VaadinUtils.stringToBigDecimal(true, true, false, 2, "1.234%", LOCALE).doubleValue(), 0.001);
        assertEquals(1234, VaadinUtils.stringToBigDecimal(false, true, true, 2, "€ 1.234", LOCALE).doubleValue(), 0.001);
    }

    @Test
    public void testStringToLong() {
        // use default locale
        assertEquals(1234L, VaadinUtils.stringToLong(true, "1.234").longValue());
        assertEquals(1234L, VaadinUtils.stringToLong(false, "1234", LOCALE).longValue());
        assertEquals(1234L, VaadinUtils.stringToLong(true, "1.234", LOCALE).longValue());
    }

    @Test
    public void testStringToDouble() {
        assertEquals(1234.34, VaadinUtils.stringToDouble(false, false, false, 2, "1234,34", LOCALE), 0.001);
        assertEquals(1234.3415, VaadinUtils.stringToDouble(false, false, false, 4, "1234,3415", LOCALE), 0.001);
        assertEquals(1234, VaadinUtils.stringToDouble(false, true, false, 2, "1.234", LOCALE), 0.001);
        assertEquals(1234, VaadinUtils.stringToDouble(true, true, false, 2, "1.234%", LOCALE), 0.001);
        assertEquals(1234, VaadinUtils.stringToDouble(false, true, true, 2, "€ 1.234", LOCALE), 0.001);
    }
}
