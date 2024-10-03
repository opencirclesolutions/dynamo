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
package org.dynamoframework.functional.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import org.dynamoframework.domain.AbstractAuditableEntity;
import org.dynamoframework.domain.model.EditableType;
import org.dynamoframework.domain.model.annotation.Attribute;
import org.dynamoframework.domain.model.annotation.AttributeOrder;
import org.dynamoframework.domain.model.annotation.Model;
import org.dynamoframework.domain.model.annotation.SearchMode;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Reusable parameter class
 *
 * @author BasRutten
 */
@Getter
@Setter
@ToString
@Entity
@AttributeOrder(attributeNames = {"name", "parameterType", "value", "createdBy", "createdOn"})
@Model(displayProperty = "name", sortOrder = "name asc")
public class Parameter extends AbstractAuditableEntity<Integer> {

	private static final long serialVersionUID = 3570240623304694175L;

	public static final String ATTRIBUTE_NAME = "name";

	@Id
	private Integer id;

	@NotNull
	@Attribute(main = true, maxLength = 100, searchable = SearchMode.ALWAYS, editable = EditableType.READ_ONLY)
	private String name;

	@NotNull
	@Attribute(searchable = SearchMode.ALWAYS, editable = EditableType.READ_ONLY)
	@Column(name = "type")
	@Enumerated(EnumType.STRING)
	private ParameterType parameterType;

	@NotNull
	@Attribute(maxLength = 50)
	private String value;

	@AssertTrue(message = "{Parameter.type.valid}")
	public boolean isValueCorrect() {
		if (value == null) {
			return true;
		}

		if (ParameterType.BOOLEAN.equals(this.parameterType)) {
			return "true".equals(value) || "false".equals(value);
		} else if (ParameterType.INTEGER.equals(this.parameterType)) {
			return value.matches("\\d+");
		} else {
			return true;
		}
	}
}
