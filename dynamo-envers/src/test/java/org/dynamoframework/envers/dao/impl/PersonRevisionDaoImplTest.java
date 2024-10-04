package org.dynamoframework.envers.dao.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;

import org.dynamoframework.BackendIntegrationTest;
import org.dynamoframework.dao.Pageable;
import org.dynamoframework.dao.PageableImpl;
import org.dynamoframework.dao.SortOrder;
import org.dynamoframework.dao.SortOrder.Direction;
import org.dynamoframework.dao.SortOrders;
import org.dynamoframework.envers.dao.PersonDao;
import org.dynamoframework.envers.domain.Person;
import org.dynamoframework.envers.domain.PersonRevision;
import org.dynamoframework.envers.domain.RevisionKey;
import org.dynamoframework.envers.domain.RevisionType;
import org.dynamoframework.filter.And;
import org.dynamoframework.filter.Compare;
import org.dynamoframework.filter.Like;
import org.dynamoframework.filter.Not;
import org.dynamoframework.filter.Or;

@Disabled
public class PersonRevisionDaoImplTest extends BackendIntegrationTest {

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
		assertEquals(1L, count);

		List<PersonRevision> list = personRevisionDao.fetch(new Compare.Equal("id", person.getId()), (Pageable) null);
		assertEquals(1, list.size());

		assertEquals(2, list.get(0).getRevision());
		assertEquals(person.getId(), list.get(0).getId().getId());
		assertEquals("Bas", list.get(0).getEntity().getName());
		assertEquals(RevisionType.ADD, list.get(0).getRevisionType());

		status = startTransaction();

		person.setName("Jeroen");
		person = personDao.save(person);

		commitTransaction(status);

		count = personRevisionDao.count(new Compare.Equal("id", person.getId()), true);
		assertEquals(2L, count);
		list = personRevisionDao.fetch(new Compare.Equal("id", person.getId()), (Pageable) null);
		assertEquals(2, list.size());

		assertEquals(3, list.get(1).getRevision());
		assertEquals(person.getId(), list.get(1).getId().getId());
		assertEquals("Jeroen", list.get(1).getEntity().getName());
		assertEquals(RevisionType.MOD, list.get(1).getRevisionType());

		status = startTransaction();

		personDao.delete(person);

		commitTransaction(status);

		list = personRevisionDao.fetch(new Compare.Equal("id", person.getId()), (Pageable) null);
		assertEquals(3, list.size());
		assertEquals(RevisionType.DEL, list.get(2).getRevisionType());

		// try with sorting
		Pageable p = new PageableImpl(0, 10, new SortOrders(new SortOrder("name", Direction.ASC)));
		list = personRevisionDao.fetch(new Compare.Equal("id", person.getId()), p);
		assertEquals(3, list.size());

		// sort on revision type
		p = new PageableImpl(0, 10, new SortOrders(new SortOrder("revisionType", Direction.ASC)));
		list = personRevisionDao.fetch(new Compare.Equal("id", person.getId()), p);
		assertEquals(3, list.size());

		// sort on revision property
		p = new PageableImpl(0, 10, new SortOrders(new SortOrder("revision", Direction.ASC)));
		list = personRevisionDao.fetch(new Compare.Equal("id", person.getId()), p);
		assertEquals(3, list.size());

		// find by revision key
		RevisionKey<Integer> key = new RevisionKey<Integer>(person.getId(), 3);
		PersonRevision pr = personRevisionDao.fetchById(key);
		assertNotNull(pr);

		list = personRevisionDao.findRevisions(person.getId());
		assertEquals(3, list.size());

		// fetch non existing
		key = new RevisionKey<>(person.getId(), 5);
		pr = personRevisionDao.fetchById(key);
		assertNull(pr);

		// fetch non existing part 2
		key = new RevisionKey<>(-1, 1);
		pr = personRevisionDao.fetchById(key);
		assertNull(pr);

		// check last revision number
		Number revNumber = personRevisionDao.findRevisionNumber(LocalDateTime.now());
		assertEquals(4, revNumber);
	}

	@Test
	public void testFindByIds() {
		TransactionStatus status = startTransaction();

		person = new Person();
		person.setName("Bob");
		person = personDao.save(person);

		commitTransaction(status);

		long count = personRevisionDao.count(new Compare.Equal("id", person.getId()), true);
		assertEquals(1L, count);

		List<RevisionKey<Integer>> findIds = personRevisionDao.findIds(new Compare.Equal("id", person.getId()));
		assertEquals(1, findIds.size());

		List<PersonRevision> list = personRevisionDao.fetchByIds(findIds, null);
		assertEquals(1, findIds.size());

		assertEquals(person.getId(), list.get(0).getId().getId());
		assertEquals("Bob", list.get(0).getEntity().getName());
		assertEquals(RevisionType.ADD, list.get(0).getRevisionType());

		findIds = personRevisionDao.findIds(new Compare.Equal("name", "Bob"));
		assertEquals(1, findIds.size());

		findIds = personRevisionDao.findIds(new Compare.Equal("name", "Pete"));
		assertEquals(0, findIds.size());

		status = startTransaction();

		Person person2 = new Person();
		person2.setName("Boris");
		person2 = personDao.save(person2);

		commitTransaction(status);

		findIds = personRevisionDao.findIds(new Like("name", "%Bo%"));
		assertEquals(2, findIds.size());

		list = personRevisionDao.fetchByIds(findIds, null);
		assertEquals(2, findIds.size());
	}

	@Test
	public void testFiltering() {
		assertDoesNotThrow(() -> {
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
		});
	}

}
