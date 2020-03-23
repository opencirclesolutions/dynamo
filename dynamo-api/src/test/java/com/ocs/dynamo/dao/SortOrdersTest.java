package com.ocs.dynamo.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class SortOrdersTest {

	@Test
	public void testCreateAndAdd() {
		SortOrders so = new SortOrders();
		assertEquals(0, so.toArray().length);
		assertEquals(0, so.getNrOfSortOrders());

		// empty sort order will not be added
		so.addSortOrder(new SortOrder(null));
		assertEquals(0, so.toArray().length);

		// create sort order with a single operand
		so = new SortOrders(new SortOrder("test"));
		assertEquals(1, so.toArray().length);
		assertNotNull(so.getOrderFor("test"));
		assertNull(so.getOrderFor("test2"));

		so.addSortOrder(new SortOrder("test2"));
		assertNotNull(so.getOrderFor("test"));
	}
}
