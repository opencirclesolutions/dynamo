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

import org.springframework.util.StringUtils;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.Reloadable;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

/**
 * A layout that contains a tab sheet with tabs that are lazily loaded. Use the getTabCaptions method
 * to specify the captions of the tabs, and (implicitly) the number of tabs. Whenever the user selects
 * a tab for the first time, the initTab method is called in order to lazily construct the tab.
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
public abstract class LazyTabLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseCustomComponent
		implements Reloadable {

	private static final long serialVersionUID = 3788799136302802727L;

	/**
	 * The captions of the tabs that have already been constructed
	 */
	private Set<String> constructedTabs = new HashSet<>();

	/**
	 * The entity that is being shown
	 */
	private T entity;

	/**
	 * The panel that surrounds the tab sheet
	 */
	private Panel panel;

	/**
	 * The tab sheet component
	 */
	private TabSheet tabs;

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
	 * Callback method that is called before a tab is reloaded - use this to make
	 * sure the correct data is available in the tab
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
			tabs = new TabSheet();
			tabs.setSizeFull();

			String title = createTitle();
			if (!StringUtils.isEmpty(title)) {
				panel = new Panel();
				panel.setCaptionAsHtml(true);
				panel.setCaption(createTitle());

				VerticalLayout main = new DefaultVerticalLayout(true, true);
				panel.setContent(main);
				main.addComponent(tabs);
				setCompositionRoot(panel);
			} else {
				setCompositionRoot(tabs);
			}

			setupLazySheet(tabs);

		}
	}

	/**
	 * Constructs the title of the page. If this method returns null then no title
	 * will be displayed
	 * 
	 * @return
	 */
	protected abstract String createTitle();

	public T getEntity() {
		return entity;
	}

	/**
	 * Returns the icon to use inside a certain tab
	 * 
	 * @param index
	 *            the zero-based index of the tab
	 * @return
	 */
	protected abstract Resource getIconForTab(int index);

	/**
	 * Returns the tab identified by a certain index
	 * 
	 * @param index
	 *            the index
	 * @return
	 */
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
	 * Returns the description (tool tip) for a certain tab
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
	protected void initOrReload(Component selectedTab) {
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

	protected Tab getTab(Component selectedTab) {
		Tab tab = tabs.getTab(selectedTab);
		return tab;
	}

	/**
	 * Lazily creates a certain tab
	 * 
	 * @param index
	 *            the zero-based index of the tab to create
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
		if (panel != null) {
			panel.setCaption(createTitle());
		}
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
			t.setIcon(getIconForTab(i));
			t.setDescription(getTabDescription(i++));
		}

		// load the first tab
		((Layout) tabs.getTab(0).getComponent()).addComponent(initTab(0));
		constructedTabs.add(tabs.getTab(0).getCaption());

		// respond to a tab change by actually loading the sheet
		tabs.addSelectedTabChangeListener(event -> {
			Component component = event.getTabSheet().getSelectedTab();
			initOrReload(component);
		});
	}
}
