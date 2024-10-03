package org.dynamoframework.dao.impl;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.dynamoframework.domain.QTestEntity;
import org.dynamoframework.domain.TestEntity;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.domain.model.EntityModelFactory;
import org.dynamoframework.test.BaseMockitoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


public class DefaultDaoImplTest extends BaseMockitoTest {

	private DefaultDaoImpl<Integer, TestEntity> dao;

	@Mock
	private EntityModelFactory entityModelFactory;

	@Mock
	private EntityModel entityModel;

	@BeforeEach
	void beforeEach() {
	}

	@Test
	public void testCreateWithoutFetch() {
		dao = new DefaultDaoImpl<>(QTestEntity.testEntity, TestEntity.class);
		when(entityModelFactory.getModel(TestEntity.class)).thenReturn(entityModel);
		ReflectionTestUtils.setField(dao, "entityModelFactory", entityModelFactory);

		assertEquals(QTestEntity.testEntity, dao.getDslRoot());
		assertEquals(TestEntity.class, dao.getEntityClass());

		// no fetch joins
		assertEquals(0, dao.getJoins().length);
	}

	@Test
	public void testCreateWithFetch() {
		dao = new DefaultDaoImpl<>(QTestEntity.testEntity, TestEntity.class,
			"testEntities");
		ReflectionTestUtils.setField(dao, "entityModelFactory", entityModelFactory);

		assertEquals(QTestEntity.testEntity, dao.getDslRoot());
		assertEquals(TestEntity.class, dao.getEntityClass());

		// check that the fetch joins are properly set
		assertEquals(1, dao.getJoins().length);
		assertEquals("testEntities", dao.getJoins()[0].getProperty());
	}

}
