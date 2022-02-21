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

import javax.annotation.Nullable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import com.ocs.dynamo.domain.model.VisibilityType;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.Model;

import lombok.Getter;
import lombok.Setter;

/**
 * A Country identified by an ISO country code
 * 
 * @author bas.rutten
 *
 */
@Getter
@Setter
@Entity
@DiscriminatorValue("COUNTRY")
@Model(displayNamePlural = "Countries", displayProperty = "name", sortOrder = "name asc")
public class Country extends DomainChild<Country, Region> {

	private static final long serialVersionUID = 1410771214783677106L;

	public Country() {
		// default constructor
	}

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
	 * @return
	 */
	public Region getRegion() {
		return getParent();
	}

	/**
	 * Overridden so we can modify the attribute model
	 */
	@Nullable
	@Override
	@Attribute(complexEditable = true, displayName = "Region", visibleInGrid = VisibilityType.SHOW)
	public Region getParent() {
		return super.getParent();
	}

	/**
	 * Overridden so we can modify the attribute model
	 */
	@Override
	@NotNull
	@Attribute(visible = VisibilityType.SHOW)
	public String getCode() {
		return super.getCode();
	}

//    @Override
//    public String toString() {
//        return ReflectionToStringBuilder.toStringExclude(this, "parent", "region");
//    }
	
}
