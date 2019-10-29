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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.UIHelper;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * A base class for Views. Provides easy access to the entity model factory and
 * the navigator
 * 
 * @author bas.rutten
 */
@Component
public abstract class BaseView extends VerticalLayout {

    public static final String SELECTED_ID = "selectedId";

    private static final long serialVersionUID = 8340448520371840427L;

    @Autowired
    private EntityModelFactory modelFactory;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UIHelper uiHelper;

    @PostConstruct
    public final void init() {
        doInit();
    }

    protected abstract void doInit();

    /**
     * Clears the current screen mode
     */
    protected void clearScreenMode() {
        uiHelper.setScreenMode(null);
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public EntityModelFactory getModelFactory() {
        return modelFactory;
    }

    /**
     * Returns the current screen mode
     */
    protected String getScreenMode() {
        return uiHelper.getScreenMode();
    }

    /**
     * Sets up the outermost layout
     * 
     * @return
     */
    protected VerticalLayout initLayout() {
        VerticalLayout container = new DefaultVerticalLayout(false, true);
        add(container);
        return container;
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

    public UIHelper getUiHelper() {
        return uiHelper;
    }

}
