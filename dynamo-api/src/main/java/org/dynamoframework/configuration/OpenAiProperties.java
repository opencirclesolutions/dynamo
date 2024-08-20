package org.dynamoframework.configuration;

public interface OpenAiProperties {
    boolean isEnabled();

    String getApiKey();

    String getModel();

    Integer getMaxTokens();
}
