package nl.ocs.dao.impl;

import nl.ocs.domain.AbstractEntity;

import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.types.path.EntityPathBase;

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
