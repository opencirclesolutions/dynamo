package nl.ocs.ui.composite.table;

import javax.inject.Inject;

import nl.ocs.dao.query.FetchJoinInformation;
import nl.ocs.domain.TestEntity;
import nl.ocs.domain.model.EntityModel;
import nl.ocs.domain.model.EntityModelFactory;
import nl.ocs.service.MessageService;
import nl.ocs.service.TestEntityService;
import nl.ocs.test.BaseIntegrationTest;
import nl.ocs.ui.container.QueryType;
import nl.ocs.ui.container.ServiceContainer;
import nl.ocs.ui.utils.VaadinUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.shared.data.sort.SortDirection;

public class ModelBasedTableIntegrationTest extends BaseIntegrationTest {

	@Inject
	private TestEntityService testEntityService;

	@Inject
	private EntityModelFactory entityModelFactory;

	@Inject
	private MessageService messageService;

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
				false, 20, QueryType.ID_BASED, new FetchJoinInformation[0]);
		Assert.assertNotNull(container.getService());

		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);
		ModelBasedTable<Integer, TestEntity> table = new ModelBasedTable<>(container, model,
				entityModelFactory, messageService);

		Assert.assertEquals(13, table.getVisibleColumns().length);

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

		ServiceResultsTableWrapper<Integer, TestEntity> wrapper = new ServiceResultsTableWrapper<>(
				testEntityService, model, QueryType.ID_BASED, null, null, null);
		wrapper.build();

		Assert.assertNotNull(wrapper.getTable());
		Assert.assertNull(wrapper.getSortOrder());
		Assert.assertNull(wrapper.getJoins());
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

		ServiceResultsTableWrapper<Integer, TestEntity> wrapper = new ServiceResultsTableWrapper<>(
				testEntityService, model, QueryType.ID_BASED, null, new SortOrder("name",
						SortDirection.ASCENDING), null);
		wrapper.build();

		Assert.assertNotNull(wrapper.getTable());
		Assert.assertEquals(new SortOrder("name", SortDirection.ASCENDING), wrapper.getSortOrder());
		Assert.assertNull(wrapper.getJoins());
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
				false, 20, QueryType.PAGING, null);
		Assert.assertNotNull(container.getService());

		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);
		ModelBasedTable<Integer, TestEntity> table = new ModelBasedTable<>(container, model,
				entityModelFactory, messageService);

		Assert.assertEquals(13, table.getVisibleColumns().length);

		// items can be retrieved by their primary key
		Item item = table.getItem(entity.getId());
		Assert.assertNotNull(item);

		Assert.assertEquals(1, table.getContainer().size());

		Assert.assertEquals("Bob", item.getItemProperty("name").getValue());
	}

	@Test
	public void testSaveNewEntity() {
		ServiceContainer<Integer, TestEntity> container = new ServiceContainer<>(testEntityService,
				false, 20, QueryType.PAGING, null);

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
				false, 20, QueryType.PAGING, null);

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
				false, 20, QueryType.PAGING, null);
		container.getQueryView().getQueryDefinition().addFilter(new Compare.Equal("name", "Hank"));

		Assert.assertEquals(1, testEntityService.findAll().size());
		Assert.assertEquals(0, container.size());
	}

	@Test
	public void testSort() {
		ServiceContainer<Integer, TestEntity> container = new ServiceContainer<>(testEntityService,
				entityModelFactory, false, 20, QueryType.PAGING, null);

		TestEntity entity2 = new TestEntity("Kevin", 74L);
		testEntityService.save(entity2);

		Assert.assertEquals(2, container.size());

		container.sort(new SortOrder("name", SortDirection.DESCENDING));
		Assert.assertEquals("Kevin",
				container.getItem(container.getIdByIndex(0)).getItemProperty("name").getValue());
		Assert.assertEquals("Bob",
				container.getItem(container.getIdByIndex(1)).getItemProperty("name").getValue());

		container.sort(new SortOrder("name", SortDirection.ASCENDING));
		Assert.assertEquals("Bob",
				container.getItem(container.getIdByIndex(0)).getItemProperty("name").getValue());
		Assert.assertEquals("Kevin",
				container.getItem(container.getIdByIndex(1)).getItemProperty("name").getValue());
	}
}
