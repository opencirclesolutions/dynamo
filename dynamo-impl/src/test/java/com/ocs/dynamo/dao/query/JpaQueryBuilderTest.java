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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.BackendIntegrationTest;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.QueryFunction;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.dao.impl.JpaQueryBuilder;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity.TestEnum;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.filter.And;
import com.ocs.dynamo.filter.Compare;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.filter.In;
import com.ocs.dynamo.filter.IsNull;
import com.ocs.dynamo.filter.Like;
import com.ocs.dynamo.filter.Modulo;
import com.ocs.dynamo.filter.Not;
import com.ocs.dynamo.filter.Or;

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
		TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class,
				new Compare.Equal("name", "Bob"), false);
		long count = tQuery.getSingleResult();

		assertEquals(1, count);
	}

	@Test
	public void testCreateCountQuery_Greater() {
		TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class,
				new Compare.Greater("age", 25L), false);
		long count = tQuery.getSingleResult();

		assertEquals(2, count);
	}

	@Test
	public void testCreateCountQuery_GreaterOrEqual() {
		TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class,
				new Compare.GreaterOrEqual("age", 25L), false);
		long count = tQuery.getSingleResult();
		assertEquals(3, count);
	}

	@Test
	public void testCreateCountQuery_Less() {
		TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class,
				new Compare.Less("age", 25L), false);
		long count = tQuery.getSingleResult();
		assertEquals(0, count);
	}

	@Test
	public void testCreateCountQuery_LessOrEqual() {
		TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class,
				new Compare.LessOrEqual("age", 25L), false);
		long count = tQuery.getSingleResult();
		assertEquals(1, count);
	}

	@Test
	public void testCreateCountQuery_LikeCaseSensitive() {
		TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class,
				new Like("name", "s%", true), false);
		long count = tQuery.getSingleResult();
		assertEquals(0, count);
	}

	@Test
	public void testCreateCountQuery_LikeCaseInsensitive() {
		TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class,
				new Like("name", "s%", false), false);
		long count = tQuery.getSingleResult();
		// "Sally" should match
		assertEquals(1, count);
	}

	@Test
	public void testCreateCountQuery_LikeCaseInsensitiveInfx() {
		TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class,
				new Like("name", "%a%", false), false);
		long count = tQuery.getSingleResult();
		// "Sally" should match
		assertEquals(1, count);
	}

	@Test
	public void testCreateCountQuery_Between() {
		TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class,
				new com.ocs.dynamo.filter.Between("age", 20L, 30L), false);
		long count = tQuery.getSingleResult();
		assertEquals(1, count);
	}

	@Test
	public void testCreateCountQuery_IsNull() {
		TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class, new IsNull("age"),
				false);
		long count = tQuery.getSingleResult();
		assertEquals(0, count);
	}

	@Test
	public void testCreateCountQuery_In() {
		TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class,
				new In("name", Lists.newArrayList("Bob", "Sally")), false);
		long count = tQuery.getSingleResult();
		assertEquals(2, count);
	}

	@Test
	public void testCreateCountQuery_InEmpty() {
		TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class,
				new In("id", new ArrayList<>()), false);
		long count = tQuery.getSingleResult();
		assertEquals(0, count);
	}

	@Test
	public void testCreateCountQuery_ModuloLiteral() {
		TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class,
				new Modulo("age", 4, 0), false);
		long count = tQuery.getSingleResult();
		assertEquals(1, count);
	}

	@Test
	public void testCreateCountQuery_ModuloExpression() {
		TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class,
				new Modulo("age", "age", 0), false);
		long count = tQuery.getSingleResult();
		assertEquals(3, count);
	}

	@Test
	public void testCreateCountQuery_And() {
		TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class,
				new And(new Compare.Equal("name", "Bob"), new Compare.Equal("age", 25L)), false);
		long count = tQuery.getSingleResult();
		assertEquals(1, count);
	}

	@Test
	public void testCreateCountQuery_Not() {
		TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class,
				new Not(new Compare.Equal("name", "Bob")), false);
		long count = tQuery.getSingleResult();
		assertEquals(2, count);
	}

	@Test
	public void testCreateCountQuery_Or() {
		TypedQuery<Long> tQuery = JpaQueryBuilder.createCountQuery(entityManager, TestEntity.class,
				new Or(new Compare.Equal("name", "Bob"), new Compare.Equal("age", 35L)), false);
		long count = tQuery.getSingleResult();
		assertEquals(2, count);
	}

	@Test
	public void testCreateFetchQuery() {
		TestEntity e1 = entityManager.createQuery("from TestEntity t where t.name = 'Bob'", TestEntity.class)
				.getSingleResult();

		TypedQuery<TestEntity> tQuery = JpaQueryBuilder.createFetchQuery(entityManager, TestEntity.class,
				Lists.newArrayList(e1.getId()), null, null);
		List<TestEntity> entity = tQuery.getResultList();

		assertEquals(1, entity.size());
	}

	@Test
	public void testCreateFetchQuery2() {
		TestEntity e1 = entityManager.createQuery("from TestEntity t where t.name = 'Bob'", TestEntity.class)
				.getSingleResult();
		TestEntity2 e2 = new TestEntity2();
		e2.setTestEntity(e1);
		entityManager.persist(e2);

		// fetch join the testEntity
		TypedQuery<TestEntity2> tQuery = JpaQueryBuilder.createFetchQuery(entityManager, TestEntity2.class,
				Lists.newArrayList(e2.getId()), null,
				new FetchJoinInformation[] { new FetchJoinInformation("testEntity") });
		List<TestEntity2> entity = tQuery.getResultList();

		assertEquals(1, entity.size());
		assertEquals(e1, entity.get(0).getTestEntity());
	}

	@Test
	public void testCreateFetchSingleObjectQuery() {
		TestEntity e1 = entityManager.createQuery("from TestEntity t where t.name = 'Bob'", TestEntity.class)
				.getSingleResult();
		TypedQuery<TestEntity> tQuery = JpaQueryBuilder.createFetchSingleObjectQuery(entityManager, TestEntity.class,
				e1.getId(), null);
		TestEntity entity = tQuery.getSingleResult();
		assertEquals(e1, entity);
	}

	private void save(String name, long age) {
		TestEntity entity = new TestEntity(name, age);
		entityManager.persist(entity);
	}

	@Test
	public void testCreateSelectQuery() {
		TestEntity e1 = entityManager.createQuery("from TestEntity t where t.name = 'Bob'", TestEntity.class)
				.getSingleResult();
		TestEntity e2 = entityManager.createQuery("from TestEntity t where t.name = 'Pete'", TestEntity.class)
				.getSingleResult();

		SortOrder sortName = new SortOrder("name");
		Filter filter = new In("name", Lists.newArrayList(e1.getName(), e2.getName()));
		TypedQuery<Object[]> tQuery = JpaQueryBuilder.createSelectQuery(filter, entityManager, TestEntity.class,
				new String[] { "name", "age" }, new SortOrders(sortName));
		List<Object[]> result = tQuery.getResultList();

		assertEquals(2, result.size());
		assertEquals(e1.getName(), result.get(0)[0]);
		assertEquals(e1.getAge(), result.get(0)[1]);
		assertEquals(e2.getName(), result.get(1)[0]);
		assertEquals(e2.getAge(), result.get(1)[1]);
	}

	@Test
	public void testCreateSelectAggregateQuery() {
		Object base = entityManager.createQuery("select avg(age), count(1), sum(age) from TestEntity")
				.getSingleResult();

		TypedQuery<Object[]> tQuery = JpaQueryBuilder.createSelectQuery(null, entityManager, TestEntity.class,
				new String[] { QueryFunction.AF_AVG.with("age"), QueryFunction.AF_COUNT.with("age"),
						QueryFunction.AF_SUM.with("age") },
				null);
		List<Object[]> result = tQuery.getResultList();

		assertEquals(1, result.size());
		Object[] br = (Object[]) base;
		assertTrue(br[0].equals(result.get(0)[0]));
		assertTrue(br[1].equals(result.get(0)[1]));
		assertTrue(br[2].equals(result.get(0)[2]));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testCreateSelectAggregateAndGroupQuery() {
		insertNestedTestEntities();

		List<Object[]> base = entityManager.createQuery(
				"select avg(age), count(1), sum(age), someEnum from TestEntity group by someEnum order by someEnum")
				.getResultList();

		TypedQuery<Object[]> tQuery = JpaQueryBuilder.createSelectQuery(
				null, entityManager, TestEntity.class, new String[] { QueryFunction.AF_AVG.with("age"),
						QueryFunction.AF_COUNT.with("age"), QueryFunction.AF_SUM.with("age"), "someEnum" },
				new SortOrders(new SortOrder("someEnum")));
		List<Object[]> result = tQuery.getResultList();

		assertEquals(3, result.size());
		Object[] br = base.get(0);
		assertTrue(br[0].equals(result.get(0)[0]));
		assertTrue(br[1].equals(result.get(0)[1]));
		assertTrue(br[2].equals(result.get(0)[2]));
		// assertTrue(br[3].equals(result.get(0)[3]));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testCreateSelectAggregateJoinAndGroupQuery() {
		insertNestedTestEntities();

		List<Object[]> base = entityManager.createQuery(
				"select t1.name, sum(t2.value) from TestEntity t1 join t1.testEntities t2 group by t1.name order by t1.name")
				.getResultList();

		TypedQuery<Object[]> tQuery = JpaQueryBuilder.createSelectQuery(null, entityManager, TestEntity.class,
				new String[] { "name", QueryFunction.AF_SUM.with("testEntities.value") },
				new SortOrders(new SortOrder("name")));
		List<Object[]> result = tQuery.getResultList();

		assertEquals(2, result.size());
		Object[] br = base.get(0);
		assertTrue(br[0].equals(result.get(0)[0]));
		assertTrue(br[1].equals(result.get(0)[1]));

		Object[] br1 = base.get(1);
		assertTrue(br1[0].equals(result.get(1)[0]));
		assertTrue(br1[1].equals(result.get(1)[1]));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testCreateSelectAggregateJoinAndGroupQuery2() {
		insertNestedTestEntities();

		List<Object[]> base = entityManager.createQuery(
				"select t2.value, sum(t3.age) from TestEntity t1 join t1.testEntities t2 join t2.testEntityAlt t3 group by t2.value order by t2.value")
				.getResultList();

		TypedQuery<Object[]> tQuery = JpaQueryBuilder.createSelectQuery(null, entityManager, TestEntity.class,
				new String[] { "testEntities.value", QueryFunction.AF_SUM.with("testEntities.testEntityAlt.age") },
				new SortOrders(new SortOrder("testEntities.value")));
		List<Object[]> result = tQuery.getResultList();

		assertEquals(2, result.size());
		Object[] br = base.get(0);
		assertTrue(br[0].equals(result.get(0)[0]));
		assertTrue(br[1].equals(result.get(0)[1]));
	}

	@Test
	public void testCreateDistinctQuery() {
		insertNestedTestEntities();

		TypedQuery<Tuple> tQuery = JpaQueryBuilder.createDistinctQuery(new Like("testEntities.name", "Lik%", false),
				entityManager, TestEntity.class, "testEntities.value");
		List<Tuple> result = tQuery.getResultList();
		assertEquals(2, result.size());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testCreateSelectDistinctCountQuery() {
		List<Long> base = entityManager.createQuery("select count( distinct age) from TestEntity").getResultList();

		TypedQuery<Object[]> tQuery = JpaQueryBuilder.createSelectQuery(null, entityManager, TestEntity.class,
				new String[] { QueryFunction.AF_COUNT_DISTINCT.with("age") }, null);
		List<?> result = tQuery.getResultList();

		assertEquals(1, result.size());
		Long b = base.get(0);
		Long r = (Long) result.get(0);
		assertTrue(b.equals(r));
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
