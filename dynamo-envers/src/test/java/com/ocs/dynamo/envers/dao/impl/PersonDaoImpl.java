package com.ocs.dynamo.envers.dao.impl;

import org.springframework.stereotype.Repository;

import com.ocs.dynamo.dao.impl.BaseDaoImpl;
import com.ocs.dynamo.envers.dao.PersonDao;
import com.ocs.dynamo.envers.domain.Person;
import com.ocs.dynamo.envers.domain.QPerson;
import com.querydsl.core.types.dsl.EntityPathBase;

@Repository
public class PersonDaoImpl extends BaseDaoImpl<Integer, Person> implements PersonDao {

	@Override
	public Class<Person> getEntityClass() {
		return Person.class;
	}

	@Override
	protected EntityPathBase<Person> getDslRoot() {
		return QPerson.person;
	}

	public Person save(Person t) {
		return super.save(t);
	}



}
