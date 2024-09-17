package org.dynamoframework.envers.dao.impl;

import org.springframework.stereotype.Repository;

import org.dynamoframework.envers.domain.Person;
import org.dynamoframework.envers.domain.PersonRevision;

@Repository
public class PersonRevisionDaoImpl extends VersionedEntityDaoImpl<Integer, Person, PersonRevision>
		implements PersonRevisionDao {

	@Override
	public Class<PersonRevision> getEntityClass() {
		return PersonRevision.class;
	}

	@Override
	protected PersonRevision createVersionedEntity(Person t, int revision) {
		return new PersonRevision(t, revision);
	}

	@Override
	public Class<Person> getBaseEntityClass() {
		return Person.class;
	}

}