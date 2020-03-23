package com.ocs.dynamo.ui.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.filter.Compare;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.function.SerializablePredicate;

public class IdBasedDataProviderTest extends BaseMockitoTest {

	private IdBasedDataProvider<Integer, TestEntity> provider;

	@Mock
	private TestEntityService service;

	@Mock
	private Query<TestEntity, SerializablePredicate<TestEntity>> query;

	private EntityModelFactory emf = new EntityModelFactoryImpl();

	@BeforeEach
	public void setUp() {
		when(query.getLimit()).thenReturn(5);
	}

	@Test
	public void testSizeWithoutFilter() {
		when(service.findIds(nullable(Filter.class), isNull(), any()))
				.thenReturn(List.of(1, 2, 3));
		provider = new IdBasedDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.size(query);
		assertEquals(3, provider.getSize());

		provider.fetch(query);
		verify(service).fetchByIds(eq(List.of(1, 2, 3)), nullable(SortOrders.class),
				any());
	}

	/**
	 * Test that the max results setting is respected
	 */
	@Test
	public void testSizeWithoutFilterMaxResults() {
		when(service.findIds(nullable(Filter.class), anyInt(), any()))
				.thenReturn(List.of(1, 2));
		when(service.count(nullable(Filter.class), eq(false))).thenReturn(5L);

		provider = new IdBasedDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.setMaxResults(2);

		provider.size(query);
		assertEquals(2, provider.getSize());

		provider.fetch(query);
		verify(service).fetchByIds(eq(List.of(1, 2)), nullable(SortOrders.class),
				any());
	}

	@Test
	public void testSizeWithoutOnlyReturnPart() {
		when(service.findIds(nullable(Filter.class), isNull(), any()))
				.thenReturn(List.of(1, 2, 3, 4, 5, 6));
		provider = new IdBasedDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.size(query);
		assertEquals(6, provider.getSize());

		// only fetch the first 5 items
		provider.fetch(query);
		verify(service).fetchByIds(eq(List.of(1, 2, 3, 4, 5)),
				nullable(SortOrders.class), any());

	}

	@Test
	public void testSizeWithFilter() {
		when(query.getFilter()).thenReturn(Optional.ofNullable(new EqualsPredicate<>("name", "Bob")));
		when(service.findIds(eq(new Compare.Equal("name", "Bob")), isNull(), any()))
				.thenReturn(List.of(1, 2, 3));
		provider = new IdBasedDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.size(query);
		assertEquals(3, provider.getSize());
	}

	/**
	 * Test that if you perform a fetch before a size, the size query is executed
	 * anyway
	 */
	@Test
	public void testFetchWithFilter() {
		when(query.getFilter()).thenReturn(Optional.ofNullable(new EqualsPredicate<>("name", "Bob")));
		when(service.findIds(eq(new Compare.Equal("name", "Bob")), isNull(), any()))
				.thenReturn(List.of(1, 2, 3));
		provider = new IdBasedDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.fetch(query);
		assertEquals(3, provider.getSize());
	}

	@Test
	public void testSizeWithFilterAndSortOrder() {
		when(query.getFilter()).thenReturn(Optional.ofNullable(new EqualsPredicate<>("name", "Bob")));
		when(query.getSortOrders())
				.thenReturn(List.of(new QuerySortOrder("name", SortDirection.DESCENDING)));

		when(service.findIds(eq(new Compare.Equal("name", "Bob")), isNull(), any()))
				.thenReturn(List.of(1, 2, 3));
		provider = new IdBasedDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.size(query);
		assertEquals(3, provider.getSize());

		provider.fetch(query);
		ArgumentCaptor<SortOrders> captor = ArgumentCaptor.forClass(SortOrders.class);
		verify(service).fetchByIds(eq(List.of(1, 2, 3)), captor.capture(), any());

		SortOrders so = captor.getValue();
		assertNotNull(so.getOrderFor("name"));
	}

	@Test
	public void testNextItemId() {

		when(service.findIds(isNull(), isNull(), any()))
				.thenReturn(List.of(1, 2, 3));
		provider = new IdBasedDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.size(query);

		provider.setCurrentlySelectedId(1);
		assertTrue(provider.hasNextItemId());

		Integer next = provider.getNextItemId();
		assertEquals(2, next.intValue());
		assertTrue(provider.hasNextItemId());

		next = provider.getNextItemId();
		assertEquals(3, next.intValue());
		assertFalse(provider.hasNextItemId());
	}

	@Test
	public void testPreviousItemId() {

		when(service.findIds(isNull(), isNull(), any()))
				.thenReturn(List.of(1, 2, 3));
		provider = new IdBasedDataProvider<>(service, emf.getModel(TestEntity.class));
		provider.size(query);

		provider.setCurrentlySelectedId(2);
		assertTrue(provider.hasPreviousItemId());

		Integer next = provider.getPreviousItemId();
		assertEquals(1, next.intValue());
		assertFalse(provider.hasPreviousItemId());
	}

}
