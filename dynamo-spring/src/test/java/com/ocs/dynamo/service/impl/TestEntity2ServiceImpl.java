package com.ocs.dynamo.service.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.dao.TestEntity2Dao;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.service.TestEntity2Service;

@Service("testEntity2Service")
public class TestEntity2ServiceImpl extends BaseServiceImpl<Integer, TestEntity2>
        implements TestEntity2Service {

	@Inject
	private TestEntity2Dao dao;

	@Override
	protected BaseDao<Integer, TestEntity2> getDao() {
		return dao;
	}

}
