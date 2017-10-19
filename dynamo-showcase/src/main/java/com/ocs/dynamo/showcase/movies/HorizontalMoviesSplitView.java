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
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.layout.ServiceBasedSplitLayout;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.view.BaseView;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.VerticalLayout;

@SpringView(name = Views.HORIZONTAL_MOVIES_SPLIT_VIEW)
@UIScope
@SuppressWarnings("serial")
public class HorizontalMoviesSplitView extends BaseView {

	/** The Movies View is using the MovieService for data access. */
	@Inject
	private MovieService movieService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.
	 * ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {

		// Apply Vaadin Layout.
		VerticalLayout mainLayout = new DefaultVerticalLayout(true, true);

		// Set form options by convention.
		FormOptions fo = new FormOptions();
		fo.setOpenInViewMode(true).setOpenInViewMode(true).setShowRemoveButton(true).setEditAllowed(true)
				.setShowQuickSearchField(true);

		// A SplitLayout is a component that displays a search screen and an
		// edit form
		ServiceBasedSplitLayout<Integer, Movie> movieLayout = new ServiceBasedSplitLayout<Integer, Movie>(movieService,
				getModelFactory().getModel(Movie.class), QueryType.PAGING, fo,
				new SortOrder("title", SortDirection.ASCENDING)) {

			@Override
			protected Filter constructQuickSearchFilter(String value) {
				// quick search field
				return new SimpleStringFilter("title", value, true, false);
			}
		};

		// Some plumbing.
		mainLayout.addComponent(movieLayout);
		setCompositionRoot(mainLayout);
	}
}
