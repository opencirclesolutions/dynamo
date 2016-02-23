package com.ocs.dynamo.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.Model;

@Entity
@Table(name = "test_entity2")
@Model(displayProperty = "name")
public class TestEntity2 extends AbstractEntity<Integer> {

	private static final long serialVersionUID = 3481759712992449747L;

	@Id
	@GeneratedValue
	private Integer id;

	private String name;

	@ManyToOne
	@Attribute(selectMode = AttributeSelectMode.LOOKUP)
	private TestEntity testEntity;

	@ManyToOne
	@Attribute(selectMode = AttributeSelectMode.COMBO)
	private TestEntity testEntityAlt;

	@ManyToOne
	@Attribute(selectMode = AttributeSelectMode.LIST)
	private TestEntity testEntityAlt2;

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

}
