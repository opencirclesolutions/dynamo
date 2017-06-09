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
package com.ocs.dynamo.ui.view;

/**
 * A base class for a "lazy" view that is only constructed once per UI - data will not be
 * reloaded once the view is opened again
 */
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;

public abstract class LazyBaseView extends BaseView {

	private static final long serialVersionUID = -2500168085668166838L;

	private Layout lazy = null;

	/**
	 * Method that is called when the view is entered - lazily constructs the layout
	 */
	@Override
	public final void enter(ViewChangeEvent event) {
		if (lazy == null) {
			lazy = initLayout();
			lazy.addComponent(build());
			setCompositionRoot(lazy);
			afterBuild();
		} else {
			refresh();
		}
	}

	/**
	 * Constructs the view
	 * 
	 * @return the parent component of the constructed view
	 */
	protected abstract Component build();

	/**
	 * Refreshes the screen after it is re-opened
	 */
	protected void refresh() {
		// override in subclasses
	}
	
	protected void afterBuild() {
		
	}
}
