package com.ocs.dynamo.ui.component;

import java.util.Collection;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.ocs.dynamo.ui.UIHelper;

public class QuickAddTokenSelectTest extends BaseMockitoTest {

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    @Mock
    private UIHelper ui;

    @Mock
    private TestEntityService service;

    private TestEntity t1;

    private TestEntity t2;

    private TestEntity t3;

    @Before
    public void setUp() {
        t1 = new TestEntity(1, "Kevin", 12L);
        t2 = new TestEntity(2, "Bob", 13L);
        t3 = new TestEntity(3, "Stewart", 14L);

        Mockito.when(service.find(Mockito.isNull(), (SortOrder[]) Mockito.any())).thenReturn(Lists.newArrayList(t1, t2, t3));
        Mockito.when(service.find(Mockito.isNull())).thenReturn(Lists.newArrayList(t1, t2, t3));

        Filter f = new com.ocs.dynamo.filter.Compare.Equal("name", "Kevin");
        Mockito.when(service.find(Mockito.eq(f))).thenReturn(Lists.newArrayList(t1));

        Mockito.when(service.createNewEntity()).thenReturn(new TestEntity());
        MockUtil.mockServiceSave(service, TestEntity.class);
    }

    /**
     * Test the creation of the component and a simple selection
     */
    @Test
    public void testCreateAndSelect() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        AttributeModel am = em.getAttributeModel("testDomain");

        QuickAddTokenSelect<Integer, TestEntity> select = new QuickAddTokenSelect<>(em, am, service, null, false);
        select.initContent();

        // list must contain 3 items
        Assert.assertEquals(3, select.getTokenSelect().getDataProviderSize());

        // test propagation of the value
        select.getTokenSelect().select(t1);
        Assert.assertTrue(select.getTokenSelect().getValue().contains(t1));

        // .. and the other way around
        select.getTokenSelect().select(t2);
        Assert.assertTrue(select.getTokenSelect().getValue().contains(t2));
    }

    @Test
    public void testSetValue() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        AttributeModel am = em.getAttributeModel("testDomain");

        QuickAddTokenSelect<Integer, TestEntity> select = new QuickAddTokenSelect<>(em, am, service, null, false);
        select.initContent();

        select.setValue(null);
        Assert.assertEquals(Collections.emptySet(), select.getValue());
    }

    @Test
    public void testAdditionalFilter() {

        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        AttributeModel am = em.getAttributeModel("testDomain");

        QuickAddTokenSelect<Integer, TestEntity> select = new QuickAddTokenSelect<>(em, am, service, null, false);
        select.initContent();

        // list must contain 3 items
        Assert.assertEquals(3, select.getTokenSelect().getDataProviderSize());

        select.setAdditionalFilter(new EqualsPredicate<TestEntity>("name", "Kevin"));

        // after filter there must be 1 item left
        Assert.assertEquals(1, select.getTokenSelect().getDataProviderSize());

        // clear filter again
        select.clearAdditionalFilter();
        Assert.assertEquals(3, select.getTokenSelect().getDataProviderSize());

        select.setValue(Lists.newArrayList(t2));

        Collection<TestEntity> value = select.getValue();
        Assert.assertTrue(value.contains(t2));

        select.refresh();

        select.refresh(new EqualsPredicate<TestEntity>("name", "Bob"));
        Assert.assertNotNull(select.getFilter());
    }
}
