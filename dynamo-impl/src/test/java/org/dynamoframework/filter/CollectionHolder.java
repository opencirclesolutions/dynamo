package org.dynamoframework.filter;

import org.dynamoframework.domain.TestEntity;

import java.util.HashSet;
import java.util.Set;

public class CollectionHolder {

    private Set<TestEntity> entities = new HashSet<>();

    private Integer other;

    public Set<TestEntity> getEntities() {
        return entities;
    }

    public void setEntities(Set<TestEntity> entities) {
        this.entities = entities;
    }

    public Integer getOther() {
        return other;
    }

    public void setOther(Integer other) {
        this.other = other;
    }

}
