package nl.ocs.dao.impl;

import nl.ocs.dao.TestEntityDao;
import nl.ocs.domain.QTestEntity;
import nl.ocs.domain.TestEntity;

import org.springframework.stereotype.Repository;

import com.mysema.query.types.path.EntityPathBase;

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
