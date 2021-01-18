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
package com.ocs.dynamo.ui.view;

import java.time.ZoneId;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.UIHelper;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.menu.MenuService;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveEvent.ContinueNavigationAction;
import com.vaadin.flow.router.BeforeLeaveObserver;

/**
 * A base class for Views. Provides easy access to the entity model factory and
 * the navigator
 * 
 * @author bas.rutten
 */
@Component
public abstract class BaseView extends VerticalLayout implements BeforeLeaveObserver, BeforeEnterObserver {

	public static final String SELECTED_ID = "selectedId";

	private static final long serialVersionUID = 8340448520371840427L;

	@Autowired
	private EntityModelFactory modelFactory;

	@Autowired
	private MessageService messageService;

	@Autowired
	private UIHelper uiHelper;

	@Autowired
	private MenuService menuService;

	/**
	 * Indicates whether the application must ask for confirmation before navigating
	 * to a different view
	 */
	private boolean confirmBeforeLeave;

	public BaseView() {
		this(false);
		setSpacing(false);
	}

	public BaseView(boolean confirmBeforeLeave) {
		this.confirmBeforeLeave = confirmBeforeLeave;
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		// store browser time zone in session
		if (SystemPropertyUtils.useBrowserTimezone()) {
			UI.getCurrent().getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
				String timeZoneId = extendedClientDetails.getTimeZoneId();
				VaadinUtils.storeTimeZone(ZoneId.of(timeZoneId));
			});
		}

		uiHelper.setSelectedView(event.getNavigationTarget());
		MenuBar menuBar = uiHelper.getMenuBar();
		if (menuBar != null) {
			String path = event.getLocation().getPath();
			menuService.setLastVisited(menuBar, path.replace('/', '#'));
		}
	}

	@Override
	public void beforeLeave(BeforeLeaveEvent event) {
		if (confirmBeforeLeave && isEditing()) {
			MenuBar menuBar = uiHelper.getMenuBar();
			String lastVisited = menuBar == null ? null : menuService.getLastVisited();

			ContinueNavigationAction postpone = event.postpone();
			VaadinUtils.showConfirmDialog(message("ocs.confirm.navigate"), () -> postpone.proceed(), () -> {
				if (lastVisited != null) {
					menuService.setLastVisited(menuBar, lastVisited);
				}
			});
		}
	}

	/**
	 * Performs the actual initialization
	 */
	protected abstract void doInit(VerticalLayout main);

	public MessageService getMessageService() {
		return messageService;
	}

	public EntityModelFactory getModelFactory() {
		return modelFactory;
	}

	public UIHelper getUiHelper() {
		return uiHelper;
	}

	@PostConstruct
	public final void init() {
		VerticalLayout main = initLayout();
		doInit(main);
	}

	/**
	 * Sets up the outermost layout
	 * 
	 * @return
	 */
	protected VerticalLayout initLayout() {
		VerticalLayout container = new DefaultVerticalLayout(true, false);
		container.addClassName(DynamoConstants.CSS_BASE_VIEW_PARENT);
		add(container);
		return container;
	}

	/**
	 * Callback method that is called when the user wants to navigate away from a
	 * page that is being edited
	 * 
	 * @return
	 */
	protected boolean isEditing() {
		return false;
	}

	/**
	 * Retrieves a message based on its key
	 * 
	 * @param key the key of the message
	 * @return
	 */
	protected String message(String key) {
		return messageService.getMessage(key, VaadinUtils.getLocale());
	}

	/**
	 * Retrieves a message based on its key
	 * 
	 * @param key  the key of the message
	 * @param args any arguments to pass to the message
	 * @return
	 */
	protected String message(String key, Object... args) {
		return messageService.getMessage(key, VaadinUtils.getLocale(), args);
	}

	/**
	 * Navigates to the selected view
	 * 
	 * @param viewId the ID of the desired view
	 */
	protected void navigate(String viewId) {
		UI.getCurrent().navigate(viewId);
	}

	/**
	 * Navigates to the specified view
	 * 
	 * @param viewId the ID of the view
	 * @param mode
	 */
	protected void navigate(String viewId, String mode) {
		UI.getCurrent().navigate(viewId + "/" + mode);
	}

}
