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

import org.apache.commons.lang3.StringUtils;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.Reloadable;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;

/**
 * A layout that contains a tab sheet with tabs that are lazily loaded. Use the
 * getTabCaptions method to specify the captions of the tabs, and (implicitly)
 * the number of tabs. Whenever the user selects a tab for the first time, the
 * initTab method is called in order to lazily construct the tab.
 * 
 * @author bas.rutten
 * @param <ID> type of the primary key
 * @param <T> type of the entity
 */
public abstract class LazyTabLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseCustomComponent
        implements Reloadable {

    private static final long serialVersionUID = 3788799136302802727L;

    /**
     * The caption to display above the tabs
     */
    private Label caption;

    /**
     * The indices of the tabs that have already been constructed
     */
    private Set<Integer> constructedTabs = new HashSet<>();

    /**
     * The entity that is being shown
     */
    private T entity;

    /**
     * The tab sheet component
     */
    private TabWrapper tabs;

    /**
     * Constructor
     * 
     * @param entity the entity to display
     */
    public LazyTabLayout(T entity) {
        this.entity = entity;
    }

    /**
     * Callback method that is called before a tab is reloaded - use this to make
     * sure the correct data is available in the tab
     * 
     * @param index     the index of the selected tab
     * @param component
     */
    protected void beforeReload(int index, Component component) {
        // overwrite in subclasses
    }

    @Override
    public void build() {
        if (tabs == null) {
            VerticalLayout main = new VerticalLayout();
            add(main);

            String title = createTitle();
            if (!StringUtils.isEmpty(title)) {
                caption = new Label(createTitle());
                main.add(caption);
            }

            tabs = new TabWrapper();
            tabs.setSizeFull();

            main.add(tabs);

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

    /**
     * Returns the tab identified by a certain index
     * 
     * @param index the index
     * @return
     */
    @Override
    public Component getComponentAt(int index) {
        return tabs.getComponentAt(index);
    }

    public T getEntity() {
        return entity;
    }

    protected abstract Icon getIconForTab(int index);

    /**
     * Returns the captions of the tabs
     * 
     * @return
     */
    protected abstract String[] getTabCaptions();

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
            Component constructed = initTab(index);
            tabs.setComponent(index, constructed);
        } else {
            // reload the tab if needed
            Component component = tabs.getComponentAt(index);
            if (component instanceof Reloadable) {
                if (component instanceof CanAssignEntity) {
                    ((CanAssignEntity<?, T>) component).assignEntity(getEntity());
                }
                beforeReload(index, component);
                ((Reloadable) component).reload();
            }
        }
    }

    /**
     * Lazily creates a certain tab
     * 
     * @param index the zero-based index of the tab to create
     * @return
     */
    protected abstract Component initTab(int index);

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
     * 
     * @param index
     */
    public void selectTab(int index) {
        tabs.setSelectedIndex(index);
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
            caption.setText(createTitle());
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
     * Constructs the lazy tab sheet by setting up empty dummy tabs
     * 
     * @param tabs the tab sheet
     */
    private void setupLazySheet(TabWrapper tabs) {

        // build up placeholder tabs that only contain an empty layout
        int index = 0;
        for (String caption : getTabCaptions()) {
            VerticalLayout dummy = new DefaultVerticalLayout(false, false);
            tabs.addTab(caption, dummy, getIconForTab(index++));
        }

        // construct first tab
        Component component = initTab(0);
        constructedTabs.add(0);
        tabs.setComponent(0, component);

        // respond to a tab change by actually loading the sheet
        tabs.addSelectedChangeListener(event -> initOrReload(event.getSource().getSelectedIndex()));
    }

    public Tab getTabByIndex(int index) {
        return tabs.getTabByIndex(index);
    }

}
