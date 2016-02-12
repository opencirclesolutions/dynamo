package nl.ocs.ui.composite.layout;

import java.io.Serializable;

import nl.ocs.domain.AbstractEntity;
import nl.ocs.domain.model.AttributeModel;
import nl.ocs.domain.model.AttributeType;
import nl.ocs.domain.model.EntityModel;
import nl.ocs.service.BaseService;
import nl.ocs.ui.component.DefaultHorizontalLayout;
import nl.ocs.ui.composite.form.FormOptions;

import com.vaadin.ui.HorizontalLayout;

/**
 * A simple horizontal layout for read-only display of attributes (i.e. for a
 * header bar)
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 * @param <T>
 */
public class HorizontalDisplayLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends
		BaseServiceCustomComponent<ID, T> {

	private static final long serialVersionUID = -2610435729199505546L;

	private T entity;

	/**
	 * Constructor
	 * 
	 * @param service
	 *            the service used to query the database
	 * @param entityModel
	 *            the entity model of the entity to display
	 * @param entity
	 *            the entity to display
	 * @param formOptions
	 *            the form options that govern how the layout is displayed
	 */
	public HorizontalDisplayLayout(BaseService<ID, T> service, EntityModel<T> entityModel,
			T entity, FormOptions formOptions) {
		super(service, entityModel, formOptions);
		this.entity = entity;
	}

	@Override
	public void attach() {
		super.attach();
		build();
	}

	@Override
	public void build() {
		HorizontalLayout layout = new DefaultHorizontalLayout(false, true);

		for (AttributeModel attributeModel : getEntityModel().getAttributeModels()) {
			if (attributeModel.isVisible()
					&& AttributeType.BASIC.equals(attributeModel.getAttributeType())) {
				layout.addComponent(constructLabel(entity, attributeModel));
			}
		}

		setCompositionRoot(layout);
	}
}
