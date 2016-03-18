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

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;

public class AttributeComparatorTest {

    @Test
    public void test() {

        TestEntity t1 = new TestEntity("bert", 44L);
        TestEntity t2 = new TestEntity("chloe", 33L);
        TestEntity t3 = new TestEntity(null, 33L);

        Assert.assertEquals(0, new AttributeComparator("name").compare(t1, t1));

        Assert.assertEquals(-1, new AttributeComparator("name").compare(t1, t2));
        Assert.assertEquals(1, new AttributeComparator("name").compare(t2, t1));

        // when comparing on age, the result is reversed
        Assert.assertEquals(1, new AttributeComparator("age").compare(t1, t2));
        Assert.assertEquals(-1, new AttributeComparator("age").compare(t2, t1));

        // null value wins
        Assert.assertEquals(1, new AttributeComparator("name").compare(t1, t3));
        Assert.assertEquals(-1, new AttributeComparator("name").compare(t3, t1));
    }

}
