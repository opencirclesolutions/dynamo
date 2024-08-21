package com.example.domain;

import org.dynamoframework.domain.AbstractEntity;

/**
 * An entity class outside package <code>org.dynamoframework</code> to use in <code>ClassUtilsTest.testFindClass()</code>.
 */
public class ExternalEntity extends AbstractEntity {
    @Override
    public Object getId() {
        return null;
    }

    @Override
    public void setId(Object o) {

    }
}
