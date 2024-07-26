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

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.dao.impl.TestEntityDao;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.annotation.ModelAction;
import com.ocs.dynamo.service.TestEntityDTO;
import com.ocs.dynamo.service.TestEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service("testEntityService")
@Transactional
public class TestEntityServiceImpl extends BaseServiceImpl<Integer, TestEntity>
        implements TestEntityService {

    @Inject
    private TestEntityDao dao;

    @Override
    protected BaseDao<Integer, TestEntity> getDao() {
        return dao;
    }

    @Override
    @ModelAction(displayName = "Partial Action", id = "PartialAction", roles = {"role12"})
    public TestEntity partialAction(TestEntityDTO dto) {
        // do nothing
        return null;
    }
}
