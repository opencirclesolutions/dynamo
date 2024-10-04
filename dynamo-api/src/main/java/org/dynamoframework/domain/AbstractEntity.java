package org.dynamoframework.domain;

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

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * Base class for entities
 *
 * @param <ID> the type of the primary key
 * @author bas.rutten
 */
@MappedSuperclass
@Data
public abstract class AbstractEntity<ID> implements Serializable {

	private static final long serialVersionUID = -8442763252267825950L;

	@Version
	private Integer version;

	public abstract ID getId();

	public abstract void setId(ID id);

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
	 * @param obj the object to compare true
	 * @return <code>true</code> if the objects are equal and <code>false</code> otherwise
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
