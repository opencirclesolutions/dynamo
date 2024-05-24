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
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.vaadin.flow.data.provider.SortOrder;

/**
 * A split layout that displays a fixed set of items related to a parent entity
 *
 * @param <ID>  the type of the primary key
 * @param <T>   the type of the entity
 * @param <ID2> the type of the primary key of the parent entity
 * @param <Q>   the type of the parent entity
 * @author bas.rutten
 */
public class FixedDetailLayout<ID extends Serializable, T extends AbstractEntity<ID>, ID2 extends Serializable, Q extends AbstractEntity<ID2>>
        extends FixedSplitLayout<ID, T> implements CanAssignEntity<ID2, Q> {

    private static final long serialVersionUID = 4606800218149558500L;

    private final BaseService<ID2, Q> parentService;

    private Q parentEntity;

    /**
     * Constructor
     *
     * @param service       the service used to interact with the database
     * @param parentEntity  the parent entity
     * @param parentService the service used to interact with the paren entity
     * @param entityModel   the entity model that is used to build the form
     * @param formOptions   form options
     * @param sortOrder     the sort order
     */
    public FixedDetailLayout(BaseService<ID, T> service, Q parentEntity, BaseService<ID2, Q> parentService,
                             EntityModel<T> entityModel, FormOptions formOptions, SortOrder<?> sortOrder) {
        super(service, entityModel, formOptions, sortOrder);
        this.parentEntity = parentEntity;
        this.parentService = parentService;
    }

    public Q getParentEntity() {
        return parentEntity;
    }

    public void setParentEntity(Q parentEntity) {
        this.parentEntity = parentEntity;
    }

    public BaseService<ID2, Q> getParentService() {
        return parentService;
    }

    @Override
    public void assignEntity(Q parentEntity) {
        setParentEntity(parentEntity);
    }
}
