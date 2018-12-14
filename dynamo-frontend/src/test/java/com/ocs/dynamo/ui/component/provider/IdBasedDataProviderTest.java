package com.ocs.dynamo.ui.component.provider;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.filter.Compare;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.provider.IdBasedDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.data.sort.SortDirection;

public class IdBasedDataProviderTest extends BaseMockitoTest {

	private IdBasedDataProvider<Integer, TestEntity> provider;

	@Mock
	private TestEntityService service;

	@Mock
	private Query<TestEntity, SerializablePredicate<TestEntity>> query;

	private EntityModelFactory emf = new EntityModelFactoryImpl();

	@Override
	public void setUp() {
		super.setUp();
		Mockito.when(query.getLimit()).thenReturn(5);
	}

	@Test
	public void testSizeWithoutFilter() {
		Mockito.when(service.findIds(Mockito.nullable(Filter.class), Mockito.isNull(), Mockito.any()))
				.thenReturn(Lists.newArrayList(1, 2, 3));
		provider = new IdBasedDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.size(query);
		Assert.assertEquals(3, provider.getSize());

		provider.fetch(query);
		Mockito.verify(service).fetchByIds(Mockito.eq(Lists.newArrayList(1, 2, 3)), Mockito.nullable(SortOrders.class),
				Mockito.any());
	}

	/**
	 * Test that the max results setting is respected
	 */
	@Test
	public void testSizeWithoutFilterMaxResults() {
		// modified "find by"
		Mockito.when(service.findIds(Mockito.nullable(Filter.class), Mockito.eq(2), Mockito.any()))
				.thenReturn(Lists.newArrayList(1, 2));
		provider = new IdBasedDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.setMaxResults(2);

		provider.size(query);
		Assert.assertEquals(2, provider.getSize());

		provider.fetch(query);
		Mockito.verify(service).fetchByIds(Mockito.eq(Lists.newArrayList(1, 2)), Mockito.nullable(SortOrders.class),
				Mockito.any());
	}

	@Test
	public void testSizeWithoutOnlyReturnPart() {
		Mockito.when(service.findIds(Mockito.nullable(Filter.class), Mockito.isNull(), Mockito.any()))
				.thenReturn(Lists.newArrayList(1, 2, 3, 4, 5, 6));
		provider = new IdBasedDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.size(query);
		Assert.assertEquals(6, provider.getSize());

		// only fetch the first 5 items
		provider.fetch(query);
		Mockito.verify(service).fetchByIds(Mockito.eq(Lists.newArrayList(1, 2, 3, 4, 5)),
				Mockito.nullable(SortOrders.class), Mockito.any());

	}

	@Test
	public void testSizeWithFilter() {
		Mockito.when(query.getFilter()).thenReturn(Optional.ofNullable(new EqualsPredicate<>("name", "Bob")));
		Mockito.when(service.findIds(Mockito.eq(new Compare.Equal("name", "Bob")), Mockito.isNull(), Mockito.any()))
				.thenReturn(Lists.newArrayList(1, 2, 3));
		provider = new IdBasedDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.size(query);
		Assert.assertEquals(3, provider.getSize());
	}

	@Test
	public void testSizeWithFilterAndSortOrder() {
		Mockito.when(query.getFilter()).thenReturn(Optional.ofNullable(new EqualsPredicate<>("name", "Bob")));
		Mockito.when(query.getSortOrders())
				.thenReturn(Lists.newArrayList(new QuerySortOrder("name", SortDirection.DESCENDING)));

		Mockito.when(service.findIds(Mockito.eq(new Compare.Equal("name", "Bob")), Mockito.isNull(), Mockito.any()))
				.thenReturn(Lists.newArrayList(1, 2, 3));
		provider = new IdBasedDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.size(query);
		Assert.assertEquals(3, provider.getSize());

		provider.fetch(query);
		ArgumentCaptor<SortOrders> captor = ArgumentCaptor.forClass(SortOrders.class);
		Mockito.verify(service).fetchByIds(Mockito.eq(Lists.newArrayList(1, 2, 3)), captor.capture(), Mockito.any());

		SortOrders so = captor.getValue();
		Assert.assertNotNull(so.getOrderFor("name"));
	}

}
