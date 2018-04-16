package com.ocs.dynamo.functional.dao.impl;

import org.springframework.stereotype.Repository;

import com.mysema.query.types.path.EntityPathBase;
import com.ocs.dynamo.dao.impl.BaseDaoImpl;
import com.ocs.dynamo.functional.dao.EntityWithTranslationsDao;
import com.ocs.dynamo.functional.domain.EntityWithTranslations;

@Repository
public class EntityWithTranslationsDaoImpl extends BaseDaoImpl<Integer, EntityWithTranslations>
		implements EntityWithTranslationsDao {

	@Override
	public Class<EntityWithTranslations> getEntityClass() {
		return EntityWithTranslations.class;
	}

	@Override
	protected EntityPathBase<EntityWithTranslations> getDslRoot() {
		return null;
	}

}
