/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.Pageable;
import com.ocs.dynamo.dao.PageableImpl;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrder.Direction;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.exception.OCSNonUniqueException;
import com.ocs.dynamo.exception.OCSValidationException;
import com.ocs.dynamo.filter.Compare;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;

/**
 * A basic test class for both testing the general testing framework and the
 * BaseServiceImpl class
 * 
 * @author bas.rutten
 */
@ExtendWith(SpringExtension.class)
public class BaseServiceImplTest extends BaseMockitoTest {

    private class Dependency {

        public void noop() {
            // do nothing
        }
    }

    private class TestService extends BaseServiceImpl<Integer, TestEntity> {

        @Autowired
        private Dependency dependency;

        @Override
        protected TestEntity findIdenticalEntity(TestEntity entity) {
            return dao.findByUniqueProperty("name", entity.getName(), true);
        }

        @Override
        protected BaseDao<Integer, TestEntity> getDao() {
            return dao;
        }

        public void noop() {
            dependency.noop();
        }
    }

    private static final int ID = 1;

    @Mock
    private BaseDao<Integer, TestEntity> dao;

    @Mock
    private Dependency dependency;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private TestService service = new TestService();

    @Mock
    private Validator validator;

    @Spy
    private ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    @BeforeEach
    public void setupBaseServiceImplTest() throws NoSuchFieldException {
        when(dao.getEntityClass()).thenReturn(TestEntity.class);
    }

    @Test
    public void testCount() {
        service.count();
        verify(dao).count();
    }

    @Test
    public void testCountFilter() {
        Filter filter = new Compare.Equal("property1", 1);
        service.count(filter, false);
        verify(dao).count(filter, false);

        service.count(filter, true);
        verify(dao).count(filter, true);
    }

    @Test
    public void testCreateNewEntity() {
        TestEntity entity = service.createNewEntity();
        assertNotNull(entity);
    }

    /**
     * Test that a pageable object is properly created
     */
    @Test
    public void testCreatePageable() {
        Filter filter = new Compare.Equal("name", "Piet");

        SortOrder order = new SortOrder("name", Direction.ASC);
        SortOrder order2 = new SortOrder("age", Direction.DESC);
        service.fetch(filter, 2, 10, new SortOrders(order, order2));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(dao).fetch(any(Filter.class), captor.capture());

        Pageable p = captor.getValue();

        Direction dir = p.getSortOrders().getOrderFor("name").getDirection();
        assertEquals(Direction.ASC, dir);

        Direction dir2 = p.getSortOrders().getOrderFor("age").getDirection();
        assertEquals(Direction.DESC, dir2);

        assertEquals(2, p.getPageNumber());
        assertEquals(20, p.getOffset());
        assertEquals(10, p.getPageSize());
    }

    /**
     * Check that sort objects are properly created
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testCreateSort() {
        List<Integer> ids = new ArrayList<>();
        ids.add(ID);

        SortOrder order = new SortOrder("name", Direction.ASC);
        SortOrder order2 = new SortOrder("age", Direction.DESC);

        service.fetchByIds(ids, new SortOrders(order, order2), new FetchJoinInformation("test"));

        ArgumentCaptor<SortOrders> captor = ArgumentCaptor.forClass(SortOrders.class);
        verify(dao).fetchByIds(any(List.class), captor.capture(), any());

        SortOrders s = captor.getValue();
        assertNotNull(s);

        Direction dir = s.getOrderFor("name").getDirection();
        assertEquals(Direction.ASC, dir);

        dir = s.getOrderFor("age").getDirection();
        assertEquals(Direction.DESC, dir);

        assertNull(s.getOrderFor("notExists"));
    }

    @Test
    public void testDelete() {
        TestEntity obj = new TestEntity();
        service.delete(obj);
        verify(dao).delete(obj);
    }

    @Test
    public void testDeleteList() {
        TestEntity obj = new TestEntity();
        TestEntity obj2 = new TestEntity();

        service.delete(Lists.newArrayList(obj, obj2));
        verify(dao).delete(Lists.newArrayList(obj, obj2));
    }

    @Test
    public void testFetchById() {

        service.fetchById(1);
        verify(dao).fetchById(1);

        service.fetchById(1, new FetchJoinInformation("property1"));
        verify(dao).fetchById(1, new FetchJoinInformation("property1"));
    }

    @Test
    public void testFetchByIds() {

        service.fetchByIds(Lists.newArrayList(1, 2));
        verify(dao).fetchByIds(Lists.newArrayList(1, 2), null);

        service.fetchByIds(Lists.newArrayList(1, 2), new FetchJoinInformation("property1"));
        verify(dao).fetchByIds(Lists.newArrayList(1, 2), null, new FetchJoinInformation("property1"));
    }

    @Test
    public void testFetchByUniqueProperty() {

        service.fetchByUniqueProperty("property1", "test", true);
        verify(dao).fetchByUniqueProperty("property1", "test", true);

        service.fetchByUniqueProperty("property1", "test", false);
        verify(dao).fetchByUniqueProperty("property1", "test", false);
    }

    @Test
    public void testFetchFilter() {
        Filter filter = new Compare.Equal("property1", 1);
        service.fetch(filter, new FetchJoinInformation("testEntities"));

        verify(dao).fetch(filter, new FetchJoinInformation("testEntities"));

        service.fetch(filter);
        verify(dao).fetch(filter);
    }

    @Test
    public void testFetchFilterPageable() {
        Filter filter = new Compare.Equal("property1", 1);
        service.fetch(filter, 2, 10, new FetchJoinInformation("testEntities"));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(dao).fetch(eq(filter), captor.capture(), eq(new FetchJoinInformation("testEntities")));

        Pageable pb = captor.getValue();
        assertEquals(20, pb.getOffset());
        assertEquals(10, pb.getPageSize());
        assertEquals(2, pb.getPageNumber());
    }

    @Test
    public void testFetchSortOrderJoins() {
        Filter filter = new Compare.Equal("property1", 1);
        SortOrders orders = new SortOrders(new SortOrder("property2"));
        FetchJoinInformation[] joins = new FetchJoinInformation[] { new FetchJoinInformation("testEntities") };

        service.fetch(filter, orders, joins);
        verify(dao).fetch(filter, orders, joins);
    }

    @Test
    public void testFilter() {
        Filter filter = new Compare.Equal("property1", 1);
        service.find(filter);
        verify(dao).find(filter);
    }

    @Test
    public void testFindAll() {
        service.findAll();
        verify(dao).findAll();

        service.findAll(new SortOrder("property1"));
        verify(dao).findAll(new SortOrder("property1", Direction.ASC));

        service.findAll(new SortOrder("property1", Direction.DESC));
        verify(dao).findAll(new SortOrder("property1", Direction.DESC));
    }

    @Test
    public void testFindById() {

        TestEntity obj = new TestEntity();
        when(dao.findById(ID)).thenReturn(obj);

        TestEntity result = service.findById(ID);
        assertNotNull(result);
    }

    @Test
    public void testFindDistinct() {
        Filter filter = new Compare.Equal("property1", 1);
        SortOrder so = new SortOrder("property2");

        service.findDistinct(filter, "property1", String.class, so);
        verify(dao).findDistinct(filter, "property1", String.class, so);
    }

    @Test
    public void testFindIds() {
        service.findIds(new Compare.Equal("property1", 12), new SortOrder("property1"));
        verify(dao).findIds(new Compare.Equal("property1", 12), new SortOrder("property1", Direction.ASC));
    }

    @Test
    public void testFindSelect() {
        Filter filter = new Compare.Equal("property1", 1);
        SortOrders so = new SortOrders(new SortOrder("property1"));
        service.findSelect(filter, new String[] { "property1", "property2" }, so);
        verify(dao).findSelect(filter, new String[] { "property1", "property2" }, so);
    }

    @Test
    public void testFindSelect2() {
        Filter filter = new Compare.Equal("property1", 1);
        SortOrders so = new SortOrders(new SortOrder("property1"));
        service.findSelect(filter, new String[] { "property1", "property2" }, 1, 10, so);
        verify(dao).findSelect(eq(filter), eq(new String[] { "property1", "property2" }), any(PageableImpl.class));
    }

    /**
     * tests that a custom method is correctly delegated
     */
    @Test
    public void testNoop() {
        service.noop();
        verify(dependency).noop();
    }

    @Test
    public void testSave() {
        TestEntity obj = new TestEntity("name1", 14L);
        MockUtil.mockSave(dao, TestEntity.class);

        TestEntity result = service.save(obj);
        assertNotNull(result);
        verify(dao).save(obj);
    }

    @Test
    public void testSaveList() {
        TestEntity obj1 = new TestEntity("name1", 14L);
        TestEntity obj2 = new TestEntity("name2", 15L);

        service.save(Lists.newArrayList(obj1, obj2));
        verify(dao).save(Lists.newArrayList(obj1, obj2));
    }

    @Test
    public void testValidate() {
        TestEntity entity = new TestEntity("name1", 15L);
        service.validate(entity);
    }

    @Test
    public void testValidate_AssertTrue() {
        TestEntity entity = new TestEntity("bogus", 15L);
        assertThrows(OCSValidationException.class, () -> service.validate(entity));
    }

    @Test
    public void testValidate_Error() {
        TestEntity entity = new TestEntity(null, 15L);
        assertThrows(OCSValidationException.class, () -> service.validate(entity));
    }

    /**
     * That that a OCSNonUniqueException is thrown in case the validation process
     * results in a duplicate
     */
    @Test
    public void testValidate_Identical() {
        TestEntity entity = new TestEntity("kevin", 15L);

        TestEntity other = new TestEntity();
        other.setId(4);
        when(dao.findByUniqueProperty("name", "kevin", true)).thenReturn(other);

        assertThrows(OCSNonUniqueException.class, () -> service.validate(entity));
    };
}
