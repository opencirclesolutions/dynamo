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

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

/**
 * Base class for entities
 * 
 * @author bas.rutten
 * @param <ID> the type of the primary key
 */
@MappedSuperclass
public abstract class AbstractEntity<ID> implements Serializable {

	private static final long serialVersionUID = -8442763252267825950L;

	// version field for optimistic locking
	@Version
	private Integer version;

	public abstract ID getId();

	public abstract void setId(ID id);

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	/**
	 * Basic hash code function - uses the ID. Override this (and the equals()
	 * method) if your entity has a more meaningful key
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	/**
	 * Basic equals function. Override this (and of the hashCode() method) if your
	 * entity has a more meaningful key
	 * 
	 * @param obj
	 * @return
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || !this.getClass().isAssignableFrom(obj.getClass())) {
			return false;
		}
		return Objects.equals(this.getId(), ((AbstractEntity<?>) obj).getId());
	}

}
