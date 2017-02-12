package com.ocs.dynamo.ui.component;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.explicatis.ext_token_field.Tokenizable;
import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.UI;

public class TokenFieldSelectTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private UI ui;

	@Mock
	private TestEntityService service;

	private TestEntity t1;

	private TestEntity t2;

	private TestEntity t3;

	@Before
	public void setUp() {
		t1 = new TestEntity(1, "Kevin", 12L);
		t2 = new TestEntity(2, "Bob", 13L);
		t3 = new TestEntity(3, "Stewart", 14L);

		Mockito.when(service.find(Matchers.any(Filter.class), (SortOrder[]) Matchers.anyVararg())).thenReturn(
		        Lists.newArrayList(t1, t2, t3));
		Mockito.when(service.createNewEntity()).thenReturn(new TestEntity());

		// make sure an ID is set on the entity when it is being saved
		Mockito.when(service.save(Matchers.any(TestEntity.class))).thenAnswer(new Answer<TestEntity>() {

			@Override
			public TestEntity answer(InvocationOnMock invocation) throws Throwable {
				TestEntity temp = (TestEntity) invocation.getArguments()[0];
				temp.setId(1234);
				return temp;
			}

		});
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testCreateWithQuickAdd() {

		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("testDomain");
		com.vaadin.data.sort.SortOrder order = new com.vaadin.data.sort.SortOrder("id", SortDirection.ASCENDING);
		TokenFieldSelect<Integer, TestEntity> select = new TokenFieldSelect<Integer, TestEntity>(em, am, service, null,
		        false, order);
		select.initContent();
		MockUtil.injectUI(select, ui);

		// verify that 3 items have been loaded
		Mockito.verify(service).find(null, new SortOrder("id"));
		Assert.assertEquals(3, select.getComboBox().getContainerDataSource().size());

		// quick add must be possible
		Assert.assertTrue(select.getAddButton().isVisible());

		// bring up the add dialog
		ArgumentCaptor<AddNewValueDialog> captor = ArgumentCaptor.forClass(AddNewValueDialog.class);

		select.getAddButton().click();
		Mockito.verify(ui).addWindow(captor.capture());

		AddNewValueDialog<Integer, TestEntity> dialog = (AddNewValueDialog<Integer, TestEntity>) captor.getValue();

		dialog.getValueField().setValue("New Item");

		dialog.getOkButton().click();

		// verify that an extra item has been added
		Assert.assertEquals(4, select.getComboBox().getContainerDataSource().size());

	}

	/**
	 * Create in search mode and verify there is no "Add" button
	 */
	@Test
	public void testCreateInSearchMode() {

		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("testDomain");
		com.vaadin.data.sort.SortOrder order = new com.vaadin.data.sort.SortOrder("id", SortDirection.ASCENDING);
		TokenFieldSelect<Integer, TestEntity> select = new TokenFieldSelect<Integer, TestEntity>(em, am, service, null,
		        true, order);
		select.initContent();
		MockUtil.injectUI(select, ui);

		Assert.assertNull(select.getAddButton());
	}

	@Test
	public void testCreateWithAndSelect() {

		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("testDomain");
		com.vaadin.data.sort.SortOrder order = new com.vaadin.data.sort.SortOrder("id", SortDirection.ASCENDING);
		TokenFieldSelect<Integer, TestEntity> select = new TokenFieldSelect<Integer, TestEntity>(em, am, service, null,
		        false, order);
		select.initContent();
		MockUtil.injectUI(select, ui);

		// select a value in the combo box and verify it is added to the token field
		select.getComboBox().setValue(t1);

		List<? extends Tokenizable> list = select.getTokenField().getValue();
		Assert.assertEquals(1, list.size());
		Assert.assertEquals(t1.getId().longValue(), list.get(0).getIdentifier());
	}
}