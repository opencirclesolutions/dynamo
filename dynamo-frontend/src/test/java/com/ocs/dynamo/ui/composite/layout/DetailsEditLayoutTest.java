package com.ocs.dynamo.ui.composite.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.composite.layout.DetailsEditLayout.FormContainer;
import com.vaadin.flow.component.UI;

public class DetailsEditLayoutTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private UI ui;

	private TestEntity e1;

	private TestEntity e2;

	@Mock
	private TestEntityService service;

	private boolean buttonBarPostconstruct = false;

	private boolean detailButtonBarPostconstruct = false;

	@BeforeEach
	public void setUp() {
		MockVaadin.setup();
		e1 = new TestEntity(1, "Kevin", 12L);
		e2 = new TestEntity(2, "Bob", 14L);
		when(service.getEntityClass()).thenReturn(TestEntity.class);
	}

	/**
	 * Test a table in editable mode
	 */
	@Test
	public void testEditable() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

		DetailsEditLayout<Integer, TestEntity, Integer, TestEntity> layout = createLayout(em,
				em.getAttributeModel("testEntities"), false, new FormOptions().setShowRemoveButton(true));
		assertTrue(layout.getAddButton().isVisible());
		assertTrue(buttonBarPostconstruct);

		layout.setValue(List.of(e1, e2));
		assertTrue(detailButtonBarPostconstruct);

		assertEquals(2, layout.getFormCount().intValue());

		layout.getAddButton().click();
		assertEquals(3, layout.getFormCount().intValue());

		//
		layout.setDeleteEnabled(0, false);
		@SuppressWarnings("rawtypes")
		FormContainer container = layout.getFormContainer(0);
		assertFalse(container.getDeleteButton().isEnabled());

		// out of bounds
		layout.setDeleteEnabled(4, false);

		layout.setDeleteVisible(0, false);
		assertFalse(container.getDeleteButton().isVisible());

		// disable field
		layout.setFieldEnabled(0, "age", false);

		// get first entity (sorted by name, some "Bob" comes first)
		TestEntity t1 = layout.getEntity(0);
		assertEquals(e2, t1);

		assertTrue(layout.validateAllFields());
	}

	/**
	 * Test read only with search functionality
	 */
	@Test
	public void testReadOnly() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

		DetailsEditLayout<Integer, TestEntity, Integer, TestEntity> layout = createLayout(em,
				em.getAttributeModel("testEntities"), true, new FormOptions().setDetailsGridSearchMode(true));

		// adding is not possible
		assertFalse(layout.getAddButton().isVisible());
	}

	private DetailsEditLayout<Integer, TestEntity, Integer, TestEntity> createLayout(EntityModel<TestEntity> em,
			AttributeModel am, boolean viewMode, FormOptions fo) {

		DetailsEditLayout<Integer, TestEntity, Integer, TestEntity> layout = new DetailsEditLayout<>(service, em, am,
				viewMode, fo, Comparator.comparing(TestEntity::getName));
		layout.setPostProcessButtonBar(buttonBar -> buttonBarPostconstruct = true);
		layout.setPostProcessDetailButtonBar((index, buttonBar, vm) -> {
			detailButtonBarPostconstruct = true;
		});
		layout.setCreateEntity(p -> new TestEntity());
		layout.setRemoveEntity((p, t) -> {
		});

		layout.build();
		return layout;
	}
}
