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
package org.dynamoframework.dao.query;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import org.dynamoframework.BackendIntegrationTest;
import org.dynamoframework.dao.FetchJoinInformation;
import org.dynamoframework.dao.impl.JpaQueryBuilder;
import org.dynamoframework.domain.TestEntity;
import org.dynamoframework.domain.TestEntity.TestEnum;
import org.dynamoframework.domain.TestEntity2;
import org.dynamoframework.filter.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.dynamoframework.dao.impl.JpaQueryBuilder.createFetchQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JpaQueryBuilderTest extends BackendIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    public void setUp() {
        save("Bob", 25);
        save("Sally", 35);
        save("Pete", 44);
    }

    private void insertNestedTestEntities() {
        TestEntity2 t1 = new TestEntity2();
        t1.setName("Likes science fiction");
        t1.setValue(12);

        TestEntity2 t2 = new TestEntity2();
        t2.setName("Likes adventure");
        t2.setValue(24);

        TestEntity m1 = createTestEntity("Manager 1", 30, TestEnum.C, t1);
        t1.setTestEntityAlt(m1);

        TestEntity m2 = createTestEntity("Manager 2", 40, TestEnum.A, t2);
        t2.setTestEntityAlt(m2);

        TestEntity2 t3 = new TestEntity2();
        t3.setName("Not into much");
        t3.setValue(0);
        entityManager.persist(t3);
    }

    @Test
    public void testCreateCountQuery() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, null, false);
        long count = tQuery.getSingleResult();

        assertEquals(3, count);
    }

    @Test
    public void testCreateCountQuery_Equals() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new Compare.Equal("name", "Bob"), false);
        long count = tQuery.getSingleResult();

        assertEquals(1, count);
    }

    @Test
    public void testCreateCountQuery_Greater() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new Compare.Greater("age", 25L), false);
        long count = tQuery.getSingleResult();

        assertEquals(2, count);
    }

    @Test
    public void testCreateCountQuery_GreaterOrEqual() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new Compare.GreaterOrEqual("age", 25L), false);
        long count = tQuery.getSingleResult();
        assertEquals(3, count);
    }

    @Test
    public void testCreateCountQuery_Less() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new Compare.Less("age", 25L), false);
        long count = tQuery.getSingleResult();
        assertEquals(0, count);
    }

    @Test
    public void testCreateCountQuery_LessOrEqual() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new Compare.LessOrEqual("age", 25L), false);
        long count = tQuery.getSingleResult();
        assertEquals(1, count);
    }

    @Test
    public void testCreateCountQuery_LikeCaseSensitive() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new Like("name", "s%", true), false);
        long count = tQuery.getSingleResult();
        assertEquals(0, count);
    }

    @Test
    public void testCreateCountQuery_LikeCaseInsensitive() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new Like("name", "s%", false), false);
        long count = tQuery.getSingleResult();
        // "Sally" should match
        assertEquals(1, count);
    }

    @Test
    public void testCreateCountQuery_LikeCaseInsensitiveInfix() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new Like("name", "%a%", false), false);
        long count = tQuery.getSingleResult();
        // "Sally" should match
        assertEquals(1, count);
    }

    @Test
    public void testCreateCountQuery_LikeCaseInsensitiveInfix2() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new Like("name", "%a%", false), false);
        long count = tQuery.getSingleResult();
        // "Sally" should match
        assertEquals(1, count);
    }

    @Test
    public void testCreateCountQuery_Between() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new Between("age", 20L, 30L), false);
        long count = tQuery.getSingleResult();
        assertEquals(1, count);
    }

    @Test
    public void testCreateCountQuery_IsNull() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new IsNull("age"), false);
        long count = tQuery.getSingleResult();
        assertEquals(0, count);
    }

    @Test
    public void testCreateCountQuery_In() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new In("name", List.of("Bob", "Sally")), false);
        long count = tQuery.getSingleResult();
        assertEquals(2, count);
    }

    @Test
    public void testCreateCountQuery_InEmpty() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new In("id", new ArrayList<>()), false);
        long count = tQuery.getSingleResult();
        assertEquals(0, count);
    }

    @Test
    public void testCreateCountQuery_ModuloLiteral() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new Modulo("age", 4, 0), false);
        long count = tQuery.getSingleResult();
        assertEquals(1, count);
    }

    @Test
    public void testCreateCountQuery_ModuloExpression() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new Modulo("age", "age", 0), false);
        long count = tQuery.getSingleResult();
        assertEquals(3, count);
    }

    @Test
    public void testCreateCountQuery_And() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new And(new Compare.Equal("name", "Bob"), new Compare.Equal("age", 25L)), false);
        long count = tQuery.getSingleResult();
        assertEquals(1, count);
    }

    @Test
    public void testCreateCountQuery_Not() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new Not(new Compare.Equal("name", "Bob")), false);
        long count = tQuery.getSingleResult();
        assertEquals(2, count);
    }

    @Test
    public void testCreateCountQuery_Or() {
        TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new Or(new Compare.Equal("name", "Bob"), new Compare.Equal("age", 35L)), false);
        long count = tQuery.getSingleResult();
        assertEquals(2, count);
    }

    @Test
    public void testCreateFetchQuery() {
        TestEntity e1 = entityManager.createQuery("from TestEntity t where t.name = 'Bob'", TestEntity.class).getSingleResult();

        TypedQuery<TestEntity> tQuery = JpaQueryBuilder.createFetchQuery(entityManager, TestEntity.class, List.of(e1.getId()), null, null);
        List<TestEntity> entity = tQuery.getResultList();

        assertEquals(1, entity.size());
    }

    @Test
    public void testCreateFetchQueryAdditionalFilter() {
        TestEntity e1 = entityManager.createQuery("from TestEntity t where t.name = 'Bob'", TestEntity.class).getSingleResult();

        TypedQuery<TestEntity> tQuery = JpaQueryBuilder.createFetchQuery(entityManager, TestEntity.class, List.of(e1.getId()), new Compare.Equal("name", "Bob"), null);
        List<TestEntity> entity = tQuery.getResultList();

        assertEquals(1, entity.size());
    }

    @Test
    public void testCreateFetchQuery2() {
        TestEntity e1 = entityManager.createQuery("from TestEntity t where t.name = 'Bob'", TestEntity.class).getSingleResult();
        TestEntity2 e2 = new TestEntity2();
        e2.setTestEntity(e1);
        entityManager.persist(e2);

        // fetch join the testEntity
        TypedQuery<TestEntity2> tQuery = createFetchQuery(entityManager, TestEntity2.class, List.of(e2.getId()), null, null, new FetchJoinInformation[]{new FetchJoinInformation("testEntity")});
        List<TestEntity2> entity = tQuery.getResultList();

        assertEquals(1, entity.size());
        assertEquals(e1, entity.get(0).getTestEntity());
    }

    @Test
    public void testCreateFetchSingleObjectQuery() {
        TestEntity e1 = entityManager.createQuery("from TestEntity t where t.name = 'Bob'", TestEntity.class).getSingleResult();
        TypedQuery<TestEntity> tQuery = JpaQueryBuilder.createFetchSingleObjectQuery(entityManager, TestEntity.class, e1.getId(), null);
        TestEntity entity = tQuery.getSingleResult();
        assertEquals(e1, entity);
    }

    private void save(String name, long age) {
        TestEntity entity = new TestEntity(name, age);
        entityManager.persist(entity);
    }

    @Test
    public void testCreateDistinctQuery() {
        insertNestedTestEntities();

        TypedQuery<Tuple> tQuery = JpaQueryBuilder.createDistinctQuery(new Like("testEntities.name", "Lik%", false), entityManager, TestEntity.class, "testEntities.value");
        List<Tuple> result = tQuery.getResultList();
        assertEquals(2, result.size());
    }


    private TestEntity createTestEntity(String name, long age, TestEnum te, TestEntity2... testEntities2) {
        TestEntity entity = new TestEntity(name, age);
        entity.setSomeEnum(te);
        if (testEntities2 != null) {
            for (TestEntity2 t : testEntities2) {
                entity.addTestEntity2(t);
            }
        }
        entityManager.persist(entity);
        return entity;
    }
}
