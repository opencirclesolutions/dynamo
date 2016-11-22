package com.ocs.dynamo.ui.component;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.UI;

public class SimpleTokenFieldSelectTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private UI ui;

	@Mock
	private TestEntityService service;

	private TestEntity t1;

	private TestEntity t2;

	private TestEntity t3;

	@Before
	public void setUp() throws Exception {
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
	public void testCreate() {

		List<String> items = Lists.newArrayList("Kevin", "Stuart", "Bob");

		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("name");
		com.vaadin.data.sort.SortOrder order = new com.vaadin.data.sort.SortOrder("name", SortDirection.ASCENDING);
		SimpleTokenFieldSelect<String> select = new SimpleTokenFieldSelect<String>(am, items, String.class, order);
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

		List<String> items = Lists.newArrayList("Stuart", "Kevin", "Bob");

		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("name");
		com.vaadin.data.sort.SortOrder order = new com.vaadin.data.sort.SortOrder("name", SortDirection.ASCENDING);
		SimpleTokenFieldSelect<String> select = new SimpleTokenFieldSelect<String>(am, items, String.class, order);
		select.initContent();

		// select two values and verify that they are added as tokens and removed from the combo box
		select.getComboBox().setValue("Kevin");
		select.getComboBox().setValue("Bob");
		Assert.assertEquals(2, select.getTokenField().getValue().size());
		Assert.assertEquals(1, select.getComboBox().getContainerDataSource().size());
	}

}
