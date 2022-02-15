package com.ocs.dynamo.ui.composite.grid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.GroupTogetherMode;
import com.ocs.dynamo.ui.component.CustomFieldContext;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.Converter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * A context for keeping track of component settings that cannot be modified
 * externally
 * 
 * @author BasRutten
 *
 */
@Getter
@Builder(toBuilder = true)
@Setter
public class ComponentContext<ID extends Serializable, T extends AbstractEntity<ID>> {

	private BiConsumer<ModelBasedEditForm<ID, T>, T> afterEntitySelected;

	private Consumer<T> afterEntitySet;

	private BiConsumer<HasComponents, Boolean> afterLayoutBuilt;

	private BiConsumer<ModelBasedEditForm<ID, T>, Boolean> afterModeChanged;

	private Consumer<Integer> afterTabSelected;

	private BiConsumer<String, byte[]> afterUploadCompleted;

	@Builder.Default
	private Map<String, Supplier<Converter<?, ?>>> customConverters = new HashMap<>();

	@Builder.Default
	private Map<String, Supplier<Validator<?>>> customRequiredValidators = new HashMap<>();

	@Builder.Default
	private Map<String, Function<CustomFieldContext, Component>> customFields = new HashMap<>();

	private BiConsumer<ModelBasedEditForm<ID, T>, T> customSaveConsumer;

	private Function<RuntimeException, Boolean> customSaveExceptionHandler;

	@Builder.Default
	private Map<String, Supplier<Validator<?>>> customValidators = new HashMap<>();

	private boolean editable;

	@Getter
	@Setter
	@Builder.Default
	private List<String> editColumnThresholds = new ArrayList<>();

	@Getter
	@Setter
	@Builder.Default
	private Map<String, String> fieldEntityModels = new HashMap<>();

	/**
	 * Whether the edit form is nested within another form
	 */
	private boolean formNested;

	@Getter
	@Setter
	@Builder.Default
	private GroupTogetherMode groupTogetherMode = SystemPropertyUtils.getDefaultGroupTogetherMode();

	@Getter
	@Setter
	@Builder.Default
	private Integer groupTogetherWidth = SystemPropertyUtils.getDefaultGroupTogetherWidth();

	@Builder.Default
	private String maxEditFormWidth = SystemPropertyUtils.getDefaultMaxEditFormWidth();

	@Getter
	@Setter
	@Builder.Default
	private String maxSearchFormWidth = SystemPropertyUtils.getDefaultMaxSearchFormWidth();

	private boolean multiSelect;

	private boolean popup;

	@Getter
	@Setter
	@Builder.Default
	private List<String> searchColumnThresholds = new ArrayList<>();

	/**
	 * Whether to use check boxes for multiple selection inside popup
	 */
	@Builder.Default
	private boolean useCheckboxesForMultiSelect = SystemPropertyUtils.useGridSelectionCheckBoxes();

	public void addCustomConverter(String path, Supplier<Converter<?, ?>> converter) {
		customConverters.put(path, converter);
	}

	public void addCustomRequiredValidator(String path, Supplier<Validator<?>> validator) {
		customRequiredValidators.put(path, validator);
	}

	public void addCustomValidator(String path, Supplier<Validator<?>> validator) {
		customValidators.put(path, validator);
	}

	/**
	 * Sets a custom entity model to use for a certain field/property
	 *
	 * @param path      the path of the property
	 * @param reference the unique ID of the entity model
	 */
	public void addFieldEntityModel(String path, String reference) {
		fieldEntityModels.put(path, reference);
	}

	@SuppressWarnings("rawtypes")
	public Converter findCustomConverter(AttributeModel attributeModel) {
		Supplier<Converter<?, ?>> supplier = customConverters.get(attributeModel.getPath());
		if (supplier != null) {
			Converter<?, ?> converter = supplier.get();
			return converter;
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Validator findCustomRequiredValidator(AttributeModel attributeModel) {
		Supplier<Validator<?>> supplier = customRequiredValidators.get(attributeModel.getPath());
		if (supplier != null) {
			Validator<?> validator = supplier.get();
			return validator;
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Validator findCustomValidator(AttributeModel attributeModel) {
		Supplier<Validator<?>> supplier = customValidators.get(attributeModel.getPath());
		if (supplier != null) {
			Validator<?> validator = supplier.get();
			return validator;
		}
		return null;
	}

	public String getFieldEntityModel(String path) {
		return fieldEntityModels.get(path);
	}

	public void addCustomField(String path, Function<CustomFieldContext,Component> function) {
		customFields.put(path, function);
	}

	public Function<CustomFieldContext, Component> getCustomFieldCreator(String path) {
		return customFields.get(path);
	}
}
