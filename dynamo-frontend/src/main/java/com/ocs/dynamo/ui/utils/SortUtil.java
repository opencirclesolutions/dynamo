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

import java.util.ArrayList;

import com.ocs.dynamo.dao.SortOrder.Direction;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.vaadin.data.Container;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.AbstractBeanContainer;
import com.vaadin.shared.data.sort.SortDirection;

public final class SortUtil {

    private SortUtil() {
    }

    /**
	 * Translates one or more non transient Vaadin sort orders to OCS sort orders
	 * 
	 * @param originalOrders
	 * @return non transient sortorders which can be applied to the service
	 */
    public static com.ocs.dynamo.dao.SortOrder[] translate(SortOrder... originalOrders) {
		return translateAndFilterOnTransient(false, null, originalOrders);
	}

	/**
	 * Translates one or more (non) transient Vaadin sort orders to OCS sort orders
	 * 
	 * @param isTransient
	 * @param model
	 * @param originalOrders
	 * @return the sort orders for which transient is equal to the given isTransient value
	 */
	public static <T> com.ocs.dynamo.dao.SortOrder[] translateAndFilterOnTransient(Boolean isTransient,
			EntityModel<T> model, SortOrder... originalOrders) {
        if (originalOrders != null && originalOrders.length > 0) {
            final com.ocs.dynamo.dao.SortOrder[] orders = new com.ocs.dynamo.dao.SortOrder[originalOrders.length];
			AttributeModel am = null;
			for (int i = 0; i < originalOrders.length; i++) {
				if (model != null) {
					am = model.getAttributeModel(originalOrders[i].getPropertyId().toString());
				}
				if (am == null || isTransient == null || am.isTransient() == isTransient) {
					orders[i] = new com.ocs.dynamo.dao.SortOrder(
							SortDirection.ASCENDING.equals(originalOrders[i].getDirection()) ? Direction.ASC
									: Direction.DESC,
							originalOrders[i].getPropertyId().toString());
				}
			}
            return orders;
        }
        return null;
    }

	/**
	 * Adds sort order to container optionally with only the (non)transient properties when isTransient is supplied
	 * 
	 * @param container
	 * @param isTransient
	 * @param model
	 * @param originalOrders
	 */
	public static <T> void applyContainerSortOrder(Container.Sortable container, Boolean isTransient,
			EntityModel<T> model,
			SortOrder... originalOrders) {
		if (originalOrders != null && originalOrders.length > 0) {
			ArrayList<SortOrder> fo = new ArrayList<>();
			AttributeModel am = null;
			for (int i = 0; i < originalOrders.length; i++) {
				if (model != null) {
					am = model.getAttributeModel(originalOrders[i].getPropertyId().toString());
				}
				if (am == null || isTransient == null || am.isTransient() == isTransient) {
					fo.add(originalOrders[i]);
				}
			}
			if (!fo.isEmpty()) {
				Object[] propertyIds = new Object[fo.size()];
				boolean[] asc = new boolean[fo.size()];
				int i = 0;
				for (SortOrder so : fo) {
					propertyIds[i] = so.getPropertyId();
					asc[i] = SortDirection.ASCENDING.equals(so.getDirection());
					if (container instanceof AbstractBeanContainer
							&& i < propertyIds.length
							&& !container.getContainerPropertyIds().contains(propertyIds[i])) {
						((AbstractBeanContainer) container).addNestedContainerProperty(propertyIds[i].toString());
					}
					i++;
				}
				container.sort(propertyIds, asc);
			}
		}
	}
}
