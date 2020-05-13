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
package com.ocs.dynamo.ui.composite.export.impl;

import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.composite.export.CustomXlsStyleGenerator;
import com.ocs.dynamo.ui.composite.export.ExportDelegate;

/**
 * A helper class for registering custom style generators
 * 
 * @author Bas Rutten
 *
 */
public final class ExportDelegateHelper {

    private ExportDelegateHelper() {
        // hidden constructor
    }

    /**
     * Adds a custom style generator
     * 
     * @param entityModel the entity model for which to add the generator
     * @param generator   the generator
     */
    public static void addCustomStyleGenerator(EntityModel<?> entityModel, CustomXlsStyleGenerator<?, ?> generator) {
        ExportDelegateImpl delegate = (ExportDelegateImpl) ServiceLocatorFactory.getServiceLocator().getService(ExportDelegate.class);
        delegate.addCustomStyleGenerator(entityModel, generator);
    }
}
