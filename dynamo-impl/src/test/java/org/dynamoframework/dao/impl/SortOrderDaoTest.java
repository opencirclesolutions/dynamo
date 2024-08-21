package org.dynamoframework.dao.impl;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.dynamoframework.dao.SortOrder;
import org.dynamoframework.dao.SortOrder.Direction;
import org.dynamoframework.exception.OCSRuntimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SortOrderDaoTest {

    @Test
    public void testDefaultAscending() {
        SortOrder order = new SortOrder("property");

        assertEquals(Direction.ASC, order.getDirection());
        assertEquals("property", order.getProperty());
    }

    @Test
    public void testPropertyAndDirection() {
        SortOrder order = new SortOrder("property", Direction.DESC);

        assertEquals(Direction.DESC, order.getDirection());
        assertEquals("property", order.getProperty());
    }

    @Test
    public void testPropertyAndDirectionFromString() {
        SortOrder order = new SortOrder("property", Direction.fromString("ASC"));

        assertEquals(Direction.ASC, order.getDirection());
        assertEquals("property", order.getProperty());
    }

    @Test
    public void testPropertyAndDirectionFromString2() {
        SortOrder order = new SortOrder("property", Direction.fromString("DESC"));

        assertEquals(Direction.DESC, order.getDirection());
        assertEquals("property", order.getProperty());
    }

    @Test()
    public void testPropertyAndDirectionFromString_Wrong() {
        assertThrows(OCSRuntimeException.class, () -> new SortOrder("property", Direction.fromString("AS")));
    }

    @Test
    public void testEquals() {
        SortOrder order = new SortOrder("property");
        SortOrder order2 = new SortOrder("property");
        SortOrder order3 = new SortOrder("property", Direction.DESC);

        assertFalse(order.equals(null));
        assertFalse(order.equals(new Object()));
        assertTrue(order.equals(order));
        assertTrue(order.equals(order2));
        assertFalse(order.equals(order3));
    }
}
