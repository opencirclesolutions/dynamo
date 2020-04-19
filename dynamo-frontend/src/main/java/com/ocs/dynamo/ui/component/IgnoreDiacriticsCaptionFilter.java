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

import java.text.Normalizer;
import java.util.Objects;

import com.vaadin.flow.component.combobox.ComboBox.ItemFilter;

/**
 * A caption filter that ignores diacritic characters when comparing
 * 
 * @author bas.rutten
 *
 */
public class IgnoreDiacriticsCaptionFilter<T> implements ItemFilter<T> {

	private static final long serialVersionUID = -8965855020406086688L;

	private final boolean ignoreCase;

	private final boolean onlyMatchPrefix;

	/**
	 * Constructor
	 * 
	 * @param propertyId      the name of the property to filter on
	 * @param filterString    the filter string
	 * @param ignoreCase      whether to ignore case
	 * @param onlyMatchPrefix whether to only match the prefix
	 */
	public IgnoreDiacriticsCaptionFilter(boolean ignoreCase, boolean onlyMatchPrefix) {
		this.ignoreCase = ignoreCase;
		this.onlyMatchPrefix = onlyMatchPrefix;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(ignoreCase) + Objects.hashCode(onlyMatchPrefix);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof IgnoreDiacriticsCaptionFilter)) {
			return false;
		}
		IgnoreDiacriticsCaptionFilter<T> o = (IgnoreDiacriticsCaptionFilter<T>) obj;
		return Objects.equals(ignoreCase, o.ignoreCase) && Objects.equals(onlyMatchPrefix, o.onlyMatchPrefix);
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public boolean isOnlyMatchPrefix() {
		return onlyMatchPrefix;
	}

	@Override
	public boolean test(T item, String filterText) {
		// replace any diacritical characters
		String t = item == null ? "" : item.toString();
		String temp = ignoreCase ? t.toLowerCase() : t.toString();
		filterText = ignoreCase ? filterText.toLowerCase() : filterText;

		temp = Normalizer.normalize(temp, Normalizer.Form.NFD);
		temp = temp.replaceAll("[^\\p{ASCII}]", "");

		return onlyMatchPrefix ? temp.startsWith(filterText) : temp.contains(filterText);
	}

}
