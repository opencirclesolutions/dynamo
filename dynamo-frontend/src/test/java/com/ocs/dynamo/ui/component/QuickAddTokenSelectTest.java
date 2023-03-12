package com.ocs.dynamo.ui.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.SelectMode;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.ocs.dynamo.ui.UIHelper;

public class QuickAddTokenSelectTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private UIHelper ui;

	@Mock
	private TestEntityService service;

	private TestEntity t1;

	private TestEntity t2;

	private TestEntity t3;

	@BeforeEach
	public void setUp() {
		t1 = new TestEntity(1, "Kevin", 12L);
		t2 = new TestEntity(2, "Bob", 13L);
		t3 = new TestEntity(3, "Stewart", 14L);

		when(service.find(isNull(),  any())).thenReturn(List.of(t1, t2, t3));
		when(service.find(isNull())).thenReturn(List.of(t1, t2, t3));
		when(service.findAll(isNull())).thenReturn(List.of(t1, t2, t3));

		Filter f = new com.ocs.dynamo.filter.Compare.Equal("name", "Kevin");
		when(service.find(eq(f))).thenReturn(List.of(t1));

		when(service.fetch(isNull(), any(Integer.class), any(Integer.class), any(SortOrders.class)))
				.thenReturn(List.of(t1, t2, t3));
		when(service.fetch(eq(f), any(Integer.class), any(Integer.class), any(SortOrders.class)))
				.thenReturn(List.of(t1));

		when(service.createNewEntity()).thenReturn(new TestEntity());
		MockUtil.mockServiceSave(service, TestEntity.class);

	}

	/**
	 * Test the creation of the component and a simple selection
	 */
	@Test
	public void testCreateAndSelect() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("testDomain");

		QuickAddTokenSelect<Integer, TestEntity> select = new QuickAddTokenSelect<>(em, am, service, SelectMode.ALL,
				null, null, false);
		select.initContent();

		// test propagation of the value
		select.getTokenSelect().select(t1);
		assertTrue(select.getTokenSelect().getValue().contains(t1));

		// .. and the other way around
		select.getTokenSelect().select(t2);
		assertTrue(select.getTokenSelect().getValue().contains(t2));
	}

	@Test
	public void testSetValue() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("testDomain");

		QuickAddTokenSelect<Integer, TestEntity> select = new QuickAddTokenSelect<>(em, am, service, SelectMode.ALL,
				null, null, false);
		select.initContent();

		ComponentTestUtil.query(select.getTokenSelect().getDataProvider());

		assertEquals(3, select.getTokenSelect().getDataProviderSize());

		select.setValue(null);
		assertEquals(Collections.emptySet(), select.getValue());

	}

	@Test
	public void testAdditionalFilter() {

		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("testDomain");

		QuickAddTokenSelect<Integer, TestEntity> select = new QuickAddTokenSelect<>(em, am, service,
				SelectMode.FILTERED_PAGED, null, null, false);
		select.initContent();

		ComponentTestUtil.query(select.getTokenSelect().getDataProvider());
		assertEquals(3, select.getTokenSelect().getDataProviderSize());

		select.setAdditionalFilter(new EqualsPredicate<TestEntity>("name", "Kevin"));
		ComponentTestUtil.query(select.getTokenSelect().getDataProvider());

		assertEquals(1, select.getTokenSelect().getDataProviderSize());
	}
}
