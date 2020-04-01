package com.ocs.dynamo.ui.composite.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Comparator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.composite.layout.DetailsEditLayout.FormContainer;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

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

		DetailsEditLayout<Integer, TestEntity> layout = createLayout(em, em.getAttributeModel("testEntities"), false,
				new FormOptions().setShowRemoveButton(true));
		assertTrue(layout.getAddButton().isVisible());
		assertTrue(buttonBarPostconstruct);

		layout.setValue(Lists.newArrayList(e1, e2));
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

		DetailsEditLayout<Integer, TestEntity> layout = createLayout(em, em.getAttributeModel("testEntities"), true,
				new FormOptions().setDetailsGridSearchMode(true));

		// adding is not possible
		assertFalse(layout.getAddButton().isVisible());
	}

	private DetailsEditLayout<Integer, TestEntity> createLayout(EntityModel<TestEntity> em, AttributeModel am,
			boolean viewMode, FormOptions fo) {

		DetailsEditLayout<Integer, TestEntity> layout = new DetailsEditLayout<Integer, TestEntity>(service, em, am,
				viewMode, fo, Comparator.comparing(TestEntity::getName)) {

			private static final long serialVersionUID = -4333833542380882076L;

			@Override
			protected void postProcessButtonBar(FlexLayout buttonBar) {
				buttonBarPostconstruct = true;
			}

			@Override
			protected void postProcessDetailButtonBar(int index, HorizontalLayout buttonBar, boolean viewMode) {
				detailButtonBarPostconstruct = true;
			}
		};
		layout.setCreateEntitySupplier(p -> new TestEntity());
		layout.setRemoveEntityConsumer((p, t) -> {
		});

		layout.initContent();
		return layout;
	}
}
