package nl.ocs.service.impl;

import javax.inject.Inject;

import nl.ocs.dao.BaseDao;
import nl.ocs.dao.TestEntity2Dao;
import nl.ocs.domain.TestEntity2;
import nl.ocs.service.TestEntity2Service;

import org.springframework.stereotype.Service;

@Service("testEntity2Service")
public class TestEntity2ServiceImpl extends BaseServiceImpl<Integer, TestEntity2> implements TestEntity2Service {

	@Inject
	private TestEntity2Dao dao;
	
	@Override
	protected BaseDao<Integer, TestEntity2> getDao() {
		return dao;
	}

}
