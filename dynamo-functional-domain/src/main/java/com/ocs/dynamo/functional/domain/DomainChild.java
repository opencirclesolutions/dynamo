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
