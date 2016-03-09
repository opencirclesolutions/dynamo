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
package com.ocs.dynamo.dao;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.dao.impl.DefaultDaoImpl;
import com.ocs.dynamo.domain.QTestEntity;
import com.ocs.dynamo.domain.TestEntity;

public class DefaultDaoImplTest {

    private DefaultDaoImpl<Integer, TestEntity> dao;

    @Test
    public void testCreate() {
        dao = new DefaultDaoImpl<>(QTestEntity.testEntity, TestEntity.class);
        Assert.assertEquals(QTestEntity.testEntity, dao.getDslRoot());
        Assert.assertEquals(TestEntity.class, dao.getEntityClass());
    }
}
