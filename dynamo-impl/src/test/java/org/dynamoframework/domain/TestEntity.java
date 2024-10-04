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

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dynamoframework.domain.model.AttributeTextFieldMode;
import org.dynamoframework.domain.model.VisibilityType;
import org.dynamoframework.domain.model.annotation.Attribute;
import org.dynamoframework.domain.model.annotation.Model;
import org.dynamoframework.domain.model.annotation.SearchMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity used for testing purposes - has to be included in src/main/java
 * because otherwise QueryDSL code generation fails
 *
 * @author bas.rutten
 */
@ToString
@Getter
@Setter
@Entity
@Table(name = "test_entity")
@Model(displayProperty = "name", sortOrder = "name,age")
public class TestEntity extends AbstractEntity<Integer> {

	private static final long serialVersionUID = 5557043276302609211L;

	public enum TestEnum {
		A, B, C
	}

	@Id
	@GeneratedValue
	@Attribute(visibleInForm = VisibilityType.HIDE, visibleInGrid = VisibilityType.HIDE)
	private Integer id;

	@Size(max = 25)
	@Attribute(searchable = SearchMode.ALWAYS)
	@NotNull
	private String name;

	@Attribute(searchable = SearchMode.ALWAYS)
	private Long age;

	@Attribute(searchable = SearchMode.ALWAYS)
	private BigDecimal discount;

	@Attribute(percentage = true, searchable = SearchMode.ALWAYS)
	private BigDecimal rate;

	@Attribute(displayFormat = "dd/MM/yyyy", searchable = SearchMode.ALWAYS)
	private LocalDate birthDate;

	private LocalTime registrationTime;

	private LocalDateTime lastLogin;

	@Attribute(searchable = SearchMode.ALWAYS)
	@Column(columnDefinition = "smallint")
	private TestEnum someEnum;

	@Lob
	@Column(columnDefinition = "varbinary")
	private byte[] someBytes;

	private Boolean someBoolean;

	private String someString;

	private Integer someInt;

	@Attribute(textFieldMode = AttributeTextFieldMode.TEXTAREA)
	private String someTextArea;

	@Attribute(trueRepresentation = "On", falseRepresentation = "Off")
	private Boolean someBoolean2;

	@OneToMany(mappedBy = "testEntity", cascade = CascadeType.ALL)
	@Attribute(searchable = SearchMode.ALWAYS)
	private Set<TestEntity2> testEntities = new HashSet<>();

	@Attribute(displayFormat = "HH:mm:ss")
	private LocalTime someTime;

	@ElementCollection
	@Attribute(maxLength = 25)
	private Set<String> tags = new HashSet<>();

	@Attribute(url = true)
	private String url;

	@Attribute(quickAddAllowed = true, navigable = true)
	private TestDomain testDomain;

	@ElementCollection
	private Set<Integer> intTags = new HashSet<>();

	@ElementCollection
	@Attribute(minValue = 34)
	private Set<Long> longTags = new HashSet<>();

	private Double someDouble;

	@Attribute(textFieldMode = AttributeTextFieldMode.PASSWORD)
	private String password;

	public TestEntity() {
		// default constructor
	}

	public TestEntity(int id, String name, Long age) {
		this.id = id;
		this.name = name;
		this.age = age;
	}

	public TestEntity(String name, Long age) {
		this.name = name;
		this.age = age;
	}

	public void addTestEntity2(TestEntity2 entity2) {
		this.testEntities.add(entity2);
		entity2.setTestEntity(this);
	}

	@AssertTrue
	public boolean isAssertSomething() {
		return !"bogus".equals(name);
	}

}
