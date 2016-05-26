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

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.ObjectUtils;

import com.ocs.dynamo.domain.AbstractEntity;

/**
 * Base class for reference information.
 * 
 * @author Patrick Deenen (patrick@opencircle.solutions)
 *
 */
@Inheritance
@DiscriminatorColumn(name = "TYPE")
@Entity
public abstract class Domain extends AbstractEntity<Integer> {

	private static final long serialVersionUID = 1598343469161718498L;

	@Id
	@SequenceGenerator(name = "DOMAIN_ID_GENERATOR", sequenceName = "DOMAIN_ID_SEQ")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DOMAIN_ID_GENERATOR")
	private Integer id;

	@NotNull
	private String code;

	@NotNull
	private String name;

	public Domain() {
		super();
	}

	public Domain(String code, String name) {
		super();
		this.code = code;
		this.name = name;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		if (id == null) {
			return ObjectUtils.hashCode(code);
		} else {
			return ObjectUtils.hashCode(id);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Domain)) {
			return false;
		}

		// if either of the objects does not have an ID, then they are
		// in memory only and are never equal
		Domain other = (Domain) obj;
		if (this.id == null || other.id == null) {
			return ObjectUtils.equals(this.code, other.code);
		}
		return ObjectUtils.equals(this.id, other.id);
	}
}
