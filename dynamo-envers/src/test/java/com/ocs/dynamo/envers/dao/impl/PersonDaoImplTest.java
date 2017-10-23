package com.ocs.dynamo.envers.dao.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.ocs.dynamo.dao.Pageable;
import com.ocs.dynamo.envers.dao.PersonDao;
import com.ocs.dynamo.envers.domain.Person;
import com.ocs.dynamo.envers.domain.PersonRevision;
import com.ocs.dynamo.envers.domain.RevisionKey;
import com.ocs.dynamo.envers.domain.RevisionType;
import com.ocs.dynamo.filter.Compare;
import com.ocs.dynamo.test.BaseIntegrationTest;

public class PersonDaoImplTest extends BaseIntegrationTest {

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private PersonDao personDao;

	@Autowired
	private PersonRevisionDao personRevisionDao;

	private Person person;

	@Test
	public void testCreateUpdateDelete() {
		TransactionStatus status = transactionManager
				.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

		person = new Person();
		person.setName("Bas");
		person = personDao.save(person);

		transactionManager.commit(status);

		long count = personRevisionDao.count(new Compare.Equal("id", person.getId()), true);
		Assert.assertEquals(1L, count);

		List<PersonRevision> list = personRevisionDao.fetch(new Compare.Equal("id", person.getId()), (Pageable) null);
		Assert.assertEquals(1, list.size());

		Assert.assertEquals(1, list.get(0).getRevision());
		Assert.assertEquals(person.getId(), list.get(0).getId().getId());
		Assert.assertEquals("Bas", list.get(0).getEntity().getName());
		Assert.assertEquals(RevisionType.ADD, list.get(0).getRevisionType());

		status = transactionManager
				.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

		person.setName("Jeroen");
		person = personDao.save(person);

		transactionManager.commit(status);

		count = personRevisionDao.count(new Compare.Equal("id", person.getId()), true);
		Assert.assertEquals(2L, count);
		list = personRevisionDao.fetch(new Compare.Equal("id", person.getId()), (Pageable) null);
		Assert.assertEquals(2, list.size());

		Assert.assertEquals(2, list.get(1).getRevision());
		Assert.assertEquals(person.getId(), list.get(1).getId().getId());
		Assert.assertEquals("Jeroen", list.get(1).getEntity().getName());
		Assert.assertEquals(RevisionType.MOD, list.get(1).getRevisionType());

		status = transactionManager
				.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

		personDao.delete(person);

		transactionManager.commit(status);

		list = personRevisionDao.fetch(new Compare.Equal("id", person.getId()), (Pageable) null);
		Assert.assertEquals(3, list.size());
		Assert.assertEquals(RevisionType.DEL, list.get(2).getRevisionType());

		// the revision key
		RevisionKey<Integer> key = new RevisionKey<Integer>(person.getId(), 3);
		PersonRevision pr = personRevisionDao.fetchById(key);
		Assert.assertNotNull(pr);

		key = new RevisionKey<Integer>(person.getId(), 4);
		pr = personRevisionDao.fetchById(key);
		Assert.assertNull(pr);
	}
}
