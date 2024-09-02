package org.dynamoframework.configuration;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
    private boolean capitalizePropertyNames = true;

    /**
     * Default properties
     */
    private DefaultProperties defaults = new DefaultConfigurationProperties();

    @Data
    @ConfigurationProperties(prefix = "dynamoframework.defaults")
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
        private boolean usePromptValue = true;

        /**
         * Whether to trim white space for text inputs
         */
        private boolean trimSpaces = false;

        /**
         * The default AI service
         */
        private String aiService;

        /**
         * The configuration of the Dynamo endpoints
         */
        private EndpointProperties endpoints = new EndpointConfigurationProperties();

        @Data
        @ConfigurationProperties(prefix = "dynamoframework.defaults.endpoints")
        public static class EndpointConfigurationProperties implements Serializable, EndpointProperties {
            /**
             * The endpoint for the export controller
             */
            private String export = "/api/dynamo/export";
            /**
             * The endpoint for the model controller
             */
            private String model = "/api/dynamo/model";
            /**
             * The endpoint for the autofill controller
             */
            private String autofill = "/api/dynamo/autofill";
            /**
             * The endpoint for the status controller
             */
            private String status = "/api/dynamo/status";
            /**
             * The endpoint for the crud controller
             */
            private String crud = "/api/dynamo/crud";
            /**
             * The endpoint for the files controller
             */
            private String files = "/api/dynamo/files";
        }

    }

    /**
     * Properties related to csv, import and export
     */
    private CsvProperties csv = new CsvConfigurationProperties();

    @Data
    @ConfigurationProperties(prefix = "dynamoframework.csv")
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
     * OpenAI properties
     */
    private OpenAiProperties openai = new OpenAiConfigurationProperties();

    @Data
    @ConfigurationProperties(prefix = "dynamoframework.openai")
    public static class OpenAiConfigurationProperties implements Serializable, OpenAiProperties {
        @Serial
        private static final long serialVersionUID = 4426516280121454055L;
        /**
         * Enable OpenAI
         */
        private boolean enabled = false;
        /**
         * The OpenAI API key
         */
        private String apiKey;
        /**
         * The model to use
         */
        private String model = "gpt-4-turbo";
        /**
         * Maximum number of tokens
         */
        private Integer maxTokens = 4096;

    }

    /**
     * Ollama properties
     */
    private OllamaProperties ollama = new OllamaConfigurationProperties();

    @Data
    @ConfigurationProperties(prefix = "dynamoframework.ollama")
    public static class OllamaConfigurationProperties implements Serializable, OllamaProperties {

        private static final long serialVersionUID = -2357577501146838042L;
        /**
         * Enable Ollama
         */
        private boolean enabled = false;
        /**
         * Ollama URL
         */
        private String url;
        /**
         * The model to use
         */
        private String model = "llama3";
    }

    /**
     * VertexAI properties
     */
    private VertexAiProperties vertexai = new VertexAiConfigurationProperties();

    @Data
    @ConfigurationProperties(prefix = "dynamoframework.vertexai")
    public static class VertexAiConfigurationProperties implements Serializable, VertexAiProperties {
        @Serial
        private static final long serialVersionUID = -1404865335552095931L;
        /**
         * Enable VertexAI
         */
        private boolean enabled = false;
        /**
         * The project id
         */
        private String projectId;
        /**
         * The region of the project
         */
        private String projectRegion = "europe-west1";
        /**
         * The model to use
         */
        private String model = "gemini-1.5-flash-preview-0514";
    }

    /**
     * Bedrock properties
     */
    private BedrockProperties bedrock = new BedrockConfigurationProperties();

    @Data
    @ConfigurationProperties(prefix = "dynamoframework.bedrock")
    public static class BedrockConfigurationProperties implements Serializable, BedrockProperties {
        @Serial
        private static final long serialVersionUID = -6324259015491446578L;
        /**
         * Enable Bedrock
         */
        private boolean enabled = false;
        /**
         * Access key
         */
        private String accessKey;
        /**
         * Access secret
         */
        private String accessSecret;
        /**
         * Model id
         */
        private String modelId;
        /**
         * Region
         */
        private String region;
    }

    /**
     * The name of the database function used to replace accents
     */
    private String unaccentFunctionName = "";



}
