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

import java.util.Objects;

/**
 * A DTO representing the properties of a fetch join
 * 
 * @author bas.rutten
 */
public class FetchJoinInformation {

    private final String property;

    private final JoinType joinType;

    /**
     * Constructor
     * 
     * @param property the property to use for the fetch
     * @param joinType the desired join type (left, inner etc)
     */
    public FetchJoinInformation(String property, JoinType joinType) {
        this.property = property;
        this.joinType = joinType;
    }

    /**
     * Constructor - defaults to left join
     * 
     * @param property
     */
    public FetchJoinInformation(String property) {
        this(property, JoinType.LEFT);
    }

    public String getProperty() {
        return property;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(property) + Objects.hashCode(joinType);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FetchJoinInformation)) {
            return false;
        }
        FetchJoinInformation other = (FetchJoinInformation) obj;
        return Objects.equals(this.getProperty(), other.getProperty()) && Objects.equals(this.getJoinType(), other.getJoinType());
    }

    /**
     * Creates an array based on the specified vararg
     * 
     * @param joins
     * @return
     */
    public static FetchJoinInformation[] of(FetchJoinInformation... joins) {
        return joins;
    }
}
