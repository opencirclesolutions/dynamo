package com.ocs.dynamo.ui.provider;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
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
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.data.sort.SortDirection;

public class PagingDataProviderTest extends BaseMockitoTest {

	private PagingDataProvider<Integer, TestEntity> provider;

	@Mock
	private TestEntityService service;

	@Mock
	private Query<TestEntity, SerializablePredicate<TestEntity>> query;

	private EntityModelFactory emf = new EntityModelFactoryImpl();

	@Before
	public void setUp() {
		Mockito.when(query.getLimit()).thenReturn(5);
	}

	@Test
	public void testSizeWithoutFilter() {
		Mockito.when(service.count(Mockito.nullable(Filter.class), Mockito.eq(false))).thenReturn(3L);

		provider = new PagingDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.size(query);
		Assert.assertEquals(3, provider.getSize());

		provider.fetch(query);
		Mockito.verify(service).fetch(Mockito.isNull(), Mockito.eq(0), Mockito.eq(5), Mockito.any(SortOrders.class),
				Mockito.any());
	}

	/**
	 * Test that the max results setting is respected
	 */
	@Test
	public void testSizeWithoutFilterMaxResults() {
		Mockito.when(service.count(Mockito.nullable(Filter.class), Mockito.eq(false))).thenReturn(5L);

		provider = new PagingDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.setMaxResults(2);

		provider.size(query);
		Assert.assertEquals(2, provider.getSize());

		// check that the number of results is limited to two
		provider.fetch(query);
		Mockito.verify(service).fetch(Mockito.isNull(), Mockito.eq(0), Mockito.eq(2), Mockito.any(SortOrders.class),
				Mockito.any());
	}

	@Test
	public void testSizeWithoutFilterOnlyReturnPart() {
		Mockito.when(service.count(Mockito.nullable(Filter.class), Mockito.eq(false))).thenReturn(6L);
		provider = new PagingDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.size(query);
		Assert.assertEquals(6, provider.getSize());

		// only fetch the first 5 items
		provider.fetch(query);
		Mockito.verify(service).fetch(Mockito.isNull(), Mockito.eq(0), Mockito.eq(5), Mockito.any(SortOrders.class),
				Mockito.any());

	}

	@Test
	public void testSizeWithFilter() {
		Mockito.when(query.getFilter()).thenReturn(Optional.ofNullable(new EqualsPredicate<>("name", "Bob")));
		Mockito.when(service.count(Mockito.any(Filter.class), Mockito.eq(false))).thenReturn(3L);

		provider = new PagingDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.size(query);
		Assert.assertEquals(3, provider.getSize());

		Mockito.verify(service).count(new Compare.Equal("name", "Bob"), false);

		provider.fetch(query);
		Mockito.verify(service).fetch(Mockito.eq(new Compare.Equal("name", "Bob")), Mockito.eq(0), Mockito.eq(5),
				Mockito.any(SortOrders.class), Mockito.any());

	}

	@Test
	public void testSizeWithFilterAndSortOrder() {
		Mockito.when(query.getFilter()).thenReturn(Optional.ofNullable(new EqualsPredicate<>("name", "Bob")));
		Mockito.when(query.getSortOrders())
				.thenReturn(Lists.newArrayList(new QuerySortOrder("name", SortDirection.DESCENDING)));
		Mockito.when(service.count(Mockito.any(Filter.class), Mockito.eq(false))).thenReturn(3L);

		provider = new PagingDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.size(query);
		Assert.assertEquals(3, provider.getSize());

		Mockito.verify(service).count(new Compare.Equal("name", "Bob"), false);

		provider.fetch(query);
		ArgumentCaptor<SortOrders> captor = ArgumentCaptor.forClass(SortOrders.class);
		Mockito.verify(service).fetch(Mockito.eq(new Compare.Equal("name", "Bob")), Mockito.eq(0), Mockito.eq(5),
				captor.capture(), Mockito.any());

		SortOrders so = captor.getValue();
		Assert.assertNotNull(so.getOrderFor("name"));
	}

}
