package org.dynamoframework.filter;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.dynamoframework.configuration.DynamoConfigurationProperties;
import org.dynamoframework.domain.TestEntity;
import org.dynamoframework.domain.TestEntity2;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.domain.model.impl.EntityModelFactoryImpl;
import org.dynamoframework.service.ServiceLocator;
import org.dynamoframework.test.BaseMockitoTest;
import org.dynamoframework.configuration.DynamoPropertiesHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@Import({EntityModelFactoryImpl.class, DynamoPropertiesHolder.class})
@EnableConfigurationProperties(value = DynamoConfigurationProperties.class)
public class DynamoFilterUtilTest extends BaseMockitoTest {

	@Autowired
	private EntityModelFactoryImpl entityModelFactory; // = new EntityModelFactoryImpl();

	@Mock
	private static ServiceLocator serviceLocator;

	@BeforeEach
	void beforeEach() {
		ReflectionTestUtils.setField(entityModelFactory, "serviceLocator", serviceLocator);
		when(serviceLocator.getEntityModelFactory())
			.thenReturn(entityModelFactory);
	}

	@Test
	public void testReplaceFilter() {

		And and = new And(new Compare.Equal("a", 12), new Compare.Equal("b", 24));

		DynamoFilterUtil.replaceFilter(and, new Compare.Equal("c", 13), "b", false);

		Filter filter = and.getFilters().get(1);
		assertTrue(filter instanceof Compare.Equal);
		assertEquals("c", ((Compare.Equal) filter).getPropertyId());
	}

	@Test
	public void testReplaceFilterAll() {

		And and = new And(new Compare.Equal("a", 12), new Compare.Equal("a", 12));

		DynamoFilterUtil.replaceFilter(and, new Compare.Equal("c", 13), "a", false);

		Filter f0 = and.getFilters().get(0);
		assertTrue(f0 instanceof Compare.Equal);
		assertEquals("c", ((Compare.Equal) f0).getPropertyId());

		Filter f1 = and.getFilters().get(1);
		assertTrue(f1 instanceof Compare.Equal);
		assertEquals("c", ((Compare.Equal) f1).getPropertyId());
	}

	/**
	 * Check that only the first filter is replaced
	 */
	@Test
	public void testReplaceFilterFirstOnly() {

		And and = new And(new Compare.Equal("a", 12), new Compare.Equal("a", 12));

		DynamoFilterUtil.replaceFilter(and, new Compare.Equal("c", 13), "a", true);

		Filter f0 = and.getFilters().get(0);
		assertTrue(f0 instanceof Compare.Equal);
		assertEquals("c", ((Compare.Equal) f0).getPropertyId());

		Filter f1 = and.getFilters().get(1);
		assertTrue(f1 instanceof Compare.Equal);
		assertEquals("a", ((Compare.Equal) f1).getPropertyId());
	}

	@Test
	public void testReplaceNot() {

		Not not = new Not(new Compare.Equal("a", 12));

		DynamoFilterUtil.replaceFilter(not, new Compare.Equal("d", 13), "a", false);

		Filter f = not.getFilter();
		assertTrue(f instanceof Compare.Equal);
		assertEquals("d", ((Compare.Equal) f).getPropertyId());
	}

	@Test
	public void testReplaceComplex() {

		Not not = new Not(new Compare.Equal("a", 12));
		And and = new And(new Compare.Equal("b", 12), new Compare.Equal("c", 24));
		Or or = new Or(not, and);

		DynamoFilterUtil.replaceFilter(or, new And(new Compare.Greater("f", 4), new Compare.Less("g", 7)), "b", false);

		// get the AND filter
		Filter f = or.getFilters().get(1);
		assertTrue(f instanceof And);

		And inner = (And) ((And) f).getFilters().get(0);
		assertEquals("f", ((PropertyFilter) inner.getFilters().get(0)).getPropertyId());
	}

	@Test
	public void testExtractFilter_Like2() {

		Like like = new Like("prop1", "someString");
		Filter f1 = DynamoFilterUtil.extractFilter(like, "prop1");
		assertNotNull(f1);

		// wrong property
		Filter f2 = DynamoFilterUtil.extractFilter(like, "prop2");
		assertNull(f2);
	}

	@Test
	public void testExtractFilter_Between() {

		Between between = new Between("prop1", 100, 200);
		Filter f1 = DynamoFilterUtil.extractFilter(between, "prop1");
		assertNotNull(f1);

		// wrong property
		Filter f2 = DynamoFilterUtil.extractFilter(between, "prop2");
		assertNull(f2);
	}

	@Test
	public void testExtractFilter_Complex2() {

		Like compare = new Like("prop1", "someString");
		And and = new And(compare,
			new Compare.Equal("prop3", "someString"));

		// first operand
		Filter f1 = DynamoFilterUtil.extractFilter(and, "prop1");
		assertNotNull(f1);

		// second operand
		Filter f3 = DynamoFilterUtil.extractFilter(and, "prop3");
		assertNotNull(f3);

		// wrong property
		Filter f2 = DynamoFilterUtil.extractFilter(compare, "prop2");
		assertNull(f2);
	}

	@Test
	public void testExtractFilter_In() {

		In in = new In("prop1", List.of("a", "b"));
		And and = new And(in,
			new Compare.Equal("prop3", "someString"));

		// first operand
		Filter f1 = DynamoFilterUtil.extractFilter(and, "prop1");
		assertTrue(f1 instanceof In);
	}

	@Test
	public void testExtractFilter_Contains() {

		Contains contains = new Contains("prop1", "a");
		And and = new And(contains,
			new Compare.Equal("prop3", "someString"));

		// first operand
		Filter f1 = DynamoFilterUtil.extractFilter(and, "prop1");
		assertTrue(f1 instanceof Contains);
	}

	@Test
	public void testExtractFilter_Compare2() {

		Compare.Equal compare = new Compare.Equal("prop1", "someString");
		Filter f1 = DynamoFilterUtil.extractFilter(compare, "prop1");
		assertNotNull(f1);

		// wrong property
		Filter f2 = DynamoFilterUtil.extractFilter(compare, "prop2");
		assertNull(f2);
	}

	@Test
	public void testFlattenAnd() {
		Like compare = new Like("prop1", "someString");
		And and = new And(compare,
			new Compare.Equal("prop3", "someString"));

		Like compare2 = new Like("prop1", "someString2");
		And and2 = new And(and, compare2);

		List<Filter> flattened2 = DynamoFilterUtil.flattenAnd(and);
		assertEquals(2, flattened2.size());
		assertEquals(compare, flattened2.get(0));

		List<Filter> flattened = DynamoFilterUtil.flattenAnd(and2);
		assertEquals(3, flattened.size());
		assertEquals(compare, flattened.get(0));
		assertEquals(compare2, flattened.get(2));
	}

	@Test
	public void testRemoveFilters() {

		Compare.Equal compare1 = new Compare.Equal("prop1", "someString");
		Compare.Equal compare2 = new Compare.Equal("prop2", "someString");
		Compare.Equal compare3 = new Compare.Equal("prop3", "someString");

		And and = new And(compare1, compare2, compare3);

		// remove a filter and check there are still 2 left
		DynamoFilterUtil.removeFilters(and, "prop1");
		assertEquals(2, and.getFilters().size());

		and = new And(compare1, compare2, compare3);
		// remove a non-existing filter and check there are still 2 left
		DynamoFilterUtil.removeFilters(and, "prop4");
		assertEquals(3, and.getFilters().size());

		// remove nested filters and check that the empty filter on the top level is
		// removed
		And nested = new And(compare1, new And(compare2, compare3));
		DynamoFilterUtil.removeFilters(nested, "prop2", "prop3");
		assertEquals(1, nested.getFilters().size());

		and = new And(compare1, new Not(compare2));
		DynamoFilterUtil.removeFilters(and, "prop2");
		assertEquals(1, and.getFilters().size());

		and = new And(compare1, new Not(new And(compare2, compare3)));
		DynamoFilterUtil.removeFilters(and, "prop2", "prop3");
		assertEquals(1, and.getFilters().size());
	}

	/**
	 * Test that a filter that searches for a detail
	 */
	@Test
	public void testReplaceMasterAndDetailFilters1() {
		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);

		And and = new And(new Compare.Equal("tags", List.of("abc")));

		// check that the equals filter is replaced by an Or-filter that consists of
		// "contains"
		// clauses
		DynamoFilterUtil.replaceMasterAndDetailFilters(and, model);
		Filter replaced = and.getFilters().get(0);
		assertTrue(replaced instanceof Or);
		Or or = (Or) replaced;
		assertTrue(or.getFilters().get(0) instanceof Contains);
		Contains ct = (Contains) or.getFilters().get(0);
		assertEquals("abc", ct.getValue());

		// now once more but now without the intermediate OR filter
		and = new And(new Compare.Equal("tags", "def"));

		DynamoFilterUtil.replaceMasterAndDetailFilters(and, model);
		replaced = and.getFilters().get(0);
		assertTrue(replaced instanceof Contains);
		ct = (Contains) replaced;
		assertEquals("def", ct.getValue());

	}

	/**
	 * Test that a filter that searches on a master field is replaced by an "In"
	 * filter
	 */
	@Test
	public void testReplaceMasterAndDetailFilters2() {
		EntityModel<TestEntity2> model = entityModelFactory.getModel(TestEntity2.class);

		And and = new And(new Compare.Equal("testEntity", List.of(new TestEntity(), new TestEntity())));

		// check that the equals filter is replaced by an "in" filter
		DynamoFilterUtil.replaceMasterAndDetailFilters(and, model);
		Filter replaced = and.getFilters().get(0);
		assertTrue(replaced instanceof In);
		In in = (In) replaced;
		assertEquals(2, in.getValues().size());

		// if there is just one value then no replacement is needed
		and = new And(new Compare.Equal("testEntity", new TestEntity()));
		DynamoFilterUtil.replaceMasterAndDetailFilters(and, model);
		replaced = and.getFilters().get(0);
		assertTrue(replaced instanceof Compare.Equal);
	}

	@Test
	public void testReplaceMasterAndDetailFilters3() {
		EntityModel<TestEntity2> model = entityModelFactory.getModel(TestEntity2.class);

		And and = new And(new Compare.Equal("testEntity", List.of(new TestEntity(), new TestEntity())));

		// check that the equals filter is replaced by an "in" filter
		DynamoFilterUtil.replaceMasterAndDetailFilters(and, model);
		Filter replaced = and.getFilters().get(0);
		assertTrue(replaced instanceof In);
		In in = (In) replaced;
		assertEquals(2, in.getValues().size());

		// if there is just one value then no replacement is needed
		and = new And(new Compare.Equal("testEntity", new TestEntity()));
		DynamoFilterUtil.replaceMasterAndDetailFilters(and, model);
		replaced = and.getFilters().get(0);
		assertTrue(replaced instanceof Compare.Equal);
	}

}
