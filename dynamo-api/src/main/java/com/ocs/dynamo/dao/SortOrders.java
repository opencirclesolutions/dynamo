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
package com.ocs.dynamo.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * A list of SortOrder objects
 * 
 * @author bas.rutten
 *
 */
public class SortOrders {

	private List<SortOrder> orders = new ArrayList<>();

	/**
	 * Constructor
	 */
	public SortOrders(SortOrder... orders) {
		if (orders != null) {
			for (SortOrder o : orders) {
				if (o != null && o.getProperty() != null) {
					addSortOrder(o);
				}
			}
		}
	}

	/**
	 * Adds a sort order
	 * 
	 * @param order the sort order to add
	 * @return
	 */
	public SortOrders addSortOrder(SortOrder order) {
		if (order != null && order.getProperty() != null) {
			this.orders.add(order);
		}
		return this;
	}

	/**
	 * Returns the first sort order for the specified property
	 * 
	 * @param property the property
	 * @return
	 */
	public SortOrder getOrderFor(String property) {
		return orders.stream().filter(o -> o.getProperty().equals(property)).findAny().orElse(null);
	}

	public SortOrder[] toArray() {
		return orders.toArray(new SortOrder[0]);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof SortOrders)) {
			return false;
		}

		SortOrders other = (SortOrders) obj;
		if (other.toArray().length != this.toArray().length) {
			return false;
		}

		for (int i = 0; i < toArray().length; i++) {
			SortOrder so1 = toArray()[i];
			SortOrder so2 = other.toArray()[i];
			if (!so1.equals(so2)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
