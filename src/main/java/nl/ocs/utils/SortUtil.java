package nl.ocs.utils;

import nl.ocs.dao.SortOrder.Direction;

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
	public static nl.ocs.dao.SortOrder[] translate(SortOrder... originalOrders) {
		if (originalOrders != null && originalOrders.length > 0) {
			final nl.ocs.dao.SortOrder[] orders = new nl.ocs.dao.SortOrder[originalOrders.length];
			for (int i = 0; i < originalOrders.length; i++) {
				orders[i] = new nl.ocs.dao.SortOrder(
						SortDirection.ASCENDING.equals(originalOrders[i].getDirection()) ? Direction.ASC
								: Direction.DESC, originalOrders[i].getPropertyId().toString());
			}
			return orders;
		}
		return null;
	}
}
