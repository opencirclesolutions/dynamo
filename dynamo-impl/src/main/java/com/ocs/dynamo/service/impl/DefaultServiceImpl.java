/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.service.impl;

import com.mysema.query.types.path.EntityPathBase;
import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.dao.impl.DefaultDaoImpl;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.utils.ClassUtils;

/**
 * Default service implementation that uses the DefaultDaoImpl when no other
 * implementation is given.
 * 
 * @author Patrick Deenen
 */
public class DefaultServiceImpl<ID, T extends AbstractEntity<ID>> extends BaseServiceImpl<ID, T> {

	/**
	 * The DAO
	 */
	private BaseDao<ID, T> dao;

	/**
	 * The name of the property that is used to check if a value is unique
	 */
	private String[] uniquePropertyIds;

	/**
	 * Whether the unique value is case sensitive
	 */
	private boolean uniqueCaseSensitive;

	/**
	 * Constructor - no unique property
	 * 
	 * @param dslRoot
	 *            the DSL root
	 * @param entityClass
	 *            the entity class
	 */
	public DefaultServiceImpl(EntityPathBase<T> dslRoot, Class<T> entityClass) {
		this(dslRoot, entityClass, null);
	}

	/**
	 * Constructor - with unique property, not case sensitive
	 * 
	 * @param dslRoot
	 *            the DSL root
	 * @param entityClass
	 *            the entity class
	 * @param uniquePropertyIds
	 *            the unique property name
	 */
	public DefaultServiceImpl(EntityPathBase<T> dslRoot, Class<T> entityClass, String[] uniquePropertyIds) {
		this(dslRoot, entityClass, uniquePropertyIds, false);
	}

	/**
	 * Constructor
	 * 
	 * @param dslRoot
	 *            the QueryDSL root
	 * @param entityClass
	 *            the entity class
	 * @param uniquePropertyIds
	 * @param uniqueCaseSensitive
	 */
	public DefaultServiceImpl(EntityPathBase<T> dslRoot, Class<T> entityClass, String[] uniquePropertyIds,
			boolean uniqueCaseSensitive) {
		dao = new DefaultDaoImpl<>(dslRoot, entityClass);
		this.uniquePropertyIds = uniquePropertyIds;
		this.uniqueCaseSensitive = uniqueCaseSensitive;
	}

	/**
	 * 
	 * @param dao
	 */
	public DefaultServiceImpl(BaseDao<ID, T> dao) {
		this(dao, (String[]) null);
	}

	/**
	 * Constructor
	 * 
	 * @param dao
	 *            the DAO
	 * @param uniquePropertyIds
	 *            the name of the property that must be unique
	 */
	public DefaultServiceImpl(BaseDao<ID, T> dao, String... uniquePropertyIds) {
		this(dao, uniquePropertyIds, false);
	}

	/**
	 * Constructor
	 * 
	 * @param dao
	 *            the DAO used to retrieve the data
	 * @param uniquePropertyIds
	 *            the unique property
	 * @param uniqueCaseSensitive
	 *            whether the unique property is case sensitive
	 */
	public DefaultServiceImpl(BaseDao<ID, T> dao, String[] uniquePropertyIds, boolean uniqueCaseSensitive) {
		this.dao = dao;
		this.uniquePropertyIds = uniquePropertyIds;
		this.uniqueCaseSensitive = uniqueCaseSensitive;
	}

	@Override
	protected BaseDao<ID, T> getDao() {
		return dao;
	}

	/**
	 * Check for an identical entry - by default we to this by simply checking
	 * for a unique property
	 */
	@Override
	protected T findIdenticalEntity(T entity) {
		if (uniquePropertyIds == null || uniquePropertyIds.length == 0) {
			return super.findIdenticalEntity(entity);
		}

		for (String u : uniquePropertyIds) {
			T t = getDao().findByUniqueProperty(u, ClassUtils.getFieldValue(entity, u), uniqueCaseSensitive);
			if (t != null) {
				return t;
			}
		}
		return null;
	}

	public String[] getUniquePropertyIds() {
		return uniquePropertyIds;
	}

	public boolean isUniqueCaseSensitive() {
		return uniqueCaseSensitive;
	}

}
