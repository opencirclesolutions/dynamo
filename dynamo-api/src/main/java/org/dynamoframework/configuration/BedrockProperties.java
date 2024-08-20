package org.dynamoframework.configuration;

public interface BedrockProperties {
    boolean isEnabled();

    String getAccessKey();

    String getAccessSecret();

    String getModelId();

    String getRegion();
}
