package com.ocs.dynamo.dao;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.dao.SortOrder.Direction;
import com.ocs.dynamo.exception.OCSRuntimeException;

public class SortOrderDaoTest {

	@Test
	public void testDefaultAscending() {
		SortOrder order = new SortOrder("property");

		Assert.assertEquals(Direction.ASC, order.getDirection());
		Assert.assertEquals("property", order.getProperty());
	}

	@Test
	public void testPropertyAndDirection() {
		SortOrder order = new SortOrder(Direction.DESC, "property");

		Assert.assertEquals(Direction.DESC, order.getDirection());
		Assert.assertEquals("property", order.getProperty());
	}

	@Test
	public void testPropertyAndDirectionFromString() {
		SortOrder order = new SortOrder(Direction.fromString("ASC"), "property");

		Assert.assertEquals(Direction.ASC, order.getDirection());
		Assert.assertEquals("property", order.getProperty());
	}

	@Test
	public void testPropertyAndDirectionFromString2() {
		SortOrder order = new SortOrder(Direction.fromString("DESC"), "property");

		Assert.assertEquals(Direction.DESC, order.getDirection());
		Assert.assertEquals("property", order.getProperty());
	}

	@Test(expected = OCSRuntimeException.class)
	public void testPropertyAndDirectionFromString_Wrong() {
		new SortOrder(Direction.fromString("AS"), "property");
	}

	@Test
	public void testEquals() {
		SortOrder order = new SortOrder("property");
		SortOrder order2 = new SortOrder("property");
		SortOrder order3 = new SortOrder(Direction.DESC, "property");

		Assert.assertFalse(order.equals(null));
		Assert.assertFalse(order.equals(new Object()));
		Assert.assertTrue(order.equals(order));
		Assert.assertTrue(order.equals(order2));
		Assert.assertFalse(order.equals(order3));
	}
}
