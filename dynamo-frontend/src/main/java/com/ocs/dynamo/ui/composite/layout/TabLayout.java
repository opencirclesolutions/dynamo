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
package com.ocs.dynamo.ui.composite.layout;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.Reloadable;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;

import lombok.Getter;
import lombok.Setter;

/**
 * A layout that contains a tab sheet with tabs that are lazily loaded. Use the
 * getTabCaptions method to specify the captions of the tabs, and (implicitly)
 * the number of tabs. Whenever the user selects a tab for the first time, the
 * initTab method is called in order to lazily construct the tab.
 * 
 * @author bas.rutten
 * @param <ID> type of the primary key
 * @param <T>  type of the entity
 */
public class TabLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseCustomComponent
		implements Reloadable {

	private static final long serialVersionUID = 3788799136302802727L;

	@Getter
	@Setter
	private BiConsumer<Integer, Component> beforeReload;

	/**
	 * The caption to display above the tabs
	 */
	private Span caption;

	@Getter
	@Setter
	private String[] captions;

	/**
	 * The indices of the tabs that have already been constructed
	 */
	private Set<Integer> constructedTabs = new HashSet<>();

	@Getter
	@Setter
	private Function<Integer, String> descriptionCreator;

	/**
	 * The entity that is being displayed
	 */
	@Getter
	private T entity;

	/**
	 * The code that is used to construct an icon for a tab
	 */
	@Getter
	@Setter
	private Function<Integer, Icon> iconCreator;

	/**
	 * The tab sheet component
	 */
	private TabWrapper tabs;

	@Getter
	@Setter
	private Function<Integer, Component> tabCreator;

	@Getter
	@Setter
	private Supplier<String> titleCreator = () -> "";

	/**
	 * Constructor
	 * 
	 * @param entity the entity to display
	 */
	public TabLayout(T entity) {
		setSpacing(false);
		setPadding(false);
		addClassName(DynamoConstants.CSS_TAB_LAYOUT);
		this.entity = entity;
	}

	@Override
	public void build() {
		if (tabs == null) {
			VerticalLayout main = new DefaultVerticalLayout();
			add(main);

			String title = titleCreator.get();
			if (!StringUtils.isEmpty(title)) {
				caption = new Span(title);
				main.add(caption);
			}

			tabs = new TabWrapper();
			tabs.setSizeFull();

			main.add(tabs);
			setupSheets(tabs);
		}
	}

	/**
	 * Returns the tab identified by a certain index
	 * 
	 * @param index the index
	 * @return the tab identified by the index
	 */
	@Override
	public Component getComponentAt(int index) {
		return tabs.getComponentAt(index);
	}

	public Tab getTabByIndex(int index) {
		return tabs.getTabByIndex(index);
	}

	/**
	 * Constructs or reloads a tab
	 * 
	 * @param selectedTab the currently selected tab
	 */
	@SuppressWarnings("unchecked")
	protected void initOrReload(int index) {

		// lazily load a tab
		if (!constructedTabs.contains(index)) {
			constructedTabs.add(index);
			// paste the real tab into the placeholder
			Component constructed = tabCreator.apply(index);
			tabs.setComponent(index, constructed);
		} else {
			// reload the tab if needed
			Component component = tabs.getComponentAt(index);
			if (component instanceof Reloadable) {
				if (component instanceof CanAssignEntity) {
					((CanAssignEntity<?, T>) component).assignEntity(getEntity());
				}

				if (beforeReload != null) {
					beforeReload.accept(index, component);
				}

				((Reloadable) component).reload();
			}
		}
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		build();
	}

	@Override
	public void reload() {
		initOrReload(tabs.getSelectedIndex());
	}

	/**
	 * Selects the tab specified by the provided index
	 * 
	 * @param index the index
	 */
	public void selectTab(int index) {
		if (tabs != null) {
			tabs.setSelectedIndex(index);
		}
	}

	/**
	 * Sets the specified entity as the entity displayed in this component
	 * 
	 * @param entity
	 * @param preserveTab
	 */
	public void setEntity(T entity, boolean preserveTab) {
		this.entity = entity;
		if (!preserveTab) {
			selectTab(0);
		}

		reload();

		// update title
		if (caption != null) {
			caption.setText(titleCreator.get());
		}
	}

	/**
	 * Sets the visibility of a tab
	 * 
	 * @param index   the index of the tab
	 * @param visible the desired visibility
	 */
	public void setTabVisible(int index, boolean visible) {
		if (tabs.getTabByIndex(index) != null) {
			tabs.getTabByIndex(index).setVisible(visible);
		}
	}

	/**
	 * Sets up the various tabs
	 * 
	 * @param tabs the tab wrapper that holds the various tabs
	 */
	private void setupSheets(TabWrapper tabs) {

		// build up placeholder tabs that only contain an empty layout
		int index = 0;
		for (String cap : getCaptions()) {
			VerticalLayout dummy = new DefaultVerticalLayout(false, false);
			String description = descriptionCreator == null ? "" : descriptionCreator.apply(index);
			tabs.addTab(cap, description, dummy, iconCreator == null ? null : iconCreator.apply(index));
			index++;
		}

		// construct first tab
		Component component = tabCreator.apply(0);
		constructedTabs.add(0);
		tabs.setComponent(0, component);

		// respond to a tab change by actually loading the sheet
		tabs.addSelectedChangeListener(event -> initOrReload(event.getSource().getSelectedIndex()));
	}

}
