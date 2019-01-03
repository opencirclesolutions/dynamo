package com.ocs.dynamo.ui.composite.layout;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.TestEntity2Service;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.ocs.dynamo.ui.provider.QueryType;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.TextField;

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

	private boolean modeChanged = false;

	private boolean entitySelected = false;

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
	public void testCreateTableDividers() {
		FormOptions fo = new FormOptions().setShowQuickSearchField(true);
		ServiceBasedSplitLayout<Integer, TestEntity> layout = new ServiceBasedSplitLayout<Integer, TestEntity>(
				testEntityService, entityModelFactory.getModel(TestEntity.class), QueryType.PAGING, fo,
				new SortOrder<String>("name", SortDirection.ASCENDING));
		layout.setDividerProperty("name");
		layout.build();
	}

	@Test
	public void testCreateInEditMode() {
		FormOptions fo = new FormOptions().setShowQuickSearchField(true);
		ServiceBasedSplitLayout<Integer, TestEntity> layout = new ServiceBasedSplitLayout<Integer, TestEntity>(
				testEntityService, entityModelFactory.getModel(TestEntity.class), QueryType.PAGING, fo,
				new SortOrder<String>("name", SortDirection.ASCENDING)) {

			private static final long serialVersionUID = 6308563510081372500L;

			@Override
			protected SerializablePredicate<TestEntity> constructQuickSearchFilter(String value) {
				return new EqualsPredicate<TestEntity>("name", "%" + value + "%");
			}
		};
		layout.build();

		Assert.assertNull(layout.getFilter());

		// no filter, so all items are visible
		// Assert.assertEquals(2, layout.getDataProviderSize());

		// select an item and check that the edit form is generated
		layout.getGridWrapper().getGrid().select(e1);
		Assert.assertNotNull(layout.getEditForm());
		Assert.assertFalse(layout.getEditForm().isViewMode());

		// check that a quick search field is created
		Assert.assertNotNull(layout.getQuickSearchField());

		// test selection
		layout.setSelectedItems(e1);
		Assert.assertEquals(e1, layout.getSelectedItem());

		layout.setSelectedItems(Lists.newArrayList(e2));
		Assert.assertEquals(e2, layout.getSelectedItem());

		layout.setSelectedItem(null);
		Assert.assertNull(layout.getSelectedItem());
	}

	/**
	 * Test creation in vertical mode
	 */
	@Test
	public void testCreateInVerticalMode() {
		FormOptions fo = new FormOptions().setShowQuickSearchField(true).setScreenMode(ScreenMode.VERTICAL);
		ServiceBasedSplitLayout<Integer, TestEntity> layout = new ServiceBasedSplitLayout<Integer, TestEntity>(
				testEntityService, entityModelFactory.getModel(TestEntity.class), QueryType.PAGING, fo,
				new SortOrder<String>("name", SortDirection.ASCENDING));
		layout.build();

		// test selection
		layout.getGridWrapper().getGrid().select(e1);
		Assert.assertEquals(e1, layout.getSelectedItem());

		// try saving
		TextField tf = (TextField) layout.getEditForm().getField("name");
		tf.setValue("NewName");
		layout.getEditForm().getSaveButtons().get(0).click();

		Assert.assertEquals("NewName", e1.getName());

	}

	@Test
	public void testCreateWithFilter() {
		FormOptions fo = new FormOptions();
		ServiceBasedSplitLayout<Integer, TestEntity> layout = new ServiceBasedSplitLayout<Integer, TestEntity>(
				testEntityService, entityModelFactory.getModel(TestEntity.class), QueryType.PAGING, fo,
				new SortOrder<String>("name", SortDirection.ASCENDING));
		layout.setFilterSupplier(() -> new EqualsPredicate<TestEntity>("name", "Bob"));
		layout.build();

		Assert.assertNotNull(layout.getFilter());

		// no quick search field this time
		Assert.assertNull(layout.getQuickSearchField());
	}

	@Test
	public void testCreateInViewMode() {
		FormOptions fo = new FormOptions().setOpenInViewMode(true);
		ServiceBasedSplitLayout<Integer, TestEntity> layout = new ServiceBasedSplitLayout<Integer, TestEntity>(
				testEntityService, entityModelFactory.getModel(TestEntity.class), QueryType.PAGING, fo,
				new SortOrder<String>("name", SortDirection.ASCENDING)) {

			private static final long serialVersionUID = 7323448065402690401L;

			@Override
			protected void afterModeChanged(boolean viewMode, ModelBasedEditForm<Integer, TestEntity> editForm) {
				modeChanged = true;
			}

			@Override
			protected void afterEntitySelected(ModelBasedEditForm<Integer, TestEntity> editForm, TestEntity entity) {
				entitySelected = true;
			}

		};
		layout.buildFilter();
		layout.build();

		// select an item and check that the edit form is generated (in view
		// mode)
		layout.getGridWrapper().getGrid().select(e1);
		Assert.assertNotNull(layout.getEditForm());
		Assert.assertTrue(layout.getEditForm().isViewMode());

		// change into edit mode
		layout.getEditForm().getEditButtons().get(0).click();
		Assert.assertFalse(layout.getEditForm().isViewMode());
		Assert.assertTrue(modeChanged);
		Assert.assertTrue(entitySelected);
	}

	/**
	 * Test the creation of a detail layout
	 */
	@Test
	public void testCreateDetailLayout() {
		FormOptions fo = new FormOptions();
		ServiceBasedDetailLayout<Integer, TestEntity2, Integer, TestEntity> layout = new ServiceBasedDetailLayout<Integer, TestEntity2, Integer, TestEntity>(
				testEntity2Service, e1, testEntityService, entityModelFactory.getModel(TestEntity2.class),
				QueryType.ID_BASED, fo, null);

		layout.setParentFilterSupplier(parent -> new EqualsPredicate<TestEntity2>("testEntity", parent));
		layout.build();

		Assert.assertEquals(1, layout.getGridWrapper().getDataProviderSize());
		Assert.assertEquals(e1, layout.getParentEntity());
		Assert.assertNotNull(layout.getFilter());

		layout.reload();
	}
}
