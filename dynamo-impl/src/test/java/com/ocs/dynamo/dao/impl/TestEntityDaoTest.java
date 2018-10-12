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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrder.Direction;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.filter.And;
import com.ocs.dynamo.filter.Compare;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.filter.In;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.utils.DateUtils;

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

        List<?> names = dao.findDistinct(null, "name", String.class, new SortOrder("name"));
        Assert.assertEquals(2, names.size());
        Assert.assertEquals("Bob", names.get(0));
        Assert.assertEquals("Kevin", names.get(1));

        List<?> ages = dao.findDistinct(null, "age", String.class, new SortOrder("age"));
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

	@Test
	public void testFetchSelect() {
		save("Pete", 1L);
		save("Bob", 2L);
		save("Isaac", 3L);
		TestEntity e1 = entityManager.createQuery("from TestEntity t where t.name = 'Bob'", TestEntity.class)
				.getSingleResult();
		TestEntity e2 = entityManager.createQuery("from TestEntity t where t.name = 'Pete'", TestEntity.class)
				.getSingleResult();

		SortOrder sortName = new SortOrder("name");
		Filter filter = new In("name", Lists.newArrayList(e1.getName(), e2.getName()));
		List<Object[]> result = dao.fetchSelect(filter, new String[] { "name", "age" }, new SortOrders(sortName));

		Assert.assertEquals(2, result.size());
		Assert.assertEquals(e1.getName(), result.get(0)[0]);
		Assert.assertEquals(e1.getAge(), result.get(0)[1]);
		Assert.assertEquals(e2.getName(), result.get(1)[0]);
		Assert.assertEquals(e2.getAge(), result.get(1)[1]);

	}

    public void testFindByBirthDateLocal() {
        List<TestEntity> result = dao.findByBirthDateLocal();
        Assert.assertEquals(0, result.size());

        TestEntity bob = save("Bob", 55L);
        bob.setBirthDateLocal(DateUtils.createLocalDate("10081980"));
        bob = dao.save(bob);

        result = dao.findByBirthDateLocal();
        Assert.assertEquals(1, result.size());
    }

    private TestEntity save(String name, long age) {
        TestEntity entity = new TestEntity();
        entity.setName(name);
        entity.setAge(age);
        entity = dao.save(entity);
        return entity;
    }

}
