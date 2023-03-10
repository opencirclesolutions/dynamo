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
import com.ocs.dynamo.utils.ClassUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A comparator for comparing two entities based on a value of an attribute (using reflection)
 * 
 * @author bas.rutten
 */
@Getter
@AllArgsConstructor
public class AttributeComparator<T extends AbstractEntity<?>> implements Comparator<T> {

    private final String attribute;

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public int compare(T o1, T o2) {
        Object v1 = ClassUtils.getFieldValue(o1, attribute);
        Object v2 = ClassUtils.getFieldValue(o2, attribute);

        if (v1 == null && v2 == null) {
            return 0;
        } else if (v1 == null) {
            return -1;
        } else if (v2 == null) {
            return 1;
        } else if (v1 instanceof String str1) {
            return str1.compareToIgnoreCase((String) v2);
        } else if (v1 instanceof Comparable comp) {
            return comp.compareTo(v2);
        }
        return 0;
    }
}
