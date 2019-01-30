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

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.dao.SortOrder.Direction;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;

public class SortUtilTest {

	@Test
	public void testTranslateSortOrders() {

		com.ocs.dynamo.dao.SortOrder[] orders = SortUtils
				.translateSortOrders(new SortOrder<String>("test1", SortDirection.ASCENDING));
		Assert.assertEquals(1, orders.length);

		Assert.assertEquals("test1", orders[0].getProperty());
		Assert.assertEquals(Direction.ASC, orders[0].getDirection());

		orders = SortUtils.translateSortOrders(new SortOrder<String>("test2", SortDirection.DESCENDING));
		Assert.assertEquals(1, orders.length);

		Assert.assertEquals("test2", orders[0].getProperty());
		Assert.assertEquals(Direction.DESC, orders[0].getDirection());
	}
}
