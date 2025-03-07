//package org.dynamoframework.envers.domain;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//import org.junit.jupiter.api.Test;
//
//import org.dynamoframework.domain.model.AttributeModel;
//import org.dynamoframework.domain.model.EntityModel;
//import org.dynamoframework.domain.model.EntityModelFactory;
//import org.dynamoframework.domain.model.impl.EntityModelFactoryImpl;
//
//public class PersonRevisionTest {
//
//	private EntityModelFactory entityModelFactory = new EntityModelFactoryImpl();
//
//	@Test
//	public void test() {
//		EntityModel<PersonRevision> model = entityModelFactory.getModel(PersonRevision.class);
//
//		// check that a nested module is created
//		AttributeModel am = model.getAttributeModel("entity.name");
//		assertNotNull(am);
//
//		am = model.getAttributeModel("revision");
//		assertNotNull(am);
//
//		am = model.getAttributeModel("revisionTimeStamp");
//		assertNotNull(am);
//
//		am = model.getAttributeModel("revisionType");
//		assertNotNull(am);
//	}
//}
