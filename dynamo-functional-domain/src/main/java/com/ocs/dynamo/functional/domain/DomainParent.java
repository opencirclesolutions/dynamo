package com.ocs.dynamo.functional.domain;


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