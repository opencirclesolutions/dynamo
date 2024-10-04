package org.dynamoframework.importer.impl;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.dynamoframework.importer.ImportField;
import org.dynamoframework.importer.dto.AbstractDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PersonDTO extends AbstractDTO {

	private static final long serialVersionUID = 7099498644417977630L;

	public enum Gender {
		M, V
	}

	public PersonDTO() {
	}

	@ImportField(index = 0, defaultValue = "Unknown")
	private String name;

	@ImportField(index = 1, required = true)
	private Integer number;

	@ImportField(index = 2, defaultValue = "1.0", cannotBeNegative = true)
	private BigDecimal factor;

	@ImportField(index = 3)
	private String random;

	@ImportField(index = 4)
	private Gender gender;

	@ImportField(index = 5, percentage = true)
	private BigDecimal percentage;

	@ImportField(index = 6, defaultValue = "true")
	private Boolean abool;

	@ImportField(index = 7, defaultValue = "01-01-2015", required = true)
	private LocalDate date;

	@ImportField(index = 8)
	private Double rating;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public BigDecimal getFactor() {
		return factor;
	}

	public void setFactor(BigDecimal factor) {
		this.factor = factor;
	}

	public String getRandom() {
		return random;
	}

	public void setRandom(String random) {
		this.random = random;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public BigDecimal getPercentage() {
		return percentage;
	}

	public void setPercentage(BigDecimal percentage) {
		this.percentage = percentage;
	}

	public Boolean getAbool() {
		return abool;
	}

	public void setAbool(Boolean abool) {
		this.abool = abool;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Double getRating() {
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

}
