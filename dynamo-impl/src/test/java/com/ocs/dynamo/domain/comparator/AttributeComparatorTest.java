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
package com.ocs.dynamo.domain.comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.TestEntity;

public class AttributeComparatorTest {

    @Test
    public void test() {

        TestEntity t1 = new TestEntity("bert", 44L);
        TestEntity t2 = new TestEntity("chloe", 33L);
        TestEntity t3 = new TestEntity(null, 33L);

        assertEquals(0, new AttributeComparator<TestEntity>("name").compare(t1, t1));

        assertEquals(-1, new AttributeComparator<TestEntity>("name").compare(t1, t2));
        assertEquals(1, new AttributeComparator<TestEntity>("name").compare(t2, t1));

        // when comparing on age, the result is reversed
        assertEquals(1, new AttributeComparator<TestEntity>("age").compare(t1, t2));
        assertEquals(-1, new AttributeComparator<TestEntity>("age").compare(t2, t1));

        // null value wins
        assertEquals(1, new AttributeComparator<TestEntity>("name").compare(t1, t3));
        assertEquals(-1, new AttributeComparator<TestEntity>("name").compare(t3, t1));
    }

}
