package com.ocs.dynamo.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;

import org.apache.log4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.exception.OCSNonUniqueException;
import com.ocs.dynamo.exception.OCSValidationException;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
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

public abstract class BaseServiceImpl<ID, T extends AbstractEntity<ID>>
        implements BaseService<ID, T> {

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
	 * @param orders
	 * @return
	 */
	private PageRequest constructPageRequest(int pageNumber, int pageSize, SortOrder... orders) {
		PageRequest pr = null;
		if (orders != null && orders.length > 0) {
			List<org.springframework.data.domain.Sort.Order> list = new ArrayList<>();
			for (SortOrder o : orders) {
				if (o != null) {
					list.add(new org.springframework.data.domain.Sort.Order(
					        o.getDirection() == SortOrder.Direction.ASC
					                ? org.springframework.data.domain.Sort.Direction.ASC
					                : org.springframework.data.domain.Sort.Direction.DESC,
					        o.getProperty()));
				}
			}
			if (list.isEmpty()) {
				pr = new PageRequest(pageNumber, pageSize);
			} else {
				pr = new PageRequest(pageNumber, pageSize, new Sort(list));
			}
		} else {
			pr = new PageRequest(pageNumber, pageSize);
		}
		return pr;
	}

	/**
	 * Create a Sort object based on a number of Order objects
	 * 
	 * @param orders
	 * @return
	 */
	private Sort constructSortOrder(SortOrder... orders) {
		if (orders != null && orders.length > 0) {
			List<org.springframework.data.domain.Sort.Order> list = new ArrayList<>();
			for (SortOrder o : orders) {
				if (o != null) {
					list.add(new org.springframework.data.domain.Sort.Order(
					        o.getDirection() == SortOrder.Direction.ASC
					                ? org.springframework.data.domain.Sort.Direction.ASC
					                : org.springframework.data.domain.Sort.Direction.DESC,
					        o.getProperty()));
				}
			}

			if (!list.isEmpty()) {
				return new Sort(list);
			}
		}
		return null;
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
	public List<T> fetchByIds(List<ID> ids, FetchJoinInformation[] joins, SortOrder... orders) {
		return getDao().fetchByIds(ids, constructSortOrder(orders), joins);
	}

	@Override
	public T fetchByUniqueProperty(String propertyName, Object value, boolean caseSensitive,
	        FetchJoinInformation... joins) {
		return getDao().fetchByUniqueProperty(propertyName, value, caseSensitive, joins);
	}

	@Override
	public List<T> fetch(Filter filter, int pageNumber, int pageSize, FetchJoinInformation[] joins,
	        SortOrder... orders) {
		return getDao().find(filter, constructPageRequest(pageNumber, pageSize, orders), joins);
	}

	@Override
	public List<T> find(Filter filter, int pageNumber, int pageSize, SortOrder... orders) {
		return getDao().find(filter, constructPageRequest(pageNumber, pageSize, orders));
	}

	@Override
	public List<T> find(Filter filter, SortOrder... orders) {
		return getDao().find(filter, constructSortOrder(orders));
	}

	@Override
	public List<T> findAll(SortOrder... orders) {
		return getDao().findAll(constructSortOrder(orders));
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
	 * @param t
	 * @return
	 */
	protected T findIdenticalEntity(T t) {
		return null;
	}

	@Override
	public List<ID> findIds(Filter filter, SortOrder... orders) {
		return getDao().findIds(filter, constructSortOrder(orders));
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
	 * Checks if there is an entity that is identical to this one
	 * Subclasses must override the findIdenticalEntity method to perform the
	 * actual calculation
	 * 
	 * @param t
	 *            the entity to check
	 * @return
	 */
	protected final boolean identicalEntityExists(T t) {
		T other = findIdenticalEntity(t);
		if (other != null && (t.getId() == null || !t.getId().equals(other.getId()))) {
			return true;
		}
		return false;
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
	protected void validate(T t) {

		Validator validator = factory.getValidator();
		Set<ConstraintViolation<T>> constraintViolations = validator.validate(t);

		if (!constraintViolations.isEmpty()) {
			List<String> errors = new ArrayList<>();
			for (ConstraintViolation<T> c : constraintViolations) {
				Class<?> annotationType = c.getConstraintDescriptor().getAnnotation()
				        .annotationType();
				if (annotationType.equals(AssertTrue.class)
				        || annotationType.equals(AssertFalse.class)) {
					// in case of assert true or assert false, don't mention
					// the property name
					errors.add(c.getMessage());
				} else {
					errors.add(c.getPropertyPath() + " " + c.getMessage());
				}
			}

			for (String error : errors) {
				LOGGER.error(error);
			}

			throw new OCSValidationException(errors);
		}

		if (identicalEntityExists(t)) {
			throw new OCSNonUniqueException(
			        messageService.getMessage(getEntityClass().getSimpleName() + ".not.unique"));
		}
	}
}
