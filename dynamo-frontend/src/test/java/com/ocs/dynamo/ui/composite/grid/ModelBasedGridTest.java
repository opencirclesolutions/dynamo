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
package com.ocs.dynamo.ui.composite.grid;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.composite.ComponentContext;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.type.GridEditMode;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;

public class ModelBasedGridTest extends BaseMockitoTest {

	private final EntityModelFactory entityModelFactory = new EntityModelFactoryImpl();

	@Mock
	private MessageService messageService;

	@Mock
	private TestEntityService service;

	@BeforeEach
	public void setUp() {
		ReflectionTestUtils.setField(entityModelFactory, "messageService", messageService);
	}

	@Test
	public void testDataProvider() {
		ListDataProvider<Person> provider = new ListDataProvider<>(new ArrayList<>());
		EntityModel<Person> model = entityModelFactory.getModel(Person.class);

		Person person = new Person(1, "Bob", 50, BigDecimal.valueOf(76.4), BigDecimal.valueOf(44.4));
		provider.getItems().add(person);

		ComponentContext<Integer, Person> cc = ComponentContext.<Integer, Person>builder().editable(false).build();
		FormOptions fo = new FormOptions().setPreserveSortOrders(true).setGridEditMode(GridEditMode.SINGLE_ROW);

		assertDoesNotThrow(
				() -> new ModelBasedGrid<>(provider, model, new HashMap<String, SerializablePredicate<?>>(), fo, cc));
	}

	@Test
	public void testMultipleRowsEditable() {
		ListDataProvider<Person> provider = new ListDataProvider<>(new ArrayList<>());
		EntityModel<Person> model = entityModelFactory.getModel(Person.class);

		Person person = new Person(1, "Bob", 50, BigDecimal.valueOf(76.4), BigDecimal.valueOf(44.4));
		provider.getItems().add(person);

		ComponentContext<Integer, Person> cc = ComponentContext.<Integer, Person>builder().editable(true).build();
		FormOptions fo = new FormOptions().setPreserveSortOrders(true).setGridEditMode(GridEditMode.SIMULTANEOUS);

		assertDoesNotThrow(
				() -> new ModelBasedGrid<>(provider, model, new HashMap<>(), fo, cc));
	}

	@Test
	public void testFixedTableWrapper() {
		TestEntity entity = new TestEntity();

		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);
		FixedGridWrapper<Integer, TestEntity> wrapper = new FixedGridWrapper<>(service, model, new FormOptions(),
				ComponentContext.<Integer, TestEntity>builder().build(),
				new HashMap<String, SerializablePredicate<?>>(), List.of(entity), new ArrayList<>());
		wrapper.build();

		Grid<TestEntity> grid = wrapper.getGrid();
		assertNotNull(grid);
	}

	@Test
	public void testSetVisible() {
		ListDataProvider<Person> provider = new ListDataProvider<>(new ArrayList<>());
		EntityModel<Person> model = entityModelFactory.getModel(Person.class);

		Person person = new Person(1, "Bob", 50, BigDecimal.valueOf(76.4), BigDecimal.valueOf(44.4));
		provider.getItems().add(person);

		ComponentContext<Integer, Person> cc = ComponentContext.<Integer, Person>builder().editable(false).build();
		FormOptions fo = new FormOptions().setPreserveSortOrders(true).setGridEditMode(GridEditMode.SINGLE_ROW);

		ModelBasedGrid<Integer, Person> grid = new ModelBasedGrid<>(provider, model,
				new HashMap<>(), fo, cc);
		grid.build();

		assertTrue(grid.getColumnByKey("name").isVisible());

		grid.setColumnVisible("name", true);
		assertTrue(grid.getColumnByKey("name").isVisible());

		grid.setColumnVisible("name", false);
		assertFalse(grid.getColumnByKey("name").isVisible());
	}

}
