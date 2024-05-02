/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ocs.dynamo.domain.QTestEntity;
import com.ocs.dynamo.domain.TestEntity;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;

@Repository("testEntityDao")
@Transactional
public class TestEntityDaoImpl extends BaseDaoImpl<Integer, TestEntity> implements TestEntityDao {

	@Override
	protected EntityPathBase<TestEntity> getDslRoot() {
		return QTestEntity.testEntity;
	}

	@Override
	public Class<TestEntity> getEntityClass() {
		return TestEntity.class;
	}

	@Override
	public List<TestEntity> findByBirthDate() {
		JPAQuery<TestEntity> query = createQuery();
		query.where(QTestEntity.testEntity.birthDate.isNotNull());
		return query.from(QTestEntity.testEntity).fetch();
	}
}
