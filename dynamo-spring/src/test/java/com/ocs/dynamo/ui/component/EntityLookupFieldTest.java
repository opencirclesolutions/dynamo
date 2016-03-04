package com.ocs.dynamo.ui.component;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Component;

import junitx.util.PrivateAccessor;

public class EntityLookupFieldTest extends BaseMockitoTest {

	@Mock
	private TestEntityService service;

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		PrivateAccessor.setField(factory, "defaultPrecision", 2);
	}

	@Test
	public void test() {
		EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service,
		        factory.getModel(TestEntity.class), null, null,
		        new SortOrder("name", SortDirection.ASCENDING));
		field.initContent();

		Assert.assertEquals(new SortOrder("name", SortDirection.ASCENDING), field.getSortOrder());
		Assert.assertEquals(TestEntity.class, field.getType());

		Component comp = field.iterator().next();
		Assert.assertTrue(comp instanceof DefaultHorizontalLayout);

		try {
			field.getSelectButton().click();
		} catch (com.vaadin.event.ListenerMethod.MethodException ex) {
			// expected since there is no UI
			Assert.assertTrue(ex.getCause() instanceof NullPointerException);
		}

		field.setEnabled(false);
		Assert.assertFalse(field.getSelectButton().isEnabled());
		Assert.assertFalse(field.getClearButton().isEnabled());
	}

	@Test
	public void testPageLength() {
		EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service,
		        factory.getModel(TestEntity.class), null, null,
		        new SortOrder("name", SortDirection.ASCENDING));
		field.setPageLength(10);
		field.initContent();

		Assert.assertEquals(new SortOrder("name", SortDirection.ASCENDING), field.getSortOrder());
		Assert.assertEquals(TestEntity.class, field.getType());

		Component comp = field.iterator().next();
		Assert.assertTrue(comp instanceof DefaultHorizontalLayout);

		try {
			field.getSelectButton().click();
		} catch (com.vaadin.event.ListenerMethod.MethodException ex) {
			// expected since there is no UI
			Assert.assertTrue(ex.getCause() instanceof NullPointerException);
		}

	}
}
