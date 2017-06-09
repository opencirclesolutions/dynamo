package com.ocs.dynamo.ui.composite.layout;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

public class HorizontalDisplayLayoutTest extends BaseIntegrationTest {

    @Inject
    private EntityModelFactory entityModelFactory;

    private TestEntity e1;

    @Inject
    private TestEntityService service;

    @Before
    public void setup() {
        e1 = new TestEntity("Bob", 11L);
        e1 = service.save(e1);
    }

    @Test
    public void test() {
        HorizontalDisplayLayout<Integer, TestEntity> layout = new HorizontalDisplayLayout<Integer, TestEntity>(service,
                entityModelFactory.getModel(TestEntity.class), e1);
        layout.build();

        Component comp = layout.iterator().next();
        Assert.assertTrue(comp instanceof HorizontalLayout);
    }

}
