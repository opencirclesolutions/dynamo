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

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.ui.BaseUI;
import com.vaadin.navigator.Navigator;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;

/**
 * Command for navigating to a certain view
 * 
 * @author bas.rutten
 */
public class NavigateCommand implements Command {

	private static final long serialVersionUID = 5192333331107840255L;

	private final Navigator navigator;

	private MenuBar menuBar;

	private final String destination;

	private final String selectedTab;

	private final String mode;

	/**
	 * Constructor
	 * 
	 * @param navigator
	 *            the Vaadin navigator
	 * @param destination
	 *            the destination to navigate to
	 * @param selectedTab
	 *            the index of the tab to select
	 * @param mode
	 *            an optional screen mode
	 */
	public NavigateCommand(Navigator navigator, MenuBar menuBar, String destination, String selectedTab, String mode) {
		this.menuBar = menuBar;
		this.navigator = navigator;
		this.destination = destination;
		this.selectedTab = selectedTab;
		this.mode = mode;
	}

	@Override
	public void menuSelected(MenuItem selectedItem) {
		UI ui = UI.getCurrent();
		if (ui instanceof BaseUI) {
			BaseUI b = (BaseUI) ui;

			if (selectedTab != null) {
				b.setSelectedTab(Integer.valueOf(selectedTab));
			} else {
				b.setSelectedTab(null);
			}
			b.setScreenMode(mode);
		}

		// reset style names
		for (MenuItem item : menuBar.getItems()) {
			item.setStyleName(null);
		}

		// mark top level menu
		while (selectedItem.getParent() != null) {
			selectedItem = selectedItem.getParent();
		}
		selectedItem.setStyleName(DynamoConstants.CSS_LAST_VISITED);

		navigator.navigateTo(destination);
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

}
