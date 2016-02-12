package nl.ocs.service.impl;

import javax.inject.Inject;

import nl.ocs.dao.BaseDao;
import nl.ocs.dao.TestEntityDao;
import nl.ocs.domain.TestEntity;
import nl.ocs.service.TestEntityService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("testEntityService")
@Transactional
public class TestEntityServiceImpl extends BaseServiceImpl<Integer, TestEntity> implements
		TestEntityService {

	@Inject
	private TestEntityDao dao;

	@Override
	protected BaseDao<Integer, TestEntity> getDao() {
		return dao;
	}

}
