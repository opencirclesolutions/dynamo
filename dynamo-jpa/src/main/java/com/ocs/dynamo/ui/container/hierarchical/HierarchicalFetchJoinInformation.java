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
package com.ocs.dynamo.ui.container.hierarchical;

import javax.persistence.criteria.JoinType;

import com.ocs.dynamo.dao.query.FetchJoinInformation;

public class HierarchicalFetchJoinInformation extends FetchJoinInformation {

    private int level;

    public HierarchicalFetchJoinInformation(int level, String property) {
        super(property);
        this.level = level;
    }

    public HierarchicalFetchJoinInformation(int level, String property, JoinType joinType) {
        super(property, joinType);
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
