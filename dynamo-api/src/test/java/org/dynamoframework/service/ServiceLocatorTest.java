package org.dynamoframework.service;

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
