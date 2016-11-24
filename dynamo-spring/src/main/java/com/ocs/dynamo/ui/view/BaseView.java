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

import org.springframework.beans.factory.annotation.Autowired;

import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.BaseUI;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.vaadin.navigator.View;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * A base class for Views. Provides easy access to the entity model factory and the navigator
 * 
 * @author bas.rutten
 */
public abstract class BaseView extends CustomComponent implements View {

	public static final String SELECTED_ID = "selectedId";

	private static final long serialVersionUID = 8340448520371840427L;

	private UI ui = UI.getCurrent();

	@Autowired
	private EntityModelFactory modelFactory;

	@Autowired
	private MessageService messageService;

	public EntityModelFactory getModelFactory() {
		return modelFactory;
	}

	/**
	 * Returns the current screen mode
	 */
	protected String getScreenMode() {
		if (ui instanceof BaseUI) {
			BaseUI b = (BaseUI) ui;
			return b.getScreenMode();
		}
		return null;
	}

	/**
	 * Navigates to the selected view
	 * 
	 * @param viewId
	 *            the ID of the desired view
	 */
	protected void navigate(String viewId) {
		ui.getNavigator().navigateTo(viewId);
	}

	/**
	 * Retrieves a message based on its key
	 * 
	 * @param key
	 *            the key of the message
	 * @return
	 */
	protected String message(String key) {
		return messageService.getMessage(key);
	}

	/**
	 * Retrieves a message based on its key
	 * 
	 * @param key
	 *            the key of the message
	 * @param args
	 *            any arguments to pass to the message
	 * @return
	 */
	protected String message(String key, Object... args) {
		return messageService.getMessage(key, args);
	}

	/**
	 * Sets up the outermost layout
	 * 
	 * @return
	 */
	protected Layout initLayout() {
		VerticalLayout container = new DefaultVerticalLayout(true, true);
		setCompositionRoot(container);
		return container;
	}

	public MessageService getMessageService() {
		return messageService;
	}

}
