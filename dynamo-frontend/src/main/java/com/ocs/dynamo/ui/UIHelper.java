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

import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;

/**
 * Helper class for the UI, mainly concerned with handling navigation and
 * storing state
 * 
 * @author Bas Rutten
 *
 */
public class UIHelper {

	/**
	 * Mapping for navigating to pages after clicking on a link
	 */
	private Map<Class<?>, Consumer<?>> entityOnViewMapping = new HashMap<>();

	private MenuBar menuBar;

	private String screenMode;

	private Object selectedEntity;

	private Integer selectedTab;

	/**
	 * The UI that is tied to this UIHelper
	 */
	private Object currentUI;

	/**
	 * Adds a mapping for carrying out navigation within the application
	 * 
	 * @param entityClass    the type of the entity
	 * @param navigateAction the action to carry out
	 */
	public void addEntityNavigationMapping(Class<?> entityClass, Consumer<?> navigateAction) {
		entityOnViewMapping.put(entityClass, navigateAction);
	}

	public void clearScreenMode() {
		this.screenMode = null;
	}

	/**
	 * Clears the session state
	 */
	public void clearState() {
		this.screenMode = null;
		setSelectedEntity(null);
		setSelectedTab(null);
	}

	public Object getCurrentUI() {
		return currentUI;
	}

	@SuppressWarnings("unchecked")
	public <T> T getCurrentUI(Class<T> clazz) {
		return (T) currentUI;
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public String getScreenMode() {
		return screenMode;
	}

	public Object getSelectedEntity() {
		return selectedEntity;
	}

	public Integer getSelectedTab() {
		return selectedTab;
	}

	/**
	 * Navigates to a view
	 * 
	 * @param viewName the name of the view to navigate to
	 */
	public void navigate(String viewName) {
		UI.getCurrent().navigate(viewName);
	}

	/**
	 * Navigates to the screen and opens it in the desired mode
	 * 
	 * @param viewName the name of the view
	 * @param mode     the mode
	 */
	public void navigate(String viewName, String mode) {
		this.screenMode = mode;
		UI.getCurrent().navigate(viewName + "/" + mode);
	}

	/**
	 * Navigate to a screen based on the actual type of parameter o. During
	 * initialization of the UI of your project a mapping from type to consumer must
	 * have been provided by adding it through the method addEntityOnViewMapping.
	 * 
	 * @param o The selected object to be displayed on the target screen.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void navigateToEntityScreen(Object o) {
		if (o != null) {
			Consumer navigateToView = entityOnViewMapping.getOrDefault(o.getClass(),
					err -> VaadinUtils.showNotification("No view mapping registered for class: " + o.getClass(),
							Position.MIDDLE, NotificationVariant.LUMO_ERROR));
			if (navigateToView != null) {
				try {
					navigateToView.accept(o);
				} catch (Exception e) {
					VaadinUtils.showNotification(
							"An exception occurred while executing the mapped action for class: " + o.getClass()
									+ " with message: " + e.getMessage(),
							Position.MIDDLE, NotificationVariant.LUMO_ERROR);
					throw e;
				}
			}
		}
	}

	public void selectAndNavigate(Object selectedEntity, String viewName) {
		setSelectedEntity(selectedEntity);
		navigate(viewName);
	}

	public void setCurrentUI(Object currentUI) {
		this.currentUI = currentUI;
	}

	public void setMenuBar(MenuBar menuBar) {
		this.menuBar = menuBar;
	}

	public void setSelectedEntity(Object selectedEntity) {
		this.selectedEntity = selectedEntity;
	}

	public void setSelectedTab(Integer selectedTab) {
		this.selectedTab = selectedTab;
	}

}
