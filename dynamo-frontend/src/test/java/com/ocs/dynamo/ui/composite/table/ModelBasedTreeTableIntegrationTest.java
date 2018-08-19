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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.TestEntity2Service;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.container.hierarchical.ModelBasedHierarchicalContainer;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.shared.data.sort.SortDirection;

public class ModelBasedTreeTableIntegrationTest extends BaseIntegrationTest {

	@Inject
	private TestEntityService testEntityService;

	@Inject
	private TestEntity2Service testEntity2Service;

	@Inject
	private EntityModelFactory entityModelFactory;

	@Inject
	private MessageService messageService;

	private TestEntity entity;

	@Before
	public void setup() {
		System.setProperty(DynamoConstants.SP_ALLOW_TABLE_EXPORT, "true");

		entity = new TestEntity("Bob", 45L);

		TestEntity2 child1 = new TestEntity2();
		TestEntity2 child2 = new TestEntity2();
		entity.addTestEntity2(child1);
		entity.addTestEntity2(child2);
		entity = testEntityService.save(entity);
	}

	@Test
	public void testCreateModelBasedHierarchicalContainer() {
		EntityModel<TestEntity> model = entityModelFactory.getModel("TestEntityNested", TestEntity.class);
		List<BaseService<?, ?>> services = new ArrayList<>();

		services.add(testEntityService);
		services.add(testEntity2Service);

		ModelBasedHierarchicalContainer<TestEntity> container = new ModelBasedHierarchicalContainer<>(messageService,
				model, services, null);

		ModelBasedTreeTable<Integer, TestEntity> table = new ModelBasedTreeTable<>(container, entityModelFactory, true);

		Assert.assertEquals(1, table.getContainerDataSource().size());

		Object firstItemId = table.firstItemId();

		// check that properties are properly created
		String name = (String) table.getItem(firstItemId).getItemProperty("name").getValue();
		Assert.assertEquals("Bob", name);
		Assert.assertEquals(3, table.getVisibleColumns().length);

		Assert.assertNotNull(table.getActionHideAll());
		Assert.assertNotNull(table.getActionExpandAll());

		// call the expand action
		Assert.assertEquals(2, table.getActions(null, null).length);

		table.setValue(firstItemId);
		table.handleAction(table.getActions(null, null)[0], table, table);

		// check that node is expanded now
		Assert.assertFalse(table.isCollapsed(firstItemId));

		// collapse it again
		table.setValue(firstItemId);
		table.handleAction(table.getActions(null, null)[1], table, table);

		Assert.assertTrue(table.isCollapsed(firstItemId));

		// expand again (using a set)
		table.setMultiSelect(true);
		table.select(firstItemId);
		table.handleAction(table.getActions(null, null)[0], table, table);
		Assert.assertFalse(table.isCollapsed(firstItemId));

		table.setExpandAndHideAllowed(false);
		Assert.assertEquals(0, table.getActions(null, null).length);
	}

	@Test
	public void testCreateServiceResultsTreeTableWrapper() {
		EntityModel<TestEntity> model = entityModelFactory.getModel("TestEntityNested", TestEntity.class);
		List<BaseService<?, ?>> services = new ArrayList<>();

		services.add(testEntityService);
		services.add(testEntity2Service);

		ServiceResultsTreeTableWrapper<Integer, TestEntity> wrapper = new ServiceResultsTreeTableWrapper<>(services,
				model, QueryType.PAGING, null, true);
		wrapper.build();

		Assert.assertNotNull(wrapper.getContainer());
		Assert.assertNotNull(wrapper.getTable());
		Assert.assertNull(wrapper.getTable().getSortContainerPropertyId());

		Assert.assertEquals(1, wrapper.getContainer().size());
	}

	@Test
	public void testCreateServiceResultsTreeTableWrapper_Sorting() {
		EntityModel<TestEntity> model = entityModelFactory.getModel("TestEntityNested", TestEntity.class);
		List<BaseService<?, ?>> services = new ArrayList<>();

		services.add(testEntityService);
		services.add(testEntity2Service);

		ServiceResultsTreeTableWrapper<Integer, TestEntity> wrapper = new ServiceResultsTreeTableWrapper<>(services,
				model, QueryType.PAGING, Lists.newArrayList(new SortOrder("name", SortDirection.ASCENDING)), true);
		wrapper.build();

		Assert.assertNotNull(wrapper.getContainer());
		Assert.assertNotNull(wrapper.getTable());
		Assert.assertEquals("name", wrapper.getTable().getSortContainerPropertyId());
		Assert.assertTrue(wrapper.getTable().isSortAscending());

		Assert.assertEquals(1, wrapper.getContainer().size());

		// try a search
		wrapper.search(new Compare.Equal("name", "Kevin"));
		Assert.assertEquals(0, wrapper.getContainer().size());
	}
}
