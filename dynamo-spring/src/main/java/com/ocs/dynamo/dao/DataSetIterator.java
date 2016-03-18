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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ocs.dynamo.domain.AbstractEntity;

/**
 * An iterator for traversing large data sets without loading them all into memory
 * 
 * @author bas.rutten
 * @param <ID>
 * @param <T>
 */
public abstract class DataSetIterator<ID extends Serializable, T extends AbstractEntity<ID>> {

    private static final int PAGE_SIZE = 1000;

    private List<ID> idList;

    private List<T> page;

    // the overal index
    private int index;

    // the index of the first record in the latest page that was read
    private int lastRead;

    // the index in the current page
    private int indexInPage;

    private int pageSize;

    public DataSetIterator(List<ID> idList) {
        this(idList, PAGE_SIZE);
    }

    public DataSetIterator(List<ID> idList, int pageSize) {
        this.idList = idList;
        index = 0;
        lastRead = 0;
        indexInPage = 0;
        this.pageSize = pageSize;
    }

    public T next() {
        if (index > idList.size()) {
            return null;
        }

        // lazily load the next page if needed
        if (index >= lastRead) {
            List<ID> ids = new ArrayList<>();
            for (int i = 0; i < pageSize && index + i < idList.size(); i++) {
                ids.add(idList.get(index + i));
            }

            if (!ids.isEmpty()) {
                page = readPage(ids);
            } else {
                page = new ArrayList<>();
            }

            lastRead = index + ids.size();
            indexInPage = 0;
        }

        if (indexInPage < page.size()) {
            T t = page.get(indexInPage);
            index++;
            indexInPage++;
            return t;
        }
        return null;
    }

    /**
     * Returns a page of data given a list of IDs
     * 
     * @param ids
     * @return
     */
    protected abstract List<T> readPage(List<ID> ids);
}
