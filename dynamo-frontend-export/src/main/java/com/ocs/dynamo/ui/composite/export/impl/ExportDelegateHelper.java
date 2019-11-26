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
