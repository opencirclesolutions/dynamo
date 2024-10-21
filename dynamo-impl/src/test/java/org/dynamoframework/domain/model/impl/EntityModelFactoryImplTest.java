package org.dynamoframework.domain.model.impl;

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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.dynamoframework.configuration.DynamoConfigurationProperties;
import org.dynamoframework.dao.JoinType;
import org.dynamoframework.domain.TestEntity;
import org.dynamoframework.domain.model.*;
import org.dynamoframework.domain.model.annotation.*;
import org.dynamoframework.exception.OCSRuntimeException;
import org.dynamoframework.service.MessageService;
import org.dynamoframework.service.ServiceLocator;
import org.dynamoframework.service.impl.BaseServiceImpl;
import org.dynamoframework.service.impl.MessageServiceImpl;
import org.dynamoframework.service.impl.TestEntityServiceImpl;
import org.dynamoframework.test.BaseMockitoTest;
import org.dynamoframework.utils.DateUtils;
import org.dynamoframework.configuration.DynamoPropertiesHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Import({EntityModelFactoryImpl.class, DynamoPropertiesHolder.class})
@EnableConfigurationProperties(value = DynamoConfigurationProperties.class)
public class EntityModelFactoryImplTest extends BaseMockitoTest {

	@Autowired
	private EntityModelFactoryImpl factory;

	private final ResourceBundleMessageSource source = new ResourceBundleMessageSource();

	private MessageService messageService = new MessageServiceImpl();

	private final Locale locale = new Locale.Builder().setLanguage("nl").build();

	@MockBean
	private static ServiceLocator serviceLocator;

	@BeforeEach
	public void beforeEach() {

		ReflectionTestUtils.setField(factory, "serviceLocator", serviceLocator);
		when(serviceLocator.getMessageService())
			.thenReturn(messageService);

		BaseServiceImpl<?, ?> service = new TestEntityServiceImpl();
		Mockito.when(serviceLocator.getServiceForEntity(TestEntity.class))
			.thenAnswer(a -> service);

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
		assertEquals(Integer.MAX_VALUE, model.getMaxSearchResults());

		assertTrue(model.isListAllowed());
		assertTrue(model.isCreateAllowed());
		assertFalse(model.isDeleteAllowed());
		assertTrue(model.isUpdateAllowed());
		assertTrue(model.isExportAllowed());

		AttributeModel nameModel = model.getAttributeModel("name");
		assertNotNull(nameModel);

		assertNull(nameModel.getDefaultValue());
		assertEquals("Name", nameModel.getPrompt(locale));
		assertEquals("Name", nameModel.getDisplayName(locale));
		assertEquals(4, nameModel.getOrder().intValue());
		assertEquals(String.class, nameModel.getType());
		assertNull(nameModel.getDisplayFormat(locale));
		assertEquals(AttributeType.BASIC, nameModel.getAttributeType());
		assertFalse(nameModel.isRequired());
		assertTrue(nameModel.isVisibleInForm());
		assertEquals(55, nameModel.getMaxLength().intValue());
		assertEquals(AttributeTextFieldMode.TEXTAREA, nameModel.getTextFieldMode());
		assertFalse(nameModel.isTrimSpaces());

		assertEquals("ross", nameModel.getCustomSetting("bob"));
		assertEquals(4, nameModel.getCustomSetting("bobInt"));
		assertEquals(true, nameModel.getCustomSetting("bobBool"));

		assertTrue(nameModel.isSortable());
		assertEquals(EditableType.EDITABLE, nameModel.getEditableType());

		AttributeModel ageModel = model.getAttributeModel("age");
		assertNull(ageModel.getDefaultValue());
		assertEquals("Age", ageModel.getDisplayName(locale));
		assertEquals(0, ageModel.getOrder().intValue());
		assertEquals(Integer.class, ageModel.getType());
		assertNull(nameModel.getDisplayFormat(locale));
		assertEquals(AttributeType.BASIC, ageModel.getAttributeType());
		assertTrue(ageModel.isRequired());
		assertEquals(NumberFieldMode.TEXTFIELD, ageModel.getNumberFieldMode());

		AttributeModel birthDateModel = model.getAttributeModel("birthDate");
		assertNull(birthDateModel.getDefaultValue());
		assertEquals("Birth Date", birthDateModel.getDisplayName(locale));
		assertEquals(1, birthDateModel.getOrder().intValue());
		assertEquals(LocalDate.class, birthDateModel.getType());
		assertNotNull(birthDateModel.getDisplayFormat(locale));
		assertEquals(AttributeType.BASIC, birthDateModel.getAttributeType());

		assertTrue(model.usesDefaultGroupOnly());

		AttributeModel weightModel = model.getAttributeModel("weight");
		assertEquals(2, weightModel.getPrecision());

		AttributeModel boolModel = model.getAttributeModel("bool");
		assertEquals("Yes", boolModel.getTrueRepresentation(locale));
		assertEquals("No", boolModel.getFalseRepresentation(locale));
		assertEquals(AttributeBooleanFieldMode.TOGGLE, boolModel.getBooleanFieldMode());

		AttributeModel mailModel = model.getAttributeModel("email");
		assertTrue(mailModel.isEmail());

		AttributeModel urlModel = model.getAttributeModel("url");
		assertTrue(urlModel.isUrl());

		AttributeModel passwordModel = model.getAttributeModel("password");
		assertEquals(AttributeTextFieldMode.PASSWORD, passwordModel.getTextFieldMode());

		// test the total size
		assertEquals(8, model.getAttributeModels().size());

		assertEquals(1, model.getReadRoles().size());
		assertTrue(model.getReadRoles().contains("role1"));
		assertEquals(1, model.getWriteRoles().size());
		assertTrue(model.getWriteRoles().contains("role2"));
		assertEquals(1, model.getDeleteRoles().size());
		assertTrue(model.getDeleteRoles().contains("role3"));


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
		String group1 = model.getAttributeGroups().getFirst();
		assertEquals("group1.key", group1);

		List<AttributeModel> models = model.getAttributeModelsForGroup(group1);
		assertEquals("name", models.getFirst().getName());
		assertEquals(SearchMode.ALWAYS, models.getFirst().getSearchMode());

		String group2 = model.getAttributeGroups().get(1);
		assertEquals("group2.key", group2);

		List<AttributeModel> models2 = model.getAttributeModelsForGroup(group2);
		assertEquals("age", models2.getFirst().getName());
		assertTrue(models2.getFirst().isRequiredForSearching());

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
		assertEquals(100, model.getMaxSearchResults());
		assertEquals("Fill me", model.getAutofillInstructions());

		// check changes to methods
		assertFalse(model.isCreateAllowed());
		assertFalse(model.isUpdateAllowed());
		assertTrue(model.isDeleteAllowed());
		assertFalse(model.isExportAllowed());

		AttributeModel nameModel = model.getAttributeModel("name");
		assertNotNull(nameModel);

		assertEquals("Bas", nameModel.getDefaultValue());
		assertEquals("Bob", nameModel.getDefaultSearchValue());

		assertEquals("Naampje", nameModel.getDisplayName(locale));
		assertEquals("Test", nameModel.getDescription(locale));
		assertEquals("Prompt", nameModel.getPrompt(locale));
		assertEquals(String.class, nameModel.getType());
		assertEquals("Fill me carefully", nameModel.getAutofillInstructions());

		assertNull(nameModel.getDisplayFormat(locale));
		assertEquals(AttributeType.BASIC, nameModel.getAttributeType());
		assertFalse(nameModel.isSearchCaseSensitive());
		assertFalse(nameModel.isSearchPrefixOnly());

		assertFalse(nameModel.isSortable());
		assertTrue(nameModel.isSearchable());
		assertEquals(EditableType.READ_ONLY, nameModel.getEditableType());

		AttributeModel ageModel = model.getAttributeModel("age");
		assertNotNull(ageModel);
		assertTrue(ageModel.isSearchCaseSensitive());
		assertTrue(ageModel.isSearchPrefixOnly());
		assertEquals(4, ageModel.getDefaultSearchValueFrom());
		assertEquals(10, ageModel.getDefaultSearchValueTo());

		assertEquals(NumberFieldMode.NUMBERFIELD, ageModel.getNumberFieldMode());
		assertEquals(3, ageModel.getNumberFieldStep());

		AttributeModel entityModel = model.getAttributeModel("entity2");
		assertEquals(AttributeType.MASTER, entityModel.getAttributeType());
		assertTrue(entityModel.isVisibleInForm());
		assertTrue(entityModel.isNavigable());
		assertEquals("Entity2Ref", entityModel.getLookupEntityReference());
		assertEquals(entityModel.getNavigationLink(), "navLink");

		AttributeModel entityListModel = model.getAttributeModel("entityList");
		assertEquals(AttributeType.DETAIL, entityListModel.getAttributeType());

		AttributeModel birthDateModel = model.getAttributeModel("birthDate");
		assertEquals("dd/MM/yyyy", birthDateModel.getDisplayFormat(locale));

		// test that attribute annotations on getters are also picked up
		AttributeModel derivedModel = model.getAttributeModel("derived");
		assertNotNull(derivedModel);
		assertEquals("deri", derivedModel.getDisplayName(locale));

		AttributeModel weightModel = model.getAttributeModel("weight");
		assertNotNull(weightModel);
		assertEquals(4, weightModel.getPrecision());
		assertEquals("EUR", weightModel.getCurrencyCode());
	}

	@Test
	public void testEntityModelActions() {

		BaseServiceImpl<?, ?> service = new TestEntityServiceImpl();
		Mockito.when(serviceLocator.getServiceForEntity(TestEntity.class))
			.thenAnswer(a -> service);

		EntityModel<TestEntity> model = factory.getModel(TestEntity.class);
		assertEquals(1, model.getEntityModelActions().size());

		EntityModelAction partialAction = model.findAction("PartialAction");
		assertNotNull(partialAction);
		assertEquals(EntityModelActionType.CREATE, partialAction.getType());
		assertEquals(3, partialAction.getEntityModel().getAttributeModels().size());
		assertEquals("PartialAction", partialAction.getEntityModel().getReference());
		assertEquals("ChangedName", partialAction.getEntityModel()
			.getAttributeModel("name").getDisplayName(locale));
		assertTrue(partialAction.getRoles().contains("role12"));
		assertEquals(partialAction.getFormMode(), ActionFormMode.BOTH);

		// icon overridden in message bundle
		assertEquals("iconOverride", partialAction.getIcon());
	}

	@Test
	public void testReference() {
		EntityModel<Entity6> model = factory.getModel("Special", Entity6.class);
		assertEquals("Special", model.getDisplayName(locale));
		assertEquals("SpecialPlural", model.getDisplayNamePlural(locale));
	}

	@Test
	public void testMessageBundleOverrides() {
		EntityModel<Entity6> model = factory.getModel(Entity6.class);
		assertNotNull(model);

		// the following are overwritten using the message bundle
		assertEquals("Override", model.getDisplayName(locale));
		assertEquals("Overrides", model.getDisplayNamePlural(locale));
		assertEquals("Description override", model.getDescription(locale));
		assertEquals("Prop", model.getDisplayProperty());
		assertEquals(150, model.getMaxSearchResults());

		assertTrue(model.isUpdateAllowed());
		assertTrue(model.isDeleteAllowed());
		assertFalse(model.isListAllowed());
		assertFalse(model.isCreateAllowed());

		AttributeModel nameModel = model.getAttributeModel("name");
		assertNotNull(nameModel);

		assertTrue(nameModel.isSearchCaseSensitive());
		assertTrue(nameModel.isSearchPrefixOnly());

		assertEquals("customValue", nameModel.getCustomSetting("custom"));
		assertEquals(4, nameModel.getCustomSetting("custom2"));
		assertEquals(true, nameModel.getCustomSetting("custom3"));
		assertEquals("Henk", nameModel.getDefaultSearchValue());

		assertEquals("Override", nameModel.getDisplayName(locale));
		assertEquals("Prompt override", nameModel.getPrompt(locale));
		assertEquals(EditableType.CREATE_ONLY, nameModel.getEditableType());

		assertFalse(model.usesDefaultGroupOnly());

		String group1 = model.getAttributeGroups().get(0);
		assertEquals("group1", group1);

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
		assertEquals("7", ageModel.getDefaultSearchValueFrom());
		assertEquals("9", ageModel.getDefaultSearchValueTo());

		assertEquals(2, model.getReadRoles().size());
		assertTrue(model.getReadRoles().contains("role4"));
		assertTrue(model.getReadRoles().contains("role5"));

		assertEquals(1, model.getWriteRoles().size());
		assertTrue(model.getWriteRoles().contains("role6"));

		assertEquals(1, model.getDeleteRoles().size());
		assertTrue(model.getDeleteRoles().contains("role7"));


	}

	@Test
	public void testLob() {
		EntityModel<Entity5> model = factory.getModel(Entity5.class);
		AttributeModel attributeModel = model.getAttributeModel("logo");
		assertEquals(AttributeType.LOB, attributeModel.getAttributeType());
		assertTrue(attributeModel.isImage());
		assertTrue(attributeModel.isDownloadAllowed());
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

		// check on-demand construction of model
		EntityModel<EntityChild> child2 = factory.getModel("EntityChild.parent.children", EntityChild.class);
		assertNotNull(child2);

		// check that the nested model attribute is not searchable...
		EntityModel<EntityChild> childModel = factory.getModel("EntityChild.parent", EntityChild.class);
		assertNotNull(childModel);
		assertFalse(childModel.getAttributeModel("name").isSearchable());

		// but the parent is
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

		AttributeModel am = model.getAttributeModel("child.embedded2");
		assertNotNull(am);
		assertTrue(am.isSearchable());

		// visible attribute is overridden using message bundle
		assertFalse(am.isVisibleInForm());

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

		// multiple search defaults to token
		AttributeModel am2 = model.getAttributeModel("entity5");
		assertEquals(AttributeSelectMode.COMBO, am2.getSelectMode());
		assertEquals(AttributeSelectMode.MULTI_SELECT, am2.getSearchSelectMode());

		// overwritten attribute modes
		AttributeModel am3 = model.getAttributeModel("entity52");
		assertEquals(AttributeSelectMode.COMBO, am3.getSelectMode());
		assertEquals(AttributeSelectMode.MULTI_SELECT, am3.getSearchSelectMode());
	}

	@Test
	public void testDateType() {
		EntityModel<Entity8> model = factory.getModel(Entity8.class);

		// default
		AttributeModel am = model.getAttributeModel("date1");
		assertEquals(AttributeDateType.DATE, am.getDateType());
		assertEquals("dd-MM-yyyy", am.getDisplayFormat(locale));

		// temporal annotation
		am = model.getAttributeModel("date2");
		assertEquals(AttributeDateType.LOCAL_DATE_TIME, am.getDateType());
		assertEquals("dd-MM-yyyy HH:mm:ss", am.getDisplayFormat(locale));

		am = model.getAttributeModel("date3");
		assertEquals(AttributeDateType.TIME, am.getDateType());
		assertEquals("HH:mm:ss", am.getDisplayFormat(locale));

		// overridden annotation
		am = model.getAttributeModel("date4");
		assertEquals(AttributeDateType.TIME, am.getDateType());
		assertEquals("ss:mm:HH", am.getDisplayFormat(locale));

		// overridden annotation
		am = model.getAttributeModel("date5");
		assertEquals(AttributeDateType.DATE, am.getDateType());
		assertEquals("yyyy-dd-MM ss:mm:HH", am.getDisplayFormat(locale));

		// defaults
		am = model.getAttributeModel("date6");
		assertEquals(AttributeDateType.DATE, am.getDateType());
		assertEquals("dd-MM-yyyy", am.getDisplayFormat(locale));
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
		assertThrows(OCSRuntimeException.class, () -> factory.getModel(Entity11.class));
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
		assertEquals("attribute1", model.getCascadeAttributeModels().getFirst().getPath());
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
		assertEquals("dd/MM/yyyy", am1.getDisplayFormat(locale));
		assertEquals(AttributeDateType.DATE, am1.getDateType());
		assertEquals(DateUtils.createLocalDate("01011980"), am1.getDefaultValue());

		AttributeModel am2 = model.getAttributeModel("localTime");
		assertNotNull(am2);
		assertEquals("HH-mm-ss", am2.getDisplayFormat(locale));
		assertEquals(AttributeDateType.TIME, am2.getDateType());
		assertEquals(DateUtils.createLocalTime("121314"), am2.getDefaultValue());

		AttributeModel am3 = model.getAttributeModel("localDateTime");
		assertNotNull(am3);
		assertEquals("dd/MM/yyyy HH-mm-ss", am3.getDisplayFormat(locale));
		assertEquals(AttributeDateType.LOCAL_DATE_TIME, am3.getDateType());
		assertEquals(DateUtils.createLocalDateTime("01011980 121314"), am3.getDefaultValue());
		assertEquals(LocalDate.of(1980, 1, 1), am3.getDefaultSearchValue());

		AttributeModel am4 = model.getAttributeModel("instant");
		assertNotNull(am4);
		assertEquals(AttributeDateType.INSTANT, am4.getDateType());
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
	@Roles(readRoles = "role1", writeRoles = "role2", deleteRoles = "role3")
	public static class Entity1 {

		@Size(max = 55)
		@Attribute(textFieldMode = AttributeTextFieldMode.TEXTAREA, custom = {
			@CustomSetting(name = "bob", value = "ross"),
			@CustomSetting(name = "bobInt", value = "4", type = CustomType.INT),
			@CustomSetting(name = "bobBool", value = "true", type = CustomType.BOOLEAN)})
		private String name;

		@NotNull
		private Integer age;

		private BigDecimal weight;

		private LocalDate birthDate;

		@Attribute(trueRepresentation = "Yes", falseRepresentation = "No",
			booleanFieldMode = AttributeBooleanFieldMode.TOGGLE)
		private Boolean bool;

		@Email
		private String email;

		@Attribute(textFieldMode = AttributeTextFieldMode.PASSWORD)
		private String password;

		@Attribute(url = true)
		private String url;

	}

	@AttributeOrder(attributeNames = {"name", "birthDate"})
	@Getter
	@Setter
	public static class Entity2 {

		@Attribute(trimSpaces = TrimType.TRIM)
		private String name;

		private Integer age;

		private LocalDate birthDate;
	}

	@Model(description = "desc", displayName = "dis", displayNamePlural = "diss", displayProperty = "prop", sortOrder = "name asc",
		createAllowed = false, updateAllowed = false, deleteAllowed = true, maxSearchResults = 100,
		exportAllowed = false, autofillInstructions = "Fill me")
	@AttributeGroups(value = {@AttributeGroup(messageKey = "group1.key", attributeNames = {"name"}),
		@AttributeGroup(messageKey = "group2.key", attributeNames = {"age"})})
	@FetchJoins(joins = @FetchJoin(attribute = "entity2"))
	@Getter
	@Setter
	public static class Entity3 {

		@Attribute(defaultValue = "Bas",
			defaultSearchValue = "Bob", description = "Test", displayName = "Naampje", editable = EditableType.READ_ONLY, prompt = "Prompt", searchable = SearchMode.ALWAYS, sortable = false,
			autoFillInstructions = "Fill me carefully")
		private String name;

		@Attribute(numberFieldStep = 3, searchCaseSensitive = BooleanType.TRUE,
			searchPrefixOnly = BooleanType.TRUE, requiredForSearching = true,
			searchable = SearchMode.ALWAYS, numberFieldMode = NumberFieldMode.NUMBERFIELD,
			defaultSearchValueFrom = "4",
			defaultSearchValueTo = "10")
		private Integer age;

		@Attribute(displayFormat = "dd/MM/yyyy")
		private LocalDate birthDate;

		@OneToOne
		@Attribute(visibleInForm = VisibilityType.SHOW, navigable = true,
			lookupEntityReference = "Entity2Ref", navigationLink = "navLink")
		private Entity2 entity2;

		@OneToMany
		private List<Entity4> entityList;

		@Attribute(precision = 4, currencyCode = "EUR")
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

		@Attribute(multipleSearch = true)
		private Entity5 entity5;

		@Attribute(multipleSearch = true, searchSelectMode = AttributeSelectMode.MULTI_SELECT)
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

	@Getter
	@Setter
	@AttributeOrder(attributeNames = {"attribute1", "attribute2"})
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

		@Attribute(displayFormat = "dd/MM/yyyy", defaultValue = "01-01-1980")
		private LocalDate localDate;

		@Attribute(displayFormat = "dd/MM/yyyy HH-mm-ss", defaultValue = "01-01-1980 12:13:14",
			searchDateOnly = true, defaultSearchValue = "01-01-1980")
		private LocalDateTime localDateTime;

		@Attribute(displayFormat = "HH-mm-ss", defaultValue = "12:13:14")
		private LocalTime localTime;

		@Attribute(displayFormat = "dd/MM/yyyy HH-mm-ss")
		private Instant instant;
	}

	public static class Entity4 {
	}

	@Getter
	@Setter
	public static class Entity5 {

		@Lob
		@Basic(fetch = FetchType.LAZY)
		@Attribute(image = true, allowedExtensions = {"gif", "bmp"}
			, downloadAllowed = true)
		private byte[] logo;

		@AssertTrue
		@SuppressWarnings("unused")
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

	@AttributeOrder(attributeNames = {"child.embedded1", "child.embedded2", "name"})
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

		@Attribute(visibleInForm = VisibilityType.SHOW)
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

	@AttributeOrder(attributeNames = {"field1", "field2", "field3"})
	@GridAttributeOrder(attributeNames = {"field2", "field1", "field3"})
	@SearchAttributeOrder(attributeNames = {"field3", "field2", "field1"})
	@Getter
	@Setter
	public static class SearchOrderEntity {

		private String field1;

		private String field2;

		private String field3;
	}

	@AttributeOrder(attributeNames = {"field1", "field2", "field3"})
	@Getter
	@Setter
	public static class SearchOrderEntityMessage {

		private String field1;

		private String field2;

		private String field3;

	}

}
