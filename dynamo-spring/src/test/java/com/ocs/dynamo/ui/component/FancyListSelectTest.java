package com.ocs.dynamo.ui.component;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

public class FancyListSelectTest extends BaseMockitoTest {

    private EntityModelFactory factory = new EntityModelFactoryImpl();

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
    }
}
