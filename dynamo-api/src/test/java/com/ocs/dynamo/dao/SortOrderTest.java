package com.ocs.dynamo.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.dao.SortOrder.Direction;
import com.ocs.dynamo.exception.OCSRuntimeException;

public class SortOrderTest {

	@Test
	public void testDirectionFromString() {
		assertEquals(Direction.ASC, Direction.fromString("asc"));
		assertEquals(Direction.ASC, Direction.fromString("ASC"));
		assertEquals(Direction.DESC, Direction.fromString("DESC"));
		assertEquals(Direction.DESC, Direction.fromString("desc"));
	}

	@Test
	public void testDirectionFromStringFail() {
		assertThrows(OCSRuntimeException.class, () -> Direction.fromString("bogus"));
	}

	@Test
	public void testCreate() {
		SortOrder order = new SortOrder("test");
		assertEquals("test", order.getProperty());
		assertEquals(Direction.ASC, order.getDirection());

		order = new SortOrder("test", Direction.DESC);
		assertEquals("test", order.getProperty());
		assertEquals(Direction.DESC, order.getDirection());

		order = order.withDirection(Direction.ASC);
		assertEquals(Direction.ASC, order.getDirection());

		order = order.withDirection(Direction.fromString("desc"));
		assertEquals(Direction.DESC, order.getDirection());
	}

	@Test
	public void testEquals() {
		SortOrder order1 = new SortOrder("test");
		SortOrder order2 = new SortOrder("test");
		SortOrder order3 = new SortOrder("test", Direction.DESC);

		assertNotEquals(null, order1);
		assertNotEquals(new Object(), order1);

		assertEquals(order1, order1);
		assertEquals(order1, order2);
		assertNotEquals(order1, order3);
	}
}
