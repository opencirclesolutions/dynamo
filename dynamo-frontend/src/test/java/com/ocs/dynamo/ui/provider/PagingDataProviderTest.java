package com.ocs.dynamo.ui.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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

public class PagingDataProviderTest extends BaseMockitoTest {

    private PagingDataProvider<Integer, TestEntity> provider;

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
        when(service.count(nullable(Filter.class), eq(false))).thenReturn(3L);

        provider = new PagingDataProvider<>(service, emf.getModel(TestEntity.class), false);
        provider.size(query);
        assertEquals(3, provider.getSize());

        provider.fetch(query);
        verify(service).fetch(isNull(), eq(0), eq(5), any(SortOrders.class), any());
    }

    /**
     * Test that the max results setting is respected
     */
    @Test
    public void testSizeWithoutFilterMaxResults() {
        when(service.count(nullable(Filter.class), eq(false))).thenReturn(5L);

        provider = new PagingDataProvider<>(service, emf.getModel(TestEntity.class), false);
        provider.setMaxResults(2);

        provider.size(query);
        assertEquals(2, provider.getSize());

        // check that the number of results is limited to two
        provider.fetch(query);
        verify(service).fetch(isNull(), eq(0), eq(2), any(SortOrders.class), any());
    }

    @Test
    public void testSizeWithoutFilterOnlyReturnPart() {
        when(service.count(nullable(Filter.class), eq(false))).thenReturn(6L);
        provider = new PagingDataProvider<>(service, emf.getModel(TestEntity.class), false);
        provider.size(query);
        assertEquals(6, provider.getSize());

        // only fetch the first 5 items
        provider.fetch(query);
        verify(service).fetch(isNull(), eq(0), eq(5), any(SortOrders.class), any());

    }

    @Test
    public void testSizeWithFilter() {
        when(query.getFilter()).thenReturn(Optional.ofNullable(new EqualsPredicate<>("name", "Bob")));
        when(service.count(any(Filter.class), eq(false))).thenReturn(3L);

        provider = new PagingDataProvider<>(service, emf.getModel(TestEntity.class), false);
        provider.size(query);
        assertEquals(3, provider.getSize());

        verify(service).count(new Compare.Equal("name", "Bob"), false);

        provider.fetch(query);
        verify(service).fetch(eq(new Compare.Equal("name", "Bob")), eq(0), eq(5),
                any(SortOrders.class), any());

    }

    @Test
    public void testSizeWithFilterAndSortOrder() {
        when(query.getFilter()).thenReturn(Optional.ofNullable(new EqualsPredicate<>("name", "Bob")));
        when(query.getSortOrders()).thenReturn(List.of(new QuerySortOrder("name", SortDirection.DESCENDING)));
        when(service.count(any(Filter.class), eq(false))).thenReturn(3L);

        provider = new PagingDataProvider<>(service, emf.getModel(TestEntity.class), false);
        provider.size(query);
        assertEquals(3, provider.getSize());

        verify(service).count(new Compare.Equal("name", "Bob"), false);

        provider.fetch(query);
        ArgumentCaptor<SortOrders> captor = ArgumentCaptor.forClass(SortOrders.class);
        verify(service).fetch(eq(new Compare.Equal("name", "Bob")), eq(0), eq(5), captor.capture(),
                any());

        SortOrders so = captor.getValue();
        assertNotNull(so.getOrderFor("name"));
    }

}
