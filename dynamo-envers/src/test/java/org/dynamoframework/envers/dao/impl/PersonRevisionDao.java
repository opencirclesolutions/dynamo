package org.dynamoframework.envers.dao.impl;

import com.ocs.dynamo.envers.dao.VersionedEntityDao;
import com.ocs.dynamo.envers.domain.Person;
import com.ocs.dynamo.envers.domain.PersonRevision;

public interface PersonRevisionDao extends VersionedEntityDao<Integer, Person, PersonRevision> {

}
