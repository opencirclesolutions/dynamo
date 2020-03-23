package com.ocs.dynamo.ui.composite.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.type.AttributeGroupMode;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.function.SerializablePredicate;

public class SimpleEditLayoutTest extends FrontendIntegrationTest {

    @Inject
    private EntityModelFactory entityModelFactory;

    @Inject
    private TestEntityService testEntityService;

    private TestEntity e1;

    private TestEntity e2;

    private boolean modeChanged;

    @BeforeEach
    public void setup() {
        MockVaadin.setup();
        e1 = new TestEntity("Bob", 11L);
        e1 = testEntityService.save(e1);

        e2 = new TestEntity("Harry", 12L);
        e2 = testEntityService.save(e2);
    }

    @Test
    public void testSimpleEditLayout() {
        SimpleEditLayout<Integer, TestEntity> layout = createLayout(e1, "TestEntity", new FormOptions());

        Map<String, SerializablePredicate<?>> ff = new HashMap<>();
        ff.put("testEntities", new EqualsPredicate<TestEntity>("name", "Harry"));

        layout.setFieldFilters(ff);
        layout.build();

        assertNotNull(layout.getEditForm());

        assertEquals(1, layout.getFieldFilters().size());
        assertEquals("Bob", ((HasValue<?, ?>) layout.getEditForm().getField("name")).getValue());

        // check that the screen is not in view mode
        assertFalse(layout.isViewMode());
        assertTrue(layout.getEditForm().getSaveButtons().get(0).isVisible());

        layout.setEntity(e2);
        assertEquals("Harry", ((HasValue<?, ?>) layout.getEditForm().getField("name")).getValue());

    }

    /**
     * Test opening the screen in view mode
     */
    @Test
    public void testSimpleEditLayout_ViewMode() {
        SimpleEditLayout<Integer, TestEntity> layout = createLayout(e1, "TestEntity",
                new FormOptions().setOpenInViewMode(true).setEditAllowed(true));
        layout.build();

        assertNotNull(layout.getEditForm());

        // check that the screen is in view mode and no save buttons are visible
        assertTrue(layout.isViewMode());
        assertTrue(layout.getEditForm().getSaveButtons().isEmpty());

        modeChanged = false;

        // click the edit button
        assertFalse(layout.getEditForm().getCancelButtons().get(0).isVisible());
        assertTrue(layout.getEditForm().getEditButtons().get(0).isVisible());
        assertFalse(layout.getEditForm().getBackButtons().get(0).isVisible());
        layout.getEditForm().getEditButtons().get(0).click();

        // check that we are now in edit mode
        assertFalse(layout.isViewMode());
        assertTrue(layout.getEditForm().getCancelButtons().get(0).isVisible());
        assertFalse(layout.getEditForm().getEditButtons().get(0).isVisible());
        assertTrue(modeChanged);

        // back to view mode
        layout.getEditForm().getCancelButtons().get(0).click();
        assertTrue(layout.isViewMode());

        layout.getEditForm().getEditButtons().get(0).click();
        assertFalse(layout.isViewMode());

        // after a reload we go back to the view mode
        layout.reload();
        assertTrue(layout.isViewMode());
    }

    /**
     * Test the creation of a tab layout
     */
    @Test
    public void testSimpleEditLayout_TabLayout() {
        SimpleEditLayout<Integer, TestEntity> layout = createLayout(e1, "TestEntityGroups",
                new FormOptions().setEditAllowed(true).setAttributeGroupMode(AttributeGroupMode.TABSHEET));
        layout.build();

        // try hiding an attribute group
        assertTrue(layout.getEditForm().isAttributeGroupVisible("testentity.group.1"));
        layout.getEditForm().setAttributeGroupVisible("testentity.group.1", false);
        assertFalse(layout.getEditForm().isAttributeGroupVisible("testentity.group.1"));

        assertTrue(layout.getEditForm().isAttributeGroupVisible("ocs.default.attribute.group"));
    }

    /**
     * Test the creation of a layout with multiple panels
     */
    @Test
    public void testSimpleEditLayout_PanelLayout() {
        SimpleEditLayout<Integer, TestEntity> layout = createLayout(e1, "TestEntityGroups",
                new FormOptions().setEditAllowed(true).setAttributeGroupMode(AttributeGroupMode.PANEL));
        layout.build();

        // try hiding an attribute group
        assertTrue(layout.getEditForm().isAttributeGroupVisible("testentity.group.1"));
        layout.getEditForm().setAttributeGroupVisible("testentity.group.1", false);
        assertFalse(layout.getEditForm().isAttributeGroupVisible("testentity.group.1"));

        assertTrue(layout.getEditForm().isAttributeGroupVisible("ocs.default.attribute.group"));
    }

    /**
     * Test that attribute are grouped together on the same line
     */
//	@Test
//	public void testSimpleEditLayout_GroupAttributesTogether() {
//		SimpleEditLayout<Integer, TestEntity> layout = createLayout(e1, "TestEntityGroupTogether",
//				new FormOptions().setEditAllowed(true).setAttributeGroupMode(AttributeGroupMode.PANEL));
//		layout.build();
//
//		// check that the "age" field is the first component on a row (followed by the
//		// "name" field)
//		Component field = layout.getEditForm().getField("age");
//		HasComponents hc = field.getParent();
//		assertTrue(hc instanceof FormLayout);
//		FormLayout fl = (FormLayout) hc;
//		assertEquals(DynamoConstants.CSS_FIRST, fl.getStyleName());
//		HasComponents horizontal = fl.getParent();
//		assertTrue(horizontal instanceof HorizontalLayout);
//
//		// check that the "name" field is properly nested
//		field = layout.getEditForm().getField("name");
//		hc = field.getParent();
//		assertTrue(hc instanceof FormLayout);
//		fl = (FormLayout) hc;
//		assertEquals(DynamoConstants.CSS_ADDITIONAL, fl.getStyleName());
//		horizontal = fl.getParent();
//		assertTrue(horizontal instanceof HorizontalLayout);
//	}

    private SimpleEditLayout<Integer, TestEntity> createLayout(TestEntity entity, String reference, FormOptions fo) {
        return new SimpleEditLayout<Integer, TestEntity>(entity, testEntityService,
                entityModelFactory.getModel(reference, TestEntity.class), fo) {

            private static final long serialVersionUID = 4568283356505463568L;

            @Override
            protected void afterModeChanged(boolean viewMode, ModelBasedEditForm<Integer, TestEntity> editForm) {
                modeChanged = true;
            }
        };

    }
}
