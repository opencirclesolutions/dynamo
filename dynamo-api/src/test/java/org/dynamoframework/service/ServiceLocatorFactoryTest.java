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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceLocatorFactoryTest {

    @Test
    public void testDefaultServiceLocator() {
        assertEquals("org.dynamoframework.SpringWebServiceLocator", ServiceLocatorFactory.getServiceLocatorClassName());
    }

    @Test
    public void testOverrideServiceLocatorCorrectFile() {
        File targetClassesDir = new File(ServiceLocatorFactory.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        File dynamoservicelocator = new File(targetClassesDir.getParentFile().getAbsoluteFile() + "/classes", "dynamoservicelocator");
        try {
            FileUtils.write(dynamoservicelocator, "org.dynamoframework.OtherServiceLocator", StandardCharsets.UTF_8);
            // Actual test:
            assertEquals("org.dynamoframework.OtherServiceLocator", ServiceLocatorFactory.getServiceLocatorClassName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            dynamoservicelocator.delete();
            assertFalse(dynamoservicelocator.exists());
        }
    }

    @Test
    public void testOverrideServiceLocatorEmptyFile() {
        File targetClassesDir = new File(ServiceLocatorFactory.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        File dynamoservicelocator = new File(targetClassesDir.getParentFile().getAbsoluteFile() + "/classes", "dynamoservicelocator");
        try {
            assertTrue(dynamoservicelocator.createNewFile());
            // Actual test:
            assertEquals("org.dynamoframework.SpringWebServiceLocator", ServiceLocatorFactory.getServiceLocatorClassName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            dynamoservicelocator.delete();
            assertFalse(dynamoservicelocator.exists());
        }
    }

    @Test
    public void testOverrideServiceLocatorMoreThanOneLine() {
        File targetClassesDir = new File(ServiceLocatorFactory.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        File dynamoservicelocator = new File(targetClassesDir.getParentFile().getAbsoluteFile() + "/classes", "dynamoservicelocator");
        try {
            FileUtils.writeLines(dynamoservicelocator, StandardCharsets.UTF_8.toString(), List.of("org.dynamoframework.OtherServiceLocator", "org.dynamoframework.AnotherServiceLocator"));
            // Actual test:
            assertEquals("org.dynamoframework.OtherServiceLocator", ServiceLocatorFactory.getServiceLocatorClassName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            dynamoservicelocator.delete();
            assertFalse(dynamoservicelocator.exists());
        }
    }


}
