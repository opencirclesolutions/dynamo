package com.ocs.dynamo.ui.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
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
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.ocs.dynamo.ui.component.EntityComboBox.SelectMode;

public class QuickAddEntityComboBoxTest extends BaseMockitoTest {

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
        when(service.find(isA(com.ocs.dynamo.filter.Filter.class))).thenReturn(Lists.newArrayList(t1));

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

        QuickAddEntityComboBox<Integer, TestEntity> select = new QuickAddEntityComboBox<>(em, am, service, SelectMode.FILTERED, null, false,
                null, null);
        select.initContent();

        // list must contain 3 items
        assertEquals(3, select.getComboBox().getDataProviderSize());

        // test propagation of the value
        select.setValue(t1);
        assertEquals(t1, select.getComboBox().getValue());

        // .. and the other way around
        select.getComboBox().setValue(t2);
        assertEquals(t2, select.getValue());

    }

    @Test
    public void testAdditionalFilter() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        AttributeModel am = em.getAttributeModel("testDomain");

        QuickAddEntityComboBox<Integer, TestEntity> select = new QuickAddEntityComboBox<>(em, am, service, SelectMode.FILTERED, null, false,
                null, null);
        select.initContent();

        assertEquals(3, select.getComboBox().getDataProviderSize());

        // apply an additional filter
        select.setAdditionalFilter(new EqualsPredicate<TestEntity>("name", "Kevin"));
        assertEquals(1, select.getComboBox().getDataProviderSize());

        // and remove it again
        select.clearAdditionalFilter();
        assertEquals(3, select.getComboBox().getDataProviderSize());
    }

    @Test
    public void testRefresh() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        AttributeModel am = em.getAttributeModel("testDomain");

        QuickAddEntityComboBox<Integer, TestEntity> select = new QuickAddEntityComboBox<>(em, am, service, SelectMode.FILTERED, null, false,
                null, null);
        select.initContent();

        assertEquals(3, select.getComboBox().getDataProviderSize());

        // refresh with a filter
        select.refresh(new EqualsPredicate<TestEntity>("name", "Kevin"));
        assertEquals(1, select.getComboBox().getDataProviderSize());

        // just a regular refresh
        select.refresh();
        assertEquals(1, select.getComboBox().getDataProviderSize());
    }

}
