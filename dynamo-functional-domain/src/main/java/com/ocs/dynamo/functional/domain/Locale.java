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

import com.ocs.dynamo.domain.model.VisibilityType;
import com.ocs.dynamo.domain.model.annnotation.Attribute;
import com.ocs.dynamo.domain.model.annnotation.Model;

/**
 * A locale identified by an IETF BCP 47 code
 *
 * @author bas.rutten
 */
@Entity
@DiscriminatorValue("LOCALE")
@Model(displayNamePlural = "Locales", displayProperty = "name", sortOrder = "name asc")
public class Locale extends Domain {

	private static final long serialVersionUID = 3270223599926941961L;

	public Locale() {
		// default constructor
	}

	/**
	 * Constructor
	 *
	 * @param code
	 *            the code of the currency
	 * @param name
	 *            the name of the currency
	 */
	public Locale(String code, String name) {
		super(code, name);
	}

	@Attribute(visible = VisibilityType.HIDE)
	public String getCodeAndName() {
		return getCode() + " - " + getName();
	}

	@Attribute(visible = VisibilityType.SHOW)
	public String getCode() {
		return super.getCode();
	}

	@Attribute(visible = VisibilityType.SHOW)
	public String getName() {
		return super.getName();
	}
}
