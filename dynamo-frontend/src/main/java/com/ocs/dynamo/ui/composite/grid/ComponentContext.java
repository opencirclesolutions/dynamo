package com.ocs.dynamo.ui.composite.grid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.GroupTogetherMode;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.util.SystemPropertyUtils;
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

	private boolean multiSelect;

	/**
	 * Whether to use check boxes for multiple selection inside popup
	 */
	@Builder.Default
	private boolean useCheckboxesForMultiSelect = SystemPropertyUtils.useGridSelectionCheckBoxes();

	private boolean popup;

	/**
	 * Whether the edit form is nested within another form
	 */
	private boolean formNested;

	private boolean editable;

	@Builder.Default
	private Map<String, Supplier<Converter<?, ?>>> customConverters = new HashMap<>();

	@Builder.Default
	private Map<String, Supplier<Validator<?>>> customRequiredValidators = new HashMap<>();

	@Builder.Default
	private Map<String, Supplier<Validator<?>>> customValidators = new HashMap<>();

	@Builder.Default
	private String maxEditFormWidth = SystemPropertyUtils.getDefaultMaxEditFormWidth();

	@Getter
	@Setter
	@Builder.Default
	private List<String> editColumnThresholds = new ArrayList<>();

	@Getter
	@Setter
	@Builder.Default
	private List<String> searchColumnThresholds = new ArrayList<>();

	@Getter
	@Setter
	@Builder.Default
	private String maxSearchFormWidth = SystemPropertyUtils.getDefaultMaxSearchFormWidth();

	@Getter
	@Setter
	private BiConsumer<ModelBasedEditForm<ID, T>, T> customSaveConsumer;

	@Getter
	@Setter
	@Builder.Default
	private GroupTogetherMode groupTogetherMode = SystemPropertyUtils.getDefaultGroupTogetherMode();

	@Getter
	@Setter
	@Builder.Default
	private Map<String, String> fieldEntityModels = new HashMap<>();

	@Getter
	@Setter
	@Builder.Default
	private Integer groupTogetherWidth = SystemPropertyUtils.getDefaultGroupTogetherWidth();
	
	@Getter
	@Setter
	private BiConsumer<ModelBasedEditForm<ID, T>, T> afterEntitySelected;

	@Getter
	@Setter
	private Consumer<T> afterEntitySet;

	@Getter
	@Setter
	private BiConsumer<HasComponents, Boolean> afterLayoutBuilt;

	@Getter
	@Setter
	private BiConsumer<ModelBasedEditForm<ID, T>, Boolean> afterModeChanged;

	@Getter
	@Setter
	private Consumer<Integer> afterTabSelected;

	@Getter
	@Setter
	private BiConsumer<String, byte[]> afterUploadCompleted;

	public void addCustomConverter(String path, Supplier<Converter<?, ?>> converter) {
		customConverters.put(path, converter);
	}

	public void addCustomRequiredValidator(String path, Supplier<Validator<?>> validator) {
		customRequiredValidators.put(path, validator);
	}

	public void addCustomValidator(String path, Supplier<Validator<?>> validator) {
		customValidators.put(path, validator);
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
	public Validator findCustomValidator(AttributeModel attributeModel) {
		Supplier<Validator<?>> supplier = customValidators.get(attributeModel.getPath());
		if (supplier != null) {
			Validator<?> validator = supplier.get();
			return validator;
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

	/**
	 * Sets a custom entity model to use for a certain field/property
	 *
	 * @param path      the path of the property
	 * @param reference the unique ID of the entity model
	 */
	public void addFieldEntityModel(String path, String reference) {
		fieldEntityModels.put(path, reference);
	}

	public String getFieldEntityModel(String path) {
		return fieldEntityModels.get(path);
	}
}
