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
package com.ocs.dynamo.domain.model.impl;

import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;

/**
 * Interface for entity model factories that provides access to methods during the creation of a new entitymodel by a
 * other entity model factory
 *
 * @author patrickdeenen
 * @author Bas Rutten
 *
 */
interface EntityModelConstruct {

	EntityModel<?> constructNestedEntityModel(EntityModelFactory master, Class<?> type, String reference);
}
