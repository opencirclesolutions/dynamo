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
package com.ocs.dynamo.domain;

import com.ocs.dynamo.domain.model.annotation.Chart;
import com.ocs.dynamo.domain.model.annotation.Model;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity used for testing purposes
 * 
 * @author patrick.deenen
 */
@Entity
@Table(name = "population")
@Model(displayProperty = "World population by region")
@Chart(subTitle = "Source: Wikipedia.org", seriesPath = "year", namePath = "region", dataPath = "population", tooltip = "this.series.name +': '+ this.y +' millions for region '+this.x")
public class Population extends AbstractEntity<Integer> {

	private static final long serialVersionUID = -9024232769159705990L;

	@Id
	@GeneratedValue
	private Integer id;

	private String region;

	private Integer year;

	private Integer population;

	public Population() {
		// default constructor
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
	 * @return the region
	 */
	public String getRegion() {
		return region;
	}

	/**
	 * @param region
	 *            the region to set
	 */
	public void setRegion(String region) {
		this.region = region;
	}

	/**
	 * @return the year
	 */
	public Integer getYear() {
		return year;
	}

	/**
	 * @param year
	 *            the year to set
	 */
	public void setYear(Integer year) {
		this.year = year;
	}

	/**
	 * @return the population
	 */
	public Integer getPopulation() {
		return population;
	}

	/**
	 * @param population
	 *            the population to set
	 */
	public void setPopulation(Integer population) {
		this.population = population;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
