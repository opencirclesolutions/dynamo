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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;

import com.ocs.dynamo.ui.menu.MenuService;
import com.ocs.dynamo.ui.navigator.CustomNavigator;
import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
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

	private Map<Class<?>, Consumer<?>> entityOnViewMapping = new HashMap<>();

	@Autowired
	private MenuService menuService;

	private MenuBar menuBar;

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
	 *            indicates whether the view must always be reloaded (even when
	 *            navigating from the view to the same view)
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

	/**
	 * Adds a mapping for carrying out navigation within the application
	 * 
	 * @param entityClass
	 * @param navigateAction
	 */
	public void addEntityOnViewMapping(Class<?> entityClass, Consumer<?> navigateAction) {
		entityOnViewMapping.put(entityClass, navigateAction);
	}

	/**
	 * Navigate to a screen based on the actual type of parameter o. During
	 * initialisation of the UI of your project a mapping from type to consumer must
	 * have been provided by adding it through the method addEntityOnViewMapping.
	 * 
	 * @param o
	 *            The selected object to be displayed on the target screen.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void navigateToEntityScreenDirectly(Object o) {
		if (o != null) {
			Consumer navigateToView = entityOnViewMapping.getOrDefault(o.getClass(), err -> Notification
					.show("No view mapping registered for class: " + o.getClass(), Notification.Type.ERROR_MESSAGE));
			if (navigateToView != null) {
				try {
					navigateToView.accept(o);
				} catch (Exception e) {
					Notification.show("An exception occurred while executing the mapped action for class: "
							+ o.getClass() + " with message: " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
					throw e;
				}
			}
		}
	}

	public void navigate(String viewName) {
		navigator.navigateTo(viewName);
		if (menuBar != null) {
			menuService.setLastVisited(menuBar, viewName);
		}
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public void setMenuBar(MenuBar menuBar) {
		this.menuBar = menuBar;
	}

}
