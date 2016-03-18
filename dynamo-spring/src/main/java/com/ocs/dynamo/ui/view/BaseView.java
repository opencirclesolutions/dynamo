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

import com.ocs.dynamo.constants.OCSConstants;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.vaadin.navigator.View;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.UI;

/**
 * A base class for Views. Provides easy access to the entity model factory and the navigator
 * 
 * @author bas.rutten
 */
public abstract class BaseView extends CustomComponent implements View {

    public static final String SELECTED_ID = "selectedId";

    private static final long serialVersionUID = 8340448520371840427L;

    @Autowired
    private EntityModelFactory modelFactory;

    public EntityModelFactory getModelFactory() {
        return modelFactory;
    }

    protected String getScreenMode() {
        String mode = (String) VaadinSession.getCurrent().getAttribute(OCSConstants.SCREEN_MODE);
        VaadinSession.getCurrent().setAttribute(OCSConstants.SCREEN_MODE, null);
        return mode;
    }

    /**
     * Navigates to the selected view
     * 
     * @param viewId
     */
    protected void navigate(String viewId) {
        UI.getCurrent().getNavigator().navigateTo(viewId);
    }

    /**
     * Sets the ID of the selected object in the session, then navigates to the desired view
     * 
     * @param viewId
     * @param id
     */
    protected void selectAndNavigate(String viewId, Object id) {
        UI.getCurrent().getSession().setAttribute(SELECTED_ID, id);
        UI.getCurrent().getNavigator().navigateTo(viewId);
    }

}
