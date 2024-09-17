package org.dynamoframework.service.impl;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.dynamoframework.dao.BaseDao;
import org.dynamoframework.dao.impl.TestEntity2Dao;
import org.dynamoframework.domain.TestEntity2;
import org.dynamoframework.service.TestEntity2Service;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service("testEntity2Service")
public class TestEntity2ServiceImpl extends BaseServiceImpl<Integer, TestEntity2>
        implements TestEntity2Service {

    @Inject
    private TestEntity2Dao dao;

    @Override
    protected BaseDao<Integer, TestEntity2> getDao() {
        return dao;
    }

}