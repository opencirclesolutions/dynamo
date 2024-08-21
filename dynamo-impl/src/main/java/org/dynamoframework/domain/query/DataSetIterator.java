package org.dynamoframework.domain.query;

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

import org.dynamoframework.domain.AbstractEntity;

import java.io.Serializable;

/**
 * Interface for an iterator that iterators over a set of entities using paging
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T>  the type of the entity
 */
public interface DataSetIterator<ID extends Serializable, T extends AbstractEntity<ID>> {

    /**
     * Returns the next entity
     * 
     * @return the next entity in the iterator, or <code>null</code> if there is none
     */
    T next();

    /**
     * Returns the total number of entities
     * 
     * @return the total number of entities in the iterator
     */
    int size();
}
