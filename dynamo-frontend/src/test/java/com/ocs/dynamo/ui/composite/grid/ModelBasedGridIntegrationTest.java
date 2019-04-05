package com.ocs.dynamo.ui.composite.grid;

import java.util.HashMap;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.type.GridEditMode;
import com.ocs.dynamo.ui.provider.IdBasedDataProvider;
import com.ocs.dynamo.ui.provider.PagingDataProvider;
import com.ocs.dynamo.ui.provider.QueryType;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.data.sort.SortDirection;

public class ModelBasedGridIntegrationTest extends FrontendIntegrationTest {

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
		IdBasedDataProvider<Integer, TestEntity> provider = new IdBasedDataProvider<>(testEntityService,
				entityModelFactory.getModel(TestEntity.class));

		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);
		ModelBasedGrid<Integer, TestEntity> grid = new ModelBasedGrid<>(provider, model,
				new HashMap<String, SerializablePredicate<?>>(), false, GridEditMode.SINGLE_ROW);

		Assert.assertEquals(17, grid.getColumns().size());
		Assert.assertNotNull(grid.getDataProvider().getId(entity));
	}

	@Test
	public void testWrapperIdBasedQuery() {

		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);

		ServiceBasedGridWrapper<Integer, TestEntity> wrapper = new ServiceBasedGridWrapper<>(testEntityService, model,
				QueryType.ID_BASED, new FormOptions(), null, new HashMap<String, SerializablePredicate<?>>(), null,
				false);
		wrapper.build();

		Assert.assertNotNull(wrapper.getGrid());
		Assert.assertEquals(0, wrapper.getSortOrders().size());
		Assert.assertEquals(0, wrapper.getJoins().length);
		DataProvider<TestEntity, SerializablePredicate<TestEntity>> provider = wrapper.getDataProvider();
		Assert.assertNotNull(provider);

		wrapper.getGrid().getDataCommunicator().getDataProviderSize();
		Assert.assertEquals(1, wrapper.getDataProviderSize());

		// add an entity and refresh the container - check that the new item
		// shows up
		TestEntity t2 = new TestEntity("Pete", 55L);
		testEntityService.save(t2);

		wrapper.reloadDataProvider();
		wrapper.getGrid().getDataCommunicator().getDataProviderSize();
		Assert.assertEquals(2, wrapper.getDataProviderSize());

		wrapper.search(new EqualsPredicate<TestEntity>("name", "John"));
		wrapper.getGrid().getDataCommunicator().getDataProviderSize();
		Assert.assertEquals(0, wrapper.getDataProviderSize());
	}

	@Test
	public void testWrapperIdBasedQuery_SortOrder() {
		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);

		ServiceBasedGridWrapper<Integer, TestEntity> wrapper = new ServiceBasedGridWrapper<>(testEntityService, model,
				QueryType.ID_BASED, new FormOptions(), null, new HashMap<String, SerializablePredicate<?>>(),
				Lists.newArrayList(new SortOrder<String>("name", SortDirection.ASCENDING)), false);
		wrapper.build();

		Assert.assertNotNull(wrapper.getGrid());
		Assert.assertEquals("name", wrapper.getSortOrders().get(0).getSorted());
		Assert.assertEquals(0, wrapper.getJoins().length);

	}

	/**
	 * Test the working of a model based table combined with a service container
	 * using a paging query
	 */
	@Test
	public void testPagingQuery() {
		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);
		ServiceBasedGridWrapper<Integer, TestEntity> wrapper = new ServiceBasedGridWrapper<>(testEntityService, model,
				QueryType.PAGING, new FormOptions(), null, new HashMap<String, SerializablePredicate<?>>(),
				Lists.newArrayList(new SortOrder<String>("name", SortDirection.ASCENDING)), false);
		wrapper.build();

		Assert.assertTrue(wrapper.getDataProvider() instanceof PagingDataProvider);
		Assert.assertEquals(17, wrapper.getGrid().getColumns().size());

	}

}
