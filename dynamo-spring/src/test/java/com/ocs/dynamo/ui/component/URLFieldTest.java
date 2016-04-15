package com.ocs.dynamo.ui.component;

import junitx.util.PrivateAccessor;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.vaadin.server.ExternalResource;

public class URLFieldTest extends BaseMockitoTest {

    private static final String URL = "http://www.google.nl";

    @Mock
    private TestEntityService service;

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        PrivateAccessor.setField(factory, "defaultPrecision", 2);
    }

    @Test
    public void test() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);

        URLField field = new URLField(em.getAttributeModel("url"));
        field.initContent();

        Assert.assertEquals(String.class, field.getType());
        Assert.assertNull(field.getValue());

        field.setInternalValue(URL);
        Assert.assertNotNull(field.getLink());

        ExternalResource resource = (ExternalResource) field.getLink().getResource();
        Assert.assertEquals(URL, resource.getURL());

        field.setInternalValue(null);
        Assert.assertNull(field.getLink());
    }
}
