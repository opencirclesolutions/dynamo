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
package com.ocs.dynamo.dao;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.query.DataSetIterator;
import com.ocs.dynamo.domain.TestEntity;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DataSetIteratorTest {

    private DataSetIterator<Integer, TestEntity> iterator;

    private List<Integer> ids = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    private List<Integer> ids2 = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);

    private int pagesRead = 0;

    @Test
    public void test() {

        iterator = new DataSetIterator<Integer, TestEntity>(ids, 5) {

            @Override
            protected List<TestEntity> readPage(List<Integer> ids) {
                List<TestEntity> result = new ArrayList<>();
                for (Integer i : ids) {
                    TestEntity entity = new TestEntity();
                    entity.setId(i);
                    result.add(entity);
                }
                pagesRead++;
                return result;
            }
        };
        int i = 0;
        TestEntity entity = null;
        do {
            entity = iterator.next();
            if (entity != null) {
                i++;
            }
        } while (entity != null);

        Assert.assertEquals(10, i);
        Assert.assertEquals(2, pagesRead);
    }

    @Test
    public void testPartial() {

        iterator = new DataSetIterator<Integer, TestEntity>(ids2, 5) {

            @Override
            protected List<TestEntity> readPage(List<Integer> ids) {
                List<TestEntity> result = new ArrayList<>();
                for (Integer i : ids) {
                    TestEntity entity = new TestEntity();
                    entity.setId(i);
                    result.add(entity);
                }
                pagesRead++;
                return result;
            }
        };
        int i = 0;
        TestEntity entity = null;
        do {
            entity = iterator.next();
            if (entity != null) {
                i++;
            }
        } while (entity != null);

        Assert.assertEquals(12, i);
        Assert.assertEquals(3, pagesRead);
    }
}
