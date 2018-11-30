package com.ocs.dynamo.ui.component;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
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
import com.vaadin.data.provider.ListDataProvider;

public class SimpleTokenFieldSelectTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private TestEntityService service;

	@Test
	public void testCreate() {

		List<String> items = Lists.newArrayList("Kevin", "Stuart", "Bob");
		Mockito.when(service.findDistinct(Mockito.isNull(), Mockito.eq("name"), Mockito.eq(String.class)))
				.thenReturn(items);

		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("name");

		SimpleTokenFieldSelect<Integer, TestEntity, String> select = new SimpleTokenFieldSelect<>(service, em, am, null,
				"name", String.class, false);
		select.initContent();

		// select an item and verify it is added as a token
		select.getComboBox().setValue("Kevin");
		Assert.assertEquals(1, select.getTokenField().getValue().size());
		Tokenizable t = select.getTokenField().getValue().get(0);
		Assert.assertEquals("Kevin", t.getStringValue());
		Assert.assertEquals(Lists.newArrayList("Kevin"), select.getValue());

		// remove the value again
		select.getTokenField().removeTokenizable(t);
		Assert.assertTrue(select.getValue().isEmpty());
	}

	@Test
	public void testCreateAndOrder() {

		List<String> items = Lists.newArrayList("Kevin", "Stuart", "Bob");
		Mockito.when(service.findDistinct(Mockito.isNull(), Mockito.eq("name"), Mockito.eq(String.class)))
				.thenReturn(items);

		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("name");
		SimpleTokenFieldSelect<Integer, TestEntity, String> select = new SimpleTokenFieldSelect<>(service, em, am, null,
				"name", String.class, false);
		select.initContent();

		// select two values and verify that they are added as tokens and removed from
		// the combo box
		select.getComboBox().setValue("Kevin");
		select.getComboBox().setValue("Bob");
		Assert.assertEquals(2, select.getTokenField().getValue().size());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testElementCollection() {

		List<String> items = Lists.newArrayList("Kevin", "Stuart", "Bob");
		Mockito.when(
				service.findDistinctInCollectionTable(Mockito.isNull(), Mockito.isNull(), Mockito.eq(String.class)))
				.thenReturn(items);

		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("name");
		SimpleTokenFieldSelect<Integer, TestEntity, String> select = new SimpleTokenFieldSelect<>(service, em, am, null,
				"name", String.class, true);
		select.initContent();

		// select two values and verify that they are added as tokens and removed from
		// the combo box
		select.getComboBox().setValue("Kevin");
		select.getComboBox().setValue("Bob");
		Assert.assertEquals(2, select.getTokenField().getValue().size());

		ListDataProvider<String> provider = (ListDataProvider<String>) select.getComboBox().getDataProvider();
		Assert.assertEquals(3, provider.getItems().size());
	}
}
