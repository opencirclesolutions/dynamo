package com.ocs.dynamo.ui.component;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.vaadin.data.util.filter.Compare;

import junitx.util.PrivateAccessor;

public class EntityListSelectTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private TestEntityService service;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		PrivateAccessor.setField(factory, "defaultPrecision", 2);
	}

	@Test
	public void testAll() {

		EntityListSelect<Integer, TestEntity> select = new EntityListSelect<>(
		        factory.getModel(TestEntity.class), null, service);
		Assert.assertEquals(EntityListSelect.SelectMode.ALL, select.getSelectMode());

		Mockito.verify(service).findAll((SortOrder[]) null);
	}

	@Test
	public void testFixed() {

		EntityListSelect<Integer, TestEntity> select = new EntityListSelect<>(
		        factory.getModel(TestEntity.class), null, Lists.newArrayList(new TestEntity()));
		Assert.assertEquals(EntityListSelect.SelectMode.FIXED, select.getSelectMode());

		Mockito.verifyZeroInteractions(service);
	}

	@Test
	public void testFilter() {

		EntityListSelect<Integer, TestEntity> select = new EntityListSelect<>(
		        factory.getModel(TestEntity.class), null, service,
		        new Compare.Equal("name", "Bob"));
		Assert.assertEquals(EntityListSelect.SelectMode.FILTERED, select.getSelectMode());

		Mockito.verify(service).find(Matchers.any(com.ocs.dynamo.filter.Filter.class),
		        Matchers.any(com.ocs.dynamo.dao.SortOrder[].class));
	}
}
