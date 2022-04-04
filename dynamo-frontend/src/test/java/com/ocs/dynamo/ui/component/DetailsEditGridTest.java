package com.ocs.dynamo.ui.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.comparator.AttributeComparator;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.TestEntity2Service;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.provider.ListDataProvider;

public class DetailsEditGridTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private UI ui;

	private TestEntity e1;

	private TestEntity e2;

	private TestEntity2 t1;

	@Mock
	private TestEntityService service;

	@Mock
	private TestEntity2Service service2;

	@BeforeEach
	public void setUp() {
		MockVaadin.setup();

		e1 = new TestEntity(1, "Kevin", 12L);
		e1.setId(1);
		e2 = new TestEntity(2, "Bob", 14L);
		e2.setId(2);

		t1 = new TestEntity2();
	}

	/**
	 * Test a grid in editable mode
	 */
	@Test
	public void testEditable() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

		DetailsEditGrid<Integer, TestEntity> grid = createGrid(em, em.getAttributeModel("testEntities"), false, false,
				new FormOptions().setShowRemoveButton(true).setShowEditButton(true));
		assertTrue(grid.getAddButton().isVisible());
		assertFalse(grid.getSearchDialogButton().isVisible());

		grid.setValue(Lists.newArrayList(e1, e2));

		assertEquals(2, grid.getItemCount());

		// test that the add button will add a row
		grid.getAddButton().click();
		assertEquals(3, grid.getItemCount());

		// explicitly set field value
		grid.setValue(Lists.newArrayList(e1));
		assertEquals(1, grid.getItemCount());
	}

	/**
	 * Test read only with search functionality
	 */
	@Test
	@SuppressWarnings({ "unchecked" })
	public void testReadOnlyWithSearch() {
		EntityModel<TestEntity2> em = factory.getModel(TestEntity2.class);
		EntityModel<TestEntity> em2 = factory.getModel(TestEntity.class);

		Mockito.when(service2.fetch(Mockito.nullable(Filter.class), Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(List.of(t1));

		DetailsEditGrid<Integer, TestEntity2> grid = createGrid2(em, em2.getAttributeModel("testEntities"), false, true,
				new FormOptions().setDetailsGridSearchMode(true));
		grid.setService(service2);

		ListDataProvider<TestEntity2> lep = (ListDataProvider<TestEntity2>) grid.getGrid().getDataProvider();
		assertEquals(0, lep.getItems().size());

		// adding is not possible
		assertFalse(grid.getAddButton().isVisible());
		// but bringing up the search dialog is
		assertTrue(grid.getSearchDialogButton().isVisible());

		grid.getSearchDialogButton().click();

		assertNotNull(grid.getSearchDialog());

		grid.getSearchDialog().select(t1);
		grid.getSearchDialog().getOkButton().click();
	}

	@Test
	public void testReadOnly() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

		DetailsEditGrid<Integer, TestEntity> grid = createGrid(em, em.getAttributeModel("testEntities"), true, false,
				new FormOptions());
		assertFalse(grid.getAddButton().isVisible());
		assertFalse(grid.getSearchDialogButton().isVisible());

		grid.setValue(Lists.newArrayList(e1, e2));
		assertEquals(2, grid.getItemCount());
	}

	private DetailsEditGrid<Integer, TestEntity> createGrid(EntityModel<TestEntity> em, AttributeModel am,
			boolean viewMode, boolean readOnly, FormOptions fo) {

		if (readOnly) {
			fo.setReadOnly(true);
		}

		DetailsEditGrid<Integer, TestEntity> grid = new DetailsEditGrid<Integer, TestEntity>(em, am, viewMode, fo);

		grid.setCreateEntity(() -> new TestEntity());
		grid.build();
		grid.setComparator(new AttributeComparator<>("name"));
		return grid;
	}

	private DetailsEditGrid<Integer, TestEntity2> createGrid2(EntityModel<TestEntity2> em, AttributeModel am,
			boolean viewMode, boolean readOnly, FormOptions fo) {

		if (readOnly) {
			fo.setReadOnly(true);
		}

		DetailsEditGrid<Integer, TestEntity2> grid = new DetailsEditGrid<Integer, TestEntity2>(em, am, viewMode, fo);

		grid.setCreateEntity(() -> new TestEntity2());
		grid.build();
		grid.setComparator(new AttributeComparator<>("name"));
		return grid;
	}
}
