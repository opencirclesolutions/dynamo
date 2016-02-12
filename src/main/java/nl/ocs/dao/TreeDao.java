package nl.ocs.dao;

import java.util.List;

import nl.ocs.domain.AbstractEntity;

public interface TreeDao<ID, T extends AbstractEntity<ID>> extends BaseDao<ID, T> {

	/**
	 * Find all root objects.
	 * 
	 * @return All root domain objects
	 */
	public List<T> findByParentIsNull();

	/**
	 * Find all children for a given parent.
	 * 
	 * @param parent
	 *            The parent
	 * @return ALl children for the given parent
	 */
	public List<T> findByParent(T parent);

}
