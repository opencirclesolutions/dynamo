package nl.ocs.ui.component;

import java.util.List;

/**
 * Sub class of combo to provide a workaround for issue
 * http://dev.vaadin.com/ticket/10544
 * 
 * @author Patrick Deenen (patrick.deenen@opencirclesolutions.nl)
 *
 */
public class ComboBox extends com.vaadin.ui.ComboBox {

	private static final long serialVersionUID = -4596900792135738280L;

	private boolean inFilterMode;

	@Override
	public void containerItemSetChange(com.vaadin.data.Container.ItemSetChangeEvent event) {
		if (inFilterMode) {
			super.containerItemSetChange(event);
		}
	}

	@Override
	protected List<?> getOptionsWithFilter(boolean needNullSelectOption) {
		try {
			inFilterMode = true;
			return super.getOptionsWithFilter(needNullSelectOption);
		} finally {
			inFilterMode = false;
		}
	}
}
