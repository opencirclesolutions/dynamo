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
