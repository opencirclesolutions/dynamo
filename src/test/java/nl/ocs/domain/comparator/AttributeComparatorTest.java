package nl.ocs.domain.comparator;

import nl.ocs.domain.TestEntity;

import org.junit.Assert;
import org.junit.Test;

public class AttributeComparatorTest {

	@Test
	public void test() {

		TestEntity t1 = new TestEntity("bert", 44L);
		TestEntity t2 = new TestEntity("chloe", 33L);
		TestEntity t3 = new TestEntity(null, 33L);

		Assert.assertEquals(0, new AttributeComparator("name").compare(t1, t1));

		Assert.assertEquals(-1, new AttributeComparator("name").compare(t1, t2));
		Assert.assertEquals(1, new AttributeComparator("name").compare(t2, t1));

		// when comparing on age, the result is reversed
		Assert.assertEquals(1, new AttributeComparator("age").compare(t1, t2));
		Assert.assertEquals(-1, new AttributeComparator("age").compare(t2, t1));

		// null value wins
		Assert.assertEquals(1, new AttributeComparator("name").compare(t1, t3));
		Assert.assertEquals(-1, new AttributeComparator("name").compare(t3, t1));
	}

}
