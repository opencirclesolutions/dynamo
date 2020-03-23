package com.ocs.dynamo.ui.composite.layout;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class HorizontalDisplayLayoutTest extends FrontendIntegrationTest {

    @Inject
    private EntityModelFactory entityModelFactory;

    private TestEntity e1;

    @Inject
    private TestEntityService service;

    @BeforeEach
    public void setup() {
        e1 = new TestEntity("Bob", 11L);
        e1 = service.save(e1);
    }

    @Test
    public void test() {
        HorizontalDisplayLayout<Integer, TestEntity> layout = new HorizontalDisplayLayout<Integer, TestEntity>(service,
                entityModelFactory.getModel(TestEntity.class), e1);
        layout.build();

        Component comp = layout.getChildren().iterator().next();
        assertTrue(comp instanceof HorizontalLayout);
    }

}
