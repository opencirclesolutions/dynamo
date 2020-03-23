package com.ocs.dynamo.domain.model.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.TestEntity;

public class LazyEntityModelWrapperTest {

    EntityModelFactoryImpl factory = new EntityModelFactoryImpl();

    @Test
    public void test() {
        LazyEntityModelWrapper<TestEntity> wrapper = new LazyEntityModelWrapper<>(factory, "TestEntity", TestEntity.class);

        // verify that the model has not been loaded yet
        assertFalse(factory.hasModel("TestEntity"));

        // call a method and verify the model is created
        wrapper.getAttributeGroups();
        assertTrue(factory.hasModel("TestEntity"));
    }
}
