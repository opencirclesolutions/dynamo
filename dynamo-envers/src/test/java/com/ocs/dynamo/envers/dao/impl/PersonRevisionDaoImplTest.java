package com.ocs.dynamo.envers.dao.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;

import com.ocs.dynamo.BackendIntegrationTest;
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
    public void testFiltering() {
        // no filter
        personRevisionDao.fetch(null, (Pageable) null);
        personRevisionDao.fetch(new Compare.Equal("name", "Kevin"), (Pageable) null);
        personRevisionDao.fetch(new Compare.GreaterOrEqual("name", "Kevin"), (Pageable) null);
        personRevisionDao.fetch(new Compare.Greater("name", "Kevin"), (Pageable) null);
        personRevisionDao.fetch(new Compare.LessOrEqual("name", "Kevin"), (Pageable) null);
        personRevisionDao.fetch(new Compare.Less("name", "Kevin"), (Pageable) null);
        personRevisionDao.fetch(new Not(new Compare.Equal("name", "Kevin")), (Pageable) null);
        personRevisionDao.fetch(new And(new Compare.Equal("name", "Kevin"), new Compare.Equal("name", "Bob")), (Pageable) null);
        personRevisionDao.fetch(new Or(new Compare.Equal("name", "Kevin"), new Compare.Equal("name", "Bob")), (Pageable) null);
    }

}
