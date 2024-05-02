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
package com.ocs.dynamo.ui.menu;

import org.apache.commons.lang3.StringUtils;

import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.UIHelper;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;

import lombok.Getter;

/**
 * Command for navigating to a certain view
 * 
 * @author bas.rutten
 */
public class NavigateCommand<T extends Component, U> implements ComponentEventListener<ClickEvent<T>> {

	private static final long serialVersionUID = 5192333331107840255L;

	private final U rootComponent;

	private final BaseMenuService<T, U> menuService;

	@Getter
	private final String destination;

	@Getter
	private final String selectedTab;

	@Getter
	private final String mode;

	/**
	 * Constructor
	 * 
	 * @param destination the destination to navigate to
	 * @param selectedTab the index of the tab to select
	 * @param mode        an optional screen mode
	 */
	public NavigateCommand(BaseMenuService<T, U> menuService, U rootComponent, String destination, String selectedTab,
			String mode) {
		this.menuService = menuService;
		this.destination = destination;
		this.selectedTab = selectedTab;
		this.rootComponent = rootComponent;
		this.mode = mode;
	}

	@Override
	public void onComponentEvent(ClickEvent<T> event) {
		UIHelper helper = ServiceLocatorFactory.getServiceLocator().getService(UIHelper.class);

		if (selectedTab != null) {
			helper.setSelectedTab(Integer.valueOf(selectedTab));
		} else {
			helper.setSelectedTab(null);
		}
		String lastVisited;
		if (StringUtils.isEmpty(mode)) {
			helper.navigate(destination);
			lastVisited = destination;
		} else {
			helper.navigate(destination, mode);
			lastVisited = destination + "#" + mode;
		}
		menuService.setLastVisited(rootComponent, lastVisited);
	}

}
