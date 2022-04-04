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
package com.ocs.dynamo.ui.component;

import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.utils.EntityModelUtils;
import com.ocs.dynamo.utils.StringUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 
 * @author BasRutten
 *
 * @param <T> the entity class to search on
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseIgnoreDiacriticsFilter<T> {

	@EqualsAndHashCode.Include
	@Getter
	private final boolean ignoreCase;

	@EqualsAndHashCode.Include
	@Getter
	private final boolean onlyMatchPrefix;

	private final EntityModel<T> entityModel;

	/**
	 * Constructor
	 * 
	 * @param entityModel
	 * @param ignoreCase
	 * @param onlyMatchPrefix
	 */
	protected BaseIgnoreDiacriticsFilter(EntityModel<T> entityModel, boolean ignoreCase, boolean onlyMatchPrefix) {
		this.ignoreCase = ignoreCase;
		this.onlyMatchPrefix = onlyMatchPrefix;
		this.entityModel = entityModel;
	}

	public boolean test(T item, String filterText) {
		// replace any diacritical characters
		if (item == null) {
			return false;
		}

		String temp = entityModel == null ? item.toString()
				: EntityModelUtils.getDisplayPropertyValue(item, entityModel);

		temp = ignoreCase ? temp.toLowerCase() : temp;
		filterText = ignoreCase ? filterText.toLowerCase() : filterText;

		temp = StringUtils.removeAccents(temp);

		return onlyMatchPrefix ? temp.startsWith(filterText) : temp.contains(filterText);
	}

}
