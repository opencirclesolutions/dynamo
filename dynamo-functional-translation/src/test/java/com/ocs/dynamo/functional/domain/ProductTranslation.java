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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.ocs.dynamo.domain.model.annotation.Model;

/**
 * Example of translations for an entity
 * 
 * @author patrickdeenen
 *
 */
@Entity
@DiscriminatorValue("Product")
@Model(displayName = "Translation", displayNamePlural = "Translations", displayProperty = "fullDescription")
public class ProductTranslation extends Translation<Product> {

	private static final long serialVersionUID = -5832862919356045090L;

	@ManyToOne
	@JoinColumn(name = "key")
	private Product entity;

	@Override
	public Product getEntity() {
		return entity;
	}

	@Override
	public void setEntity(Product entity) {
		this.entity = entity;
	}

}
