package nl.ocs.ui;

import nl.ocs.domain.AbstractEntity;

/**
 * A subject (also known as an Observable) according to the well-known pattern
 * 
 * @author bas.rutten
 *
 * @param <T>
 *            the type of the entity being observed
 */
public interface Subject<T extends AbstractEntity<?>> {

	/**
	 * Registers an observer
	 * 
	 * @param observer
	 *            the observer to register
	 */
	public void register(Observer<T> observer);

	/**
	 * Unregisters an observer
	 * 
	 * @param observer
	 *            the observer to unregister
	 */
	public void unregister(Observer<T> observer);

	/**
	 * Unregisters all observers
	 */
	public void unregisterAll();

	/**
	 * Notifies all observers
	 * 
	 * @param entity
	 *            the entity that has changed
	 */
	public void notifyObservers(T entity);
}
