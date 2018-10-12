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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;

import com.ocs.dynamo.domain.model.annotation.Attribute;

/**
 * Exmple of translated entity
 * 
 * @author patrickdeenen
 *
 */
@Entity
public class Product extends AbstractEntityTranslated<Integer, ProductTranslation> {

	private static final long serialVersionUID = -1281716785808553371L;

	enum TranslatedFields {
		NAME
	}

	public Product() {
	}

	private Integer id;
	private String sku;
	private BigDecimal price;

	@Override
	public Collection<Locale> getRequiredLocales() {
		return Arrays.asList(new Locale[] { new Locale("NL", "NL"), new Locale("EN", "EN") });
	}

	@Override
	protected List<String> getRequiredTranslatedFields() {
		ArrayList<String> enums = new ArrayList<>();
		for (TranslatedFields f : TranslatedFields.values()) {
			enums.add(f.name());
		}
		return enums;
	}

	public ProductTranslation getName(String locale) {
		return getTranslations(TranslatedFields.NAME.name(), locale);
	}

	@Attribute(complexEditable = true, memberType = ProductTranslation.class)
	public Set<ProductTranslation> getName() {
		return getTranslations(TranslatedFields.NAME.name());
	}

	public void setName(Set<ProductTranslation> names) {
		setTranslations(TranslatedFields.NAME.name(), names);
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the sku
	 */
	public String getSku() {
		return sku;
	}

	/**
	 * @param sku
	 *            the sku to set
	 */
	public void setSku(String sku) {
		this.sku = sku;
	}

	/**
	 * @return the price
	 */
	public BigDecimal getPrice() {
		return price;
	}

	/**
	 * @param price
	 *            the price to set
	 */
	public void setPrice(BigDecimal price) {
		this.price = price;
	}

}
