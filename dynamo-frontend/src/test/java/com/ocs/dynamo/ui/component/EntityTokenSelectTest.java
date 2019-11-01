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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;

public class EntityTokenSelectTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private TestEntityService service;

	@Test
	public void testAll() {
		EntityTokenSelect<Integer, TestEntity> select = new EntityTokenSelect<>(factory.getModel(TestEntity.class), null,
				service);
		Assert.assertEquals(EntityTokenSelect.SelectMode.ALL, select.getSelectMode());
		Mockito.verify(service).findAll((SortOrder[]) null);
	}

	@Test
	public void testFixed() {
		EntityTokenSelect<Integer, TestEntity> select = new EntityTokenSelect<>(factory.getModel(TestEntity.class), null,
				Lists.newArrayList(new TestEntity()));
		Assert.assertEquals(EntityTokenSelect.SelectMode.FIXED, select.getSelectMode());
		Mockito.verifyZeroInteractions(service);
	}

	@Test
	public void testFilter() {

		EntityTokenSelect<Integer, TestEntity> select = new EntityTokenSelect<>(factory.getModel(TestEntity.class), null,
				service, new EqualsPredicate<TestEntity>("name", "Bob"));
		Assert.assertEquals(EntityTokenSelect.SelectMode.FILTERED, select.getSelectMode());

		Mockito.verify(service).find(Mockito.any(com.ocs.dynamo.filter.Filter.class), Mockito.isNull());
	}

	@Test
	public void testRefreshFiltered() {

		EntityTokenSelect<Integer, TestEntity> select = new EntityTokenSelect<>(factory.getModel(TestEntity.class), null,
				service, new EqualsPredicate<TestEntity>("name", "Bob"));
		Assert.assertEquals(EntityTokenSelect.SelectMode.FILTERED, select.getSelectMode());

		select.refresh();

		Mockito.verify(service).find(Mockito.any(com.ocs.dynamo.filter.Filter.class), Mockito.isNull());
	}

	@Test
	public void testRefreshAll() {

		EntityTokenSelect<Integer, TestEntity> select = new EntityTokenSelect<>(factory.getModel(TestEntity.class), null,
				service, new EqualsPredicate<TestEntity>("name", "Bob"));
		Assert.assertEquals(EntityTokenSelect.SelectMode.FILTERED, select.getSelectMode());

		select.refresh();

		Mockito.verify(service).find(Mockito.any(com.ocs.dynamo.filter.Filter.class), Mockito.isNull());
	}
}