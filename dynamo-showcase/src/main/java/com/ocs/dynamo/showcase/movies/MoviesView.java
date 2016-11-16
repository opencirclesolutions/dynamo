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

import com.ocs.dynamo.showcase.Views;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.layout.SimpleSearchLayout;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.view.BaseView;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Layout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Dynamo contains a Abstract View class wich essentially is a Vaadin View Custom Component that has
 * access to the Dynamo Entity Model Factory and supports the Vaadin Navigation.
 */
@UIScope // To preserve UI scope place this annotation above @SpringView.
@SpringView(name = Views.MOVIES_VIEW)
@SuppressWarnings("serial")
public class MoviesView extends BaseView {

    /** Logger for {@link MoviesView}. */
    private static final Logger LOG = LoggerFactory.getLogger(MoviesView.class);

    /** The Movies View is using the {@link MovieService} for data access. */
    @Inject
    private MovieService movieService;

    /**
     * Construct view.
     */
    @PostConstruct
    void init() {
        LOG.debug("Initialize View - {}.", this.getClass().getSimpleName());

        Layout mainLayout = super.initLayout();

        // Set form options by convention.
        FormOptions fo = new FormOptions();

        // Add a remove button and edit button
        fo.setShowRemoveButton(true).setShowEditButton(true);

        // This is where the magic happens. The Simple Search layout uses the Dynamo Entity
        // Model Factory to define a Simple Search Screen with sorting, filtering and lazy loading
        // of data.
        SimpleSearchLayout<Integer, Movie> movieLayout = new SimpleSearchLayout<Integer, Movie>(
                movieService, getModelFactory().getModel(Movie.class), QueryType.ID_BASED, fo,
                new com.vaadin.data.sort.SortOrder("id", SortDirection.ASCENDING)) {
        };

        // Add layout.
        mainLayout.addComponent(movieLayout);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public void enter(ViewChangeEvent event) {
        LOG.debug("Update View - {}.", this.getClass().getSimpleName());
    }

}
