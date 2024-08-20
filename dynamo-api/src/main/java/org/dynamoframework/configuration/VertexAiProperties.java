package org.dynamoframework.configuration;

public interface VertexAiProperties {
    boolean isEnabled();

    String getProjectId();

    String getProjectRegion();

    String getModel();
}
