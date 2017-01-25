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
package com.ocs.dynamo.functional.dao;

import java.util.List;

import javax.persistence.criteria.CriteriaQuery;

import org.springframework.stereotype.Repository;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.path.EntityPathBase;
import com.ocs.dynamo.dao.impl.DefaultDaoImpl;
import com.ocs.dynamo.functional.domain.Domain;
import com.ocs.dynamo.functional.domain.DomainChild;
import com.ocs.dynamo.functional.domain.DomainParent;
import com.ocs.dynamo.functional.domain.QDomain;
import com.ocs.dynamo.functional.domain.QDomainChild;

/**
 * Data access implementation for managing domain entities.
 * 
 * @author Patrick Deenen (patrick@opencircle.solutions)
 *
 */
@Repository("domainDao")
public class DomainDaoImpl extends DefaultDaoImpl<Integer, Domain> implements DomainDao {

	public DomainDaoImpl() {
		super(QDomain.domain, Domain.class);
	}

	public DomainDaoImpl(EntityPathBase<Domain> dslRoot, Class<Domain> entityClass) {
		super(dslRoot, entityClass);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<DomainChild<? extends DomainParent>> findChildren(DomainParent<? extends DomainChild> parent) {
		QDomainChild qDH = QDomainChild.domainChild;
		JPAQuery query = new JPAQuery(getEntityManager()).from(qDH);
		query.where(qDH.parent.eq(parent));
		return query.list(qDH);
	}

	@Override
	public List<? extends Domain> findAllByType(Class<? extends Domain> type) {
		CriteriaQuery<? extends Domain> cq = getEntityManager().getCriteriaBuilder().createQuery(type);
		cq.from(type);
		return getEntityManager().createQuery(cq).getResultList();
	}
}
