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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.ocs.dynamo.ui.composite.dialog.ModelBasedSearchDialog;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

public class EntityLookupFieldTest extends BaseMockitoTest {

	@Mock
	private UI ui;

	@Mock
	private TestEntityService service;

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	private TestEntity e1;

	private TestEntity e2;

	@Override
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		e1 = new TestEntity(1, "Bob", 14L);
		e2 = new TestEntity(2, "Kevin", 15L);

		Mockito.when(service.getEntityClass()).thenReturn(TestEntity.class);

		Mockito.when(service.findIds(Matchers.any(Filter.class), (com.ocs.dynamo.dao.SortOrder[]) Matchers.anyVararg()))
		        .thenReturn(Lists.newArrayList(1, 2));

		Mockito.when(
		        service.fetchByIds(Matchers.any(List.class), Matchers.any(SortOrders.class),
		                (FetchJoinInformation[]) Matchers.anyVararg())).thenReturn(Lists.newArrayList(e1, e2));

		Mockito.when(service.createNewEntity()).thenReturn(new TestEntity());
		MockUtil.mockServiceSave(service, TestEntity.class);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void test() {
		EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service,
		        factory.getModel(TestEntity.class), null, null, false, false, Lists.newArrayList(new SortOrder("name",
		                SortDirection.ASCENDING)));
		MockUtil.injectUI(field, ui);

		field.initContent();

		Assert.assertEquals(new SortOrder("name", SortDirection.ASCENDING), field.getSortOrders().get(0));
		Assert.assertEquals(Object.class, field.getType());

		Component comp = field.iterator().next();
		Assert.assertTrue(comp instanceof DefaultHorizontalLayout);

		ArgumentCaptor<ModelBasedSearchDialog> captor = ArgumentCaptor.forClass(ModelBasedSearchDialog.class);

		field.getSelectButton().click();
		Mockito.verify(ui).addWindow(captor.capture());

		captor.getValue().getOkButton().click();

		field.setEnabled(false);
		Assert.assertFalse(field.getSelectButton().isEnabled());
		Assert.assertFalse(field.getClearButton().isEnabled());
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testMultipleSelect() {
		EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service,
		        factory.getModel(TestEntity.class), null, null, false, true, Lists.newArrayList(new SortOrder("name",
		                SortDirection.ASCENDING)));
		MockUtil.injectUI(field, ui);
		field.initContent();

		ArgumentCaptor<ModelBasedSearchDialog> captor = ArgumentCaptor.forClass(ModelBasedSearchDialog.class);

		field.getSelectButton().click();
		Mockito.verify(ui).addWindow(captor.capture());

		ModelBasedSearchDialog<Integer, TestEntity> dialog = (ModelBasedSearchDialog<Integer, TestEntity>) captor
		        .getValue();
		dialog.getOkButton().click();
	}

	@Test
	public void testPageLength() {
		EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service,
		        factory.getModel(TestEntity.class), null, null, false, false, Lists.newArrayList(new SortOrder("name",
		                SortDirection.ASCENDING)));
		field.setPageLength(10);
		field.initContent();
		MockUtil.injectUI(field, ui);

		Assert.assertEquals(new SortOrder("name", SortDirection.ASCENDING), field.getSortOrders().get(0));
		Assert.assertEquals(10, field.getPageLength().intValue());

	}

	/**
	 * Test that the clear button has the desired effect
	 */
	@Test
	public void testClear() {
		EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service,
		        factory.getModel(TestEntity.class), null, null, false, false, Lists.newArrayList(new SortOrder("name",
		                SortDirection.ASCENDING)));
		field.initContent();
		field.setValue(new TestEntity("Kevin", 47L));

		field.getClearButton().click();
		Assert.assertNull(field.getValue());
	}

	@Test
	public void testAdd() {
		EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service,
		        factory.getModel(TestEntity.class), factory.getModel(TestEntity.class).getAttributeModel("testDomain"),
		        null, false, false, Lists.newArrayList(new SortOrder("name", SortDirection.ASCENDING)));
		field.initContent();
		MockUtil.injectUI(field, ui);

		// check that a model dialog is shown
		addNewValue(field, "New Item");

		// check that a new item has been added
		TestEntity tNew = (TestEntity) field.getValue();
		Assert.assertEquals("New Item", tNew.getName());
	}

	/**
	 * Test adding a new item in multiple select mode
	 */
	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testAddMultiple() {
		// now in multiple select mode
		EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service,
		        factory.getModel(TestEntity.class), factory.getModel(TestEntity.class).getAttributeModel("testDomain"),
		        null, false, true, Lists.newArrayList(new SortOrder("name", SortDirection.ASCENDING)));
		field.initContent();
		MockUtil.injectUI(field, ui);

		field.setValue(Lists.newArrayList(new TestEntity(1, "Old Item", 2L)));
		addNewValue(field, "New Item");

		// check that a new item has been added
		List<TestEntity> newValue = (List<TestEntity>) field.getValue();
		Assert.assertEquals(2, newValue.size());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addNewValue(EntityLookupField<Integer, TestEntity> field, String newValue) {
		ArgumentCaptor<AddNewValueDialog> captor = ArgumentCaptor.forClass(AddNewValueDialog.class);

		field.getAddButton().click();
		Mockito.verify(ui).addWindow(captor.capture());

		AddNewValueDialog<Integer, TestEntity> dialog = (AddNewValueDialog<Integer, TestEntity>) captor.getValue();
		dialog.getValueField().setValue(newValue);
		dialog.getOkButton().click();
	}
}
