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

import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.vaadin.data.sort.SortOrder;

/**
 * A tabular edit layout that keeps a reference to a parent object of the collection being edited
 * 
 * @author bas.rutten
 * @param <ID>
 * @param <T>
 * @param <ID2>
 * @param
 *            <Q>
 */
public abstract class TabularDetailEditLayout<ID extends Serializable, T extends AbstractEntity<ID>, ID2 extends Serializable, Q extends AbstractEntity<ID2>>
        extends TabularEditLayout<ID, T> {

    private static final long serialVersionUID = -3432301286152665223L;

    private final BaseService<ID2, Q> parentService;

    private Q parentEntity;

    /**
     * Constructor
     * 
     * @param service
     * @param parentEntity
     * @param parentService
     * @param entityModel
     * @param formOptions
     * @param sortOrder
     * @param joins
     */
    public TabularDetailEditLayout(BaseService<ID, T> service, Q parentEntity,
            BaseService<ID2, Q> parentService, EntityModel<T> entityModel, FormOptions formOptions,
            SortOrder sortOrder, FetchJoinInformation... joins) {
        super(service, entityModel, formOptions, sortOrder, joins);
        this.parentService = parentService;
        this.parentEntity = parentEntity;
    }

    public void setParentEntity(Q parentEntity) {
        this.parentEntity = parentEntity;
    }

    public Q getParentEntity() {
        return parentEntity;
    }

    public BaseService<ID2, Q> getParentService() {
        return parentService;
    }

}
