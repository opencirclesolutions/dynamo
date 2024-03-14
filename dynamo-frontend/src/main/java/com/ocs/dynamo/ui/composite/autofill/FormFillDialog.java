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
package com.ocs.dynamo.ui.composite.autofill;

import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.dialog.BaseModalDialog;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.ai.formfiller.services.ChatGPTChatCompletionService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * A dialog that can be used to provide input and instructions for automatically
 * filling a form using a LLM (Large Language Model) service
 */
@Slf4j
public class FormFillDialog extends BaseModalDialog {

    private TextArea content;

    private TextArea context;

    public FormFillDialog(Component targetComponent, EntityModel<?> entityModel,
                          ModelBasedEditForm<?, ?> form) {
        setTitle(message("ocs.fill.form"));
        buildMainLayout(entityModel);
        buildButtonBar(targetComponent, entityModel, form);
    }

    private void buildButtonBar(Component targetComponent, EntityModel<?> entityModel, ModelBasedEditForm<?, ?> form) {
        setBuildButtonBar(buttonBar -> {
            Button fillButton = new Button(message("ocs.fill.button"));
            fillButton.setIcon(VaadinIcon.MAGIC.create());
            fillButton.addClickListener(event -> {

                if (StringUtils.isEmpty(content.getValue())) {
                    VaadinUtils.showErrorNotification(message("ocs.fill.contents.not.empty"));
                    return;
                }

                Map<Component, String> instructions = extractInstructions(entityModel, form);

                List<String> contextInstructions = new ArrayList<>();
                if (context.getValue() != null) {
                    contextInstructions.add(context.getValue());
                }

                try {
                    FormFiller formFiller = new FormFiller(targetComponent, instructions, contextInstructions, new ChatGPTChatCompletionService());
                    formFiller.fill(content.getValue(), entityModel);
                    this.close();
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    VaadinUtils.showErrorNotification(ex.getMessage());
                }
            });
            buttonBar.add(fillButton);

            Button cancelButton = new Button(message("ocs.cancel"));
            cancelButton.setIcon(VaadinIcon.BAN.create());
            cancelButton.addClickListener(event -> this.close());
            buttonBar.add(cancelButton);
        });
    }

    private void buildMainLayout(EntityModel<?> entityModel) {
        setBuildMainLayout(parent -> {
            VerticalLayout layout = new DefaultVerticalLayout();

            content = new TextArea();
            content.setLabel(message("ocs.fill.form.contents"));
            content.setWidth("100%");
            content.setHeight("30vh");
            layout.add(content);

            context = new TextArea();
            context.setLabel(message("ocs.fill.form.additional"));
            if (!StringUtils.isEmpty(entityModel.getAutofillInstructions())) {
                context.setValue(entityModel.getAutofillInstructions());
            }
            context.setWidth("100%");
            content.setHeight("30vh");
            layout.add(context);

            parent.add(layout);
        });
    }

    @NotNull
    private Map<Component, String> extractInstructions(EntityModel<?> entityModel, ModelBasedEditForm<?, ?> form) {
        return entityModel.getAttributeModels().stream().filter(
                am -> !StringUtils.isEmpty(am.getAutoFillInstructions())
        ).filter(am -> form.getField(am.getPath()) != null).collect(toMap(am -> form.getField(am.getPath()), am -> am.getAutoFillInstructions()
        ));
    }

}

