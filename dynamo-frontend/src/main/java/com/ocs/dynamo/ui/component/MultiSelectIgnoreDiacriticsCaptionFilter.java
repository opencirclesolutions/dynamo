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

import java.util.Objects;


import com.ocs.dynamo.domain.model.EntityModel;
import com.vaadin.flow.component.combobox.ComboBox;

/**
 * A caption filter that ignores diacritical characters when comparing. 
 * 
 * @author bas.rutten
 *
 */
public class MultiSelectIgnoreDiacriticsCaptionFilter<T> extends BaseIgnoreDiacriticsFilter<T> implements ComboBox.ItemFilter<T> {

	private static final long serialVersionUID = -8965855020406086688L;

	/**
	 * Constructor
	 * 
	 * @param ignoreCase      whether to ignore case
	 * @param onlyMatchPrefix whether to only match the prefix
	 */
	public MultiSelectIgnoreDiacriticsCaptionFilter(EntityModel<T> entityModel, boolean ignoreCase,
			boolean onlyMatchPrefix) {
		super(entityModel, ignoreCase, onlyMatchPrefix);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(isIgnoreCase()) + Objects.hashCode(isOnlyMatchPrefix());
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof MultiSelectIgnoreDiacriticsCaptionFilter)) {
			return false;
		}
		MultiSelectIgnoreDiacriticsCaptionFilter<T> other = (MultiSelectIgnoreDiacriticsCaptionFilter<T>) obj;
		return Objects.equals(isIgnoreCase(), other.isIgnoreCase())
				&& Objects.equals(isOnlyMatchPrefix(), other.isOnlyMatchPrefix());
	}

}
