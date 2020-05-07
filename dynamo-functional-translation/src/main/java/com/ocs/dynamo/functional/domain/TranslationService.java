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
package com.ocs.dynamo.functional.domain;

import java.util.List;

import com.ocs.dynamo.service.BaseService;

/**
 * 
 * @author Bas Rutten
 *
 */
public interface TranslationService extends BaseService<Integer, Translation<?>> {

	/**
	 * Fetches the entity ID and translation identified by the provided IDs
	 * 
	 * @param entity the entity which contains the field
	 * @param field  the field for which to fetch the text
	 * @param locale the locale for which to fetch the translated text
	 * @param ids    the IDs of the entities to fetch
	 * @return the id in object[0] and the field in object[1]
	 */
	List<?> fetchByIds(Class<?> entity, String field, String locale, List<Integer> ids);

}
