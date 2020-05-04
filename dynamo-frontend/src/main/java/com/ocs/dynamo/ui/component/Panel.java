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

import com.ocs.dynamo.constants.DynamoConstants;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * A simple panel for adding a title bar and border around a layout
 * 
 * @author Bas Rutten
 *
 */
public class Panel extends DefaultVerticalLayout {

    private static final long serialVersionUID = -4620931565010614799L;

    private Text captionText;

    public Panel() {
        this("");
    }

    public Panel(String caption) {
        super();
        setPadding(false);
        setMargin(false);
        
        addClassName(DynamoConstants.CSS_PANEL);
        VerticalLayout titleLayout = new DefaultVerticalLayout(false, false);
        titleLayout.setPadding(true);
        captionText = new Text(caption);
        titleLayout.add(captionText);
        titleLayout.addClassName(DynamoConstants.CSS_PANEL_TITLE);
        add(titleLayout);
    }

    public void setContent(Component component) {
        add(component);
    }
    
    public void setCaption(String caption) {
        captionText.setText(caption);
    }
}
