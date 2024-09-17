package com.example.domain;

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

import org.dynamoframework.domain.AbstractEntity;

/**
 * An entity class outside package <code>org.dynamoframework</code> to use in <code>ClassUtilsTest.testFindClass()</code>.
 */
public class ExternalEntity extends AbstractEntity {
    @Override
    public Object getId() {
        return null;
    }

    @Override
    public void setId(Object o) {

    }
}