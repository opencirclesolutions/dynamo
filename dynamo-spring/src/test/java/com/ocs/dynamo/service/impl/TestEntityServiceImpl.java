package com.ocs.dynamo.service.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.dao.TestEntityDao;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.service.TestEntityService;

@Service("testEntityService")
@Transactional
public class TestEntityServiceImpl extends BaseServiceImpl<Integer, TestEntity>
        implements TestEntityService {

	@Inject
	private TestEntityDao dao;

	@Override
	protected BaseDao<Integer, TestEntity> getDao() {
		return dao;
	}

}
