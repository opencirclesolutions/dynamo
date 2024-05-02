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
package com.ocs.dynamo.functional.service;

import java.util.List;

import com.ocs.dynamo.functional.domain.Domain;
import com.ocs.dynamo.functional.domain.DomainChild;
import com.ocs.dynamo.functional.domain.DomainParent;
import com.ocs.dynamo.service.BaseService;

/**
 * Service for working with reference information.
 * 
 * @author Patrick Deenen (patrick@opencircle.solutions)
 *
 */
public interface DomainService extends BaseService<Integer, Domain> {

    /**
     * Query all entities for a specific type (subclass)
     * 
     * @param type The subclass
     * @return All entities of given type
     */
    <D extends Domain> List<D> findAllByType(Class<D> type);

    /**
     * Find a domain entity based on type and unique property value
     * 
     * @param clazz the class of the entity
     * @param propertyName the property value
     * @param value the property value
     * @param caseSensitive whether the comparison is case-sensitive
     * @return the domain value
     */
    <D extends Domain> D findByTypeAndUniqueProperty(Class<D> clazz, String propertyName, Object value, boolean caseSensitive);

    /**
     * Query the children for a given parent
     * 
     * @param parent the parent entity
     * @return the children for the given parent
     */
    <C extends DomainChild<C, P>, P extends DomainParent<C, P>> List<C> findChildren(P parent);
}
