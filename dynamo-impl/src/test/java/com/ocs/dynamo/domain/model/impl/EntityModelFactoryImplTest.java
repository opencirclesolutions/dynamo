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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
import javax.persistence.Transient;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.AttributeTextFieldMode;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.CascadeMode;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.VisibilityType;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.AttributeGroup;
import com.ocs.dynamo.domain.model.annotation.AttributeOrder;
import com.ocs.dynamo.domain.model.annotation.Cascade;
import com.ocs.dynamo.domain.model.annotation.CustomSetting;
import com.ocs.dynamo.domain.model.annotation.CustomType;
import com.ocs.dynamo.domain.model.annotation.Model;
import com.ocs.dynamo.domain.model.annotation.SearchMode;
import com.ocs.dynamo.domain.model.validator.Email;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.impl.MessageServiceImpl;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.utils.DateUtils;

@SuppressWarnings("unused")
public class EntityModelFactoryImplTest extends BaseMockitoTest {

	private EntityModelFactoryImpl factory = new EntityModelFactoryImpl();

	private ResourceBundleMessageSource source = new ResourceBundleMessageSource();

	private MessageService messageService = new MessageServiceImpl();

	private Locale locale = new Locale("en");

	@BeforeEach
	public void setupEntityModelFactoryTest() throws NoSuchFieldException {

		System.setProperty("ocs.use.default.prompt.value", "true");

		source.setBasename("META-INF/entitymodel");
		ReflectionTestUtils.setField(messageService, "source", source);
		ReflectionTestUtils.setField(factory, "messageService", messageService);
	}

	/**
	 * Test a simple entity that relies solely on defaults
	 */
	@Test
	public void testDefaults() {
		EntityModel<Entity1> model = factory.getModel(Entity1.class);
		assertNotNull(model);

		assertEquals("Entity1", model.getDisplayName(locale));
		assertEquals("Entity1s", model.getDisplayNamePlural(locale));
		assertEquals("Entity1", model.getDescription(locale));
		assertNull(model.getDisplayProperty());

		AttributeModel nameModel = model.getAttributeModel("name");
		assertNotNull(nameModel);

		assertNull(nameModel.getDefaultValue());
		assertEquals("Name", nameModel.getPrompt(locale));
		assertEquals("Name", nameModel.getDisplayName(locale));
		assertEquals(4, nameModel.getOrder().intValue());
		assertEquals(String.class, nameModel.getType());
		assertNull(nameModel.getDisplayFormat());
		assertEquals(AttributeType.BASIC, nameModel.getAttributeType());
		assertFalse(nameModel.isRequired());
		assertTrue(nameModel.isVisible());
		assertEquals(55, nameModel.getMaxLength().intValue());
		assertEquals(AttributeTextFieldMode.TEXTAREA, nameModel.getTextFieldMode());

		assertEquals("ross", nameModel.getCustomSetting("bob"));
		assertEquals(4, nameModel.getCustomSetting("bobInt"));
		assertEquals(true, nameModel.getCustomSetting("bobBool"));

		assertTrue(nameModel.isSortable());
		assertTrue(nameModel.isMainAttribute());
		assertEquals(EditableType.EDITABLE, nameModel.getEditableType());

		AttributeModel ageModel = model.getAttributeModel("age");
		assertNull(ageModel.getDefaultValue());
		assertEquals("Age", ageModel.getDisplayName(locale));
		assertEquals(0, ageModel.getOrder().intValue());
		assertEquals(Integer.class, ageModel.getType());
		assertNull(nameModel.getDisplayFormat());
		assertEquals(AttributeType.BASIC, ageModel.getAttributeType());
		assertTrue(ageModel.isRequired());
		assertTrue(ageModel.isThousandsGrouping());

		AttributeModel birthDateModel = model.getAttributeModel("birthDate");
		assertNull(birthDateModel.getDefaultValue());
		assertEquals("Birth Date", birthDateModel.getDisplayName(locale));
		assertEquals(1, birthDateModel.getOrder().intValue());
		assertEquals(LocalDate.class, birthDateModel.getType());
		assertNotNull(birthDateModel.getDisplayFormat());
		assertEquals(AttributeType.BASIC, birthDateModel.getAttributeType());

		assertTrue(model.usesDefaultGroupOnly());

		AttributeModel weightModel = model.getAttributeModel("weight");
		assertEquals(2, weightModel.getPrecision());

		AttributeModel boolModel = model.getAttributeModel("bool");
		assertEquals("Yes", boolModel.getTrueRepresentation(locale));
		assertEquals("No", boolModel.getFalseRepresentation(locale));

		AttributeModel mailModel = model.getAttributeModel("email");
		assertTrue(mailModel.isEmail());

		AttributeModel urlModel = model.getAttributeModel("url");
		assertTrue(urlModel.isUrl());

		// test the total size
		assertEquals(7, model.getAttributeModels().size());
	}

	@Test
	public void testPropertyOrder() {
		EntityModel<Entity2> model = factory.getModel(Entity2.class);
		assertNotNull(model);

		AttributeModel nameModel = model.getAttributeModel("name");
		assertEquals(0, nameModel.getOrder().intValue());

		AttributeModel birthDateModel = model.getAttributeModel("birthDate");
		assertEquals(1, birthDateModel.getOrder().intValue());

		AttributeModel age = model.getAttributeModel("age");
		assertEquals(2, age.getOrder().intValue());
	}

	@Test
	public void testAttributeGroups() {
		EntityModel<Entity3> model = factory.getModel(Entity3.class);
		assertNotNull(model);

		assertEquals(3, model.getAttributeGroups().size());
		String group1 = model.getAttributeGroups().get(0);
		assertEquals("group1.key", group1);

		List<AttributeModel> models = model.getAttributeModelsForGroup(group1);
		assertEquals("name", models.get(0).getName());
		assertEquals(SearchMode.ALWAYS, models.get(0).getSearchMode());

		String group2 = model.getAttributeGroups().get(1);
		assertEquals("group2.key", group2);

		List<AttributeModel> models2 = model.getAttributeModelsForGroup(group2);
		assertEquals("age", models2.get(0).getName());
		assertTrue(models2.get(0).isRequiredForSearching());

		assertEquals(1, model.getRequiredForSearchingAttributeModels().size());

		List<AttributeModel> models3 = model.getAttributeModelsForGroup(EntityModel.DEFAULT_GROUP);
		assertEquals("advancedAge", models3.get(0).getName());

		assertTrue(model.isAttributeGroupVisible(EntityModel.DEFAULT_GROUP, true));
		assertTrue(model.isAttributeGroupVisible(EntityModel.DEFAULT_GROUP, false));

		AttributeModel advancedAgeModel = model.getAttributeModel("advancedAge");
		assertEquals(SearchMode.ADVANCED, advancedAgeModel.getSearchMode());
	}

	/**
	 * Test that anything can be overwritten with annotations
	 */
	@Test
	public void testAnnotationOverrides() {
		EntityModel<Entity3> model = factory.getModel(Entity3.class);
		assertNotNull(model);

		// the following are overwritten using the annotation
		assertEquals("dis", model.getDisplayName(locale));
		assertEquals("diss", model.getDisplayNamePlural(locale));
		assertEquals("desc", model.getDescription(locale));
		assertEquals("prop", model.getDisplayProperty());

		AttributeModel nameModel = model.getAttributeModel("name");
		assertNotNull(nameModel);

		assertEquals("Bas", nameModel.getDefaultValue());
		assertEquals("Naampje", nameModel.getDisplayName(locale));
		assertEquals("Test", nameModel.getDescription(locale));
		assertEquals("Prompt", nameModel.getPrompt(locale));
		assertEquals("myStyle", nameModel.getStyles());
		assertEquals(String.class, nameModel.getType());
		assertNull(nameModel.getDisplayFormat());
		assertEquals(AttributeType.BASIC, nameModel.getAttributeType());
		assertFalse(nameModel.isSearchCaseSensitive());
		assertFalse(nameModel.isSearchPrefixOnly());

		assertFalse(nameModel.isSortable());
		assertTrue(nameModel.isSearchable());
		assertTrue(nameModel.isMainAttribute());
		assertEquals(EditableType.READ_ONLY, nameModel.getEditableType());

		AttributeModel ageModel = model.getAttributeModel("age");
		assertNotNull(ageModel);
		assertTrue(ageModel.isSearchCaseSensitive());
		assertTrue(ageModel.isSearchPrefixOnly());
		assertFalse(ageModel.isThousandsGrouping());

		AttributeModel entityModel = model.getAttributeModel("entity2");
		assertEquals(AttributeType.MASTER, entityModel.getAttributeType());
		assertTrue(entityModel.isComplexEditable());
		assertTrue(entityModel.isNavigable());

		AttributeModel entityListModel = model.getAttributeModel("entityList");
		assertEquals(AttributeType.DETAIL, entityListModel.getAttributeType());

		AttributeModel birthDateModel = model.getAttributeModel("birthDate");
		assertEquals("dd/MM/yyyy", birthDateModel.getDisplayFormat());

		// test that attribute annotations on getters are also picked up
		AttributeModel derivedModel = model.getAttributeModel("derived");
		assertNotNull(derivedModel);
		assertEquals("deri", derivedModel.getDisplayName(locale));

		AttributeModel weightModel = model.getAttributeModel("weight");
		assertNotNull(weightModel);
		assertEquals(4, weightModel.getPrecision());
		assertTrue(weightModel.isCurrency());
	}

	@Test
	public void testReference() {
		EntityModel<Entity6> model = factory.getModel("Special", Entity6.class);
		assertEquals("Special", model.getDisplayName(locale));
		assertEquals("SpecialPlural", model.getDisplayNamePlural(locale));
	}

	@Test
	public void testMessageOverrides() {
		EntityModel<Entity6> model = factory.getModel(Entity6.class);
		assertNotNull(model);

		// the following are overwritten using the message bundle
		assertEquals("Override", model.getDisplayName(locale));
		assertEquals("Overrides", model.getDisplayNamePlural(locale));
		assertEquals("Description override", model.getDescription(locale));
		assertEquals("Prop", model.getDisplayProperty());

		AttributeModel nameModel = model.getAttributeModel("name");
		assertNotNull(nameModel);

		assertTrue(nameModel.isSearchCaseSensitive());
		assertTrue(nameModel.isSearchPrefixOnly());

		assertEquals("customValue", nameModel.getCustomSetting("custom"));
		assertEquals(4, nameModel.getCustomSetting("custom2"));
		assertEquals(true, nameModel.getCustomSetting("custom3"));

		assertEquals("Override", nameModel.getDisplayName(locale));
		assertEquals("Prompt override", nameModel.getPrompt(locale));
		assertEquals("Style override", nameModel.getStyles());
		assertEquals(EditableType.CREATE_ONLY, nameModel.getEditableType());
		assertEquals("Style override", nameModel.getStyles());

		assertFalse(model.usesDefaultGroupOnly());

		String group1 = model.getAttributeGroups().get(0);
		assertEquals("group1", model.getAttributeGroups().get(0));

		String group2 = model.getAttributeGroups().get(1);
		assertEquals("group2", group2);

		assertEquals("age", model.getAttributeModelsForGroup(group2).get(0).getName());
		assertEquals("birthDate", model.getAttributeModelsForGroup(group2).get(1).getName());

		String group3 = model.getAttributeGroups().get(2);
		assertEquals(EntityModel.DEFAULT_GROUP, group3);
	}

	@Test
	public void testLob() {
		EntityModel<Entity5> model = factory.getModel(Entity5.class);
		AttributeModel attributeModel = model.getAttributeModel("logo");
		assertEquals(AttributeType.LOB, attributeModel.getAttributeType());
		assertTrue(attributeModel.isImage());
		assertTrue(attributeModel.getAllowedExtensions().contains("gif"));
		assertTrue(attributeModel.getAllowedExtensions().contains("bmp"));
	}

	/**
	 * Test that a field that has an "@AssertTrue" annotation is ignored
	 */
	@Test
	public void testAssertTrueIgnored() {
		EntityModel<Entity5> model = factory.getModel(Entity5.class);
		AttributeModel attributeModel = model.getAttributeModel("someValidation");
		assertNull(attributeModel);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testNestedEntityModel() {
		// Check MASTER
		EntityModel<EntityChild> child = factory.getModel(EntityChild.class);
		AttributeModel attributeModel = child.getAttributeModel("parent");
		assertNotNull(attributeModel);
		EntityModel<EntityParent> parent = (EntityModel<EntityParent>) attributeModel.getNestedEntityModel();
		assertNotNull(parent);
		assertEquals("EntityChild.parent", parent.getReference());
		// Check DETAIL
		parent = factory.getModel(EntityParent.class);
		attributeModel = parent.getAttributeModel("children");
		assertNotNull(attributeModel);
		child = (EntityModel<EntityChild>) attributeModel.getNestedEntityModel();
		assertNotNull(child);
		assertEquals("EntityParent.children", child.getReference());

		// check a detail property that does not map directly to a field
		attributeModel = parent.getAttributeModel("calculatedChildren");
		assertNotNull(attributeModel);
		EntityModel<EntityGrandChild> grandChild = (EntityModel<EntityGrandChild>) attributeModel
				.getNestedEntityModel();
		assertNotNull(grandChild);
		assertEquals("EntityParent.calculatedChildren", grandChild.getReference());
		assertEquals(EntityGrandChild.class, attributeModel.getMemberType());
		assertEquals("children", attributeModel.getReplacementSearchPath());

		// lazily constructed models are not there yet
		assertFalse(factory.hasModel("EntityChild.parent.children"));
		assertFalse(factory.hasModel("EntityParent.children.parent"));

		// check on demand constrution of model
		EntityModel<EntityChild> child2 = factory.getModel("EntityChild.parent.children", EntityChild.class);
		assertNotNull(child2);

		// check that the nested model attribute is not searchable...
		EntityModel<EntityChild> childModel = factory.getModel("EntityChild.parent", EntityChild.class);
		assertNotNull(childModel);
		assertFalse(childModel.getAttributeModel("name").isSearchable());

		// .. but the parent is
		EntityModel<EntityParent> parentModel = factory.getModel(EntityParent.class);
		assertTrue(parentModel.getAttributeModel("name").isSearchable());

		// check that the search paths are set recursively
		assertEquals("name", parentModel.getAttributeModel("name").getPath());
		assertEquals("parent.name", childModel.getAttributeModel("name").getPath());
	}

	@Test
	public void testId() {
		EntityModel<EntityParent> model = factory.getModel(EntityParent.class);
		AttributeModel attributeModel = model.getIdAttributeModel();
		assertNotNull(attributeModel);
	}

	@Test
	public void testGetAttributeModelsForType() {
		// Find by both
		EntityModel<EntityChild> model = factory.getModel(EntityChild.class);
		List<AttributeModel> models = model.getAttributeModelsForType(AttributeType.MASTER, EntityParent.class);
		assertNotNull(models);
		assertEquals(2, models.size());
		assertEquals("parent", models.get(0).getName());
		// Find by attribute type
		model = factory.getModel(EntityChild.class);
		models = model.getAttributeModelsForType(AttributeType.MASTER, null);
		assertNotNull(models);
		assertEquals(2, models.size());
		assertEquals("parent", models.get(0).getName());
		// Find by type
		EntityModel<EntityParent> pmodel = factory.getModel(EntityParent.class);
		models = pmodel.getAttributeModelsForType(null, EntityChild.class);
		assertNotNull(models);
		assertEquals(1, models.size());
		assertEquals("children", models.get(0).getName());
	}

	@Test
	public void testNestedAttributes() {

		EntityModel<EntityParent> parentModel = factory.getModel(EntityParent.class);
		AttributeModel pm = parentModel.getAttributeModel("name");
		assertNotNull(pm);
		assertEquals("name", pm.getName());
		assertTrue(pm.isVisibleInGrid());

		EntityModel<EntityChild> model = factory.getModel(EntityChild.class);
		AttributeModel am = model.getAttributeModel("parent.name");
		assertNotNull(am);
		assertEquals("name", am.getName());
		assertEquals(EntityParent.class, am.getEntityModel().getEntityClass());
		assertFalse(am.isVisibleInGrid());

	}

	@Test
	public void testSortOrder() {
		// Test success
		EntityModel<EntityParent> model = factory.getModel(EntityParent.class);
		assertNotNull(model.getSortOrder());
		assertEquals(2, model.getSortOrder().size());
		AttributeModel amName = model.getAttributeModel("name");
		AttributeModel amId = model.getAttributeModel("id");
		assertEquals(false, model.getSortOrder().get(amName));
		assertEquals(true, model.getSortOrder().get(amId));
		// Test failure
		EntityModel<EntitySortError> semodel = factory.getModel(EntitySortError.class);
		assertNotNull(semodel.getSortOrder());
		assertEquals(1, semodel.getSortOrder().size());
		amName = semodel.getAttributeModel("code");
		assertEquals(true, semodel.getSortOrder().get(amName));
	}

	@Test
	public void testEmbedded() {
		EntityModel<EmbeddedParent> model = factory.getModel(EmbeddedParent.class);
		assertNotNull(model.getAttributeModel("name"));
		assertEquals(AttributeType.BASIC, model.getAttributeModel("name").getAttributeType());

		// there must not be a separate model for the embedded object
		assertNull(model.getAttributeModel("child"));

		AttributeModel m = model.getAttributeModel("child.embedded2");
		assertNotNull(m);
		assertTrue(m.isSearchable());

		// visible attribute is overridden using message bundle
		assertFalse(m.isVisible());

		// nested embedding
		assertNull(model.getAttributeModel("child.grandChild"));

		AttributeModel m2 = model.getAttributeModel("child.grandChild.sometAttribute");
		assertNotNull(m2);

	}

	@Test
	public void testSelectMode() {
		EntityModel<Entity7> model = factory.getModel(Entity7.class);

		// default
		AttributeModel am = model.getAttributeModel("entity6");
		assertEquals(AttributeSelectMode.LOOKUP, am.getSelectMode());
		assertEquals(AttributeSelectMode.LOOKUP, am.getSearchSelectMode());
		assertEquals(AttributeSelectMode.LOOKUP, am.getGridSelectMode());

		// multiple search defaults to token
		AttributeModel am2 = model.getAttributeModel("entity5");
		assertEquals(AttributeSelectMode.COMBO, am2.getSelectMode());
		assertEquals(AttributeSelectMode.LOOKUP, am2.getSearchSelectMode());
		assertEquals(AttributeSelectMode.COMBO, am2.getGridSelectMode());

		// overwritten attribute modes
		AttributeModel am3 = model.getAttributeModel("entity52");
		assertEquals(AttributeSelectMode.COMBO, am3.getSelectMode());
		assertEquals(AttributeSelectMode.TOKEN, am3.getSearchSelectMode());
		assertEquals(AttributeSelectMode.LIST, am3.getGridSelectMode());
	}

	@Test
	public void testDateType() {
		EntityModel<Entity8> model = factory.getModel(Entity8.class);

		// default
		AttributeModel am = model.getAttributeModel("date1");
		assertEquals(AttributeDateType.DATE, am.getDateType());
		assertEquals("dd-MM-yyyy", am.getDisplayFormat());

		// temporal annotation
		am = model.getAttributeModel("date2");
		assertEquals(AttributeDateType.TIMESTAMP, am.getDateType());
		assertEquals("dd-MM-yyyy HH:mm:ss", am.getDisplayFormat());

		am = model.getAttributeModel("date3");
		assertEquals(AttributeDateType.TIME, am.getDateType());
		assertEquals("HH:mm:ss", am.getDisplayFormat());

		// overridden annotation
		am = model.getAttributeModel("date4");
		assertEquals(AttributeDateType.TIME, am.getDateType());
		assertEquals("ss:mm:HH", am.getDisplayFormat());

		// overridden annotation
		am = model.getAttributeModel("date5");
		assertEquals(AttributeDateType.DATE, am.getDateType());
		assertEquals("yyyy-dd-MM ss:mm:HH", am.getDisplayFormat());

		// defaults
		am = model.getAttributeModel("date6");
		assertEquals(AttributeDateType.DATE, am.getDateType());
		assertEquals("dd-MM-yyyy", am.getDisplayFormat());
	}

	@Test
	public void testElementCollection() {
		EntityModel<Entity9> model = factory.getModel(Entity9.class);

		AttributeModel am = model.getAttributeModel("elements");
		assertEquals(AttributeType.ELEMENT_COLLECTION, am.getAttributeType());
		assertEquals("element_table", am.getCollectionTableName());
		assertEquals("element", am.getCollectionTableFieldName());

		AttributeModel am2 = model.getAttributeModel("longElements");
		assertEquals(AttributeType.ELEMENT_COLLECTION, am2.getAttributeType());
		assertEquals(100L, am2.getMinValue().longValue());
		assertEquals(500L, am2.getMaxValue().longValue());
	}

	@Test
	public void testGroupTogetherWith() {
		EntityModel<Entity10> model = factory.getModel(Entity10.class);

		AttributeModel am = model.getAttributeModel("attribute1");
		assertEquals(1, am.getGroupTogetherWith().size());
		assertEquals("attribute2", am.getGroupTogetherWith().get(0));
	}

	/**
	 * "group together with" attributes are in the wrong order
	 */
	@Test
	public void testGroupTogetherWithWrongOrder() {
		EntityModel<Entity11> model = factory.getModel(Entity11.class);

		AttributeModel am = model.getAttributeModel("attribute2");
		assertEquals(1, am.getGroupTogetherWith().size());
		assertEquals("attribute1", am.getGroupTogetherWith().get(0));

		AttributeModel am1 = model.getAttributeModel("attribute1");
		assertTrue(am1.isAlreadyGrouped());
	}

	/**
	 * Test cascading
	 */
	@Test
	public void testCascade() {
		EntityModel<Entity12> model = factory.getModel(Entity12.class);
		AttributeModel am = model.getAttributeModel("attribute1");
		assertTrue(am.getCascadeAttributes().contains("attribute2"));
		assertEquals("somePath", am.getCascadeFilterPath("attribute2"));
		assertEquals(CascadeMode.BOTH, am.getCascadeMode("attribute2"));

		assertEquals(1, model.getCascadeAttributeModels().size());
		assertEquals("attribute1", model.getCascadeAttributeModels().iterator().next().getPath());
	}

	/**
	 * Test cascading defined in message bundle
	 */
	@Test
	public void testCascadeMessageBundle() {
		EntityModel<Entity13> model = factory.getModel(Entity13.class);
		AttributeModel am = model.getAttributeModel("attribute1");
		assertTrue(am.getCascadeAttributes().contains("attribute2"));
		assertEquals("somePath", am.getCascadeFilterPath("attribute2"));
		assertEquals(CascadeMode.EDIT, am.getCascadeMode("attribute2"));
	}

	@Test
	public void testJava8DateTypes() {
		EntityModel<Entity14> model = factory.getModel(Entity14.class);
		AttributeModel am1 = model.getAttributeModel("localDate");
		assertNotNull(am1);
		assertEquals("dd/MM/yyyy", am1.getDisplayFormat());
		assertEquals(AttributeDateType.DATE, am1.getDateType());
		assertEquals(DateUtils.createLocalDate("01011980"), am1.getDefaultValue());

		AttributeModel am2 = model.getAttributeModel("localTime");
		assertNotNull(am2);
		assertEquals("HH-mm-ss", am2.getDisplayFormat());
		assertEquals(AttributeDateType.TIME, am2.getDateType());
		assertEquals(DateUtils.createLocalTime("121314"), am2.getDefaultValue());

		AttributeModel am3 = model.getAttributeModel("localDateTime");
		assertNotNull(am3);
		assertEquals("dd/MM/yyyy HH-mm-ss", am3.getDisplayFormat());
		assertEquals(AttributeDateType.TIMESTAMP, am3.getDateType());
		assertEquals(DateUtils.createLocalDateTime("01011980 121314"), am3.getDefaultValue());

		AttributeModel am4 = model.getAttributeModel("zonedDateTime");
		assertNotNull(am4);
		assertEquals("dd-MM-yyyy HH:mm:ssZ", am4.getDisplayFormat());
		assertEquals(AttributeDateType.TIMESTAMP, am4.getDateType());
		assertEquals(DateUtils.createZonedDateTime("01-01-2017 12:00:00+0100"), am4.getDefaultValue());
	}

	public class Entity1 {

		@Size(max = 55)
		@Attribute(main = true, textFieldMode = AttributeTextFieldMode.TEXTAREA, custom = {
				@CustomSetting(name = "bob", value = "ross"),
				@CustomSetting(name = "bobInt", value = "4", type = CustomType.INT),
				@CustomSetting(name = "bobBool", value = "true", type = CustomType.BOOLEAN) })
		private String name;

		@NotNull
		private Integer age;

		private BigDecimal weight;

		private LocalDate birthDate;

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

		public LocalDate getBirthDate() {
			return birthDate;
		}

		public void setBirthDate(LocalDate birthDate) {
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
	public class Entity2 {

		private String name;

		private Integer age;

		private LocalDate birthDate;

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

		public LocalDate getBirthDate() {
			return birthDate;
		}

		public void setBirthDate(LocalDate birthDate) {
			this.birthDate = birthDate;
		}
	}

	@Model(description = "desc", displayName = "dis", displayNamePlural = "diss", displayProperty = "prop", sortOrder = "name asc")
	@AttributeGroup(messageKey = "group1.key", attributeNames = { "name" })
	@AttributeGroup(messageKey = "group2.key", attributeNames = { "age" })
	public class Entity3 {

		@Attribute(defaultValue = "Bas", description = "Test", displayName = "Naampje", editable = EditableType.READ_ONLY, prompt = "Prompt", searchable = SearchMode.ALWAYS, main = true, sortable = false, styles = "myStyle")
		private String name;

		@Attribute(searchCaseSensitive = true, searchPrefixOnly = true, thousandsGrouping = false, requiredForSearching = true, searchable = SearchMode.ALWAYS)
		private Integer age;

		@Attribute(displayFormat = "dd/MM/yyyy")
		private LocalDate birthDate;

		@OneToOne
		@Attribute(complexEditable = true, navigable = true)
		private Entity2 entity2;

		@OneToMany
		private List<Entity4> entityList;

		@Attribute(precision = 4, currency = true)
		private BigDecimal weight;

		@Attribute(searchable = SearchMode.ADVANCED)
		private Integer advancedAge;

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

		public LocalDate getBirthDate() {
			return birthDate;
		}

		public void setBirthDate(LocalDate birthDate) {
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

		public Integer getAdvancedAge() {
			return advancedAge;
		}

		public void setAdvancedAge(Integer advancedAge) {
			this.advancedAge = advancedAge;
		}

	}

	public class Entity6 {

		private String name;

		private Integer age;

		private LocalDate birthDate;

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

		public LocalDate getBirthDate() {
			return birthDate;
		}

		public void setBirthDate(LocalDate birthDate) {
			this.birthDate = birthDate;
		}

	}

	/**
	 * For testing attribute lookup mode
	 * 
	 * @author bas.rutten
	 */
	public class Entity7 {

		@Attribute(selectMode = AttributeSelectMode.LOOKUP)
		private Entity6 entity6;

		@Attribute(multipleSearch = true)
		private Entity5 entity5;

		@Attribute(multipleSearch = true, searchSelectMode = AttributeSelectMode.TOKEN, gridSelectMode = AttributeSelectMode.LIST)
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

	public class Entity8 {

		private LocalDate date1;

		private LocalDateTime date2;

		@Attribute(dateType = AttributeDateType.TIME)
		private LocalTime date3;

		@Attribute(dateType = AttributeDateType.TIME, displayFormat = "ss:mm:HH")
		private LocalTime date4;

		@Attribute(displayFormat = "yyyy-dd-MM ss:mm:HH")
		private LocalDate date5;

		@Attribute
		private LocalDate date6;

		public LocalDate getDate1() {
			return date1;
		}

		public void setDate1(LocalDate date1) {
			this.date1 = date1;
		}

		public LocalDateTime getDate2() {
			return date2;
		}

		public void setDate2(LocalDateTime date2) {
			this.date2 = date2;
		}

		public LocalTime getDate3() {
			return date3;
		}

		public void setDate3(LocalTime date3) {
			this.date3 = date3;
		}

		public LocalTime getDate4() {
			return date4;
		}

		public void setDate4(LocalTime date4) {
			this.date4 = date4;
		}

		public LocalDate getDate5() {
			return date5;
		}

		public void setDate5(LocalDate date5) {
			this.date5 = date5;
		}

		public LocalDate getDate6() {
			return date6;
		}

		public void setDate6(LocalDate date6) {
			this.date6 = date6;
		}
	}

	public class Entity9 {

		@ElementCollection
		@CollectionTable(name = "element_table")
		@Column(name = "element")
		private Set<String> elements = new HashSet<>();

		@ElementCollection
		@CollectionTable(name = "long_element_table")
		@Column(name = "element")
		@Attribute(minValue = 100, maxValue = 500)
		private Set<Long> longElements = new HashSet<>();

		public Set<String> getElements() {
			return elements;
		}

		public void setElements(Set<String> elements) {
			this.elements = elements;
		}

		public Set<Long> getLongElements() {
			return longElements;
		}

		public void setLongElements(Set<Long> longElements) {
			this.longElements = longElements;
		}

	}

	public class Entity10 {

		@Attribute(groupTogetherWith = "attribute2")
		private String attribute1;

		@Transient
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
	public class Entity11 {

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

	public class Entity12 {

		@Attribute(cascade = @Cascade(cascadeTo = "attribute2", filterPath = "somePath"))
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

	/**
	 * Cascading in message bundle
	 * 
	 * @author bas.rutten
	 *
	 */
	public class Entity13 {

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

	public class Entity14 {

		@Attribute(displayFormat = "dd/MM/yyyy", defaultValue = "01/01/1980")
		private LocalDate localDate;

		@Attribute(displayFormat = "dd/MM/yyyy HH-mm-ss", defaultValue = "01/01/1980 12-13-14")
		private LocalDateTime localDateTime;

		@Attribute(displayFormat = "HH-mm-ss", defaultValue = "12-13-14")
		private LocalTime localTime;

		@Attribute(defaultValue = "01-01-2017 12:00:00+0100")
		private ZonedDateTime zonedDateTime;

		public LocalDate getLocalDate() {
			return localDate;
		}

		public void setLocalDate(LocalDate localDate) {
			this.localDate = localDate;
		}

		public LocalDateTime getLocalDateTime() {
			return localDateTime;
		}

		public void setLocalDateTime(LocalDateTime localDateTime) {
			this.localDateTime = localDateTime;
		}

		public LocalTime getLocalTime() {
			return localTime;
		}

		public void setLocalTime(LocalTime localTime) {
			this.localTime = localTime;
		}

		public ZonedDateTime getZonedDateTime() {
			return zonedDateTime;
		}

		public void setZonedDateTime(ZonedDateTime zonedDateTime) {
			this.zonedDateTime = zonedDateTime;
		}

	}

	public class Entity4 {

	}

	public class Entity5 {

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
	public class EntityParent {

		@Id
		private int id;

		@Attribute(searchable = SearchMode.ALWAYS)
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

	public class EntityChild {
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

	public class EntityGrandChild extends EntityChild {

	}

	@Model(sortOrder = "code unknown, unknown asc")
	public class EntitySortError {
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
	public class EmbeddedParent {

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
	public class EmbeddedChild {

		@Attribute(visible = VisibilityType.HIDE)
		private String embedded1;

		@Attribute(searchable = SearchMode.ALWAYS)
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
	public class EmbeddedGrandChild {

		private String sometAttribute;

		public String getSometAttribute() {
			return sometAttribute;
		}

		public void setSometAttribute(String sometAttribute) {
			this.sometAttribute = sometAttribute;
		}
	}

}
