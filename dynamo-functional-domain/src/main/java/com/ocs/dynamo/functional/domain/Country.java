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

//import jakarta.annotation.Nullable;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;

import com.ocs.dynamo.domain.model.VisibilityType;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.Model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

/**
 * A Country identified by an ISO country code
 * 
 * @author bas.rutten
 *
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@DiscriminatorValue("COUNTRY")
@Model(displayNamePlural = "Countries", displayProperty = "name", sortOrder = "name asc")
public class Country extends DomainChild<Country, Region> {

	private static final long serialVersionUID = 1410771214783677106L;

	public Country(String code, String name) {
		super(code, name);
	}

	public void setRegion(Region region) {
		setParent(region);
	}

	/**
	 * The region - note that this is not a JPA attribute and you must use "parent"
	 * instead in queries
	 * 
	 * @return the region
	 */
	public Region getRegion() {
		return getParent();
	}

	/**
	 * Overridden so we can modify the attribute model
	 */
	@Override
	@Attribute(visibleInForm = VisibilityType.SHOW, displayName = "Region", visibleInGrid = VisibilityType.SHOW)
	public Region getParent() {
		return (Region) Hibernate.unproxy(super.getParent());
	}

	/**
	 * Overridden so we can modify the attribute model
	 */
	@Override
	@NotNull
	@Attribute(visibleInForm = VisibilityType.SHOW)
	public String getCode() {
		return super.getCode();
	}

}
