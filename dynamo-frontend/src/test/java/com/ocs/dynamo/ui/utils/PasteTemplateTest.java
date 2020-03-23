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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.grid.Grid;

public class PasteTemplateTest extends BaseMockitoTest {

	@Mock
	private ValueChangeEvent<String> event;

	private PasteTemplate<TestEntity> template;

	private Locale locale = new Locale("nl");

	private Grid<TestEntity> grid = new Grid<TestEntity>();

	/**
	 * Test that nothing happens for a single value
	 */
	@Test
	public void testIgnoreForSingleValue() {
		when(event.getValue()).thenReturn("3");
		final List<String> values = new ArrayList<>();

		template = new PasteTemplate<TestEntity>(locale, grid, event) {

			@Override
			protected void process(int index, String value) {
				values.add(value);
			}

			@Override
			protected void clearSourceField(ValueChangeEvent<String> event) {
				// do nothing
			}
		};

		template.execute();
		assertEquals(0, values.size());
	}

	@Test
	public void testMultipleValues() {
		when(event.getValue()).thenReturn("3 4 5");
		final List<String> values = new ArrayList<>();

		template = new PasteTemplate<TestEntity>(locale, grid, event) {

			@Override
			protected void process(int index, String value) {
				values.add(value);
			}

			@Override
			protected void clearSourceField(ValueChangeEvent<String> event) {
				// do nothing
			}
		};

		template.execute();
		assertEquals(3, values.size());
		assertEquals("3", values.get(0));
		assertEquals("4", values.get(1));
		assertEquals("5", values.get(2));
	}

	@Test
	public void testMultipleValuesWithTabs() {
		when(event.getValue()).thenReturn("3\t4\t5");
		final List<String> values = new ArrayList<>();

		template = new PasteTemplate<TestEntity>(locale, grid, event) {

			@Override
			protected void process(int index, String value) {
				values.add(value);
			}

			@Override
			protected void clearSourceField(ValueChangeEvent<String> event) {
				// do nothing
			}
		};

		template.execute();
		assertEquals(3, values.size());
		assertEquals("3", values.get(0));
		assertEquals("4", values.get(1));
		assertEquals("5", values.get(2));
	}

	/**
	 * Tests that the decimal separator is correctly translated to the correct
	 * locale first
	 */
	@Test
	public void testSeparatorReplace() {
		when(event.getValue()).thenReturn("4.2 5.2");
		final List<String> values = new ArrayList<>();

		template = new PasteTemplate<TestEntity>(locale, grid, event) {

			@Override
			protected void process(int index, String value) {
				values.add(value);
			}

			@Override
			protected void clearSourceField(ValueChangeEvent<String> event) {
				// do nothing
			}
		};

		template.execute();
		assertEquals(2, values.size());
		assertEquals("4,2", values.get(0));
		assertEquals("5,2", values.get(1));
	}
}
