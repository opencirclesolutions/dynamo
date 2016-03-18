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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class SubListTest {

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubListInvalid1() {
        List<Integer> list = Lists.newArrayList(1, 2, 3, 4, 5, 6);
        new SubList<>(list, -1, 3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubListInvalid2() {
        List<Integer> list = Lists.newArrayList(1, 2, 3, 4, 5, 6);
        new SubList<>(list, 0, 7);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubListInvalid3() {
        List<Integer> list = Lists.newArrayList(1, 2, 3, 4, 5, 6);
        new SubList<>(list, 2, 1);
    }

    @Test
    public void testSubList() {
        List<Integer> list = Lists.newArrayList(1, 2, 3, 4, 5, 6);

        // creation
        SubList<Integer> subList = new SubList<>(list, 0, 3);
        Assert.assertEquals(3, subList.size);

        // setting
        subList.set(0, 4);
        Assert.assertEquals(4, subList.get(0).intValue());

        // setting a non existent field
        try {
            subList.set(4, 4);
            Assert.fail();
        } catch (Exception ex) {
            // this is expected
        }

        // adding
        subList.add(5);
        Assert.assertEquals(4, subList.size());

        // creating a sublist from a sublist
        List<Integer> subList2 = subList.subList(0, 1);
        Assert.assertEquals(1, subList2.size());

        Iterator<Integer> it = subList.iterator();
        Assert.assertTrue(it.hasNext());

        // iterate over the list
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        Assert.assertEquals(4, count);

        // iterate over the list and empty it
        it = subList.iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
        Assert.assertEquals(0, subList.size());

    }

    @Test
    public void testAddAll() {
        List<Integer> list = Lists.newArrayList(1, 2, 3, 4, 5, 6);
        SubList<Integer> subList = new SubList<>(list, 0, 3);

        // adding multiple items
        subList.addAll(Lists.newArrayList(4, 5, 6));
        Assert.assertEquals(6, subList.size());

        List<Integer> empty = Collections.emptyList();
        Assert.assertFalse(subList.addAll(empty));

        subList.addAll(1, Lists.newArrayList(7, 8, 9));
        Assert.assertEquals(9, subList.size());
        Assert.assertEquals(7, subList.get(1).intValue());
    }

    @Test
    public void testRemove() {
        List<Integer> list = Lists.newArrayList(1, 2, 3, 4, 5, 6);
        SubList<Integer> subList = new SubList<>(list, 0, 3);

        // adding multiple items
        Integer result = subList.remove(0);
        Assert.assertEquals(1, result.intValue());
        Assert.assertEquals(2, subList.size());
    }
}
