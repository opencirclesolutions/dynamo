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
package org.dynamoframework.service.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dynamoframework.configuration.DynamoProperties;
import org.dynamoframework.dao.*;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.exception.OCSNonUniqueException;
import org.dynamoframework.exception.OCSValidationException;
import org.dynamoframework.filter.Filter;
import org.dynamoframework.service.BaseService;
import org.dynamoframework.service.MessageService;
import org.dynamoframework.utils.ClassUtils;
import org.dynamoframework.utils.SystemPropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Base service implementation
 *
 * @param <ID> type of the primary key
 * @param <T>  type of the entity
 * @author bas.rutten
 */
@Slf4j
public abstract class BaseServiceImpl<ID, T extends AbstractEntity<ID>> implements BaseService<ID, T> {

    @Autowired
    private DynamoProperties dynamoProperties;

    @Autowired
    private ValidatorFactory factory;

    @Autowired
    @Getter
    private MessageService messageService;

    /**
     * Creates a paging request
     *
     * @param pageNumber the zero-based number of the first page
     * @param pageSize   the page size
     * @param orders     the sort orders
     * @return the request
     */
    private Pageable constructPageRequest(int pageNumber, int pageSize, SortOrder... orders) {
        return new PageableImpl(pageNumber, pageSize, orders);
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
    @Transactional
    public void delete(List<T> list) {
        getDao().delete(list);
    }

    @Override
    @Transactional
    public void delete(T entity) {
        getDao().delete(entity);
    }

    @Override
    public List<T> fetch(Filter filter, FetchJoinInformation... joins) {
        return getDao().fetch(filter, joins);
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

    @Override
    public T fetchById(ID id, FetchJoinInformation... joins) {
        return getDao().fetchById(id, joins);
    }

    @Override
    public List<T> fetchByIds(List<ID> ids, Filter additionalFilter, SortOrders sortOrders,
                              FetchJoinInformation... joins) {
        return getDao().fetchByIds(ids, additionalFilter, sortOrders, joins);
    }

    @Override
    public T fetchByUniqueProperty(String propertyName, Object value, boolean caseSensitive,
                                   FetchJoinInformation... joins) {
        return getDao().fetchByUniqueProperty(propertyName, value, caseSensitive, joins);
    }

    @Override
    public List<T> find(Filter filter, SortOrder... sortOrders) {
        return getDao().find(filter, sortOrders);
    }

    @Override
    public List<T> findAll(SortOrder... orders) {
        return getDao().findAll(orders);
    }

    @Override
    public T findById(ID id) {
        return getDao().findById(id);
    }

    @Override
    public T findByUniqueProperty(String propertyName, Object value, boolean caseSensitive) {
        return getDao().findByUniqueProperty(propertyName, value, caseSensitive);
    }

    @Override
    public <S> List<S> findDistinctValues(Filter filter, String distinctField, Class<S> elementType, SortOrder... orders) {
        return getDao().findDistinctValues(filter, distinctField, elementType, orders);
    }

    /**
     * Looks for an identical entity (which has a different primary key but is otherwise identical)
     * Returns <code>null</code> by default, override in subclasses
     *
     * @param entity the entity
     * @return the identical entity
     */
    protected T findIdenticalEntity(T entity) {
        return null;
    }

    @Override
    public List<ID> findIds(Filter filter, Integer maxResults, SortOrder... orders) {
        return getDao().findIds(filter, maxResults, orders);
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


    /**
     * Checks if there is an entity that is identical to this one. Subclasses must
     * override the findIdenticalEntity method to perform the actual calculation
     *
     * @param entity the entity to check
     * @return true if an identical entity exists, false otherwise
     */
    protected final boolean identicalEntityExists(T entity) {
        T other = findIdenticalEntity(entity);
        return other != null && (entity.getId() == null || !entity.getId().equals(other.getId()));
    }

    protected String message(String key) {
        return messageService.getMessage(key, Locale.getDefault());
    }

    protected String message(String key, Object... args) {
        return messageService.getMessage(key, Locale.getDefault(), args);
    }

    @Override
    @Transactional
    public List<T> save(List<T> list) {
        for (T entity : list) {
            validate(entity);
        }
        return getDao().save(list);
    }

    @Override
    @Transactional
    public T save(T entity) {
        validate(entity);
        return getDao().save(entity);
    }

    /**
     * Validates an entity
     *
     * @param entity the entity to validate
     */
    @Override
    public void validate(T entity) {

        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(entity);

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
            errors.forEach(error -> log.warn(error));

            throw new OCSValidationException(errors);
        }

        if (identicalEntityExists(entity)) {
            throw new OCSNonUniqueException(messageService.getMessage(getEntityClass().getSimpleName() + ".not.unique", dynamoProperties.getDefaults().getLocale()));
        }
    }

    @Override
    @Transactional
    public void deleteAll() {
        getDao().deleteAll();
    }

    public T initialize() {
        return ClassUtils.instantiateClass(getDao().getEntityClass());
    }

}
