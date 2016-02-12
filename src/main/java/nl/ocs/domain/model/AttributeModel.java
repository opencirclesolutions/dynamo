package nl.ocs.domain.model;

import java.util.Set;

public interface AttributeModel extends Comparable<AttributeModel> {

	/**
	 * In case of a LOB object - the allowed extensions
	 * 
	 * @return
	 */
	Set<String> getAllowedExtensions();

	/**
	 * @return the attribute type for this property
	 */
	AttributeType getAttributeType();

	/**
	 * The type of date information (date, time, or timestamp)
	 * 
	 * @return
	 */
	public AttributeDateType getDateType();

	/**
	 * Returns the default value of the attribute
	 * 
	 * @return
	 */
	Object getDefaultValue();

	/**
	 * The description of the attribute
	 * 
	 * @return
	 */
	String getDescription();

	/**
	 * Returns the display format of a property. Mainly used for strings
	 * 
	 * @return
	 */
	String getDisplayFormat();

	/**
	 * The display name of the attribute
	 * 
	 * @return
	 */
	String getDisplayName();

	/**
	 * @return the model which contains this attribute
	 */
	EntityModel<?> getEntityModel();

	/**
	 * Indicates a string value to use instead of "false"
	 * 
	 * @return
	 */
	String getFalseRepresentation();

	/**
	 * If the attribute is a Collection, then this returns the member type of
	 * the collection
	 * 
	 * @return
	 */
	Class<?> getMemberType();

	/**
	 * The name of the attribute
	 * 
	 * @return
	 */
	String getName();

	/**
	 * The maximum allowed length of the attribute
	 * 
	 * @return
	 */
	Integer getMaxLength();

	/**
	 * When this is a MASTER or DETAIL attribute then return the entity model
	 * for the nested entity
	 * 
	 * @return The nested entity model
	 */
	EntityModel<?> getNestedEntityModel();

	/**
	 * The order number of the attribute
	 * 
	 * @return
	 */
	Integer getOrder();

	/**
	 * 
	 * @return the (nested) path to this attribute
	 */
	String getPath();

	/**
	 * The precision (number of decimals) to use when displaying a decimal
	 * number
	 * 
	 * @return
	 */
	int getPrecision();

	/**
	 * The input prompt to use for a field
	 * 
	 * @return
	 */
	String getPrompt();

	/**
	 * The select mode for picking MASTER objects (combo box or lookup field)
	 * 
	 * @return
	 */
	public AttributeSelectMode getSelectMode();

	/**
	 * Returns the text field mode
	 * 
	 * @return
	 */
	AttributeTextFieldMode getTextFieldMode();

	/**
	 * Indicates a string value to use instead of "true"
	 * 
	 * @return
	 */
	String getTrueRepresentation();

	/**
	 * The Java type of the property
	 * 
	 * @return
	 */
	Class<?> getType();

	/**
	 * Indicates whether the attribute is a one-to-one or many-to-one attribute
	 * that can ben selected in a form
	 */
	boolean isComplexEditable();

	/**
	 * Is this a currency?
	 * 
	 * @return
	 */
	boolean isCurrency();

	/**
	 * Indicates this field must get the focus on a detail table
	 * 
	 * @return
	 */
	boolean isDetailFocus();

	/**
	 * Is this an embedded object
	 * 
	 * @return
	 */
	boolean isEmbedded();

	/**
	 * Is this an email field
	 * 
	 * @return
	 */
	boolean isEmail();

	/**
	 * Whether this attribute should be presented as an image
	 * 
	 * @return
	 */
	boolean isImage();

	/**
	 * Indicates whether this is the main attribute
	 * 
	 * @return
	 */
	boolean isMainAttribute();

	/**
	 * Is this a numeric attribute
	 * 
	 * @return
	 */
	boolean isNumerical();

	/**
	 * Indicates whether a numerical field is a percentage
	 * 
	 * @return
	 */
	boolean isPercentage();

	/**
	 * Indicates whether the attribute is read only
	 * 
	 * @return
	 */
	boolean isReadOnly();

	/**
	 * Indicates whether this is a required attribute
	 * 
	 * @return
	 */
	boolean isRequired();

	/**
	 * Indicates whether it is possible to search on this attribute
	 * 
	 * @return
	 */
	boolean isSearchable();

	/**
	 * Indicates whether saerching on this fields is case sensitive
	 * 
	 * @return
	 */
	public boolean isSearchCaseSensitive();

	/**
	 * Indicates whether to only match the prefix when performing a search
	 * 
	 * @return
	 */
	public boolean isSearchPrefixOnly();

	/**
	 * Indicates whether the attribute is sortable
	 * 
	 * @return
	 */
	boolean isSortable();

	/**
	 * Indicates whether the attribute is visible
	 * 
	 * @return
	 */
	boolean isVisible();

	/**
	 * Indicates whether the attribute must be shown in a table
	 * 
	 * @return
	 */
	boolean isVisibleInTable();

	/**
	 * Indicates whether this attribute represents a weekly recurring date
	 * 
	 * @return
	 */
	boolean isWeek();

	/**
	 * Marks this attribute as the main attribute
	 * 
	 * @param main
	 * @return
	 */
	void setMainAttribute(boolean main);

}
