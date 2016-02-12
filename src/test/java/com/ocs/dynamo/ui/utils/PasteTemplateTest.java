package com.ocs.dynamo.ui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.ocs.dynamo.test.BaseMockitoTest;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.ui.Table;

public class PasteTemplateTest extends BaseMockitoTest {

	@Mock
	private TextChangeEvent event;

	private PasteTemplate template;

	private Locale locale = new Locale("nl");

	private Table table = new Table();

	@Override
	public void setUp() throws Exception {
		super.setUp();

	}

	/**
	 * Test that nothing happens for a single value
	 */
	@Test
	public void testIgnoreForSingleValue() {
		Mockito.when(event.getText()).thenReturn("3");
		final List<String> values = new ArrayList<>();

		template = new PasteTemplate(locale, table, event) {

			@Override
			protected void process(int index, String value) {
				values.add(value);
			}

			@Override
			protected void clearSourceField(TextChangeEvent event) {
				// do nothing
			}
		};

		template.execute();
		Assert.assertEquals(0, values.size());
	}

	@Test
	public void testMultipleValues() {
		Mockito.when(event.getText()).thenReturn("3 4 5");
		final List<String> values = new ArrayList<>();

		template = new PasteTemplate(locale, table, event) {

			@Override
			protected void process(int index, String value) {
				values.add(value);
			}

			@Override
			protected void clearSourceField(TextChangeEvent event) {
				// do nothing
			}
		};

		template.execute();
		Assert.assertEquals(3, values.size());
		Assert.assertEquals("3", values.get(0));
		Assert.assertEquals("4", values.get(1));
		Assert.assertEquals("5", values.get(2));
	}

	@Test
	public void testMultipleValuesWithTabs() {
		Mockito.when(event.getText()).thenReturn("3\t4\t5");
		final List<String> values = new ArrayList<>();

		template = new PasteTemplate(locale, table, event) {

			@Override
			protected void process(int index, String value) {
				values.add(value);
			}

			@Override
			protected void clearSourceField(TextChangeEvent event) {
				// do nothing
			}
		};

		template.execute();
		Assert.assertEquals(3, values.size());
		Assert.assertEquals("3", values.get(0));
		Assert.assertEquals("4", values.get(1));
		Assert.assertEquals("5", values.get(2));
	}

	/**
	 * Tests that the decimal separator is correctly translated to the correct
	 * locale first
	 */
	@Test
	public void testSeparatorReplace() {
		Mockito.when(event.getText()).thenReturn("4.2 5.2");
		final List<String> values = new ArrayList<>();

		template = new PasteTemplate(locale, table, event) {

			@Override
			protected void process(int index, String value) {
				values.add(value);
			}

			@Override
			protected void clearSourceField(TextChangeEvent event) {
				// do nothing
			}
		};

		template.execute();
		Assert.assertEquals(2, values.size());
		Assert.assertEquals("4,2", values.get(0));
		Assert.assertEquals("5,2", values.get(1));
	}
}
