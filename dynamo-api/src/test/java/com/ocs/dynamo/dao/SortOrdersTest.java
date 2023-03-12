package com.ocs.dynamo.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
		assertTrue(so.getOrderFor("test").isPresent());
		assertFalse(so.getOrderFor("test2").isPresent());

		so.addSortOrder(new SortOrder("test2"));
		assertTrue(so.getOrderFor("test").isPresent());
	}
}
