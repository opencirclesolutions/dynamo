/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.dynamoframework.autofill;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dynamoframework.configuration.DynamoProperties;
import org.springframework.ai.bedrock.anthropic.AnthropicChatOptions;
import org.springframework.ai.bedrock.anthropic.BedrockAnthropicChatClient;
import org.springframework.ai.bedrock.anthropic.api.AnthropicChatBedrockApi;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "dynamoframework.bedrock.enabled", havingValue = "true")
public class BedrockService implements AIService {

    @Autowired
    private DynamoProperties dynamoProperties;

    private BedrockAnthropicChatClient client;

    @PostConstruct
    public void init() {
        AnthropicChatBedrockApi api = new AnthropicChatBedrockApi(dynamoProperties.getBedrock().getModelId(),
                StaticCredentialsProvider.create(AwsBasicCredentials.create(dynamoProperties.getBedrock().getAccessKey(), dynamoProperties.getBedrock().getAccessSecret())), dynamoProperties.getBedrock().getRegion(), new ObjectMapper());
        this.client = new BedrockAnthropicChatClient(api,
                AnthropicChatOptions.builder()
                        .withAnthropicVersion(AnthropicChatBedrockApi.DEFAULT_ANTHROPIC_VERSION)
                        .withTemperature(1.0f)
                        .withTopK(250)
                        .withTopP(0.999f)
                        .withMaxTokensToSample(2048)
                        .build());
    }

    @Override
    public boolean supports(AIServiceType type) {
        return AIServiceType.BEDROCK.equals(type);
    }

    @Override
    public String execute(String input, Map<String, Object> objectMap, Map<String, String> typesMap, Map<String, String> componentInstructions, List<String> contextInstructions) {
        StringBuilder request = new StringBuilder(String.format(
                "Based on the user input: \n \"%s\", " +
                        "generate a JSON object according to these instructions: " +
                        "Never include duplicate keys, in case of duplicate keys just keep the first occurrence in the response. " +
                        "Fill out \"N/A\" in the JSON value if the user did not specify a value. " +
                        "Return only a valid JSON object in this format: '%s'."
                , input, objectMap));
        appendInstructions(request, typesMap, componentInstructions, contextInstructions);

        log.debug("AI request: {}", request);

        ChatResponse call = client.call(new Prompt(request.toString()));
        return call.getResult().getOutput().getContent();
    }
}
