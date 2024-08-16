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
package org.dynamoframework.functional.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import org.dynamoframework.domain.model.VisibilityType;
import org.dynamoframework.domain.model.annotation.Attribute;
import org.dynamoframework.domain.model.annotation.Model;

/**
 * A currency identified by an ISO currency code
 * 
 * @author bas.rutten
 *
 */
@Entity
@DiscriminatorValue("CURRENCY")
@Model(displayNamePlural = "Currencies", displayProperty = "codeAndName", sortOrder = "name asc")
@NoArgsConstructor
public class Currency extends Domain {

	private static final long serialVersionUID = 3270223599926941961L;

	/**
	 * Constructor
	 * 
	 * @param code the code of the currency
	 * @param name the name of the currency
	 */
	public Currency(String code, String name) {
		super(code, name);
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

	@Attribute(visibleInForm = VisibilityType.HIDE)
	public String getCodeAndName() {
		return getCode() + " - " + getName();
	}
}
