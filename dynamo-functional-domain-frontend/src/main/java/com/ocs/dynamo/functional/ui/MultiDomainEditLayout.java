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
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.filter.OrPredicate;
import com.ocs.dynamo.filter.SimpleStringPredicate;
import com.ocs.dynamo.functional.domain.Domain;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.component.CustomFieldContext;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.layout.BaseCustomComponent;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.layout.ServiceBasedSplitLayout;
import com.ocs.dynamo.ui.provider.QueryType;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.componentfactory.EnhancedFormLayout;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
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
	 * Callback method that is executed after the user selects a domain using the
	 * combo box
	 */
	@Getter
	@Setter
	private Consumer<Class<? extends Domain>> afterDomainSelected;

	/**
	 * Callback method that is executed to create a custom header layout that is
	 * displayed above the search grid
	 */
	@Getter
	@Setter
	private Supplier<Component> headerLayoutCreator;

	private List<Component> componentsToRegister = new ArrayList<>();

	private Map<String, Function<CustomFieldContext, Component>> customFields = new HashMap<>();

	@Getter
	@Setter
	private Predicate<Class<?>> deleteAllowed = clazz -> true;

	/**
	 * The classes of the domains that are managed by this screen
	 */
	@Getter
	private final List<Class<? extends Domain>> domainClasses;

	@Getter
	@Setter
	private BooleanSupplier editAllowed = () -> true;

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

	@Getter
	@Setter
	private Consumer<FlexLayout> postProcessButtonBar;

	@Getter
	@Setter
	private Consumer<VerticalLayout> postProcessSplitLayout;

	/**
	 * The selected domain class
	 */
	@Getter
	private Class<? extends Domain> selectedDomain;

	/**
	 * The layout that contains the controls for editing the selected domain
	 */
	private VerticalLayout selectedDomainLayout;

	/**
	 * The split layout that displays the currently selected domain
	 */
	@Getter
	private ServiceBasedSplitLayout<?, ?> splitLayout;

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
	 * Adds a custom field for a certain attribute
	 * 
	 * @param path     the path to the attribute
	 * @param function the function used to construct the custom component
	 */
	public void addCustomField(String path, Function<CustomFieldContext, Component> function) {
		customFields.put(path, function);
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
	public void build() {
		if (mainLayout == null) {

			mainLayout = new DefaultVerticalLayout(true, true);

			// form that contains the combo box
			EnhancedFormLayout form = new EnhancedFormLayout();
			mainLayout.add(form);

			// combo box for selecting domain
			ComboBox<Class<? extends Domain>> domainCombo = new ComboBox<>(message("ocs.select.domain"),
					getDomainClasses());
			domainCombo.setItemLabelGenerator(item -> getEntityModel(item).getDisplayName(VaadinUtils.getLocale()));
			domainCombo.setSizeFull();

			// respond to a change by displaying the correct domain
			domainCombo.addValueChangeListener(event -> selectDomain(event.getValue()));

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

	public boolean checkDeleteAllowed(Class<?> clazz) {
		return deleteAllowed == null ? true : deleteAllowed.test(clazz);
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
			componentsToRegister.clear();
			ServiceBasedSplitLayout<Integer, T> layout = new ServiceBasedSplitLayout<Integer, T>(baseService,
					getEntityModelFactory().getModel(domainClass), QueryType.ID_BASED, formOptions,
					new SortOrder<String>(Domain.ATTRIBUTE_NAME, SortDirection.ASCENDING));

			layout.setAfterLayoutBuilt(postProcessSplitLayout);
			layout.setMustEnableComponent((component, t) -> {
				if (layout.getRemoveButton() == component) {
					return deleteAllowed == null ? true : deleteAllowed.test(getSelectedDomain());
				}
				return true;
			});
			layout.setHeaderLayoutCreator(headerLayoutCreator);
			layout.setPostProcessMainButtonBar(postProcessButtonBar);
			layout.setQuickSearchFilterCreator(
					value -> new OrPredicate<>(new SimpleStringPredicate<>(Domain.ATTRIBUTE_NAME, value, false, false),
							new SimpleStringPredicate<>(Domain.ATTRIBUTE_CODE, value, false, false)));

			layout.setEditAllowed(getEditAllowed());
			for (Entry<String, Function<CustomFieldContext, Component>> entry : customFields.entrySet()) {
				layout.addCustomField(entry.getKey(), entry.getValue());
			}

			// register afterwards so that we actually register for the current layout
			// rather than the previous one
			layout.build();
			for (Component comp : componentsToRegister) {
				layout.registerComponent(comp);
			}

			return layout;
		} else {
			throw new OCSRuntimeException(message("ocs.no.service.class.found", domainClass));
		}
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
	 * @return the currently selected item
	 */
	public Domain getSelectedItem() {
		return (Domain) splitLayout.getSelectedItem();
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		build();
	}

	/**
	 * Registers a component. The component will be disabled or enabled depending on
	 * whether an item is selected
	 *
	 * @param button the button to register
	 */
	public final void registerComponent(Component comp) {
		componentsToRegister.add(comp);
	}

	/**
	 * Reloads the screen
	 */
	public final void reload() {
		if (splitLayout != null) {
			splitLayout.reload();
		}
	}

	/**
	 * Constructs a layout for editing a certain domain
	 *
	 * @param clazz the domain class
	 */
	public final void selectDomain(Class<? extends Domain> clazz) {
		selectedDomain = clazz;
		ServiceBasedSplitLayout<?, ?> layout = constructSplitLayout(clazz, formOptions);
		selectedDomainLayout.replace(splitLayout, layout);
		splitLayout = layout;
		if (afterDomainSelected != null) {
			afterDomainSelected.accept(clazz);
		}
	}
}
