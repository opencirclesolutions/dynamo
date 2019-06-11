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
package com.ocs.dynamo.test;

import java.text.DecimalFormat;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.ocs.dynamo.constants.DynamoConstants;

/**
 * Base class for testing Spring beans. Automatically injects all dependencies
 * annotated with "@Mock" into the bean
 * 
 * @author bas.rutten
 */
@RunWith(SpringRunner.class)
public abstract class BaseMockitoTest {

    @BeforeClass
    public static void beforeClass() {
        System.setProperty(DynamoConstants.SP_SERVICE_LOCATOR_CLASS_NAME, "com.ocs.dynamo.ui.SpringTestServiceLocator");
    }
    
    /**
     * 
     * @param str
     * @return
     */
    protected String formatNumber(String str) {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getNumberInstance();
        char ds = df.getDecimalFormatSymbols().getDecimalSeparator();
        return str.replace(',', ds);
    }

}
