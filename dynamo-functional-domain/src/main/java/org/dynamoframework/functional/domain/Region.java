package org.dynamoframework.functional.domain;

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


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.dynamoframework.domain.model.annotation.Model;

import java.util.Set;

@Entity
@DiscriminatorValue("REGION")
@Model(
        displayProperty = "name",
        sortOrder = "name asc",
        listAllowed = true
)
public class Region extends DomainParent<Country, Region> {
    private static final long serialVersionUID = 1410771214783677106L;

    public Region(String code, String name) {
        super(code, name);
    }

    public Set<Country> getCountries() {
        return this.getChildren();
    }

    public void setCountries(Set<Country> countries) {
        this.setChildren(countries);
    }

    public Region() {
    }
}
