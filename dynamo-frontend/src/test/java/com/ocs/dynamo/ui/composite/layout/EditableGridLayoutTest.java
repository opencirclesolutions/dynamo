package com.ocs.dynamo.ui.composite.layout;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.TestEntity2Service;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.ui.composite.type.GridEditMode;
import com.vaadin.ui.VerticalLayout;

public class EditableGridLayoutTest extends BaseIntegrationTest {

    @Autowired
    private EntityModelFactory entityModelFactory;

    @Autowired
    private TestEntityService testEntityService;

    @Autowired
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
        child1 = testEntity2Service.save(child1);

        child2 = new TestEntity2();
        child2 = testEntity2Service.save(child2);
    }

    @Test
    @Transactional
    public void testCreateSimulatenous() {
        FormOptions fo = new FormOptions().setGridEditMode(GridEditMode.SIMULTANEOUS);
        EditableGridLayout<Integer, TestEntity> layout = new EditableGridLayout<>(testEntityService,
                entityModelFactory.getModel(TestEntity.class), fo, null);
        layout.build();

        VerticalLayout parent = new VerticalLayout();
        parent.addComponent(layout);

        // open in edit mode by default
        Assert.assertFalse(layout.isViewmode());
        Assert.assertFalse(layout.getEditButton().isVisible());
        Assert.assertFalse(layout.getCancelButton().isVisible());
        Assert.assertTrue(layout.getAddButton().isVisible());
        Assert.assertTrue(layout.getSaveButton().isVisible());

        Assert.assertEquals(2, layout.getGridWrapper().getDataProviderSize());

        // no remove button
        Assert.assertTrue(layout.getGridWrapper().getGrid().getColumn("remove") == null);

        // try selecting an item
        layout.getGridWrapper().getGrid().select(e1);
        Assert.assertEquals(e1, layout.getSelectedItem());
    }

    @Test
    @Transactional
    public void testCreate() {
        FormOptions fo = new FormOptions();
        EditableGridLayout<Integer, TestEntity> layout = new EditableGridLayout<>(testEntityService,
                entityModelFactory.getModel(TestEntity.class), fo, null);
        layout.build();

        VerticalLayout parent = new VerticalLayout();
        parent.addComponent(layout);

        // open in edit mode by default
        Assert.assertFalse(layout.isViewmode());
        Assert.assertFalse(layout.getEditButton().isVisible());
        Assert.assertFalse(layout.getCancelButton().isVisible());
        Assert.assertTrue(layout.getAddButton().isVisible());

        // no save button in "row by row" mode
        Assert.assertFalse(layout.getSaveButton().isVisible());

        Assert.assertEquals(2, layout.getGridWrapper().getDataProviderSize());
        Assert.assertTrue(layout.getGridWrapper().getGrid().getColumn("remove") == null);

        // try selecting an item
        layout.getGridWrapper().getGrid().select(e1);
        Assert.assertEquals(e1, layout.getSelectedItem());

    }

    /**
     * Test that a remove button is created when appropriate
     */
    @Test
    @Transactional
    public void testCreateWithRemoveButton() {
        FormOptions fo = new FormOptions().setShowRemoveButton(true);
        EditableGridLayout<Integer, TestEntity> layout = new EditableGridLayout<>(testEntityService,
                entityModelFactory.getModel(TestEntity.class), fo, null);
        layout.build();

        // check that a remove button is created
        Assert.assertTrue(layout.getGridWrapper().getGrid().getColumn("remove") != null);
    }

    @Test
    @Transactional
    public void testCreateDetailLayout() {
        FormOptions fo = new FormOptions();
        EditableGridDetailLayout<Integer, TestEntity2, Integer, TestEntity> layout = new EditableGridDetailLayout<>(testEntity2Service, e1,
                testEntityService, entityModelFactory.getModel(TestEntity2.class), fo, null);

        layout.build();

        Assert.assertEquals(2, layout.getGridWrapper().getDataProviderSize());
        Assert.assertEquals(e1, layout.getParentEntity());
    }

    /**
     * Test the creation of a layout with a filter in place and see that the number
     * of rows in the table is restricted
     */
    @Test
    @Transactional
    public void testCreateWithFilter() {
        FormOptions fo = new FormOptions();
        EditableGridLayout<Integer, TestEntity> layout = new EditableGridLayout<Integer, TestEntity>(testEntityService,
                entityModelFactory.getModel(TestEntity.class), fo, null);
        layout.setFilterSupplier(() -> new EqualsPredicate<>("name", "Bob"));
        layout.build();

        Assert.assertEquals(1, layout.getGridWrapper().getDataProviderSize());

    }

    @Test
    @Transactional()
    public void testCreateInViewMode() {
        FormOptions fo = new FormOptions().setOpenInViewMode(true).setEditAllowed(true);
        EditableGridLayout<Integer, TestEntity> layout = new EditableGridLayout<>(testEntityService,
                entityModelFactory.getModel(TestEntity.class), fo, null);
        layout.build();

        // open in view mode
        Assert.assertTrue(layout.isViewmode());
        Assert.assertTrue(layout.getEditButton().isVisible());
        Assert.assertFalse(layout.getCancelButton().isVisible());
        Assert.assertFalse(layout.getAddButton().isVisible());
        Assert.assertFalse(layout.getSaveButton().isVisible());

        Assert.assertEquals(2, layout.getGridWrapper().getDataProviderSize());

        // switch to edit mode
        layout.getEditButton().click();
        Assert.assertFalse(layout.isViewmode());
        Assert.assertFalse(layout.getEditButton().isVisible());
        Assert.assertTrue(layout.getCancelButton().isVisible());
        Assert.assertTrue(layout.getAddButton().isVisible());

        // switch back
        layout.getCancelButton().click();
        Assert.assertTrue(layout.isViewmode());
        Assert.assertTrue(layout.getEditButton().isVisible());
        Assert.assertFalse(layout.getCancelButton().isVisible());
        Assert.assertFalse(layout.getAddButton().isVisible());
        Assert.assertFalse(layout.getSaveButton().isVisible());

    }
}
