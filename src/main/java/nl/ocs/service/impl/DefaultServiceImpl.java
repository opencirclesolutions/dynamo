package nl.ocs.service.impl;

import nl.ocs.dao.BaseDao;
import nl.ocs.dao.impl.DefaultDaoImpl;
import nl.ocs.domain.AbstractEntity;

import com.mysema.query.types.path.EntityPathBase;

/**
 * Default service implementation that uses the DefaultDaoImpl when no other
 * implementation is given.
 * 
 * @author Patrick Deenen
 *
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
