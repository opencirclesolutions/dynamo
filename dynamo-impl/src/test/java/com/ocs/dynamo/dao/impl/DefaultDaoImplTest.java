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


import com.ocs.dynamo.domain.QTestEntity;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.test.BaseMockitoTest;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultDaoImplTest extends BaseMockitoTest {

    private DefaultDaoImpl<Integer, TestEntity> dao;

    @Mock
    private EntityModelFactory entityModelFactory;

    @Mock
    private EntityModel<TestEntity> entityModel;

    @BeforeEach
    void beforeEach() {
    }

    @Test
    public void testCreateWithoutFetch() {
        dao = new DefaultDaoImpl<>(QTestEntity.testEntity, TestEntity.class);
        Mockito.when(entityModelFactory.getModel(TestEntity.class)).thenReturn(entityModel);
        ReflectionTestUtils.setField(dao, "entityModelFactory", entityModelFactory);

        assertEquals(QTestEntity.testEntity, dao.getDslRoot());
        assertEquals(TestEntity.class, dao.getEntityClass());

        // no fetch joins
        assertEquals(0, dao.getJoins().length);
    }

    @Test
    public void testCreateWithFetch() {
        dao = new DefaultDaoImpl<>(QTestEntity.testEntity, TestEntity.class,
                "testEntities");
        ReflectionTestUtils.setField(dao, "entityModelFactory", entityModelFactory);

        assertEquals(QTestEntity.testEntity, dao.getDslRoot());
        assertEquals(TestEntity.class, dao.getEntityClass());

        // check that the fetch joins are properly set
        assertEquals(1, dao.getJoins().length);
        assertEquals("testEntities", dao.getJoins()[0].getProperty());
    }

}
