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

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A domain object that is the child of another domain object
 * 
 * @author Bas Rutten
 *
 * @param <C> the type of the child
 * @param <P> the type of the parent
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DomainChild<C extends DomainChild<C, P>, P extends DomainParent<C, P>> extends Domain
		implements Serializable {

	private static final long serialVersionUID = 2615942460028599211L;

	@ManyToOne(fetch = FetchType.EAGER, targetEntity = DomainParent.class)
	@JoinColumn(name = "parent")
	private P parent;

	protected DomainChild(String code, String name) {
		super(code, name);
	}

}
