package com.ocs.dynamo.dao;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.dao.SortOrder.Direction;
import com.ocs.dynamo.exception.OCSRuntimeException;

public class SortOrderTest {

	@Test
	public void testDirectionFromString() {
		Assert.assertEquals(Direction.ASC, Direction.fromString("asc"));
		Assert.assertEquals(Direction.ASC, Direction.fromString("ASC"));
		Assert.assertEquals(Direction.DESC, Direction.fromString("DESC"));
		Assert.assertEquals(Direction.DESC, Direction.fromString("desc"));
	}

	@Test(expected = OCSRuntimeException.class)
	public void testDirectionFromStringFail() {
		Direction.fromString("bogus");
	}

	@Test
	public void testCreate() {
		SortOrder order = new SortOrder("test");
		Assert.assertEquals("test", order.getProperty());
		Assert.assertEquals(Direction.ASC, order.getDirection());

		order = new SortOrder(Direction.DESC, "test");
		Assert.assertEquals("test", order.getProperty());
		Assert.assertEquals(Direction.DESC, order.getDirection());

		order = order.with(Direction.ASC);
		Assert.assertEquals(Direction.ASC, order.getDirection());

		order = order.with(Direction.fromString("desc"));
		Assert.assertEquals(Direction.DESC, order.getDirection());
	}

	@Test
	public void testEquals() {
		SortOrder order1 = new SortOrder("test");
		SortOrder order2 = new SortOrder("test");
		SortOrder order3 = new SortOrder(Direction.DESC, "test");

		Assert.assertFalse(order1.equals(null));
		Assert.assertFalse(order1.equals(new Object()));

		Assert.assertTrue(order1.equals(order1));
		Assert.assertTrue(order1.equals(order2));
		Assert.assertFalse(order1.equals(order3));
	}
}
