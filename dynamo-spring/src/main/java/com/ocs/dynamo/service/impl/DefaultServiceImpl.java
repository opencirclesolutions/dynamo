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

/**
 * Default service implementation that uses the DefaultDaoImpl when no other implementation is
 * given.
 * 
 * @author Patrick Deenen
 */
public class DefaultServiceImpl<ID, T extends AbstractEntity<ID>> extends BaseServiceImpl<ID, T> {

    private BaseDao<ID, T> dao;

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
