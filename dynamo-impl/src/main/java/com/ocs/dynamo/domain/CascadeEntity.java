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
package com.ocs.dynamo.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.ocs.dynamo.domain.model.CascadeMode;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.Cascade;
import com.ocs.dynamo.domain.model.annotation.SearchMode;

@Entity
public class CascadeEntity extends AbstractEntity<Integer> {

    private static final long serialVersionUID = 9196343420168206197L;

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @Attribute(cascade = @Cascade(cascadeTo = "testEntity2", filterPath = "testEntity", mode = CascadeMode.BOTH), searchable = SearchMode.ALWAYS)
    private TestEntity testEntity;

    @ManyToOne
    @Attribute(searchable = SearchMode.ALWAYS)
    private TestEntity2 testEntity2;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public TestEntity getTestEntity() {
        return testEntity;
    }

    public void setTestEntity(TestEntity testEntity) {
        this.testEntity = testEntity;
    }

    public TestEntity2 getTestEntity2() {
        return testEntity2;
    }

    public void setTestEntity2(TestEntity2 testEntity2) {
        this.testEntity2 = testEntity2;
    }

}
