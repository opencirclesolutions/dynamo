package com.ocs.dynamo.ui.composite.layout;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.TestEntity2Service;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.ui.VerticalLayout;

public class TabularEditLayoutTest extends BaseIntegrationTest {

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
    public void testCreate() {
        FormOptions fo = new FormOptions();
        TabularEditLayout<Integer, TestEntity> layout = new TabularEditLayout<>(testEntityService,
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

        Assert.assertEquals(2, layout.getTableWrapper().getTable().size());

        // click the add button and check that a row is added
        layout.getAddButton().click();
        Assert.assertEquals(3, layout.getTableWrapper().getTable().size());

        // no remove button
        Assert.assertTrue(layout.getTableWrapper().getTable().getColumnGenerator("Remove") == null);

        // not all required fields filled, so saving is disabled
        Assert.assertFalse(layout.getSaveButton().isEnabled());

        // try selecting an item
        layout.getTableWrapper().getTable().select(e1.getId());
        Assert.assertEquals(e1, layout.getSelectedItem());

        layout.getTableWrapper().getTable().select(Lists.newArrayList(e1, e2));
        Assert.assertEquals(e1, layout.getSelectedItem());
    }

    /**
     * Test that a remove button is created when appropriate
     */
    @Test
    public void testCreateWithRemoveButton() {
        FormOptions fo = new FormOptions().setShowRemoveButton(true);
        TabularEditLayout<Integer, TestEntity> layout = new TabularEditLayout<>(testEntityService,
                entityModelFactory.getModel(TestEntity.class), fo, null);
        layout.build();

        // check that a remove button is created
        Assert.assertTrue(layout.getTableWrapper().getTable().getColumnGenerator("Remove") != null);
    }

    @Test
    public void testCreateDetailLayout() {
        FormOptions fo = new FormOptions();
        TabularDetailEditLayout<Integer, TestEntity2, Integer, TestEntity> layout = new TabularDetailEditLayout<>(
                testEntity2Service, e1, testEntityService, entityModelFactory.getModel(TestEntity2.class), fo, null);

        layout.build();

        Assert.assertEquals(2, layout.getTableWrapper().getTable().size());
        Assert.assertEquals(e1, layout.getParentEntity());
    }

    /**
     * Test the creation of a layout with a filter in place and see that the number of rows in the
     * table is restricted
     */
    @Test
    public void testCreateWithFilter() {
        FormOptions fo = new FormOptions();
        TabularEditLayout<Integer, TestEntity> layout = new TabularEditLayout<Integer, TestEntity>(testEntityService,
                entityModelFactory.getModel(TestEntity.class), fo, null) {

            private static final long serialVersionUID = -4868427813173119069L;

            @Override
            protected Filter constructFilter() {
                return new Compare.Equal("name", "Bob");
            }
        };
        layout.build();

        Assert.assertEquals(1, layout.getTableWrapper().getTable().size());

    }

    @Test
    public void testCreateInViewMode() {
        FormOptions fo = new FormOptions().setOpenInViewMode(true).setEditAllowed(true);
        TabularEditLayout<Integer, TestEntity> layout = new TabularEditLayout<>(testEntityService,
                entityModelFactory.getModel(TestEntity.class), fo, null);
        layout.build();

        // open in view mode
        Assert.assertTrue(layout.isViewmode());
        Assert.assertTrue(layout.getEditButton().isVisible());
        Assert.assertFalse(layout.getCancelButton().isVisible());
        Assert.assertFalse(layout.getAddButton().isVisible());
        Assert.assertFalse(layout.getSaveButton().isVisible());

        Assert.assertEquals(2, layout.getTableWrapper().getTable().size());

        // switch to edit mode
        layout.getEditButton().click();
        Assert.assertFalse(layout.isViewmode());
        Assert.assertFalse(layout.getEditButton().isVisible());
        Assert.assertTrue(layout.getCancelButton().isVisible());
        Assert.assertTrue(layout.getAddButton().isVisible());
        Assert.assertTrue(layout.getSaveButton().isVisible());

        // switch back
        layout.getCancelButton().click();
        Assert.assertTrue(layout.isViewmode());
        Assert.assertTrue(layout.getEditButton().isVisible());
        Assert.assertFalse(layout.getCancelButton().isVisible());
        Assert.assertFalse(layout.getAddButton().isVisible());
        Assert.assertFalse(layout.getSaveButton().isVisible());

    }
}
