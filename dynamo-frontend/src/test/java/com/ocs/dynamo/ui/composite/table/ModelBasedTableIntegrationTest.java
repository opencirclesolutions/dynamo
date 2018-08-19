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

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.container.ServiceContainer;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.Item;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.shared.data.sort.SortDirection;

public class ModelBasedTableIntegrationTest extends BaseIntegrationTest {

	@Inject
	private TestEntityService testEntityService;

	@Inject
	private EntityModelFactory entityModelFactory;

	private TestEntity entity;

	@Before
	public void setup() {
		entity = new TestEntity("Bob", 45L);
		entity = testEntityService.save(entity);

	}

	/**
	 * Test the working of a model based table combined with a service container
	 * using an ID-based query
	 */
	@Test
	public void testIdBasedQuery() {
		ServiceContainer<Integer, TestEntity> container = new ServiceContainer<>(testEntityService,
				entityModelFactory.getModel(TestEntity.class), 20, QueryType.ID_BASED);
		Assert.assertNotNull(container.getService());

		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);
		ModelBasedTable<Integer, TestEntity> table = new ModelBasedTable<>(container, model, false);

		Assert.assertEquals(17, table.getVisibleColumns().length);

		// items can be retrieved by their primary key
		Item item = table.getItem(entity.getId());
		Assert.assertNotNull(item);

		Assert.assertEquals(1, table.getContainer().size());
		Assert.assertEquals("Bob", item.getItemProperty("name").getValue());
	}

	/**
	 * Test the
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testWrapperIdBasedQuery() {

		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);

		ServiceResultsTableWrapper<Integer, TestEntity> wrapper = new ServiceResultsTableWrapper<>(testEntityService,
				model, QueryType.ID_BASED, null, null, false);
		wrapper.build();

		Assert.assertNotNull(wrapper.getTable());
		Assert.assertEquals(0, wrapper.getSortOrders().size());
		Assert.assertEquals(0, wrapper.getJoins().length);
		ServiceContainer<Integer, TestEntity> container = (ServiceContainer<Integer, TestEntity>) wrapper
				.getContainer();
		Assert.assertNotNull(container);

		Assert.assertEquals(1, wrapper.getContainer().size());

		// add an entity and refresh the container - check that the new item
		// shows up
		TestEntity t2 = new TestEntity("Pete", 55L);
		testEntityService.save(t2);

		wrapper.reloadContainer();
		Assert.assertEquals(2, wrapper.getContainer().size());

		wrapper.search(new Compare.Equal("name", "John"));
		Assert.assertEquals(0, wrapper.getContainer().size());
	}

	/**
	 * Test the
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testWrapperIdBasedQuery_SortOrder() {

		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);

		ServiceResultsTableWrapper<Integer, TestEntity> wrapper = new ServiceResultsTableWrapper<>(testEntityService,
				model, QueryType.ID_BASED, null, Lists.newArrayList(new SortOrder("name", SortDirection.ASCENDING)),
				false);
		wrapper.build();

		Assert.assertNotNull(wrapper.getTable());
		Assert.assertEquals(new SortOrder("name", SortDirection.ASCENDING), wrapper.getSortOrders().get(0));
		Assert.assertEquals(0, wrapper.getJoins().length);
		ServiceContainer<Integer, TestEntity> container = (ServiceContainer<Integer, TestEntity>) wrapper
				.getContainer();
		Assert.assertNotNull(container);

	}

	/**
	 * Test the working of a model based table combined with a service container
	 * using a paging query
	 */
	@Test
	public void testPagingQuery() {
		ServiceContainer<Integer, TestEntity> container = new ServiceContainer<>(testEntityService,
				entityModelFactory.getModel(TestEntity.class), 20, QueryType.PAGING);
		Assert.assertNotNull(container.getService());

		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);
		ModelBasedTable<Integer, TestEntity> table = new ModelBasedTable<>(container, model, false);

		Assert.assertEquals(17, table.getVisibleColumns().length);

		// items can be retrieved by their primary key
		Item item = table.getItem(entity.getId());
		Assert.assertNotNull(item);

		Assert.assertEquals(1, table.getContainer().size());

		Assert.assertEquals("Bob", item.getItemProperty("name").getValue());
	}

	@Test
	public void testSaveNewEntity() {
		ServiceContainer<Integer, TestEntity> container = new ServiceContainer<>(testEntityService,
				entityModelFactory.getModel(TestEntity.class), 20, QueryType.PAGING);

		Integer id = (Integer) container.addItem();
		TestEntity te = VaadinUtils.getEntityFromContainer(container, id);

		// committing the container will save the item
		te.setName("John");
		container.commit();

		TestEntity comp = testEntityService.findByUniqueProperty("name", "John", false);
		Assert.assertNotNull(comp);
	}

	@Test
	public void testRemoveEntity() {
		ServiceContainer<Integer, TestEntity> container = new ServiceContainer<>(testEntityService,
				entityModelFactory.getModel(TestEntity.class), 20, QueryType.PAGING);

		Assert.assertEquals(1, testEntityService.findAll().size());

		container.removeItem(container.getIdByIndex(0));
		container.commit();

		// verify that the item was removed
		Assert.assertEquals(0, testEntityService.findAll().size());

	}

	/**
	 * Test that inappropriate results are filtered out
	 */
	@Test
	public void testFilter() {
		ServiceContainer<Integer, TestEntity> container = new ServiceContainer<>(testEntityService,
				entityModelFactory.getModel(TestEntity.class), 20, QueryType.PAGING);
		container.getQueryView().getQueryDefinition().addFilter(new Compare.Equal("name", "Hank"));

		Assert.assertEquals(1, testEntityService.findAll().size());
		Assert.assertEquals(0, container.size());
	}

	@Test
	public void testSort() {
		ServiceContainer<Integer, TestEntity> container = new ServiceContainer<>(testEntityService,
				entityModelFactory.getModel(TestEntity.class), 20, QueryType.PAGING);

		TestEntity entity2 = new TestEntity("Kevin", 74L);
		testEntityService.save(entity2);

		Assert.assertEquals(2, container.size());

		container.sort(new SortOrder("name", SortDirection.DESCENDING));
		Assert.assertEquals("Kevin", container.getItem(container.getIdByIndex(0)).getItemProperty("name").getValue());
		Assert.assertEquals("Bob", container.getItem(container.getIdByIndex(1)).getItemProperty("name").getValue());

		container.sort(new SortOrder("name", SortDirection.ASCENDING));
		Assert.assertEquals("Bob", container.getItem(container.getIdByIndex(0)).getItemProperty("name").getValue());
		Assert.assertEquals("Kevin", container.getItem(container.getIdByIndex(1)).getItemProperty("name").getValue());
	}
}
