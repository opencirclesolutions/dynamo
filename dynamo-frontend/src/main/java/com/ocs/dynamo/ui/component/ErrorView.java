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
package com.ocs.dynamo.ui.component;

import com.ocs.dynamo.ui.composite.layout.BaseCustomComponent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;


/**
 * A view that is displayed when the user navigates to a non-existing view or a view for which the
 * user is not authorized
 * 
 * @author bas.rutten
 */
public class ErrorView extends BaseCustomComponent implements View {

    private static final long serialVersionUID = 3955677765990706688L;

    @Override
    public void enter(ViewChangeEvent event) {
        build();
    }

    @Override
    public void build() {
        VerticalLayout main = new DefaultVerticalLayout(true, true);

        Panel panel = new Panel();
        panel.setCaption(message("ocs.error.occurred"));
        main.addComponent(panel);

        VerticalLayout inside = new DefaultVerticalLayout(true, true);
        panel.setContent(inside);

        Label errorLabel = new Label(message("ocs.view.unknown"));
        inside.addComponent(errorLabel);

        setCompositionRoot(main);
    }
}
