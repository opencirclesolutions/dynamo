package com.ocs.dynamo.ui.component;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.test.BaseMockitoTest;

public class InternalLinkFieldTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Test
	public void test() {
		EntityModel<TestEntity2> em = factory.getModel(TestEntity2.class);
		InternalLinkField<Integer, TestEntity> field = new InternalLinkField<>(em.getAttributeModel("testEntity"), null,
				null);
		field.initContent();

		Assert.assertNotNull(field.getLinkButton());
		Assert.assertEquals("", field.getLinkButton().getText());

		TestEntity t2 = new TestEntity();
		t2.setName("Bob");
		field.setValue(t2);

		Assert.assertEquals("Bob", field.getLinkButton().getText());

	}
}
