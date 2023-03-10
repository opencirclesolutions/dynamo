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

import java.util.Comparator;

import com.ocs.dynamo.domain.AbstractEntity;

/**
 * A comparator for comparing entities based on their IDs (entities without
 * IDs are returned first)
 * 
 * @author bas.rutten
 */
public class IdComparator implements Comparator<AbstractEntity<Integer>> {

    @Override
    public int compare(AbstractEntity<Integer> o1, AbstractEntity<Integer> o2) {
        if (o1.getId() == null) {
            return -1;
        }
        if (o2.getId() == null) {
            return 1;
        }
        return o1.getId().compareTo(o2.getId());
    }

}
