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
package com.ocs.dynamo.functional.ui;

import javax.inject.Inject;

import com.ocs.dynamo.functional.domain.Domain;
import com.ocs.dynamo.functional.service.DomainService;
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
import com.vaadin.ui.VerticalLayout;

@SpringView
@UIScope
@SuppressWarnings("serial")
public class DomainHorizontalSplitView extends BaseView {

	/** Vaadin vertical layout. */
	private VerticalLayout mainLayout;

	/** The Domain View is using the DomainService for data access. */
	@Inject
	private DomainService domainService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener
	 * .ViewChangeEvent)
	 */
	public void enter(ViewChangeEvent event) {

		// Apply Vaadin Layout.
		mainLayout = new DefaultVerticalLayout(true, true);

		// Set form options by convention.
		FormOptions fo = new FormOptions();
		fo.setOpenInViewMode(true);

		// Add a remove button.
		fo.setShowRemoveButton(true);

		// Add an edit button.
		fo.setShowEditButton(true);
		fo.setShowQuickSearchField(true);

		// A SplitLayout is a component that displays a search screen and an
		// edit form
		ServiceBasedSplitLayout<Integer, Domain> layout = new ServiceBasedSplitLayout<Integer, Domain>(
				domainService, getModelFactory().getModel(Domain.class), fo,
				new SortOrder("name", SortDirection.ASCENDING)) {

			@Override
			protected Filter constructFilter() {
				// TODO Auto-generated method stub
				return super.constructFilter();
			}

			@Override
			protected Filter constructQuickSearchFilter(String value) {
				// quick search field
				return new SimpleStringFilter("name", value, true, false);
			}
		};
		layout.getTableWrapper().getContainer();

		// Add the layout to the main layout
		mainLayout.addComponent(layout);
		setCompositionRoot(mainLayout);
	}
}
