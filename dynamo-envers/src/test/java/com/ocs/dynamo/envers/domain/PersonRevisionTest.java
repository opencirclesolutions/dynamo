package com.ocs.dynamo.envers.domain;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;

public class PersonRevisionTest {

	private EntityModelFactory emf = new EntityModelFactoryImpl();

	@Test
	public void test() {
		EntityModel<PersonRevision> model = emf.getModel(PersonRevision.class);

		// check that a nested module is created
		AttributeModel am = model.getAttributeModel("entity.name");
		Assert.assertNotNull(am);

		am = model.getAttributeModel("revision");
		Assert.assertNotNull(am);

		am = model.getAttributeModel("revisionTimeStamp");
		Assert.assertNotNull(am);

		am = model.getAttributeModel("revisionType");
		Assert.assertNotNull(am);
	}
}
