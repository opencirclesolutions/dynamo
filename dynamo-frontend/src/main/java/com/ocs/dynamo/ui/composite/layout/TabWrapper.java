package com.ocs.dynamo.ui.composite.layout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.Tabs.SelectedChangeEvent;
import com.vaadin.flow.shared.Registration;

/**
 * A simple wrapper component around the Vaadin Flow tab sheet that displays a
 * component when a tab sheet is clicked
 * 
 * @author Bas Rutten
 *
 */
public class TabWrapper extends VerticalLayout {

    private static final long serialVersionUID = -3247803933862947954L;

    /**
     * The components that are currently being shown
     */
    private Set<Component> shown = new HashSet<>();

    /**
     * The tab component
     */
    private Tabs tabs;

    /**
     * Mapping between tab and selected page
     */
    private Map<Tab, Component> tabsToPages = new HashMap<>();

    public TabWrapper() {
        tabs = new Tabs();
        tabs.addSelectedChangeListener(event -> {
            shown.forEach(page -> page.setVisible(false));
            shown.clear();
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            if (selectedPage != null) {
                selectedPage.setVisible(true);
                shown.add(selectedPage);
            }
        });
        add(tabs);
    }

    /**
     * 
     * @param listener
     * @return
     */
    public Registration addSelectedChangeListener(ComponentEventListener<SelectedChangeEvent> listener) {
        return tabs.addSelectedChangeListener(listener);
    }

    /**
     * Adds a new tab
     * 
     * @param caption
     * @param component
     * @return
     */
    public Tab addTab(String label, Component component) {
        Tab tab = new Tab(label);
        tabsToPages.put(tab, component);
        tabs.add(tab);
        add(component);
        return tab;
    }

    public int getSelectedIndex() {
        return tabs.getSelectedIndex();
    }

    public Tab getTabByIndex(int index) {
        return (Tab) tabs.getComponentAt(index);
    }

    /**
     * Replaces the component at the specified index by the specified component
     * 
     * @param index     the index
     * @param component the component
     */
    public void setComponent(int index, Component component) {

        // hide all tabs
        shown.forEach(page -> page.setVisible(false));
        shown.clear();

        Tab tab = getTabByIndex(index);
        Component old = tabsToPages.get(tab);
        tabsToPages.put(tab, component);
        replace(old, component);

        // show just this tab
        if (component != null) {
            component.setVisible(true);
            shown.add(component);
        }
    }

    public void setSelectedIndex(int selectedIndex) {
        tabs.setSelectedIndex(selectedIndex);
    }
}
