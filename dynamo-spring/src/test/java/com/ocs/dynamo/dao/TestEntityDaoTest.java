package com.ocs.dynamo.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.support.SortDefinition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.google.common.collect.Lists;
import com.mysema.query.BooleanBuilder;
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

		List<TestEntity> list = dao.findAll(null);
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

		List<TestEntity> list = dao.findAll(null);
		assertEquals(3, list.size());

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(QTestEntity.testEntity.name.eq("Jan"));
		list = dao.find(builder);
		assertEquals(1, list.size());

		// delete a list of objects
		dao.delete(list);

		// verify the delete
		list = dao.findAll(null);
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

		Order order = new Order(Direction.ASC, "name");
		list = dao.find(and, new Sort(order));
		assertEquals(1, list.size());

		list = dao.find(null, new Sort(order));
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

		List<TestEntity> result = dao.find(builder, 0, 10, new SortDefinition() {

			@Override
			public String getProperty() {
				return "name";
			}

			@Override
			public boolean isIgnoreCase() {
				return true;
			}

			@Override
			public boolean isAscending() {
				return true;
			}

		});
		Assert.assertEquals("Jan", result.get(0).getName());
		Assert.assertEquals("Klaas", result.get(1).getName());

		result = dao.find(builder, 0, 10, new SortDefinition() {

			@Override
			public String getProperty() {
				return "name";
			}

			@Override
			public boolean isIgnoreCase() {
				return true;
			}

			@Override
			public boolean isAscending() {
				return false;
			}

		});
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
		List<Integer> ids = dao.findIds(null, new Sort(new Order(Direction.ASC, "name")));
		Assert.assertEquals(3, ids.size());

		TestEntity entity = dao.fetchById(ids.get(0));
		Assert.assertEquals("Jan", entity.getName());

		ids = dao.findIds(null, new Sort(new Order(Direction.DESC, "name")));
		Assert.assertEquals(3, ids.size());

		entity = dao.fetchById(ids.get(0));
		Assert.assertEquals("Piet", entity.getName());

		List<TestEntity> list = dao.fetchByIds(ids, new Sort(new Order(Direction.ASC, "name")));
		Assert.assertEquals("Jan", list.get(0).getName());
		Assert.assertEquals("Klaas", list.get(1).getName());
		Assert.assertEquals("Piet", list.get(2).getName());
	}

	@Test
	public void testFlushAndClear() {
		TestEntity entity = save("Jan", 11L);

		Assert.assertTrue(getEntityManager().contains(entity));

		dao.flushAndClear();

		Assert.assertFalse(getEntityManager().contains(entity));
	}

	private TestEntity save(String name, long age) {
		TestEntity entity = new TestEntity();
		entity.setName(name);
		entity.setAge(age);
		entity = dao.save(entity);
		return entity;
	}

}
