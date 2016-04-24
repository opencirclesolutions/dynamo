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
package com.ocs.dynamo.showcase;

import javax.inject.Inject;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.ContextLoaderListener;

import com.ocs.dynamo.ui.BaseUI;
import com.ocs.dynamo.ui.component.ErrorView;
import com.ocs.dynamo.ui.menu.MenuService;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.EnableVaadin;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.spring.server.SpringVaadinServlet;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Vaadin UI.
 */
@Theme("light")
@SpringUI()
@SuppressWarnings("serial")
@UIScope
public class DefaultUI extends BaseUI {

    private MenuBar menu;

    @Autowired
    private MenuService menuService;

    /**
     * 'Marker class' to initialize the Vaadin Web Context.
     */
    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DefaultUI.class)
    public static class Servlet extends SpringVaadinServlet {
    }

    /**
     * 'Marker class' to initialize the Spring Context.
     */
    @WebListener
    public static class MyContextLoaderListener extends ContextLoaderListener {
    }

    /**
     * 'Marker class' to setup the Spring Context within the Vaadin Web Context.
     */
    @EnableVaadin
    @Configuration
    public static class MyConfiguration {
    }

    @Inject
    private SpringViewProvider viewProvider;

    private Panel viewPanel;

    private Navigator navigator;

    /**
     * Main method - sets up the application
     */
    @Override
    protected void init(VaadinRequest request) {

        // Create the content root layout for the UI
        VerticalLayout content = new VerticalLayout();
        setContent(content);

        // navigator part
        VerticalLayout viewLayout = new VerticalLayout();
        viewPanel = new Panel();
        viewPanel.setImmediate(Boolean.TRUE);
        viewPanel.setContent(viewLayout);

        // create a state manager and set its default view
        // this is done to circumvent a bug with the view being created twice if
        // navigator.navigateTo is called directly
        Navigator.UriFragmentManager stateManager = new com.vaadin.navigator.Navigator.UriFragmentManager(
                this.getPage());
        stateManager.setState(Views.MOVIES_VIEW);

        // create the navigator
        navigator = new Navigator(this, stateManager,
                new Navigator.SingleComponentContainerViewDisplay(viewPanel));
        UI.getCurrent().setNavigator(navigator);
        navigator.addProvider(viewProvider);
        navigator.setErrorView(new ErrorView());

        menu = menuService.constructMenu("movies.menu", navigator);
        content.addComponent(menu);

        // Display the greeting
        content.addComponent(viewPanel);
    }
}
