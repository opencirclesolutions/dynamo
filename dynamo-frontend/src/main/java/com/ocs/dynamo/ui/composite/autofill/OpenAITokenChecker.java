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
package com.ocs.dynamo.ui.composite.autofill;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class OpenAITokenChecker {

    private static String OPEN_AI_KEY;

    static {
        // read apiKey from system variable
        OPEN_AI_KEY = System.getProperty("OPENAI_TOKEN");
        if (OPEN_AI_KEY == null || OPEN_AI_KEY.isBlank()) {
            // read apiKey from environment variable
            OPEN_AI_KEY = System.getenv("OPENAI_TOKEN");
        }
        if (!StringUtils.isEmpty(OPEN_AI_KEY)) {
            log.info("OPENAI_TOKEN was filled properly");
        } else {
            log.error("OPENAI_TOKEN was not filled properly");
        }
    }

    public static String getOpenAiKey() {
        return OPEN_AI_KEY;
    }
}

