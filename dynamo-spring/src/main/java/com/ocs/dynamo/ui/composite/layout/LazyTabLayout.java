package com.ocs.dynamo.ui.composite.layout;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.ui.Reloadable;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

/**
 * A layout that contains a tab sheet with tabs that are lazily loaded
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
@SuppressWarnings("serial")
public abstract class LazyTabLayout<ID extends Serializable, T extends AbstractEntity<ID>>
        extends BaseCustomComponent {

	private static final long serialVersionUID = 3788799136302802727L;

	// the entity whose details are being shown
	private T entity;

	private Set<String> replacedTabs = new HashSet<>();

	private TabSheet tabs;

	/**
	 * Constructor
	 * 
	 * @param entity
	 */
	public LazyTabLayout(T entity) {
		this.entity = entity;
	}

	@Override
	public void attach() {
		super.attach();
		build();
	}

	@Override
	public void build() {
		Panel panel = new Panel();
		panel.setCaptionAsHtml(true);
		panel.setCaption(createTitle());

		VerticalLayout main = new DefaultVerticalLayout(true, true);
		panel.setContent(main);

		tabs = new TabSheet();
		tabs.setSizeFull();

		main.addComponent(tabs);
		setupLazySheet(tabs);
		setCompositionRoot(panel);
	}

	/**
	 * Returns the captions of the tabs
	 * 
	 * @return
	 */
	protected abstract String[] getTabCaptions();

	/**
	 * Lazily creates a certain tab
	 * 
	 * @param index
	 *            the index of the tab to create
	 * @return
	 */
	protected abstract Component initTab(int index);

	private void setupLazySheet(final TabSheet tabs) {

		// build up placeholder tabs that only contain an empty layout
		for (String caption : getTabCaptions()) {
			tabs.addTab(new DefaultVerticalLayout(false, false), caption);
		}

		// load the first tab
		((Layout) tabs.getTab(0).getComponent()).addComponent(initTab(0));
		replacedTabs.add(tabs.getTab(0).getCaption());

		// respond to a tab change by actually loading the sheet
		tabs.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				Component selectedTab = event.getTabSheet().getSelectedTab();
				Tab tab = event.getTabSheet().getTab(selectedTab);

				// lazily load a tab
				if (!replacedTabs.contains(tab.getCaption())) {
					replacedTabs.add(tab.getCaption());

					// look up the tab in the copies
					int index = 0;
					for (int i = 0; i < tabs.getComponentCount(); i++) {
						Tab t = tabs.getTab(i);
						if (t.getCaption().equals(tab.getCaption())) {
							index = i;
							break;
						}
					}

					// paste the real tab into the placeholder
					Component realTab = initTab(index);
					((Layout) selectedTab).addComponent(realTab);

				} else {
					// reload the tab if needed
					Layout layout = (Layout) selectedTab;
					Component next = layout.iterator().next();
					if (next instanceof Reloadable) {
						((Reloadable) next).reload();
					}
				}

			}
		});
	}

	/**
	 * Constructs the title of the page
	 * 
	 * @return
	 */
	protected abstract String createTitle();

	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}

	public void selectTab(int index) {
		tabs.setSelectedTab(index);
	}
}
