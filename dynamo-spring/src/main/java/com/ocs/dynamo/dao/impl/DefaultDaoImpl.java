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

import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.types.path.EntityPathBase;
import com.ocs.dynamo.domain.AbstractEntity;

/**
 * A default DAO implementation
 *
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 */
@Transactional
public class DefaultDaoImpl<ID, T extends AbstractEntity<ID>> extends BaseDaoImpl<ID, T> {

    private EntityPathBase<T> dslRoot;

    private Class<T> entityClass;

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
