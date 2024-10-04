package org.dynamoframework.envers.dao.impl;

import org.springframework.stereotype.Repository;

import org.dynamoframework.dao.impl.BaseDaoImpl;
import org.dynamoframework.envers.dao.PersonDao;
import org.dynamoframework.envers.domain.Person;
import org.dynamoframework.envers.domain.QPerson;
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
