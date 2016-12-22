package com.ocs.dynamo.ui.composite.form;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.comparator.AttributeComparator;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.ocs.dynamo.ui.composite.dialog.ModelBasedSearchDialog;
import com.vaadin.ui.UI;

public class DetailsEditTableTest extends BaseMockitoTest {

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    @Mock
    private UI ui;

    private TestEntity e1;

    private TestEntity e2;
    
    @Mock
    private TestEntityService service;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        e1 = new TestEntity(1, "Kevin", 12L);
        e2 = new TestEntity(2, "Bob", 14L);
    }

    /**
     * Test a table in editable mode
     */
    @Test
    public void testEditable() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

        DetailsEditTable<Integer, TestEntity> table = createTable(em, false, false,
                new FormOptions().setShowRemoveButton(true));
        Assert.assertTrue(table.getAddButton().isVisible());
        Assert.assertFalse(table.getSearchDialogButton().isVisible());

        Assert.assertEquals(2, table.getTable().getContainerDataSource().size());

        // test that the add button will add a row
        table.getAddButton().click();
        Assert.assertEquals(3, table.getTable().getContainerDataSource().size());

        // explicitly set field value
        table.setValue(Lists.newArrayList(e1));
        Assert.assertEquals(1, table.getTable().getContainerDataSource().size());
    }

    /**
     * Test read only with search functionality
     */
    @Test
    @SuppressWarnings("rawtypes")
    public void testReadOnlyWithSearch() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

        DetailsEditTable<Integer, TestEntity> table = createTable(em, false, true,
                new FormOptions().setShowSearchDialogButton(true));
        table.setTableReadOnly(true);
        table.setService(service);

        // adding is not possible
        Assert.assertFalse(table.getAddButton().isVisible());
        // but bringing up the search dialog is
        Assert.assertTrue(table.getSearchDialogButton().isVisible());

        table.getSearchDialogButton().click();
        ArgumentCaptor<ModelBasedSearchDialog> captor = ArgumentCaptor.forClass(ModelBasedSearchDialog.class);
        Mockito.verify(ui).addWindow(captor.capture());
    }

    @Test
    public void testReadOnly() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

        DetailsEditTable<Integer, TestEntity> table = createTable(em, true, false, new FormOptions());
        Assert.assertFalse(table.getAddButton().isVisible());
        Assert.assertFalse(table.getSearchDialogButton().isVisible());

        Assert.assertEquals(2, table.getTable().getContainerDataSource().size());

    }

    private DetailsEditTable<Integer, TestEntity> createTable(EntityModel<TestEntity> em, boolean viewMode,
            boolean tableReadOnly, FormOptions fo) {
        DetailsEditTable<Integer, TestEntity> table = new DetailsEditTable<Integer, TestEntity>(Lists.newArrayList(e1,
                e2), em, viewMode, fo) {

            private static final long serialVersionUID = -4333833542380882076L;

            @Override
            protected void removeEntity(TestEntity toRemove) {
                // not needed
            }

            @Override
            protected TestEntity createEntity() {
                return new TestEntity();
            }
        };
        MockUtil.injectUI(table, ui);
        table.setTableReadOnly(tableReadOnly);
        table.initContent();
        table.setComparator(new AttributeComparator<TestEntity>("name"));
        return table;
    }
}
