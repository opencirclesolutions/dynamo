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

import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.Model;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

/**
 * Entity used for testing purposes
 * 
 * @author patrick.deenen
 */
@Entity
@Table(name = "worldpopulation")
@Model(displayName = "World population")
public class WorldPopulation extends AbstractEntity<Integer> {

	private static final long serialVersionUID = -3910599511872410346L;

	@Id
	@GeneratedValue
	private Integer id;

	private String description;

	@OneToMany
	@Attribute(memberType = Population.class)
	private Set<Population> populations;

	public WorldPopulation() {
		// default constructor
	}

	/**
	 * @param id
	 * @param description
	 */
	public WorldPopulation(Integer id, String description) {
		super();
		this.id = id;
		this.description = description;
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the populations
	 */
	public Set<Population> getPopulations() {
		return populations;
	}

	/**
	 * @param populations
	 *            the populations to set
	 */
	public void setPopulations(Set<Population> populations) {
		this.populations = populations;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
