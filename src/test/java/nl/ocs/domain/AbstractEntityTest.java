package nl.ocs.domain;

import org.junit.Assert;
import org.junit.Test;

public class AbstractEntityTest {

	@Test
	public void testEquals() {
		TestEntity e1 = new TestEntity();
		e1.setId(1);

		Assert.assertFalse(e1.equals(null));
		Assert.assertFalse(e1.equals(new Integer(14)));
		Assert.assertTrue(e1.equals(e1));

		// objects with the same ID are equal
		TestEntity e2 = new TestEntity();
		e2.setId(1);
		Assert.assertTrue(e1.equals(e2));

		// IDs not equal
		TestEntity e3 = new TestEntity();
		Assert.assertFalse(e1.equals(e3));

		TestEntity e4 = new TestEntity();
		e4.setId(2);
		Assert.assertFalse(e1.equals(e4));
	}
}
