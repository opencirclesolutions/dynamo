package com.ocs.dynamo.domain.comparator;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;

public class IdComparatorTest {

	@Test
	public void test() {
		TestEntity t1 = new TestEntity("name", 12L);
		t1.setId(4);

		TestEntity t2 = new TestEntity("name", 12L);
		t2.setId(3);

		TestEntity t3 = new TestEntity("name", 12L);
		t3.setId(null);

		Assert.assertEquals(0, new IdComparator().compare(t1, t1));
		Assert.assertEquals(1, new IdComparator().compare(t1, t2));
		Assert.assertEquals(-1, new IdComparator().compare(t2, t1));

		Assert.assertEquals(-1, new IdComparator().compare(t3, t1));
		Assert.assertEquals(1, new IdComparator().compare(t1, t3));

	}

}
