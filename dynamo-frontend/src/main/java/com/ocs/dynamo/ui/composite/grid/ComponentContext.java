package com.ocs.dynamo.ui.composite.grid;

import com.ocs.dynamo.util.SystemPropertyUtils;

import lombok.Builder;
import lombok.Getter;

/**
 * A context for keeping track of component settings that cannot be modified
 * externally
 * 
 * @author BasRutten
 *
 */
@Getter
@Builder
public class ComponentContext {

	private boolean multiSelect;

	/**
	 * Whether to use check boxes for multiple selection inside popup
	 */
	@Builder.Default
	private boolean useCheckboxesForMultiSelect = SystemPropertyUtils.useGridSelectionCheckBoxes();

	private boolean popup;
}
