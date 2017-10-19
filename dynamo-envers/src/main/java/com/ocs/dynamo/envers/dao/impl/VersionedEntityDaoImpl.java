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
package com.ocs.dynamo.envers.dao.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.types.path.EntityPathBase;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.Pageable;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.dao.impl.BaseDaoImpl;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.envers.dao.VersionedEntityDao;
import com.ocs.dynamo.envers.domain.DynamoRevisionEntity;
import com.ocs.dynamo.envers.domain.RevisionKey;
import com.ocs.dynamo.envers.domain.RevisionType;
import com.ocs.dynamo.envers.domain.VersionedEntity;
import com.ocs.dynamo.filter.Compare;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.filter.FilterUtil;
import com.ocs.dynamo.utils.DateUtils;

/**
 * Implementation of Data Access object for versioned entities
 * 
 * @author bas.rutten
 *
 * @param <ID>
 * @param <T>
 * @param <U>
 */
public abstract class VersionedEntityDaoImpl<ID, T extends AbstractEntity<ID>, U extends VersionedEntity<ID, T>>
		extends BaseDaoImpl<RevisionKey<ID>, U> implements VersionedEntityDao<ID, T, U> {

	/**
	 * Adds a filter on the ID field to an audit query
	 * 
	 * @param aq
	 *            the audit query
	 * @param filter
	 *            the overall filter
	 */
	@SuppressWarnings("unchecked")
	private void addIdFilter(AuditQuery aq, Filter filter) {
		if (filter != null) {
			Filter idFilter = FilterUtil.extractFilter(filter, DynamoConstants.ID);
			if (idFilter != null) {
				Compare.Equal comp = (Compare.Equal) idFilter;
				ID id = (ID) comp.getValue();
				aq.add(AuditEntity.id().eq(id));
			}
		} else {
			throw new IllegalArgumentException("Filtering on ID is mandatory");
		}
	}

	/**
	 * Overwrite count method to query
	 */
	@Override
	@Transactional
	public long count(Filter filter, boolean distinct) {
		// filter on ID (this should always be there)
		AuditQuery aq = getAuditReader().createQuery().forRevisionsOfEntity(getBaseEntityClass(), false, true);
		addIdFilter(aq, filter);
		return aq.getResultList().size();
	}

	/**
	 * Creates a new instance of the versioned entity
	 * 
	 * @return
	 */
	protected abstract U createVersionedEntity(T t, int revision);

	protected void doMap(U u) {
		// overwrite in subclasses
	}

	@Override
	@Transactional
	@SuppressWarnings("unchecked")
	public List<U> fetch(Filter filter, Pageable pageable, FetchJoinInformation... joins) {

		AuditQuery aq = getAuditReader().createQuery().forRevisionsOfEntity(getBaseEntityClass(), false, true);
		addIdFilter(aq, filter);

		if (pageable != null) {
			aq.setFirstResult(pageable.getOffset());
			aq.setMaxResults(pageable.getPageSize());
		}

		List<U> resultList = new ArrayList<>();
		List<Object[]> revs = aq.getResultList();

		for (Object[] rev : revs) {
			// first comes the actual snapshot of the entity
			U u = map(rev);
			resultList.add(u);
		}

		return resultList;
	}

	@Override
	@Transactional
	public U fetchById(RevisionKey<ID> id, FetchJoinInformation... joins) {
		AuditQuery aq = getAuditReader().createQuery().forRevisionsOfEntity(getBaseEntityClass(), false, true);
		aq.add(AuditEntity.id().eq(id.getId()));
		aq.add(AuditEntity.revisionNumber().eq(id.getRevision()));
		Object[] rev = (Object[]) aq.getSingleResult();
		return map(rev);
	}

	@Override
	public List<U> fetchByIds(List<RevisionKey<ID>> ids, SortOrders sortOrders, FetchJoinInformation... joins) {
		// only paging access is supported
		throw new UnsupportedOperationException();
	}

	@Override
	public List<RevisionKey<ID>> findIds(Filter filter, SortOrder... sortOrders) {
		// only paging access is supported
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional
	public List<U> findRevisions(ID id) {
		List<Object[]> revs = getAuditReader().createQuery().forRevisionsOfEntity(getBaseEntityClass(), false, true)
				.add(AuditEntity.id().eq(id)).getResultList();
		List<U> resultList = new ArrayList<>();

		for (Object[] rev : revs) {
			resultList.add(map(rev));
		}
		return resultList;
	}

	@Override
	public Number findRevisionNumber(LocalDateTime ldt) {
		return getAuditReader().getRevisionNumberForDate(DateUtils.toLegacyDate(ldt));
	}

	private AuditReader getAuditReader() {
		return AuditReaderFactory.get(getEntityManager());
	}

	/**
	 * Returns the class of the "base entity" (i.e. the non-versioned class)
	 * 
	 * @return
	 */
	public abstract Class<T> getBaseEntityClass();

	@Override
	protected EntityPathBase<U> getDslRoot() {
		// not needed
		return null;
	}

	/**
	 * Maps the data structure returned by Envers to a VersionedEntity. This
	 * structure contains of the snapshot data (position 0), the revision data
	 * (position 1) and the modification type (position 2)
	 * 
	 * @param rev
	 *            the data structure to map
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private U map(Object[] rev) {
		T t = (T) rev[0];
		DynamoRevisionEntity revisionData = (DynamoRevisionEntity) rev[1];
		U u = createVersionedEntity(t, revisionData.getId());

		Instant i = Instant.ofEpochMilli(revisionData.getTimestamp());
		u.setRevisionTimeStamp(ZonedDateTime.ofInstant(i, ZoneId.systemDefault()));
		u.setUser(revisionData.getUsername());
		u.setRevisionType(RevisionType.fromInternal((org.hibernate.envers.RevisionType) rev[2]));
		doMap(u);
		return u;
	}
}
