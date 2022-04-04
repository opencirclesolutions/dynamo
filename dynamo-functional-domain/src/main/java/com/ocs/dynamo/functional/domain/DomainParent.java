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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A domain entity that is the parent of another domain entity
 * 
 * @author Bas Rutten
 *
 * @param <C> the child entity class
 * @param <P> the parent entity class
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public abstract class DomainParent<C extends DomainChild<C, P>, P extends DomainParent<C, P>> extends Domain
		implements Serializable {

	private static final long serialVersionUID = 20446010658685722L;

	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = DomainChild.class)
	private Set<C> children = new HashSet<>();

	protected DomainParent(String code, String name) {
		super(code, name);
	}

	@SuppressWarnings("unchecked")
	public C addChild(C child) {
		getChildren().add(child);
		child.setParent((P) this);
		return child;
	}

	public C removeChild(C child) {
		getChildren().remove(child);
		child.setParent(null);
		return child;
	}
}
