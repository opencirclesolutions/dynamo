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
package com.ocs.dynamo.domain;

import org.junit.Assert;
import org.junit.Test;

public class AbstractEntityTest {

    @Test
    public void testEquals() {
        TestEntity e1 = new TestEntity();
        e1.setId(1);

        Assert.assertFalse(e1.equals(null));
        Assert.assertFalse(e1.equals(new Integer(14)));
        Assert.assertTrue(e1.equals(e1));

        // objects with the same ID are equal
        TestEntity e2 = new TestEntity();
        e2.setId(1);
        Assert.assertTrue(e1.equals(e2));

        // IDs not equal
        TestEntity e3 = new TestEntity();
        Assert.assertFalse(e1.equals(e3));

        TestEntity e4 = new TestEntity();
        e4.setId(2);
        Assert.assertFalse(e1.equals(e4));
    }
}
