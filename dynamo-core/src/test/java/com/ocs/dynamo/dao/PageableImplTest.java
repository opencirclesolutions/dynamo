package com.ocs.dynamo.dao;

import org.junit.Assert;
import org.junit.Test;

public class PageableImplTest {

	@Test
	public void testCreate() {
		PageableImpl p = new PageableImpl(0, 10);

		Assert.assertEquals(0, p.getPageNumber());
		Assert.assertEquals(0, p.getOffset());
		Assert.assertEquals(0, p.getSortOrders().toArray().length);

		p = new PageableImpl(2, 10);
		Assert.assertEquals(20, p.getOffset());
		Assert.assertEquals(2, p.getPageNumber());

		p = new PageableImpl(2, 10, new SortOrder("test1"));
		Assert.assertEquals(20, p.getOffset());
		Assert.assertEquals(1, p.getSortOrders().toArray().length);

		p = new PageableImpl(2, 10, new SortOrders(new SortOrder("test1"), new SortOrder("test2")));
		Assert.assertEquals(20, p.getOffset());
		Assert.assertEquals(2, p.getSortOrders().toArray().length);
		
		p = new PageableImpl(2, 10, new SortOrder("test1"), new SortOrder("test2"), new SortOrder("test3"));
		Assert.assertEquals(20, p.getOffset());
		Assert.assertEquals(3, p.getSortOrders().toArray().length);
	}
}
