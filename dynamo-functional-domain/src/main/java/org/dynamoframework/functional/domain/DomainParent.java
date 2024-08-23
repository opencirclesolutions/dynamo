package org.dynamoframework.functional.domain;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
public abstract class DomainParent<C extends DomainChild<C, P>, P extends DomainParent<C, P>> extends Domain implements Serializable {
    private static final long serialVersionUID = 20446010658685722L;
    @OneToMany(
            mappedBy = "parent",
            fetch = FetchType.EAGER,
            cascade = {CascadeType.ALL},
            orphanRemoval = true,
            targetEntity = DomainChild.class
    )
    private Set<C> children = new HashSet<>();

    protected DomainParent(String code, String name) {
        super(code, name);
    }

    //@JsonBackReference("domainparent-domainchild")
    public Set<C> getChildren() {
        return this.children;
    }

    public void setChildren(Set<C> children) {
        this.children = children;
    }

    public DomainParent() {
    }
}
