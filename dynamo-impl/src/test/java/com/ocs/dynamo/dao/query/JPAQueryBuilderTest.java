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
package com.ocs.dynamo.dao.query;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.impl.JpaQueryBuilder;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.filter.And;
import com.ocs.dynamo.filter.Compare;
import com.ocs.dynamo.filter.In;
import com.ocs.dynamo.filter.IsNull;
import com.ocs.dynamo.filter.Like;
import com.ocs.dynamo.filter.Modulo;
import com.ocs.dynamo.filter.Or;
import com.ocs.dynamo.test.BaseIntegrationTest;

public class JPAQueryBuilderTest extends BaseIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Before
    public void setUp() {

        save("Bob", 25);
        save("Sally", 35);
        save("Pete", 44);
    }

    @Test
    public void testCreateCountQuery() {
        CriteriaQuery<Long> query = JpaQueryBuilder.createCountQuery(entityManager,
                TestEntity.class, null, false);
        TypedQuery<Long> tQuery = entityManager.createQuery(query);
        long count = tQuery.getSingleResult();

        Assert.assertEquals(3, count);
    }

    @Test
    public void testCreateCountQuery_Equals() {
        CriteriaQuery<Long> query = JpaQueryBuilder.createCountQuery(entityManager,
                TestEntity.class, new Compare.Equal("name", "Bob"), false);
        TypedQuery<Long> tQuery = entityManager.createQuery(query);
        long count = tQuery.getSingleResult();

        Assert.assertEquals(1, count);
    }

    @Test
    public void testCreateCountQuery_Greater() {
        CriteriaQuery<Long> query = JpaQueryBuilder.createCountQuery(entityManager,
                TestEntity.class, new Compare.Greater("age", 25L), false);
        TypedQuery<Long> tQuery = entityManager.createQuery(query);
        long count = tQuery.getSingleResult();

        Assert.assertEquals(2, count);
    }

    @Test
    public void testCreateCountQuery_GreaterOrEqual() {
        CriteriaQuery<Long> query = JpaQueryBuilder.createCountQuery(entityManager,
                TestEntity.class, new Compare.GreaterOrEqual("age", 25L), false);
        TypedQuery<Long> tQuery = entityManager.createQuery(query);
        long count = tQuery.getSingleResult();

        Assert.assertEquals(3, count);
    }

    @Test
    public void testCreateCountQuery_Less() {
        CriteriaQuery<Long> query = JpaQueryBuilder.createCountQuery(entityManager,
                TestEntity.class, new Compare.Less("age", 25L), false);
        TypedQuery<Long> tQuery = entityManager.createQuery(query);
        long count = tQuery.getSingleResult();

        Assert.assertEquals(0, count);
    }

    @Test
    public void testCreateCountQuery_LessOrEqual() {
        CriteriaQuery<Long> query = JpaQueryBuilder.createCountQuery(entityManager,
                TestEntity.class, new Compare.LessOrEqual("age", 25L), false);
        TypedQuery<Long> tQuery = entityManager.createQuery(query);
        long count = tQuery.getSingleResult();

        Assert.assertEquals(1, count);
    }

    @Test
    public void testCreateCountQuery_LikeCaseSensitive() {
        CriteriaQuery<Long> query = JpaQueryBuilder.createCountQuery(entityManager,
                TestEntity.class, new Like("name", "s%", true), false);
        TypedQuery<Long> tQuery = entityManager.createQuery(query);
        long count = tQuery.getSingleResult();

        Assert.assertEquals(0, count);
    }

    @Test
    public void testCreateCountQuery_LikeCaseInsensitive() {
        CriteriaQuery<Long> query = JpaQueryBuilder.createCountQuery(entityManager,
                TestEntity.class, new Like("name", "s%", false), false);
        TypedQuery<Long> tQuery = entityManager.createQuery(query);
        long count = tQuery.getSingleResult();

        // "Sally" should match
        Assert.assertEquals(1, count);
    }

    @Test
    public void testCreateCountQuery_LikeCaseInsensitiveInfx() {
        CriteriaQuery<Long> query = JpaQueryBuilder.createCountQuery(entityManager,
                TestEntity.class, new Like("name", "%a%", false), false);
        TypedQuery<Long> tQuery = entityManager.createQuery(query);
        long count = tQuery.getSingleResult();

        // "Sally" should match
        Assert.assertEquals(1, count);
    }

    @Test
    public void testCreateCountQuery_Between() {
        CriteriaQuery<Long> query = JpaQueryBuilder.createCountQuery(entityManager,
                TestEntity.class, new com.ocs.dynamo.filter.Between("age", 20L, 30L), false);
        TypedQuery<Long> tQuery = entityManager.createQuery(query);
        long count = tQuery.getSingleResult();

        Assert.assertEquals(1, count);
    }

    @Test
    public void testCreateCountQuery_IsNull() {
        CriteriaQuery<Long> query = JpaQueryBuilder.createCountQuery(entityManager,
                TestEntity.class, new IsNull("age"), false);
        TypedQuery<Long> tQuery = entityManager.createQuery(query);
        long count = tQuery.getSingleResult();

        Assert.assertEquals(0, count);
    }

    @Test
    public void testCreateCountQuery_In() {
        CriteriaQuery<Long> query = JpaQueryBuilder.createCountQuery(entityManager,
                TestEntity.class, new In("name", Lists.newArrayList("Bob", "Sally")), false);
        TypedQuery<Long> tQuery = entityManager.createQuery(query);
        long count = tQuery.getSingleResult();

        Assert.assertEquals(2, count);
    }

    @Test
    public void testCreateCountQuery_ModuloLiteral() {
        CriteriaQuery<Long> query = JpaQueryBuilder.createCountQuery(entityManager,
                TestEntity.class, new Modulo("age", 4, 0), false);
        TypedQuery<Long> tQuery = entityManager.createQuery(query);
        long count = tQuery.getSingleResult();

        Assert.assertEquals(1, count);
    }

    @Test
    public void testCreateCountQuery_ModuloExpression() {
        CriteriaQuery<Long> query = JpaQueryBuilder.createCountQuery(entityManager,
                TestEntity.class, new Modulo("age", "age", 0), false);
        TypedQuery<Long> tQuery = entityManager.createQuery(query);
        long count = tQuery.getSingleResult();

        Assert.assertEquals(3, count);
    }

    @Test
    public void testCreateCountQuery_And() {
        CriteriaQuery<Long> query = JpaQueryBuilder.createCountQuery(entityManager,
                TestEntity.class,
                new And(new Compare.Equal("name", "Bob"), new Compare.Equal("age", 25L)), false);
        TypedQuery<Long> tQuery = entityManager.createQuery(query);
        long count = tQuery.getSingleResult();

        Assert.assertEquals(1, count);
    }

    @Test
    public void testCreateCountQuery_Or() {
        CriteriaQuery<Long> query = JpaQueryBuilder.createCountQuery(entityManager,
                TestEntity.class,
                new Or(new Compare.Equal("name", "Bob"), new Compare.Equal("age", 35L)), false);
        TypedQuery<Long> tQuery = entityManager.createQuery(query);
        long count = tQuery.getSingleResult();

        Assert.assertEquals(2, count);
    }

    @Test
    public void testCreateFetchQuery() {
        TestEntity e1 = entityManager
                .createQuery("from TestEntity t where t.name = 'Bob'", TestEntity.class)
                .getSingleResult();

        CriteriaQuery<TestEntity> query = JpaQueryBuilder.createFetchQuery(entityManager,
                TestEntity.class, Lists.newArrayList(e1.getId()), null, null);
        TypedQuery<TestEntity> tQuery = entityManager.createQuery(query);
        List<TestEntity> entity = tQuery.getResultList();

        Assert.assertEquals(1, entity.size());
    }

    @Test
    public void testCreateFetchQuery2() {
        TestEntity e1 = entityManager
                .createQuery("from TestEntity t where t.name = 'Bob'", TestEntity.class)
                .getSingleResult();
        TestEntity2 e2 = new TestEntity2();
        e2.setTestEntity(e1);
        entityManager.persist(e2);

        // fetch join the testEntity
        CriteriaQuery<TestEntity2> query = JpaQueryBuilder.createFetchQuery(entityManager,
                TestEntity2.class, Lists.newArrayList(e2.getId()), null,
                new FetchJoinInformation[] { new FetchJoinInformation("testEntity") });
        TypedQuery<TestEntity2> tQuery = entityManager.createQuery(query);
        List<TestEntity2> entity = tQuery.getResultList();

        Assert.assertFalse(query.isDistinct());

        Assert.assertEquals(1, entity.size());
        Assert.assertEquals(e1, entity.get(0).getTestEntity());
    }

    @Test
    public void testCreateFetchSingleObjectQuery() {
        TestEntity e1 = entityManager
                .createQuery("from TestEntity t where t.name = 'Bob'", TestEntity.class)
                .getSingleResult();

        CriteriaQuery<TestEntity> query = JpaQueryBuilder
                .createFetchSingleObjectQuery(entityManager, TestEntity.class, e1.getId(), null);
        TypedQuery<TestEntity> tQuery = entityManager.createQuery(query);
        TestEntity entity = tQuery.getSingleResult();

        Assert.assertEquals(e1, entity);
    }

    private void save(String name, long age) {
        TestEntity entity = new TestEntity(name, age);
        entityManager.persist(entity);
    }
}
