package com.ocs.dynamo.ui.component;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.comparator.AttributeComparator;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.ocs.dynamo.ui.component.DetailsEditGrid;
import com.ocs.dynamo.ui.composite.dialog.ModelBasedSearchDialog;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.vaadin.ui.UI;

public class DetailsEditGridTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private UI ui;

	private TestEntity e1;

	private TestEntity e2;

	@Mock
	private TestEntityService service;

	@Override
	public void setUp() {
		super.setUp();
		e1 = new TestEntity(1, "Kevin", 12L);
		e2 = new TestEntity(2, "Bob", 14L);
	}

	/**
	 * Test a grid in editable mode
	 */
	@Test
	public void testEditable() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

		DetailsEditGrid<Integer, TestEntity> grid = createGrid(em, em.getAttributeModel("testEntities"), false, false,
				new FormOptions().setShowRemoveButton(true));
		Assert.assertTrue(grid.getAddButton().isVisible());
		Assert.assertFalse(grid.getSearchDialogButton().isVisible());

		grid.setValue(Lists.newArrayList(e1, e2));

		Assert.assertEquals(2, grid.getItemCount());

		// test that the add button will add a row
		grid.getAddButton().click();
		Assert.assertEquals(3, grid.getItemCount());

		// explicitly set field value
		grid.setValue(Lists.newArrayList(e1));
		Assert.assertEquals(1, grid.getItemCount());
	}

	/**
	 * Test read only with search functionality
	 */
	@Test
	@SuppressWarnings("rawtypes")
	public void testReadOnlyWithSearch() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

		DetailsEditGrid<Integer, TestEntity> table = createGrid(em, null, false, true,
				new FormOptions().setDetailsGridSearchMode(true));
		table.setService(service);

		// adding is not possible
		Assert.assertFalse(table.getAddButton().isVisible());
		// but bringing up the search dialog is
		Assert.assertTrue(table.getSearchDialogButton().isVisible());

		table.getSearchDialogButton().click();
		ArgumentCaptor<ModelBasedSearchDialog> captor = ArgumentCaptor.forClass(ModelBasedSearchDialog.class);
		Mockito.verify(ui).addWindow(captor.capture());
	}

	@Test
	public void testReadOnly() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

		DetailsEditGrid<Integer, TestEntity> grid = createGrid(em, em.getAttributeModel("testEntities"), true, false,
				new FormOptions());
		Assert.assertFalse(grid.getAddButton().isVisible());
		Assert.assertFalse(grid.getSearchDialogButton().isVisible());

		grid.setValue(Lists.newArrayList(e1, e2));
		Assert.assertEquals(2, grid.getItemCount());
	}

	private DetailsEditGrid<Integer, TestEntity> createGrid(EntityModel<TestEntity> em, AttributeModel am,
			boolean viewMode, boolean tableReadOnly, FormOptions fo) {

		if (tableReadOnly) {
			fo.setReadOnly(true);
		}

		DetailsEditGrid<Integer, TestEntity> table = new DetailsEditGrid<Integer, TestEntity>(em, am, viewMode, fo) {

			private static final long serialVersionUID = -4333833542380882076L;

			@Override
			protected void removeEntity(TestEntity toRemove) {
				// not needed
			}

			@Override
			protected TestEntity createEntity() {
				return new TestEntity();
			}
		};
		MockUtil.injectUI(table, ui);
		table.initContent();
		table.setComparator(new AttributeComparator<>("name"));
		return table;
	}
}
