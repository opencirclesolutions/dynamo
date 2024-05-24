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
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;
import lombok.experimental.UtilityClass;

/**
 * 
 * @author Bas Rutten
 *
 */
@UtilityClass
public final class SortUtils {

    /**
     * Translates one or more Vaadin sort orders to OCS sort orders
     *
     * @param originalOrders the sort order that must be translated
     * @return the sort orders for which transient is equal to the given isTransient
     *         value
     */
    public static com.ocs.dynamo.dao.SortOrder[] translateSortOrders(SortOrder<?>... originalOrders) {
        if (originalOrders != null && originalOrders.length > 0) {
            final com.ocs.dynamo.dao.SortOrder[] orders = new com.ocs.dynamo.dao.SortOrder[originalOrders.length];
            for (int i = 0; i < originalOrders.length; i++) {
                orders[i] = new com.ocs.dynamo.dao.SortOrder(originalOrders[i].getSorted().toString(),
                        SortDirection.ASCENDING.equals(originalOrders[i].getDirection()) ? Direction.ASC : Direction.DESC);
            }
            return orders;
        }
        return null;
    }

    /**
     * Translates one or more Vaadin sort orders to OCS sort orders
     *
     * @param sortOrders the sort order that must be translated
     * @return the sort orders for which transient is equal to the given isTransient
     *         value
     */
    public static com.ocs.dynamo.dao.SortOrder[] translateSortOrders(List<SortOrder<?>> sortOrders) {
        if (sortOrders == null) {
            return null;
        }
        return translateSortOrders(sortOrders.toArray(new SortOrder[0]));
    }
}
