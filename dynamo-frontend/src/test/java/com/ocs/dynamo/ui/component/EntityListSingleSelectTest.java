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
package com.ocs.dynamo.ui.component;

import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.SelectMode;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class EntityListSingleSelectTest extends BaseMockitoTest {

	private final EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private TestEntityService service;

	@Test
	public void testAll() {
		EntityListSingleSelect<Integer, TestEntity> select = new EntityListSingleSelect<>(
				factory.getModel(TestEntity.class), null, service);
		assertThat(select.getSelectMode()).isEqualTo(SelectMode.ALL);
		verify(service).findAll((SortOrder[]) null);
	}

	@Test
	public void testFixed() {
		EntityListSingleSelect<Integer, TestEntity> select = new EntityListSingleSelect<>(
				factory.getModel(TestEntity.class), null, List.of(new TestEntity()));
		assertThat(select.getSelectMode()).isEqualTo(SelectMode.FIXED);
		verifyNoInteractions(service);
	}

	@Test
	public void testFilterPaged() {

		EntityListSingleSelect<Integer, TestEntity> select = new EntityListSingleSelect<>(
				factory.getModel(TestEntity.class), null, service, new EqualsPredicate<>("name", "Bob"));
		assertThat(select.getSelectMode()).isEqualTo(SelectMode.FILTERED_PAGED);
		verify(service).fetch(nullable(com.ocs.dynamo.filter.Filter.class), eq(0), anyInt(), any(SortOrders.class));
	}

	@Test
	public void testFilterNonPaged() {

		EntityListSingleSelect<Integer, TestEntity> select = new EntityListSingleSelect<>(
				factory.getModel("TestEntityNonPaged", TestEntity.class), null, service, SelectMode.FILTERED_ALL,
				new EqualsPredicate<TestEntity>("name", "Bob"), null, null);

		assertThat(select.getSelectMode()).isEqualTo(SelectMode.FILTERED_ALL);
		verify(service).find(any(com.ocs.dynamo.filter.Filter.class), isNull());
	}

	@Test
	public void testRefresh() {

		EntityListSingleSelect<Integer, TestEntity> select = new EntityListSingleSelect<>(
				factory.getModel(TestEntity.class), null, service, new EqualsPredicate<TestEntity>("name", "Bob"));
		assertEquals(SelectMode.FILTERED_PAGED, select.getSelectMode());

		verify(service, times(1)).fetch(isNull(), any(Integer.class), any(Integer.class),
				any(SortOrders.class));

		select.refresh();
		verify(service, times(2)).fetch(isNull(), any(Integer.class), any(Integer.class),
				any(SortOrders.class));
	}

}
