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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.ocs.dynamo.domain.model.VisibilityType;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.Model;

/**
 * A Country identified by an ISO country code
 * 
 * @author bas.rutten
 *
 */
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
	@Override
	@Attribute(complexEditable = true, displayName = "Region", showInTable = VisibilityType.SHOW, replacementSearchPath = "parent")
	public Region getParent() {
		return super.getParent();
	}

	/**
	 * Overridden so we can modify the attribute model
	 */
	@Override
	@Attribute(visible = VisibilityType.SHOW, required = true)
	public String getCode() {
		return super.getCode();
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toStringExclude(this, new String[] { "parent", "region" });
	}
}
