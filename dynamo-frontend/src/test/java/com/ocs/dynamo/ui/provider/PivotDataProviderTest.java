package com.ocs.dynamo.ui.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity.TestEnum;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializablePredicate;

public class PivotDataProviderTest extends BaseMockitoTest {

    @Mock
    private IdBasedDataProvider<Integer, TestEntity> provider;

    @Mock
    private TestEntityService service;

    @Mock
    private Query<PivotedItem, SerializablePredicate<PivotedItem>> query;

    private PivotDataProvider<Integer, TestEntity> pivotProvider;

    @BeforeEach
    public void setUp() {
        when(query.getOffset()).thenReturn(0);
        when(query.getLimit()).thenReturn(5);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSizeWithoutFilter() {

        pivotProvider = new PivotDataProvider<>(provider, "name", "someEnum", List.of("name"), List.of("age"), () -> 10);
        pivotProvider.size(query);
        assertEquals(10, pivotProvider.getSize());

        verify(provider).size(any(Query.class));
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSizeWithFilter() {

        when(query.getFilter()).thenReturn(Optional.of(new EqualsPredicate<PivotedItem>("name", "Bob")));

        pivotProvider = new PivotDataProvider<>(provider, "name", "someEnum", List.of("name"), List.of("age"), () -> 5);
        pivotProvider.size(query);
        assertEquals(5, pivotProvider.getSize());

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(provider).size(captor.capture());
        assertTrue(captor.getValue().getFilter().isPresent());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFetch() {
        pivotProvider = new PivotDataProvider<>(provider, "name", "someEnum", List.of("name"), List.of("age"), () -> 1);

        TestEntity t1 = new TestEntity();
        t1.setName("Bob");
        t1.setAge(44L);
        t1.setSomeEnum(TestEnum.A);

        TestEntity t2 = new TestEntity();
        t2.setName("Bob");
        t2.setAge(45L);
        t2.setSomeEnum(TestEnum.B);

        when(query.getOffset()).thenReturn(0);
        when(query.getLimit()).thenReturn(1);

        when(provider.fetch(any(Query.class))).thenReturn(Stream.of(t1, t2), Stream.empty());
        Stream<PivotedItem> fetch = pivotProvider.fetch(query);

        ArgumentCaptor<Query<TestEntity, SerializablePredicate<TestEntity>>> captor = ArgumentCaptor.forClass(Query.class);
        verify(provider, times(2)).fetch(captor.capture());

        Query<TestEntity, SerializablePredicate<TestEntity>> value = captor.getAllValues().get(0);
        assertEquals(0, value.getOffset());
        assertEquals(1000, value.getLimit());

        value = captor.getAllValues().get(1);
        assertEquals(1000, value.getOffset());
        assertEquals(1000, value.getLimit());

        Optional<PivotedItem> findFirst = fetch.findFirst();
        assertTrue(findFirst.isPresent());

        PivotedItem pivotedItem = findFirst.get();
        assertEquals("Bob", pivotedItem.getRowKeyValue());
        assertEquals("Bob", pivotedItem.getFixedValue("name"));
        assertEquals(44L, pivotedItem.getValue(TestEnum.A, "age"));
        assertEquals(45L, pivotedItem.getValue(TestEnum.B, "age"));
    }

}
