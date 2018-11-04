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
package com.ocs.dynamo.ui;

import com.ocs.dynamo.domain.AbstractEntity;

/**
 * Marker interface for components that support the direct setting of an entity
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the primary key of the entity
 * @param <T>
 *            the type of the entity
 */
@FunctionalInterface
public interface CanAssignEntity<ID, T extends AbstractEntity<ID>> {

	/**
	 * Assigns the new entity to the component
	 * @param t the entity to assignF
	 */
    void assignEntity(T t);
}
