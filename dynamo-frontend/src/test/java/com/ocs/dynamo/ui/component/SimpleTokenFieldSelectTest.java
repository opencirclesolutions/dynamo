package com.ocs.dynamo.ui.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;

public class SimpleTokenFieldSelectTest extends BaseMockitoTest {

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    @Mock
    private TestEntityService service;

    @Test
    public void testCreate() {

        List<String> items = Lists.newArrayList("Kevin", "Stuart", "Bob");
        Mockito.when(service.findDistinct(Mockito.isNull(), Mockito.eq("name"), Mockito.eq(String.class))).thenReturn(items);

        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        AttributeModel am = em.getAttributeModel("name");

        SimpleTokenFieldSelect<Integer, TestEntity, String> select = new SimpleTokenFieldSelect<>(service, em, am, null, "name",
                String.class, false);
        select.initContent();

        // select an item and verify it is added as a token
        select.setValue(Sets.newHashSet("Kevin"));
        assertTrue(select.getValue().contains("Kevin"));
    }

    @Test
    public void testCreateAndOrder() {

        List<String> items = Lists.newArrayList("Kevin", "Stuart", "Bob");
        Mockito.when(service.findDistinct(Mockito.isNull(), Mockito.eq("name"), Mockito.eq(String.class))).thenReturn(items);

        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        AttributeModel am = em.getAttributeModel("name");
        SimpleTokenFieldSelect<Integer, TestEntity, String> select = new SimpleTokenFieldSelect<>(service, em, am, null, "name",
                String.class, false);
        select.initContent();

        // select two values and verify that they are added as tokens and removed from
        // the combo box
        select.setValue(Sets.newHashSet("Kevin", "Bob"));
        assertEquals(2, select.getValue().size());
    }

    @Test
    public void testElementCollection() {

        List<String> items = Lists.newArrayList("Kevin", "Stuart", "Bob");
        Mockito.when(service.findDistinctInCollectionTable(Mockito.isNull(), Mockito.isNull(), Mockito.eq(String.class))).thenReturn(items);

        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        AttributeModel am = em.getAttributeModel("name");
        SimpleTokenFieldSelect<Integer, TestEntity, String> select = new SimpleTokenFieldSelect<>(service, em, am, null, "name",
                String.class, true);
        select.initContent();

        // select two values and verify that they are added as tokens and removed from
        // the combo box
        select.setValue(Sets.newHashSet("Kevin", "Bob"));
        Assert.assertEquals(2, select.getValue().size());

    }
}
