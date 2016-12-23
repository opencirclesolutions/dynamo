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
package com.ocs.dynamo.domain.model.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import junitx.util.PrivateAccessor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.VisibilityType;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.AttributeGroup;
import com.ocs.dynamo.domain.model.annotation.AttributeGroups;
import com.ocs.dynamo.domain.model.annotation.AttributeOrder;
import com.ocs.dynamo.domain.model.annotation.Model;
import com.ocs.dynamo.domain.validator.Email;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.impl.MessageServiceImpl;
import com.ocs.dynamo.test.BaseMockitoTest;

@SuppressWarnings("unused")
public class EntityModelFactoryImplTest extends BaseMockitoTest {

	public EntityModelFactoryImpl factory = new EntityModelFactoryImpl();

	private ResourceBundleMessageSource source = new ResourceBundleMessageSource();

	private MessageService messageService = new MessageServiceImpl();

	@Before
	public void setupEntityModelFactoryTest() throws NoSuchFieldException {

		wireTestSubject(factory);

		source.setBasename("entitymodel");

		PrivateAccessor.setField(messageService, "source", source);
		PrivateAccessor.setField(factory, "messageService", messageService);
	}

	/**
	 * Test a simple entity that relies solely on defaults
	 */
	@Test
	public void testDefaults() {
		EntityModel<Entity1> model = factory.getModel(Entity1.class);
		Assert.assertNotNull(model);

		Assert.assertEquals("Entity1", model.getDisplayName());
		Assert.assertEquals("Entity1s", model.getDisplayNamePlural());
		Assert.assertEquals("Entity1", model.getDescription());
		Assert.assertNull(model.getDisplayProperty());

		AttributeModel nameModel = model.getAttributeModel("name");
		Assert.assertNotNull(nameModel);

		Assert.assertNull(nameModel.getDefaultValue());
		Assert.assertEquals("Name", nameModel.getPrompt());
		Assert.assertEquals("Name", nameModel.getDisplayName());
		Assert.assertEquals(4, nameModel.getOrder().intValue());
		Assert.assertEquals(String.class, nameModel.getType());
		Assert.assertNull(nameModel.getDisplayFormat());
		Assert.assertEquals(AttributeType.BASIC, nameModel.getAttributeType());
		Assert.assertFalse(nameModel.isRequired());
		Assert.assertTrue(nameModel.isVisible());
		Assert.assertEquals(55, nameModel.getMaxLength().intValue());

		Assert.assertTrue(nameModel.isSortable());
		Assert.assertTrue(nameModel.isMainAttribute());
		Assert.assertFalse(nameModel.isReadOnly());

		AttributeModel ageModel = model.getAttributeModel("age");
		Assert.assertNull(ageModel.getDefaultValue());
		Assert.assertEquals("Age", ageModel.getDisplayName());
		Assert.assertEquals(0, ageModel.getOrder().intValue());
		Assert.assertEquals(Integer.class, ageModel.getType());
		Assert.assertNull(nameModel.getDisplayFormat());
		Assert.assertEquals(AttributeType.BASIC, ageModel.getAttributeType());
		Assert.assertTrue(ageModel.isRequired());
		Assert.assertTrue(ageModel.isUseThousandsGrouping());

		AttributeModel birthDateModel = model.getAttributeModel("birthDate");
		Assert.assertNull(birthDateModel.getDefaultValue());
		Assert.assertEquals("Birth Date", birthDateModel.getDisplayName());
		Assert.assertEquals(1, birthDateModel.getOrder().intValue());
		Assert.assertEquals(Date.class, birthDateModel.getType());
		Assert.assertNotNull(birthDateModel.getDisplayFormat());
		Assert.assertEquals(AttributeType.BASIC, birthDateModel.getAttributeType());

		Assert.assertTrue(model.usesDefaultGroupOnly());

		AttributeModel weightModel = model.getAttributeModel("weight");
		Assert.assertEquals(2, weightModel.getPrecision());

		AttributeModel boolModel = model.getAttributeModel("bool");
		Assert.assertEquals("Yes", boolModel.getTrueRepresentation());
		Assert.assertEquals("No", boolModel.getFalseRepresentation());

		AttributeModel mailModel = model.getAttributeModel("email");
		Assert.assertTrue(mailModel.isEmail());

		AttributeModel urlModel = model.getAttributeModel("url");
		Assert.assertTrue(urlModel.isUrl());

		// test the total size
		Assert.assertEquals(7, model.getAttributeModels().size());
	}

	@Test
	public void testPropertyOrder() {
		EntityModel<Entity2> model = factory.getModel(Entity2.class);
		Assert.assertNotNull(model);

		AttributeModel nameModel = model.getAttributeModel("name");
		Assert.assertEquals(0, nameModel.getOrder().intValue());

		AttributeModel birthDateModel = model.getAttributeModel("birthDate");
		Assert.assertEquals(1, birthDateModel.getOrder().intValue());

		AttributeModel age = model.getAttributeModel("age");
		Assert.assertEquals(2, age.getOrder().intValue());
	}

	@Test
	public void testAttributeGroups() {
		EntityModel<Entity3> model = factory.getModel(Entity3.class);
		Assert.assertNotNull(model);

		Assert.assertEquals(3, model.getAttributeGroups().size());
		String group1 = model.getAttributeGroups().get(0);
		Assert.assertEquals("group 1", group1);

		List<AttributeModel> models = model.getAttributeModelsForGroup(group1);
		Assert.assertEquals("name", models.get(0).getName());

		String group2 = model.getAttributeGroups().get(1);
		Assert.assertEquals("group 2", group2);

		List<AttributeModel> models2 = model.getAttributeModelsForGroup(group2);
		Assert.assertEquals("age", models2.get(0).getName());

		List<AttributeModel> models3 = model.getAttributeModelsForGroup(EntityModel.DEFAULT_GROUP);
		Assert.assertEquals("birthDate", models3.get(0).getName());

		Assert.assertTrue(model.isAttributeGroupVisible(EntityModel.DEFAULT_GROUP, true));
		Assert.assertTrue(model.isAttributeGroupVisible(EntityModel.DEFAULT_GROUP, false));
	}

	/**
	 * Test that anything can be overwritten with annotations
	 */
	@Test
	public void testAnnotationOverrides() {
		EntityModel<Entity3> model = factory.getModel(Entity3.class);
		Assert.assertNotNull(model);

		// the following are overwritten using the message bundle
		Assert.assertEquals("dis", model.getDisplayName());
		Assert.assertEquals("diss", model.getDisplayNamePlural());
		Assert.assertEquals("desc", model.getDescription());
		Assert.assertEquals("prop", model.getDisplayProperty());

		AttributeModel nameModel = model.getAttributeModel("name");
		Assert.assertNotNull(nameModel);

		Assert.assertEquals("Bas", nameModel.getDefaultValue());
		Assert.assertEquals("Naampje", nameModel.getDisplayName());
		Assert.assertEquals("Test", nameModel.getDescription());
		Assert.assertEquals("Prompt", nameModel.getPrompt());
		Assert.assertEquals(String.class, nameModel.getType());
		Assert.assertNull(nameModel.getDisplayFormat());
		Assert.assertEquals(AttributeType.BASIC, nameModel.getAttributeType());
		Assert.assertFalse(nameModel.isSearchCaseSensitive());
		Assert.assertFalse(nameModel.isSearchPrefixOnly());

		Assert.assertFalse(nameModel.isSortable());
		Assert.assertTrue(nameModel.isSearchable());
		Assert.assertTrue(nameModel.isMainAttribute());
		Assert.assertTrue(nameModel.isReadOnly());

		AttributeModel ageModel = model.getAttributeModel("age");
		Assert.assertNotNull(ageModel);
		Assert.assertTrue(ageModel.isSearchCaseSensitive());
		Assert.assertTrue(ageModel.isSearchPrefixOnly());
		Assert.assertFalse(ageModel.isUseThousandsGrouping());

		AttributeModel entityModel = model.getAttributeModel("entity2");
		Assert.assertEquals(AttributeType.MASTER, entityModel.getAttributeType());
		Assert.assertTrue(entityModel.isComplexEditable());

		AttributeModel entityListModel = model.getAttributeModel("entityList");
		Assert.assertEquals(AttributeType.DETAIL, entityListModel.getAttributeType());

		AttributeModel birthDateModel = model.getAttributeModel("birthDate");
		Assert.assertEquals("dd/MM/yyyy", birthDateModel.getDisplayFormat());

		// test that attribute annotations on getters are also picked up
		AttributeModel derivedModel = model.getAttributeModel("derived");
		Assert.assertNotNull(derivedModel);
		Assert.assertEquals("deri", derivedModel.getDisplayName());

		AttributeModel weightModel = model.getAttributeModel("weight");
		Assert.assertNotNull(weightModel);
		Assert.assertEquals(4, weightModel.getPrecision());
		Assert.assertTrue(weightModel.isCurrency());
	}

	@Test
	public void testReference() {
		EntityModel<Entity6> model = factory.getModel("Special", Entity6.class);
		Assert.assertEquals("Special", model.getDisplayName());
		Assert.assertEquals("SpecialPlural", model.getDisplayNamePlural());
	}

	@Test
	public void testMessageOverrides() {
		EntityModel<Entity6> model = factory.getModel(Entity6.class);
		Assert.assertNotNull(model);

		// the following are overwritten using the message bundle
		Assert.assertEquals("Override", model.getDisplayName());
		Assert.assertEquals("Overrides", model.getDisplayNamePlural());
		Assert.assertEquals("Description override", model.getDescription());
		Assert.assertEquals("Prop", model.getDisplayProperty());

		AttributeModel nameModel = model.getAttributeModel("name");
		Assert.assertNotNull(nameModel);

		Assert.assertTrue(nameModel.isSearchCaseSensitive());
		Assert.assertTrue(nameModel.isSearchPrefixOnly());

		Assert.assertEquals("Override", nameModel.getDisplayName());
		Assert.assertEquals("Prompt override", nameModel.getPrompt());
		Assert.assertTrue(nameModel.isReadOnly());

		Assert.assertFalse(model.usesDefaultGroupOnly());

		String group1 = model.getAttributeGroups().get(0);
		Assert.assertEquals("group 1", model.getAttributeGroups().get(0));

		String group2 = model.getAttributeGroups().get(1);
		Assert.assertEquals("group 2", group2);

		Assert.assertEquals("age", model.getAttributeModelsForGroup(group2).get(0).getName());
		Assert.assertEquals("birthDate", model.getAttributeModelsForGroup(group2).get(1).getName());

		String group3 = model.getAttributeGroups().get(2);
		Assert.assertEquals(EntityModel.DEFAULT_GROUP, group3);
	}

	@Test
	public void testLob() {
		EntityModel<Entity5> model = factory.getModel(Entity5.class);
		AttributeModel attributeModel = model.getAttributeModel("logo");
		Assert.assertEquals(AttributeType.LOB, attributeModel.getAttributeType());
		Assert.assertTrue(attributeModel.isImage());
		Assert.assertTrue(attributeModel.getAllowedExtensions().contains("gif"));
		Assert.assertTrue(attributeModel.getAllowedExtensions().contains("bmp"));
	}

	/**
	 * Test that a field that has an "@AssertTrue" annotation is ignored
	 */
	@Test
	public void testAssertTrueIgnored() {
		EntityModel<Entity5> model = factory.getModel(Entity5.class);
		AttributeModel attributeModel = model.getAttributeModel("someValidation");
		Assert.assertNull(attributeModel);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testNestedEntityModel() {
		// Check MASTER
		EntityModel<EntityChild> child = factory.getModel(EntityChild.class);
		AttributeModel attributeModel = child.getAttributeModel("parent");
		Assert.assertNotNull(attributeModel);
		EntityModel<EntityParent> parent = (EntityModel<EntityParent>) attributeModel.getNestedEntityModel();
		Assert.assertNotNull(parent);
		Assert.assertEquals("EntityChild.parent", parent.getReference());
		// Check DETAIL
		parent = factory.getModel(EntityParent.class);
		attributeModel = parent.getAttributeModel("children");
		Assert.assertNotNull(attributeModel);
		child = (EntityModel<EntityChild>) attributeModel.getNestedEntityModel();
		Assert.assertNotNull(child);
		Assert.assertEquals("EntityParent.children", child.getReference());

		// check a detail property that does not map directly to a field
		attributeModel = parent.getAttributeModel("calculatedChildren");
		Assert.assertNotNull(attributeModel);
		EntityModel<EntityGrandChild> grandChild = (EntityModel<EntityGrandChild>) attributeModel
		        .getNestedEntityModel();
		Assert.assertNotNull(grandChild);
		Assert.assertEquals("EntityParent.calculatedChildren", grandChild.getReference());
		Assert.assertEquals(EntityGrandChild.class, attributeModel.getMemberType());
		Assert.assertEquals("children", attributeModel.getReplacementSearchPath());

		// lazily constructed models are not there yet
		Assert.assertFalse(factory.hasModel("EntityChild.parent.children"));
		Assert.assertFalse(factory.hasModel("EntityParent.children.parent"));

		// check on demand constrution of model
		EntityModel<EntityChild> child2 = factory.getModel("EntityChild.parent.children", EntityChild.class);
		Assert.assertNotNull(child2);

		// check that the nested model attribute is not searchable...
		EntityModel<EntityChild> childModel = factory.getModel("EntityChild.parent", EntityChild.class);
		Assert.assertNotNull(childModel);
		Assert.assertFalse(childModel.getAttributeModel("name").isSearchable());

		// .. but the parent is
		EntityModel<EntityParent> parentModel = factory.getModel(EntityParent.class);
		Assert.assertTrue(parentModel.getAttributeModel("name").isSearchable());

		// check that the search paths are set recursively
		Assert.assertEquals("name", parentModel.getAttributeModel("name").getPath());
		Assert.assertEquals("parent.name", childModel.getAttributeModel("name").getPath());
	}

	@Test
	public void testId() {
		EntityModel<EntityParent> model = factory.getModel(EntityParent.class);
		AttributeModel attributeModel = model.getIdAttributeModel();
		Assert.assertNotNull(attributeModel);
	}

	@Test
	public void testGetAttributeModelsForType() {
		// Find by both
		EntityModel<EntityChild> model = factory.getModel(EntityChild.class);
		List<AttributeModel> models = model.getAttributeModelsForType(AttributeType.MASTER, EntityParent.class);
		Assert.assertNotNull(models);
		Assert.assertEquals(2, models.size());
		Assert.assertEquals("parent", models.get(0).getName());
		// Find by attribute type
		model = factory.getModel(EntityChild.class);
		models = model.getAttributeModelsForType(AttributeType.MASTER, null);
		Assert.assertNotNull(models);
		Assert.assertEquals(2, models.size());
		Assert.assertEquals("parent", models.get(0).getName());
		// Find by type
		EntityModel<EntityParent> pmodel = factory.getModel(EntityParent.class);
		models = pmodel.getAttributeModelsForType(null, EntityChild.class);
		Assert.assertNotNull(models);
		Assert.assertEquals(1, models.size());
		Assert.assertEquals("children", models.get(0).getName());
	}

	@Test
	public void testNestedAttributes() {

		EntityModel<EntityParent> parentModel = factory.getModel(EntityParent.class);
		AttributeModel pm = parentModel.getAttributeModel("name");
		Assert.assertNotNull(pm);
		Assert.assertEquals("name", pm.getName());
		Assert.assertTrue(pm.isVisibleInTable());

		EntityModel<EntityChild> model = factory.getModel(EntityChild.class);
		AttributeModel am = model.getAttributeModel("parent.name");
		Assert.assertNotNull(am);
		Assert.assertEquals("name", am.getName());
		Assert.assertEquals(EntityParent.class, am.getEntityModel().getEntityClass());
		Assert.assertFalse(am.isVisibleInTable());

	}

	@Test
	public void testSortOrder() {
		// Test success
		EntityModel<EntityParent> model = factory.getModel(EntityParent.class);
		Assert.assertNotNull(model.getSortOrder());
		Assert.assertEquals(2, model.getSortOrder().size());
		AttributeModel amName = model.getAttributeModel("name");
		AttributeModel amId = model.getAttributeModel("id");
		Assert.assertEquals(false, model.getSortOrder().get(amName));
		Assert.assertEquals(true, model.getSortOrder().get(amId));
		// Test failure
		EntityModel<EntitySortError> semodel = factory.getModel(EntitySortError.class);
		Assert.assertNotNull(semodel.getSortOrder());
		Assert.assertEquals(1, semodel.getSortOrder().size());
		amName = semodel.getAttributeModel("code");
		Assert.assertEquals(true, semodel.getSortOrder().get(amName));
	}

	@Test
	public void testEmbedded() {
		EntityModel<EmbeddedParent> model = factory.getModel(EmbeddedParent.class);
		Assert.assertNotNull(model.getAttributeModel("name"));
		Assert.assertEquals(AttributeType.BASIC, model.getAttributeModel("name").getAttributeType());

		// there must not be a separate model for the embedded object
		Assert.assertNull(model.getAttributeModel("child"));

		AttributeModel m = model.getAttributeModel("child.embedded2");
		Assert.assertNotNull(m);
		Assert.assertTrue(m.isSearchable());

		// visible attribute is overridden using message bundle
		Assert.assertFalse(m.isVisible());

		// nested embedding
		Assert.assertNull(model.getAttributeModel("child.grandChild"));

		AttributeModel m2 = model.getAttributeModel("child.grandChild.sometAttribute");
		Assert.assertNotNull(m2);

	}

	@Test
	public void testSelectMode() {
		EntityModel<Entity7> model = factory.getModel(Entity7.class);

		// overridden
		AttributeModel am = model.getAttributeModel("entity6");
		Assert.assertEquals(AttributeSelectMode.LOOKUP, am.getSelectMode());
		Assert.assertEquals(AttributeSelectMode.LOOKUP, am.getSearchSelectMode());

		// default
		AttributeModel am2 = model.getAttributeModel("entity5");
		Assert.assertEquals(AttributeSelectMode.COMBO, am2.getSelectMode());
		// multiple search, defaults to token
		Assert.assertEquals(AttributeSelectMode.TOKEN, am2.getSearchSelectMode());

		// overwritten search mode
		AttributeModel am3 = model.getAttributeModel("entity52");
		Assert.assertEquals(AttributeSelectMode.COMBO, am3.getSelectMode());
		Assert.assertEquals(AttributeSelectMode.TOKEN, am3.getSearchSelectMode());
	}

	@Test
	public void testDateType() {
		EntityModel<Entity8> model = factory.getModel(Entity8.class);

		// default
		AttributeModel am = model.getAttributeModel("date1");
		Assert.assertEquals(AttributeDateType.DATE, am.getDateType());
		Assert.assertEquals("dd-MM-yyyy", am.getDisplayFormat());

		// temporal annotation
		am = model.getAttributeModel("date2");
		Assert.assertEquals(AttributeDateType.TIMESTAMP, am.getDateType());
		Assert.assertEquals("dd-MM-yyyy HH:mm:ss", am.getDisplayFormat());

		// overridden @Temporal annotation
		am = model.getAttributeModel("date3");
		Assert.assertEquals(AttributeDateType.TIME, am.getDateType());
		Assert.assertEquals("HH:mm:ss", am.getDisplayFormat());

		// overridden annotation
		am = model.getAttributeModel("date4");
		Assert.assertEquals(AttributeDateType.TIME, am.getDateType());
		Assert.assertEquals("ss:mm:HH", am.getDisplayFormat());

		// overridden annotation
		am = model.getAttributeModel("date5");
		Assert.assertEquals(AttributeDateType.DATE, am.getDateType());
		Assert.assertEquals("yyyy-dd-MM ss:mm:HH", am.getDisplayFormat());

		// defaults
		am = model.getAttributeModel("date6");
		Assert.assertEquals(AttributeDateType.DATE, am.getDateType());
		Assert.assertEquals("dd-MM-yyyy", am.getDisplayFormat());
	}

	@Test
	public void testElementCollection() {
		EntityModel<Entity9> model = factory.getModel(Entity9.class);

		AttributeModel am = model.getAttributeModel("elements");
		Assert.assertEquals(AttributeType.ELEMENT_COLLECTION, am.getAttributeType());
		Assert.assertEquals("element_table", am.getCollectionTableName());
		Assert.assertEquals("element", am.getCollectionTableFieldName());
	}

	@Test
	public void testGroupTogetherWith() {
		EntityModel<Entity10> model = factory.getModel(Entity10.class);

		AttributeModel am = model.getAttributeModel("attribute1");
		Assert.assertEquals(1, am.getGroupTogetherWith().size());
		Assert.assertEquals("attribute2", am.getGroupTogetherWith().get(0));
	}

	/**
	 * "group together with" attributes are in the wrong order
	 */
	@Test
	public void testGroupTogetherWithWrongOrder() {
		EntityModel<Entity11> model = factory.getModel(Entity11.class);

		AttributeModel am = model.getAttributeModel("attribute2");
		Assert.assertEquals(1, am.getGroupTogetherWith().size());
		Assert.assertEquals("attribute1", am.getGroupTogetherWith().get(0));

		AttributeModel am1 = model.getAttributeModel("attribute1");
		Assert.assertTrue(am1.isAlreadyGrouped());
	}

	private class Entity1 {

		@Size(max = 55)
		@Attribute(main = true)
		private String name;

		@NotNull
		private Integer age;

		private BigDecimal weight;

		@Temporal(TemporalType.DATE)
		private Date birthDate;

		@Attribute(trueRepresentation = "Yes", falseRepresentation = "No")
		private Boolean bool;

		@Email
		private String email;

		@Attribute(url = true)
		private String url;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

		public Date getBirthDate() {
			return birthDate;
		}

		public void setBirthDate(Date birthDate) {
			this.birthDate = birthDate;
		}

		public BigDecimal getWeight() {
			return weight;
		}

		public void setWeight(BigDecimal weight) {
			this.weight = weight;
		}

		public Boolean getBool() {
			return bool;
		}

		public void setBool(Boolean bool) {
			this.bool = bool;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

	}

	@AttributeOrder(attributeNames = { "name", "birthDate" })
	private class Entity2 {

		private String name;

		private Integer age;

		@Temporal(TemporalType.DATE)
		private Date birthDate;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

		public Date getBirthDate() {
			return birthDate;
		}

		public void setBirthDate(Date birthDate) {
			this.birthDate = birthDate;
		}
	}

	@Model(description = "desc", displayName = "dis", displayNamePlural = "diss", displayProperty = "prop")
	@AttributeGroups(attributeGroups = { @AttributeGroup(displayName = "group 1", attributeNames = { "name" }),
	        @AttributeGroup(displayName = "group 2", attributeNames = { "age" }) })
	private class Entity3 {

		@Attribute(defaultValue = "Bas", description = "Test", displayName = "Naampje", readOnly = true, prompt = "Prompt", searchable = true, main = true, sortable = false)
		private String name;

		@Attribute(searchCaseSensitive = true, searchPrefixOnly = true, useThousandsGrouping = false)
		private Integer age;

		@Attribute(displayFormat = "dd/MM/yyyy")
		@Temporal(TemporalType.DATE)
		private Date birthDate;

		@OneToOne
		@Attribute(complexEditable = true)
		private Entity2 entity2;

		@OneToMany
		private List<Entity4> entityList;

		@Attribute(precision = 4, currency = true)
		private BigDecimal weight;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

		public Date getBirthDate() {
			return birthDate;
		}

		public void setBirthDate(Date birthDate) {
			this.birthDate = birthDate;
		}

		public Entity2 getEntity2() {
			return entity2;
		}

		public void setEntity2(Entity2 entity2) {
			this.entity2 = entity2;
		}

		public List<Entity4> getEntityList() {
			return entityList;
		}

		public void setEntityList(List<Entity4> entityList) {
			this.entityList = entityList;
		}

		@Attribute(displayName = "deri")
		public String getDerived() {
			return "test";
		}

		public BigDecimal getWeight() {
			return weight;
		}

		public void setWeight(BigDecimal weight) {
			this.weight = weight;
		}

	}

	private class Entity6 {

		private String name;

		private Integer age;

		private Date birthDate;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

		public Date getBirthDate() {
			return birthDate;
		}

		public void setBirthDate(Date birthDate) {
			this.birthDate = birthDate;
		}

	}

	/**
	 * For testing attribute lookup mode
	 * 
	 * @author bas.rutten
	 */
	private class Entity7 {

		@Attribute(selectMode = AttributeSelectMode.LOOKUP)
		private Entity6 entity6;

		@Attribute(multipleSearch = true)
		private Entity5 entity5;

		@Attribute(multipleSearch = true, searchSelectMode = AttributeSelectMode.TOKEN)
		private Entity5 entity52;

		public Entity6 getEntity6() {
			return entity6;
		}

		public void setEntity6(Entity6 entity6) {
			this.entity6 = entity6;
		}

		public Entity5 getEntity5() {
			return entity5;
		}

		public void setEntity5(Entity5 entity5) {
			this.entity5 = entity5;
		}

		public Entity5 getEntity52() {
			return entity52;
		}

		public void setEntity52(Entity5 entity52) {
			this.entity52 = entity52;
		}

	}

	private class Entity8 {

		private Date date1;

		@Temporal(TemporalType.TIMESTAMP)
		private Date date2;

		@Temporal(TemporalType.DATE)
		@Attribute(dateType = AttributeDateType.TIME)
		private Date date3;

		@Attribute(dateType = AttributeDateType.TIME, displayFormat = "ss:mm:HH")
		private Date date4;

		@Attribute(displayFormat = "yyyy-dd-MM ss:mm:HH")
		private Date date5;

		@Attribute
		private Date date6;

		public Date getDate1() {
			return date1;
		}

		public void setDate1(Date date1) {
			this.date1 = date1;
		}

		public Date getDate2() {
			return date2;
		}

		public void setDate2(Date date2) {
			this.date2 = date2;
		}

		public Date getDate3() {
			return date3;
		}

		public void setDate3(Date date3) {
			this.date3 = date3;
		}

		public Date getDate4() {
			return date4;
		}

		public void setDate4(Date date4) {
			this.date4 = date4;
		}

		public Date getDate5() {
			return date5;
		}

		public void setDate5(Date date5) {
			this.date5 = date5;
		}

		public Date getDate6() {
			return date6;
		}

		public void setDate6(Date date6) {
			this.date6 = date6;
		}
	}

	private class Entity9 {

		@ElementCollection
		@CollectionTable(name = "element_table")
		@Column(name = "element")
		private Set<String> elements = new HashSet<>();

		public Set<String> getElements() {
			return elements;
		}

		public void setElements(Set<String> elements) {
			this.elements = elements;
		}
	}

	private class Entity10 {

		@Attribute(groupTogetherWith = "attribute2")
		private String attribute1;

		private String attribute2;

		public String getAttribute1() {
			return attribute1;
		}

		public void setAttribute1(String attribute1) {
			this.attribute1 = attribute1;
		}

		public String getAttribute2() {
			return attribute2;
		}

		public void setAttribute2(String attribute2) {
			this.attribute2 = attribute2;
		}

	}

	@AttributeOrder(attributeNames = { "attribute1", "attribute2" })
	private class Entity11 {

		private String attribute1;

		@Attribute(groupTogetherWith = "attribute1")
		private String attribute2;

		public String getAttribute1() {
			return attribute1;
		}

		public void setAttribute1(String attribute1) {
			this.attribute1 = attribute1;
		}

		public String getAttribute2() {
			return attribute2;
		}

		public void setAttribute2(String attribute2) {
			this.attribute2 = attribute2;
		}

	}

	private class Entity4 {

	}

	private class Entity5 {

		@Lob
		@Basic(fetch = FetchType.LAZY)
		@Attribute(image = true, allowedExtensions = { "gif", "bmp" })
		private byte[] logo;

		public byte[] getLogo() {
			return logo;
		}

		public void setLogo(byte[] logo) {
			this.logo = logo;
		}

		@AssertTrue
		public boolean isSomeValidation() {
			return true;
		}
	}

	@Model(sortOrder = "name desc, id asc")
	private class EntityParent {
		@Id
		private int id;

		@Attribute(searchable = true)
		private String name;

		@OneToMany
		private List<EntityChild> children;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<EntityChild> getChildren() {
			return children;
		}

		public void setChildren(List<EntityChild> children) {
			this.children = children;
		}

		@Attribute(memberType = EntityGrandChild.class, replacementSearchPath = "children")
		public List<EntityGrandChild> getCalculatedChildren() {
			return null;
		}

		public void setCalculatedChildren(List<EntityGrandChild> children) {
			// do nothing
		}
	}

	private class EntityChild {
		@Id
		private int id;
		private String name;
		private EntityParent parent;
		private EntityParent parent2;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public EntityParent getParent() {
			return parent;
		}

		public void setParent(EntityParent parent) {
			this.parent = parent;
		}

		public EntityParent getParent2() {
			return parent2;
		}

		public void setParent2(EntityParent parent2) {
			this.parent2 = parent2;
		}
	}

	private class EntityGrandChild extends EntityChild {

	}

	@Model(sortOrder = "code unknown, unknown asc")
	private class EntitySortError {
		private String code;
		private String name;

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	@AttributeOrder(attributeNames = { "child.embedded1", "child.embedded2", "name" })
	private class EmbeddedParent {

		private String name;

		@Embedded
		private EmbeddedChild child;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public EmbeddedChild getChild() {
			return child;
		}

		public void setChild(EmbeddedChild child) {
			this.child = child;
		}

	}

	@Embeddable
	private class EmbeddedChild {

		@Attribute(visible = VisibilityType.HIDE)
		private String embedded1;

		@Attribute(searchable = true)
		private String embedded2;

		@Embedded
		private EmbeddedGrandChild grandChild;

		public String getEmbedded1() {
			return embedded1;
		}

		public void setEmbedded1(String embedded1) {
			this.embedded1 = embedded1;
		}

		public String getEmbedded2() {
			return embedded2;
		}

		public void setEmbedded2(String embedded2) {
			this.embedded2 = embedded2;
		}

		public EmbeddedGrandChild getGrandChild() {
			return grandChild;
		}

		public void setGrandChild(EmbeddedGrandChild grandChild) {
			this.grandChild = grandChild;
		}
	}

	@Embeddable
	private class EmbeddedGrandChild {

		private String sometAttribute;

		public String getSometAttribute() {
			return sometAttribute;
		}

		public void setSometAttribute(String sometAttribute) {
			this.sometAttribute = sometAttribute;
		}
	}

}
