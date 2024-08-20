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

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.dynamoframework.configuration.DynamoProperties;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@ConditionalOnProperty(value = "dynamoframework.openai.enabled", havingValue = "true")
public class ChatGptService implements AIService {

    private OpenAiChatClient client;

    @Autowired
    private DynamoProperties dynamoProperties;

    @PostConstruct
    public void init() {
        var openAiApi = new OpenAiApi(dynamoProperties.getOpenai().getApiKey());
        client = new OpenAiChatClient(openAiApi,
                OpenAiChatOptions.builder()
                        .withModel(dynamoProperties.getOpenai().getModel())
                        .withMaxTokens(dynamoProperties.getOpenai().getMaxTokens())
                        .build());
    }

    @Override
    public boolean supports(AIServiceType type) {
        return type.equals(AIServiceType.CHAT_GPT);
    }

    @Override
    public String execute(String input, Map<String, Object> objectMap, Map<String, String> typesMap, Map<String, String> componentInstructions, List<String> contextInstructions) {

        StringBuilder request = createRequest(input, objectMap);
        appendInstructions(request, typesMap, componentInstructions, contextInstructions);

        log.info("AI request {}", request);

        ChatResponse call = client.call(new Prompt(request.toString()));
        return call.getResult().getOutput().getContent();
    }
}
