package org.dynamoframework.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DynamoPropertiesHolder {

    @Getter
    private static DynamoProperties dynamoProperties;

    @Autowired
    protected void setDynamoProperties(DynamoProperties dynamoProperties) {
        DynamoPropertiesHolder.dynamoProperties = dynamoProperties;
    }
}
