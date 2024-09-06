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
package org.dynamoframework.envers.dao.impl;

import org.dynamoframework.constants.DynamoConstants;
import org.dynamoframework.dao.FetchJoinInformation;
import org.dynamoframework.dao.Pageable;
import org.dynamoframework.dao.SortOrder;
import org.dynamoframework.dao.SortOrders;
import org.dynamoframework.dao.impl.BaseDaoImpl;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.envers.dao.VersionedEntityDao;
import org.dynamoframework.envers.domain.DynamoRevisionEntity;
import org.dynamoframework.envers.domain.RevisionKey;
import org.dynamoframework.envers.domain.RevisionType;
import org.dynamoframework.envers.domain.VersionedEntity;
import org.dynamoframework.filter.*;
import org.dynamoframework.utils.DateUtils;
import com.querydsl.core.types.dsl.EntityPathBase;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.criteria.AuditConjunction;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.hibernate.envers.query.criteria.AuditDisjunction;
import org.hibernate.envers.query.criteria.AuditProperty;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.NoResultException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of Data Access object for versioned entities
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key
 * @param <T>  the type of the original entity
 * @param <U>  the type of the versioned entity
 */
public abstract class VersionedEntityDaoImpl<ID, T extends AbstractEntity<ID>, U extends VersionedEntity<ID, T>>
		extends BaseDaoImpl<RevisionKey<ID>, U> implements VersionedEntityDao<ID, T, U> {

	private static final String ENTITY_STRING = "entity.";

	private static final Map<String, String> REVISION_PROPS = new ConcurrentHashMap<>();

	/**
	 * Adds any additional filters to an AuditQuery
	 * 
	 * @param auditQuery     the AuditQuery to which to add the filters
	 * @param filter the filter to translate
	 */
	private void addAdditionalFilters(AuditQuery auditQuery, Filter filter) {
		AuditCriterion criterion = createAuditCriterion(filter);
		if (criterion != null) {
			auditQuery.add(criterion);
		}
	}

	/**
	 * Adds a search criteria for matching any of the provided keys
	 * 
	 * @param ids the IDs to match on
	 * @param aq  the audit query to which to add the criterion
	 */
	private void addIdCriteria(List<RevisionKey<ID>> ids, AuditQuery aq) {
		AuditCriterion criterion = null;
		for (RevisionKey<ID> id : ids) {
			AuditCriterion and = AuditEntity.and(AuditEntity.id().eq(id.getId()),
					AuditEntity.revisionNumber().eq(id.getRevision()));
			if (criterion == null) {
				criterion = and;
			} else {
				criterion = AuditEntity.or(criterion, and);
			}
		}

		if (criterion != null) {
			aq.add(criterion);
		}
	}

	/**
	 * Adds a filter on the ID field to an audit query
	 * 
	 * @param aq     the audit query
	 * @param filter the overall filter
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
	 * Add a sort order to an audit query
	 * 
	 * @param aq the audit query
	 * @param so the sort orders to add
	 */
	private void addSortOrder(AuditQuery aq, SortOrder so) {
		String prop = so.getProperty();
		AuditProperty<?> ap = createAuditProperty(prop, false);
		if (so.isAscending()) {
			aq.addOrder(ap.asc());
		} else {
			aq.addOrder(ap.desc());
		}
	}

	/**
	 * Adds multiple sort orders
	 * 
	 * @param aq         the audit query
	 * @param sortOrders the sort orders
	 */
	private void addSortOrders(AuditQuery aq, SortOrder[] sortOrders) {
		for (SortOrder so : sortOrders) {
			addSortOrder(aq, so);
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
	 * @param filter the filter to translate
	 * @return the resulting AuditCriterion
	 */
	private AuditCriterion createAuditCriterion(Filter filter) {
		if (filter instanceof Compare.Equal eq) {
			return createAuditProperty(eq.getPropertyId(), eq.getValue()).eq(eq.getValue());
		} else if (filter instanceof Like like) {
			return createAuditProperty(like.getPropertyId(), like.getValue()).ilike(like.getValue());
		} else if (filter instanceof Compare.Greater gt) {
			return createAuditProperty(gt.getPropertyId(), gt.getValue()).gt(gt.getValue());
		} else if (filter instanceof Compare.GreaterOrEqual ge) {
			return createAuditProperty(ge.getPropertyId(), ge.getValue()).ge(ge.getValue());
		} else if (filter instanceof Compare.Less ls) {
			return createAuditProperty(ls.getPropertyId(), ls.getValue()).lt(ls.getValue());
		} else if (filter instanceof Compare.LessOrEqual le) {
			return createAuditProperty(le.getPropertyId(), le.getValue()).le(le.getValue());
		} else if (filter instanceof And and) {
			AuditConjunction ac = AuditEntity.conjunction();
			for (Filter andFilter : and.getFilters()) {
				AuditCriterion ct = createAuditCriterion(andFilter);
				if (ct != null) {
					ac.add(ct);
				}
			}
			return ac;
		} else if (filter instanceof Or or) {
			AuditDisjunction ad = AuditEntity.disjunction();
			for (Filter orFilter : or.getFilters()) {
				AuditCriterion ct = createAuditCriterion(orFilter);
				if (ct != null) {
					ad.add(ct);
				}
			}
			return ad;
		} else if (filter instanceof Not not) {
			AuditCriterion ct = createAuditCriterion(not.getFilter());
			if (ct != null) {
				return AuditEntity.not(ct);
			}
		} else if (filter instanceof In in) {
			return createAuditProperty(in.getPropertyId(), in.getValues()).in(in.getValues());
		} else if (filter instanceof Contains c) {
			throw new UnsupportedOperationException(
					"Contains filter is not supported for property " + c.getPropertyId());
		}
		return null;
	}

	/**
	 * Translates a property name to an AuditProperty
	 * 
	 * @param prop  the name of the property
	 * @param value the value of the property
	 * @return the created AuditProperty
	 */
	@SuppressWarnings("unchecked")
	private <X> AuditProperty<X> createAuditProperty(String prop, Object value) {
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

			boolean isEntity = isEntity(value);
			if (isEntity) {
				throw new UnsupportedOperationException("Querying for complex properties is not supported yet");
			}

			return (AuditProperty<X>) AuditEntity.property(prop);
		}
	}

	/**
	 * Creates a new instance of the versioned entity
	 * 
	 * @return the newly created instance
	 */
	protected abstract U createVersionedEntity(T t, int revision);

	/**
	 * Perform mapping from the Envers revision entity to the full entity
	 * 
	 * @param u the Envers revision entity to map
	 */
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
				addSortOrders(aq, pageable.getSortOrders().toArray());
			}
		}

		List<U> resultList = new ArrayList<>();
		List<Object[]> revs = aq.getResultList();

		for (Object[] rev : revs) {
			// first comes the actual snapshot of the entity
			U u = mapRevision(rev);
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
			return mapRevision(rev);
		} catch (NoResultException ex) {
			// nothing found
			return null;
		}
	}

	@Override
	@Transactional
	public List<U> fetchByIds(List<RevisionKey<ID>> ids, Filter additionalFilter, SortOrders sortOrders,
			FetchJoinInformation... joins) {
		AuditQuery aq = getAuditReader().createQuery().forRevisionsOfEntity(getBaseEntityClass(), false, true);

		addIdCriteria(ids, aq);

		if (sortOrders != null) {
			addSortOrders(aq, sortOrders.toArray());
		}

		@SuppressWarnings("unchecked")
		List<Object[]> revs = aq.getResultList();
		return revs.stream().map(this::mapRevision).toList();
	}

	@Override
	@Transactional
	public List<U> fetchByIds(List<RevisionKey<ID>> ids, SortOrders sortOrders, FetchJoinInformation... joins) {
		return fetchByIds(ids, null, sortOrders, joins);
	}

	@Override
	@Transactional
	public List<RevisionKey<ID>> findIds(Filter filter, Integer maxResults, SortOrder... sortOrders) {
		AuditQuery aq = getAuditReader().createQuery().forRevisionsOfEntity(getBaseEntityClass(), false, true)
				.addProjection(AuditEntity.id()).addProjection(AuditEntity.revisionNumber());
		addIdFilter(aq, filter);
		addAdditionalFilters(aq, filter);

		aq.setFirstResult(0);
		if (maxResults != null) {
			aq.setMaxResults(maxResults);
		}
		if (sortOrders != null) {
			addSortOrders(aq, sortOrders);
		}

		return findRevisionKeys(aq);
	}

	@Override
	@Transactional
	public List<RevisionKey<ID>> findIds(Filter filter, SortOrder... sortOrders) {
		return findIds(filter, null, sortOrders);
	}

	@SuppressWarnings("unchecked")
	private List<RevisionKey<ID>> findRevisionKeys(AuditQuery aq) {
		List<Object[]> revs = aq.getResultList();
		return revs.stream().map(this::mapRevisionKey).toList();
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
		return revs.stream().map(this::mapRevision).toList();
	}

	private AuditReader getAuditReader() {
		return AuditReaderFactory.get(getEntityManager());
	}

	/**
	 * @return the class of the "base entity" (i.e. the non-versioned class)
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
	 * Determine whether a property value is an entity (or a collection of entities)
	 * 
	 * @param value the value for which to determine this
	 * @return true if this is the case, false otherwise
	 */
	private boolean isEntity(Object value) {
		if (value instanceof AbstractEntity) {
			return true;
		}
		if (value instanceof Collection<?> col) {
			return !col.isEmpty() && col.iterator().next() instanceof AbstractEntity;
		}
		return false;
	}

	/**
	 * Maps the data structure returned by Envers to a VersionedEntity. This
	 * structure contains of the snapshot data (position 0), the revision data
	 * (position 1) and the modification type (position 2)
	 * 
	 * @param rev the data structure to map
	 * @return the result of the mapping
	 */
	@SuppressWarnings("unchecked")
	private U mapRevision(Object[] rev) {
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
		if (u.getEntity() != null) {
			doMap(u);
		}
		return u;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private RevisionKey<ID> mapRevisionKey(Object[] rev) {
		return new RevisionKey(rev[0], (Integer) rev[1]);
	}
}
