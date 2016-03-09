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

/**
 * A filter that negates the value of another filter
 * 
 * @author bas.rutten
 */
public final class Not extends AbstractFilter {

    private Filter filter;

    /**
     * Constructor
     * 
     * @param filter
     */
    public Not(Filter filter) {
        this.filter = filter;
    }

    public Filter getFilter() {
        return filter;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        return filter.equals(((Not) obj).getFilter());
    }

    @Override
    public int hashCode() {
        return filter.hashCode();
    }

    @Override
    public boolean evaluate(Object that) {
        return !this.filter.evaluate(that);
    }

    @Override
    public String toString() {
        return super.toString() + " " + getFilter();
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

}
