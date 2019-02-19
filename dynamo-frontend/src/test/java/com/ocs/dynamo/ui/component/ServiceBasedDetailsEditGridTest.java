package com.ocs.dynamo.ui.component;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.ocs.dynamo.ui.composite.dialog.ModelBasedSearchDialog;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.provider.IdBasedDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.UI;

public class ServiceBasedDetailsEditGridTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private UI ui;

	private TestEntity e1;

	private TestEntity e2;

	private TestEntity2 parent;

	@Mock
	private TestEntityService service;

	@Override
	public void setUp() {
		super.setUp();
		e1 = new TestEntity(1, "Kevin", 12L);
		e1.setId(1);
		e2 = new TestEntity(2, "Bob", 14L);
		e2.setId(2);
		parent = new TestEntity2();
	}

	/**
	 * Test a grid in editable mode
	 */
	@Test
	public void testEditable() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

		ServiceBasedDetailsEditGrid<Integer, TestEntity, Integer, TestEntity2> grid = createGrid(em,
				em.getAttributeModel("testEntities"), false, false, new FormOptions().setShowRemoveButton(true));
		Assert.assertTrue(grid.getAddButton().isVisible());
		Assert.assertFalse(grid.getSearchDialogButton().isVisible());

		grid.setValue(parent);

	}

	/**
	 * Test read only with search functionality
	 */
	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testReadOnlyWithSearch() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

		ServiceBasedDetailsEditGrid<Integer, TestEntity, Integer, TestEntity2> grid = createGrid(em, null, false, true,
				new FormOptions().setDetailsGridSearchMode(true));
		grid.setService(service);

		IdBasedDataProvider<Integer, TestEntity> provider = (IdBasedDataProvider<Integer, TestEntity>) grid.getGrid()
				.getDataProvider();
		Assert.assertEquals(0, provider.getSize());

		// adding is not possible
		Assert.assertFalse(grid.getAddButton().isVisible());
		// but bringing up the search dialog is
		Assert.assertTrue(grid.getSearchDialogButton().isVisible());

		grid.getSearchDialogButton().click();
		ArgumentCaptor<ModelBasedSearchDialog> captor = ArgumentCaptor.forClass(ModelBasedSearchDialog.class);
		Mockito.verify(ui).addWindow(captor.capture());

		ModelBasedSearchDialog dialog = captor.getValue();

		// select item and close dialog
		dialog.select(e1);
		dialog.getOkButton().click();

	}

	@Test
	public void testReadOnly() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

		ServiceBasedDetailsEditGrid<Integer, TestEntity, Integer, TestEntity2> grid = createGrid(em,
				em.getAttributeModel("testEntities"), true, false, new FormOptions());
		Assert.assertFalse(grid.getAddButton().isVisible());
		Assert.assertFalse(grid.getSearchDialogButton().isVisible());
	}

	private ServiceBasedDetailsEditGrid<Integer, TestEntity, Integer, TestEntity2> createGrid(
			EntityModel<TestEntity> em, AttributeModel am, boolean viewMode, boolean readOnly, FormOptions fo) {

		if (readOnly) {
			fo.setReadOnly(true);
		}

		ServiceBasedDetailsEditGrid<Integer, TestEntity, Integer, TestEntity2> table = new ServiceBasedDetailsEditGrid<Integer, TestEntity, Integer, TestEntity2>(
				service, em, am, viewMode, fo);

		table.setCreateEntitySupplier(() -> new TestEntity());
		MockUtil.injectUI(table, ui);
		table.initContent();

		return table;
	}
}
