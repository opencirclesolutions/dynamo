package com.ocs.dynamo.dao.impl;

import org.springframework.stereotype.Repository;

import com.mysema.query.types.path.EntityPathBase;
import com.ocs.dynamo.dao.TestEntityDao;
import com.ocs.dynamo.domain.TestEntity;

@Repository("testEntityDao")
public class TestEntityDaoImpl extends TreeDaoImpl<Integer, TestEntity> implements TestEntityDao {

	@Override
	protected EntityPathBase<TestEntity> getDslRoot() {
		return QTestEntity.testEntity;
	}

	@Override
	public Class<TestEntity> getEntityClass() {
		return TestEntity.class;
	}

	@Override
	protected EntityPathBase<TestEntity> getParentPath() {
		return QTestEntity.testEntity.parent;
	}

}
