package com.ocs.dynamo.ui.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

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

        List<String> items = List.of("Kevin", "Stuart", "Bob");
        when(service.findDistinct(isNull(), eq("name"), eq(String.class))).thenReturn(items);

        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        AttributeModel am = em.getAttributeModel("name");

        SimpleTokenFieldSelect<Integer, TestEntity, String> select = new SimpleTokenFieldSelect<>(service, em, am, null, "name",
                String.class, false);
        select.initContent();

        // select an item and verify it is added as a token
        select.setValue(Set.of("Kevin"));
        assertTrue(select.getValue().contains("Kevin"));
    }

    @Test
    public void testCreateAndOrder() {

        List<String> items = List.of("Kevin", "Stuart", "Bob");
        when(service.findDistinct(isNull(), eq("name"), eq(String.class))).thenReturn(items);

        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        AttributeModel am = em.getAttributeModel("name");
        SimpleTokenFieldSelect<Integer, TestEntity, String> select = new SimpleTokenFieldSelect<>(service, em, am, null, "name",
                String.class, false);
        select.initContent();

        // select two values and verify that they are added as tokens and removed from
        // the combo box
        select.setValue(Set.of("Kevin", "Bob"));
        assertEquals(2, select.getValue().size());
    }

    @Test
    public void testElementCollection() {

        List<String> items = List.of("Kevin", "Stuart", "Bob");
        when(service.findDistinctInCollectionTable(isNull(), isNull(), eq(String.class))).thenReturn(items);

        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        AttributeModel am = em.getAttributeModel("name");
        SimpleTokenFieldSelect<Integer, TestEntity, String> select = new SimpleTokenFieldSelect<>(service, em, am, null, "name",
                String.class, true);
        select.initContent();

        // select two values and verify that they are added as tokens and removed from
        // the combo box
        select.setValue(Set.of("Kevin", "Bob"));
        assertEquals(2, select.getValue().size());

    }
}
