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

import com.vaadin.flow.function.SerializablePredicate;

import lombok.Builder;
import lombok.Getter;

/**
 * An event used to indicate that the value of a filter in a search form has
 * changed
 * 
 * @author bas.rutten
 */
@Getter
public final class FilterChangeEvent<T> implements Serializable {

	private static final long serialVersionUID = 7833584773075924736L;

	/**
	 * The property to search on
	 */
	private final String propertyId;

	/**
	 * The old filter value
	 */
	private final SerializablePredicate<T> oldFilter;

	/**
	 * The new filter value
	 */
	private final SerializablePredicate<T> newFilter;

	/**
	 * Constructor
	 * 
	 * @param propertyId the name of the property
	 * @param oldFilter  the old filter
	 * @param newFilter  the new filter
	 */
	@Builder
	private FilterChangeEvent(String propertyId, SerializablePredicate<T> oldFilter,
			SerializablePredicate<T> newFilter) {
		this.propertyId = propertyId;
		this.oldFilter = oldFilter;
		this.newFilter = newFilter;
	}

}
