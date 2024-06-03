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
package com.ocs.dynamo.filter;

import java.util.Collection;

import com.ocs.dynamo.utils.ClassUtils;

/**
 * A predicate for checking that a value is part of a collection of values
 * 
 * @author Bas Rutten
 *
 * @param <T>
 */
public class InPredicate<T> extends PropertyPredicate<T> {

    private static final long serialVersionUID = -9049178479062352245L;

    public InPredicate(String property, Collection<?> values) {
        super(property, values);
    }

    @Override
    public Collection<?> getValue() {
        return (Collection<?>) super.getValue();
    }

    @Override
    public boolean test(final T t) {
        if (t == null) {
            return false;
        }
        Object value = ClassUtils.getFieldValue(t, getProperty());
        if (value == null) {
            return false;
        }
        Collection<?> values = getValue();
        return values.contains(value);
    }
}
