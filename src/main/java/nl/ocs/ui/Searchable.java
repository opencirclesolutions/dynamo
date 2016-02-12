package nl.ocs.ui;

import com.vaadin.data.Container.Filter;

/**
 * An interface for objects that can receive search requests
 * 
 * @author bas.rutten
 * 
 */
public interface Searchable {

	/**
	 * Perform the search
	 * 
	 * @param filter
	 *            the filter to apply to the search
	 */
	public void search(Filter filter);

}