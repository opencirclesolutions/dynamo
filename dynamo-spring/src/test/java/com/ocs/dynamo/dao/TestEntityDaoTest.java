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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.mysema.query.BooleanBuilder;
import com.ocs.dynamo.dao.SortOrder.Direction;
import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.QTestEntity;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.filter.And;
import com.ocs.dynamo.filter.Compare;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.test.BaseIntegrationTest;

/**
 * A basic integration test for testing the functionality of a DAO
 * 
 * @author bas.rutten
 */
public class TestEntityDaoTest extends BaseIntegrationTest {

	@Inject
	TestEntityDao dao;

	@Test
	public void testSaveAndFind() {

		TestEntity entity = save("Piet", 12L);

		TestEntity detail = new TestEntity();
		detail.setAge(2L);
		detail.setName("Jantje");
		entity.addChild(detail);

		// Test save
		entity = dao.save(entity);
		assertNotNull(entity.getId());
		Integer id = entity.getId();

		// Test find one
		TestEntity other = dao.findById(id);
		assertEquals(other, entity);
	}

	@Test
	public void testSaveBulk() {
		TestEntity entity1 = new TestEntity("Bob", 1L);
		TestEntity entity2 = new TestEntity("Bob", 2L);
		TestEntity entity3 = new TestEntity("Bob", 3L);

		dao.save(Lists.newArrayList(entity1, entity2, entity3));

		List<TestEntity> list = dao.findAll();
		Assert.assertEquals(3, list.size());
	}

	/**
	 * Basic test of the count and find methods
	 */
	@Test
	public void testCountAndFind() {
		save("Jan", 11L);
		save("Piet", 12L);
		save("Klaas", 13L);

		assertEquals(3, dao.count());

		List<TestEntity> list = dao.findAll();
		assertEquals(3, list.size());

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(QTestEntity.testEntity.name.eq("Jan"));
		list = dao.find(builder);
		assertEquals(1, list.size());

		// delete a list of objects
		dao.delete(list);

		// verify the delete
		list = dao.findAll();
		assertEquals(2, list.size());

		dao.delete(list);

		assertEquals(0, dao.count());
	}

	@Test
	public void testByUniqueProperty() {
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
		Assert.assertNull(t);

		// test that NULL is returned if nothing can be found
		t = dao.findByUniqueProperty("name", "Bert", false);
		Assert.assertNull(t);
	}

	/**
	 * Test the basic working of a tree based entity
	 */
	@Test
	public void testTree() {

		TestEntity entity = save("Piet", 12L);

		TestEntity detail = new TestEntity();
		detail.setAge(24L);
		detail.setName("Jantje");
		entity.addChild(detail);

		entity.addChild(detail);
		entity = dao.save(entity);

		// verify that the detail was cascaded
		detail = entity.getChildren().get(0);
		assertNotNull(detail.getId());

		List<TestEntity> roots = dao.findByParentIsNull();
		assertEquals(1, roots.size());
		assertEquals(entity, roots.get(0));

		List<TestEntity> children = dao.findByParent(entity);
		assertEquals(1, children.size());
		assertEquals(detail, children.get(0));
	}

	@Test
	public void testFindFilter() {
		TestEntity jan = save("Jan", 11L);
		save("Piet", 12L);
		save("Klaas", 13L);

		Filter filter = new Compare.Equal("name", "Jan");
		Assert.assertEquals(1, dao.count(filter, true));
		List<TestEntity> list = dao.find(filter);

		assertEquals(jan, list.get(0));

		Filter and = new And(filter, new Compare.Equal("age", 99L));
		list = dao.find(and);
		assertEquals(0, list.size());

		and = new And(filter, new Compare.Equal("age", 11L));

		SortOrder order = new SortOrder(Direction.ASC, "name");
		list = dao.find(and, order);
		assertEquals(1, list.size());

		list = dao.find(null, order);
		assertEquals(3, list.size());
		Assert.assertEquals("Jan", list.get(0).getName());
		Assert.assertEquals("Klaas", list.get(1).getName());
		Assert.assertEquals("Piet", list.get(2).getName());
	}

	@Test
	public void testFindPredicate() {
		save("Jan", 11L);
		save("Piet", 12L);
		save("Klaas", 13L);

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(QTestEntity.testEntity.name.containsIgnoreCase("a"));

		Assert.assertEquals(2, dao.count(builder));
		Assert.assertEquals(2, dao.find(builder).size());

		List<TestEntity> result = dao.find(builder, 0, 10, new SortOrder(Direction.ASC, "name"));
		Assert.assertEquals("Jan", result.get(0).getName());
		Assert.assertEquals("Klaas", result.get(1).getName());

		result = dao.find(builder, 0, 10, new SortOrder(Direction.DESC, "name"));
		Assert.assertEquals("Klaas", result.get(0).getName());
		Assert.assertEquals("Jan", result.get(1).getName());
	}

	/**
	 * Test filtering by IDs
	 */
	@Test
	public void testFindIdsAndFetch() {
		save("Jan", 11L);
		save("Piet", 12L);
		save("Klaas", 13L);

		// retrieve the IDs (sorted by name)
		List<Integer> ids = dao.findIds(null, new SortOrder(Direction.ASC, "name"));
		Assert.assertEquals(3, ids.size());

		TestEntity entity = dao.fetchById(ids.get(0));
		Assert.assertEquals("Jan", entity.getName());

		ids = dao.findIds(null, new SortOrder(Direction.DESC, "name"));
		Assert.assertEquals(3, ids.size());

		entity = dao.fetchById(ids.get(0));
		Assert.assertEquals("Piet", entity.getName());

		List<TestEntity> list = dao.fetchByIds(ids, new SortOrders(new SortOrder(Direction.ASC, "name")));
		Assert.assertEquals("Jan", list.get(0).getName());
		Assert.assertEquals("Klaas", list.get(1).getName());
		Assert.assertEquals("Piet", list.get(2).getName());
	}

	@Test
	public void testFetch() {
		save("Kevin", 11L);
		save("Stuart", 12L);
		save("Bob", 13L);

		List<TestEntity> results = dao.fetch(null);
		Assert.assertEquals(3, results.size());

		results = dao.fetch(new Compare.Equal("name", "Bob"));
		Assert.assertEquals(1, results.size());

		// with a sort order
		results = dao.fetch(null, new SortOrders(new SortOrder("name")));
		Assert.assertEquals(3, results.size());
		Assert.assertEquals("Bob", results.get(0).getName());

		// with a sort order and a fetch
		results = dao.fetch(null, new SortOrders(new SortOrder("name")), new FetchJoinInformation("testEntities"));
		Assert.assertEquals(3, results.size());
		Assert.assertEquals("Bob", results.get(0).getName());
	}

	@Test
	public void testFlushAndClear() {
		TestEntity entity = save("Jan", 11L);

		Assert.assertTrue(getEntityManager().contains(entity));

		dao.flushAndClear();

		Assert.assertFalse(getEntityManager().contains(entity));
	}

	@Test
	public void testFindDistinct() {
		save("Kevin", 11L);
		save("Bob", 11L);
		save("Bob", 11L);

		List<? extends Object> names = dao.findDistinct(null, "name", String.class, new SortOrder("name"));
		Assert.assertEquals(2, names.size());
		Assert.assertEquals("Bob", names.get(0));
		Assert.assertEquals("Kevin", names.get(1));

		List<? extends Object> ages = dao.findDistinct(null, "age", String.class, new SortOrder("age"));
		Assert.assertEquals(1, ages.size());
		Assert.assertEquals(11L, ages.get(0));
	}

	@Test
	public void testFindDistinctForCollectionTable() {
		save("Kevin", 11L);
		save("Bob", 11L);
		save("Bob", 11L);

		List<String> names = dao.findDistinctInCollectionTable("test_entity", "name", String.class);
		Assert.assertEquals(2, names.size());
		
		List<Long> ages = dao.findDistinctInCollectionTable("test_entity", "age", Long.class);
		Assert.assertEquals(1, ages.size());
	}

	private TestEntity save(String name, long age) {
		TestEntity entity = new TestEntity();
		entity.setName(name);
		entity.setAge(age);
		entity = dao.save(entity);
		return entity;
	}

}
