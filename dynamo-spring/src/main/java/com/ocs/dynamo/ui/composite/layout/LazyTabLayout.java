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

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.Reloadable;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

/**
 * A layout that contains a tab sheet with tabs that are lazily loaded
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
@SuppressWarnings("serial")
public abstract class LazyTabLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseCustomComponent
        implements Reloadable {

	private static final long serialVersionUID = 3788799136302802727L;

	/**
	 * The entity that is being shown
	 */
	private T entity;

	/**
	 * The captions of the tabs that have already been constructed
	 */
	private Set<String> constructedTabs = new HashSet<>();

	/**
	 * The tab sheet component
	 */
	private TabSheet tabs;

	/**
	 * The panel that surrounds the tab sheet
	 */
	private Panel panel;

	/**
	 * Constructor
	 * 
	 * @param entity
	 */
	public LazyTabLayout(T entity) {
		this.entity = entity;
	}

	@Override
	public void attach() {
		super.attach();
		build();
	}

	/**
	 * Callback method that is called before a tab is reloaded - use this to make sure the correct
	 * data is available in the tab
	 * 
	 * @param index
	 *            the index of the selected tab
	 * @param component
	 */
	protected void beforeReload(int index, Component component) {
		// overwrite in subclasses
	}

	@Override
	public void build() {
		if (tabs == null) {
			panel = new Panel();
			panel.setCaptionAsHtml(true);
			panel.setCaption(createTitle());

			VerticalLayout main = new DefaultVerticalLayout(true, true);
			panel.setContent(main);

			tabs = new TabSheet();
			tabs.setSizeFull();

			main.addComponent(tabs);
			setupLazySheet(tabs);
			setCompositionRoot(panel);
		}
	}

	/**
	 * Constructs the title of the page
	 * 
	 * @return
	 */
	protected abstract String createTitle();

	public T getEntity() {
		return entity;
	}

	public Tab getTab(int index) {
		return tabs.getTab(index);
	}

	/**
	 * Returns the captions of the tabs
	 * 
	 * @return
	 */
	protected abstract String[] getTabCaptions();

	/**
	 * Returns the description (tooltip) for a certain tab
	 * 
	 * @param index
	 *            the index of the tab
	 * @return
	 */
	protected String getTabDescription(int index) {
		return null;
	}

	/**
	 * Returns the index of a tab given its caption
	 * 
	 * @param caption
	 *            the caption
	 * @return
	 */
	private int getTabIndex(String caption) {
		int index = 0;
		for (int i = 0; i < tabs.getComponentCount(); i++) {
			Tab t = tabs.getTab(i);
			if (t.getCaption().equals(caption)) {
				index = i;
				break;
			}
		}
		return index;
	}

	/**
	 * Constructs or reloads a tab
	 * 
	 * @param selectedTab
	 *            the currently selected tab
	 */
	@SuppressWarnings("unchecked")
	private void initOrReload(Component selectedTab) {
		Tab tab = tabs.getTab(selectedTab);

		// lazily load a tab
		if (!constructedTabs.contains(tab.getCaption())) {
			constructedTabs.add(tab.getCaption());

			// look up the tab in the copies
			int index = getTabIndex(tab.getCaption());

			// paste the real tab into the placeholder
			Component realTab = initTab(index);
			((Layout) selectedTab).addComponent(realTab);
		} else {
			// reload the tab if needed
			Layout layout = (Layout) selectedTab;
			Component next = layout.iterator().next();
			if (next instanceof Reloadable) {

				if (next instanceof CanAssignEntity) {
					((CanAssignEntity<?, T>) next).assignEntity(getEntity());
				}
				beforeReload(getTabIndex(tab.getCaption()), next);
				((Reloadable) next).reload();
			}
		}
	}

	/**
	 * Lazily creates a certain tab
	 * 
	 * @param index
	 *            the index of the tab to create
	 * @return
	 */
	protected abstract Component initTab(int index);

	@Override
	public void reload() {
		initOrReload(tabs.getSelectedTab());
	}

	public void selectTab(int index) {
		tabs.setSelectedTab(index);
	}

	public void setEntity(T entity) {
		this.entity = entity;
		this.selectTab(0);
		panel.setCaption(createTitle());
	}

	/**
	 * Constructs the lazy tab sheet by setting up empty dummy tabs
	 * 
	 * @param tabs
	 *            the tab sheet
	 */
	private void setupLazySheet(final TabSheet tabs) {

		// build up placeholder tabs that only contain an empty layout
		int i = 0;
		for (String caption : getTabCaptions()) {
			Tab t = tabs.addTab(new DefaultVerticalLayout(false, false), caption);
			t.setDescription(getTabDescription(i++));
		}

		// load the first tab
		((Layout) tabs.getTab(0).getComponent()).addComponent(initTab(0));
		constructedTabs.add(tabs.getTab(0).getCaption());

		// respond to a tab change by actually loading the sheet
		tabs.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				Component component = event.getTabSheet().getSelectedTab();
				initOrReload(component);
			}
		});
	}
}
