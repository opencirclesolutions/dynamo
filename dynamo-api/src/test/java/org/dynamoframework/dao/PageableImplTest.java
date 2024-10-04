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

import static org.junit.jupiter.api.Assertions.assertEquals;


public class PageableImplTest {

	@Test
	public void testCreate() {
		PageableImpl p = new PageableImpl(0, 10);

		assertEquals(0, p.getPageNumber());
		assertEquals(0, p.getOffset());
		assertEquals(0, p.getSortOrders().toArray().length);

		p = new PageableImpl(2, 10);
		assertEquals(20, p.getOffset());
		assertEquals(2, p.getPageNumber());

		p = new PageableImpl(2, 10, new SortOrder("test1"));
		assertEquals(20, p.getOffset());
		assertEquals(1, p.getSortOrders().toArray().length);

		p = new PageableImpl(2, 10, new SortOrders(new SortOrder("test1"), new SortOrder("test2")));
		assertEquals(20, p.getOffset());
		assertEquals(2, p.getSortOrders().toArray().length);

		p = new PageableImpl(2, 10, new SortOrder("test1"), new SortOrder("test2"), new SortOrder("test3"));
		assertEquals(20, p.getOffset());
		assertEquals(3, p.getSortOrders().toArray().length);
	}
}
