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
package com.ocs.dynamo.ui;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.ui.SingleComponentContainer;
import com.vaadin.ui.UI;

public abstract class BaseUI extends UI {

    private static final long serialVersionUID = 5903140845804805314L;

    /**
     * Index of the tab to select directly after opening a screen
     */
    private Integer selectedTab;

    /**
     * A string describing the desired screen mode to set after opening a screen
     */
    private String screenMode;

    /**
     * The navigator
     */
    private Navigator navigator;

    public Integer getSelectedTab() {
        return selectedTab;
    }

    public void setSelectedTab(Integer selectedTab) {
        this.selectedTab = selectedTab;
    }

    public String getScreenMode() {
        return screenMode;
    }

    public void setScreenMode(String screenMode) {
        this.screenMode = screenMode;
    }

    /**
     * Initializes the startup view
     * 
     * @param startView
     */
    protected void initNavigation(ViewProvider viewProvider, SingleComponentContainer container, String startView) {
        // create a state manager and set its default view
        // this is done to circumvent a bug with the view being created
        // twice if
        // navigator.navigateTo is called directly
        Navigator.UriFragmentManager stateManager = new com.vaadin.navigator.Navigator.UriFragmentManager(
                this.getPage());
        stateManager.setState(startView);

        // create the navigator
        navigator = new Navigator(this, stateManager,
                new Navigator.SingleComponentContainerViewDisplay(container));
        UI.getCurrent().setNavigator(navigator);
        navigator.addProvider(viewProvider);
        //navigator.setErrorView(new ErrorView());
    }

    public Navigator getNavigator() {
        return navigator;
    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

}
