package com.ocs.dynamo.ui.composite.table;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
import com.ocs.dynamo.ui.container.hierarchical.HierarchicalFetchJoinInformation;
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
		entity = new TestEntity("Bob", 45L);

		TestEntity2 child1 = new TestEntity2();
		TestEntity2 child2 = new TestEntity2();
		entity.addTestEntity2(child1);
		entity.addTestEntity2(child2);
		entity = testEntityService.save(entity);
	}

	@Test
	public void testCreateModelBasedHierarchicalContainer() {
		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);
		List<BaseService<?, ?>> services = new ArrayList<>();

		services.add(testEntityService);
		services.add(testEntity2Service);

		ModelBasedHierarchicalContainer<TestEntity> container = new ModelBasedHierarchicalContainer<TestEntity>(
				messageService, model, services, null);

		ModelBasedTreeTable<Integer, TestEntity> table = new ModelBasedTreeTable<Integer, TestEntity>(
				container, entityModelFactory);

		Assert.assertEquals(1, table.getContainerDataSource().size());

		// check that properties are properly created
		String name = (String) table.getItem(table.getItemIds().iterator().next())
				.getItemProperty("name").getValue();
		Assert.assertEquals("Bob", name);
		Assert.assertEquals(3, table.getVisibleColumns().length);
	}

	@Test
	public void testCreateServiceResultsTreeTableWrapper() {
		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);
		List<BaseService<?, ?>> services = new ArrayList<>();

		services.add(testEntityService);
		services.add(testEntity2Service);

		ServiceResultsTreeTableWrapper<Integer, TestEntity> wrapper = new ServiceResultsTreeTableWrapper<>(
				services, model, QueryType.PAGING, null, new HierarchicalFetchJoinInformation[0]);
		wrapper.build();

		Assert.assertNotNull(wrapper.getContainer());
		Assert.assertNotNull(wrapper.getTable());
		Assert.assertNull(wrapper.getTable().getSortContainerPropertyId());

		Assert.assertEquals(1, wrapper.getContainer().size());
	}

	@Test
	public void testCreateServiceResultsTreeTableWrapper_Sorting() {
		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);
		List<BaseService<?, ?>> services = new ArrayList<>();

		services.add(testEntityService);
		services.add(testEntity2Service);

		ServiceResultsTreeTableWrapper<Integer, TestEntity> wrapper = new ServiceResultsTreeTableWrapper<>(
				services, model, QueryType.PAGING, new SortOrder("name", SortDirection.ASCENDING),
				new HierarchicalFetchJoinInformation[0]);
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
