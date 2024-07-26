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
package com.ocs.dynamo.dao.impl;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.querydsl.core.types.dsl.EntityPathBase;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * A default DAO implementation for handling database access for a simple
 *
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key
 * @param <T>  the type of the entity
 */
@Transactional
public class DefaultDaoImpl<ID, T extends AbstractEntity<ID>> extends BaseDaoImpl<ID, T> {

    private final EntityPathBase<T> dslRoot;

    private final Class<T> entityClass;

    private final String[] fetchPropertyIds;

	/**
	 * Constructor
	 * @param dslRoot          the query DSL root path
	 * @param entityClass      the entity class
	 */
	public DefaultDaoImpl(EntityPathBase<T> dslRoot, Class<T> entityClass) {
		this(dslRoot, entityClass, (String[]) null);
	}

	/**
	 * Constructor
	 * 
	 * @param dslRoot          the query DSL root path
	 * @param entityClass      the entity class
	 * @param fetchPropertyIds the IDs of the properties to fetch
	 */
	public DefaultDaoImpl(EntityPathBase<T> dslRoot, Class<T> entityClass, String... fetchPropertyIds) {
		this.dslRoot = dslRoot;
		this.entityClass = entityClass;
		this.fetchPropertyIds = fetchPropertyIds;
	}

	@Override
	public EntityPathBase<T> getDslRoot() {
		return dslRoot;
	}

	@Override
	public Class<T> getEntityClass() {
		return entityClass;
	}

	@Override
	protected FetchJoinInformation[] getJoins() {
		if (fetchPropertyIds == null || fetchPropertyIds.length == 0) {
			return super.getJoins();
		}

		return Arrays.stream(fetchPropertyIds).map(FetchJoinInformation::new).toList()
				.toArray(new FetchJoinInformation[0]);
	}

	@Override
	protected FetchJoinInformation[] getDetailJoins() {
		if (fetchPropertyIds == null || fetchPropertyIds.length == 0) {
			return super.getDetailJoins();
		}

		return Arrays.stream(fetchPropertyIds).map(FetchJoinInformation::new).toList()
				.toArray(new FetchJoinInformation[0]);
	}
}
