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
package com.ocs.dynamo.ui.menu;

import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.UIHelper;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.menubar.MenuBar;

/**
 * Command for navigating to a certain view
 * 
 * @author bas.rutten
 */
public class NavigateCommand implements ComponentEventListener<ClickEvent<MenuItem>> {

    private static final long serialVersionUID = 5192333331107840255L;

    private MenuBar menuBar;

    private final String destination;

    private final String selectedTab;

    private final String mode;

    /**
     * Constructor
     * 
     * @param navigator   the Vaadin navigator
     * @param destination the destination to navigate to
     * @param selectedTab the index of the tab to select
     * @param mode        an optional screen mode
     */
    public NavigateCommand(MenuBar menuBar, String destination, String selectedTab, String mode) {
        this.menuBar = menuBar;
        // this.navigator = navigator;
        this.destination = destination;
        this.selectedTab = selectedTab;
        this.mode = mode;
    }

    public String getDestination() {
        return destination;
    }

    public String getSelectedTab() {
        return selectedTab;
    }

    public String getMode() {
        return mode;
    }

    @Override
    public void onComponentEvent(ClickEvent<MenuItem> event) {
        UIHelper ui = ServiceLocatorFactory.getServiceLocator().getService(UIHelper.class);

        if (selectedTab != null) {
            ui.setSelectedTab(Integer.valueOf(selectedTab));
        } else {
            ui.setSelectedTab(null);
        }
        ui.setScreenMode(mode);

        // reset style names
        for (MenuItem item : menuBar.getItems()) {
            // item.setStyleName(null);
        }

        // mark top level menu
        MenuItem selectedItem = event.getSource();
//        while (selectedItem.getParent() != null) {
//            selectedItem = (MenuItem) selectedItem.getParent().orElse(null);
//        }
        // selectedItem.setStyleName(DynamoConstants.CSS_LAST_VISITED);
        UI.getCurrent().navigate(destination);
    }

}
