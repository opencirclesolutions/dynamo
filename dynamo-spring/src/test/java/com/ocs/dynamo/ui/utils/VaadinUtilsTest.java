package com.ocs.dynamo.ui.utils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.container.ServiceContainer;
import com.ocs.dynamo.ui.container.ServiceQueryDefinition;
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

	@Override
	public void setUp() throws Exception {
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
		Assert.assertEquals("1.234,56%",
		        VaadinUtils.bigDecimalToString(true, true, BigDecimal.valueOf(1234.56), LOCALE));
		Assert.assertEquals("1.234,56",
		        VaadinUtils.bigDecimalToString(false, true, BigDecimal.valueOf(1234.56), LOCALE));
		Assert.assertEquals("1234,56",
		        VaadinUtils.bigDecimalToString(false, false, BigDecimal.valueOf(1234.56), LOCALE));
		Assert.assertEquals("1,234.56%",
		        VaadinUtils.bigDecimalToString(true, true, BigDecimal.valueOf(1234.56), Locale.US));
	}

	@Test
	public void testIntegerToString() {
		Assert.assertEquals("123.456", VaadinUtils.integerToString(true, 123456, LOCALE));
		Assert.assertEquals("123456", VaadinUtils.integerToString(false, 123456, LOCALE));
		Assert.assertEquals("123,456", VaadinUtils.integerToString(true, 123456, Locale.US));
	}

	@Test
	public void testLongToString() {
		Assert.assertEquals("123.456", VaadinUtils.longToString(true, 123456L, LOCALE));
		Assert.assertEquals("123456", VaadinUtils.longToString(false, 123456L, LOCALE));
		Assert.assertEquals("123,456", VaadinUtils.longToString(true, 123456L, Locale.US));
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
		ServiceContainer<Integer, TestEntity> container = new ServiceContainer<>(
		        new ServiceQueryDefinition<>(service, false, 20, QueryType.ID_BASED, null));

		Integer i = (Integer) container.addItem();
		TestEntity t2 = VaadinUtils.getEntityFromContainer(container, i);
		Assert.assertNotNull(t2);
	}

	@Test
	public void testGetTimeZone_CET() {

		TimeZone def = TimeZone.getTimeZone("CET");
		boolean dst = def.inDaylightTime(new Date());

		Mockito.when(browser.getRawTimezoneOffset()).thenReturn(3_600_000);
		Mockito.when(browser.isDSTInEffect()).thenReturn(false);

		TimeZone tz = VaadinUtils.getTimeZone(ui);
		Assert.assertEquals(3_600_000, tz.getRawOffset());
		Assert.assertEquals(dst, tz.inDaylightTime(new Date()));
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
		Assert.assertEquals(1234, VaadinUtils.stringToInteger(false, "1234", LOCALE).intValue());
		Assert.assertEquals(1234, VaadinUtils.stringToInteger(true, "1.234", LOCALE).intValue());
	}

	@Test
	public void testStringToBigDecimal() {
		Assert.assertEquals(1234,
		        VaadinUtils.stringToBigDecimal(false, false, "1234", LOCALE).intValue());
		Assert.assertEquals(1234,
		        VaadinUtils.stringToBigDecimal(false, true, "1.234", LOCALE).intValue());
		Assert.assertEquals(1234,
		        VaadinUtils.stringToBigDecimal(true, true, "1.234%", LOCALE).intValue());
	}

	@Test
	public void testStringToLong() {
		Assert.assertEquals(1234L, VaadinUtils.stringToLong(false, "1234", LOCALE).longValue());
		Assert.assertEquals(1234L, VaadinUtils.stringToLong(true, "1.234", LOCALE).longValue());
	}
}
