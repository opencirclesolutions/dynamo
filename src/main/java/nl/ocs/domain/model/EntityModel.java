package nl.ocs.domain.model;

import java.util.List;
import java.util.Map;

/**
 * An interface representating a model that contains an entity's metadata
 * 
 * @author bas.rutten
 * 
 * @param <T>
 */
public interface EntityModel<T> {

	static final String DEFAULT_GROUP = "defaultGroup";

	static final String DISPLAY_NAME = "displayName";

	static final String DISPLAY_NAME_PLURAL = "displayNamePlural";

	static final String DESCRIPTION = "description";

	static final String DEFAULT_VALUE = "defaultValue";

	static final String DISPLAY_FORMAT = "displayFormat";

	static final String PROMPT = "prompt";

	static final String MAIN = "main";

	static final String READ_ONLY = "readOnly";

	static final String SEARCHABLE = "searchable";
	
	static final String SEARCH_CASE_SENSITIVE = "searchCaseSensitive";

	static final String SEARCH_PREFIX_ONLY = "searchPrefixOnly";
	
	static final String SORTABLE = "sortable";

	static final String SHOW_IN_TABLE = "showInTable";

	static final String VISIBLE = "visible";

	static final String DISPLAY_PROPERTY = "displayProperty";

	static final String COMPLEX_EDITABLE = "complexEditable";

	static final String ATTRIBUTE_ORDER = "attributeOrder";

	static final String IMAGE = "image";

	static final String ALLOWED_EXTENSIONS = "allowedExtensions";

	static final String WEEK = "week";

	static final String TRUE_REPRESENTATION = "trueRepresentation";

	static final String FALSE_REPRESENTATION = "falseRepresentation";

	static final String ATTRIBUTE_GROUP = "attributeGroup";

	static final String ATTRIBUTE_NAMES = "attributeNames";

	static final String DETAIL_FOCUS = "detailFocus";

	static final String PERCENTAGE = "percentage";

	static final String SORT_ORDER = "sortOrder";

	static final String PRECISION = "precision";

	static final String EMBEDDED = "embedded";

	static final String CURRENCY = "currency";

	static final String DATE_TYPE = "dateType";

	static final int DEFAULT_PRECISION = 2;

	/**
	 * Indicates that a lookup field (rather than a combo box) must be used when
	 * selecting the component
	 * 
	 */
	static final String SELECT_MODE = "selectMode";
	
	static final String TEXTFIELD_MODE = "textFieldMode";

	/**
	 * The display name of the entity
	 * 
	 * @return
	 */
	String getDisplayName();

	/**
	 * The display name (plural) of the entity
	 * 
	 * @return
	 */
	String getDisplayNamePlural();

	/**
	 * Textual description of the entity
	 * 
	 * @return
	 */
	String getDescription();

	/**
	 * The class of the entity that this model is based on
	 * 
	 * @return
	 */
	Class<T> getEntityClass();

	/**
	 * The name of the property that is used when displaying the object in a
	 * select component (like a combobox) or a table
	 * 
	 * @return
	 */
	String getDisplayProperty();

	/**
	 * Returns an ordered list of all attribute models
	 * 
	 * @return
	 */
	List<AttributeModel> getAttributeModels();

	/**
	 * Looks up an attribute model by its name
	 * 
	 * @param attributeName
	 *            the name of the attribute
	 * @return
	 */
	AttributeModel getAttributeModel(String attributeName);

	/**
	 * Returns the primary attribute
	 * 
	 * @return
	 */
	AttributeModel getMainAttributeModel();

	/**
	 * Returns the attribute groups that are defined for this entity
	 * 
	 * @return
	 */
	List<String> getAttributeGroups();

	/**
	 * Returns the attribute models for a certain group
	 * 
	 * @param group
	 * @return
	 */
	List<AttributeModel> getAttributeModelsForGroup(String group);

	/**
	 * Returns the attribute models for a certain attribute type and type. Just
	 * one of the parameters is mandatory, when both are given both will be used
	 * in a boolean AND. Will also look at the generic type of a attribute, e.g.
	 * List<some generic type>.
	 * 
	 * @param attributeType
	 * @param type
	 * @return
	 */
	List<AttributeModel> getAttributeModelsForType(AttributeType attributeType, Class<?> type);

	/**
	 * Indicates whether an attribute group should be visible
	 * 
	 * @param group
	 * @return
	 */
	boolean isAttributeGroupVisible(String group, boolean readOnly);

	/**
	 * 
	 * @return
	 */
	boolean usesDefaultGroupOnly();

	/**
	 * 
	 */
	String getReference();

	/**
	 * Adds a new attribute model on the position of the given existing
	 * attribute model. The existing model will shift a position backwards. When
	 * the existing model is not found the attribute will added on the end of
	 * the list.
	 * 
	 * @param attributeGroup
	 *            The group to which the attribute model should be registered
	 * @param model
	 *            The model of the attribute
	 * @param existingModel
	 *            The existing attribute model
	 */
	void addAttributeModel(String attributeGroup, AttributeModel model, AttributeModel existingModel);

	/**
	 * @return The attribute model of the id
	 */
	AttributeModel getIdAttributeModel();

	/**
	 * Get the default sort order
	 * 
	 * @return a map of attribute models for which sort orders are set
	 */
	Map<AttributeModel, Boolean> getSortOrder();
}