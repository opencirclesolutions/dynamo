package com.ocs.dynamo.ui.composite.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.TestEntity2Service;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.ocs.dynamo.ui.composite.type.GridEditMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializablePredicate;

public class EditableGridLayoutTest extends FrontendIntegrationTest {

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

    @BeforeEach
    public void setup() {
        MockVaadin.setup();
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
    public void testCreateSimultaneous() {
        FormOptions fo = new FormOptions().setGridEditMode(GridEditMode.SIMULTANEOUS);
        EditableGridLayout<Integer, TestEntity> layout = new EditableGridLayout<>(testEntityService,
                entityModelFactory.getModel("TestEntityGrid", TestEntity.class), fo, null);
        layout.build();

        VerticalLayout parent = new VerticalLayout();
        parent.add(layout);

        // open in edit mode by default
        assertFalse(layout.isViewmode());
        assertFalse(layout.getEditButton().isVisible());
        assertFalse(layout.getCancelButton().isVisible());
        assertTrue(layout.getAddButton().isVisible());
        assertTrue(layout.getSaveButton().isVisible());

        layout.getGridWrapper().getDataProvider().size(new Query<TestEntity, SerializablePredicate<TestEntity>>());
        assertEquals(2, layout.getGridWrapper().getDataProviderSize());

        // no remove button
        assertTrue(layout.getGridWrapper().getGrid().getColumnByKey("remove") == null);

        // try selecting an item
        layout.getGridWrapper().getGrid().select(e1);
        assertEquals(e1, layout.getSelectedItem());
    }

    @Test
    @Transactional
    public void testCreate() {
        FormOptions fo = new FormOptions();
        EditableGridLayout<Integer, TestEntity> layout = new EditableGridLayout<>(testEntityService,
                entityModelFactory.getModel("TestEntityGrid", TestEntity.class), fo, null);
        layout.build();

        VerticalLayout parent = new VerticalLayout();
        parent.add(layout);

        // open in edit mode by default
        assertFalse(layout.isViewmode());
        assertFalse(layout.getEditButton().isVisible());
        assertFalse(layout.getCancelButton().isVisible());
        assertTrue(layout.getAddButton().isVisible());

        // no save button in "row by row" mode
        assertFalse(layout.getSaveButton().isVisible());

        layout.getGridWrapper().forceSearch();
        assertEquals(2, layout.getGridWrapper().getDataProviderSize());
        assertTrue(layout.getGridWrapper().getGrid().getColumnByKey("remove") == null);

        // try selecting an item
        layout.getGridWrapper().getGrid().select(e1);
        assertEquals(e1, layout.getSelectedItem());

    }

    @Test
    @Transactional
    public void testCreateDetailLayout() {
        FormOptions fo = new FormOptions();
        EditableGridDetailLayout<Integer, TestEntity2, Integer, TestEntity> layout = new EditableGridDetailLayout<>(testEntity2Service, e1,
                testEntityService, entityModelFactory.getModel("TestEntityGrid", TestEntity2.class), fo, null);

        layout.build();
        layout.getGridWrapper().forceSearch();

        assertEquals(2, layout.getGridWrapper().getDataProviderSize());
        assertEquals(e1, layout.getParentEntity());
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
                entityModelFactory.getModel("TestEntityGrid", TestEntity.class), fo, null);
        layout.setFilterSupplier(() -> new EqualsPredicate<>("name", "Bob"));
        layout.build();
        layout.getGridWrapper().forceSearch();

        assertEquals(1, layout.getGridWrapper().getDataProviderSize());
    }

    @Test
    @Transactional()
    public void testCreateInViewMode() {
        FormOptions fo = new FormOptions().setOpenInViewMode(true).setEditAllowed(true);
        EditableGridLayout<Integer, TestEntity> layout = new EditableGridLayout<>(testEntityService,
                entityModelFactory.getModel("TestEntityGrid", TestEntity.class), fo, null);
        layout.build();

        // open in view mode
        assertTrue(layout.isViewmode());
        assertTrue(layout.getEditButton().isVisible());
        assertFalse(layout.getCancelButton().isVisible());
        assertFalse(layout.getAddButton().isVisible());
        assertFalse(layout.getSaveButton().isVisible());

        layout.getGridWrapper().getDataProvider().size(new Query<TestEntity, SerializablePredicate<TestEntity>>());
        assertEquals(2, layout.getGridWrapper().getDataProviderSize());

        // switch to edit mode
        layout.getEditButton().click();
        assertFalse(layout.isViewmode());
        assertFalse(layout.getEditButton().isVisible());
        assertTrue(layout.getCancelButton().isVisible());
        assertTrue(layout.getAddButton().isVisible());

        // switch back
        layout.getCancelButton().click();
        assertTrue(layout.isViewmode());
        assertTrue(layout.getEditButton().isVisible());
        assertFalse(layout.getCancelButton().isVisible());
        assertFalse(layout.getAddButton().isVisible());
        assertFalse(layout.getSaveButton().isVisible());

    }
}
