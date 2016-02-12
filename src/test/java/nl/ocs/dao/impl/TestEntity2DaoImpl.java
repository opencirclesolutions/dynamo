package nl.ocs.dao.impl;

import nl.ocs.dao.TestEntity2Dao;
import nl.ocs.domain.QTestEntity2;
import nl.ocs.domain.TestEntity2;

import org.springframework.stereotype.Repository;

import com.mysema.query.types.path.EntityPathBase;

@Repository("testEntityDao2")
public class TestEntity2DaoImpl extends BaseDaoImpl<Integer, TestEntity2> implements TestEntity2Dao {

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
