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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.test.BaseMockitoTest;

public class FilterConverterTest extends BaseMockitoTest {

	private EntityModelFactory emf = new EntityModelFactoryImpl();

	private FilterConverter<TestEntity> converter = new FilterConverter<TestEntity>(null);

	private FilterConverter<TestEntity> modelConverter;

	private EqualsPredicate<TestEntity> f1 = new EqualsPredicate<TestEntity>("test", "test");

	private EqualsPredicate<TestEntity> f2 = new EqualsPredicate<TestEntity>("test", "test");

	@BeforeEach
	public void setUp() {
		modelConverter = new FilterConverter<TestEntity>(emf.getModel(TestEntity.class));
	}

	@Test
	public void testAnd() {
		Filter result = converter.convert(new AndPredicate<TestEntity>(f1, f2));
		assertTrue(result instanceof And);
	}

	@Test
	public void testBetween() {
		Filter result = converter.convert(new BetweenPredicate<TestEntity>("test", 1, 10));
		assertTrue(result instanceof Between);
	}

	@Test
	public void testCompareEqual() {
		Filter result = converter.convert(new EqualsPredicate<TestEntity>("test", "test"));
		assertTrue(result instanceof Compare.Equal);
	}

	@Test
	public void testCompareGreater() {
		Filter result = converter.convert(new GreaterThanPredicate<TestEntity>("test", "test"));
		assertTrue(result instanceof Compare.Greater);
	}

	@Test
	public void testCompareGreaterOrEqual() {
		Filter result = converter.convert(new GreaterOrEqualPredicate<TestEntity>("test", "test"));
		assertTrue(result instanceof Compare.GreaterOrEqual);
	}

	@Test
	public void testCompareLess() {
		Filter result = converter.convert(new LessThanPredicate<TestEntity>("test", "test"));
		assertTrue(result instanceof Compare.Less);
	}

	@Test
	public void testIn() {
		Filter result = converter.convert(new InPredicate<TestEntity>("test", Lists.newArrayList("v1", "v2")));
		assertTrue(result instanceof In);
	}

	@Test
	public void testContains() {
		Filter result = converter.convert(new ContainsPredicate<TestEntity>("test", "test"));
		assertTrue(result instanceof Contains);
	}

	@Test
	public void testModulo1() {
		Filter result = converter.convert(new ModuloPredicate<TestEntity>("test", 4, 2));
		assertTrue(result instanceof Modulo);

		Modulo mod = (Modulo) result;

		assertEquals(4, mod.getModValue());
		assertEquals(2, mod.getResult());
	}

	@Test
	public void testModulo2() {
		Filter result = converter.convert(new ModuloPredicate<TestEntity>("test", "test2", 2));
		assertTrue(result instanceof Modulo);

		Modulo mod = (Modulo) result;
		assertEquals("test2", mod.getModExpression());
		assertEquals(2, mod.getResult());
	}

	@Test
	public void testCompareLessOrEqual() {
		Filter result = converter.convert(new LessOrEqualPredicate<TestEntity>("test", "test"));
		assertTrue(result instanceof Compare.LessOrEqual);
	}

	@Test
	public void testIsNull() {
		Filter result = converter.convert(new IsNullPredicate<TestEntity>("test"));
		assertTrue(result instanceof IsNull);
	}

	@Test
	public void testLike() {
		Filter result = converter.convert(new LikePredicate<TestEntity>("test", "%test%", false));
		assertTrue(result instanceof Like);
		Like like = (Like) result;
		assertFalse(like.isCaseSensitive());
	}

	@Test
	public void testNot() {
		Filter result = converter.convert(new NotPredicate<TestEntity>(f1));
		assertTrue(result instanceof Not);
	}

	@Test
	public void testOr() {
		Filter result = converter.convert(new OrPredicate<TestEntity>(f1, f2));
		assertTrue(result instanceof Or);
	}

	/**
	 * Test that a search for a details attribute is replaced by a Contains filter
	 */
	@Test
	public void testSearchDetails() {
		Filter result = modelConverter.convert(
				new AndPredicate<TestEntity>(new EqualsPredicate<TestEntity>("testEntities", new TestEntity2())));

		assertTrue(result instanceof And);
		Filter first = ((And) result).getFilters().iterator().next();
		assertTrue(first instanceof Contains);
	}

	/**
	 * Test that a search for a details attribute is replaced by a Contains filter
	 */
	@Test
	public void testSearchDetails2() {
		List<TestEntity2> entities = Lists.newArrayList(new TestEntity2(), new TestEntity2());

		Filter result = modelConverter
				.convert(new AndPredicate<TestEntity>(new EqualsPredicate<TestEntity>("testEntities", entities)));

		assertTrue(result instanceof And);
		Filter first = ((And) result).getFilters().iterator().next();
		assertTrue(first instanceof Or);

		for (Filter f : ((Or) first).getFilters()) {
			assertTrue(f instanceof Contains);
		}
	}

	/**
	 * Simple string filter - case sensitive prefix only
	 */
	@Test
	public void testSimpleStringFilter1() {
		Filter result = converter.convert(new SimpleStringPredicate<TestEntity>("test", "test", false, true));
		assertTrue(result instanceof Like);
		Like like = (Like) result;
		assertTrue(like.isCaseSensitive());
		assertEquals("%test%", like.getValue());
	}

	/**
	 * Simple string filter - case insensitive infix
	 */
	@Test
	public void testSimpleStringFilter2() {
		Filter result = converter.convert(new SimpleStringPredicate<TestEntity>("test", "test", true, false));
		assertTrue(result instanceof Like);
		Like like = (Like) result;
		assertFalse(like.isCaseSensitive());
		assertEquals("test%", like.getValue());
	}
}
