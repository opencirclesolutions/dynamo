package com.ocs.dynamo.ui.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity.TestEnum;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
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

    private EntityModelFactory emf = new EntityModelFactoryImpl();

    private PivotDataProvider<Integer, TestEntity> pivotProvider;

    @Before
    public void setUp() {
        Mockito.when(query.getOffset()).thenReturn(0);
        Mockito.when(query.getLimit()).thenReturn(5);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSizeWithoutFilter() {

        pivotProvider = new PivotDataProvider<>(provider, "name", "someEnum", Lists.newArrayList("name"), Lists.newArrayList("age"),
                () -> 10);
        pivotProvider.size(query);
        assertEquals(10, pivotProvider.getSize());

        Mockito.verify(provider).size(Mockito.any(Query.class));
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSizeWithFilter() {

        Mockito.when(query.getFilter()).thenReturn(Optional.of(new EqualsPredicate<PivotedItem>("name", "Bob")));

        pivotProvider = new PivotDataProvider<>(provider, "name", "someEnum", Lists.newArrayList("name"), Lists.newArrayList("age"),
                () -> 5);
        pivotProvider.size(query);
        assertEquals(5, pivotProvider.getSize());

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        Mockito.verify(provider).size(captor.capture());
        assertTrue(captor.getValue().getFilter().isPresent());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFetch() {
        pivotProvider = new PivotDataProvider<>(provider, "name", "someEnum", Lists.newArrayList("name"), Lists.newArrayList("age"),
                () -> 1);

        TestEntity t1 = new TestEntity();
        t1.setName("Bob");
        t1.setAge(44L);
        t1.setSomeEnum(TestEnum.A);

        TestEntity t2 = new TestEntity();
        t2.setName("Bob");
        t2.setAge(45L);
        t2.setSomeEnum(TestEnum.B);

        Mockito.when(query.getOffset()).thenReturn(0);
        Mockito.when(query.getLimit()).thenReturn(1);

        Mockito.when(provider.fetch(Mockito.any(Query.class))).thenReturn(Stream.of(t1, t2), Stream.empty());
        Stream<PivotedItem> fetch = pivotProvider.fetch(query);

        ArgumentCaptor<Query<TestEntity, SerializablePredicate<TestEntity>>> captor = ArgumentCaptor.forClass(Query.class);
        Mockito.verify(provider, Mockito.times(2)).fetch(captor.capture());

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
