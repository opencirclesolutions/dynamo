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
package org.dynamoframework.test;

import org.dynamoframework.configuration.DynamoProperties;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.text.DecimalFormat;

/**
 * Base class for testing Spring beans. Automatically injects all dependencies
 * annotated with "@Mock" into the bean
 * 
 * @author bas.rutten
 */
@ExtendWith(SpringExtension.class)
public abstract class BaseMockitoTest {

    /**
     * @param str the string to convert
     * @return the result of the conversion
     */
    protected String formatNumber(String str) {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getNumberInstance();
        char ds = df.getDecimalFormatSymbols().getDecimalSeparator();
        return str.replace(',', ds);
    }
}
