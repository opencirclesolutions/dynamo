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

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.VisibilityType;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.Model;
import com.ocs.dynamo.domain.model.annotation.SearchMode;
import com.ocs.dynamo.functional.DomainConstants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Base class for reference information.
 * 
 * @author Patrick Deenen (patrick@opencircle.solutions)
 *
 */
@Getter
@Setter
@Inheritance
@DiscriminatorColumn(name = "type")
@Entity(name = "domain")
@ToString(onlyExplicitlyIncluded = true)
@Model(displayProperty = "name", sortOrder = "name asc")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Domain extends AbstractEntity<Integer> {

	public static final String ATTRIBUTE_NAME = "name";

	public static final String ATTRIBUTE_CODE = "code";

	private static final long serialVersionUID = 1598343469161718498L;

	@Id
	@ToString.Include
	private Integer id;

	@Attribute(visible = VisibilityType.HIDE, editable = EditableType.READ_ONLY)
	@Column(name = "type", insertable = false, updatable = false)
	private String type;

	/**
	 * By default, we only use "name" so the code is hidden
	 */
	@Attribute(visible = VisibilityType.HIDE)
	@Size(max = 5)
	@ToString.Include
	private String code;

	@Size(max = 255)
	@NotNull
	@Attribute(main = true, maxLength = DomainConstants.MAX_NAME_LENGTH, searchable = SearchMode.ALWAYS)
	@ToString.Include
	private String name;

	protected Domain(String code, String name) {
		this.code = code;
		this.name = name;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final Domain other)) {
			return false;
		}

		if (!this.getClass().equals(obj.getClass())) {
			return false;
		}

		if (this.id != null && other.id != null) {
			// first, check if the IDs match
			return Objects.equals(this.id, other.id);
		} else {
			// if this is not the case, check for code and type
			return Objects.equals(this.code, other.code);
		}

	}

}
