package nl.ocs.filter.listener;

import java.io.Serializable;

/**
 * Interface for components that have to respond to a filter change
 * 
 * @author bas.rutten
 * 
 */
public interface FilterListener extends Serializable {

	/**
	 * Method that is called when a change of a filter occurs
	 * 
	 * @param event
	 *            the filter change event
	 */
	public void onFilterChange(FilterChangeEvent event);
}
