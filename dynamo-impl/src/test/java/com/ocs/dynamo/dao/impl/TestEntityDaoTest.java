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

import com.ocs.dynamo.BackendIntegrationTest;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.PageableImpl;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrder.Direction;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.filter.And;
import com.ocs.dynamo.filter.Compare;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.filter.In;
import com.ocs.dynamo.utils.DateUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A basic integration test for testing the functionality of a DAO
 *
 * @author bas.rutten
 */
public class TestEntityDaoTest extends BackendIntegrationTest {

	@Autowired
	private TestEntityDao dao;

	private TestEntity save(String name, long age) {
		TestEntity entity = new TestEntity();
		entity.setName(name);
		entity.setAge(age);
		entity = dao.save(entity);
		return entity;
	}

	@Test
	public void findByUniqueProperty() {
		save("Jan", 11L);

		TestEntity t = dao.findByUniqueProperty("name", "Jan", false);
		assertNotNull(t);

		// try fetching
		t = dao.fetchByUniqueProperty("name", "Jan", false);
		assertNotNull(t);
		t = dao.fetchByUniqueProperty("name", "Jan", true);
		assertNotNull(t);

		// case insensitive
		t = dao.findByUniqueProperty("name", "JAN", false);
		assertNotNull(t);
		t = dao.fetchByUniqueProperty("name", "JAN", true);
		assertNull(t);

		// test that NULL is returned if nothing can be found
		t = dao.findByUniqueProperty("name", "Bert", false);
		assertNull(t);
	}

	@Test
	public void delete() {

		TestEntity entity = save("Piet", 12L);
		Integer id = entity.getId();

		dao.delete(entity);

		assertNull(dao.findById(id));
	}

	@Test
	public void fetch() {
		save("Kevin", 11L);
		save("Stuart", 12L);
		save("Bob", 13L);

		List<TestEntity> results = dao.fetch(null);
		assertEquals(3, results.size());

		results = dao.fetch(new Compare.Equal("name", "Bob"));
		assertEquals(1, results.size());

		// with a sort order
		results = dao.fetch(null, new SortOrders(new SortOrder("name")));
		assertEquals(3, results.size());
		assertEquals("Bob", results.get(0).getName());

		// with a sort order and a fetch
		results = dao.fetch(null, new SortOrders(new SortOrder("name")), new FetchJoinInformation("testEntities"));
		assertEquals(3, results.size());
		assertEquals("Bob", results.get(0).getName());

		// with a sort order, fetch and page
		results = dao.fetch(null, new PageableImpl(0, 1, new SortOrders(new SortOrder("name"))));
		assertEquals(1, results.size());
		assertEquals("Bob", results.get(0).getName());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void fetchSelect() {
		save("Pete", 1L);
		save("Bob", 2L);
		save("Isaac", 3L);
		TestEntity e1 = entityManager.createQuery("from TestEntity t where t.name = 'Bob'", TestEntity.class)
				.getSingleResult();
		TestEntity e2 = entityManager.createQuery("from TestEntity t where t.name = 'Pete'", TestEntity.class)
				.getSingleResult();

		SortOrder sortName = new SortOrder("name");
		Filter filter = new In("name", List.of(e1.getName(), e2.getName()));
		List<Object[]> result = (List<Object[]>) dao.findProperties(filter, new String[] { "name", "age" },
				new SortOrders(sortName));

		assertEquals(2, result.size());
		assertEquals(e1.getName(), result.get(0)[0]);
		assertEquals(e1.getAge(), result.get(0)[1]);
		assertEquals(e2.getName(), result.get(1)[0]);
		assertEquals(e2.getAge(), result.get(1)[1]);
	}

	public void findByBirthDateLocal() {
		List<TestEntity> result = dao.findByBirthDate();
		assertEquals(0, result.size());

		TestEntity bob = save("Bob", 55L);
		bob.setBirthDate(DateUtils.createLocalDate("10081980"));
		bob = dao.save(bob);

		result = dao.findByBirthDate();
		assertEquals(1, result.size());
	}

	@Test
	public void findDistinctValues() {
		save("Kevin", 11L);
		save("Bob", 11L);
		save("Bob", 11L);

		List<?> names = dao.findDistinctValues(null, "name", String.class, new SortOrder("name"));
		assertEquals(2, names.size());
		assertEquals("Bob", names.get(0));
		assertEquals("Kevin", names.get(1));

		List<?> ages = dao.findDistinctValues(null, "age", String.class, new SortOrder("age"));
		assertEquals(1, ages.size());
		assertEquals(11L, ages.get(0));
	}

	@Test
	public void findDistinctForCollectionTable() {
		save("Kevin", 11L);
		save("Bob", 11L);
		save("Bob", 11L);

		List<String> names = dao.findDistinctInCollectionTable("test_entity", "name", String.class);
		assertEquals(2, names.size());

		List<Long> ages = dao.findDistinctInCollectionTable("test_entity", "age", Long.class);
		assertEquals(1, ages.size());
	}

	@Test
	public void findFilter() {
		TestEntity jan = save("Jan", 11L);
		save("Piet", 12L);
		save("Klaas", 13L);

		Filter filter = new Compare.Equal("name", "Jan");
		assertEquals(1, dao.count(filter, true));
		List<TestEntity> list = dao.find(filter);

		assertEquals(jan, list.get(0));

		Filter and = new And(filter, new Compare.Equal("age", 99L));
		list = dao.find(and);
		assertEquals(0, list.size());

		and = new And(filter, new Compare.Equal("age", 11L));

		SortOrder order = new SortOrder("name", Direction.ASC);
		list = dao.find(and, order);
		assertEquals(1, list.size());

		list = dao.find(null, order);
		assertEquals(3, list.size());
		assertEquals("Jan", list.get(0).getName());
		assertEquals("Klaas", list.get(1).getName());
		assertEquals("Piet", list.get(2).getName());
	}

	/**
	 * Test filtering by IDs
	 */
	@Test
	public void findIdsAndFetch() {
		save("Jan", 11L);
		save("Piet", 12L);
		save("Klaas", 13L);

		// retrieve the IDs (sorted by name)
		List<Integer> ids = dao.findIds(null, new SortOrder("name", Direction.ASC));
		assertEquals(3, ids.size());

		TestEntity entity = dao.fetchById(ids.get(0));
		assertEquals("Jan", entity.getName());

		ids = dao.findIds(null, new SortOrder("name", Direction.DESC));
		assertEquals(3, ids.size());

		entity = dao.fetchById(ids.get(0));
		assertEquals("Piet", entity.getName());

		List<TestEntity> list = dao.fetchByIds(ids, new SortOrders(new SortOrder("name", Direction.ASC)));
		assertEquals("Jan", list.get(0).getName());
		assertEquals("Klaas", list.get(1).getName());
		assertEquals("Piet", list.get(2).getName());
	}

	@Test
	public void findSelect() {
		save("Pete", 1L);
		save("Bob", 2L);
		save("Isaac", 3L);

		List<?> found = dao.findProperties(null, new String[] { "name", "age" }, new SortOrders(new SortOrder("name")));
		assertEquals(3, found.size());

		Object[] obj = (Object[]) found.get(0);
		assertEquals("Bob", obj[0]);
		assertEquals(2L, obj[1]);
	}

//	@Test
//	@Transactional
//	public void findSelect2() {
//		save("Pete", 1L);
//		save("Bob", 2L);
//		save("Isaac", 3L);
//
//		SortOrders so = new SortOrders(new SortOrder("name"));
//		PageableImpl pag = new PageableImpl(0, 10, so);
//
//		List<?> found = dao.findProperties(null, new String[] { "name", "age" }, pag);
//		assertEquals(3, found.size());
//
//		Object[] obj = (Object[]) found.get(0);
//		assertEquals("Bob", obj[0]);
//		assertEquals(2L, obj[1]);
//	}

	@Test
	@Transactional
	public void flushAndClear() {
		TestEntity entity = save("Jan", 11L);

		assertTrue(getEntityManager().contains(entity));

		dao.flushAndClear();

		assertFalse(getEntityManager().contains(entity));
	}

	@Test
	public void saveAndFind() {

		TestEntity entity = save("Piet", 12L);

		TestEntity detail = new TestEntity();
		detail.setAge(2L);
		detail.setName("Jantje");

		// Test save
		entity = dao.save(entity);
		assertNotNull(entity.getId());
		Integer id = entity.getId();

		// Test find one
		TestEntity other = dao.findById(id);
		assertEquals(other, entity);
	}

	@Test
	public void saveBulk() {
		TestEntity entity1 = new TestEntity("Bob", 1L);
		TestEntity entity2 = new TestEntity("Bob", 2L);
		TestEntity entity3 = new TestEntity("Bob", 3L);

		dao.save(List.of(entity1, entity2, entity3));

		List<TestEntity> list = dao.findAll();
		assertEquals(3, list.size());
	}

}
