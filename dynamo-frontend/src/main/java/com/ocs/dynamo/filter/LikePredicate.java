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
package com.ocs.dynamo.filter;

import com.ocs.dynamo.utils.ClassUtils;

/**
 * A predicate for checking whether a String value matches a predicate
 * 
 * @author Bas Rutten
 *
 * @param <T> the type of the entity
 */
public class LikePredicate<T> extends PropertyPredicate<T> {

	private static final long serialVersionUID = -5077087872701525001L;

	private boolean caseSensitive;

	public LikePredicate(String property, String value, boolean caseSensitive) {
		super(property, value);
		this.caseSensitive = caseSensitive;
	}

	@Override
	public boolean test(T t) {
		if (t == null) {
			return false;
		}

		Object v = ClassUtils.getFieldValue(t, getProperty());
		if (v == null || !v.getClass().isAssignableFrom(String.class)) {
			return false;
		}
		String pattern = getValue().toString().replace("%", ".*");
		if (isCaseSensitive()) {
			return ((String) v).matches(pattern);
		}
		return ((String) v).toUpperCase().matches(pattern.toUpperCase());
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

}
