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

import com.ocs.dynamo.dao.JoinType;
import com.ocs.dynamo.domain.model.*;
import jakarta.persistence.Basic;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.AttributeGroup;
import com.ocs.dynamo.domain.model.annotation.AttributeGroups;
import com.ocs.dynamo.domain.model.annotation.AttributeOrder;
import com.ocs.dynamo.domain.model.annotation.Cascade;
import com.ocs.dynamo.domain.model.annotation.CustomSetting;
import com.ocs.dynamo.domain.model.annotation.CustomType;
import com.ocs.dynamo.domain.model.annotation.GridAttributeOrder;
import com.ocs.dynamo.domain.model.annotation.Model;
import com.ocs.dynamo.domain.model.annotation.SearchAttributeOrder;
import com.ocs.dynamo.domain.model.annotation.SearchMode;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.impl.MessageServiceImpl;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.utils.DateUtils;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("unused")
public class EntityModelFactoryImplTest extends BaseMockitoTest {

	private final EntityModelFactoryImpl factory = new EntityModelFactoryImpl();

	private final ResourceBundleMessageSource source = new ResourceBundleMessageSource();

	private final MessageService messageService = new MessageServiceImpl();

	private final Locale locale = new Locale.Builder().setLanguage("nl").build();

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
		assertFalse(nameModel.isTrimSpaces());

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
		assertEquals(ThousandsGroupingMode.ALWAYS, ageModel.getThousandsGroupingMode());
		assertEquals(NumberFieldMode.TEXTFIELD, ageModel.getNumberFieldMode());

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

		AttributeModel passwordModel = model.getAttributeModel("password");
		assertEquals(AttributeTextFieldMode.PASSWORD, passwordModel.getTextFieldMode());

		// test the total size
		assertEquals(8, model.getAttributeModels().size());
	}

	@Test
	public void testPropertyOrder() {
		EntityModel<Entity2> model = factory.getModel(Entity2.class);
		assertNotNull(model);

		AttributeModel nameModel = model.getAttributeModel("name");
		assertEquals(0, nameModel.getOrder().intValue());
		assertTrue(nameModel.isTrimSpaces());

		AttributeModel birthDateModel = model.getAttributeModel("birthDate");
		assertEquals(1, birthDateModel.getOrder().intValue());

		AttributeModel age = model.getAttributeModel("age");
		assertEquals(2, age.getOrder().intValue());
	}

	@Test
	public void testAttributeGroups() {
		EntityModel<Entity3> model = factory.getModel(Entity3.class);
		assertNotNull(model);

		assertEquals(2, model.getFetchJoins().size());
		assertEquals("entity2", model.getFetchJoins().get(0).getProperty());
		assertEquals(JoinType.LEFT, model.getFetchJoins().get(0).getJoinType());

		assertEquals("entity3", model.getFetchJoins().get(1).getProperty());
		assertEquals(JoinType.INNER, model.getFetchJoins().get(1).getJoinType());

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
		assertEquals(ThousandsGroupingMode.NEVER, ageModel.getThousandsGroupingMode());
		assertEquals(NumberFieldMode.NUMBERFIELD, ageModel.getNumberFieldMode());
		assertEquals(3, ageModel.getNumberFieldStep());

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
		assertEquals(EditableType.CREATE_ONLY, nameModel.getEditableType());

		assertFalse(model.usesDefaultGroupOnly());

		String group1 = model.getAttributeGroups().get(0);
		assertEquals("group1", model.getAttributeGroups().get(0));

		String group2 = model.getAttributeGroups().get(1);
		assertEquals("group2", group2);

		assertEquals("age", model.getAttributeModelsForGroup(group2).get(0).getName());
		assertEquals("birthDate", model.getAttributeModelsForGroup(group2).get(1).getName());

		String group3 = model.getAttributeGroups().get(2);
		assertEquals(EntityModel.DEFAULT_GROUP, group3);

		AttributeModel ageModel = model.getAttributeModel("age");
		assertNotNull(ageModel);
		assertTrue(ageModel.isPercentage());
		assertEquals(NumberFieldMode.NUMBERFIELD, ageModel.getNumberFieldMode());
		assertEquals(5, ageModel.getNumberFieldStep());
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

		assertNotNull(parent.getAttributeModelByActualSortPath("children"));

		assertTrue(factory.hasModel("EntityChild.parent.children"));
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

		AttributeModel m2 = model.getAttributeModel("child.grandChild.someAttribute");
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
		assertEquals(PagingMode.NON_PAGED, am.getPagingMode());

		// multiple search defaults to token
		AttributeModel am2 = model.getAttributeModel("entity5");
		assertEquals(AttributeSelectMode.COMBO, am2.getSelectMode());
		assertEquals(AttributeSelectMode.LOOKUP, am2.getSearchSelectMode());
		assertEquals(AttributeSelectMode.COMBO, am2.getGridSelectMode());
		assertEquals(PagingMode.PAGED, am2.getPagingMode());

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

	@Test
	public void testAttributeOrders() {
		EntityModel<SearchOrderEntity> model = factory.getModel(SearchOrderEntity.class);
		assertNotNull(model);

		assertEquals(3, model.getAttributeModels().size());

		List<AttributeModel> attributeModels = model.getAttributeModels();
		assertEquals("field1", attributeModels.get(0).getName());
		assertEquals("field2", attributeModels.get(1).getName());
		assertEquals("field3", attributeModels.get(2).getName());

		attributeModels = model.getAttributeModelsSortedForGrid();
		assertEquals("field2", attributeModels.get(0).getName());
		assertEquals("field1", attributeModels.get(1).getName());
		assertEquals("field3", attributeModels.get(2).getName());

		attributeModels = model.getAttributeModelsSortedForSearch();
		assertEquals("field3", attributeModels.get(0).getName());
		assertEquals("field2", attributeModels.get(1).getName());
		assertEquals("field1", attributeModels.get(2).getName());
	}

	@Test
	public void testAttributeOrdersMessageBundle() {
		EntityModel<SearchOrderEntityMessage> model = factory.getModel(SearchOrderEntityMessage.class);
		assertNotNull(model);

		assertEquals(3, model.getAttributeModels().size());

		List<AttributeModel> attributeModels = model.getAttributeModels();
		assertEquals("field1", attributeModels.get(0).getName());
		assertEquals("field2", attributeModels.get(1).getName());
		assertEquals("field3", attributeModels.get(2).getName());

		attributeModels = model.getAttributeModelsSortedForGrid();
		assertEquals("field3", attributeModels.get(0).getName());
		assertEquals("field2", attributeModels.get(1).getName());
		assertEquals("field1", attributeModels.get(2).getName());

		attributeModels = model.getAttributeModelsSortedForSearch();
		assertEquals("field2", attributeModels.get(0).getName());
		assertEquals("field3", attributeModels.get(1).getName());
		assertEquals("field1", attributeModels.get(2).getName());
	}

	@Getter
	@Setter
	public static class Entity1 {

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

		@Attribute(textFieldMode = AttributeTextFieldMode.PASSWORD)
		private String password;

		@Attribute(url = true)
		private String url;

	}

	@AttributeOrder(attributeNames = { "name", "birthDate" })
	public static class Entity2 {

		@Attribute(trimSpaces = TrimType.TRIM)
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
	@AttributeGroups(value = { @AttributeGroup(messageKey = "group1.key", attributeNames = { "name" }),
			@AttributeGroup(messageKey = "group2.key", attributeNames = { "age" }) })
	@Getter
	@Setter
	//@FetchJoins(joins = @FetchJoin(attribute = "entity2"))
	public static class Entity3 {

		@Attribute(defaultValue = "Bas", description = "Test", displayName = "Naampje", editable = EditableType.READ_ONLY, prompt = "Prompt", searchable = SearchMode.ALWAYS, main = true, sortable = false)
		private String name;

		@Attribute(numberFieldStep = 3, searchCaseSensitive = BooleanType.TRUE, searchPrefixOnly = BooleanType.TRUE, thousandsGrouping = ThousandsGroupingMode.NEVER, requiredForSearching = true, searchable = SearchMode.ALWAYS, numberFieldMode = NumberFieldMode.NUMBERFIELD)
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

		@Attribute(displayName = "deri")
		public String getDerived() {
			return "test";
		}
	}

	@Getter
	@Setter
	public static class Entity6 {

		private String name;

		private Integer age;

		private LocalDate birthDate;
	}

	/**
	 * For testing attribute lookup mode
	 * 
	 * @author bas.rutten
	 */
	@Getter
	@Setter
	public static class Entity7 {

		@Attribute(selectMode = AttributeSelectMode.LOOKUP)
		private Entity6 entity6;

		@Attribute(multipleSearch = true, pagingMode = PagingMode.PAGED)
		private Entity5 entity5;

		@Attribute(multipleSearch = true, searchSelectMode = AttributeSelectMode.TOKEN, gridSelectMode = AttributeSelectMode.LIST)
		private Entity5 entity52;
	}

	@Getter
	@Setter
	public static class Entity8 {

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
   }

	@Getter
	@Setter
	public static class Entity9 {

		@ElementCollection
		@CollectionTable(name = "element_table")
		@Column(name = "element")
		private Set<String> elements = new HashSet<>();

		@ElementCollection
		@CollectionTable(name = "long_element_table")
		@Column(name = "element")
		@Attribute(minValue = 100, maxValue = 500)
		private Set<Long> longElements = new HashSet<>();
	}

	@Getter
	@Setter
	public static class Entity10 {

		@Attribute(groupTogetherWith = "attribute2")
		private String attribute1;

		@Transient
		private String attribute2;
	}

	@AttributeOrder(attributeNames = { "attribute1", "attribute2" })
	@Getter
	@Setter
	public static class Entity11 {

		private String attribute1;

		@Attribute(groupTogetherWith = "attribute1")
		private String attribute2;
	}

	@Getter
	@Setter
	public static class Entity12 {

		@Attribute(cascade = @Cascade(cascadeTo = "attribute2", filterPath = "somePath"))
		private String attribute1;

		private String attribute2;
	}

	/**
	 * Cascading in message bundle
	 * 
	 * @author bas.rutten
	 *
	 */
	@Getter
	@Setter
	public static class Entity13 {

		private String attribute1;

		private String attribute2;

	}

	@Getter
	@Setter
	public static class Entity14 {

		@Attribute(displayFormat = "dd/MM/yyyy", defaultValue = "01/01/1980")
		private LocalDate localDate;

		@Attribute(displayFormat = "dd/MM/yyyy HH-mm-ss", defaultValue = "01/01/1980 12-13-14")
		private LocalDateTime localDateTime;

		@Attribute(displayFormat = "HH-mm-ss", defaultValue = "12-13-14")
		private LocalTime localTime;

		@Attribute(defaultValue = "01-01-2017 12:00:00+0100")
		private ZonedDateTime zonedDateTime;
	}

	public static class Entity4 {

	}

	@Getter
	@Setter
	public static class Entity5 {

		@Lob
		@Basic(fetch = FetchType.LAZY)
		@Attribute(image = true, allowedExtensions = { "gif", "bmp" })
		private byte[] logo;

		@AssertTrue
		public boolean isSomeValidation() {
			return true;
		}
	}

	@Model(sortOrder = "name desc, id asc")
	@Getter
	@Setter
	public class EntityParent {

		@Id
		private int id;

		@Attribute(searchable = SearchMode.ALWAYS)
		private String name;

		@OneToMany
		private List<EntityChild> children;

		@Attribute(memberType = EntityGrandChild.class, replacementSearchPath = "children")
		public List<EntityGrandChild> getCalculatedChildren() {
			return null;
		}

		public void setCalculatedChildren(List<EntityGrandChild> children) {
			// do nothing
		}
	}

	@Model(nestingDepth = 1)
	@Getter
	@Setter
	public class EntityChild {

		@Id
		private int id;

		private String name;

		private EntityParent parent;

		private EntityParent parent2;

	}

	public class EntityGrandChild extends EntityChild {

	}

	@Model(sortOrder = "code unknown, unknown asc")
	@Getter
	@Setter
	public static class EntitySortError {

		private String code;

		private String name;
	}

	@AttributeOrder(attributeNames = { "child.embedded1", "child.embedded2", "name" })
	@Getter
	@Setter
	public static class EmbeddedParent {

		private String name;

		@Embedded
		private EmbeddedChild child;
	}

	@Embeddable
	@Getter
	@Setter
	public static class EmbeddedChild {

		@Attribute(visible = VisibilityType.HIDE)
		private String embedded1;

		@Attribute(searchable = SearchMode.ALWAYS)
		private String embedded2;

		@Embedded
		private EmbeddedGrandChild grandChild;
	}

	@Embeddable
	@Getter
	@Setter
	public static class EmbeddedGrandChild {

		private String someAttribute;
	}

	@AttributeOrder(attributeNames = { "field1", "field2", "field3" })
	@GridAttributeOrder(attributeNames = { "field2", "field1", "field3" })
	@SearchAttributeOrder(attributeNames = { "field3", "field2", "field1" })
	@Getter
	@Setter
	public static class SearchOrderEntity {

		private String field1;

		private String field2;

		private String field3;
	}

	@AttributeOrder(attributeNames = { "field1", "field2", "field3" })
	@Getter
	@Setter
	public static class SearchOrderEntityMessage {

		private String field1;

		private String field2;

		private String field3;
	}

}
