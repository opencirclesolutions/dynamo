/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.dynamoframework.dao.impl;

import org.dynamoframework.domain.TestEntity;
import org.dynamoframework.domain.query.PagingDataSetIterator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataSetIteratorTest {

	private PagingDataSetIterator<Integer, TestEntity> iterator;

	private final List<Integer> ids = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

	private final List<Integer> ids2 = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);

	private int pagesRead = 0;

	@Test
	public void test() {

		iterator = new PagingDataSetIterator<>(ids, pages -> {
			List<TestEntity> result = new ArrayList<>();
			for (Integer i : pages) {
				TestEntity entity = new TestEntity();
				entity.setId(i);
				result.add(entity);
			}
			pagesRead++;
			return result;
		}, 5);

		int i = 0;
		TestEntity entity = null;
		do {
			entity = iterator.next();
			if (entity != null) {
				i++;
			}
		} while (entity != null);

		assertEquals(10, i);
		assertEquals(2, pagesRead);
	}

	@Test
	public void testPartial() {

		iterator = new PagingDataSetIterator<>(ids2, page -> {
			List<TestEntity> result = new ArrayList<>();
			for (Integer i : page) {
				TestEntity entity = new TestEntity();
				entity.setId(i);
				result.add(entity);
			}
			pagesRead++;
			return result;
		}, 5);

		int i = 0;
		TestEntity entity = null;
		do {
			entity = iterator.next();
			if (entity != null) {
				i++;
			}
		} while (entity != null);

		assertEquals(12, i);
		assertEquals(3, pagesRead);
	}
}
