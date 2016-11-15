package com.ocs.dynamo.domain.model;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;

public class LazyEntityModelWrapperTest {

	EntityModelFactory factory = new EntityModelFactoryImpl();

	@Test
	public void test() {
		LazyEntityModelWrapper<TestEntity> wrapper = new LazyEntityModelWrapper<>(factory, "TestEntity",
		        TestEntity.class);

		// verify that the model has not been loaded yet
		Assert.assertFalse(factory.hasModel("TestEntity"));

		// call a method and verify the model is created
		wrapper.getAttributeGroups();
		Assert.assertTrue(factory.hasModel("TestEntity"));
	}
}
