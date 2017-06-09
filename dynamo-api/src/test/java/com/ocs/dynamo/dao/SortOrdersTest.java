package com.ocs.dynamo.dao;

import org.junit.Assert;
import org.junit.Test;

public class SortOrdersTest {

	@Test
	public void testCreateAndAdd() {
		SortOrders so = new SortOrders();
		Assert.assertEquals(0, so.toArray().length);

		// empty sort order will not be added
		so.addSortOrder(new SortOrder(null));
		Assert.assertEquals(0, so.toArray().length);

		// create sort order with a single operand
		so = new SortOrders(new SortOrder("test"));
		Assert.assertEquals(1, so.toArray().length);
		Assert.assertNotNull(so.getOrderFor("test"));
		Assert.assertNull(so.getOrderFor("test2"));

		so.addSortOrder(new SortOrder("test2"));
		Assert.assertNotNull(so.getOrderFor("test"));
	}
}
