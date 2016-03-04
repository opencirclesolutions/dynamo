package com.ocs.dynamo.service.impl;

import com.mysema.query.types.path.EntityPathBase;
import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.dao.impl.DefaultDaoImpl;
import com.ocs.dynamo.domain.AbstractEntity;

/**
 * Default service implementation that uses the DefaultDaoImpl when no other
 * implementation is given.
 * 
 * @author Patrick Deenen
 */
public class DefaultServiceImpl<ID, T extends AbstractEntity<ID>> extends BaseServiceImpl<ID, T> {

	protected BaseDao<ID, T> dao;

	public DefaultServiceImpl(EntityPathBase<T> dslRoot, Class<T> entityClass) {
		super();
		dao = new DefaultDaoImpl<>(dslRoot, entityClass);
	}

	public DefaultServiceImpl(BaseDao<ID, T> dao) {
		super();
		this.dao = dao;
	}

	@Override
	protected BaseDao<ID, T> getDao() {
		return dao;
	}

}
