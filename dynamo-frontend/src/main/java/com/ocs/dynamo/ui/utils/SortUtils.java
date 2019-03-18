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
package com.ocs.dynamo.ui.utils;

import java.util.List;

import com.ocs.dynamo.dao.SortOrder.Direction;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;

/**
 * 
 * @author Bas Rutten
 *
 */
public final class SortUtils {

	private SortUtils() {
	}

	/**
	 * Translates one or more (non) transient Vaadin sort orders to OCS sort orders
	 * 
	 * @param model
	 * @param originalOrders
	 * @return the sort orders for which transient is equal to the given isTransient
	 *         value
	 */
	@SafeVarargs
	public static com.ocs.dynamo.dao.SortOrder[] translateSortOrders(SortOrder<?>... originalOrders) {
		if (originalOrders != null && originalOrders.length > 0) {
			final com.ocs.dynamo.dao.SortOrder[] orders = new com.ocs.dynamo.dao.SortOrder[originalOrders.length];
			for (int i = 0; i < originalOrders.length; i++) {
				orders[i] = new com.ocs.dynamo.dao.SortOrder(
						SortDirection.ASCENDING.equals(originalOrders[i].getDirection()) ? Direction.ASC
								: Direction.DESC,
						originalOrders[i].getSorted().toString());
			}
			return orders;
		}
		return null;
	}

	/**
	 * 
	 * @param sortOrders
	 * @return
	 */
	public static com.ocs.dynamo.dao.SortOrder[] translateSortOrders(List<SortOrder<?>> sortOrders) {
		if (sortOrders == null) {
			return null;
		}
		return translateSortOrders(sortOrders.toArray(new SortOrder[0]));
	}
}
