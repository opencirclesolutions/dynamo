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
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.layout.ServiceBasedSplitLayout;
import com.ocs.dynamo.ui.view.BaseView;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.filter.SimpleStringFilter;
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
 * Master-detail horizontal split view of the Movies table.
 */
@UIScope // To preserve UI scope place this annotation above @SpringView.
@SpringView(name = Views.HORIZONTAL_MOVIES_SPLIT_VIEW)
@SuppressWarnings("serial")
public class HorizontalMoviesSplitView extends BaseView {

    /** Logger for {@link HorizontalMoviesSplitView}. */
    private static final Logger LOG = LoggerFactory.getLogger(HorizontalMoviesSplitView.class);

    /** The Horizontal Movies Split View is using the MovieService for data access. */
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
        fo.setOpenInViewMode(true);

        // Add a remove button.
        fo.setShowRemoveButton(true);

        // Add an edit button.
        fo.setShowEditButton(true);
        fo.setShowQuickSearchField(true);

        // A SplitLayout is a component that displays a search screen and an edit form
        ServiceBasedSplitLayout<Integer, Movie> movieLayout = new ServiceBasedSplitLayout<Integer, Movie>(
                movieService, getModelFactory().getModel(Movie.class), fo, new SortOrder("title",
                        SortDirection.ASCENDING)) {

            @Override
            protected Filter constructQuickSearchFilter(String value) {
                // quick search field
                return new SimpleStringFilter("title", value, true, false);
            }
        };

        // Add layout.
        mainLayout.addComponent(movieLayout);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    @Override
    public void enter(ViewChangeEvent event) {
        LOG.debug("Update View - {}.", this.getClass().getSimpleName());
    }
}
