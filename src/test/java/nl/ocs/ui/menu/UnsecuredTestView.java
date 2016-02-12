package nl.ocs.ui.menu;

import nl.ocs.ui.view.BaseView;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringView;

@SpringView(name = "someDest")
public class UnsecuredTestView extends BaseView {

	private static final long serialVersionUID = 922414016634172918L;

	@Override
	public void enter(ViewChangeEvent event) {
	
	}

}
