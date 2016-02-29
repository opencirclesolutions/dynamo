package com.ocs.dynamo.utils;

import com.ocs.dynamo.dao.SortOrder.Direction;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;

public class SortUtil {

	private SortUtil() {
	}

	/**
	 * Translates one or more Vaadin sort orders to OCS sort orders
	 * 
	 * @param originalOrders
	 * @return
	 */
	public static com.ocs.dynamo.dao.SortOrder[] translate(SortOrder... originalOrders) {
		if (originalOrders != null && originalOrders.length > 0) {
			final com.ocs.dynamo.dao.SortOrder[] orders = new com.ocs.dynamo.dao.SortOrder[originalOrders.length];
			for (int i = 0; i < originalOrders.length; i++) {
				orders[i] = new com.ocs.dynamo.dao.SortOrder(
						SortDirection.ASCENDING.equals(originalOrders[i].getDirection()) ? Direction.ASC
								: Direction.DESC, originalOrders[i].getPropertyId().toString());
			}
			return orders;
		}
		return null;
	}
}
