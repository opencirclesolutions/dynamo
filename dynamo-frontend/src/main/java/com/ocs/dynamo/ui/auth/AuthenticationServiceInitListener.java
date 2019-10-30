package com.ocs.dynamo.ui.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ocs.dynamo.constants.DynamoConstants;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

public class AuthenticationServiceInitListener implements VaadinServiceInitListener {

    private static final long serialVersionUID = -6625937066463454631L;

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationServiceInitListener.class);

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
            LOG.warn("Detected invalid access to view {}", view);
            event.rerouteTo(DynamoConstants.ERROR_VIEW);
        }
    }

}
