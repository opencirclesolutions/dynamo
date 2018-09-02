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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.persistence.NoResultException;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.criteria.AuditConjunction;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.hibernate.envers.query.criteria.AuditDisjunction;
import org.hibernate.envers.query.criteria.AuditProperty;
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
import com.ocs.dynamo.filter.And;
import com.ocs.dynamo.filter.Compare;
import com.ocs.dynamo.filter.DynamoFilterUtil;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.filter.FilterUtil;
import com.ocs.dynamo.filter.In;
import com.ocs.dynamo.filter.Like;
import com.ocs.dynamo.filter.Not;
import com.ocs.dynamo.filter.Or;
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

	private static final String ENTITY_STRING = "entity.";

	private static final Map<String, String> REVISION_PROPS = new ConcurrentHashMap<>();

	/**
	 * Adds any additional filters to an AuditQuery
	 * 
	 * @param aq
	 *            the AuditQuery to which to add the filters
	 * @param filter
	 *            the filter to translate
	 */
	private void addAdditionalFilters(AuditQuery aq, Filter filter) {
		AuditCriterion ac = createAuditCriterion(filter);
		if (ac != null) {
			aq.add(ac);
		}
	}

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
			Filter idFilter = DynamoFilterUtil.extractFilter(filter, DynamoConstants.ID);
			if (idFilter != null) {
				Compare.Equal comp = (Compare.Equal) idFilter;
				ID id = (ID) comp.getValue();
				aq.add(AuditEntity.id().eq(id));
			}
		}
	}

	/**
	 * Overwrite count method to query the revision tables
	 */
	@Override
	@Transactional
	public long count(Filter filter, boolean distinct) {
		// filter on ID (this should always be there)
		AuditQuery aq = getAuditReader().createQuery().forRevisionsOfEntity(getBaseEntityClass(), false, true);
		addIdFilter(aq, filter);
		addAdditionalFilters(aq, filter);
		return aq.getResultList().size();
	}

	/**
	 * Translates a filter to an AuditCriterion
	 * 
	 * @param filter
	 * @return
	 */
	private AuditCriterion createAuditCriterion(Filter filter) {
		if (filter instanceof Compare.Equal) {
			Compare.Equal eq = (Compare.Equal) filter;
			return createAuditProperty(eq.getPropertyId()).eq(eq.getValue());
		} else if (filter instanceof Like) {
			Like like = (Like) filter;
			return createAuditProperty(like.getPropertyId()).like(like.getValue());
		} else if (filter instanceof Compare.Greater) {
			Compare.Greater gt = (Compare.Greater) filter;
			return createAuditProperty(gt.getPropertyId()).gt(gt.getValue());
		} else if (filter instanceof Compare.GreaterOrEqual) {
			Compare.GreaterOrEqual ge = (Compare.GreaterOrEqual) filter;
			return createAuditProperty(ge.getPropertyId()).ge(ge.getValue());
		} else if (filter instanceof Compare.Less) {
			Compare.Less lt = (Compare.Less) filter;
			return createAuditProperty(lt.getPropertyId()).lt(lt.getValue());
		} else if (filter instanceof Compare.LessOrEqual) {
			Compare.LessOrEqual le = (Compare.LessOrEqual) filter;
			return createAuditProperty(le.getPropertyId()).le(le.getValue());
		} else if (filter instanceof And) {
			And and = (And) filter;
			AuditConjunction ac = AuditEntity.conjunction();
			for (Filter f : and.getFilters()) {
				AuditCriterion ct = createAuditCriterion(f);
				if (ct != null) {
					ac.add(ct);
				}
			}
			return ac;
		} else if (filter instanceof Or) {
			Or or = (Or) filter;
			AuditDisjunction ad = AuditEntity.disjunction();
			for (Filter f : or.getFilters()) {
				AuditCriterion ct = createAuditCriterion(f);
				if (ct != null) {
					ad.add(ct);
				}
			}
			return ad;
		} else if (filter instanceof Not) {
			Not not = (Not) filter;
			AuditCriterion ct = createAuditCriterion(not.getFilter());
			if (ct != null) {
				return AuditEntity.not(ct);
			}
		} else if (filter instanceof In) {
			In in = (In) filter;
			return AuditEntity.property(in.getPropertyId()).in(in.getValues());
		}
		return null;
	}

	/**
	 * Translates a prperoty name to an AuditProperty
	 * 
	 * @param prop
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <X> AuditProperty<X> createAuditProperty(String prop) {
		if ("revisionType".equals(prop)) {
			return (AuditProperty<X>) AuditEntity.revisionType();
		} else if (REVISION_PROPS.containsKey(prop)) {
			prop = REVISION_PROPS.get(prop);
			return (AuditProperty<X>) AuditEntity.revisionProperty(prop);
		} else {
			int index = prop.indexOf(ENTITY_STRING);
			if (index >= 0) {
				prop = prop.substring(index + ENTITY_STRING.length());
			}
			return (AuditProperty<X>) AuditEntity.property(prop);
		}
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
	public List<U> fetch(Filter filter, FetchJoinInformation... joins) {
		return fetch(filter, (Pageable) null, joins);
	}

	@Override
	@Transactional
	@SuppressWarnings("unchecked")
	public List<U> fetch(Filter filter, Pageable pageable, FetchJoinInformation... joins) {

		AuditQuery aq = getAuditReader().createQuery().forRevisionsOfEntity(getBaseEntityClass(), false, true);
		addIdFilter(aq, filter);
		addAdditionalFilters(aq, filter);

		if (pageable != null) {
			aq.setFirstResult(pageable.getOffset());
			aq.setMaxResults(pageable.getPageSize());
			if (pageable.getSortOrders() != null) {
				for (SortOrder so : pageable.getSortOrders().toArray()) {
					String prop = so.getProperty();
					AuditProperty<?> ap = createAuditProperty(prop);
					if (so.isAscending()) {
						aq.addOrder(ap.asc());
					} else {
						aq.addOrder(ap.desc());
					}
				}
			}
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
		try {
			AuditQuery aq = getAuditReader().createQuery().forRevisionsOfEntity(getBaseEntityClass(), false, true);
			aq.add(AuditEntity.id().eq(id.getId()));
			aq.add(AuditEntity.revisionNumber().eq(id.getRevision()));
			Object[] rev = (Object[]) aq.getSingleResult();
			return map(rev);
		} catch (NoResultException ex) {
			// nothing found
			return null;
		}
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
	public Number findRevisionNumber(LocalDateTime ldt) {
		return getAuditReader().getRevisionNumberForDate(DateUtils.toLegacyDate(ldt));
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

	private AuditReader getAuditReader() {
		return AuditReaderFactory.get(getEntityManager());
	}

	/**
	 * Returns the class of the "base entity" (i.e. the non-versioned class)
	 * 
	 * @return
	 */
	public abstract Class<T> getBaseEntityClass();

	/**
	 * Returns the Query DSL root. This is not supported for versioned entities
	 */
	@Override
	protected EntityPathBase<U> getDslRoot() {
		throw new UnsupportedOperationException();
	}

	@PostConstruct
	public void init() {
		// add mapping from versioned entity properties to RevisionEntity
		// properties
		REVISION_PROPS.put("revision", "id");
		REVISION_PROPS.put("revisionTimeStamp", "timestamp");
		REVISION_PROPS.put("user", "username");
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
		if (rev == null) {
			return null;
		}

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
