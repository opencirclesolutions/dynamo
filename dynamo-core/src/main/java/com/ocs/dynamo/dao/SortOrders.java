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
	 * @param order
	 *            the sort order to add
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
	 * @param property
	 *            the property
	 * @return
	 */
	public SortOrder getOrderFor(String property) {
		for (SortOrder order : orders) {
			if (order.getProperty().equals(property)) {
				return order;
			}
		}
		return null;
	}

	public SortOrder[] toArray() {
		return orders.toArray(new SortOrder[0]);
	}
}
