///*
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// */
//package com.ocs.dynamo.ui.navigator;
//
//import com.vaadin.navigator.NavigationStateManager;
//import com.vaadin.navigator.Navigator;
//import com.vaadin.navigator.View;
//import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
//import com.vaadin.navigator.ViewDisplay;
//import com.vaadin.navigator.ViewProvider;
//import com.vaadin.shared.util.SharedUtil;
//import com.vaadin.ui.ComponentContainer;
//import com.vaadin.ui.SingleComponentContainer;
//import com.vaadin.ui.UI;
//
///**
// * Override of the Vaadin navigator class, for making sure that a view is
// * reloaded when the user navigates to it and it already is the currently active
// * view
// * 
// * 
// * @author bas.rutten
// *
// */
//public class CustomNavigator extends Navigator {
//
//	private static final long serialVersionUID = 4919429256404050039L;
//
//	private String currentNavigationState = null;
//
//	/**
//	 * The error provider
//	 */
//	private ViewProvider errorProvider;
//
//	/**
//	 * Whether to always reload the current view (even when navigating from a view
//	 * to the same view)
//	 */
//	private boolean alwaysReload = true;
//
//	protected CustomNavigator() {
//	}
//
//	public CustomNavigator(UI ui, ComponentContainer container) {
//		this(ui, new ComponentContainerViewDisplay(container));
//	}
//
//	public CustomNavigator(UI ui, NavigationStateManager stateManager, ViewDisplay display) {
//		init(ui, stateManager, display);
//	}
//
//	public CustomNavigator(UI ui, SingleComponentContainer container) {
//		this(ui, new SingleComponentContainerViewDisplay(container));
//	}
//
//	public CustomNavigator(UI ui, ViewDisplay display) {
//		this(ui, new UriFragmentManager(ui.getPage()), display);
//	}
//
//	public boolean isAlwaysReload() {
//		return alwaysReload;
//	}
//
//	/**
//	 * Navigate to a certain view
//	 * 
//	 * @param navigationState the name of the view
//	 */
//	@Override
//	public void navigateTo(String navigationState) {
//		ViewProvider longestViewNameProvider = getViewProvider(navigationState);
//		String longestViewName = longestViewNameProvider == null ? null
//				: longestViewNameProvider.getViewName(navigationState);
//		View viewWithLongestName = null;
//
//		if (longestViewName != null) {
//			viewWithLongestName = longestViewNameProvider.getView(longestViewName);
//		}
//
//		if (viewWithLongestName == null && errorProvider != null) {
//			longestViewName = errorProvider.getViewName(navigationState);
//			viewWithLongestName = errorProvider.getView(longestViewName);
//		}
//
//		if (viewWithLongestName == null) {
//			throw new IllegalArgumentException("Trying to navigate to an unknown state '" + navigationState
//					+ "' and an error view provider not present");
//		}
//
//		String parameters = "";
//		if (navigationState.length() > longestViewName.length() + 1) {
//			parameters = navigationState.substring(longestViewName.length() + 1);
//		} else if (navigationState.endsWith("/")) {
//			navigationState = navigationState.substring(0, navigationState.length() - 1);
//		}
//
//		if (isAlwaysReload() || getCurrentView() == null || !SharedUtil.equals(getCurrentView(), viewWithLongestName)
//				|| !SharedUtil.equals(currentNavigationState, navigationState)) {
//			navigateTo(viewWithLongestName, longestViewName, parameters);
//		} else {
//			updateNavigationState(
//					new ViewChangeEvent(this, getCurrentView(), viewWithLongestName, longestViewName, parameters));
//		}
//	}
//
//	protected void revertNavigation() {
//		if (currentNavigationState != null) {
//			getStateManager().setState(currentNavigationState);
//		}
//	}
//
//	public void setAlwaysReload(boolean alwaysReload) {
//		this.alwaysReload = alwaysReload;
//	}
//
//	@Override
//	public void setErrorProvider(ViewProvider provider) {
//		errorProvider = provider;
//	}
//
//	protected void updateNavigationState(ViewChangeEvent event) {
//		String viewName = event.getViewName();
//		String parameters = event.getParameters();
//		if (null != viewName && getStateManager() != null) {
//			String navigationState = viewName;
//			if (!parameters.isEmpty()) {
//				navigationState += "/" + parameters;
//			}
//			if (!navigationState.equals(getStateManager().getState())) {
//				getStateManager().setState(navigationState);
//			}
//			currentNavigationState = navigationState;
//		}
//	}
//
//}
