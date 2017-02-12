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
package com.ocs.dynamo.ui.composite.table;

import java.math.BigDecimal;
import java.util.ArrayList;

import junitx.util.PrivateAccessor;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Table;

public class ModelBasedTableTest extends BaseMockitoTest {

	private EntityModelFactory entityModelFactory = new EntityModelFactoryImpl();

	@Mock
	private MessageService messageService;

	@Mock
	private TestEntityService service;

	@Override
	public void setUp() {
		super.setUp();
		Mockito.when(service.getEntityClass()).thenReturn(TestEntity.class);
		MockUtil.mockMessageService(messageService);
		try {
			PrivateAccessor.setField(entityModelFactory, "messageService", messageService);
		} catch (NoSuchFieldException e) {
			Assert.fail();
		}
	}

	@Test
	public void testBeanItemContainer() {
		BeanItemContainer<Person> container = new BeanItemContainer<>(Person.class);
		EntityModel<Person> model = entityModelFactory.getModel(Person.class);

		Person person = new Person(1, "Bob", 50, BigDecimal.valueOf(76.4), BigDecimal.valueOf(44.4));
		container.addItem(person);

		ModelBasedTable<Integer, Person> table = new ModelBasedTable<>(container, model, false);

		Assert.assertEquals("Persons", table.getCaption());
		Assert.assertEquals("Person", table.getDescription());
		Assert.assertEquals(4, table.getVisibleColumns().length);

		// numeric column aligned to the right
		Assert.assertEquals(Table.Align.RIGHT, table.getColumnAlignment("age"));

		String result = table.formatPropertyValue(person, "age", table.getItem(person).getItemProperty("age"));
		Assert.assertEquals("50", result);
	}

	@Test
	public void testFixedTableWrapper() {
		TestEntity entity = new TestEntity();

		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);
		FixedTableWrapper<Integer, TestEntity> wrapper = new FixedTableWrapper<Integer, TestEntity>(service, model,
		        Lists.newArrayList(entity), new ArrayList<SortOrder>(), false);
		wrapper.build();

		Table table = wrapper.getTable();
		Assert.assertNotNull(table);

		Assert.assertEquals(1, wrapper.getTable().getContainerDataSource().size());
	}

}
