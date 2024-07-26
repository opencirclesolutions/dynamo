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
package com.ocs.dynamo.service;

import com.ocs.dynamo.domain.AbstractEntity;

import java.util.List;

/**
 * The interface for a service that manages an entity
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T>  the type of the entity
 */
public interface BaseService<ID, T extends AbstractEntity<ID>> extends BaseSearchService<ID, T> {

    /**
     * Deletes all entities in the provided list
     *
     * @param list the list of entities to delete
     */
    void delete(List<T> list);

    /**
     * Deletes the provided entity
     *
     * @param entity the entity to delete
     */
    void delete(T entity);

    /**
     * Returns the class of the entity managed by this DAO
     *
     * @return the entity class
     */
    Class<T> getEntityClass();

    /**
     * Saves the provided list of entities
     *
     * @param list the list of entities to save
     * @return the list of saved entities
     */
    List<T> save(List<T> list);

    /**
     * Saves the provided entity
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    T save(T entity);

    /**
     * Validates the provided entity
     *
     * @param entity the entity to validate
     */
    void validate(T entity);

    /**
     * Deletes all entities
     */
    void deleteAll();

    /**
     * Initializes a new entity
     */
    T initialize();
}
