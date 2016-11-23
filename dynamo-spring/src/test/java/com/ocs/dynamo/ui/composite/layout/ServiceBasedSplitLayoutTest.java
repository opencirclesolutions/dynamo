package com.ocs.dynamo.ui.composite.layout;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
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

	private TestEntity e1;

	private TestEntity e2;

	@Before
	public void setup() {
		e1 = new TestEntity("Bob", 11L);
		e1 = testEntityService.save(e1);

		e2 = new TestEntity("Harry", 12L);
		e2 = testEntityService.save(e2);
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

		// no filter, so all items are visible
		Assert.assertEquals(2, layout.getContainer().size());

		// select an item and check that the edit form is generated
		layout.getTableWrapper().getTable().select(e1.getId());
		Assert.assertNotNull(layout.getEditForm());
		Assert.assertFalse(layout.getEditForm().isViewMode());

		// check that a quick search field is created
		Assert.assertNotNull(layout.getQuickSearchField());

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

}
