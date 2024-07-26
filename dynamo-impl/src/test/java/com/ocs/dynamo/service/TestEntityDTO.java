package com.ocs.dynamo.service;

import com.ocs.dynamo.domain.AbstractEntity;
import lombok.Getter;
import lombok.Setter;

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
