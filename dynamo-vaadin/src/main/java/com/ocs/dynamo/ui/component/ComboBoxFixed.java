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

import java.util.List;

/**
 * Sub class of combo to provide a workaround for issue http://dev.vaadin.com/ticket/10544
 * 
 * @author Patrick Deenen (patrick.deenen@opencirclesolutions.nl)
 */
public class ComboBoxFixed extends com.vaadin.ui.ComboBox {

    private static final long serialVersionUID = -4596900792135738280L;

    private boolean inFilterMode;

    @Override
    public void containerItemSetChange(com.vaadin.data.Container.ItemSetChangeEvent event) {
        if (inFilterMode) {
            super.containerItemSetChange(event);
        }
    }

    @Override
    protected List<?> getOptionsWithFilter(boolean needNullSelectOption) {
        try {
            inFilterMode = true;
            return super.getOptionsWithFilter(needNullSelectOption);
        } finally {
            inFilterMode = false;
        }
    }
}
