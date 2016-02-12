package nl.ocs.ui.component;

import java.io.Serializable;
import java.util.List;

import nl.ocs.dao.query.FetchJoinInformation;
import nl.ocs.domain.AbstractEntity;
import nl.ocs.domain.model.AttributeModel;
import nl.ocs.domain.model.EntityModel;
import nl.ocs.domain.model.util.EntityModelUtil;
import nl.ocs.service.BaseService;
import nl.ocs.service.MessageService;
import nl.ocs.ui.ServiceLocator;
import nl.ocs.ui.composite.dialog.ModelBasedSearchDialog;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

/**
 * A composite component that displays a selected entity and offers a search
 * dialog to search for another one
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 */
public class EntityLookupField<ID extends Serializable, T extends AbstractEntity<ID>> extends
		CustomField<T> {

	private static final long serialVersionUID = 5377765863515463622L;

	/**
	 * The attribute model of the property that is bound to this component
	 */
	private final AttributeModel attributeModel;

	/**
	 * The button used to clear the current selection
	 */
	private Button clearButton;

	/**
	 * The entity model of the entities that are displayed in the component
	 */
	private final EntityModel<T> entityModel;

	/**
	 * The filters to apply to the search dialog
	 */
	private List<Filter> filters;

	/**
	 * The joins to apply to the search in the search dialog
	 */
	private final FetchJoinInformation[] joins;

	/**
	 * The label that displays the currently selected item
	 */
	private Label label;

	/**
	 * The message service
	 */
	private MessageService messageService;

	/**
	 * The page length of the table in the search dialog
	 */
	private Integer pageLength;

	/**
	 * The button that brings up the search dialog
	 */
	private Button selectButton;

	private final BaseService<ID, T> service;

	/**
	 * The sort order to apply to the search dialog
	 */
	private SortOrder sortOrder;

	/**
	 * Constructor
	 * 
	 * @param service
	 * @param entityModel
	 * @param attributeModel
	 * @param filters
	 * @param sortOrder
	 * @param joins
	 */
	public EntityLookupField(BaseService<ID, T> service, EntityModel<T> entityModel,
			AttributeModel attributeModel, List<Filter> filters, SortOrder sortOrder,
			FetchJoinInformation... joins) {
		this.service = service;
		this.entityModel = entityModel;
		this.messageService = ServiceLocator.getMessageService();
		this.sortOrder = sortOrder;
		this.filters = filters;
		this.attributeModel = attributeModel;
		this.joins = joins;
	}

	public Button getClearButton() {
		return clearButton;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public Integer getPageLength() {
		return pageLength;
	}

	public Button getSelectButton() {
		return selectButton;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	@Override
	public Class<? extends T> getType() {
		return entityModel.getEntityClass();
	}

	@Override
	protected Component initContent() {
		HorizontalLayout bar = new DefaultHorizontalLayout(false, true);

		if (this.attributeModel != null) {
			this.setCaption(attributeModel.getDisplayName());
		}

		label = new Label();
		updateLabel(getValue());
		bar.addComponent(label);

		// button for selecting an entity - brings up the search dialog
		selectButton = new Button(messageService.getMessage("ocs.select"));
		selectButton.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 8377632639548698729L;

			@Override
			public void buttonClick(ClickEvent event) {
				ModelBasedSearchDialog<ID, T> dialog = new ModelBasedSearchDialog<ID, T>(service,
						entityModel, filters, sortOrder, false, joins) {

					private static final long serialVersionUID = -3432107069929941520L;

					@Override
					protected boolean doClose() {
						setValue(getSelectedItem());
						return true;
					}
				};
				dialog.setPageLength(pageLength);
				dialog.build();
				UI.getCurrent().addWindow(dialog);
			}
		});
		bar.addComponent(selectButton);

		// button for clearing the current selection
		clearButton = new Button(messageService.getMessage("ocs.clear"));
		clearButton.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 8377632639548698729L;

			@Override
			public void buttonClick(ClickEvent event) {
				setValue(null);
			}
		});
		bar.addComponent(clearButton);

		return bar;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (selectButton != null) {
			selectButton.setEnabled(enabled);
			clearButton.setEnabled(enabled);
		}
	}

	@Override
	protected void setInternalValue(T newValue) {
		super.setInternalValue(newValue);
		updateLabel(newValue);
	}

	public void setPageLength(Integer pageLength) {
		this.pageLength = pageLength;
	}

	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}

	@Override
	public void setValue(T newFieldValue) {
		super.setValue(newFieldValue);
		updateLabel(newFieldValue);
	}

	/**
	 * Updates the value that is displayed in the label
	 * 
	 * @param newValue
	 *            the new value
	 */
	private void updateLabel(T newValue) {
		if (label != null) {
			label.setValue(newValue == null ? messageService.getMessage("ocs.no.item.selected")
					: EntityModelUtil.getDisplayPropertyValue(newValue, entityModel));
		}
	}

}
