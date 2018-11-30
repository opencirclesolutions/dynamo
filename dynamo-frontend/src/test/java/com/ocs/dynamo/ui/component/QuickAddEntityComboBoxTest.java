package com.ocs.dynamo.ui.component;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.ocs.dynamo.ui.component.EntityComboBox.SelectMode;
import com.vaadin.ui.UI;

public class QuickAddEntityComboBoxTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private UI ui;

	@Mock
	private TestEntityService service;

	private TestEntity t1;

	private TestEntity t2;

	private TestEntity t3;

	@Override
	@Before
	public void setUp() {
		t1 = new TestEntity(1, "Kevin", 12L);
		t2 = new TestEntity(2, "Bob", 13L);
		t3 = new TestEntity(3, "Stewart", 14L);

		Mockito.when(service.find(Matchers.isNull(Filter.class), (SortOrder[]) Matchers.anyVararg()))
				.thenReturn(Lists.newArrayList(t1, t2, t3));
		Mockito.when(service.find(Matchers.isNull(Filter.class))).thenReturn(Lists.newArrayList(t1, t2, t3));

		Mockito.when(service.find(Matchers.isA(com.ocs.dynamo.filter.Filter.class), (SortOrder[]) Matchers.anyVararg()))
				.thenReturn(Lists.newArrayList(t1));
		Mockito.when(service.find(Matchers.isA(com.ocs.dynamo.filter.Filter.class))).thenReturn(Lists.newArrayList(t1));

		Mockito.when(service.createNewEntity()).thenReturn(new TestEntity());
		MockUtil.mockServiceSave(service);
	}

	/**
	 * Test the creation of the component and a simple selection
	 */
	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testCreateAndSelect() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("testDomain");

		QuickAddEntityComboBox<Integer, TestEntity> select = new QuickAddEntityComboBox<>(em, am, service,
				SelectMode.FILTERED, null, false, null);
		select.initContent();
		MockUtil.injectUI(select, ui);

		// list must contain 3 items
		Assert.assertEquals(3, select.getComboBox().getDataProviderSize());

		// test propagation of the value
		select.setValue(t1);
		Assert.assertEquals(t1, select.getComboBox().getValue());

		// .. and the other way around
		select.getComboBox().setValue(t2);
		Assert.assertEquals(t2, select.getValue());

		// bring up the add dialog
		ArgumentCaptor<AddNewValueDialog> captor = ArgumentCaptor.forClass(AddNewValueDialog.class);

		select.getAddButton().click();
		Mockito.verify(ui).addWindow(captor.capture());

		AddNewValueDialog<Integer, TestEntity> dialog = captor.getValue();

		dialog.getValueField().setValue("New Item");

		dialog.getOkButton().click();

		// list must now contain an extra item
		Assert.assertEquals(4, select.getComboBox().getDataProviderSize());

	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testAddEmptyItem() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("testDomain");

		QuickAddEntityComboBox<Integer, TestEntity> select = new QuickAddEntityComboBox<>(em, am, service,
				SelectMode.FILTERED, null, false, null);
		select.initContent();
		MockUtil.injectUI(select, ui);

		// bring up the add dialog
		ArgumentCaptor<AddNewValueDialog> captor = ArgumentCaptor.forClass(AddNewValueDialog.class);

		select.getAddButton().click();
		Mockito.verify(ui).addWindow(captor.capture());

		AddNewValueDialog<Integer, TestEntity> dialog = captor.getValue();

		// try to add an empty value
		dialog.getValueField().clear();
		dialog.getOkButton().click();

		// no new item may have been created
		Assert.assertEquals(3, select.getComboBox().getDataProviderSize());
	}

	/**
	 * Try to add an item that is too long
	 */
	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testAddLongItem() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("testDomain");

		QuickAddEntityComboBox<Integer, TestEntity> select = new QuickAddEntityComboBox<>(em, am, service,
				SelectMode.FILTERED, null, false, null);
		select.initContent();
		MockUtil.injectUI(select, ui);

		// bring up the add dialog
		ArgumentCaptor<AddNewValueDialog> captor = ArgumentCaptor.forClass(AddNewValueDialog.class);

		select.getAddButton().click();
		Mockito.verify(ui).addWindow(captor.capture());

		AddNewValueDialog<Integer, TestEntity> dialog = captor.getValue();

		// try to add an empty value
		dialog.getValueField().setValue("LongLongLongLongLongLongLongLongLong");
		dialog.getOkButton().click();

		// no new item may have been created
		Assert.assertEquals(3, select.getComboBox().getDataProviderSize());
	}

	@Test
	public void testAdditionalFilter() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("testDomain");

		QuickAddEntityComboBox<Integer, TestEntity> select = new QuickAddEntityComboBox<>(em, am, service,
				SelectMode.FILTERED, null, false, null);
		select.initContent();
		MockUtil.injectUI(select, ui);

		Assert.assertEquals(3, select.getComboBox().getDataProviderSize());

		// apply an additional filter
		select.setAdditionalFilter(new EqualsPredicate<TestEntity>("name", "Kevin"));
		Assert.assertEquals(1, select.getComboBox().getDataProviderSize());

		// and remove it again
		select.clearAdditionalFilter();
		Assert.assertEquals(3, select.getComboBox().getDataProviderSize());
	}

	@Test
	public void testRefresh() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("testDomain");

		QuickAddEntityComboBox<Integer, TestEntity> select = new QuickAddEntityComboBox<>(em, am, service,
				SelectMode.FILTERED, null, false, null);
		select.initContent();
		MockUtil.injectUI(select, ui);

		Assert.assertEquals(3, select.getComboBox().getDataProviderSize());

		// refresh with a filter
		select.refresh(new EqualsPredicate<TestEntity>("name", "Kevin"));
		Assert.assertEquals(1, select.getComboBox().getDataProviderSize());

		// just a regular refresh
		select.refresh();
		Assert.assertEquals(1, select.getComboBox().getDataProviderSize());
	}

}
