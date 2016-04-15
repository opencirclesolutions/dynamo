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
import com.ocs.dynamo.ui.BaseUI;
import com.vaadin.navigator.View;
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

    /**
     * Returns the current screen mode
     */
    protected String getScreenMode() {
        if (UI.getCurrent() instanceof BaseUI) {
            BaseUI b = (BaseUI) UI.getCurrent();
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
        UI.getCurrent().getNavigator().navigateTo(viewId);
    }

}
