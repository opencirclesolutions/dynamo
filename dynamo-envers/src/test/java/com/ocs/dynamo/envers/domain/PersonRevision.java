package com.ocs.dynamo.envers.domain;

import com.ocs.dynamo.domain.model.annnotation.Attribute;

public class PersonRevision extends VersionedEntity<Integer, Person> {

	private static final long serialVersionUID = 6164420504491180851L;

	public PersonRevision(Person entity, int revision) {
		super(entity, revision);
	}

	@Override
	@Attribute(embedded = true)
	public Person getEntity() {
		return super.getEntity();
	}
}
