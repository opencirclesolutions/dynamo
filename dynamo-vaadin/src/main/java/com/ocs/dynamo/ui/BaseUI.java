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

import com.ocs.dynamo.ui.navigator.CustomNavigator;
import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.ui.SingleComponentContainer;
import com.vaadin.ui.UI;

@Widgetset(value = "com.ocs.dynamo.DynamoWidgetSet")
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
	private CustomNavigator navigator;

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
	 * @param alwaysReload
	 *            indicates whether the view must always be reloaded (even when navigating from the
	 *            view to the same view)
	 */
	protected void initNavigation(ViewProvider viewProvider, SingleComponentContainer container, String startView,
	        boolean alwaysReload) {

		// create the navigator
		navigator = new CustomNavigator(this, new Navigator.SingleComponentContainerViewDisplay(container));
		navigator.setAlwaysReload(alwaysReload);

		UI.getCurrent().setNavigator(navigator);
		navigator.addProvider(viewProvider);
		navigator.navigateTo(startView);
	}

	@Override
	public CustomNavigator getNavigator() {
		return navigator;
	}

	public void setNavigator(CustomNavigator navigator) {
		this.navigator = navigator;
	}

}
