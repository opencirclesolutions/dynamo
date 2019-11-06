package com.ocs.dynamo.ui.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * A simple panel for adding a title bar and border around a layout
 * 
 * @author Bas Rutten
 *
 */
public class Panel extends VerticalLayout {

    private static final long serialVersionUID = -4620931565010614799L;

    private Text captionText;

    public Panel() {
        this("");
    }

    public Panel(String caption) {
        super();
        addClassName("dynamoPanel");
        VerticalLayout titleLayout = new VerticalLayout();
        captionText = new Text(caption);
        titleLayout.add(captionText);
        titleLayout.addClassName("dialogTitle");
        add(titleLayout);
    }

    public void setContent(Component component) {
        add(component);
    }
    
    public void setCaption(String caption) {
        captionText.setText(caption);
    }
}
