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
import org.dynamoframework.domain.QTestEntity;
import org.dynamoframework.domain.TestEntity;
import org.dynamoframework.test.BaseMockitoTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class DefaultServiceImplTest extends BaseMockitoTest {

    @Mock
    private BaseDao<Integer, TestEntity> dao;

    private DefaultServiceImpl<Integer, TestEntity> service;

    @Test
    public void testConstructWithDao() {
        service = new DefaultServiceImpl<>(dao);
        assertNull(service.getUniquePropertyIds());

        TestEntity te = new TestEntity();
        te.setName("Kevin");
        service.findIdenticalEntity(te);

        // no unique property known, no query for unique entity
        verifyNoInteractions(dao);
    }

    @Test
    public void testConstructWithDaoAndUniqueProperty() {
        service = new DefaultServiceImpl<>(dao, "name");
        assertEquals("name", service.getUniquePropertyIds()[0]);
        assertFalse(service.isUniqueCaseSensitive());

        TestEntity te = new TestEntity();
        te.setName("Kevin");
        service.findIdenticalEntity(te);

        verify(dao).findByUniqueProperty("name", "Kevin", false);
    }

    @Test
    public void testConstructWithDaoAndUniquePropertyCaseSensitive() {
        service = new DefaultServiceImpl<>(dao, new String[] { "name" }, true);
        assertEquals("name", service.getUniquePropertyIds()[0]);
        assertTrue(service.isUniqueCaseSensitive());

        TestEntity te = new TestEntity();
        te.setName("Kevin");
        service.findIdenticalEntity(te);

        verify(dao).findByUniqueProperty("name", "Kevin", true);
    }

    @Test
    public void testConstructWithDslAndClass() {
        service = new DefaultServiceImpl<>(QTestEntity.testEntity, TestEntity.class);
        assertNull(service.getUniquePropertyIds());
    }

    @Test
    public void testConstructWithDslAndClassAndProperty() {
        service = new DefaultServiceImpl<>(QTestEntity.testEntity, TestEntity.class, new String[] { "name" });
        assertEquals("name", service.getUniquePropertyIds()[0]);
        assertFalse(service.isUniqueCaseSensitive());
    }

    @Test
    public void testConstructWithDslAndClassAndPropertyCaseSensitive() {
        service = new DefaultServiceImpl<>(QTestEntity.testEntity, TestEntity.class, new String[] { "name" }, true);
        assertEquals("name", service.getUniquePropertyIds()[0]);
        assertTrue(service.isUniqueCaseSensitive());
    }
}
