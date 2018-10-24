package com.ocs.dynamo.functional.dao.impl;

import com.ocs.dynamo.dao.impl.BaseDaoImpl;
import com.ocs.dynamo.functional.dao.EntityWithTranslationsDao;
import com.ocs.dynamo.functional.domain.EntityWithTranslations;
import com.querydsl.core.types.dsl.EntityPathBase;
import org.springframework.stereotype.Repository;

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
