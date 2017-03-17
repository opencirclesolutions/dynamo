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

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FilterTest {

    @Test
    public void testFilterSingleProperty() {

        TestEntity entity = new TestEntity("Bert", 23L);

        // single string property
        Assert.assertTrue(new Compare.Equal("name", "Bert").evaluate(entity));
        Assert.assertTrue(new Compare.LessOrEqual("name", "Bert").evaluate(entity));

        // single long property
        Assert.assertTrue(new Compare.LessOrEqual("age", 23L).evaluate(entity));
        Assert.assertTrue(new Compare.Equal("age", 23L).evaluate(entity));
        Assert.assertTrue(new Compare.Less("age", 25L).evaluate(entity));
        Assert.assertFalse(new Compare.GreaterOrEqual("age", 25L).evaluate(entity));
    }

    @Test
    public void testFilterBetween() {

        TestEntity entity = new TestEntity("Bert", 23L);

        Between between = new Between("age", 22L, 24L);
        Assert.assertTrue(between.evaluate(entity));

        between = new Between("age", 43L, 45L);
        Assert.assertFalse(between.evaluate(entity));

        // not a comparable
        TestEntity2 e2 = new TestEntity2();
        between = new Between("testEntity", 22L, 24L);
        Assert.assertFalse(between.evaluate(e2));

        Assert.assertFalse(between.equals(null));
        Assert.assertTrue(between.equals(between));

        // different object but identical filter
        Assert.assertTrue(between.equals(new Between("testEntity", 22L, 24L)));

        // different bounds
        Assert.assertFalse(between.equals(new Between("testEntity", 22L, 23L)));

        // different type of filter
        Assert.assertFalse(between.equals(new Compare.Equal("testEntity", 44)));
    }

    @Test
    public void testFilterNot() {
        TestEntity entity = new TestEntity("Bert", 23L);

        Compare.Equal filter = new Compare.Equal("name", "Bert");

        Assert.assertTrue(filter.evaluate(entity));
        Assert.assertFalse(new Not(filter).evaluate(entity));
        Assert.assertTrue(filter.and().not(new Compare.Equal("age", 24)).evaluate(entity));

        Not not = new Not(filter);
        Assert.assertFalse(not.equals(null));
        Assert.assertFalse(not.equals(filter));
        Assert.assertTrue(not.equals(not));
    }

    @Test
    public void testFilterAnd() {
        TestEntity entity = new TestEntity("Bert", 23L);

        Filter conjunction = new Compare.Equal("name", "Bert").and(new Compare.Equal("age", 23L));
        Assert.assertTrue(conjunction.evaluate(entity));

        conjunction = new Compare.Equal("name", "Bert").and(new Compare.Equal("age", 24L));
        Assert.assertFalse(conjunction.evaluate(entity));

        conjunction = new And(new Like("name", "%er%"), new Compare.Equal("age", 23L));
        Assert.assertTrue(conjunction.evaluate(entity));

        // test the equals operator
        Assert.assertFalse(conjunction.equals(null));
        Assert.assertFalse(conjunction.equals(new Object()));
        Assert.assertTrue(conjunction.equals(conjunction));
        Assert.assertFalse(conjunction.equals(new And(new Like("name", "%er%"), new Compare.Equal(
                "age", 24L))));

        Assert.assertNotNull(conjunction.toString());
    }

    @Test
    public void testFilterOr() {
        TestEntity entity = new TestEntity("Bert", 23L);

        Filter disjunction = new Compare.Equal("name", "Bert").or(new Compare.Equal("age", 23L));
        Assert.assertTrue(disjunction.evaluate(entity));

        disjunction = new Compare.Equal("name", "Bert").or(new Compare.Equal("age", 24L));
        Assert.assertTrue(disjunction.evaluate(entity));

        List<Filter> filters = new ArrayList<>();
        filters.add(new Like("name", "%ob%"));
        filters.add(new Compare.Equal("age", 25L));
        disjunction = new Or(filters);
        Assert.assertFalse(disjunction.evaluate(entity));

        // test the equals operator
        Assert.assertFalse(disjunction.equals(null));
        Assert.assertFalse(disjunction.equals(new Object()));
        Assert.assertTrue(disjunction.equals(disjunction));
        Assert.assertFalse(disjunction.equals(new Or(new Like("name", "%er%"), new Compare.Equal(
                "age", 24L))));
    }

    @Test
    public void testFilterIsNull() {
        TestEntity entity = new TestEntity(null, 23L);

        IsNull isNull = new IsNull("name");
        Assert.assertFalse(isNull.evaluate(null));
        Assert.assertTrue(isNull.evaluate(entity));

        IsNull isNull2 = new IsNull("age");
        Assert.assertFalse(isNull2.evaluate(entity));

        Assert.assertTrue(isNull.equals(isNull));
        Assert.assertFalse(isNull.equals(null));
        Assert.assertFalse(isNull.equals(isNull2));
    }

    @Test
    public void testCompare() {
        TestEntity testEntity1 = new TestEntity("A", 14L);
        TestEntity testEntity2 = new TestEntity("B", 12L);

        Compare.Equal equal = new Compare.Equal("age", 14L);
        Assert.assertFalse(equal.evaluate(null));
        Assert.assertFalse(equal.evaluate(new TestEntity()));
        Assert.assertTrue(equal.evaluate(testEntity1));
        Assert.assertFalse(equal.evaluate(testEntity2));

        Compare.Less less = new Compare.Less("age", 14L);
        Assert.assertFalse(less.evaluate(testEntity1));
        Assert.assertTrue(less.evaluate(testEntity2));

        Compare.Greater greater = new Compare.Greater("age", 13L);
        Assert.assertTrue(greater.evaluate(testEntity1));
        Assert.assertFalse(greater.evaluate(testEntity2));

        Compare.GreaterOrEqual greaterEqual = new Compare.GreaterOrEqual("age", 14L);
        Assert.assertTrue(greaterEqual.evaluate(testEntity1));
        Assert.assertFalse(greaterEqual.evaluate(testEntity2));

        Compare.LessOrEqual lessEqual = new Compare.LessOrEqual("age", 12L);
        Assert.assertFalse(lessEqual.evaluate(testEntity1));
        Assert.assertTrue(lessEqual.evaluate(testEntity2));

        // equals test
        Assert.assertFalse(equal.equals(null));
        Assert.assertTrue(equal.equals(equal));
        Assert.assertFalse(equal.equals(new Compare.Equal("age", 15L)));
        Assert.assertFalse(equal.equals(less));

    }

    @Test
    public void testIn() {
        TestEntity testEntity = new TestEntity();
        testEntity.setId(1);

        In in = new In("id", Lists.newArrayList(1));

        Assert.assertFalse(in.evaluate(null));
        Assert.assertTrue(in.evaluate(testEntity));

        In in2 = new In("id", Lists.newArrayList(1));
        In in3 = new In("id", Lists.newArrayList(1, 2));

        Assert.assertFalse(in.equals(null));
        Assert.assertFalse(in.equals(new Object()));
        Assert.assertTrue(in.equals(in));
        Assert.assertTrue(in.equals(in2));
        Assert.assertFalse(in.equals(in3));
    }

    @Test
    public void testContains() {
        TestEntity testEntity = new TestEntity();
        testEntity.setId(1);

        Contains contains = new Contains("entities", testEntity);

        Contains contains2 = new Contains("other", testEntity);
        Assert.assertFalse(contains.equals(contains2));

        CollectionHolder holder = new CollectionHolder();
        holder.getEntities().add(testEntity);

        Assert.assertTrue(contains.evaluate(holder));
        Assert.assertFalse(contains.evaluate(new CollectionHolder()));

        Assert.assertFalse(contains.evaluate(null));

        contains = new Contains("other", testEntity);
        holder = new CollectionHolder();
        holder.setOther(2);

        Assert.assertFalse(contains.evaluate(holder));

        Assert.assertTrue(contains.equals(contains));
        Assert.assertFalse(contains.equals(null));

    }

    @Test
    public void testFilterModulo() {

        TestEntity testEntity = new TestEntity();
        testEntity.setId(4);
        testEntity.setAge(44L);
        testEntity.setName("Bob");

        Modulo modulo = new Modulo("age", "id", 0);
        Assert.assertTrue(modulo.evaluate(testEntity));

        testEntity.setAge(45L);
        modulo = new Modulo("age", "id", 0);
        Assert.assertFalse(modulo.evaluate(testEntity));

        // 45 mod 4 = 1
        modulo = new Modulo("age", "id", 1);
        Assert.assertTrue(modulo.evaluate(testEntity));

        // now, compare using a literal value
        modulo = new Modulo("age", 9, 0);
        Assert.assertTrue(modulo.evaluate(testEntity));

        modulo = new Modulo("age", 9, 1);
        Assert.assertFalse(modulo.evaluate(testEntity));

        // not a numeric value
        modulo = new Modulo("name", 9, 1);
        Assert.assertFalse(modulo.evaluate(testEntity));

        // null
        modulo = new Modulo("name", 9, 1);
        Assert.assertFalse(modulo.evaluate(null));
    }

    @Test
    public void testFilterLike() {
        TestEntity testEntity = new TestEntity();
        testEntity.setName("Bob");

        Like like = new Like("name", "b%", false);
        Assert.assertFalse(like.evaluate(null));

        Assert.assertTrue(like.evaluate(testEntity));

        like = new Like("name", "b%", true);
        Assert.assertFalse(like.evaluate(testEntity));

        Like notString = new Like("age", "b%", false);
        Assert.assertFalse(notString.evaluate(testEntity));

        Assert.assertFalse(like.equals(null));
        Assert.assertFalse(like.equals(new Object()));
        Assert.assertTrue(like.equals(like));
        Assert.assertTrue(like.equals(new Like("name", "b%", true)));
        Assert.assertFalse(like.equals(new Like("name", "b%", false)));

        // infix matching
        Like like2 = new Like("name", "%o%", true);
        Assert.assertTrue(like2.evaluate(testEntity));

    }

    @Test
    public void testCompose() {
        Compare.Equal equal = new Compare.Equal("property1", 1);

        AbstractJunctionFilter filter = equal.greater("property2", 2);
        Assert.assertTrue(filter instanceof And);
        Assert.assertEquals(2, filter.getFilters().size());

        filter = filter.greaterOrEqual("property3", 3);
        Assert.assertTrue(filter instanceof And);
        Assert.assertEquals(3, filter.getFilters().size());

        filter = filter.less("property4", 4);
        Assert.assertTrue(filter instanceof And);
        Assert.assertEquals(4, filter.getFilters().size());

        filter = filter.lessOrEqual("property4", 5);
        Assert.assertTrue(filter instanceof And);
        Assert.assertEquals(5, filter.getFilters().size());

        filter = filter.isNull("property5");
        Assert.assertTrue(filter instanceof And);
        Assert.assertEquals(6, filter.getFilters().size());

        filter = filter.between("property6", 6, 7);
        Assert.assertTrue(filter instanceof And);
        Assert.assertEquals(7, filter.getFilters().size());

        filter = filter.like("property7", "abc");
        Assert.assertTrue(filter instanceof And);
        Assert.assertEquals(8, filter.getFilters().size());
        Like like = (Like) filter.getFilters().get(7);
        Assert.assertTrue(like.isCaseSensitive());

        filter = filter.isEqual("property8", 8);
        Assert.assertTrue(filter instanceof And);
        Assert.assertEquals(9, filter.getFilters().size());

        filter = filter.like("property9", "abc", false);
        Assert.assertTrue(filter instanceof And);
        Assert.assertEquals(10, filter.getFilters().size());
        like = (Like) filter.getFilters().get(9);
        Assert.assertFalse(like.isCaseSensitive());

        filter = filter.likeIgnoreCase("property8", "abc");
        Assert.assertTrue(filter instanceof And);
        Assert.assertEquals(11, filter.getFilters().size());
        like = (Like) filter.getFilters().get(10);
        Assert.assertFalse(like.isCaseSensitive());
    }

    @Test
    public void testComposeOr() {
        Compare.Equal equal1 = new Compare.Equal("property1", 1);
        Compare.Equal equal2 = new Compare.Equal("property2", 1);

        Or or = equal1.or(equal2);
        Assert.assertEquals(2, or.getFilters().size());

        AbstractJunctionFilter filter = or.greater("property3", 3);
        Assert.assertTrue(filter instanceof Or);
        Assert.assertEquals(3, filter.getFilters().size());

        // wrap a filter inside an Or
        Filter result = equal1.or();
        Assert.assertTrue(result instanceof Or);
    }

    @Test
    public void testApplyFilter() {
        Compare.Equal equal1 = new Compare.Equal("name", "Bob");

        TestEntity t1 = new TestEntity("Pete", 14L);
        TestEntity t2 = new TestEntity("Bob", 14L);
        List<TestEntity> list = Lists.newArrayList(t1, t2);

        List<TestEntity> result = equal1.applyFilter(list);
        Assert.assertEquals(1, result.size());
    }

}
