package nl.ocs.dao.impl;

import java.util.List;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.path.EntityPathBase;

import nl.ocs.dao.TreeDao;
import nl.ocs.domain.AbstractEntity;

/**
 * Base implementation of a DAO with tree support
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
public abstract class TreeDaoImpl<ID, T extends AbstractEntity<ID>> extends BaseDaoImpl<ID, T>
		implements TreeDao<ID, T> {

	/**
	 * 
	 * @return the QueryDSL path to the parent
	 */
	protected abstract EntityPathBase<T> getParentPath();

	@Override
	public List<T> findByParentIsNull() {
		JPAQuery query = createQuery();
		query.where(getParentPath().isNull());
		return query.list(getDslRoot());
	}

	@Override
	public List<T> findByParent(T parent) {
		JPAQuery query = createQuery();
		query.where(getParentPath().eq(parent));
		return query.list(getDslRoot());
	}

}
