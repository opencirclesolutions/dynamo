package com.ocs.dynamo.ui.component;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.vaadin.ui.UI;

public class FancyListSelectTest extends BaseMockitoTest {

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    @Mock
    private UI ui;

    @Mock
    private TestEntityService service;

    private TestEntity t1;

    private TestEntity t2;

    private TestEntity t3;

    @Before
    public void setUp() throws Exception {
        t1 = new TestEntity(1, "Kevin", 12L);
        t2 = new TestEntity(2, "Bob", 13L);
        t3 = new TestEntity(3, "Stewart", 14L);

        Mockito.when(service.find(Matchers.any(Filter.class), (SortOrder[]) Matchers.anyVararg()))
                .thenReturn(Lists.newArrayList(t1, t2, t3));

        Mockito.when(service.createNewEntity()).thenReturn(new TestEntity());
        MockUtil.mockServiceSave(service, TestEntity.class);
    }

    /**
     * Test the creation of the component and a simple selection
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testCreateAndSelect() {
        AttributeModel am = factory.getModel(TestEntity.class).getAttributeModel("name");
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

        FancyListSelect<Integer, TestEntity> select = new FancyListSelect<>(service,
                factory.getModel(TestEntity.class), am, null, false);
        select.initContent();

        Assert.assertEquals(am, select.getAttributeModel());
        Assert.assertEquals(em, select.getEntityModel());

        Assert.assertEquals(3, select.getComboBox().getContainerDataSource().size());

        select.getComboBox().setValue(t1);
        select.getSelectButton().click();

        Assert.assertTrue(select.getValue() instanceof Collection);
        Collection<TestEntity> col = (Collection<TestEntity>) select.getValue();
        Assert.assertTrue(col.contains(t1));

        // test the removal of a value
        select.getListSelect().setValue(Lists.newArrayList(t1));
        select.getRemoveButton().click();

        col = (Collection<TestEntity>) select.getValue();
        Assert.assertFalse(col.contains(t1));

        select.getComboBox().setValue(t2);
        select.getSelectButton().click();
        select.getComboBox().setValue(t1);
        select.getSelectButton().click();

        col = (Collection<TestEntity>) select.getValue();
        Assert.assertTrue(col.contains(t2));

        select.getClearButton().click();

        col = (Collection<TestEntity>) select.getValue();
        Assert.assertTrue(col.isEmpty());
        
        select.refresh();
    }

    /**
     * Test the addition of a new value
     */
    @Test
    public void testAdd() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        AttributeModel am = em.getAttributeModel("testDomain");

        FancyListSelect<Integer, TestEntity> select = new FancyListSelect<>(service, em, am, null,
                false);
        select.initContent();
        MockUtil.injectUI(select, ui);

        Assert.assertEquals(3, select.getComboBox().getContainerDataSource().size());

        // bring up the add dialog
        addNewValue(select, "New Item");

        // check that a new item has been added
        Assert.assertEquals(4, select.getComboBox().getContainerDataSource().size());
    }

    /**
     * Test the addition of a new value that is too long (will result in an exception)
     */
    @Test
    public void testAddTooLong() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        AttributeModel am = em.getAttributeModel("testDomain");

        FancyListSelect<Integer, TestEntity> select = new FancyListSelect<>(service, em, am, null,
                false);
        select.initContent();
        MockUtil.injectUI(select, ui);

        Assert.assertEquals(3, select.getComboBox().getContainerDataSource().size());

        // bring up the add dialog and add a value that is too long
        addNewValue(select, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        
        Assert.assertEquals(3, select.getComboBox().getContainerDataSource().size());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void addNewValue(FancyListSelect<Integer, TestEntity> select, String newValue) {
        ArgumentCaptor<AddNewValueDialog> captor = ArgumentCaptor.forClass(AddNewValueDialog.class);

        select.getAddButton().click();
        Mockito.verify(ui).addWindow(captor.capture());

        AddNewValueDialog<Integer, TestEntity> dialog = (AddNewValueDialog<Integer, TestEntity>) captor
                .getValue();
        dialog.getValueField().setValue(newValue);
        dialog.getOkButton().click();
    }
}
