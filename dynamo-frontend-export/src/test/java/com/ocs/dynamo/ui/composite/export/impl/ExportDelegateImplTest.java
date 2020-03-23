package com.ocs.dynamo.ui.composite.export.impl;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.composite.export.ExportService;
import com.ocs.dynamo.ui.composite.type.ExportMode;

@Disabled
public class ExportDelegateImplTest extends BaseMockitoTest {

    @BeforeEach
    public void setup() {
        MockVaadin.setup();
    }

    @Mock
    private ExportService exportService;

    private ExportDelegateImpl delegate = new ExportDelegateImpl();

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    @Test
    public void testExport() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        delegate.export(em, ExportMode.FULL, null, null);
    }

    @Test
    public void testExportFixed() {
        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
        delegate.exportFixed(em, ExportMode.FULL, new ArrayList<>());
    }
}
