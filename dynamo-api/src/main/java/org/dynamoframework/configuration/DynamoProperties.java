package org.dynamoframework.configuration;

public interface DynamoProperties {

    boolean isCapitalizePropertyNames();

    DefaultProperties getDefaults();

    CsvProperties getCsv();

    OpenAiProperties getOpenai();
    OllamaProperties getOllama();
    VertexAiProperties getVertexai();
    BedrockProperties getBedrock();

    String getServiceLocatorClassName();

    String getUnaccentFunctionName();
}
