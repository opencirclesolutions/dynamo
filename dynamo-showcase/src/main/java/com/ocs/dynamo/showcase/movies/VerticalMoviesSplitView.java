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
import com.ocs.dynamo.ui.composite.layout.ServiceBasedSplitLayout;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.ocs.dynamo.ui.view.BaseView;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.VerticalLayout;

@SpringView(name = Views.VERTICAL_MOVIES_SPLIT_VIEW)
@UIScope
@SuppressWarnings("serial")
public class VerticalMoviesSplitView extends BaseView {

    /** The Movies View is using the MovieService for data access. */
    @Inject
    private MovieService movieService;

    @Override
    public void enter(ViewChangeEvent event) {

        // Apply Vaadin Layout.
        VerticalLayout mainLayout = new DefaultVerticalLayout(true, true);

        // Set form options by convention.
        FormOptions fo = new FormOptions().setOpenInViewMode(true).setShowRemoveButton(true).setEditAllowed(true)
                .setShowQuickSearchField(true).setScreenMode(ScreenMode.VERTICAL);

        // A SplitLayout is a component that displays a search screen and an edit form
        ServiceBasedSplitLayout<Integer, Movie> movieLayout = new ServiceBasedSplitLayout<Integer, Movie>(movieService,
                getModelFactory().getModel(Movie.class), fo, new SortOrder("title", SortDirection.ASCENDING)) {

            @Override
            protected Filter constructQuickSearchFilter(String value) {
                // quick search field
                return new SimpleStringFilter("title", value, true, false);
            }
        };
        movieLayout.setPageLength(10);

        // Some plumbing.
        mainLayout.addComponent(movieLayout);
        setCompositionRoot(mainLayout);
    }
}
