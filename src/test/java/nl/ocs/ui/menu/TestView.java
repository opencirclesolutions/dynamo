package nl.ocs.ui.menu;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringView;

import nl.ocs.ui.auth.Authorized;
import nl.ocs.ui.view.BaseView;

@SpringView(name = "Destination 1.1")
@Authorized(roles = { "someRole" }, editOnly = true)
public class TestView extends BaseView {

	private static final long serialVersionUID = 2364788614441398562L;

	@Override
	public void enter(ViewChangeEvent event) {

	}

}
