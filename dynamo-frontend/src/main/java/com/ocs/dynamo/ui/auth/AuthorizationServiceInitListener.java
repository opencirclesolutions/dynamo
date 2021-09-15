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
package com.ocs.dynamo.ui.auth;

import org.springframework.beans.factory.annotation.Autowired;

import com.ocs.dynamo.constants.DynamoConstants;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthorizationServiceInitListener implements VaadinServiceInitListener {

	private static final long serialVersionUID = -6625937066463454631L;

	@Autowired
	private PermissionChecker permissionChecker;

	@Override
	public void serviceInit(ServiceInitEvent event) {
		event.getSource().addUIInitListener(uiEvent -> {
			UI ui = uiEvent.getUI();
			ui.addBeforeEnterListener(this::beforeEnter);
		});
	}

	/**
	 * Reroutes the user if they are not authorized to access the view.
	 *
	 * @param event before navigation event with event details
	 */
	private void beforeEnter(BeforeEnterEvent event) {
		String view = event.getLocation().getPath();
		if (!permissionChecker.isAccessAllowed(view)) {
			log.warn("Detected invalid access to view {}", view);
			event.rerouteTo(DynamoConstants.ERROR_VIEW);
		}
	}

}
