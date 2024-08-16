package org.dynamoframework.envers.dao.impl;

import org.dynamoframework.envers.dao.VersionedEntityDao;
import org.dynamoframework.envers.domain.Person;
import org.dynamoframework.envers.domain.PersonRevision;

public interface PersonRevisionDao extends VersionedEntityDao<Integer, Person, PersonRevision> {

}
