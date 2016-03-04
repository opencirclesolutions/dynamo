package com.ocs.dynamo.dao.impl;

import org.springframework.stereotype.Repository;

import com.mysema.query.types.path.EntityPathBase;
import com.ocs.dynamo.dao.TestEntity2Dao;
import com.ocs.dynamo.domain.QTestEntity2;
import com.ocs.dynamo.domain.TestEntity2;

@Repository("testEntityDao2")
public class TestEntity2DaoImpl extends BaseDaoImpl<Integer, TestEntity2>
        implements TestEntity2Dao {

	private QTestEntity2 qEntity = QTestEntity2.testEntity2;

	@Override
	public Class<TestEntity2> getEntityClass() {
		return TestEntity2.class;
	}

	@Override
	protected EntityPathBase<TestEntity2> getDslRoot() {
		return qEntity;
	}

}
