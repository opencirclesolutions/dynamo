package org.dynamoframework.service;

import lombok.Getter;
import lombok.Setter;
import org.dynamoframework.domain.AbstractEntity;

@Getter
@Setter
public class TestEntityDTO extends AbstractEntity<Integer> {

    @Override
    public Integer getId() {
        return null;
    }

    @Override
    public void setId(Integer integer) {

    }

    private String name;

    private Integer age;
}
