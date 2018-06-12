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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.ocs.dynamo.domain.AbstractAuditableEntity;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.AttributeOrder;
import com.ocs.dynamo.domain.model.annotation.Model;
import com.ocs.dynamo.domain.model.EditableType;

/**
 * Base class for reference information.
 *
 * @author R.E.M. Claassen (ruud@opencircle.solutions)
 *
 */
@Entity
@AttributeOrder(attributeNames = { "name", "parameterType", "value", "createdBy", "createdOn" })
@Model(displayProperty = "name", sortOrder = "name asc")
public class Parameter extends AbstractAuditableEntity<Integer> {

	private static final long serialVersionUID = 3570240623304694175L;

	public static final String ATTRIBUTE_NAME = "name";

    @Id

    private Integer id;

	@NotNull
	@Attribute(main = true, maxLength = 100, searchable = true, editable = EditableType.READ_ONLY)
	private String name;

	@NotNull
	@Attribute(searchable = true, editable = EditableType.READ_ONLY)
	@Column(name = "type")
	private ParameterType parameterType;

	@NotNull
	@Attribute(maxLength = 50)
	private String value;

	public ParameterType getParameterType() {
		return parameterType;
	}

	public void setParameterType(ParameterType parameterType) {
		this.parameterType = parameterType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public Integer getId() {
		return this.id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Parameter)) {
			return false;
		}

		if (!this.getClass().equals(obj.getClass())) {
			return false;
		}

		Parameter other = (Parameter) obj;
		if (this.id != null && other.id != null) {
			// first, check if the IDs match
			return ObjectUtils.equals(this.id, other.id);
		} else {
			// if this is not the case, check for code and type
			return ObjectUtils.equals(this.name, other.name);
		}

	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

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
