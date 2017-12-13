package com.ocs.dynamo.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.domain.QTestEntity;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.test.BaseMockitoTest;

public class DefaultServiceImplTest extends BaseMockitoTest {

	@Mock
	private BaseDao<Integer, TestEntity> dao;

	private DefaultServiceImpl<Integer, TestEntity> service;

	@Test
	public void testConstructWithDao() {
		service = new DefaultServiceImpl<>(dao);
		Assert.assertNull(service.getUniquePropertyIds());

		TestEntity te = new TestEntity();
		te.setName("Kevin");
		service.findIdenticalEntity(te);

		// no unique property known, no query for unique entity
		Mockito.verifyZeroInteractions(dao);
	}

	@Test
	public void testConstructWithDaoAndUniqueProperty() {
		service = new DefaultServiceImpl<>(dao, "name");
		Assert.assertEquals("name", service.getUniquePropertyIds()[0]);
		Assert.assertFalse(service.isUniqueCaseSensitive());

		TestEntity te = new TestEntity();
		te.setName("Kevin");
		service.findIdenticalEntity(te);

		Mockito.verify(dao).findByUniqueProperty("name", "Kevin", false);
	}

	@Test
	public void testConstructWithDaoAndUniquePropertyCaseSensitive() {
		service = new DefaultServiceImpl<>(dao, new String[] { "name" }, true);
		Assert.assertEquals("name", service.getUniquePropertyIds()[0]);
		Assert.assertTrue(service.isUniqueCaseSensitive());

		TestEntity te = new TestEntity();
		te.setName("Kevin");
		service.findIdenticalEntity(te);

		Mockito.verify(dao).findByUniqueProperty("name", "Kevin", true);
	}

	@Test
	public void testConstructWithDslAndClass() {
		service = new DefaultServiceImpl<>(QTestEntity.testEntity, TestEntity.class);
		Assert.assertNull(service.getUniquePropertyIds());
	}

	@Test
	public void testConstructWithDslAndClassAndProperty() {
		service = new DefaultServiceImpl<>(QTestEntity.testEntity, TestEntity.class, new String[] { "name" });
		Assert.assertEquals("name", service.getUniquePropertyIds()[0]);
		Assert.assertFalse(service.isUniqueCaseSensitive());
	}

	@Test
	public void testConstructWithDslAndClassAndPropertyCaseSensitive() {
		service = new DefaultServiceImpl<>(QTestEntity.testEntity, TestEntity.class, new String[] { "name" }, true);
		Assert.assertEquals("name", service.getUniquePropertyIds()[0]);
		Assert.assertTrue(service.isUniqueCaseSensitive());
	}
}
