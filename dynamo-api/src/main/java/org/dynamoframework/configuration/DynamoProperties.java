package org.dynamoframework.configuration;

public interface DynamoProperties {

    boolean isCapitalizePropertyNames();

    DefaultProperties getDefaults();

    CsvProperties getCsv();

    String getServiceLocatorClassName();

    String getUnaccentFunctionName();
}
