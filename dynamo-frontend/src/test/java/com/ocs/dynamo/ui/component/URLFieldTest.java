package com.ocs.dynamo.ui.component;

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
import com.vaadin.ui.TextField;

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

		Assert.assertEquals(tf, field.getTextField());
		//Assert.assertNull(field.getValue());

		field.doSetValue(URL);
		Assert.assertNotNull(field.getLink());

		ExternalResource resource = (ExternalResource) field.getLink().getResource();
		Assert.assertEquals(URL, resource.getURL());
		Assert.assertEquals(URL, tf.getValue());

		field.doSetValue(null);
		Assert.assertNull(field.getLink());
	}
}
