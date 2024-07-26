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
package com.ocs.dynamo.functional.domain;

import com.ocs.dynamo.domain.model.VisibilityType;
import com.ocs.dynamo.domain.model.annotation.*;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import org.hibernate.Hibernate;

@Entity
@DiscriminatorValue("COUNTRY")
@Model(
        displayNamePlural = "Countries",
        displayProperty = "name",
        sortOrder = "name asc")
@AttributeOrder(attributeNames = { "id", "code", "name"})
@FetchJoins(joins = { @FetchJoin(attribute = "parent")})
public class Country extends DomainChild<Country, Region> {

    private static final long serialVersionUID = 1410771214783677106L;

    public Country(String code, String name) {
        super(code, name);
    }

    public void setRegion(Region region) {
        this.setParent(region);
    }

    public Region getRegion() {
        return this.getParent();
    }

    @Attribute(
            visibleInForm = VisibilityType.SHOW,
            displayName = "Region",
            visibleInGrid = VisibilityType.HIDE,
            searchable = SearchMode.ALWAYS
    )
    public Region getParent() {
        return (Region) Hibernate.unproxy(super.getParent());
    }

    @Attribute(visibleInForm = VisibilityType.SHOW, visibleInGrid = VisibilityType.SHOW)
    public Integer getId() {
        return super.getId();
    }

    @Attribute(
            visibleInForm = VisibilityType.SHOW,
            searchable = SearchMode.ALWAYS
    )
    public @NotNull String getCode() {
        return super.getCode();
    }

    public Country() {
    }
}
