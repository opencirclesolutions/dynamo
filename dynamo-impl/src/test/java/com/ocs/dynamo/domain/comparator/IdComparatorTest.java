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

public class IdComparatorTest {

    @Test
    public void test() {
        TestEntity t1 = new TestEntity("name", 12L);
        t1.setId(4);

        TestEntity t2 = new TestEntity("name", 12L);
        t2.setId(3);

        TestEntity t3 = new TestEntity("name", 12L);
        t3.setId(null);

        assertEquals(0, new IdComparator().compare(t1, t1));
        assertEquals(1, new IdComparator().compare(t1, t2));
        assertEquals(-1, new IdComparator().compare(t2, t1));

        assertEquals(-1, new IdComparator().compare(t3, t1));
        assertEquals(1, new IdComparator().compare(t1, t3));

    }

}
