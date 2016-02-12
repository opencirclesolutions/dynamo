package com.ocs.dynamo.dao.impl;

import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.types.path.EntityPathBase;
import com.ocs.dynamo.domain.AbstractEntity;

/**
 * Default implementation of a DAO, when default capabilities are sufficient.
 * 
 * @author Patrick Deenen
 *
 */
@Transactional
public class DefaultDaoImpl<ID, T extends AbstractEntity<ID>> extends BaseDaoImpl<ID, T> {

	protected EntityPathBase<T> dslRoot;
	protected Class<T> entityClass;

	public DefaultDaoImpl(EntityPathBase<T> dslRoot, Class<T> entityClass) {
		this.dslRoot = dslRoot;
		this.entityClass = entityClass;
	}

	@Override
	public EntityPathBase<T> getDslRoot() {
		return dslRoot;
	}

	@Override
	public Class<T> getEntityClass() {
		return entityClass;
	}

}
