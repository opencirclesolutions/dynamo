package nl.ocs.domain.model;

import nl.ocs.service.MessageService;

/**
 * Factory for entity models
 * 
 * @author bas.rutten
 * 
 */
public interface EntityModelFactory {

	public <T> EntityModel<T> getModel(Class<T> entityClass);

	public <T> EntityModel<T> getModel(String reference, Class<T> entityClass);

	public MessageService getMessageService();

	/**
	 * Checks if the factory contains a model for a certain reference
	 * 
	 * @param reference
	 * @return
	 */
	public boolean hasModel(String reference);

}
