package com.ocs.dynamo.ui.composite.layout;

import java.util.Collection;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.TestEntity2Service;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;

public class FixedSplitLayoutTest extends BaseIntegrationTest {

	@Inject
	private EntityModelFactory entityModelFactory;

	private TestEntity e1;

	private TestEntity e2;

	private TestEntity2 child1;

	private TestEntity2 child2;

	@Inject
	private TestEntityService testEntityService;

	@Inject
	private TestEntity2Service testEntity2Service;

	@Before
	public void setup() {
		e1 = new TestEntity("Bob", 11L);
		e1 = testEntityService.save(e1);

		e2 = new TestEntity("Harry", 12L);
		e2 = testEntityService.save(e2);

		child1 = new TestEntity2();
		child1.setTestEntity(e1);
		child1 = testEntity2Service.save(child1);

		child2 = new TestEntity2();
		child2.setTestEntity(e2);
		child2 = testEntity2Service.save(child2);
	}

	@Test
	public void testCreateInEditMode() {
		FormOptions fo = new FormOptions();
		FixedSplitLayout<Integer, TestEntity> layout = new FixedSplitLayout<Integer, TestEntity>(testEntityService,
		        entityModelFactory.getModel(TestEntity.class), fo, new SortOrder("name", SortDirection.ASCENDING)) {

			private static final long serialVersionUID = 6308563510081372500L;

			@Override
			protected Collection<TestEntity> loadItems() {
				return Lists.newArrayList(e1, e2);
			}
		};
		layout.build();

		// layout must contain 2 items
		Assert.assertEquals(2, layout.getTableWrapper().getTable().getContainerDataSource().size());

		// select an item and check that the edit form is generated
		layout.getTableWrapper().getTable().select(e1);
		Assert.assertEquals(e1, layout.getSelectedItem());
		Assert.assertNotNull(layout.getEditForm());
		Assert.assertFalse(layout.getEditForm().isViewMode());

		// no quick search field for this component
		Assert.assertNull(layout.getQuickSearchField());

		layout.reload();
	}

	/**
	 * Test the creation of a detail layout
	 */
	@Test
	public void testCreateDetailLayout() {
		FormOptions fo = new FormOptions();
		FixedDetailLayout<Integer, TestEntity2, Integer, TestEntity> layout = new FixedDetailLayout<Integer, TestEntity2, Integer, TestEntity>(
		        testEntity2Service, e1, testEntityService, entityModelFactory.getModel(TestEntity2.class), fo, null) {

			private static final long serialVersionUID = 7009824287226683886L;

			@Override
			protected Collection<TestEntity2> loadItems() {
				return Lists.newArrayList(child1);
			}
		};

		layout.build();

		Assert.assertEquals(1, layout.getTableWrapper().getTable().size());
		Assert.assertEquals(e1, layout.getParentEntity());

		layout.reload();
	}

}