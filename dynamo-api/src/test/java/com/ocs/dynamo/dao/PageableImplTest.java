package com.ocs.dynamo.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


public class PageableImplTest {

    @Test
    public void testCreate() {
        PageableImpl p = new PageableImpl(0, 10);

        assertEquals(0, p.getPageNumber());
        assertEquals(0, p.getOffset());
        assertEquals(0, p.getSortOrders().toArray().length);

        p = new PageableImpl(2, 10);
        assertEquals(20, p.getOffset());
        assertEquals(2, p.getPageNumber());

        p = new PageableImpl(2, 10, new SortOrder("test1"));
        assertEquals(20, p.getOffset());
        assertEquals(1, p.getSortOrders().toArray().length);

        p = new PageableImpl(2, 10, new SortOrders(new SortOrder("test1"), new SortOrder("test2")));
        assertEquals(20, p.getOffset());
        assertEquals(2, p.getSortOrders().toArray().length);

        p = new PageableImpl(2, 10, new SortOrder("test1"), new SortOrder("test2"), new SortOrder("test3"));
        assertEquals(20, p.getOffset());
        assertEquals(3, p.getSortOrders().toArray().length);
    }
}
