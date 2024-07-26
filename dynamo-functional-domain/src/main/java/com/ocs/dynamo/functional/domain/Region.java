package com.ocs.dynamo.functional.domain;


import com.ocs.dynamo.domain.model.annotation.Model;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

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
