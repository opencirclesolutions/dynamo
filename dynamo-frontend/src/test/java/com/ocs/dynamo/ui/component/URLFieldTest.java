package com.ocs.dynamo.ui.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.vaadin.flow.component.textfield.TextField;

public class URLFieldTest extends BaseMockitoTest {

    private static final String URL = "http://www.google.nl";

    @Mock
    private TestEntityService service;

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    @Test
    public void test() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

        TextField tf = new TextField();

        URLField field = new URLField(tf, em.getAttributeModel("url"), true);
        field.initContent();

        assertEquals(tf, field.getTextField());

        field.setPresentationValue(URL);
        assertNotNull(field.getLink());

        String href = field.getLink().getHref();
        assertEquals(URL, href);

        field.setPresentationValue(null);
        assertNull(field.getLink());
    }
}
