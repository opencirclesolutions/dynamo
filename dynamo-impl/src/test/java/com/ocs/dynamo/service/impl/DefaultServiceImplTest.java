package com.ocs.dynamo.service.impl;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.domain.QTestEntity;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.test.BaseMockitoTest;
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
