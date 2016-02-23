package com.ocs.dynamo.ui.menu;

import com.ocs.dynamo.ui.auth.Authorized;
import com.ocs.dynamo.ui.view.BaseView;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringView;

@SpringView(name = "Destination 1.1")
@Authorized(roles = { "someRole" }, editOnly = true)
public class TestView extends BaseView {

	private static final long serialVersionUID = 2364788614441398562L;

	@Override
	public void enter(ViewChangeEvent event) {

	}

}
