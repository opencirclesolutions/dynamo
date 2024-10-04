package org.dynamoframework.dao;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.dynamoframework.exception.OCSRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SortOrderTest {

	@Test
	public void testDirectionFromString() {
		Assertions.assertEquals(SortOrder.Direction.ASC, SortOrder.Direction.fromString("asc"));
		Assertions.assertEquals(SortOrder.Direction.ASC, SortOrder.Direction.fromString("ASC"));
		Assertions.assertEquals(SortOrder.Direction.DESC, SortOrder.Direction.fromString("DESC"));
		Assertions.assertEquals(SortOrder.Direction.DESC, SortOrder.Direction.fromString("desc"));
	}

	@Test
	public void testDirectionFromStringFail() {
		assertThrows(OCSRuntimeException.class, () -> SortOrder.Direction.fromString("bogus"));
	}

	@Test
	public void testCreate() {
		SortOrder order = new SortOrder("test");
		assertEquals("test", order.getProperty());
		Assertions.assertEquals(SortOrder.Direction.ASC, order.getDirection());

		order = new SortOrder("test", SortOrder.Direction.DESC);
		assertEquals("test", order.getProperty());
		Assertions.assertEquals(SortOrder.Direction.DESC, order.getDirection());

		order = order.withDirection(SortOrder.Direction.ASC);
		Assertions.assertEquals(SortOrder.Direction.ASC, order.getDirection());

		order = order.withDirection(SortOrder.Direction.fromString("desc"));
		Assertions.assertEquals(SortOrder.Direction.DESC, order.getDirection());
	}

	@Test
	public void testEquals() {
		SortOrder order1 = new SortOrder("test");
		SortOrder order2 = new SortOrder("test");
		SortOrder order3 = new SortOrder("test", SortOrder.Direction.DESC);

		assertNotEquals(null, order1);
		assertNotEquals(new Object(), order1);

		assertEquals(order1, order1);
		assertEquals(order1, order2);
		assertNotEquals(order1, order3);
	}
}
