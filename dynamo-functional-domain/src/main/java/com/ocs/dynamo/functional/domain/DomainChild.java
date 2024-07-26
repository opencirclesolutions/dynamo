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
package com.ocs.dynamo.functional.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;

@Entity
public abstract class DomainChild<C extends DomainChild<C, P>, P extends DomainParent<C, P>> extends Domain implements Serializable {
    private static final long serialVersionUID = 2615942460028599211L;
    @ManyToOne(
            fetch = FetchType.LAZY,
            targetEntity = DomainParent.class
    )
    @JoinColumn(
            name = "parent"
    )
    private P parent;

    protected DomainChild(String code, String name) {
        super(code, name);
    }

    public P getParent() {
        return this.parent;
    }

    public void setParent(P parent) {
        this.parent = parent;
    }

    protected DomainChild() {
    }
}
