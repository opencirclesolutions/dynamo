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
package com.ocs.dynamo.filter.listener;

import java.io.Serializable;

import com.vaadin.server.SerializablePredicate;
/**
 * An event used to indicate that the value of a filter in a search form has
 * changed
 * 
 * @author bas.rutten
 */
public class FilterChangeEvent implements Serializable {

	private static final long serialVersionUID = 7833584773075924736L;

	private final String propertyId;

	private final SerializablePredicate<?> oldFilter;

	private final SerializablePredicate<?> newFilter;

	private final transient Object value;

	/**
	 * Constructor
	 * 
	 * @param propertyId
	 *            the name of the property
	 * @param oldFilter
	 *            the old filter
	 * @param newFilter
	 *            the new filter
	 * @param value
	 *            the new value
	 */
	public FilterChangeEvent(String propertyId, SerializablePredicate<?> oldFilter, SerializablePredicate<?> newFilter,
			Object value) {
		this.propertyId = propertyId;
		this.oldFilter = oldFilter;
		this.newFilter = newFilter;
		this.value = value;
	}

	public SerializablePredicate<?> getOldFilter() {
		return oldFilter;
	}

	public SerializablePredicate<?> getNewFilter() {
		return newFilter;
	}

	public String getPropertyId() {
		return propertyId;
	}

	public Object getValue() {
		return value;
	}
}
