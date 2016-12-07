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
package com.ocs.dynamo.ui.composite.table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.Buildable;
import com.ocs.dynamo.ui.ServiceLocator;
import com.ocs.dynamo.ui.composite.table.export.TableExportActionHandler;
import com.ocs.dynamo.ui.composite.table.export.TableExportMode;
import com.ocs.dynamo.ui.converter.ConverterFactory;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.PasteUtils;
import com.ocs.dynamo.utils.SystemPropertyUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.event.Action;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;

/**
 * A custom tree table for displaying a hierarchical data collection. This table allows data
 * modification although in a rather cumbersome way. It is only suitable for displaying fairly
 * limited data collections since it loads everything in memory
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key of the child entity
 * @param <U>
 *            type of the child entity
 * @param <ID2>
 *            type of the primary key of the parent entity
 * @param <V>
 *            type of the parent entity
 */
@SuppressWarnings({ "serial", "unchecked" })
public abstract class InMemoryTreeTable<ID, U extends AbstractEntity<ID>, ID2, V extends AbstractEntity<ID2>> extends
        TreeTable implements Buildable {

	// the prefix that is added to the key of a child row
	public static final String PREFIX_CHILDROW = "c";

	// the prefix that is added to the key of a parent row
	public static final String PREFIX_PARENTROW = "p";

	// the property ID of the column on which the user performed a right-click
	private String clickedColumn;

	// the message service
	private MessageService messageService;

	// whether to propagate changes (disabled during screen building)
	private boolean propagateChanges = true;

	// whether the screen is in view mode
	private boolean viewMode;

	public InMemoryTreeTable() {
		this.messageService = ServiceLocator.getMessageService();
	}

	/**
	 * Adds container properties and sets column headers
	 */
	protected abstract void addContainerProperties();

	@Override
	public void build() {
		setEditable(true);
		setSizeFull();

		// retrieve the rows to display
		final List<V> parentCollection = getParentCollection();

		addContainerProperties();
		int nrOfProperties = getContainerPropertyIds().size();

		String[] sumColumns = getSumColumns();

		// adds a style generator that highlights the parent rows in bold
		setCellStyleGenerator(new CellStyleGenerator() {

			@Override
			public String getStyle(Table source, Object itemId, Object propertyId) {
				if (itemId.toString().startsWith(PREFIX_PARENTROW)) {
					return DynamoConstants.CSS_PARENT_ROW;
				} else {
					return getCustomStyle(itemId, propertyId);
				}
			}
		});

		// custom field factory for creating editable fields for certain
		// properties
		this.setTableFieldFactory(new TableFieldFactory() {
			private boolean editAllowed = isEditAllowed();

			@Override
			public Field<?> createField(Container container, Object itemId, final Object propertyId, Component uiContext) {
				if (!isViewMode() && editAllowed && isEditable(propertyId.toString())
				        && itemId.toString().startsWith(PREFIX_CHILDROW)) {
					final TextField tf = new TextField();
					tf.setData(itemId);
					tf.setNullRepresentation("");
					tf.setNullSettingAllowed(true);
					// set the appropriate converter
					tf.setConverter(createConverter(propertyId.toString()));
					tf.addFocusListener(new FocusListener() {

						@Override
						public void focus(FocusEvent event) {
							clickedColumn = propertyId.toString();
						}
					});

					// add a value change listener (for responding to paste
					// events and normal changes)
					tf.addValueChangeListener(new Property.ValueChangeListener() {

						@Override
						public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
							if (propagateChanges) {
								handleChange(tf, propertyId.toString(), (String) event.getProperty().getValue());
							}
						}
					});

					// align all text field to the right
					tf.setStyleName(DynamoConstants.CSS_NUMERICAL);

					postProcessField(propertyId, itemId, tf);
					return tf;
				}
				return null;
			}
		});

		int parentCounter = 0;
		int childCounter = 0;

		// footer sums
		Map<String, BigDecimal> totalSumMap = new HashMap<>();

		for (String s : sumColumns) {
			totalSumMap.put(s, BigDecimal.ZERO);
		}

		for (V v : parentCollection) {

			// sum on the parent level
			Map<String, BigDecimal> sumMap = new HashMap<>();
			for (String s : sumColumns) {
				sumMap.put(s, BigDecimal.ZERO);
			}

			Object[] parentRow = new Object[nrOfProperties];
			fillParentRow(parentRow, v);

			Object parentId = this.addItem(parentRow, PREFIX_PARENTROW + parentCounter);
			this.setChildrenAllowed(parentId, true);
			this.setCollapsed(parentId, false);

			List<U> rowCollection = getRowCollection(v);
			for (U u : rowCollection) {
				Object[] childRow = new Object[nrOfProperties];
				fillChildRow(childRow, u, v);

				// add the child and set the connection to the parent
				Object childId = this.addItem(childRow, PREFIX_CHILDROW + childCounter);
				this.setParent(childId, parentId);
				this.setChildrenAllowed(childId, false);

				// update the sum columns on the parent level
				for (String column : sumColumns) {
					Number value = (Number) this.getItem(childId).getItemProperty(column).getValue();
					BigDecimal sum = sumMap.get(column);
					sumMap.put(column, sum.add(value == null ? BigDecimal.ZERO : toBigDecimal(value)));
				}
				childCounter++;
			}

			// set the sum on the parent level and update the grand total
			for (String s : sumColumns) {
				BigDecimal sum = sumMap.get(s);
				this.getItem(parentId).getItemProperty(s).setValue(convertNumber(sum, s));
				BigDecimal totalSum = totalSumMap.get(s);
				totalSumMap.put(s, totalSum.add(sum));
			}

			parentCounter++;
		}

		// update the footer sums
		for (String column : sumColumns) {
			BigDecimal bd = totalSumMap.get(column);
			this.setColumnFooter(column, convertToString(bd, column));
		}
		setFooterVisible(true);

		// right align certain columns
		for (Object propertyId : this.getContainerPropertyIds()) {
			if (isRightAligned(propertyId.toString())) {
				this.setColumnAlignment(propertyId, Table.Align.RIGHT);
			}
		}

		// respond to a click by storing the column ID
		this.addItemClickListener(new ItemClickListener() {

			@Override
			public void itemClick(ItemClickEvent event) {
				if (MouseButton.RIGHT.equals(event.getButton())) {
					clickedColumn = (String) event.getPropertyId();
				}
			}
		});

		if (isShowActionMenu()) {
			constructActionMenu(parentCollection);
		}
	}

	/**
	 * Constructs the right-click menu
	 * 
	 * @param parentCollection
	 */
	protected void constructActionMenu(final List<V> parentCollection) {

		final Action copyPreviousAction = new Action(messageService.getMessage("ocs.copy.previous.column"));
		final Action clearColumnAction = new Action(messageService.getMessage("ocs.clear.column"));
		final Action fillColumnAction = new Action(messageService.getMessage("ocs.fill.column"));

		final List<Action> actions = new ArrayList<>();
		if (!isViewMode() && isEditAllowed()) {
			actions.addAll(Lists.newArrayList(copyPreviousAction, fillColumnAction, clearColumnAction));
		}

		actions.addAll(getAdditionalActions());

		this.addActionHandler(new Action.Handler() {

			@Override
			public Action[] getActions(Object target, Object sender) {
				return actions.toArray(new Action[0]);
			}

			@Override
			@SuppressWarnings("unused")
			public void handleAction(Action action, Object sender, Object target) {
				if (!StringUtils.isEmpty(clickedColumn) && isRightClickable(clickedColumn)) {
					propagateChanges = false;

					if (action == fillColumnAction) {
						// fill all cells in column with the same value
						String targetRow = (String) target;
						Number value = (Number) getItem(targetRow).getItemProperty(clickedColumn).getValue();

						int i = 0;
						for (V v : parentCollection) {
							for (U u : getRowCollection(v)) {
								if (!targetRow.equals(PREFIX_CHILDROW + i)) {
									InMemoryTreeTable.this.handleChange(PREFIX_CHILDROW + i, clickedColumn,
									        value == null ? null : convertToString(toBigDecimal(value), clickedColumn));
								}
								i++;
							}
						}
					} else if (action == copyPreviousAction) {
						// copy all values from the previous row
						String previousColumnId = getPreviousColumnId(clickedColumn);
						copyValue(previousColumnId, clickedColumn);
					} else if (action == clearColumnAction) {
						// empty the entire row
						int i = 0;
						for (V v : parentCollection) {
							for (U u : getRowCollection(v)) {
								InMemoryTreeTable.this.handleChange(PREFIX_CHILDROW + i, clickedColumn, null);
								i++;
							}
						}
					} else {
						// custom action handling
						handleAdditionalAction(action, sender, target);
					}
					propagateChanges = true;
				}
				clickedColumn = null;
			}
		});

		// Add export functionality
		if (SystemPropertyUtils.allowTableExport()) {
			addActionHandler(new TableExportActionHandler(UI.getCurrent(), null, getReportTitle(), true,
			        TableExportMode.EXCEL, null));
			addActionHandler(new TableExportActionHandler(UI.getCurrent(), null, getReportTitle(), true,
			        TableExportMode.EXCEL_SIMPLIFIED, null));
			addActionHandler(new TableExportActionHandler(UI.getCurrent(), null, getReportTitle(), true,
			        TableExportMode.CSV, null));
		}
	}

	/**
	 * Converts a string value to its numeric representation
	 * 
	 * @param value
	 *            the string value
	 * @param propertyId
	 *            the ID of the property
	 * @return
	 */
	protected Number convertFromString(String value, String propertyId) {
		Class<?> clazz = getEditablePropertyClass(propertyId);
		if (clazz.equals(Integer.class)) {
			return VaadinUtils.stringToInteger(true, value);
		} else if (clazz.equals(Long.class)) {
			return VaadinUtils.stringToLong(true, value);
		} else if (clazz.equals(BigDecimal.class)) {
			return VaadinUtils.stringToBigDecimal(false, false, true, value);
		}
		return null;
	}

	/**
	 * Converts a numeric value from its BigDecimal representation to its native form
	 * 
	 * @param value
	 *            the value
	 * @param propertyId
	 *            the ID of the property
	 * @return
	 */
	protected Number convertNumber(BigDecimal value, String propertyId) {
		Class<?> clazz = getEditablePropertyClass(propertyId);
		if (clazz.equals(Integer.class)) {
			return value.intValue();
		} else if (clazz.equals(Long.class)) {
			return value.longValue();
		} else if (clazz.equals(BigDecimal.class)) {
			return value;
		}
		return null;
	}

	/**
	 * Converts a numeric value to its string representation
	 * 
	 * @param value
	 *            the BigDecimal value
	 * @param propertyId
	 *            the ID of the property
	 * @return
	 */
	protected String convertToString(BigDecimal value, String propertyId) {
		if (value == null) {
			return null;
		}

		Class<?> clazz = getEditablePropertyClass(propertyId);
		if (clazz.equals(Integer.class)) {
			return VaadinUtils.integerToString(true, value.intValue());
		} else if (clazz.equals(Long.class)) {
			return VaadinUtils.longToString(true, value.longValue());
		} else if (clazz.equals(BigDecimal.class)) {
			return VaadinUtils.bigDecimalToString(false, true, value);
		}
		return null;
	}

	/**
	 * Copies all values from the source column to the target column
	 * 
	 * @param sourceColumnId
	 *            the ID of the source column
	 * @param targetColumnId
	 *            the ID of the target column
	 */
	@SuppressWarnings("unused")
	protected void copyValue(String sourceColumnId, String targetColumnId) {
		int i = 0;
		if (!StringUtils.isEmpty(sourceColumnId)) {
			for (V v : getParentCollection()) {
				for (U u : getRowCollection(v)) {
					Object value = getItem(PREFIX_CHILDROW + i).getItemProperty(sourceColumnId).getValue();
					if (value instanceof Number || value == null) {
						InMemoryTreeTable.this.handleChange(PREFIX_CHILDROW + i, targetColumnId, value == null ? null
						        : convertToString(toBigDecimal((Number) value), targetColumnId));
					}
					i++;
				}
			}
		}
	}

	/**
	 * Creates a converter for a certain field
	 * 
	 * @param propertyId
	 *            the ID of the property for which to create a converter
	 * @return
	 */
	protected Converter<String, ?> createConverter(String propertyId) {
		Class<?> clazz = getEditablePropertyClass(propertyId);
		if (clazz.equals(Integer.class)) {
			return ConverterFactory.createIntegerConverter(useThousandsGrouping());
		} else if (clazz.equals(Long.class)) {
			return ConverterFactory.createLongConverter(useThousandsGrouping());
		} else if (clazz.equals(BigDecimal.class)) {
			return ConverterFactory.createBigDecimalConverter(false, false, useThousandsGrouping(),
			        getDefaultPrecision(), null);
		}
		return null;
	}

	/**
	 * Calculates the difference between two values, either of which can be null
	 * 
	 * @param newValue
	 *            the new value
	 * @param oldValue
	 *            the old value
	 * @return
	 */
	protected BigDecimal difference(BigDecimal newValue, BigDecimal oldValue) {
		return (newValue == null ? BigDecimal.ZERO : newValue).subtract(oldValue == null ? BigDecimal.ZERO : oldValue);
	}

	/**
	 * Fills a child row with appropriate data
	 * 
	 * @param row
	 *            the row
	 * @param entity
	 *            the child entity
	 * @param parentEntity
	 *            the parent entity
	 */
	protected abstract void fillChildRow(Object[] row, U entity, V parentEntity);

	/**
	 * Fills the parent row with appropriate data
	 * 
	 * @param row
	 *            the row
	 * @param entity
	 *            the (parent) entity
	 */
	protected abstract void fillParentRow(Object[] row, V entity);

	/**
	 * @return any additional actions to add to the context menu
	 */
	protected List<Action> getAdditionalActions() {
		return Lists.newArrayList();
	}

	public String getClickedColumn() {
		return clickedColumn;
	}

	/**
	 * Returns an array of Strings containing the propertyId IDs of the columns to update after a
	 * change to a certain column
	 * 
	 * @param propertyId
	 *            the propertyId of the column that changes
	 * @return
	 */
	protected abstract String[] getColumnstoUpdate(String propertyId);

	/**
	 * Returns the custom CSS class name for a certain cell
	 * 
	 * @param itemId
	 *            the row ID of the cell
	 * @param propertyId
	 *            the column ID of the cell
	 * @return
	 */
	protected String getCustomStyle(Object itemId, Object propertyId) {
		// overwrite in subclasses
		return null;
	}

	/**
	 * Returns the class for an editable property
	 * 
	 * @param propertyId
	 *            the ID of the column
	 * @return
	 */
	protected abstract Class<?> getEditablePropertyClass(String propertyId);

	/**
	 * Indicates which property represents the primary key -
	 * 
	 * @return
	 */
	protected abstract String getKeyPropertyId();

	/**
	 * 
	 * @return
	 */
	public MessageService getMessageService() {
		return messageService;
	}

	/**
	 * Returns the primary key value for a certain row
	 * 
	 * @param itemId
	 *            the ID of the row
	 * @return
	 */
	public String getObjectKey(String itemId) {
		return (String) getItem(itemId).getItemProperty(getKeyPropertyId()).getValue();
	}

	/**
	 * Returns the offset (row counter) of a certain row ID
	 * 
	 * @param rowId
	 *            the row ID
	 * @return
	 */
	private int getOffset(String rowId) {
		return Integer.parseInt(rowId.substring(1));
	}

	/**
	 * Returns the entities that make up the rows
	 * 
	 * @return
	 */
	protected abstract List<V> getParentCollection();

	/**
	 * Returns the previous column ID given a certain column ID
	 * 
	 * @param columnId
	 *            the column ID
	 * @return
	 */
	protected abstract String getPreviousColumnId(String columnId);

	/**
	 * @return the title of the report that can be exported
	 */
	protected abstract String getReportTitle();

	/**
	 * Returns the entities that make up the child rows
	 * 
	 * @return
	 */
	protected abstract List<U> getRowCollection(V parent);

	/**
	 * Returns the property IDs of the columns for which a sum (on the parent level) must be
	 * calculated
	 * 
	 * @return
	 */
	protected abstract String[] getSumColumns();

	/**
	 * Callback method for handling any extra context menu actions
	 * 
	 * @param action
	 *            the action that is being carried out
	 * @param sender
	 *            the sender of the action
	 * @param target
	 *            the target of the action
	 */
	protected void handleAdditionalAction(Action action, Object sender, Object target) {
		// do nothing
	}

	/**
	 * Handles a change
	 * 
	 * @param rowId
	 *            the ID of the row in which a value changes
	 * @param propertyId
	 *            the ID of the column in which a value changes
	 * @param value
	 *            the new value
	 */
	public void handleChange(String rowId, String propertyId, String value) {

		// get the key of the child entity
		String childKey = getObjectKey(rowId);

		// get the ID of the parent row
		String parentId = (String) getParent(rowId);
		String parentKey = null;

		// get the key of the parent entity
		if (parentId != null) {
			parentKey = getObjectKey(parentId);
		}

		if (childKey != null && parentKey != null) {
			String[] values = PasteUtils.split(value);
			if (values != null && values.length > 1) {
				// copy/paste of multiple values
				propagateChanges = false;

				// determine the index of the starting row
				int offset = getOffset(rowId);

				for (int i = 0; i < values.length; i++) {

					String rId = PREFIX_CHILDROW + (offset + i);

					Item item = getItem(rId);
					if (item != null) {

						// get the child key and parent key of this row
						String cKey = (String) getItem(rId).getItemProperty(getKeyPropertyId()).getValue();
						parentId = (String) getParent(rId);
						if (parentId != null) {
							parentKey = (String) getItem(parentId).getItemProperty(getKeyPropertyId()).getValue();
						}

						// propagate the change
						Number change = handleChange(propertyId, rId, parentId, cKey, parentKey, values[i]);

						// update the dependent fields
						if (hasValueChanged(change)) {
							updateDependentFields(rId, parentId, propertyId, toBigDecimal(change));
							setValue(rId, propertyId, values[i]);
						}
					}
				}
				VaadinUtils.enableCopyPaste();
				propagateChanges = true;
			} else {
				// update a single value
				Number change = handleChange(propertyId.toString(), rowId, parentId, childKey, parentKey, value);
				if (hasValueChanged(change)) {
					updateDependentFields(rowId, parentId, propertyId, toBigDecimal(change));
					setValue(rowId, propertyId, value);
				}
			}
		}
	}

	/**
	 * Handles a change of an editable value
	 * 
	 * @param propertyId
	 *            ID of the property that has changed
	 * @param rowId
	 *            ID of the row
	 * @param parentRowId
	 *            ID of the parent row
	 * @param childKey
	 *            ID of the child object
	 * @param parentKey
	 *            ID of the parent object
	 * @param newValue
	 *            the new value (can be NULL in case the cell is emptied)
	 */
	protected abstract Number handleChange(String propertyId, String rowId, String parentRowId, String childKey,
	        String parentKey, Object newValue);

	/**
	 * Handles the change of a text field
	 * 
	 * @param tf
	 *            the text field
	 * @param propertyId
	 *            the name of the property that is changed
	 * @param value
	 *            the new value
	 */
	private void handleChange(TextField tf, String propertyId, String value) {
		handleChange((String) tf.getData(), propertyId, value);
	}

	private boolean hasValueChanged(Number change) {
		return change != null && Math.abs(change.doubleValue()) > 0.00001;
	}

	/**
	 * Indicates whether a certain column can be edited
	 * 
	 * @param propertyId
	 *            the ID of the column
	 * @return
	 */
	protected abstract boolean isEditable(String propertyId);

	public boolean isPropagateChanges() {
		return propagateChanges;
	}

	/**
	 * Indicates whether a certain column must be right-aligned
	 * 
	 * @param propertyId
	 *            the ID of the property
	 * @return
	 */
	protected abstract boolean isRightAligned(String propertyId);

	/**
	 * Indicates whether a right click menu should be shown
	 * 
	 * @param columnId
	 *            the ID of the column
	 * @return
	 */
	protected boolean isRightClickable(String columnId) {
		return false;
	}

	protected boolean isShowActionMenu() {
		return true;
	}

	public boolean isViewMode() {
		return viewMode;
	}

	/**
	 * @param value
	 * @return
	 */
	protected Number nvl(Number value) {
		if (value instanceof BigDecimal) {
			return value == null ? BigDecimal.ZERO : value;
		} else if (value instanceof Long) {
			return value == null ? 0L : value;
		} else if (value instanceof Integer) {
			return value == null ? 0 : value;
		}
		return value;
	}

	/**
	 * Post process an editable field
	 * 
	 * @param propertyId
	 *            the ID of the property
	 * @param itemId
	 *            the ID of the item (entity)
	 * @param field
	 *            the field
	 */
	protected void postProcessField(Object propertyId, Object itemId, Field<?> field) {
		// overwrite in subclass
	}

	/**
	 * Stores the column on which the user clicked for the last time. This is used to determine the
	 * action to undertake from the right-click menu
	 * 
	 * @param clickedColumn
	 */
	public void setClickedColumn(String clickedColumn) {
		this.clickedColumn = clickedColumn;
	}

	public void setPropagateChanges(boolean propagateChanges) {
		this.propagateChanges = propagateChanges;
	}

	/**
	 * Sets the value for a certain field
	 * 
	 * @param rowId
	 *            the row ID
	 * @param propertyId
	 *            the property ID
	 * @param value
	 *            the string representation of the value
	 */
	private void setValue(Object rowId, String propertyId, String value) {
		Number number = convertFromString(value, propertyId);
		getItem(rowId).getItemProperty(propertyId).setValue(number);
	}

	public void setViewMode(boolean viewMode) {
		this.viewMode = viewMode;
	}

	/**
	 * Converts a numeric value to a BigDecimal
	 * 
	 * @param value
	 *            the value to convert
	 * @return
	 */
	protected BigDecimal toBigDecimal(Number value) {
		return value == null ? null : BigDecimal.valueOf(value.doubleValue());
	}

	/**
	 * Converts a value to an integer (taking decimal separators into account)
	 * 
	 * @param value
	 *            the value to convert
	 */
	protected Integer toInt(Object value) {
		String temp = value == null ? null : (String) value;
		if (StringUtils.isEmpty(temp)) {
			return null;
		}
		// remove any separators that might have been copy/pasted
		temp = PasteUtils.stripSeparators(temp);
		// use Vaadin converter to make sure the formatting is correct
		return ConverterFactory.createIntegerConverter(false).convertToModel(temp, Integer.class, null);
	}

	/**
	 * Updates the dependent fields after a change
	 * 
	 * @param rowId
	 *            the ID of the row
	 * @param parentRowId
	 *            the ID of the parent row
	 * @param propertyId
	 *            the ID of the property
	 * @param delta
	 *            the delta between the old and the new value
	 */
	private void updateDependentFields(String rowId, String parentRowId, String propertyId, BigDecimal delta) {
		for (String column : getColumnstoUpdate(propertyId)) {
			updateTableField(rowId, column, delta);
			updateParentAndFooter(parentRowId, column, delta);
		}
		updateParentAndFooter(parentRowId, propertyId, delta);
	}

	/**
	 * Updates parent and footer totals in response to a change
	 * 
	 * @param parentRowId
	 *            the row ID of the parent row
	 * @param propertyId
	 *            the ID of the property to update
	 * @param delta
	 *            the change
	 */
	private void updateParentAndFooter(String parentRowId, String propertyId, BigDecimal delta) {
		updateTableField(parentRowId, propertyId, delta);
		String footerString = this.getColumnFooter(propertyId);
		Number footerValue = convertFromString(footerString, propertyId);
		BigDecimal bd = footerValue == null ? BigDecimal.ZERO : toBigDecimal(footerValue);
		bd = bd.add(delta);
		this.setColumnFooter(propertyId, convertToString(bd, propertyId));
	}

	/**
	 * Updates a calculated field in response to a change
	 * 
	 * @param rowId
	 *            the ID of the row to update
	 * @param propertyId
	 *            the property ID
	 * @param delta
	 *            the change
	 */
	private void updateTableField(String rowId, String propertyId, BigDecimal delta) {
		Number value = (Number) getItem(rowId).getItemProperty(propertyId).getValue();
		BigDecimal bd = value == null ? BigDecimal.ZERO : toBigDecimal(value);
		bd = bd.add(delta);
		getItem(rowId).getItemProperty(propertyId).setValue(convertNumber(bd, propertyId));
	}

	/**
	 * Indicates whether to use the thousands separator in edit fields
	 * 
	 * @return
	 */
	private boolean useThousandsGrouping() {
		return Boolean.getBoolean(DynamoConstants.SP_THOUSAND_GROUPING);
	}

	/**
	 * Indicates whether is is allowed to edit this component
	 * 
	 * @return
	 */
	protected boolean isEditAllowed() {
		return true;
	}

	/**
	 * Returns the default precision
	 * 
	 * @return
	 */
	protected int getDefaultPrecision() {
		Integer i = Integer.getInteger(DynamoConstants.SP_DECIMAL_PRECISION);
		return i == null ? SystemPropertyUtils.getDefaultDecimalPrecision() : i;
	}
}
