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

public class SimpleStringPredicate<T> extends PropertyPredicate<T> {

	private static final long serialVersionUID = -5077087872701525001L;

	/**
	 * Whether to perform case-sensitive search
	 */
	private final boolean caseSensitive;

	/**
	 * Whether to only match the prefix
	 */
	private final boolean onlyMatchPrefix;

	public SimpleStringPredicate(String property, String value, boolean onlyMatchPrefix, boolean caseSensitive) {
		super(property, value);
		this.onlyMatchPrefix = onlyMatchPrefix;
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
		String value = caseSensitive ? v.toString() : v.toString().toLowerCase();
		String match = caseSensitive ? getValue().toString() : getValue().toString().toLowerCase();
		if (onlyMatchPrefix) {
			return value.startsWith(match);
		} else {
			return value.contains(match);
		}
	}

	public boolean isOnlyMatchPrefix() {
		return onlyMatchPrefix;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

}
