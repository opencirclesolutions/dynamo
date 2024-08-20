package org.dynamoframework.configuration;

import lombok.Data;
import org.dynamoframework.domain.model.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@ConfigurationProperties(prefix = "dynamoframework")
@Data
@Validated
public class DynamoConfigurationProperties implements Serializable, DynamoProperties {
    @Serial
    private static final long serialVersionUID = -8329492319537273489L;

    /**
     * Indicates whether to capitalize individual words in property names
     */
    private boolean capitalizePropertyNames = true;

    private DefaultProperties defaults = new DefaultConfigurationProperties();

    @Data
    public static class DefaultConfigurationProperties implements Serializable, DefaultProperties {
        @Serial
        private static final long serialVersionUID = -3229613809797417359L;

        /**
         * Indicates the default mode to use for boolean components
         */
        private AttributeBooleanFieldMode booleanFieldMode = AttributeBooleanFieldMode.CHECKBOX;

        /**
         * The default date format
         */
        private String dateFormat = "dd-MM-yyyy";

        /**
         * The default date/time (time stamp) format
         */
        private String dateTimeFormat = "dd-MM-yyyy HH:mm:ss";
        /**
         * The default decimal precision
         */
        private Integer decimalPrecision = 2;

        /**
         * Indicates the default mode to use for element collection fields
         */
        private ElementCollectionMode elementCollectionMode = ElementCollectionMode.CHIPS;

        /**
         * The default field type to use for enumeration attributes
         */
        private AttributeEnumFieldMode enumFieldMode = AttributeEnumFieldMode.DROPDOWN;

        /**
         * The representation of the value <code>false</code>
         */
        private String falseRepresentation = "false";

        /**
         * Localized representations of the value <code>false</code>
         */
        private Map<String, String> falseRepresentations = new HashMap<>();

        /**
         * Localized representations of the value <code>true</code>
         */
        private Map<String, String> trueRepresentations = new HashMap<>();

        /**
         * The representation of the value <code>true</code>
         */
        private String trueRepresentation = "true";

        /**
         * The default group together mode
         */
        private GroupTogetherMode groupTogetherMode;

        /**
         * The column width from grouping together
         */
        private Integer groupTogetherWidth = 300;

        /**
         * The default locale
         */
        private Locale locale = Locale.ENGLISH;;

        /**
         * The default nesting depth
         */
        private Integer nestingDepth = 2;

        /**
         * The default number field mode
         */
        private NumberFieldMode numberFieldMode = NumberFieldMode.TEXTFIELD;

        /**
         * The default case sensitiveness for search
         */
        private boolean searchCaseSensitive = false;

        /**
         * Whether search is prefix only
         */
        private boolean searchPrefixOnly = false;

        /**
         * The default time format
         */
        private String timeFormat = "HH:mm:ss";

        /**
         * Indicates whether to use the display name as the input prompt by default
         */
        private boolean promptValue = true;

        /**
         * Whether to trim white space for text inputs
         */
        private boolean trimSpaces = false;

    }

//    private EntityModelProperties entityModel = new EntityModelProperties();
//
//    @Data
//    public static class EntityModelProperties implements Serializable {
//        @Serial
//        private static final long serialVersionUID = 2907487755230383369L;
//
//        /**
//         * The packages that contain the entity model
//         */
//        private List<String> packages;
//    }


    private CsvProperties csv = new CsvConfigurationProperties();

    @Data
    public static class CsvConfigurationProperties implements Serializable, CsvProperties {
        @Serial
        private static final long serialVersionUID = 6909139472091672387L;

        /**
         * The CSV escape character when importing/exporting
         */
        private String escapeChar = "\"\"";
        /**
         * The CSV quote char when importing/exporting
         */
        private String quoteChar = "\"\"";

        /**
         * The CSV separator when importing/exporting
         */
        private String separatorChar = ";";

        /**
         * The number of rows that must be present in a result set before resorting to a
         * streaming approach for Excel export
         */
        private Integer maxRowsBeforeStreaming = 1000;

        /**
         * Whether to use thousands grouping in XLS files
         */
        private boolean thousandsGrouping = false;
    }


    /**
     * Class name for the service locator (override to create a different service
     * locator, e.g. to use a separate service locator for integration tests)
     */
    private String serviceLocatorClassName = "org.dynamoframework.SpringWebServiceLocator";

    /**
     * The name of the database function used to replace accents
     */
    private String unaccentFunctionName = "";


}
