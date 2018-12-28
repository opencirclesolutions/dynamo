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

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.CheckboxMode;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.Model;
import com.ocs.dynamo.domain.model.validator.Email;

@Entity
@Table(name = "test_entity2")
@Model(displayProperty = "name")
public class TestEntity2 extends AbstractEntity<Integer> {

	private static final long serialVersionUID = 3481759712992449747L;

	@Id
	@GeneratedValue
	private Integer id;

	@Attribute(styles = "myStyle")
	private String name;

	private Integer value;

	private Integer value2;

	private Integer valueSum;

	@Attribute(checkboxMode = CheckboxMode.SWITCH)
	private Boolean switchBool;

	@ManyToOne
	@Attribute(selectMode = AttributeSelectMode.LOOKUP, multipleSearch = true, navigable = true)
	private TestEntity testEntity;

	@ManyToOne
	@Attribute(selectMode = AttributeSelectMode.COMBO)
	private TestEntity testEntityAlt;

	@ManyToOne
	@Attribute(selectMode = AttributeSelectMode.LIST)
	private TestEntity testEntityAlt2;

	@Attribute(selectMode = AttributeSelectMode.TOKEN)
	private String basicToken;

	@Attribute(currency = true)
	private BigDecimal currency;

	@Attribute
	@Email
	private String email;

	@Attribute(editable = EditableType.READ_ONLY)
	private String readOnly;

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TestEntity getTestEntity() {
		return testEntity;
	}

	public void setTestEntity(TestEntity testEntity) {
		this.testEntity = testEntity;
	}

	public TestEntity getTestEntityAlt() {
		return testEntityAlt;
	}

	public void setTestEntityAlt(TestEntity testEntityAlt) {
		this.testEntityAlt = testEntityAlt;
	}

	public TestEntity getTestEntityAlt2() {
		return testEntityAlt2;
	}

	public void setTestEntityAlt2(TestEntity testEntityAlt2) {
		this.testEntityAlt2 = testEntityAlt2;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public Integer getValue2() {
		return value2;
	}

	public void setValue2(Integer value2) {
		this.value2 = value2;
	}

	public Integer getValueSum() {
		return valueSum;
	}

	public void setValueSum(Integer valueSum) {
		this.valueSum = valueSum;
	}

	public String getBasicToken() {
		return basicToken;
	}

	public void setBasicToken(String basicToken) {
		this.basicToken = basicToken;
	}

	public Boolean getSwitchBool() {
		return switchBool;
	}

	public void setSwitchBool(Boolean switchBool) {
		this.switchBool = switchBool;
	}

	public BigDecimal getCurrency() {
		return currency;
	}

	public void setCurrency(BigDecimal currency) {
		this.currency = currency;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(String readOnly) {
		this.readOnly = readOnly;
	}

}
