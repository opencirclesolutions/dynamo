package com.ocs.dynamo.dao;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.dao.impl.DefaultDaoImpl;
import com.ocs.dynamo.domain.TestEntity;

public class DefaultDaoImplTest {

	private DefaultDaoImpl<Integer, TestEntity> dao;

	@Test
	public void testCreate() {
		dao = new DefaultDaoImpl<>(QTestEntity.testEntity, TestEntity.class);
		Assert.assertEquals(QTestEntity.testEntity, dao.getDslRoot());
		Assert.assertEquals(TestEntity.class, dao.getEntityClass());
	}
}
