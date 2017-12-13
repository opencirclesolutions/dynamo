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
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.container.ServiceContainer;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.DateUtils;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

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

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("ocs.default.locale", "de");
    }

    @Override
    public void setUp() {
        super.setUp();
        Mockito.when(service.getEntityClass()).thenReturn(TestEntity.class);

        Mockito.when(ui.getPage()).thenReturn(page);
        Mockito.when(page.getWebBrowser()).thenReturn(browser);
    }

    @Test
    public void testGetParentOfClass() {

        VerticalLayout vert = new VerticalLayout();
        HorizontalLayout hor = new HorizontalLayout();
        Button button = new Button();

        vert.addComponent(hor);
        hor.addComponent(button);

        Assert.assertEquals(hor, VaadinUtils.getParentOfClass(button, HorizontalLayout.class));
        Assert.assertEquals(vert, VaadinUtils.getParentOfClass(button, VerticalLayout.class));
        Assert.assertNull(VaadinUtils.getParentOfClass(button, Panel.class));
    }

    @Test
    public void testBigDecimalToString() {
        Assert.assertEquals("1.234,56%", VaadinUtils.bigDecimalToString(false, true, true,
                SystemPropertyUtils.getDefaultDecimalPrecision(), BigDecimal.valueOf(1234.56), LOCALE));
        Assert.assertEquals("1.234,56", VaadinUtils.bigDecimalToString(false, false, true,
                SystemPropertyUtils.getDefaultDecimalPrecision(), BigDecimal.valueOf(1234.56), LOCALE));
        Assert.assertEquals("1234,51", VaadinUtils.bigDecimalToString(false, false, false,
                SystemPropertyUtils.getDefaultDecimalPrecision(), BigDecimal.valueOf(1234.512), LOCALE));
        Assert.assertEquals("1234,512",
                VaadinUtils.bigDecimalToString(false, false, false, 3, BigDecimal.valueOf(1234.512), LOCALE));

        Assert.assertEquals("1,234.56%", VaadinUtils.bigDecimalToString(false, true, true,
                SystemPropertyUtils.getDefaultDecimalPrecision(), BigDecimal.valueOf(1234.56), Locale.US));
    }

    @Test
    public void testIntegerToString() {
        Assert.assertEquals("123.456", VaadinUtils.integerToString(true, false, 123456, LOCALE));
        Assert.assertEquals("123456", VaadinUtils.integerToString(false, false, 123456, LOCALE));
        Assert.assertEquals("123,456", VaadinUtils.integerToString(true, false, 123456, Locale.US));
        Assert.assertEquals("123,456%", VaadinUtils.integerToString(true, true, 123456, Locale.US));
    }

    @Test
    public void testLongToString() {
        Assert.assertEquals("123.456", VaadinUtils.longToString(true, false, 123456L, LOCALE));
        Assert.assertEquals("123456", VaadinUtils.longToString(false, false, 123456L, LOCALE));
        Assert.assertEquals("123,456", VaadinUtils.longToString(true, false, 123456L, Locale.US));
    }

    @Test
    public void testGetItemFromContainer() {
        BeanItemContainer<TestEntity> container = new BeanItemContainer<>(TestEntity.class);

        TestEntity t = new TestEntity();
        t.setId(1);
        container.addBean(t);

        TestEntity t2 = VaadinUtils.getEntityFromContainer(container, t);
        Assert.assertEquals(t, t2);
    }

    @Test
    public void testGetItemFromContainer2() {
        EntityModel<TestEntity> model = new EntityModelFactoryImpl().getModel(TestEntity.class);

        ServiceContainer<Integer, TestEntity> container = new ServiceContainer<Integer, TestEntity>(service, model, 20,
                QueryType.PAGING);

        Integer i = (Integer) container.addItem();
        TestEntity t2 = VaadinUtils.getEntityFromContainer(container, i);
        Assert.assertNotNull(t2);
    }

    @Test
    public void testGetTimeZone_CET() {

        Date dt = DateUtils.createDate("01012016");

        TimeZone def = TimeZone.getTimeZone("CET");
        boolean dst = def.inDaylightTime(dt);

        Mockito.when(browser.getRawTimezoneOffset()).thenReturn(3_600_000);
        Mockito.when(browser.isDSTInEffect()).thenReturn(false);

        TimeZone tz = VaadinUtils.getTimeZone(ui);
        Assert.assertEquals(3_600_000, tz.getRawOffset());
        Assert.assertEquals(dst, tz.inDaylightTime(dt));
    }

    @Test
    public void testGetTimeZone_Eastern() {

        Mockito.when(browser.getRawTimezoneOffset()).thenReturn(7_200_000);
        Mockito.when(browser.isDSTInEffect()).thenReturn(false);

        TimeZone tz = VaadinUtils.getTimeZone(ui);

        Assert.assertEquals(7_200_000, tz.getRawOffset());
        Assert.assertEquals(false, tz.inDaylightTime(new Date()));
    }

    @Test
    public void testStringToInteger() {
        // default locale (Central Europe)
        Assert.assertEquals(1234, VaadinUtils.stringToInteger(true, "1.234").intValue());

        Assert.assertEquals(1234, VaadinUtils.stringToInteger(false, "1234", LOCALE).intValue());
        Assert.assertEquals(1234, VaadinUtils.stringToInteger(true, "1.234", LOCALE).intValue());
    }

    @Test
    public void testStringToBigDecimal() {
        // test defaults (European locale and 2 decimals)
        Assert.assertEquals(1234.34, VaadinUtils.stringToBigDecimal(false, false, false, "1234,341").doubleValue(),
                0.001);

        Assert.assertEquals(1234.34,
                VaadinUtils.stringToBigDecimal(false, false, false, 2, "1234,34", LOCALE).doubleValue(), 0.001);
        Assert.assertEquals(1234.3415,
                VaadinUtils.stringToBigDecimal(false, false, false, 4, "1234,3415", LOCALE).doubleValue(), 0.001);

        Assert.assertEquals(1234, VaadinUtils.stringToBigDecimal(false, true, false, 2, "1.234", LOCALE).doubleValue(),
                0.001);
        Assert.assertEquals(1234, VaadinUtils.stringToBigDecimal(true, true, false, 2, "1.234%", LOCALE).doubleValue(),
                0.001);
        Assert.assertEquals(1234, VaadinUtils.stringToBigDecimal(false, true, true, 2, "€ 1.234", LOCALE).doubleValue(),
                0.001);
    }

    @Test
    public void testStringToLong() {
        // use default locale
        Assert.assertEquals(1234L, VaadinUtils.stringToLong(true, "1.234").longValue());

        Assert.assertEquals(1234L, VaadinUtils.stringToLong(false, "1234", LOCALE).longValue());
        Assert.assertEquals(1234L, VaadinUtils.stringToLong(true, "1.234", LOCALE).longValue());
    }
}
