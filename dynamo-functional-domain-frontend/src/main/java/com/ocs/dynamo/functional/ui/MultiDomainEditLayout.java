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
package com.ocs.dynamo.functional.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.filter.OrPredicate;
import com.ocs.dynamo.filter.SimpleStringPredicate;
import com.ocs.dynamo.functional.domain.Domain;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.layout.BaseCustomComponent;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.layout.ServiceBasedSplitLayout;
import com.ocs.dynamo.ui.provider.QueryType;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;

import lombok.Getter;
import lombok.Setter;

/**
 * A layout that allows the user to easily manage multiple domains. The list of
 * domain classes can be passed to the constructor. Please note that for every
 * domain class, you must define a (default) service
 *
 * @author bas.rutten
 */
public class MultiDomainEditLayout extends BaseCustomComponent {

	private static final long serialVersionUID = 4410282343830892631L;

	/**
	 * The classes of the domains that are managed by this screen
	 */
	private final List<Class<? extends Domain>> domainClasses;

	/**
	 * Entity model overrides
	 */
	private Map<Class<?>, String> entityModelOverrides = new HashMap<>();

	/**
	 * The form options (these are passed directly to the split layout)
	 */
	private FormOptions formOptions;

	/**
	 * The main layout
	 */
	private VerticalLayout mainLayout;

	/**
	 * The selected domain class
	 */
	private Class<? extends Domain> selectedDomain;

	/**
	 * The layout that contains the controls for editing the selected domain
	 */
	private VerticalLayout selectedDomainLayout;

	/**
	 * The split layout that displays the currently selected domain
	 */
	private ServiceBasedSplitLayout<?, ?> splitLayout;

	private List<Component> toRegister = new ArrayList<>();

	@Getter
	@Setter
	private Consumer<FlexLayout> postProcessMainButtonBar;

	@Getter
	@Setter
	private Supplier<Component> buildHeaderLayout;

	@Getter
	@Setter
	private Consumer<Class<? extends Domain>> afterDomainSelected;

	/**
	 * Constructor
	 *
	 * @param formOptions   the form options
	 * @param domainClasses the classes of the domains
	 */
	public MultiDomainEditLayout(FormOptions formOptions, List<Class<? extends Domain>> domainClasses) {
		this.formOptions = formOptions;
		this.domainClasses = domainClasses;
	}

	/**
	 * Adds an entity model override
	 *
	 * @param clazz     the entity class
	 * @param reference the reference to use for the overridden model
	 */
	public void addEntityModelOverride(Class<?> clazz, String reference) {
		entityModelOverrides.put(clazz, reference);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		build();
	}

	@Override
	public void build() {
		if (mainLayout == null) {

			mainLayout = new DefaultVerticalLayout(true, true);

			// form that contains the combo box
			FormLayout form = new FormLayout();
			mainLayout.add(form);

			// combo box for selecting domain
			ComboBox<Class<? extends Domain>> domainCombo = new ComboBox<>(message("ocs.select.domain"),
					getDomainClasses());
			domainCombo.setItemLabelGenerator(item -> getEntityModel(item).getDisplayName(VaadinUtils.getLocale()));
			domainCombo.setSizeFull();

			// respond to a change by displaying the correct domain
			domainCombo.addValueChangeListener(event -> selectDomain((Class<? extends Domain>) event.getValue()));

			form.add(domainCombo);

			selectedDomainLayout = new DefaultVerticalLayout();
			mainLayout.add(selectedDomainLayout);

			// select the first domain (if there is any)
			if (!getDomainClasses().isEmpty()) {
				domainCombo.setValue(getDomainClasses().get(0));
			}
			add(mainLayout);
		}
	}

	/**
	 * Constructs a custom field
	 * 
	 * @param entityModel    the entity model
	 * @param attributeModel the attribute mode
	 * @param viewMode       whether the screen is in view mode
	 * @return
	 */
	protected <R extends AbstractEntity<?>> Component constructCustomField(EntityModel<R> entityModel,
			AttributeModel attributeModel, boolean viewMode) {
		// overwrite in subclasses
		return null;
	}

	/**
	 * Construct a split layout for a certain domain
	 *
	 * @param domainClass the class of the domain
	 * @param formOptions the form options
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T extends Domain> ServiceBasedSplitLayout<Integer, T> constructSplitLayout(Class<T> domainClass,
			FormOptions formOptions) {

		BaseService<Integer, T> baseService = (BaseService<Integer, T>) ServiceLocatorFactory.getServiceLocator()
				.getServiceForEntity(domainClass);
		if (baseService != null) {
			toRegister.clear();
			ServiceBasedSplitLayout<Integer, T> layout = new ServiceBasedSplitLayout<Integer, T>(baseService,
					getEntityModelFactory().getModel(domainClass), QueryType.ID_BASED, formOptions,
					new SortOrder<String>(Domain.ATTRIBUTE_NAME, SortDirection.ASCENDING)) {

				private static final long serialVersionUID = -6504072714662771230L;

				@Override
				protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
						boolean viewMode, boolean searchMode) {
					return MultiDomainEditLayout.this.constructCustomField(entityModel, attributeModel, viewMode);
				}

				@Override
				protected boolean isEditAllowed() {
					return MultiDomainEditLayout.this.isEditAllowed();
				}

//				@Override
//				protected boolean mustEnableComponent(Component component, T selectedItem) {
//					if (getRemoveButton() == component) {
//						return isDeleteAllowed(getSelectedDomain());
//					}
//					return true;
//				}

//                @Override
//                protected void postProcessButtonBar(FlexLayout buttonBar) {
//                    MultiDomainEditLayout.this.postProcessButtonBar(buttonBar);
//                }

				@Override
				protected void postProcessLayout(VerticalLayout main) {
					MultiDomainEditLayout.this.postProcessSplitLayout(main);
				}
			};

			layout.setMustEnableComponent((component, t) -> {
				if (layout.getRemoveButton() == component) {
					return isDeleteAllowed(getSelectedDomain());
				}
				return true;
			});
			layout.setBuildHeaderLayout(buildHeaderLayout);
			layout.setPostProcessMainButtonBar(postProcessMainButtonBar);
			layout.setQuickSearchFilterSupplier(
					value -> new OrPredicate<>(new SimpleStringPredicate<>(Domain.ATTRIBUTE_NAME, value, false, false),
							new SimpleStringPredicate<>(Domain.ATTRIBUTE_CODE, value, false, false)));

			// register afterwards so that we actually register for the current layout
			// rather than the previous one
			layout.build();
			for (Component c : toRegister) {
				layout.registerComponent(c);
			}

			return layout;
		} else {
			throw new OCSRuntimeException(message("ocs.no.service.class.found", domainClass));
		}
	}

	public List<Class<? extends Domain>> getDomainClasses() {
		return domainClasses;
	}

	/**
	 * Returns the entity model to use for a certain domain class
	 *
	 * @param domainClass the domain class
	 * @return
	 */
	private <T> EntityModel<T> getEntityModel(Class<T> domainClass) {
		String override = entityModelOverrides.get(domainClass);
		return override != null ? getEntityModelFactory().getModel(override, domainClass)
				: getEntityModelFactory().getModel(domainClass);
	}

	/**
	 * @return the currently selected domain class
	 */
	public Class<? extends Domain> getSelectedDomain() {
		return selectedDomain;
	}

	/**
	 * @return the currently selected item
	 */
	public Domain getSelectedItem() {
		return (Domain) splitLayout.getSelectedItem();
	}

	/**
	 * @return the currently selected split layout
	 */
	public ServiceBasedSplitLayout<?, ?> getSplitLayout() {
		return splitLayout;
	}

	/**
	 * Check if the deletion of domain values for a certain class is allowed
	 *
	 * @param clazz the class
	 * @return
	 */
	protected boolean isDeleteAllowed(Class<?> clazz) {
		return true;
	}

	/**
	 * Indicates whether editing is allowed
	 */
	protected boolean isEditAllowed() {
		return true;
	}

	/**
	 * Post processes the button bar that appears below the search screen
	 * 
	 * @param buttonBar
	 */
	protected void postProcessButtonBar(FlexLayout buttonBar) {
		// overwrite in subclasses
	}

	/**
	 * Post processes the split layout after it has been created
	 *
	 * @param main
	 */
	protected void postProcessSplitLayout(VerticalLayout main) {
		// overwrite in subclasses
	}

	/**
	 * Registers a component. The component will be disabled or enabled depending on
	 * whether an item is selected
	 *
	 * @param button the button to register
	 */
	public void registerComponent(Component comp) {
		toRegister.add(comp);
	}

	/**
	 * Reloads the screen
	 */
	public void reload() {
		if (splitLayout != null) {
			splitLayout.reload();
		}
	}

	/**
	 * Constructs a layout for editing a certain domain
	 *
	 * @param clazz the domain class
	 */
	public void selectDomain(Class<? extends Domain> clazz) {
		selectedDomain = clazz;
		ServiceBasedSplitLayout<?, ?> layout = constructSplitLayout(clazz, formOptions);
		selectedDomainLayout.replace(splitLayout, layout);
		splitLayout = layout;
		if (afterDomainSelected != null) {
			afterDomainSelected.accept(clazz);
		}
	}
}
