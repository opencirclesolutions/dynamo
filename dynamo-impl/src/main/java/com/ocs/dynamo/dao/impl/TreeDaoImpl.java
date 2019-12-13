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

import com.ocs.dynamo.dao.TreeDao;
import com.ocs.dynamo.domain.AbstractEntity;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;

import java.util.List;

/**
 * Base implementation of a DAO with tree support
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
public abstract class TreeDaoImpl<ID, T extends AbstractEntity<ID>> extends BaseDaoImpl<ID, T>
        implements TreeDao<ID, T> {

    /**
     * @return the QueryDSL path to the parent
     */
    protected abstract EntityPathBase<T> getParentPath();

    @Override
    public List<T> findByParentIsNull() {
        JPAQuery<T> query = createQuery();
        query.where(getParentPath().isNull());
        return query.fetch();
    }

    @Override
    public List<T> findByParent(T parent) {
        JPAQuery<T> query = createQuery();
        query.where(getParentPath().eq(parent));
        return query.fetch();
    }

}
