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
package com.ocs.dynamo.ui.component;

import org.apache.commons.lang.StringUtils;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.utils.StringUtil;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.BorderStyle;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;

/**
 * A custom field for displaying a clickable URL
 * 
 * @author bas.rutten
 *
 */
public class URLField extends CustomField<String> {

    private static final long serialVersionUID = -1899451186343723434L;

    private AttributeModel attributeModel;

    private HorizontalLayout bar;

    private Link link;

    public URLField(AttributeModel attributeModel) {
        this.attributeModel = attributeModel;
    }

    protected Link getLink() {
        return link;
    }

    @Override
    public Class<? extends String> getType() {
        return String.class;
    }

    @Override
    protected Component initContent() {
        bar = new DefaultHorizontalLayout(false, true);
        setCaption(attributeModel.getDisplayName());
        updateLink(getValue());
        return bar;
    }

    @Override
    protected void setInternalValue(String newValue) {
        super.setInternalValue(newValue);
        updateLink(newValue);
    }

    @Override
    public void setValue(String newValue) {
        super.setValue(newValue);
        updateLink(newValue);
    }

    /**
     * Updates the field value - renders a clickable URL if the field value is not empty
     * 
     * @param value
     */
    private void updateLink(String value) {
        if (bar != null) {
            bar.removeAllComponents();
            if (!StringUtils.isEmpty(value)) {
                String temp = StringUtil.prependProtocol(value);
                link = new Link(temp, new ExternalResource(temp), "_blank", 0, 0,
                        BorderStyle.DEFAULT);
                bar.addComponent(link);
            } else {
                link = null;
            }
        }
    }

}
