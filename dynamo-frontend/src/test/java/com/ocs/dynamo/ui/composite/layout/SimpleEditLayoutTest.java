package com.ocs.dynamo.ui.composite.layout;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.type.AttributeGroupMode;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;

public class SimpleEditLayoutTest extends BaseIntegrationTest {

    @Inject
    private EntityModelFactory entityModelFactory;

    @Inject
    private TestEntityService testEntityService;

    private TestEntity e1;

    private TestEntity e2;

    private boolean modeChanged;

    @Before
    public void setup() {
        e1 = new TestEntity("Bob", 11L);
        e1 = testEntityService.save(e1);

        e2 = new TestEntity("Harry", 12L);
        e2 = testEntityService.save(e2);
    }

    @Test
    public void testSimpleEditLayout() {
        SimpleEditLayout<Integer, TestEntity> layout = createLayout(e1, "TestEntity", new FormOptions());

        Map<String, Filter> ff = new HashMap<>();
        ff.put("testEntities", new Compare.Equal("name", "Harry"));

        layout.setFieldFilters(ff);
        layout.build();

        Assert.assertNotNull(layout.getEditForm());

        Assert.assertEquals(1, layout.getFieldFilters().size());
        Assert.assertEquals("Bob", layout.getEditForm().getField("name").getValue());

        // check that the screen is not in view mode
        Assert.assertFalse(layout.getEditForm().isViewMode());
        Assert.assertTrue(layout.getEditForm().getSaveButtons().get(0).isVisible());

        layout.setEntity(e2);
        Assert.assertEquals("Harry", layout.getEditForm().getField("name").getValue());

    }

    /**
     * Test opening the screen in view mode
     */
    @Test
    public void testSimpleEditLayout_ViewMode() {
        SimpleEditLayout<Integer, TestEntity> layout = createLayout(e1, "TestEntity",
                new FormOptions().setOpenInViewMode(true).setEditAllowed(true));
        layout.build();

        Assert.assertNotNull(layout.getEditForm());

        // check that the screen is in view mode and no save buttons are visible
        Assert.assertTrue(layout.getEditForm().isViewMode());
        Assert.assertTrue(layout.getEditForm().getSaveButtons().isEmpty());

        modeChanged = false;

        // click the edit button
        Assert.assertFalse(layout.getEditForm().getCancelButtons().get(0).isVisible());
        Assert.assertTrue(layout.getEditForm().getEditButtons().get(0).isVisible());
        Assert.assertFalse(layout.getEditForm().getBackButtons().get(0).isVisible());
        layout.getEditForm().getEditButtons().get(0).click();

        // check that we are now in edit mode
        Assert.assertFalse(layout.getEditForm().isViewMode());
        Assert.assertTrue(layout.getEditForm().getCancelButtons().get(0).isVisible());
        Assert.assertFalse(layout.getEditForm().getEditButtons().get(0).isVisible());
        Assert.assertTrue(modeChanged);

        // back to view mode
        layout.getEditForm().getCancelButtons().get(0).click();
        Assert.assertTrue(layout.getEditForm().isViewMode());

        layout.getEditForm().getEditButtons().get(0).click();
        Assert.assertFalse(layout.getEditForm().isViewMode());

        // after a reload we go back to the view mode
        layout.reload();
        Assert.assertTrue(layout.getEditForm().isViewMode());
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
        Assert.assertTrue(layout.getEditForm().isAttributeGroupVisible("testentity.group.1"));
        layout.getEditForm().setAttributeGroupVisible("testentity.group.1", false);
        Assert.assertFalse(layout.getEditForm().isAttributeGroupVisible("testentity.group.1"));

        Assert.assertTrue(layout.getEditForm().isAttributeGroupVisible("ocs.default.attribute.group"));
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
        Assert.assertTrue(layout.getEditForm().isAttributeGroupVisible("testentity.group.1"));
        layout.getEditForm().setAttributeGroupVisible("testentity.group.1", false);
        Assert.assertFalse(layout.getEditForm().isAttributeGroupVisible("testentity.group.1"));

        Assert.assertTrue(layout.getEditForm().isAttributeGroupVisible("ocs.default.attribute.group"));
    }

    /**
     * Test that attribute are grouped together on the same line
     */
    @Test
    public void testSimpleEditLayout_GroupAttributesTogether() {
        SimpleEditLayout<Integer, TestEntity> layout = createLayout(e1, "TestEntityGroupTogether",
                new FormOptions().setEditAllowed(true).setAttributeGroupMode(AttributeGroupMode.PANEL));
        layout.build();

        // check that the "age" field is the first component on a row (followed by the "name" field)
        Field<?> field = layout.getEditForm().getField("age");
        HasComponents hc = field.getParent();
        Assert.assertTrue(hc instanceof FormLayout);
        FormLayout fl = (FormLayout) hc;
        Assert.assertEquals(DynamoConstants.CSS_FIRST, fl.getStyleName());
        HasComponents horizontal = fl.getParent();
        Assert.assertTrue(horizontal instanceof HorizontalLayout);

        // check that the "name" field is properly nested
        field = layout.getEditForm().getField("name");
        hc = field.getParent();
        Assert.assertTrue(hc instanceof FormLayout);
        fl = (FormLayout) hc;
        Assert.assertEquals(DynamoConstants.CSS_ADDITIONAL, fl.getStyleName());
        horizontal = fl.getParent();
        Assert.assertTrue(horizontal instanceof HorizontalLayout);
    }

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
