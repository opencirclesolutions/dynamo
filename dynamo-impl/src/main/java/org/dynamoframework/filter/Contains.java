package org.dynamoframework.filter;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import lombok.Getter;

import java.util.Collection;
import java.util.Objects;

/**
 * A filter that checks if a value is contained in a collection
 *
 * @author bas.rutten
 */
public class Contains extends AbstractFilter implements PropertyFilter {

	@Getter
	private final Object value;

	private final String propertyId;

	/**
	 * @param propertyId the property that represents the collection
	 * @param value      the object that needs to be checked
	 */
	public Contains(String propertyId, Object value) {
		this.propertyId = propertyId;
		this.value = value;
	}

	@Override
	public String getPropertyId() {
		return propertyId;
	}

	@Override
	public boolean evaluate(Object that) {
		if (that == null) {
			return false;
		}
		Object collection = getProperty(that, getPropertyId());
		if (collection != null && Collection.class.isAssignableFrom(collection.getClass())) {
			Collection<?> col = (Collection<?>) collection;
			return col.contains(value);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(value) + Objects.hashCode(propertyId);
	}

	@Override
	public boolean equals(Object obj) {
		// Only objects of the same class can be equal
		if (!(obj instanceof Contains con)) {
			return false;
		}
		return Objects.equals(propertyId, con.getPropertyId()) && Objects.equals(value, con.getValue());
	}

	@Override
	public String toString() {
		return getPropertyId() + " " + super.toString() + " " + getValue();
	}

}
