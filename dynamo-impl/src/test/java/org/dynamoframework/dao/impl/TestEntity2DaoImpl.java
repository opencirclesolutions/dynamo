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
package org.dynamoframework.dao.impl;

import com.querydsl.core.types.dsl.EntityPathBase;
import org.dynamoframework.domain.QTestEntity2;
import org.dynamoframework.domain.TestEntity2;
import org.springframework.stereotype.Repository;

@Repository("testEntityDao2")
public class TestEntity2DaoImpl extends BaseDaoImpl<Integer, TestEntity2>
        implements TestEntity2Dao {

    private QTestEntity2 qEntity = QTestEntity2.testEntity2;

    @Override
    public Class<TestEntity2> getEntityClass() {
        return TestEntity2.class;
    }

    @Override
    protected EntityPathBase<TestEntity2> getDslRoot() {
        return qEntity;
    }

}
