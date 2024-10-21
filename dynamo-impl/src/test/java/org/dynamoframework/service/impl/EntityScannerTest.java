package org.dynamoframework.service.impl;

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

import org.dynamoframework.BackendIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EntityScannerTest extends BackendIntegrationTest {

    @Autowired
    private EntityScanner entityScanner;

    @Test
    public void testFindClass() {
        assertNull(entityScanner.findClass("NonExistingClass"));
        assertNull(entityScanner.findClass("AbstractEntity1"));
        Class<?> clazz = entityScanner.findClass("TestEntity");
        assertEquals("org.dynamoframework.domain.TestEntity", clazz.getName());
        // In a package not listed in @EntityScan
        assertNull(entityScanner.findClass("ExternalEntity"));
        assertNull(entityScanner.findClass("ExternalAbstractEntity"));
    }
}
