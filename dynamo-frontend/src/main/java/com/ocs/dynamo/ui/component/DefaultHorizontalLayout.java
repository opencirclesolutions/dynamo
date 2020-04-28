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

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Default horizontal layout
 * 
 * @author bas.rutten
 */
public class DefaultHorizontalLayout extends HorizontalLayout {

    private static final long serialVersionUID = 9070636803023696052L;

    public DefaultHorizontalLayout() {
        this(false, true);
    }

    /**
     * Constructor
     * 
     * @param margin  whether to include a margin
     * @param spacing whether to include spacing
     */
    public DefaultHorizontalLayout(boolean padding, boolean spacing) {
    	setMargin(false);
        setPadding(padding);
        setSpacing(spacing);
    }
}
