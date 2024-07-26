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
