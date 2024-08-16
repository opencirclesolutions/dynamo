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
package org.dynamoframework.filter;

import java.util.Collection;
import java.util.Objects;

/**
 * A filter for testing that the value of a certain property is included in a
 * collection of values
 * 
 * @author bas.rutten
 */
public class In extends AbstractFilter implements PropertyFilter {

    private final Collection<?> values;

    private final String propertyId;

    /**
     * @param propertyId the property that represents the collection
     * @param values     the objects that needs to be checked
     */
    public In(String propertyId, Collection<?> values) {
        this.propertyId = propertyId;
        this.values = values;
    }

    @Override
    public String getPropertyId() {
        return propertyId;
    }

    public Collection<?> getValues() {
        return values;
    }

    @Override
    public boolean evaluate(Object that) {
        if (that == null) {
            return false;
        }
        Object other = getProperty(that, getPropertyId());
        if (other == null) {
            return false;
        }

        return values.contains(other);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(values) + Objects.hashCode(propertyId);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof In in)) {
            return false;
        }

        return Objects.equals(propertyId, in.getPropertyId()) && Objects.equals(values, in.getValues());
    }

    @Override
    public String toString() {
        return getPropertyId() + " " + super.toString() + " " + getValues();
    }
}
