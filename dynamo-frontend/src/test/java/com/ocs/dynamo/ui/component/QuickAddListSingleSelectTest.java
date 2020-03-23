package com.ocs.dynamo.ui.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

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

public class QuickAddListSingleSelectTest extends BaseMockitoTest {

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    @Mock
    private TestEntityService service;

    private TestEntity t1;

    private TestEntity t2;

    private TestEntity t3;

    @BeforeEach
    public void setUp() {
        t1 = new TestEntity(1, "Kevin", 12L);
        t2 = new TestEntity(2, "Bob", 13L);
        t3 = new TestEntity(3, "Stewart", 14L);

        when(service.find(isNull(), (SortOrder[]) any())).thenReturn(Lists.newArrayList(t1, t2, t3));
        when(service.find(isNull())).thenReturn(Lists.newArrayList(t1, t2, t3));

        Filter f = new com.ocs.dynamo.filter.Compare.Equal("name", "Kevin");
        when(service.find(eq(f))).thenReturn(Lists.newArrayList(t1));

        when(service.createNewEntity()).thenReturn(new TestEntity());
        MockUtil.mockServiceSave(service, TestEntity.class);
    }

    /**
     * Test the creation of the component and a simple selection
     */
    @Test
    public void testCreateAndSelect() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        AttributeModel am = em.getAttributeModel("testDomain");

        QuickAddListSingleSelect<Integer, TestEntity> select = new QuickAddListSingleSelect<>(em, am, service, null, null, false);
        select.initContent();

        // list must contain 3 items
        assertEquals(3, select.getListSelect().getDataProviderSize());

        // test propagation of the value
        select.getListSelect().setValue(t1);
        assertEquals(t1, select.getListSelect().getValue());

        // .. and the other way around
        select.getListSelect().setValue(t2);
        assertEquals(t2, select.getListSelect().getValue());

    }

    @Test
    public void testAdditionalFilter() {

        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        AttributeModel am = em.getAttributeModel("testDomain");

        QuickAddListSingleSelect<Integer, TestEntity> select = new QuickAddListSingleSelect<>(em, am, service, null, null, false);
        select.initContent();

        // list must contain 3 items
        assertEquals(3, select.getListSelect().getDataProviderSize());

        select.setAdditionalFilter(new EqualsPredicate<TestEntity>("name", "Kevin"));

        // after filter there must be 1 item left
        assertEquals(1, select.getListSelect().getDataProviderSize());

        // clear filter again
        select.clearAdditionalFilter();
        assertEquals(3, select.getListSelect().getDataProviderSize());

        select.setValue(t2);
        assertEquals(t2, select.getValue());

        select.refresh();

        select.refresh(new EqualsPredicate<TestEntity>("name", "Bob"));
        assertNotNull(select.getFilter());
    }
}
