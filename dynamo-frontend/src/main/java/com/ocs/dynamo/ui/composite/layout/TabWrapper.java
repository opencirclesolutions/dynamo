package com.ocs.dynamo.ui.composite.layout;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
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
     * The layout that serves as the container for the currently selected tab
     */
    private VerticalLayout displayedPage;

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
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            if (selectedPage != null) {
                displayedPage.removeAll();
                displayedPage.add(selectedPage);
            }
        });
        add(tabs);
        displayedPage = new VerticalLayout();
        add(displayedPage);
    }

    /**
     * Adds a selection change listener
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
    public Tab addTab(String caption, Component component, Icon icon) {
        Button button = new Button(caption);

        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        if (icon != null) {
            button.setIcon(icon);
        }
        Tab tab = new Tab(button);

        tabsToPages.put(tab, component);
        tabs.add(tab);
        if (displayedPage.getChildren().count() == 0L) {
            displayedPage.add(component);
        }

        return tab;
    }

    @Override
    public Component getComponentAt(int index) {
        Tab tab = getTabByIndex(index);
        return tabsToPages.get(tab);
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

        Tab tab = getTabByIndex(index);
        tabsToPages.put(tab, component);

        // show just this tab
        if (component != null) {
            displayedPage.removeAll();
            displayedPage.add(component);
        }
    }

    public void setSelectedIndex(int selectedIndex) {
        tabs.setSelectedIndex(selectedIndex);
    }
}
