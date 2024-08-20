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
package org.dynamoframework.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.dynamoframework.constants.DynamoConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility methods for retrieving system property values
 *
 * @author bas.rutten
 */
@Slf4j
@UtilityClass
public final class SystemPropertyUtils {

    private static final Properties PROPERTIES = new Properties();

    static {
        try {
            InputStream resourceAsStream = SystemPropertyUtils.class.getClassLoader()
                    .getResourceAsStream("application.properties");
            if (resourceAsStream != null) {
                PROPERTIES.load(resourceAsStream);
            }
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * @return the name of the service locator to use. Used internally by the
     * framework, highly unlikely this needs to be modified directly
     */
    public static String getServiceLocatorClassName() {
        return getStringProperty(DynamoConstants.SP_SERVICE_LOCATOR_CLASS_NAME,
                "org.dynamoframework.SpringWebServiceLocator");
    }

    /**
     * Looks up the value of a String property by scanning the system properties
     * first and falling back to application.properties
     *
     * @param propertyName the name of the property
     * @param defaultValue the default value
     * @return the property
     */
    private static String getStringProperty(String propertyName, String defaultValue) {
        String sys = System.getProperty(propertyName);
        if (sys == null) {
            sys = PROPERTIES.getProperty(propertyName, defaultValue);
        }
        return sys;
    }


}
