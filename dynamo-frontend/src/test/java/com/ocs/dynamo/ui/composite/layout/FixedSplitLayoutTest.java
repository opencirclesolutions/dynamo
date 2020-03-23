package com.ocs.dynamo.ui.composite.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collection;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.TestEntity2Service;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;

public class FixedSplitLayoutTest extends FrontendIntegrationTest {

    @Inject
    private EntityModelFactory entityModelFactory;

    private TestEntity e1;

    private TestEntity e2;

    private TestEntity2 child1;

    private TestEntity2 child2;

    @Inject
    private TestEntityService testEntityService;

    @Inject
    private TestEntity2Service testEntity2Service;

    @BeforeEach
    public void setup() {
        MockVaadin.setup();
        e1 = new TestEntity("Bob", 11L);
        e1 = testEntityService.save(e1);

        e2 = new TestEntity("Harry", 12L);
        e2 = testEntityService.save(e2);

        child1 = new TestEntity2();
        child1.setTestEntity(e1);
        child1 = testEntity2Service.save(child1);

        child2 = new TestEntity2();
        child2.setTestEntity(e2);
        child2 = testEntity2Service.save(child2);
    }

    @Test
    public void testCreateInEditMode() {
        FormOptions fo = new FormOptions().setExportAllowed(true);
        FixedSplitLayout<Integer, TestEntity> layout = new FixedSplitLayout<Integer, TestEntity>(testEntityService,
                entityModelFactory.getModel(TestEntity.class), fo, new SortOrder<String>("name", SortDirection.ASCENDING)) {

            private static final long serialVersionUID = 6308563510081372500L;

            @Override
            protected Collection<TestEntity> loadItems() {
                return Lists.newArrayList(e1, e2);
            }
        };
        layout.build();

        // layout must contain 2 items
        assertEquals(2, layout.getGridWrapper().getDataProviderSize());

        // select an item and check that the edit form is generated
        layout.getGridWrapper().getGrid().select(e1);
        assertEquals(e1, layout.getSelectedItem());
        assertNotNull(layout.getEditForm());
        assertFalse(layout.getEditForm().isViewMode());

        // no quick search field for this component
        assertNull(layout.getQuickSearchField());

        layout.getGridWrapper().getGrid().deselectAll();
        assertNull(layout.getSelectedItem());

        layout.reload();
    }

    /**
     * Test the creation of a detail layout
     */
    @Test
    public void testCreateDetailLayout() {
        FormOptions fo = new FormOptions();
        FixedDetailLayout<Integer, TestEntity2, Integer, TestEntity> layout = new FixedDetailLayout<Integer, TestEntity2, Integer, TestEntity>(
                testEntity2Service, e1, testEntityService, entityModelFactory.getModel(TestEntity2.class), fo, null) {

            private static final long serialVersionUID = 7009824287226683886L;

            @Override
            protected Collection<TestEntity2> loadItems() {
                return Lists.newArrayList(child1);
            }
        };

        layout.build();

        assertEquals(1, layout.getGridWrapper().getDataProviderSize());
        assertEquals(e1, layout.getParentEntity());

        layout.reload();
    }

}