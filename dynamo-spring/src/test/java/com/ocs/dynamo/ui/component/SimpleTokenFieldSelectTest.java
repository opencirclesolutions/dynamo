package com.ocs.dynamo.ui.component;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

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
import com.vaadin.shared.data.sort.SortDirection;

public class SimpleTokenFieldSelectTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private TestEntityService service;

	@Test
	public void testCreate() {
		com.vaadin.data.sort.SortOrder order = new com.vaadin.data.sort.SortOrder("name", SortDirection.ASCENDING);
		List<String> items = Lists.newArrayList("Kevin", "Stuart", "Bob");
		Mockito.when(
		        service.findDistinct(Matchers.any(Filter.class), Matchers.eq("name"), Matchers.eq(String.class),
		                (SortOrder) Matchers.anyVararg())).thenReturn(items);

		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("name");

		SimpleTokenFieldSelect<Integer, TestEntity, String> select = new SimpleTokenFieldSelect<Integer, TestEntity, String>(
		        service, em, am, null, "name", String.class, false, order);
		select.initContent();

		// select an item and verify it is added as a token
		select.getComboBox().setValue("Kevin");
		Assert.assertEquals(1, select.getTokenField().getValue().size());
		Tokenizable t = select.getTokenField().getValue().get(0);
		Assert.assertEquals("Kevin", t.getStringValue());
		Assert.assertEquals(Lists.newArrayList("Kevin"), select.getValue());

		// remove the value again
		select.getTokenField().removeTokenizable(t);
		Assert.assertNull(select.getTokenField().getValue());
		Assert.assertNull(select.getValue());
	}

	@Test
	public void testCreateAndOrder() {

		com.vaadin.data.sort.SortOrder order = new com.vaadin.data.sort.SortOrder("name", SortDirection.ASCENDING);
		List<String> items = Lists.newArrayList("Kevin", "Stuart", "Bob");
		Mockito.when(
		        service.findDistinct(Matchers.any(Filter.class), Matchers.eq("name"), Matchers.eq(String.class),
		                (SortOrder) Matchers.anyVararg())).thenReturn(items);

		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("name");
		SimpleTokenFieldSelect<Integer, TestEntity, String> select = new SimpleTokenFieldSelect<Integer, TestEntity, String>(
		        service, em, am, null, "name", String.class, false, order);
		select.initContent();

		// select two values and verify that they are added as tokens and removed from the combo box
		select.getComboBox().setValue("Kevin");
		select.getComboBox().setValue("Bob");
		Assert.assertEquals(2, select.getTokenField().getValue().size());
		Assert.assertEquals(1, select.getComboBox().getContainerDataSource().size());
	}

	@Test
	public void testElementCollection() {

		com.vaadin.data.sort.SortOrder order = new com.vaadin.data.sort.SortOrder("name", SortDirection.ASCENDING);
		List<String> items = Lists.newArrayList("Kevin", "Stuart", "Bob");
		Mockito.when(
		        service.findDistinctInCollectionTable(Matchers.anyString(), Matchers.anyString(), 
		        		Matchers.eq(String.class))).thenReturn(items);

		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("name");
		SimpleTokenFieldSelect<Integer, TestEntity, String> select = new SimpleTokenFieldSelect<Integer, TestEntity, String>(
		        service, em, am, null, "name", String.class, true, order);
		select.initContent();

		// select two values and verify that they are added as tokens and removed from the combo box
		select.getComboBox().setValue("Kevin");
		select.getComboBox().setValue("Bob");
		Assert.assertEquals(2, select.getTokenField().getValue().size());
		Assert.assertEquals(1, select.getComboBox().getContainerDataSource().size());
	}
}
