package com.ocs.dynamo.functional.dao.impl;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ocs.dynamo.functional.dao.EntityWithTranslationsDao;
import com.ocs.dynamo.functional.domain.EntityWithTranslations;
import com.ocs.dynamo.functional.domain.SomeTranslation;
import com.ocs.dynamo.test.BaseIntegrationTest;

public class EntityWithTranslationsDaoImplTest extends BaseIntegrationTest {

	@Autowired
	private EntityWithTranslationsDao dao;

	@Test
	public void test() {
		EntityWithTranslations entity = new EntityWithTranslations();
		SomeTranslation t = new SomeTranslation();
		entity.addTranslation(t);

		dao.save(entity);
	}

}
