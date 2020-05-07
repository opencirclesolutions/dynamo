package com.ocs.dynamo.functional.domain;

import javax.persistence.Entity;

@Entity
public class SomeTranslation extends Translation<EntityWithTranslations> {

	private static final long serialVersionUID = -3133268387848282391L;

	private EntityWithTranslations entity;

	@Override
	public EntityWithTranslations getEntity() {
		return entity;
	}

	@Override
	public void setEntity(EntityWithTranslations entity) {
		this.entity = entity;

	}

}