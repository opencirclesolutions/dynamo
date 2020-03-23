package com.ocs.dynamo.ui.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.provider.IdBasedDataProvider;
import com.vaadin.flow.component.UI;

public class ServiceBasedDetailsEditGridTest extends BaseMockitoTest {

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    @Mock
    private UI ui;

    private TestEntity e1;

    private TestEntity e2;

    private TestEntity2 parent;

    @Mock
    private TestEntityService service;

    @BeforeEach
    public void setUp() {
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

        ServiceBasedDetailsEditGrid<Integer, TestEntity, Integer, TestEntity2> grid = createGrid(em, em.getAttributeModel("testEntities"),
                false, false, new FormOptions().setShowRemoveButton(true));
        assertTrue(grid.getAddButton().isVisible());
        assertFalse(grid.getSearchDialogButton().isVisible());

        grid.setValue(parent);

    }

    /**
     * Test read only with search functionality
     */
    @Test
    @SuppressWarnings({ "unchecked" })
    public void testReadOnlyWithSearch() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

        ServiceBasedDetailsEditGrid<Integer, TestEntity, Integer, TestEntity2> grid = createGrid(em, null, false, true,
                new FormOptions().setDetailsGridSearchMode(true));
        grid.setService(service);

        IdBasedDataProvider<Integer, TestEntity> provider = (IdBasedDataProvider<Integer, TestEntity>) grid.getGrid().getDataProvider();
        assertEquals(0, provider.getSize());

        // adding is not possible
        assertFalse(grid.getAddButton().isVisible());
        // but bringing up the search dialog is
        assertTrue(grid.getSearchDialogButton().isVisible());
    }

    @Test
    public void testReadOnly() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

        ServiceBasedDetailsEditGrid<Integer, TestEntity, Integer, TestEntity2> grid = createGrid(em, em.getAttributeModel("testEntities"),
                true, false, new FormOptions());
        assertFalse(grid.getAddButton().isVisible());
        assertFalse(grid.getSearchDialogButton().isVisible());
    }

    private ServiceBasedDetailsEditGrid<Integer, TestEntity, Integer, TestEntity2> createGrid(EntityModel<TestEntity> em, AttributeModel am,
            boolean viewMode, boolean readOnly, FormOptions fo) {

        if (readOnly) {
            fo.setReadOnly(true);
        }

        ServiceBasedDetailsEditGrid<Integer, TestEntity, Integer, TestEntity2> table = new ServiceBasedDetailsEditGrid<Integer, TestEntity, Integer, TestEntity2>(
                service, em, am, viewMode, fo);

        table.setCreateEntitySupplier(() -> new TestEntity());
        table.initContent();

        return table;
    }
}
