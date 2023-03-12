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
import java.util.Optional;

import lombok.ToString;

/**
 * A list of SortOrder objects
 * 
 * @author bas.rutten
 *
 */
@ToString
public class SortOrders {

	private List<SortOrder> orders = new ArrayList<>();

	/**
	 * Constructor
	 * @param orders the sort orders that must be used
	 */
	public SortOrders(SortOrder... orders) {
		if (orders != null) {
			for (SortOrder order : orders) {
				if (order != null && order.getProperty() != null) {
					addSortOrder(order);
				}
			}
		}
	}

	/**
	 * Adds a sort order
	 * 
	 * @param order the sort order that must be added
	 * @return the current list of sort orders
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
	 * @return an Optional containing the sort order
	 */
	public Optional<SortOrder> getOrderFor(String property) {
		return orders.stream().filter(o -> o.getProperty().equals(property)).findFirst();
	}

	public SortOrder[] toArray() {
		return orders.toArray(new SortOrder[0]);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof SortOrders other)) {
			return false;
		}

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

	public List<SortOrder> getOrders() {
		return orders;
	}

	public void setOrders(List<SortOrder> orders) {
		this.orders = orders;
	}

	public int getNrOfSortOrders() {
		return orders.size();
	}
}
