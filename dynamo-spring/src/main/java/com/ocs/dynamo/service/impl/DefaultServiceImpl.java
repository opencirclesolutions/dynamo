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
 * Default service implementation that uses the DefaultDaoImpl when no other implementation is
 * given.
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
    private String uniquePropertyId;

    /**
     * Constructor
     * 
     * @param dslRoot
     * @param entityClass
     */
    public DefaultServiceImpl(EntityPathBase<T> dslRoot, Class<T> entityClass) {
        this(dslRoot, entityClass, null);
    }

    /**
     * Constructor
     * 
     * @param dslRoot
     * @param entityClass
     * @param uniquePropertyId
     */
    public DefaultServiceImpl(EntityPathBase<T> dslRoot, Class<T> entityClass,
            String uniquePropertyId) {
        dao = new DefaultDaoImpl<>(dslRoot, entityClass);
        this.uniquePropertyId = uniquePropertyId;
    }

    /**
     * 
     * @param dao
     */
    public DefaultServiceImpl(BaseDao<ID, T> dao) {
        this(dao, null);
    }

    /**
     * 
     * @param dao
     * @param uniquePropertyId
     */
    public DefaultServiceImpl(BaseDao<ID, T> dao, String uniquePropertyId) {
        this.dao = dao;
        this.uniquePropertyId = uniquePropertyId;
    }

    @Override
    protected BaseDao<ID, T> getDao() {
        return dao;
    }

    /**
     * Check for an identical entry - by default we to this by simply checking for a unique property
     */
    @Override
    protected T findIdenticalEntity(T entity) {
        if (uniquePropertyId == null) {
            return super.findIdenticalEntity(entity);
        }
        return getDao().findByUniqueProperty(uniquePropertyId,
                ClassUtils.getFieldValue(entity, uniquePropertyId), false);
    }

    public String getUniquePropertyId() {
        return uniquePropertyId;
    }

}
