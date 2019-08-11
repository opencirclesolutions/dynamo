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
package com.ocs.dynamo.domain.query;

import java.io.Serializable;
import java.util.List;

import com.ocs.dynamo.domain.AbstractEntity;

/**
 * A simple iterator for iterating over a fixed set of data - this basically
 * just delegates to the underlying collection
 * 
 * @author Bas Rutten
 *
 * @param <ID>
 * @param <T>
 */
public class FixedDataSetIterator<ID extends Serializable, T extends AbstractEntity<ID>> implements DataSetIterator<ID, T> {

    private List<T> items;

    private int index = 0;

    public FixedDataSetIterator(List<T> items) {
        this.items = items;
    }

    @Override
    public T next() {
        if (index < items.size()) {
            return items.get(index++);
        }
        return null;
    }

    @Override
    public int size() {
        return items.size();
    }

}
