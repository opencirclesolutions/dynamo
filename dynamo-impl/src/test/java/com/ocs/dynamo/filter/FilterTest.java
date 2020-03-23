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
package com.ocs.dynamo.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;

public class FilterTest {

    @Test
    public void testFilterSingleProperty() {

        TestEntity entity = new TestEntity("Bert", 23L);

        // single string property
        assertTrue(new Compare.Equal("name", "Bert").evaluate(entity));
        assertTrue(new Compare.LessOrEqual("name", "Bert").evaluate(entity));

        // single long property
        assertTrue(new Compare.LessOrEqual("age", 23L).evaluate(entity));
        assertTrue(new Compare.Equal("age", 23L).evaluate(entity));
        assertTrue(new Compare.Less("age", 25L).evaluate(entity));
        assertFalse(new Compare.GreaterOrEqual("age", 25L).evaluate(entity));
    }

    @Test
    public void testFilterBetween() {

        TestEntity entity = new TestEntity("Bert", 23L);

        Between between = new Between("age", 22L, 24L);
        assertTrue(between.evaluate(entity));

        between = new Between("age", 43L, 45L);
        assertFalse(between.evaluate(entity));

        // not a comparable
        TestEntity2 e2 = new TestEntity2();
        between = new Between("testEntity", 22L, 24L);
        assertFalse(between.evaluate(e2));

        assertFalse(between.equals(null));
        assertTrue(between.equals(between));

        // different object but identical filter
        assertTrue(between.equals(new Between("testEntity", 22L, 24L)));

        // different bounds
        assertFalse(between.equals(new Between("testEntity", 22L, 23L)));

        // different type of filter
        assertFalse(between.equals(new Compare.Equal("testEntity", 44)));
    }

    @Test
    public void testFilterNot() {
        TestEntity entity = new TestEntity("Bert", 23L);

        Compare.Equal filter = new Compare.Equal("name", "Bert");

        assertTrue(filter.evaluate(entity));
        assertFalse(new Not(filter).evaluate(entity));
        assertTrue(filter.and().not(new Compare.Equal("age", 24)).evaluate(entity));

        Not not = new Not(filter);
        assertFalse(not.equals(null));
        assertFalse(not.equals(filter));
        assertTrue(not.equals(not));
    }

    @Test
    public void testFilterAnd() {
        TestEntity entity = new TestEntity("Bert", 23L);

        Filter conjunction = new Compare.Equal("name", "Bert").and(new Compare.Equal("age", 23L));
        assertTrue(conjunction.evaluate(entity));

        conjunction = new Compare.Equal("name", "Bert").and(new Compare.Equal("age", 24L));
        assertFalse(conjunction.evaluate(entity));

        conjunction = new And(new Like("name", "%er%"), new Compare.Equal("age", 23L));
        assertTrue(conjunction.evaluate(entity));

        // test the equals operator
        assertFalse(conjunction.equals(null));
        assertFalse(conjunction.equals(new Object()));
        assertTrue(conjunction.equals(conjunction));
        assertFalse(conjunction.equals(new And(new Like("name", "%er%"), new Compare.Equal("age", 24L))));

        assertNotNull(conjunction.toString());
    }

    @Test
    public void testFilterOr() {
        TestEntity entity = new TestEntity("Bert", 23L);

        Filter disjunction = new Compare.Equal("name", "Bert").or(new Compare.Equal("age", 23L));
        assertTrue(disjunction.evaluate(entity));

        disjunction = new Compare.Equal("name", "Bert").or(new Compare.Equal("age", 24L));
        assertTrue(disjunction.evaluate(entity));

        List<Filter> filters = new ArrayList<>();
        filters.add(new Like("name", "%ob%"));
        filters.add(new Compare.Equal("age", 25L));
        disjunction = new Or(filters);
        assertFalse(disjunction.evaluate(entity));

        // test the equals operator
        assertFalse(disjunction.equals(null));
        assertFalse(disjunction.equals(new Object()));
        assertTrue(disjunction.equals(disjunction));
        assertFalse(disjunction.equals(new Or(new Like("name", "%er%"), new Compare.Equal("age", 24L))));
    }

    @Test
    public void testFilterIsNull() {
        TestEntity entity = new TestEntity(null, 23L);

        IsNull isNull = new IsNull("name");
        assertFalse(isNull.evaluate(null));
        assertTrue(isNull.evaluate(entity));

        IsNull isNull2 = new IsNull("age");
        assertFalse(isNull2.evaluate(entity));

        assertTrue(isNull.equals(isNull));
        assertFalse(isNull.equals(null));
        assertFalse(isNull.equals(isNull2));
    }

    @Test
    public void testCompare() {
        TestEntity testEntity1 = new TestEntity("A", 14L);
        TestEntity testEntity2 = new TestEntity("B", 12L);

        Compare.Equal equal = new Compare.Equal("age", 14L);
        assertFalse(equal.evaluate(null));
        assertFalse(equal.evaluate(new TestEntity()));
        assertTrue(equal.evaluate(testEntity1));
        assertFalse(equal.evaluate(testEntity2));
        assertFalse(equal.evaluate(null));

        Compare.Equal equalNull = new Compare.Equal("age", null);
        assertFalse(equalNull.evaluate(testEntity1));

        Compare.Less less = new Compare.Less("age", 14L);
        assertFalse(less.evaluate(testEntity1));
        assertTrue(less.evaluate(testEntity2));
        assertFalse(less.evaluate(null));

        Compare.Greater greater = new Compare.Greater("age", 13L);
        assertTrue(greater.evaluate(testEntity1));
        assertFalse(greater.evaluate(testEntity2));

        Compare.GreaterOrEqual greaterEqual = new Compare.GreaterOrEqual("age", 14L);
        assertTrue(greaterEqual.evaluate(testEntity1));
        assertFalse(greaterEqual.evaluate(testEntity2));

        Compare.LessOrEqual lessEqual = new Compare.LessOrEqual("age", 12L);
        assertFalse(lessEqual.evaluate(testEntity1));
        assertTrue(lessEqual.evaluate(testEntity2));

        // equals test
        assertFalse(equal.equals(null));
        assertTrue(equal.equals(equal));
        assertFalse(equal.equals(new Compare.Equal("age", 15L)));
        assertFalse(equal.equals(less));

    }

    @Test
    public void testIn() {
        TestEntity testEntity = new TestEntity();
        testEntity.setId(1);

        In in = new In("id", Lists.newArrayList(1));

        assertFalse(in.evaluate(null));
        assertTrue(in.evaluate(testEntity));

        In in2 = new In("id", Lists.newArrayList(1));
        In in3 = new In("id", Lists.newArrayList(1, 2));

        assertFalse(in.equals(null));
        assertFalse(in.equals(new Object()));
        assertTrue(in.equals(in));
        assertTrue(in.equals(in2));
        assertFalse(in.equals(in3));
    }

    @Test
    public void testContains() {
        TestEntity testEntity = new TestEntity();
        testEntity.setId(1);

        Contains contains = new Contains("entities", testEntity);

        Contains contains2 = new Contains("other", testEntity);
        assertFalse(contains.equals(contains2));

        CollectionHolder holder = new CollectionHolder();
        holder.getEntities().add(testEntity);

        assertTrue(contains.evaluate(holder));
        assertFalse(contains.evaluate(new CollectionHolder()));

        assertFalse(contains.evaluate(null));

        contains = new Contains("other", testEntity);
        holder = new CollectionHolder();
        holder.setOther(2);

        assertFalse(contains.evaluate(holder));

        assertTrue(contains.equals(contains));
        assertFalse(contains.equals(null));

    }

    @Test
    public void testFilterModulo() {

        TestEntity testEntity = new TestEntity();
        testEntity.setId(4);
        testEntity.setAge(44L);
        testEntity.setName("Bob");

        Modulo modulo = new Modulo("age", "id", 0);
        assertTrue(modulo.evaluate(testEntity));

        testEntity.setAge(45L);
        modulo = new Modulo("age", "id", 0);
        assertFalse(modulo.evaluate(testEntity));

        // 45 mod 4 = 1
        modulo = new Modulo("age", "id", 1);
        assertTrue(modulo.evaluate(testEntity));

        // now, compare using a literal value
        modulo = new Modulo("age", 9, 0);
        assertTrue(modulo.evaluate(testEntity));

        modulo = new Modulo("age", 9, 1);
        assertFalse(modulo.evaluate(testEntity));

        // not a numeric value
        modulo = new Modulo("name", 9, 1);
        assertFalse(modulo.evaluate(testEntity));

        // null
        modulo = new Modulo("name", 9, 1);
        assertFalse(modulo.evaluate(null));
    }

    @Test
    public void testFilterLike() {
        TestEntity testEntity = new TestEntity();
        testEntity.setName("Bob");

        Like like = new Like("name", "b%", false);
        assertFalse(like.evaluate(null));

        assertTrue(like.evaluate(testEntity));

        like = new Like("name", "b%", true);
        assertFalse(like.evaluate(testEntity));

        Like notString = new Like("age", "b%", false);
        assertFalse(notString.evaluate(testEntity));

        assertFalse(like.equals(null));
        assertFalse(like.equals(new Object()));
        assertTrue(like.equals(like));
        assertTrue(like.equals(new Like("name", "b%", true)));
        assertFalse(like.equals(new Like("name", "b%", false)));

        // infix matching
        Like like2 = new Like("name", "%o%", true);
        assertTrue(like2.evaluate(testEntity));

    }

    @Test
    public void testCompose() {
        Compare.Equal equal = new Compare.Equal("property1", 1);

        AbstractJunctionFilter filter = equal.greater("property2", 2);
        assertTrue(filter instanceof And);
        assertEquals(2, filter.getFilters().size());

        filter = filter.greaterOrEqual("property3", 3);
        assertTrue(filter instanceof And);
        assertEquals(3, filter.getFilters().size());

        filter = filter.less("property4", 4);
        assertTrue(filter instanceof And);
        assertEquals(4, filter.getFilters().size());

        filter = filter.lessOrEqual("property4", 5);
        assertTrue(filter instanceof And);
        assertEquals(5, filter.getFilters().size());

        filter = filter.isNull("property5");
        assertTrue(filter instanceof And);
        assertEquals(6, filter.getFilters().size());

        filter = filter.between("property6", 6, 7);
        assertTrue(filter instanceof And);
        assertEquals(7, filter.getFilters().size());

        filter = filter.like("property7", "abc");
        assertTrue(filter instanceof And);
        assertEquals(8, filter.getFilters().size());
        Like like = (Like) filter.getFilters().get(7);
        assertTrue(like.isCaseSensitive());

        filter = filter.isEqual("property8", 8);
        assertTrue(filter instanceof And);
        assertEquals(9, filter.getFilters().size());

        filter = filter.like("property9", "abc", false);
        assertTrue(filter instanceof And);
        assertEquals(10, filter.getFilters().size());
        like = (Like) filter.getFilters().get(9);
        assertFalse(like.isCaseSensitive());

        filter = filter.likeIgnoreCase("property8", "abc");
        assertTrue(filter instanceof And);
        assertEquals(11, filter.getFilters().size());
        like = (Like) filter.getFilters().get(10);
        assertFalse(like.isCaseSensitive());
    }

    @Test
    public void testComposeOr() {
        Compare.Equal equal1 = new Compare.Equal("property1", 1);
        Compare.Equal equal2 = new Compare.Equal("property2", 1);

        Or or = equal1.or(equal2);
        assertEquals(2, or.getFilters().size());

        AbstractJunctionFilter filter = or.greater("property3", 3);
        assertTrue(filter instanceof Or);
        assertEquals(3, filter.getFilters().size());

        // wrap a filter inside an Or
        Filter result = equal1.or();
        assertTrue(result instanceof Or);
    }

    @Test
    public void testApplyFilter() {
        Compare.Equal equal1 = new Compare.Equal("name", "Bob");

        TestEntity t1 = new TestEntity("Pete", 14L);
        TestEntity t2 = new TestEntity("Bob", 14L);
        List<TestEntity> list = Lists.newArrayList(t1, t2);

        List<TestEntity> result = equal1.applyFilter(list);
        assertEquals(1, result.size());
    }

}
