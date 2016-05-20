/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.ui.composite.layout;

import java.io.Serializable;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.vaadin.ui.HorizontalLayout;

/**
 * A simple horizontal layout for read-only display of attributes (i.e. for a header bar)
 * 
 * @author bas.rutten
 * @param <ID>
 *            the type of the primary key of the entity
 * @param <T>
 *            the type of the entity being displayed
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
