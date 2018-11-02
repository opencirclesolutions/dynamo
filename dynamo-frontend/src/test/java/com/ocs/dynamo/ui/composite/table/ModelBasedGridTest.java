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
package com.ocs.dynamo.ui.composite.table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.Grid;

import junitx.util.PrivateAccessor;

public class ModelBasedGridTest extends BaseMockitoTest {

	private EntityModelFactory entityModelFactory = new EntityModelFactoryImpl();

	@Mock
	private MessageService messageService;

	@Mock
	private TestEntityService service;

	@Override
	public void setUp() {
		super.setUp();
		Mockito.when(service.getEntityClass()).thenReturn(TestEntity.class);
		MockUtil.mockMessageService(messageService);
		try {
			PrivateAccessor.setField(entityModelFactory, "messageService", messageService);
		} catch (NoSuchFieldException e) {
			Assert.fail();
		}
	}

	@Test
	public void testDataProvider() {
		ListDataProvider<Person> provider = new ListDataProvider<Person>(Lists.newArrayList());
		EntityModel<Person> model = entityModelFactory.getModel(Person.class);

		Person person = new Person(1, "Bob", 50, BigDecimal.valueOf(76.4), BigDecimal.valueOf(44.4));
		provider.getItems().add(person);

		ModelBasedGrid<Integer, Person> grid = new ModelBasedGrid<>(provider, model, false, false);

		Assert.assertEquals("Persons", grid.getCaption());
		Assert.assertEquals("Person", grid.getDescription());

		// String result = grid.formatPropertyValue(person, "age",
		// grid.getItem(person).getItemProperty("age"));
		// Assert.assertEquals("50", result);
	}

	@Test
	@Ignore
	public void testFixedTableWrapper() {
		TestEntity entity = new TestEntity();

		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);
		FixedGridWrapper<Integer, TestEntity> wrapper = new FixedGridWrapper<>(service, model,
				Lists.newArrayList(entity), new ArrayList<>(), false);
		wrapper.build();

		Grid<TestEntity> grid = wrapper.getGrid();
		Assert.assertNotNull(grid);

	}

//	@Test
//	public void testSetVisible() {
//		ListDataProvider<Person> container = new ListDataProvider<>(new ArrayList<>());
//		EntityModel<Person> model = entityModelFactory.getModel(Person.class);
//
//		Person person = new Person(1, "Bob", 50, BigDecimal.valueOf(76.4), BigDecimal.valueOf(44.4));
//		container.getItems().add(person);
//
//		ModelBasedTable<Integer, Person> table = new ModelBasedTable<>(container, model, false);
//
//		Assert.assertTrue(Arrays.asList(table.getVisibleColumns()).contains("name"));
//
//		// setting an already visible column to true doesn't do anything
//		table.setColumnVisible("name", true);
//		Assert.assertTrue(Arrays.asList(table.getVisibleColumns()).contains("name"));
//
//		// hide column
//		table.setColumnVisible("name", false);
//		Assert.assertFalse(Arrays.asList(table.getVisibleColumns()).contains("name"));
//
//		/// show it again
//		table.setColumnVisible("name", true);
//		Assert.assertTrue(Arrays.asList(table.getVisibleColumns()).contains("name"));
//	}

}
