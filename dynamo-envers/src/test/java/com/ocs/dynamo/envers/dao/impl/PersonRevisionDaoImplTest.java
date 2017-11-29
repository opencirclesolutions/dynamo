package com.ocs.dynamo.envers.dao.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;

import com.ocs.dynamo.dao.Pageable;
import com.ocs.dynamo.dao.PageableImpl;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrder.Direction;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.envers.dao.PersonDao;
import com.ocs.dynamo.envers.domain.Person;
import com.ocs.dynamo.envers.domain.PersonRevision;
import com.ocs.dynamo.envers.domain.RevisionKey;
import com.ocs.dynamo.envers.domain.RevisionType;
import com.ocs.dynamo.filter.And;
import com.ocs.dynamo.filter.Compare;
import com.ocs.dynamo.filter.Not;
import com.ocs.dynamo.filter.Or;
import com.ocs.dynamo.test.BaseIntegrationTest;

public class PersonRevisionDaoImplTest extends BaseIntegrationTest {

	@Autowired
	private PersonDao personDao;

	@Autowired
	private PersonRevisionDao personRevisionDao;

	private Person person;

	@Test
	public void testCreateUpdateDelete() {
		TransactionStatus status = startTransaction();

		person = new Person();
		person.setName("Bas");
		person = personDao.save(person);

		commitTransaction(status);

		long count = personRevisionDao.count(new Compare.Equal("id", person.getId()), true);
		Assert.assertEquals(1L, count);

		List<PersonRevision> list = personRevisionDao.fetch(new Compare.Equal("id", person.getId()), (Pageable) null);
		Assert.assertEquals(1, list.size());

		Assert.assertEquals(1, list.get(0).getRevision());
		Assert.assertEquals(person.getId(), list.get(0).getId().getId());
		Assert.assertEquals("Bas", list.get(0).getEntity().getName());
		Assert.assertEquals(RevisionType.ADD, list.get(0).getRevisionType());

		status = startTransaction();

		person.setName("Jeroen");
		person = personDao.save(person);

		commitTransaction(status);

		count = personRevisionDao.count(new Compare.Equal("id", person.getId()), true);
		Assert.assertEquals(2L, count);
		list = personRevisionDao.fetch(new Compare.Equal("id", person.getId()), (Pageable) null);
		Assert.assertEquals(2, list.size());

		Assert.assertEquals(2, list.get(1).getRevision());
		Assert.assertEquals(person.getId(), list.get(1).getId().getId());
		Assert.assertEquals("Jeroen", list.get(1).getEntity().getName());
		Assert.assertEquals(RevisionType.MOD, list.get(1).getRevisionType());

		status = startTransaction();

		personDao.delete(person);

		commitTransaction(status);

		list = personRevisionDao.fetch(new Compare.Equal("id", person.getId()), (Pageable) null);
		Assert.assertEquals(3, list.size());
		Assert.assertEquals(RevisionType.DEL, list.get(2).getRevisionType());

		// try with sorting
		Pageable p = new PageableImpl(0, 10, new SortOrders(new SortOrder(Direction.ASC, "name")));
		list = personRevisionDao.fetch(new Compare.Equal("id", person.getId()), p);
		Assert.assertEquals(3, list.size());

		// sort on revision type
		p = new PageableImpl(0, 10, new SortOrders(new SortOrder(Direction.ASC, "revisionType")));
		list = personRevisionDao.fetch(new Compare.Equal("id", person.getId()), p);
		Assert.assertEquals(3, list.size());

		// sort on revision property
		p = new PageableImpl(0, 10, new SortOrders(new SortOrder(Direction.ASC, "revision")));
		list = personRevisionDao.fetch(new Compare.Equal("id", person.getId()), p);
		Assert.assertEquals(3, list.size());

		// find by revision key
		RevisionKey<Integer> key = new RevisionKey<Integer>(person.getId(), 3);
		PersonRevision pr = personRevisionDao.fetchById(key);
		Assert.assertNotNull(pr);

		list = personRevisionDao.findRevisions(person.getId());
		Assert.assertEquals(3, list.size());

		// fetch non existing
		key = new RevisionKey<>(person.getId(), 4);
		pr = personRevisionDao.fetchById(key);
		Assert.assertNull(pr);

		// fetch non existing part 2
		key = new RevisionKey<>(-1, 1);
		pr = personRevisionDao.fetchById(key);
		Assert.assertNull(pr);

		// check last revision number
		Number revNumber = personRevisionDao.findRevisionNumber(LocalDateTime.now());
		Assert.assertEquals(3, revNumber);
	}

	@Test
	public void testFiltering() {
		// no filter
		personRevisionDao.fetch(null, (Pageable) null);
		personRevisionDao.fetch(new Compare.Equal("name", "Kevin"), (Pageable) null);
		personRevisionDao.fetch(new Compare.GreaterOrEqual("name", "Kevin"), (Pageable) null);
		personRevisionDao.fetch(new Compare.Greater("name", "Kevin"), (Pageable) null);
		personRevisionDao.fetch(new Compare.LessOrEqual("name", "Kevin"), (Pageable) null);
		personRevisionDao.fetch(new Compare.Less("name", "Kevin"), (Pageable) null);
		personRevisionDao.fetch(new Not(new Compare.Equal("name", "Kevin")), (Pageable) null);
		personRevisionDao.fetch(new And(new Compare.Equal("name", "Kevin"), new Compare.Equal("name", "Bob")),
				(Pageable) null);
		personRevisionDao.fetch(new Or(new Compare.Equal("name", "Kevin"), new Compare.Equal("name", "Bob")),
				(Pageable) null);
	}

}
