package com.ocs.dynamo.ui.composite.layout;

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
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.shared.data.sort.SortDirection;

public class ServiceBasedSplitLayoutTest extends BaseIntegrationTest {

	@Inject
	private EntityModelFactory entityModelFactory;

	@Inject
	private TestEntityService testEntityService;

	@Inject
	private TestEntity2Service testEntity2Service;

	private TestEntity e1;

	private TestEntity e2;

	private TestEntity2 child1;

	private TestEntity2 child2;

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
		FormOptions fo = new FormOptions().setShowQuickSearchField(true);
		ServiceBasedSplitLayout<Integer, TestEntity> layout = new ServiceBasedSplitLayout<Integer, TestEntity>(
		        testEntityService, entityModelFactory.getModel(TestEntity.class), fo, new SortOrder("name",
		                SortDirection.ASCENDING)) {

			private static final long serialVersionUID = 6308563510081372500L;

			@Override
			protected Filter constructQuickSearchFilter(String value) {
				return new Compare.Equal("name", "%" + value + "%");
			}
		};
		layout.buildFilter();
		layout.build();

		Assert.assertNull(layout.getFilter());

		// no filter, so all items are visible
		Assert.assertEquals(2, layout.getContainer().size());

		// select an item and check that the edit form is generated
		layout.getTableWrapper().getTable().select(e1.getId());
		Assert.assertNotNull(layout.getEditForm());
		Assert.assertFalse(layout.getEditForm().isViewMode());

		// check that a quick search field is created
		Assert.assertNotNull(layout.getQuickSearchField());

		// test selection
		layout.setSelectedItems(e1.getId());
		Assert.assertEquals(e1, layout.getSelectedItem());

		layout.setSelectedItems(Lists.newArrayList(e2.getId()));
		Assert.assertEquals(e2, layout.getSelectedItem());

		layout.setSelectedItem(null);
		Assert.assertNull(layout.getSelectedItem());
	}

	@Test
	public void testCreateWithFilter() {
		FormOptions fo = new FormOptions();
		ServiceBasedSplitLayout<Integer, TestEntity> layout = new ServiceBasedSplitLayout<Integer, TestEntity>(
		        testEntityService, entityModelFactory.getModel(TestEntity.class), fo, new SortOrder("name",
		                SortDirection.ASCENDING)) {

			private static final long serialVersionUID = 6308563510081372500L;

			@Override
			protected Filter constructFilter() {
				return new Compare.Equal("name", "Bob");
			}
		};
		layout.buildFilter();
		layout.build();

		// search results contain only "Bob"
		Assert.assertEquals(1, layout.getContainer().size());

		// no quick search field this time
		Assert.assertNull(layout.getQuickSearchField());
	}

	@Test
	public void testCreateInViewMode() {
		FormOptions fo = new FormOptions().setOpenInViewMode(true);
		ServiceBasedSplitLayout<Integer, TestEntity> layout = new ServiceBasedSplitLayout<Integer, TestEntity>(
		        testEntityService, entityModelFactory.getModel(TestEntity.class), fo, new SortOrder("name",
		                SortDirection.ASCENDING));
		layout.buildFilter();
		layout.build();

		// select an item and check that the edit form is generated (in view mode)
		layout.getTableWrapper().getTable().select(e1.getId());
		Assert.assertNotNull(layout.getEditForm());
		Assert.assertTrue(layout.getEditForm().isViewMode());
	}

	/**
	 * Test the creation of a detail layout
	 */
	@Test
	public void testCreateDetailLayout() {
		FormOptions fo = new FormOptions();
		ServiceBasedDetailLayout<Integer, TestEntity2, Integer, TestEntity> layout = new ServiceBasedDetailLayout<Integer, TestEntity2, Integer, TestEntity>(
		        testEntity2Service, e1, testEntityService, entityModelFactory.getModel(TestEntity2.class), fo, null) {

			private static final long serialVersionUID = 7009824287226683886L;

			protected Filter constructFilter() {
				return new Compare.Equal("testEntity", getParentEntity());
			}
		};

		layout.build();

		Assert.assertEquals(1, layout.getTableWrapper().getTable().size());
		Assert.assertEquals(e1, layout.getParentEntity());
		Assert.assertNotNull(layout.getFilter());
		
		layout.reload();
	}
}
