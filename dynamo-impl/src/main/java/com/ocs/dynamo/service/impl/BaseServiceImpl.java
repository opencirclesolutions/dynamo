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
package com.ocs.dynamo.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.Pageable;
import com.ocs.dynamo.dao.PageableImpl;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.exception.OCSNonUniqueException;
import com.ocs.dynamo.exception.OCSValidationException;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.ClassUtils;

/**
 * Base service implementation
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */

public abstract class BaseServiceImpl<ID, T extends AbstractEntity<ID>> implements BaseService<ID, T> {

	private static final Logger LOGGER = Logger.getLogger(BaseServiceImpl.class);

	@Inject
	private ValidatorFactory factory;

	@Inject
	private MessageService messageService;

	/**
	 * Creates a paging request
	 * 
	 * @param pageNumber
	 *            the zero-based number of the first apge
	 * @param pageSize
	 *            the page size
	 * @param orders
	 *            the sort orders
	 * @return
	 */
	private Pageable constructPageRequest(int pageNumber, int pageSize, SortOrder... orders) {
		return new PageableImpl(pageNumber, pageSize, orders);
	}

	@Override
	public List<T> findAll() {
		return getDao().findAll();
	}

	@Override
	public long count() {
		return getDao().count();
	}

	@Override
	public long count(Filter filter, boolean distinct) {
		return getDao().count(filter, distinct);
	}

	@Override
	public T createNewEntity() {
		return ClassUtils.instantiateClass(getEntityClass());
	}

	@Override
	@Transactional
	public void delete(List<T> list) {
		getDao().delete(list);
	}

	@Override
	@Transactional
	public void delete(T t) {
		getDao().delete(t);
	}

	@Override
	public T fetchById(ID id, FetchJoinInformation... joins) {
		return getDao().fetchById(id, joins);
	}

	@Override
	public List<T> fetchByIds(List<ID> ids, FetchJoinInformation... joins) {
		return getDao().fetchByIds(ids, null, joins);
	}

	@Override
	public List<T> fetchByIds(List<ID> ids, SortOrders sortOrders, FetchJoinInformation... joins) {
		return getDao().fetchByIds(ids, sortOrders, joins);
	}

	@Override
	public T fetchByUniqueProperty(String propertyName, Object value, boolean caseSensitive,
			FetchJoinInformation... joins) {
		return getDao().fetchByUniqueProperty(propertyName, value, caseSensitive, joins);
	}

	@Override
	public List<T> fetch(Filter filter, FetchJoinInformation... joins) {
		return getDao().fetch(filter, joins);
	}

	@Override
	public List<T> fetch(Filter filter, int pageNumber, int pageSize, FetchJoinInformation... joins) {
		return getDao().fetch(filter, constructPageRequest(pageNumber, pageSize, (SortOrder[]) null), joins);
	}

	@Override
	public List<T> fetch(Filter filter, int pageNumber, int pageSize, SortOrders sortOrders,
			FetchJoinInformation... joins) {
		return getDao().fetch(filter,
				constructPageRequest(pageNumber, pageSize, sortOrders == null ? null : sortOrders.toArray()), joins);
	}

	@Override
	public List<T> fetch(Filter filter, SortOrders orders, FetchJoinInformation... joins) {
		return getDao().fetch(filter, orders, joins);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ocs.dynamo.service.BaseService#fetchSelect(com.ocs.dynamo.filter.Filter, java.lang.String[],
	 * com.ocs.dynamo.dao.SortOrders, com.ocs.dynamo.dao.FetchJoinInformation[])
	 */
	@Override
	public List<Object[]> fetchSelect(Filter filter, String[] selectProperties, SortOrders sortOrders,
			FetchJoinInformation... joins) {
		return getDao().fetchSelect(filter, selectProperties, sortOrders, joins);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ocs.dynamo.service.BaseService#fetchSelect(com.ocs.dynamo.filter.Filter, java.lang.String[], int, int,
	 * com.ocs.dynamo.dao.SortOrders, com.ocs.dynamo.dao.FetchJoinInformation[])
	 */
	@Override
	public List<Object[]> fetchSelect(Filter filter, String[] selectProperties, int pageNumber, int pageSize,
			SortOrders sortOrders, FetchJoinInformation... joins) {
		return getDao().fetchSelect(filter, selectProperties,
				constructPageRequest(pageNumber, pageSize, sortOrders == null ? null : sortOrders.toArray()), joins);
	}

	@Override
	public List<T> findAll(SortOrder... orders) {
		return getDao().findAll(orders);
	}

	@Override
	public List<T> find(Filter filter) {
		return getDao().find(filter);
	}

	@Override
	public List<T> find(Filter filter, SortOrder... orders) {
		return getDao().find(filter, orders);
	}

	@Override
	public <S> List<S> findDistinct(Filter filter, String distinctField, Class<S> elementType, SortOrder... orders) {
		return getDao().findDistinct(filter, distinctField, elementType, orders);
	}

	@Override
	public T findById(ID id) {
		return getDao().findById(id);
	}

	@Override
	public T findByUniqueProperty(String propertyName, Object value, boolean caseSensitive) {
		return getDao().findByUniqueProperty(propertyName, value, caseSensitive);
	}

	/**
	 * Looks for an identical entity (which has a different primary key but
	 * 
	 * @param entity
	 *            the entity
	 * @return
	 */
	protected T findIdenticalEntity(T entity) {
		return null;
	}

	@Override
	public List<ID> findIds(Filter filter, SortOrder... orders) {
		return getDao().findIds(filter, orders);
	}

	protected abstract BaseDao<ID, T> getDao();

	@Override
	public Class<T> getEntityClass() {
		return getDao().getEntityClass();
	}

	public MessageService getMessageService() {
		return messageService;
	}

	/**
	 * Checks if there is an entity that is identical to this one Subclasses must
	 * override the findIdenticalEntity method to perform the actual calculation
	 * 
	 * @param t
	 *            the entity to check
	 * @return
	 */
	protected final boolean identicalEntityExists(T t) {
		T other = findIdenticalEntity(t);
		return other != null && (t.getId() == null || !t.getId().equals(other.getId()));
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	protected String message(String key) {
		return messageService.getMessage(key, Locale.getDefault());
	}

	protected String message(String key, Object... args) {
		return messageService.getMessage(key, Locale.getDefault(), args);
	}

	@Override
	@Transactional
	public List<T> save(List<T> list) {
		for (T t : list) {
			validate(t);
		}
		return getDao().save(list);
	}

	@Override
	@Transactional
	public T save(T t) {
		validate(t);
		return getDao().save(t);
	}

	/**
	 * Validates an entity
	 * 
	 * @param t
	 */
	@Override
	public void validate(T t) {

		Validator validator = factory.getValidator();
		Set<ConstraintViolation<T>> constraintViolations = validator.validate(t);

		if (!constraintViolations.isEmpty()) {
			List<String> errors = new ArrayList<>();
			for (ConstraintViolation<T> c : constraintViolations) {
				Class<?> annotationType = c.getConstraintDescriptor().getAnnotation().annotationType();
				if (annotationType.equals(AssertTrue.class) || annotationType.equals(AssertFalse.class)) {
					// in case of assert true or assert false, don't mention
					// the property name
					errors.add(c.getMessage());
				} else {
					errors.add(c.getPropertyPath() + " " + c.getMessage());
				}
			}

			for (String error : errors) {
				LOGGER.warn(error);
			}

			throw new OCSValidationException(errors);
		}

		if (identicalEntityExists(t)) {
			throw new OCSNonUniqueException(messageService.getMessage(getEntityClass().getSimpleName() + ".not.unique",
					new Locale(SystemPropertyUtils.getDefaultLocale())));
		}
	}

	@Override
	@Transactional
	public void update(List<T> toUpdate, List<T> toAdd, List<T> toDelete) {
		save(toUpdate);
		save(toAdd);
		delete(toDelete);
	}

	@Override
	public <S> List<S> findDistinctInCollectionTable(String tableName, String distinctField, Class<S> elementType) {
		return getDao().findDistinctInCollectionTable(tableName, distinctField, elementType);
	}
}
