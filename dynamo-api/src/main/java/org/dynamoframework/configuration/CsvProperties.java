package org.dynamoframework.configuration;

public interface CsvProperties {
    String getEscapeChar();

    String getQuoteChar();

    String getSeparatorChar();

    Integer getMaxRowsBeforeStreaming();

    boolean isThousandsGrouping();
}
