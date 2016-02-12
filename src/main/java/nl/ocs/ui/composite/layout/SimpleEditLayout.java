package nl.ocs.ui.composite.layout;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import nl.ocs.domain.AbstractEntity;
import nl.ocs.domain.model.AttributeModel;
import nl.ocs.domain.model.EntityModel;
import nl.ocs.service.BaseService;
import nl.ocs.ui.Reloadable;
import nl.ocs.ui.component.DefaultVerticalLayout;
import nl.ocs.ui.composite.form.FormOptions;
import nl.ocs.ui.composite.form.ModelBasedEditForm;
import nl.ocs.ui.composite.type.ScreenMode;

import com.vaadin.data.Container.Filter;
import com.vaadin.ui.Field;
import com.vaadin.ui.VerticalLayout;

/**
 * A layout for editing a single object
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
@SuppressWarnings("serial")
public class SimpleEditLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends
		BaseServiceCustomComponent<ID, T> implements Reloadable {

	private static final long serialVersionUID = -7935358582100755140L;

	private ModelBasedEditForm<ID, T> editForm;

	private T entity;

	private VerticalLayout main;

	private Map<String, Filter> fieldFilters = new HashMap<>();

	/**
	 * Constructor
	 * 
	 * @param entity
	 * @param service
	 * @param entityModel
	 * @param formOptions
	 */
	public SimpleEditLayout(T entity, BaseService<ID, T> service, EntityModel<T> entityModel,
			FormOptions formOptions) {
		super(service, entityModel, formOptions);
		this.entity = entity;
	}

	@Override
	public void attach() {
		super.attach();
		build();
	}

	/**
	 * Constructs the screen - this method is called just once
	 */
	@Override
	public void build() {
		main = new DefaultVerticalLayout(true, true);

		// if opening in edit mode, the cancel button is useless since there is
		// nothing to cancel or go back to
		if (!getFormOptions().isOpenInViewMode()) {
			getFormOptions().setHideCancelButton(true);
		}
		// there is just one component here, so the screen mode is always
		// vertical
		getFormOptions().setScreenMode(ScreenMode.VERTICAL);

		editForm = new ModelBasedEditForm<ID, T>(entity, getService(), getEntityModel(),
				getFormOptions(), fieldFilters) {
			@Override
			protected void afterEditDone(boolean cancel, boolean newObject, T entity) {
				setEntity(entity);
				SimpleEditLayout.this.afterEditDone(cancel, newObject, entity);
			}

			@Override
			protected Field<?> constructCustomField(EntityModel<T> entityModel,
					AttributeModel attributeModel, boolean viewMode) {
				return SimpleEditLayout.this.constructCustomField(entityModel, attributeModel,
						viewMode, false);
			}

			@Override
			protected String[] getParentGroupHeaders() {
				return SimpleEditLayout.this.getParentGroupHeaders();
			}

			@Override
			protected String getParentGroup(String childGroup) {
				return SimpleEditLayout.this.getParentGroup(childGroup);
			}

			@Override
			protected boolean isEditAllowed() {
				return SimpleEditLayout.this.isEditAllowed();
			}

			@Override
			protected void postProcessEditFields() {
				SimpleEditLayout.this.postProcessEditFields(editForm);
			}

			@Override
			protected void afterModeChanged(boolean viewMode) {
				SimpleEditLayout.this.afterModeChanged(viewMode, editForm);
			}

		};
		editForm.build();

		main.addComponent(editForm);

		setCompositionRoot(main);
	}

	/**
	 * Creates a new entity - override in subclass if needed
	 * 
	 * @return
	 */
	protected T createEntity() {
		return getService().createNewEntity();
	}

	/**
	 * 
	 * @param editForm
	 */
	protected void postProcessEditFields(ModelBasedEditForm<ID, T> editForm) {
		// do nothing by default - override in subclasses
	}

	/**
	 * Method that is called after the mode is changes
	 * 
	 * @param viewMode
	 *            the new view mode
	 * @param editForm
	 */
	protected void afterModeChanged(boolean viewMode, ModelBasedEditForm<ID, T> editForm) {
		// override in subclasses
	}

	/**
	 * Returns a list of additional group headers that can be used to apply an
	 * extra layer to the layout
	 * 
	 * @return
	 */
	protected String[] getParentGroupHeaders() {
		// overwrite in subclasses if needed
		return null;
	}

	/**
	 * Returns the parent group (which must be returned by the
	 * getParentGroupHeaders method) to which a certain child group belongs
	 * 
	 * @param childGroup
	 * @return
	 */
	protected String getParentGroup(String childGroup) {
		// overwrite in subclasses if needed
		return null;
	}

	@Override
	public void reload() {

		// reset to view mode
		if (getFormOptions().isOpenInViewMode()) {
			editForm.setViewMode(true);
		}

		if (entity.getId() != null) {
			setEntity(getService().fetchById(entity.getId()));
		}
	}

	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
		editForm.setEntity(entity);
	}

	/**
	 * Method that is called after the user has completed (or cancelled) an edit
	 * action
	 * 
	 * @param newObject
	 * @param entity
	 */
	protected void afterEditDone(boolean cancel, boolean newObject, T entity) {
		// reset to view mode
		if (getFormOptions().isOpenInViewMode()) {
			editForm.setViewMode(true);
		}

		if (entity.getId() != null) {
			setEntity(getService().fetchById(entity.getId()));
		}
	}

	public ModelBasedEditForm<ID, T> getEditForm() {
		return editForm;
	}

	public Map<String, Filter> getFieldFilters() {
		return fieldFilters;
	}

	public void setFieldFilters(Map<String, Filter> fieldFilters) {
		this.fieldFilters = fieldFilters;
	}

	protected boolean isEditAllowed() {
		return true;
	}

}
