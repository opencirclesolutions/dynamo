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
package org.dynamoframework.filter;

import java.util.Objects;

/**
 * A filter for checking if a string value contains a certain pattern. Use
 * percent signs ("%") to denote wildcards
 * 
 * @author bas.rutten
 */
public class Like extends AbstractFilter implements PropertyFilter {

	private final String propertyId;

	private final String value;

	private final boolean caseSensitive;

	public Like(String propertyId, String value) {
		this(propertyId, value, true);
	}

	public Like(String propertyId, String value, boolean caseSensitive) {
		this.propertyId = propertyId;
		this.value = value;
		this.caseSensitive = caseSensitive;
	}

	@Override
	public String getPropertyId() {
		return propertyId;
	}

	public String getValue() {
		return value;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	@Override
	public boolean evaluate(Object that) {
		if (that == null) {
			return false;
		}
		Object val = getProperty(that, getPropertyId());
		if (val == null) {
			return false;
		}
		if (!val.getClass().isAssignableFrom(String.class)) {
			return false;
		}
		String pattern = getValue().replace("%", ".*");
		if (isCaseSensitive()) {
			return ((String) val).matches(pattern);
		}
		return ((String) val).toUpperCase().matches(pattern.toUpperCase());
	}

	@Override
	public int hashCode() {
		return getPropertyId().hashCode() + getValue().hashCode() + (caseSensitive ? 0 : 1);
	}

	@Override
	public boolean equals(Object obj) {
		// Only objects of the same class can be equal
		if (!(obj instanceof Like)) {
			return false;
		}
		Like o = (Like) obj;
		return Objects.equals(propertyId, o.getPropertyId()) && Objects.equals(value, o.getValue())
				&& Objects.equals(caseSensitive, o.isCaseSensitive());
	}

	@Override
	public String toString() {
		return getPropertyId() + " " + super.toString() + " " + getValue()
				+ (isCaseSensitive() ? "" : " (ignore case)");
	}

}
