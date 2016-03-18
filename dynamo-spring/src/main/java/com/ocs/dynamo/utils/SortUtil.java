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
package com.ocs.dynamo.utils;

import com.ocs.dynamo.dao.SortOrder.Direction;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;

public final class SortUtil {

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
                        SortDirection.ASCENDING.equals(originalOrders[i].getDirection())
                                ? Direction.ASC : Direction.DESC,
                        originalOrders[i].getPropertyId().toString());
            }
            return orders;
        }
        return null;
    }
}
