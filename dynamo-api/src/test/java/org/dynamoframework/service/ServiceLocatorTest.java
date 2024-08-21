package org.dynamoframework.service;

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

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ServiceLocatorTest {

    @Test
    public void testDefaultServiceLocator() {
        assertEquals("org.dynamoframework.SpringWebServiceLocator", ServiceLocatorFactory.getServiceLocatorClassName());
    }

    @Test
    public void testOverrideServiceLocator() {
        File targetClassesDir = new File(ServiceLocatorFactory.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        File propertiesFile = new File(targetClassesDir.getParentFile().getAbsoluteFile() + "/classes", "dynamoframework.properties");
        Properties properties = new Properties();
        properties.setProperty("service-locator", "org.dynamoframework.OtherServiceLocator");
        try {
            properties.store(new FileOutputStream(propertiesFile), "Comment");
            assertEquals("org.dynamoframework.OtherServiceLocator", ServiceLocatorFactory.getServiceLocatorClassName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            propertiesFile.delete();
            assertFalse(propertiesFile.exists());
        }

    }
}
