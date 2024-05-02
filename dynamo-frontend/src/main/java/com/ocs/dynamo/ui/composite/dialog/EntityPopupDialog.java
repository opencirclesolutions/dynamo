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
package com.ocs.dynamo.ui.composite.dialog;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.composite.ComponentContext;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.layout.SimpleEditLayout;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.TriConsumer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.function.SerializablePredicate;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A pop-up dialog for adding a new entity or viewing the details of an existing
 * entry
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T>  the type of the entity
 * @author bas.rutten
 */
public class EntityPopupDialog<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseModalDialog {

    private static final long serialVersionUID = -2012972894321597214L;

    @Getter
    @Setter
    private TriConsumer<Boolean, Boolean, T> afterEditDone;

    private final ComponentContext<ID, T> componentContext;

    @Getter
    private BaseService<ID, T> service;

    @Getter
    @Setter
    private Supplier<T> createEntity = () -> service.createNewEntity();

    private final T entity;

    private final EntityModel<T> entityModel;

    @Getter
    private final Map<String, SerializablePredicate<?>> fieldFilters;

    @Getter
    private final FormOptions formOptions;

    @Getter
    private final FetchJoinInformation[] joins;

    @Getter
    private SimpleEditLayout<ID, T> layout;

    private final MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

    @Getter
    private Button okButton;

    @Getter
    @Setter
    private BiConsumer<FlexLayout, Boolean> postProcessDetailButtonBar;

    /**
     * @param service          the service used for communicating with the database
     * @param entity           the entity to display
     * @param entityModel      the entity model of the entity
     * @param fieldFilters     field filters to apply to the individual components
     * @param formOptions      the form options
     * @param componentContext the component context
     * @param joins            any joins to apply when querying the database
     */
    public EntityPopupDialog(BaseService<ID, T> service, T entity, EntityModel<T> entityModel,
                             Map<String, SerializablePredicate<?>> fieldFilters, FormOptions formOptions,
                             ComponentContext<ID, T> componentContext, FetchJoinInformation... joins) {
        this.service = service;
        this.entityModel = entityModel;
        this.formOptions = formOptions;
        this.entity = entity;
        this.fieldFilters = fieldFilters;
        this.joins = joins;
        this.componentContext = componentContext.toBuilder().popup(true).build();
        setTitle(entityModel.getDisplayName(VaadinUtils.getLocale()));

        setBuildButtonBar(buttonBar -> {
            buttonBar.setVisible(formOptions.isReadOnly());
            if (formOptions.isReadOnly()) {
                okButton = new Button(messageService.getMessage("ocs.ok", VaadinUtils.getLocale()));
                okButton.addClickListener(event -> close());
                buttonBar.add(okButton);
            }
        });
        buildMain();
    }

    /**
     * @param service      the service used for communicating with the database
     * @param entity       the entity to display
     * @param entityModel  the entity model of the entity
     * @param fieldFilters field filters to apply to the individual components
     * @param formOptions  the form options
     * @param joins        any joins to apply when querying the database
     */
    public EntityPopupDialog(BaseService<ID, T> service, T entity, EntityModel<T> entityModel,
                             Map<String, SerializablePredicate<?>> fieldFilters, FormOptions formOptions,
                             FetchJoinInformation... joins) {
        this(service, entity, entityModel, fieldFilters, formOptions, ComponentContext.<ID, T>builder().build(), joins);
    }

    /**
     * Adds a custom entity model for a specified field
     * @param path the path to the field
     * @param reference the reference of the entity model
     */
    public void addFieldEntityModel(String path, String reference) {
        componentContext.addFieldEntityModel(path, reference);
    }

    public T getEntity() {
        return layout.getEntity();
    }

    public List<Button> getSaveButtons() {
        return layout.getEditForm().getSaveButtons();
    }

    public void setColumnThresholds(List<String> thresholds) {
        componentContext.setEditColumnThresholds(thresholds);
    }

    public void setPostProcessEditFields(Consumer<ModelBasedEditForm<ID, T>> postProcessEditFields) {
        componentContext.setPostProcessEditFields(postProcessEditFields);
    }

    private void buildMain() {
        setBuildMainLayout(parent -> {
            formOptions.setShowCancelButton(true);

            layout = new SimpleEditLayout<>(entity, service, entityModel, formOptions);
            layout.setComponentContext(componentContext);
            layout.setAfterEditDone((cancel, isNew, ent) -> {
                if (getAfterEditDone() != null) {
                    getAfterEditDone().accept(cancel, isNew, ent);
                }
                EntityPopupDialog.this.close();
            });
            layout.setCreateEntity(getCreateEntity());
            layout.setPostProcessButtonBar(getPostProcessDetailButtonBar());
            layout.setFieldFilters(fieldFilters);
            layout.setJoins(joins);
            parent.add(layout);
        });
    }
}
