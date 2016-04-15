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
package com.ocs.dynamo.showcase.movies;

import javax.inject.Inject;

import com.ocs.dynamo.showcase.Views;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.layout.SimpleSearchLayout;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.view.BaseView;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.VerticalLayout;

/**
 * Dynamo contains a Abstract View class wich essentially is a Vaadin View Custom Component that has
 * access to the Dynamo Entity Model Factory and supports the Vaadin Navigation.
 */
@SpringView(name = Views.MOVIES_VIEW)
@UIScope
@SuppressWarnings("serial")
public class MoviesView extends BaseView {

    /** Vaadin vertical layout. */
    private VerticalLayout mainLayout;

    /** The Movies View is using the MovieService for data access. */
    @Inject
    private MovieService movieService;

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public void enter(ViewChangeEvent event) {

        // Apply Vaadin Layout.
        mainLayout = new DefaultVerticalLayout(true, true);

        // Set form options by convention.
        FormOptions fo = new FormOptions();

        // Add a remove button.
        fo.setShowRemoveButton(true);

        // Add an edit button.
        fo.setShowEditButton(true);

        // This is where the magic happens. The Simple Search layout uses the Dynamo Entity
        // Model Factory to define a Simple Search Screen with sorting, filtering and lazy loading
        // of data.
        SimpleSearchLayout<Integer, Movie> movieLayout = new SimpleSearchLayout<Integer, Movie>(
                movieService, getModelFactory().getModel(Movie.class), QueryType.ID_BASED, fo,
                new com.vaadin.data.sort.SortOrder("id", SortDirection.ASCENDING)) {
        };

        // Some plumbing.
        mainLayout.addComponent(movieLayout);
        setCompositionRoot(mainLayout);
    }
}
